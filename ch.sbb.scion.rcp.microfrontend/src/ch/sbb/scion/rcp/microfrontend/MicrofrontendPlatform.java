package ch.sbb.scion.rcp.microfrontend;

import java.util.concurrent.CompletableFuture;

import org.eclipse.swt.browser.Browser;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.sbb.scion.rcp.microfrontend.host.MicrofrontendPlatformHostApp;
import ch.sbb.scion.rcp.microfrontend.model.MicrofrontendPlatformConfig;

/**
 * @see https://scion-microfrontend-platform-api.vercel.app/classes/MicrofrontendPlatform.html
 */
@Component(service = MicrofrontendPlatform.class)
public class MicrofrontendPlatform {

  @Reference
  private MicrofrontendPlatformHostApp microfrontendPlatformHostApp;
  
  /**
   * Starts the SCION Microfrontend Platform host.
   * 
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/MicrofrontendPlatform.html#startHost
   */
  public CompletableFuture<Browser> startHost(MicrofrontendPlatformConfig config) {
    return microfrontendPlatformHostApp.start(config);
  }
}
