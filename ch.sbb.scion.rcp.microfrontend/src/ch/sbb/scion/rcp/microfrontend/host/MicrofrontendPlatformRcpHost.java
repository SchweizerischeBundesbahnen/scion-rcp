package ch.sbb.scion.rcp.microfrontend.host;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import ch.sbb.scion.rcp.microfrontend.SciRouterOutlet;
import ch.sbb.scion.rcp.microfrontend.browser.JavaCallback;
import ch.sbb.scion.rcp.microfrontend.browser.JavaScriptExecutor;
import ch.sbb.scion.rcp.microfrontend.host.IntentInterceptorInstaller.IntentInterceptorDescriptor;
import ch.sbb.scion.rcp.microfrontend.host.MessageInterceptorInstaller.MessageInterceptorDescriptor;
import ch.sbb.scion.rcp.microfrontend.host.Webserver.Resource;
import ch.sbb.scion.rcp.microfrontend.interceptor.IntentInterceptor;
import ch.sbb.scion.rcp.microfrontend.interceptor.MessageInterceptor;
import ch.sbb.scion.rcp.microfrontend.internal.Resources;
import ch.sbb.scion.rcp.microfrontend.model.MicrofrontendPlatformConfig;
import ch.sbb.scion.rcp.microfrontend.model.Qualifier;
import ch.sbb.scion.rcp.microfrontend.script.Script.Flags;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Helpers;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Refs;

/**
 * Represents the RCP host for the SCION Microfrontend Platform that is started
 * in an invisible shell in a web browser.
 *
 * When instantiating the {@link SciRouterOutlet} SWT component, an
 * <sci-router-outlet> web component is added to the DOM of the RCP host
 * application. From the perspective of the SCION Microfrontend Platform Host,
 * the microfrontend is embedded directly into this outlet.
 * {@link SciRouterOutlet} effectively acts as a proxy, bridging traffic between
 * <sci-router-outlet> and {@link SciRouterOutlet}.
 */
@Component(service = MicrofrontendPlatformRcpHost.class)
public class MicrofrontendPlatformRcpHost {

  private boolean headless = false;
  private Shell shell;
  private Webserver webserver;
  private List<MessageInterceptorDescriptor<?>> messageInterceptors = new ArrayList<>();
  private List<IntentInterceptorDescriptor<?>> intentInterceptors = new ArrayList<>();

  public Browser hostBrowser;
  public CompletableFuture<Browser> whenHostBrowser = new CompletableFuture<>();

  @Reference
  private MessageInterceptorInstaller messageInterceptorInstaller;

  @Reference
  private IntentInterceptorInstaller intentInterceptorInstaller;

  /**
   * Starts the SCION Microfrontend Platform host.
   *
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/MicrofrontendPlatform.html#startHost
   */
  public CompletableFuture<Browser> start(MicrofrontendPlatformConfig config) {
    // Create the shell
    shell = new Shell(Display.getDefault());
    shell.setLayout(new FillLayout());
    shell.setSize(new Point(400, 700));
    shell.setText("SCION Microfrontend Platform RCP host");

    // Create webserver to serve the host app on a random port.
    webserver = new Webserver(Map.of(
        "host.html", new Resource(Resources.get("js/host.html"), "text/html", "utf-8"),
        "js/refs.js", new Resource(Resources.get("js/refs.js"), "application/javascript", "utf-8"),
        "js/refs.js.map", new Resource(Resources.get("js/refs.js.map"), "application/javascript", "utf-8"),
        "js/helpers.js", new Resource(Resources.get("js/helpers.js"), "application/javascript", "utf-8")))
        .start();

    // Create the browser and
    hostBrowser = new Browser(shell, SWT.EDGE);
    hostBrowser.addProgressListener(new ProgressAdapter() {
      public void completed(ProgressEvent event) {
        startHost(config);
      };
    });

    if (!headless) {
      shell.open();
    }

    hostBrowser.setUrl(String.format("http://localhost:%d/host.html", webserver.getPort()));
    return whenHostBrowser;
  }

  private void startHost(MicrofrontendPlatformConfig config) {
    messageInterceptors.forEach(interceptor -> messageInterceptorInstaller.install(interceptor, hostBrowser));
    intentInterceptors.forEach(interceptor -> intentInterceptorInstaller.install(interceptor, hostBrowser));

    new JavaCallback(hostBrowser, args -> {
      var error = args[0];
      if (error == null) {
        whenHostBrowser.complete(hostBrowser);
      }
      else {
        Platform.getLog(JavaScriptExecutor.class).error("Failed to start Microfrontend Platform: " + error);
        whenHostBrowser.completeExceptionally(new RuntimeException((String) error));
      }
    })
        .installOnce()
        .thenAccept(callback -> {
          new JavaScriptExecutor(hostBrowser, Resources.readString("js/host/start-host.js"))
              .replacePlaceholder("callback", callback.name)
              .replacePlaceholder("refs.MicrofrontendPlatform", Refs.MicrofrontendPlatform)
              .replacePlaceholder("platformConfig", config, Flags.ToJson)
              .replacePlaceholder("helpers.fromJson", Helpers.fromJson)
              .runInsideAsyncFunction()
              .execute();
        });
  }

  public <T> void registerMessageInterceptor(String topic, MessageInterceptor<T> interceptor, Type payloadClazz) {
    if (isHostStarted()) {
      throw new IllegalStateException("Host already started. Message interceptors must be registered prior to host startup.");
    }
    messageInterceptors.add(new MessageInterceptorDescriptor<>(topic, interceptor, payloadClazz));
  }

  public <T> void registerIntentInterceptor(String type, Qualifier qualifier, IntentInterceptor<T> interceptor,
      Type payloadClazz) {
    if (isHostStarted()) {
      throw new IllegalStateException("Host already started. Intent interceptors must be registered prior to host startup.");
    }
    intentInterceptors.add(new IntentInterceptorDescriptor<T>(type, qualifier, interceptor, payloadClazz));
  }

  @Deactivate
  public void onDeactivate() {
    if (shell != null) {
      shell.dispose();
      shell = null;
    }
    if (webserver != null) {
      webserver.stop();
      webserver = null;
    }
  }

  private boolean isHostStarted() {
    return shell != null;
  }
}
