package ch.sbb.scion.rcp.microfrontend.host;

import java.util.Map;
import java.util.UUID;
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

import ch.sbb.scion.rcp.microfrontend.browser.BrowserCallback;
import ch.sbb.scion.rcp.microfrontend.browser.BrowserCallback.Options;
import ch.sbb.scion.rcp.microfrontend.browser.BrowserScriptExecutor;
import ch.sbb.scion.rcp.microfrontend.host.Webserver.Resource;
import ch.sbb.scion.rcp.microfrontend.internal.Resources;
import ch.sbb.scion.rcp.microfrontend.model.MicrofrontendPlatformConfig;
import ch.sbb.scion.rcp.microfrontend.script.Script.Flags;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Refs;

/**
 * Starts the SCION Microfrontend Platform RCP host in an invisible shell in a web browser.
 */
@Component(service = MicrofrontendPlatformHostApp.class)
public class MicrofrontendPlatformHostApp {

  private boolean headless = false;
  private Shell shell;
  private Webserver webserver;

  public Browser hostBrowser;
  public CompletableFuture<Browser> whenHostBrowser = new CompletableFuture<>();

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
        start(config, hostBrowser);
      };
    });

    if (!headless) {
      shell.open();
    }

    webserver.whenStarted().thenRun(() -> {
      hostBrowser.setUrl(String.format("http://localhost:%d/host.html", webserver.getPort()));
    });
    return whenHostBrowser;
  }

  private void start(MicrofrontendPlatformConfig config, Browser browser) {
    var browserCallback = new BrowserCallback("__sci_microfrontendplatform$started_" + UUID.randomUUID(), browser, new Options()
        .once()
        .onCallback(args -> {
          var error = args[0];
          if (error == null) {
            whenHostBrowser.complete(browser);
          }
          else {
            Platform.getLog(BrowserScriptExecutor.class).error("Failed to start Microfrontend Platform: " + error);
            whenHostBrowser.completeExceptionally(new RuntimeException((String) error));
          }
        }));

    new BrowserScriptExecutor(browser, """
        try {
          // Set the message origin
          const config = JSON.parse('${platformConfig}');
          config.applications.forEach(application => application.messageOrigin = window.location.origin);

          await ${MicrofrontendPlatform}.startHost(config);
          window['${callbackName}'](null);
        }
        catch (error) {
          console.log('Failed to start Microfrontend Platform', error);
          window['${callbackName}'](error.message ?? `${error}` ?? 'Failed to start Microfrontend Platform');
        }
        """)
        .replacePlaceholder("callbackName", browserCallback.name)
        .replacePlaceholder("MicrofrontendPlatform", Refs.MicrofrontendPlatform)
        .replacePlaceholder("platformConfig", config, Flags.ToJson)
        .runInsideAsyncFunction()
        .execute();
  }

  @Deactivate
  public void onDeactivate() {
    if (shell != null) {
      shell.dispose();
    }
    if (webserver != null) {
      webserver.stop();
    }
  }
}
