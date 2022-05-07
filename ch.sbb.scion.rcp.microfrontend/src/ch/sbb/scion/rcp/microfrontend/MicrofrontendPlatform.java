package ch.sbb.scion.rcp.microfrontend;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

import org.eclipse.swt.browser.Browser;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.sbb.scion.rcp.microfrontend.host.MicrofrontendPlatformRcpHost;
import ch.sbb.scion.rcp.microfrontend.interceptor.MessageInterceptor;
import ch.sbb.scion.rcp.microfrontend.model.MicrofrontendPlatformConfig;

/**
 * @see https://scion-microfrontend-platform-api.vercel.app/classes/MicrofrontendPlatform.html
 */
@Component(service = MicrofrontendPlatform.class)
public class MicrofrontendPlatform {

  @Reference
  private MicrofrontendPlatformRcpHost microfrontendPlatformRcpHost;

  /**
   * Starts the SCION Microfrontend Platform host.
   * 
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/MicrofrontendPlatform.html#startHost
   */
  public CompletableFuture<Browser> startHost(MicrofrontendPlatformConfig config) {
    return microfrontendPlatformRcpHost.start(config);
  }

  /**
   * Registers an interceptor for intercepting messages before being dispatched.
   * Interceptors must be registered prior to starting the host.
   */
  public <T> void registerMessageInterceptor(String topic, MessageInterceptor<T> interceptor) {
    registerMessageInterceptor(topic, interceptor, Object.class);
  }

  /**
   * Registers an interceptor for intercepting messages before being dispatched.
   * Interceptors must be registered prior to starting the host.
   */
  public <T> void registerMessageInterceptor(String topic, MessageInterceptor<T> interceptor, Type payloadClazz) {
    microfrontendPlatformRcpHost.registerMessageInterceptor(topic, interceptor, payloadClazz);
  }
}
