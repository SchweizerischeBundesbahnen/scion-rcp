package ch.sbb.scion.rcp.microfrontend;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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

import ch.sbb.scion.rcp.microfrontend.browser.BrowserCallback;
import ch.sbb.scion.rcp.microfrontend.browser.BrowserCallback.Options;
import ch.sbb.scion.rcp.microfrontend.browser.BrowserScriptExecutor;
import ch.sbb.scion.rcp.microfrontend.internal.ContextInjectors;
import ch.sbb.scion.rcp.microfrontend.internal.Resources;
import ch.sbb.scion.rcp.microfrontend.model.IDisposable;
import ch.sbb.scion.rcp.microfrontend.proxy.RouterOutletProxy;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.JsonHelpers;

/**
 * Widget to display a microfrontend.
 * 
 * @see https://scion-microfrontend-platform-api.vercel.app/classes/SciRouterOutletElement.html
 * 
 *      This widget acts as proxy for the SCION <sci-router-outlet> web
 *      component.
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
        new BrowserScriptExecutor(browser, """
            if (${pushStateToSessionHistoryStack}) {
               window.location.assign('${url}');
            }
            else {
               window.location.replace('${url}');
            }
            """)
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
   * Taps messages from the client and dispatches them to the <sci-router-outlet>.
   */
  private IDisposable installClientToSciRouterOutletMessageDispatcher() {
    var messageCallback = new BrowserCallback("__sci_onclientmessage", browser, new Options()
        .onCallback(args -> {
          if (bridgeLoggerEnabled) {
            Platform.getLog(SciRouterOutlet.class).info("[SciBridge] [client=>host] " + args[0]);
          }
          routerOutletProxy.postJsonMessage((String) args[0]);
        }));

    new BrowserScriptExecutor(browser, """
        window.addEventListener('message', event => {
          if (event.data?.transport === 'sci://microfrontend-platform/client-to-broker' || event.data?.transport === 'sci://microfrontend-platform/microfrontend-to-outlet') {
            window['${callbackName}']?.(${helpers.stringify}(event.data, 'Map=>MapObject', 'Set=>SetObject'));
          }
        });
        """)
        .replacePlaceholder("callbackName", messageCallback.name)
        .replacePlaceholder("helpers.stringify", JsonHelpers.stringify)
        .execute();

    return () -> messageCallback.dispose();
  }

  /**
   * Dispatches messages from the <sci-router-outlet> to the client.
   */
  private IDisposable installSciRouterOutletToClientMessageDispatcher() {
    return routerOutletProxy.onMessage(json -> {
      if (bridgeLoggerEnabled) {
        Platform.getLog(SciRouterOutlet.class).info("[SciBridge] [client=>host] " + json);
      }

      new BrowserScriptExecutor(browser, "window.postMessage(${helpers.parse}('${json}'));")
          .replacePlaceholder("json", json.replace("'", "\\'"))
          .replacePlaceholder("helpers.parse", JsonHelpers.parse)
          .execute();
    });
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