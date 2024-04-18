package ch.sbb.scion.rcp.microfrontend;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import javax.inject.Inject;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ch.sbb.scion.rcp.microfrontend.browser.JavaCallback;
import ch.sbb.scion.rcp.microfrontend.browser.JavaScriptExecutor;
import ch.sbb.scion.rcp.microfrontend.internal.ContextInjectors;
import ch.sbb.scion.rcp.microfrontend.internal.Resources;
import ch.sbb.scion.rcp.microfrontend.model.Application;
import ch.sbb.scion.rcp.microfrontend.model.MessageHeaders;
import ch.sbb.scion.rcp.microfrontend.proxy.RouterOutletProxy;
import ch.sbb.scion.rcp.microfrontend.script.Scripts;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Helpers;

/**
 * Widget to display a microfrontend. This widget acts as proxy for the SCION &lt;sci-router-outlet&gt; web component.
 *
 * @see <a href="https://scion-microfrontend-platform-api.vercel.app/classes/SciRouterOutletElement.html">SciRouterOutletElement</a>
 */
public final class RouterOutlet extends Composite implements DisposeListener {

  private static final boolean BRIDGE_LOGGER_ENABLED = false;

  private final RouterOutletProxy routerOutletProxy;
  private final Browser browser;
  private URL url;
  private final IDisposable navigator;
  private final IDisposable keystrokeDispatcher;
  private final List<LoadListener> loadListeners = new ArrayList<>();
  private final List<UnloadListener> unloadListeners = new ArrayList<>();
  private final Set<String> keystrokes = new HashSet<>();

  @Inject
  private MessageClient messageClient;

  @Inject
  private ManifestService manifestService;

  public RouterOutlet(final Composite parent, final int style, final String outletName) {
    this(parent, style, outletName, null);
  }

  public RouterOutlet(final Composite parent, final int style, final String outletName, final Control keystrokeTarget) {
    super(parent, style);
    ContextInjectors.inject(this);
    Objects.requireNonNull(outletName);

    setLayout(new FillLayout());
    addDisposeListener(this);

    routerOutletProxy = new RouterOutletProxy(outletName);

    browser = new Browser(this, SWT.EDGE);
    browser.addFocusListener(new FocusListener() {

      @Override
      public void focusLost(final FocusEvent e) {
        Platform.getLog(getClass()).info("Browser: Focus lost");
      }

      @Override
      public void focusGained(final FocusEvent e) {
        Platform.getLog(getClass()).info("Browser: Focus gained");
      }
    });
    browser.addListener(SWT.Activate, event -> {
      Platform.getLog(getClass()).info("Browser: Activated");
    });
    browser.addListener(SWT.Deactivate, event -> {
      Platform.getLog(getClass()).info("Browser: Deactivated");
    });

    browser.addProgressListener(new ProgressAdapter() {

      private final List<IDisposable> disposables = new ArrayList<>();

      @Override
      public void completed(final ProgressEvent event) {
        // is invoked when completed loading the app, or when reloading it, e.g., due to
        // hot code replacement during development
        disposables.forEach(IDisposable::dispose);
        disposables.clear();

        browser.execute(Resources.readString("js/helpers.js"));
        var clientToSciRouterOutletMessageDispatcher = installClientToSciRouterOutletMessageDispatcher();
        var sciRouterOutletToClientMessageDispatcher = installSciRouterOutletToClientMessageDispatcher();

        disposables.add(clientToSciRouterOutletMessageDispatcher);
        disposables.add(sciRouterOutletToClientMessageDispatcher);
      }
    });

    navigator = installRouter(outletName);
    keystrokeDispatcher = installKeystrokeDispatcher(keystrokeTarget);
  }

  private IDisposable installRouter(final String outletName) {
    var subscription = messageClient.subscribe("sci-router-outlets/" + outletName + "/url", event -> {
      var prevUrl = url;
      if (prevUrl != null) {
        unloadListeners.forEach(listener -> listener.onUnload(url));
      }

      try {
        url = new URL(Optional.ofNullable(event.body()).orElse("about:blank"));

        if (url.equals(prevUrl)) {
          return;
        }

        var pushStateToSessionHistoryStack = Optional.ofNullable(event.headers().get("ɵPUSH_STATE_TO_SESSION_HISTORY_STACK"))
            .orElse(Boolean.FALSE);
        new JavaScriptExecutor(browser, Resources.readString("js/sci-router-outlet/navigate.js")).replacePlaceholder("url", url)
            .replacePlaceholder("pushStateToSessionHistoryStack", pushStateToSessionHistoryStack).execute()
            .thenRun(() -> loadListeners.forEach(listener -> listener.onLoad(url)));
      }
      catch (MalformedURLException e) {
        Platform.getLog(RouterOutlet.class).error(String.format("Failed to navigate in outlet [outlet=%s]", outletName), e);
      }
    });

    return subscription::unsubscribe;
  }

  private IDisposable installKeystrokeDispatcher(final Control keystrokeTarget) {
    if (keystrokeTarget == null) {
      return () -> {
        // noop
      };
    }
    return routerOutletProxy.onKeystroke(event -> {
      keystrokeTarget.setFocus();
      // Prevent infinite cycle:
      if (browser.isFocusControl()) {
        throw new IllegalStateException(
            "Browser has focus. Make sure that the keystrokeTarget is not a parent of this SciRouterOutlet and," + //
                "that the keystrokeTarget can gain focus; i.e., it does not have the SWT.NO_FOCUS style bit set.");
      }
      getDisplay().post(event);
    });
  }

  public IDisposable onLoad(final LoadListener listener) {
    Objects.requireNonNull(listener);
    loadListeners.add(listener);
    return () -> loadListeners.remove(listener);
  }

  public IDisposable onUnload(final UnloadListener listener) {
    Objects.requireNonNull(listener);
    unloadListeners.add(listener);
    return () -> unloadListeners.remove(listener);
  }

  /**
   * Instructs the web application loaded into the outlet to dispatch given keystrokes.
   */
  public CompletableFuture<Void> registerKeystroke(final String keystroke) {
    Objects.requireNonNull(keystroke);
    return registerKeystrokes(Set.of(keystroke));
  }

  /**
   * Instructs the web application loaded into the outlet to dispatch given keystrokes.
   */
  public CompletableFuture<Void> registerKeystrokes(final Set<String> keystrokes) {
    Objects.requireNonNull(keystrokes);
    this.keystrokes.addAll(keystrokes);
    return routerOutletProxy.registerKeystrokes(this.keystrokes);
  }

  public CompletableFuture<Void> unregisterKeystroke(final String keystroke) {
    Objects.requireNonNull(keystroke);
    return unregisterKeystrokes(Set.of(keystroke));
  }

  public CompletableFuture<Void> unregisterKeystrokes(final Set<String> keystrokes) {
    Objects.requireNonNull(keystrokes);
    this.keystrokes.removeAll(keystrokes);
    return routerOutletProxy.registerKeystrokes(this.keystrokes);
  }

  /**
   * Makes contextual data available to embedded content. Embedded content can lookup contextual data using the
   * <code>org.eclipse.ui.internal.contexts.ContextService</code>. Contextual data must be serializable with the structured clone algorithm.
   *
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/SciRouterOutletElement.html#setContextValue"
   */
  public CompletableFuture<Void> setContextValue(final String name, final Object value) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(value);
    return routerOutletProxy.setContextValue(name, value);
  }

  /**
   * Removes data registered under the given key from the context. Removal does not affect parent contexts, so it is possible that a
   * subsequent call to <code>org.eclipse.ui.internal.contexts.ContextService.observe()</code> with the same name will return a non-null
   * result, due to a value being stored in a parent context.
   *
   * @return `true` if removed the value from the outlet context; otherwise `false`.
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/SciRouterOutletElement.html#removeContextValue"
   */
  public CompletableFuture<Boolean> removeContextValue(final String name) {
    Objects.requireNonNull(name);
    return routerOutletProxy.removeContextValue(name);
  }

  /**
   * Taps messages from the client and dispatches them to the &lt;sci-router-outlet&gt;.
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
          Platform.getLog(RouterOutlet.class)
              .info(String.format("[BLOCKED] Request blocked. Wrong origin [actual='%s', expectedOneOf='%s', envelope='%s']", origin,
                  trustedOrigins.values(), decodeBase64(base64json)));
          disposables.forEach(IDisposable::dispose);
        }
        else if (sender != null && !trustedOrigins.containsKey(sender)) {
          Platform.getLog(RouterOutlet.class).info(
              String.format("[BLOCKED] Request blocked. Unknown application [app='%s', envelope='%s']", sender, decodeBase64(base64json)));
          disposables.forEach(IDisposable::dispose);
        }
        else if (sender != null && !origin.equals(trustedOrigins.get(sender))) {
          Platform.getLog(RouterOutlet.class)
              .info(String.format("[BLOCKED] Request blocked. Wrong origin [app='%s', actual='%s', expected='%s', envelope='%s']", sender,
                  origin, trustedOrigins.get(sender), decodeBase64(base64json)));
          disposables.forEach(IDisposable::dispose);
        }
        else {
          if (BRIDGE_LOGGER_ENABLED) {
            Platform.getLog(RouterOutlet.class).info("[SciBridge] [client=>host] " + decodeBase64(base64json));
          }
          routerOutletProxy.postJsonMessage(base64json);
        }
      }).addTo(disposables).install().thenAccept(callback -> {
        var uuid = UUID.randomUUID();
        new JavaScriptExecutor(browser, Resources.readString("js/sci-router-outlet/install-client-message-dispatcher.js"))
            .replacePlaceholder("callback", callback.name).replacePlaceholder("helpers.toJson", Helpers.toJson)
            .replacePlaceholder("headers.AppSymbolicName", MessageHeaders.APP_SYMBOLIC_NAME).replacePlaceholder("storage", Scripts.Storage)
            .replacePlaceholder("uninstallStorageKey", uuid).execute();

        disposables
            .add(() -> new JavaScriptExecutor(browser, Resources.readString("js/sci-router-outlet/uninstall-client-message-dispatcher.js"))
                .replacePlaceholder("storage", Scripts.Storage).replacePlaceholder("uninstallStorageKey", uuid).execute());
      });
    });

    return () -> disposables.forEach(IDisposable::dispose);
  }

  /**
   * Dispatches messages from the &lt;sci-router-outlet&gt; to the client.
   */
  private IDisposable installSciRouterOutletToClientMessageDispatcher() {
    return routerOutletProxy.onMessage(base64json -> {
      if (BRIDGE_LOGGER_ENABLED) {
        Platform.getLog(RouterOutlet.class).info("[SciBridge] [host=>client] " + decodeBase64(base64json));
      }

      new JavaScriptExecutor(browser, "window.postMessage(/@@helpers.fromJson@@/('/@@base64json@@/', {decode: true}));")
          .replacePlaceholder("base64json", base64json).replacePlaceholder("helpers.fromJson", Helpers.fromJson).execute();
    });
  }

  /**
   * Creates a {@link Map} with the origins of the passed applications.
   */
  private Map<String, String> getTrustedOrigins(final List<Application> applications) {
    return applications.stream().collect(Collectors.toMap(Application::symbolicName, app -> {
      try {
        var url = new URL(app.baseUrl());
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

  private String decodeBase64(final String base64) {
    try {
      return URLDecoder.decode(new String(Base64.getUrlDecoder().decode(base64)), "utf-8");
    }
    catch (UnsupportedEncodingException | RuntimeException e) {
      Platform.getLog(RouterOutlet.class).info("[SciRouterOutlet] Failed to decode base64-encoded value: " + base64, e);
      return null;
    }
  }

  @Override
  public void widgetDisposed(final DisposeEvent e) {
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
