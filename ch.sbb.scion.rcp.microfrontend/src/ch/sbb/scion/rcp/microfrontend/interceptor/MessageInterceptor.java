package ch.sbb.scion.rcp.microfrontend.interceptor;

import ch.sbb.scion.rcp.microfrontend.MicrofrontendPlatform;
import ch.sbb.scion.rcp.microfrontend.model.TopicMessage;

/**
 * Allows intercepting messages prior to their publication. Interceptors are to be registered via
 * {@link MicrofrontendPlatform#registerMessageInterceptor} prior to starting the host.
 */
public interface MessageInterceptor<T> {

  /**
   * Intercepts a message before being published to its topic. Decide whether to continue publishing by passing the message to
   * {@link InterceptorChain#doContinue}, or to reject publishing by invoking {@link InterceptorChain#doReject}, or to swallow the message
   * by calling {@link InterceptorChain#doSwallow}. If rejecting, the error is transported to the message sender.
   */
  void intercept(TopicMessage<T> message, InterceptorChain chain);
}
