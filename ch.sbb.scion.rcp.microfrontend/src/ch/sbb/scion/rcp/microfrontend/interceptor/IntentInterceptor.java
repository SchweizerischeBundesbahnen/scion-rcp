package ch.sbb.scion.rcp.microfrontend.interceptor;

import ch.sbb.scion.rcp.microfrontend.MicrofrontendPlatform;
import ch.sbb.scion.rcp.microfrontend.model.IntentMessage;

/**
 * Allows intercepting intents prior to their publication. Interceptors are to be registered via
 * {@link MicrofrontendPlatform#registerIntentInterceptor} prior to starting the host.
 */
public interface IntentInterceptor<T> {

  /**
   * Intercepts an intent before being dispatched. Decide whether to continue publishing by passing the intent to
   * {@link InterceptorChain#doContinue}, or to reject publishing by invoking {@link InterceptorChain#doReject}, or to swallow the intent by
   * calling {@link InterceptorChain#doSwallow}. If rejecting, the error is transported to the sender.
   */
  void intercept(IntentMessage<T> intentMessage, InterceptorChain chain);
}
