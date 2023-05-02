package ch.sbb.scion.rcp.microfrontend;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import java.lang.reflect.Type;

import org.eclipse.swt.browser.Browser;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.sbb.scion.rcp.microfrontend.host.MicrofrontendPlatformRcpHost;
import ch.sbb.scion.rcp.microfrontend.interceptor.IntentInterceptor;
import ch.sbb.scion.rcp.microfrontend.interceptor.MessageInterceptor;
import ch.sbb.scion.rcp.microfrontend.model.MicrofrontendPlatformConfig;
import ch.sbb.scion.rcp.microfrontend.model.Qualifier;

/**
 * @see <a href="https://scion-microfrontend-platform-api.vercel.app/classes/MicrofrontendPlatform.html">MicrofrontendPlatform</a>
 */
@Component(service = MicrofrontendPlatform.class)
public class MicrofrontendPlatform {

  @Reference
  private MicrofrontendPlatformRcpHost microfrontendPlatformRcpHost;

  /**
   * Starts the SCION Microfrontend Platform host.
   *
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/MicrofrontendPlatformHost.html#start"
   */
  public CompletableFuture<Browser> startHost(final MicrofrontendPlatformConfig config) {
    Objects.requireNonNull(config);
    return microfrontendPlatformRcpHost.start(config);
  }

  /**
   * Registers an interceptor for intercepting messages before being dispatched. Interceptors must be registered prior to starting the host.
   */
  public <T> void registerMessageInterceptor(final String topic, final MessageInterceptor<T> interceptor) {
    registerMessageInterceptor(topic, interceptor, Object.class);
  }

  /**
   * Registers an interceptor for intercepting messages before being dispatched. Interceptors must be registered prior to starting the host.
   */
  public <T> void registerMessageInterceptor(final String topic, final MessageInterceptor<T> interceptor, final Type payloadClazz) {
    Objects.requireNonNull(topic);
    Objects.requireNonNull(interceptor);
    Objects.requireNonNull(payloadClazz);
    microfrontendPlatformRcpHost.registerMessageInterceptor(topic, interceptor, payloadClazz);
  }

  /**
   * Registers an interceptor for intercepting intents before being dispatched. The interceptor will receive all intents independent of the
   * host's intentions. Interceptors must be registered prior to starting the host.
   */
  public <T> void registerIntentInterceptor(final String type, final IntentInterceptor<T> interceptor) {
    registerIntentInterceptor(type, null, interceptor);
  }

  /**
   * Registers an interceptor for intercepting intents before being dispatched. The interceptor will receive all intents independent of the
   * host's intentions. Interceptors must be registered prior to starting the host.
   */
  public <T> void registerIntentInterceptor(final String type, final IntentInterceptor<T> interceptor, final Type payloadClazz) {
    registerIntentInterceptor(type, null, interceptor, payloadClazz);
  }

  /**
   * Registers an interceptor for intercepting intents before being dispatched. The interceptor will receive all intents independent of the
   * host's intentions. Interceptors must be registered prior to starting the host.
   */
  public <T> void registerIntentInterceptor(final String type, final Qualifier qualifier, final IntentInterceptor<T> interceptor) {
    registerIntentInterceptor(type, qualifier, interceptor, Object.class);
  }

  /**
   * Registers an interceptor for intercepting intents before being dispatched. The interceptor will receive all intents independent of the
   * host's intentions. Interceptors must be registered prior to starting the host.
   */
  public <T> void registerIntentInterceptor(final String type, final Qualifier qualifier, final IntentInterceptor<T> interceptor,
      final Type payloadClazz) {
    Objects.requireNonNull(type);
    Objects.requireNonNull(interceptor);
    Objects.requireNonNull(payloadClazz);
    microfrontendPlatformRcpHost.registerIntentInterceptor(type, qualifier, interceptor, payloadClazz);
  }
}
