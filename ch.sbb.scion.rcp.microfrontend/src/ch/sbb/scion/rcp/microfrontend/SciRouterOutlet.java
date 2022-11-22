package ch.sbb.scion.rcp.microfrontend;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import ch.sbb.scion.rcp.microfrontend.browser.JavaCallback;
import ch.sbb.scion.rcp.microfrontend.browser.JavaScriptExecutor;
import ch.sbb.scion.rcp.microfrontend.internal.ContextInjectors;
import ch.sbb.scion.rcp.microfrontend.internal.Resources;
import ch.sbb.scion.rcp.microfrontend.model.Application;
import ch.sbb.scion.rcp.microfrontend.model.IDisposable;
import ch.sbb.scion.rcp.microfrontend.model.MessageHeaders;
import ch.sbb.scion.rcp.microfrontend.proxy.RouterOutletProxy;
import ch.sbb.scion.rcp.microfrontend.script.Scripts;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Helpers;

/**
 * Widget to display a microfrontend.
 * 
 * This widget acts as proxy for the SCION <sci-router-outlet> web component.
 * 
 * @see https://scion-microfrontend-platform-api.vercel.app/classes/SciRouterOutletElement.html
 */
public class SciRouterOutlet extends Composite implements DisposeListener {

  private RouterOutletProxy routerOutletProxy;
  private Browser browser;
  private URL url;
  private IDisposable navigator;
  private IDisposable keystrokeDispatcher;
  private List<LoadListener> loadListeners = new ArrayList<>();
  private List<UnloadListener> unloadListeners = new ArrayList<>();
  private Set<String> keystrokes = new HashSet<>();
  private boolean bridgeLoggerEnabled = false;

  @Inject
  private SciMessageClient messageClient;

  @Inject
  private SciManifestService manifestService;

  public SciRouterOutlet(Composite parent, int style, String outletName) {
    super(parent, style);
    ContextInjectors.inject(this);

    setLayout(new FillLayout());
    addDisposeListener(this);

    routerOutletProxy = new RouterOutletProxy(outletName);

    browser = new Browser(this, SWT.EDGE);
    browser.addProgressListener(new ProgressAdapter() {

      private List<IDisposable> disposables = new ArrayList<>();

      public void completed(ProgressEvent event) {
        // is invoked when completed loading the app, or when reloading it, e.g., due to
        // hot code replacement during development
        disposables.forEach(IDisposable::dispose);
        disposables.clear();

        browser.execute(Resources.readString("js/helpers.js"));
        var clientToSciRouterOutletMessageDispatcher = installClientToSciRouterOutletMessageDispatcher();
        var sciRouterOutletToClientMessageDispatcher = installSciRouterOutletToClientMessageDispatcher();

        disposables.add(clientToSciRouterOutletMessageDispatcher);
        disposables.add(sciRouterOutletToClientMessageDispatcher);
      };
    });
    navigator = installRouter(outletName);
    keystrokeDispatcher = installKeystrokeDispatcher();
  }

  private IDisposable installRouter(String outletName) {
    var subscription = messageClient.subscribe("sci-router-outlets/" + outletName + "/url", event -> {
      var prevUrl = url;
      if (prevUrl != null) {
        unloadListeners.forEach(listener -> listener.onUnload(url));
      }

      try {
        url = new URL(Optional.ofNullable(event.body).orElse("about:blank"));

        if (url.equals(prevUrl)) {
          return;
        }

        var pushStateToSessionHistoryStack = Optional.ofNullable(event.headers.get("ɵPUSH_STATE_TO_SESSION_HISTORY_STACK")).orElse(false);
        new JavaScriptExecutor(browser, Resources.readString("js/sci-router-outlet/navigate.js"))
            .replacePlaceholder("url", url)
            .replacePlaceholder("pushStateToSessionHistoryStack", pushStateToSessionHistoryStack)
            .execute()
            .thenRun(() -> loadListeners.forEach(listener -> listener.onLoad(url)));
      }
      catch (MalformedURLException e) {
        Platform.getLog(SciRouterOutlet.class).error(String.format("Failed to navigate in outlet [outlet=%s]", outletName), e);
      }
    });

    return () -> subscription.unsubscribe();
  }

  private IDisposable installKeystrokeDispatcher() {
    return routerOutletProxy.onKeystroke(event -> {
      Platform.getLog(SciRouterOutlet.class).info(String.format("TODO: Dispatching event to SWT [type=%s, keyCode=%s, character=%s, stateMask=%s]", event.type, event.keyCode, event.character, event.stateMask));
      // TODO Dispatching event to SWT (getDisplay().post(event))
    });
  }

  public IDisposable onLoad(LoadListener listener) {
    loadListeners.add(listener);
    return () -> loadListeners.remove(listener);
  }

  public IDisposable onUnload(UnloadListener listener) {
    unloadListeners.add(listener);
    return () -> unloadListeners.remove(listener);
  }

  /**
   * Instructs the web application loaded into the outlet to dispatch given
   * keystrokes.
   */
  public CompletableFuture<Void> registerKeystroke(String keystroke) {
    return registerKeystrokes(Set.of(keystroke));
  }

  /**
   * Instructs the web application loaded into the outlet to dispatch given
   * keystrokes.
   */
  public CompletableFuture<Void> registerKeystrokes(Set<String> keystrokes) {
    this.keystrokes.addAll(keystrokes);
    return routerOutletProxy.registerKeystrokes(this.keystrokes);
  }

  public CompletableFuture<Void> unregisterKeystroke(String keystroke) {
    return unregisterKeystrokes(Set.of(keystroke));
  }

  public CompletableFuture<Void> unregisterKeystrokes(Set<String> keystrokes) {
    this.keystrokes.removeAll(keystrokes);
    return routerOutletProxy.registerKeystrokes(this.keystrokes);
  }

  /**
   * Makes contextual data available to embedded content. Embedded content can lookup contextual data using the {@link ContextService}.
   * Contextual data must be serializable with the structured clone algorithm.
   * 
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/SciRouterOutletElement.html#setContextValue
   */
  public CompletableFuture<Void> setContextValue(String name, Object value) {
    return routerOutletProxy.setContextValue(name, value);
  }

  /**
   * Removes data registered under the given key from the context.
   *
   * Removal does not affect parent contexts, so it is possible that a subsequent call to {@link ContextService.observe$} with the same name
   * will return a non-null result, due to a value being stored in a parent context.
   *
   * @return `true` if removed the value from the outlet context; otherwise `false`.
   * 
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/SciRouterOutletElement.html#removeContextValue
   */
  public CompletableFuture<Boolean> removeContextValue(String name) {
    return routerOutletProxy.removeContextValue(name);
  }

  /**
   * Taps messages from the client and dispatches them to the <sci-router-outlet>.
   */
  private IDisposable installClientToSciRouterOutletMessageDispatcher() {
    var disposables = new ArrayList<IDisposable>();
    manifestService.getApplications().thenAccept(applications -> {
      var trustedOrigins = getTrustedOrigins(applications);
      new JavaCallback(browser, args -> {
        var base64json = (String) args[0];
        var origin = (String) args[1];
        var sender = (String) args[2];

        // Reject messages from untrusted origins. If not trusted, ignore the message and uninstall the bridge.
        // Since we are bridging messages from the client to the host, the "SCION Microfrontend Platform" host
        // cannot perform the "actual" origin check as we dispatch messages to the host under the host's origin.
        // For that reason, we need to check the "actual" origin prior to dispatching the message to the host.
        // Also, when registering the applications, we set the applications "actual" message origin to the origin
        // of the host as SCION would reject messages otherwise.
        if (sender == null && !trustedOrigins.containsValue(origin)) {
          Platform.getLog(SciRouterOutlet.class).info(String.format("[BLOCKED] Request blocked. Wrong origin [actual='%s', expectedOneOf='%s', envelope='%s']", origin, trustedOrigins.values(), decodeBase64(base64json)));
          disposables.forEach(IDisposable::dispose);
        }
        else if (sender != null && !trustedOrigins.containsKey(sender)) {
          Platform.getLog(SciRouterOutlet.class).info(String.format("[BLOCKED] Request blocked. Unknown application [app='%s', envelope='%s']", sender, decodeBase64(base64json)));
          disposables.forEach(IDisposable::dispose);
        }
        else if (sender != null && !origin.equals(trustedOrigins.get(sender))) {
          Platform.getLog(SciRouterOutlet.class).info(String.format("[BLOCKED] Request blocked. Wrong origin [app='%s', actual='%s', expected='%s', envelope='%s']", sender, origin, trustedOrigins.get(sender), decodeBase64(base64json)));
          disposables.forEach(IDisposable::dispose);
        }
        else {
          if (bridgeLoggerEnabled) {
            Platform.getLog(SciRouterOutlet.class).info("[SciBridge] [client=>host] " + decodeBase64(base64json));
          }
          routerOutletProxy.postJsonMessage(base64json);
        }
      })
          .addTo(disposables)
          .install()
          .thenAccept(callback -> {
            var uuid = UUID.randomUUID();
            new JavaScriptExecutor(browser, Resources.readString("js/sci-router-outlet/install-client-message-dispatcher.js"))
                .replacePlaceholder("callback", callback.name)
                .replacePlaceholder("helpers.toJson", Helpers.toJson)
                .replacePlaceholder("headers.AppSymbolicName", MessageHeaders.AppSymbolicName)
                .replacePlaceholder("storage", Scripts.Storage)
                .replacePlaceholder("uninstallStorageKey", uuid)
                .execute();

            disposables.add(() -> new JavaScriptExecutor(browser, Resources.readString("js/sci-router-outlet/uninstall-client-message-dispatcher.js"))
                .replacePlaceholder("storage", Scripts.Storage)
                .replacePlaceholder("uninstallStorageKey", uuid)
                .execute());
          });
    });

    return () -> disposables.forEach(IDisposable::dispose);
  }

  /**
   * Dispatches messages from the <sci-router-outlet> to the client.
   */
  private IDisposable installSciRouterOutletToClientMessageDispatcher() {
    return routerOutletProxy.onMessage(base64json -> {
      if (bridgeLoggerEnabled) {
        Platform.getLog(SciRouterOutlet.class).info("[SciBridge] [host=>client] " + decodeBase64(base64json));
      }

      new JavaScriptExecutor(browser, "window.postMessage(/@@helpers.fromJson@@/('/@@base64json@@/', {decode: true}));")
          .replacePlaceholder("base64json", base64json)
          .replacePlaceholder("helpers.fromJson", Helpers.fromJson)
          .execute();
    });
  }

  /**
   * Creates a {@link Map} with the origins of the passed applications.
   */
  private Map<String, String> getTrustedOrigins(List<Application> applications) {
    return applications
        .stream()
        .collect(Collectors.toMap(app -> app.symbolicName, app -> {
          try {
            var url = new URL(app.baseUrl);
            if (url.getPort() == -1) {
              return url.getProtocol() + "://" + url.getHost();
            }
            else {
              return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
            }
          }
          catch (MalformedURLException e) {
            throw new RuntimeException(e);
          }
        }));
  }

  private String decodeBase64(String base64) {
    try {
      return URLDecoder.decode(new String(Base64.getUrlDecoder().decode(base64)), "utf-8");
    }
    catch (UnsupportedEncodingException | RuntimeException e) {
      Platform.getLog(SciRouterOutlet.class).info("[SciRouterOutlet] Failed to decode base64-encoded value: " + base64, e);
      return null;
    }
  }

  @Override
  public void widgetDisposed(DisposeEvent e) {
    routerOutletProxy.dispose();
    navigator.dispose();
    keystrokeDispatcher.dispose();
  }

  @FunctionalInterface
  public static interface LoadListener {
    public void onLoad(URL url);
  }

  @FunctionalInterface
  public static interface UnloadListener {
    public void onUnload(URL url);
  }
}
