package ch.sbb.scion.rcp.microfrontend.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Event;

import ch.sbb.scion.rcp.microfrontend.SciRouterOutlet;
import ch.sbb.scion.rcp.microfrontend.browser.JavaScriptCallback;
import ch.sbb.scion.rcp.microfrontend.browser.JavaScriptExecutor;
import ch.sbb.scion.rcp.microfrontend.host.MicrofrontendPlatformRcpHost;
import ch.sbb.scion.rcp.microfrontend.internal.ContextInjectors;
import ch.sbb.scion.rcp.microfrontend.keyboard.JavaScriptKeyboardEvent;
import ch.sbb.scion.rcp.microfrontend.keyboard.KeyboardEventMapper;
import ch.sbb.scion.rcp.microfrontend.model.IDisposable;
import ch.sbb.scion.rcp.microfrontend.script.Script.Flags;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Helpers;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Refs;

/**
 * Proxy for the actual <sci-router-outlet> mounted in the RCP host application.
 */
public class RouterOutletProxy {

  private String outletId;
  private CompletableFuture<Browser> whenOutlet;
  private List<Consumer<String>> outletToProxyMessageListeners = new ArrayList<>();
  private List<Consumer<Event>> outletToProxyKeystrokeListeners = new ArrayList<>();
  private List<IDisposable> disposables = new ArrayList<>();

  @Inject
  private MicrofrontendPlatformRcpHost microfrontendPlatformRcpHost;

  @Inject
  private KeyboardEventMapper keyboardEventMapper;

  public RouterOutletProxy(String outletName) {
    outletId = "sci_router_outlet_" + outletName.toLowerCase() + "_" + UUID.randomUUID();
    whenOutlet = new CompletableFuture<>();
    ContextInjectors.inject(this);
    init();
  }

  public void init() {
    // Callback invoked for messages to be transported to the client.
    var outletToProxyMessageCallback = new JavaScriptCallback(microfrontendPlatformRcpHost.whenHostBrowser, args -> {
      var jsonMessage = (String) args[0];
      outletToProxyMessageListeners.forEach(listener -> listener.accept(jsonMessage));
    });

    // Callback invoked for keystrokes triggered by the client.
    var outletToProxyKeystrokeCallback = new JavaScriptCallback(microfrontendPlatformRcpHost.whenHostBrowser, args -> {
      var webEvent = new JavaScriptKeyboardEvent((String) args[0], (String) args[1], (boolean) args[2], (boolean) args[3], (boolean) args[4], (boolean) args[5]);
      var swtEvent = keyboardEventMapper.mapKeyboardEvent(webEvent);
      outletToProxyKeystrokeListeners.forEach(listener -> listener.accept(swtEvent));
    });

    CompletableFuture.allOf(outletToProxyMessageCallback.addTo(disposables).install(), outletToProxyKeystrokeCallback.addTo(disposables).install()).thenRun(() -> {
      new JavaScriptExecutor(microfrontendPlatformRcpHost.whenHostBrowser, """
          const sciRouterOutlet = document.body.appendChild(document.createElement('sci-router-outlet'));

          const outletContent= `<html>
            <head>
              <script src="${location.origin}/js/helpers.js"></script>
              <script>
                // Dispatch message to the host
                function __scion_rcp_postMessageToParentWindow(envelope) {
                  window.parent.postMessage(envelope, location.origin);
                }

                // Dispatch messages from the host
                window.addEventListener('message', event => {
                  if (event.data?.transport === 'sci://microfrontend-platform/broker-to-client') {
                    // Encode as base64 so it can be safely inserted into a script as a string literal.
                    // For example, the apostrophe character (U+0027) would terminate the string literal.                    
                    const base64json = ${helpers.toJson}(event.data, {encode: true});
                    window.parent['${outletToProxyMessageCallback}']?.(base64json);
                  }
                });
              </script>
            </head>
            <body>${outletId}</body>
          </html>`;

          // Load outlet content
          const outletUrl = URL.createObjectURL(new Blob([outletContent], {type: 'text/html'}));
          window.addEventListener('unload', () => URL.revokeObjectURL(outletUrl), {once: true});
          sciRouterOutlet.name = '${outletId}';
          ${refs.OutletRouter}.navigate(outletUrl, {outlet: '${outletId}'});

          // Install keystroke dispatcher
          sciRouterOutlet.addEventListener('keydown', event => {
            window['${outletToProxyKeystrokeCallback}']('keydown', event.key, event.ctrlKey, event.shiftKey, event.altKey, event.metaKey);
          });
          sciRouterOutlet.addEventListener('keyup', event => {
            window['${outletToProxyKeystrokeCallback}']('keyup', event.key, event.ctrlKey, event.shiftKey, event.altKey, event.metaKey);
          });
          """)
          .replacePlaceholder("outletToProxyMessageCallback", outletToProxyMessageCallback.name)
          .replacePlaceholder("outletToProxyKeystrokeCallback", outletToProxyKeystrokeCallback.name)
          .replacePlaceholder("outletId", outletId)
          .replacePlaceholder("refs.OutletRouter", Refs.OutletRouter)
          .replacePlaceholder("helpers.toJson", Helpers.toJson)
          .execute()
          .thenRun(() -> whenOutlet.complete(microfrontendPlatformRcpHost.hostBrowser));
    });
  }

  /**
   * Instructs the web application loaded into the {@link SciRouterOutlet outlet
   * proxy} to dispatch given keystrokes.
   */
  public CompletableFuture<Void> registerKeystrokes(Set<String> keystrokes) {
    return new JavaScriptExecutor(whenOutlet, """
        const sciRouterOutlet = document.querySelector('sci-router-outlet[name="${outletId}"]');
        sciRouterOutlet.keystrokes = ${helpers.fromJson}('${keystrokes}');
        """)
        .replacePlaceholder("outletId", outletId)
        .replacePlaceholder("keystrokes", new ArrayList<>(keystrokes), Flags.ToJson)
        .replacePlaceholder("helpers.fromJson", Helpers.fromJson)
        .execute();
  }

  public CompletableFuture<Void> setContextValue(String name, Object value) {
    return new JavaScriptExecutor(whenOutlet, """
        const sciRouterOutlet = document.querySelector('sci-router-outlet[name="${outletId}"]');
        sciRouterOutlet.setContextValue('${name}', ${helpers.fromJson}('${value}'));
        """)
        .replacePlaceholder("outletId", outletId)
        .replacePlaceholder("name", name)
        .replacePlaceholder("value", value, Flags.ToJson)
        .replacePlaceholder("helpers.fromJson", Helpers.fromJson)
        .execute();
  }

  public CompletableFuture<Boolean> removeContextValue(String name) {
    var removed = new CompletableFuture<Boolean>();
    new JavaScriptCallback(whenOutlet, args -> {
      removed.complete((Boolean) args[0]);
    })
        .installOnce()
        .thenAccept(callback -> {
          new JavaScriptExecutor(whenOutlet, """
              const sciRouterOutlet = document.querySelector('sci-router-outlet[name="${outletId}"]');
              const removed = sciRouterOutlet.removeContextValue('${name}');
              window['${callback}'](removed);
              """)
              .replacePlaceholder("callback", callback.name)
              .replacePlaceholder("outletId", outletId)
              .replacePlaceholder("name", name)
              .execute();
        });
    return removed;
  }

  /**
   * Posts given JSON message to the host in the name of the web application
   * loaded into the the {@link SciRouterOutlet outlet proxy}.
   */
  public void postJsonMessage(String base64json) {
    new JavaScriptExecutor(whenOutlet, """
        const sciRouterOutlet = document.querySelector('sci-router-outlet[name="${outletId}"]');

        try {
          const envelope = ${helpers.fromJson}('${base64json}', {decode: true});
          sciRouterOutlet.iframe.contentWindow.__scion_rcp_postMessageToParentWindow(envelope);
        }
        catch (error) {
          console.error('[MessageDispatchError] Failed to dispatch message to outlet window. [outlet=${outletId}]', error);
        }
        """)
        .replacePlaceholder("outletId", outletId)
        .replacePlaceholder("base64json", base64json)
        .replacePlaceholder("helpers.fromJson", Helpers.fromJson)
        .execute();
  }

  /**
   * Dispatches messages from the host which are intended for the web application
   * loaded into the {@link SciRouterOutlet outlet proxy}.
   */
  public IDisposable onMessage(Consumer<String> messageListener) {
    outletToProxyMessageListeners.add(messageListener);
    return () -> outletToProxyMessageListeners.remove(messageListener);
  }

  /**
   * Dispatches keystrokes originating from the web application loaded into the
   * {@link SciRouterOutlet outlet proxy}.
   */
  public IDisposable onKeystroke(Consumer<Event> keystrokeListener) {
    outletToProxyKeystrokeListeners.add(keystrokeListener);
    return () -> outletToProxyMessageListeners.remove(keystrokeListener);
  }

  public String getOutletId() {
    return outletId;
  }

  public void dispose() {
    new JavaScriptExecutor(whenOutlet, """
        const sciRouterOutlet = document.querySelector('sci-router-outlet[name="${outletId}"]');
        sciRouterOutlet.remove();
        """)
        .replacePlaceholder("outletId", outletId)
        .execute();

    disposables.forEach(IDisposable::dispose);
  }
}
