package ch.sbb.scion.rcp.microfrontend;

import java.util.concurrent.CompletableFuture;

import java.lang.reflect.Type;

import org.eclipse.swt.browser.Browser;

import ch.sbb.scion.rcp.microfrontend.interceptor.IntentInterceptor;
import ch.sbb.scion.rcp.microfrontend.interceptor.MessageInterceptor;
import ch.sbb.scion.rcp.microfrontend.model.MicrofrontendPlatformConfig;
import ch.sbb.scion.rcp.microfrontend.model.Qualifier;

/**
 * @see <a href="https://microfrontend-platform-api.scion.vercel.app/classes/MicrofrontendPlatform.html">MicrofrontendPlatform</a>
 */
public interface MicrofrontendPlatform {

  /**
   * Starts the SCION Microfrontend Platform host.
   *
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/MicrofrontendPlatformHost.html#start"
   */
  CompletableFuture<Browser> startHost(final MicrofrontendPlatformConfig config);

  /**
   * Starts the SCION Microfrontend Platform host. If headless is false, then an additional shell that contains the microfrontend platform
   * host browser context is opened.
   */
  CompletableFuture<Browser> startHost(final MicrofrontendPlatformConfig config, boolean headless);

  /**
   * Registers an interceptor for intercepting messages before being dispatched. Interceptors must be registered prior to starting the host.
   */
  <T> void registerMessageInterceptor(final String topic, final MessageInterceptor<T> interceptor);

  /**
   * Registers an interceptor for intercepting messages before being dispatched. Interceptors must be registered prior to starting the host.
   */
  <T> void registerMessageInterceptor(final String topic, final MessageInterceptor<T> interceptor, final Type payloadClazz);

  /**
   * Registers an interceptor for intercepting intents before being dispatched. The interceptor will receive all intents independent of the
   * host's intentions. Interceptors must be registered prior to starting the host.
   */
  <T> void registerIntentInterceptor(final String type, final IntentInterceptor<T> interceptor);

  /**
   * Registers an interceptor for intercepting intents before being dispatched. The interceptor will receive all intents independent of the
   * host's intentions. Interceptors must be registered prior to starting the host.
   */
  <T> void registerIntentInterceptor(final String type, final IntentInterceptor<T> interceptor, final Type payloadClazz);

  /**
   * Registers an interceptor for intercepting intents before being dispatched. The interceptor will receive all intents independent of the
   * host's intentions. Interceptors must be registered prior to starting the host.
   */
  <T> void registerIntentInterceptor(final String type, final Qualifier qualifier, final IntentInterceptor<T> interceptor);

  /**
   * Registers an interceptor for intercepting intents before being dispatched. The interceptor will receive all intents independent of the
   * host's intentions. Interceptors must be registered prior to starting the host.
   */
  <T> void registerIntentInterceptor(final String type, final Qualifier qualifier, final IntentInterceptor<T> interceptor,
      final Type payloadClazz);

}
