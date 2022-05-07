package ch.sbb.scion.rcp.microfrontend;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

import org.eclipse.swt.browser.Browser;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.sbb.scion.rcp.microfrontend.host.MicrofrontendPlatformRcpHost;
import ch.sbb.scion.rcp.microfrontend.interceptor.IntentInterceptor;
import ch.sbb.scion.rcp.microfrontend.interceptor.MessageInterceptor;
import ch.sbb.scion.rcp.microfrontend.model.MicrofrontendPlatformConfig;
import ch.sbb.scion.rcp.microfrontend.model.Qualifier;

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

  /**
   * Registers an interceptor for intercepting intents before being dispatched.
   * 
   * The interceptor will receive all intents independent of the host's
   * intentions. Interceptors must be registered prior to starting the host.
   */
  public <T> void registerIntentInterceptor(String type, IntentInterceptor<T> interceptor) {
    registerIntentInterceptor(type, null, interceptor);
  }

  /**
   * Registers an interceptor for intercepting intents before being dispatched.
   * 
   * The interceptor will receive all intents independent of the host's
   * intentions. Interceptors must be registered prior to starting the host.
   */
  public <T> void registerIntentInterceptor(String type, IntentInterceptor<T> interceptor, Type payloadClazz) {
    registerIntentInterceptor(type, null, interceptor, payloadClazz);
  }

  /**
   * Registers an interceptor for intercepting intents before being dispatched.
   * 
   * The interceptor will receive all intents independent of the host's
   * intentions. Interceptors must be registered prior to starting the host.
   */
  public <T> void registerIntentInterceptor(String type, Qualifier qualifier, IntentInterceptor<T> interceptor) {
    registerIntentInterceptor(type, qualifier, interceptor, Object.class);
  }

  /**
   * Registers an interceptor for intercepting intents before being dispatched.
   * 
   * The interceptor will receive all intents independent of the host's
   * intentions. Interceptors must be registered prior to starting the host.
   */
  public <T> void registerIntentInterceptor(String type, Qualifier qualifier, IntentInterceptor<T> interceptor, Type payloadClazz) {
    microfrontendPlatformRcpHost.registerIntentInterceptor(type, qualifier, interceptor, payloadClazz);
  }
}
