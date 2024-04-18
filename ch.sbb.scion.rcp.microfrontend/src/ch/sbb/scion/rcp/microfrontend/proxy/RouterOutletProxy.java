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

import ch.sbb.scion.rcp.microfrontend.IDisposable;
import ch.sbb.scion.rcp.microfrontend.RouterOutlet;
import ch.sbb.scion.rcp.microfrontend.browser.JavaCallback;
import ch.sbb.scion.rcp.microfrontend.browser.JavaScriptExecutor;
import ch.sbb.scion.rcp.microfrontend.host.MicrofrontendPlatformRcpHost;
import ch.sbb.scion.rcp.microfrontend.internal.ContextInjectors;
import ch.sbb.scion.rcp.microfrontend.internal.Resources;
import ch.sbb.scion.rcp.microfrontend.keyboard.JavaScriptKeyboardEvent;
import ch.sbb.scion.rcp.microfrontend.keyboard.KeyboardEventMapper;
import ch.sbb.scion.rcp.microfrontend.script.Script.Flags;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Helpers;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Refs;

/**
 * Proxy for the actual &lt;sci-router-outlet&gt; mounted in the RCP host application.
 */
public class RouterOutletProxy {

  private final String outletId;
  private final CompletableFuture<Browser> whenOutlet;
  private final List<Consumer<String>> outletToProxyMessageListeners = new ArrayList<>();
  private final List<Consumer<Event>> outletToProxyKeystrokeListeners = new ArrayList<>();
  private final List<Consumer<Boolean>> outletToProxyFocusWithinListeners = new ArrayList<>();
  private final List<IDisposable> disposables = new ArrayList<>();

  @Inject
  private MicrofrontendPlatformRcpHost microfrontendPlatformRcpHost;

  @Inject
  private KeyboardEventMapper keyboardEventMapper;

  public RouterOutletProxy(final String outletName) {
    outletId = "sci_router_outlet_" + outletName.toLowerCase() + "_" + UUID.randomUUID();
    whenOutlet = new CompletableFuture<>();
    ContextInjectors.inject(this);
    init();
  }

  public void init() {
    // Callback invoked for messages to be transported to the client.
    var outletToProxyMessageCallback = new JavaCallback(microfrontendPlatformRcpHost.whenHostBrowser, args -> {
      var jsonMessage = (String) args[0];
      outletToProxyMessageListeners.forEach(listener -> listener.accept(jsonMessage));
    });

    // Callback invoked for keystrokes triggered by the client.
    var outletToProxyKeystrokeCallback = new JavaCallback(microfrontendPlatformRcpHost.whenHostBrowser, args -> {

      // args 2-5 will never be null
      var webEvent = new JavaScriptKeyboardEvent((String) args[0], (String) args[1], unboxBoolean(args[2]), unboxBoolean(args[3]),
          unboxBoolean(args[4]), unboxBoolean(args[5]));
      var swtEvent = keyboardEventMapper.mapKeyboardEvent(webEvent);
      outletToProxyKeystrokeListeners.forEach(listener -> listener.accept(swtEvent));
    });

    // Callback invoked when the embedded scion-router-outlet, or any descendant, gains or loses focus.
    var outletToProxyFocusWithinCallback = new JavaCallback(microfrontendPlatformRcpHost.whenHostBrowser, args -> {
      var focusWithin = (Boolean) args[1];
      outletToProxyFocusWithinListeners.forEach(listener -> listener.accept(focusWithin));
    });

    // Callback to signal when loaded the outlet.
    var outletLoadedCallback = new JavaCallback(microfrontendPlatformRcpHost.whenHostBrowser, args -> {
      whenOutlet.complete(microfrontendPlatformRcpHost.hostBrowser);
    });

    var whenCallbacksInstalled = CompletableFuture.allOf(outletToProxyMessageCallback.addTo(disposables).install(),
        outletToProxyKeystrokeCallback.addTo(disposables).install(), outletToProxyFocusWithinCallback.addTo(disposables).install(),
        outletLoadedCallback.installOnce());

    whenCallbacksInstalled.thenRun(() -> {
      new JavaScriptExecutor(microfrontendPlatformRcpHost.whenHostBrowser,
          Resources.readString("js/router-outlet-proxy/install-sci-router-outlet.js"))
              .replacePlaceholder("outletToProxyMessageCallback", outletToProxyMessageCallback.name)
              .replacePlaceholder("outletToProxyKeystrokeCallback", outletToProxyKeystrokeCallback.name)
              .replacePlaceholder("outletToProxyFocusWithinCallback", outletToProxyFocusWithinCallback.name)
              .replacePlaceholder("outletLoadedCallback", outletLoadedCallback.name).replacePlaceholder("outletId", outletId)
              .replacePlaceholder("refs.OutletRouter", Refs.OutletRouter).replacePlaceholder("helpers.toJson", Helpers.toJson).execute();
    });
  }

  private final static boolean unboxBoolean(final Object o) {
    return ((Boolean) o).booleanValue();
  }

  /**
   * Instructs the web application loaded into the {@link RouterOutlet outlet proxy} to dispatch given keystrokes.
   */
  public CompletableFuture<Void> registerKeystrokes(final Set<String> keystrokes) {
    return new JavaScriptExecutor(whenOutlet, Resources.readString("js/router-outlet-proxy/register-keystrokes.js"))
        .replacePlaceholder("outletId", outletId).replacePlaceholder("keystrokes", new ArrayList<>(keystrokes), Flags.ToJson)
        .replacePlaceholder("helpers.fromJson", Helpers.fromJson).execute();
  }

  public CompletableFuture<Void> setContextValue(final String name, final Object value) {
    return new JavaScriptExecutor(whenOutlet, Resources.readString("js/router-outlet-proxy/set-context-value.js"))
        .replacePlaceholder("outletId", outletId).replacePlaceholder("name", name).replacePlaceholder("value", value, Flags.ToJson)
        .replacePlaceholder("helpers.fromJson", Helpers.fromJson).execute();
  }

  public CompletableFuture<Boolean> removeContextValue(final String name) {
    var removed = new CompletableFuture<Boolean>();
    new JavaCallback(whenOutlet, args -> {
      removed.complete((Boolean) args[0]);
    }).installOnce().thenAccept(callback -> {
      new JavaScriptExecutor(whenOutlet, Resources.readString("js/router-outlet-proxy/remove-context-value.js"))
          .replacePlaceholder("callback", callback.name).replacePlaceholder("outletId", outletId).replacePlaceholder("name", name)
          .execute();
    });
    return removed;
  }

  /**
   * Posts given JSON message to the host in the name of the web application loaded into the the {@link RouterOutlet outlet proxy}.
   */
  public void postJsonMessage(final String base64json) {
    new JavaScriptExecutor(whenOutlet, Resources.readString("js/router-outlet-proxy/post-message-to-host.js"))
        .replacePlaceholder("outletId", outletId).replacePlaceholder("base64json", base64json)
        .replacePlaceholder("helpers.fromJson", Helpers.fromJson).execute();
  }

  /**
   * Dispatches messages from the host which are intended for the web application loaded into the {@link RouterOutlet outlet proxy}.
   */
  public IDisposable onMessage(final Consumer<String> messageListener) {
    outletToProxyMessageListeners.add(messageListener);
    return () -> outletToProxyMessageListeners.remove(messageListener);
  }

  /**
   * Dispatches keystrokes originating from the web application loaded into the {@link RouterOutlet outlet proxy}.
   */
  public IDisposable onKeystroke(final Consumer<Event> keystrokeListener) {
    outletToProxyKeystrokeListeners.add(keystrokeListener);
    return () -> outletToProxyMessageListeners.remove(keystrokeListener);
  }

  public IDisposable onFocusWithin(final Consumer<Boolean> focusWithinListener) {
    outletToProxyFocusWithinListeners.add(focusWithinListener);
    return () -> outletToProxyFocusWithinListeners.remove(focusWithinListener);
  }

  public String getOutletId() {
    return outletId;
  }

  public void dispose() {
    new JavaScriptExecutor(whenOutlet, Resources.readString("js/router-outlet-proxy/dispose.js")).replacePlaceholder("outletId", outletId)
        .execute();

    disposables.forEach(IDisposable::dispose);
  }
}
