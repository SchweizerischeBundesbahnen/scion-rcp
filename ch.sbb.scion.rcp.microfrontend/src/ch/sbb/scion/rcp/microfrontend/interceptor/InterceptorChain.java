package ch.sbb.scion.rcp.microfrontend.interceptor;

import ch.sbb.scion.rcp.microfrontend.model.Message;

/**
 * Controls how to proceed with an intercepted message.
 */
public interface InterceptorChain {

  /**
   * Invoke to continue the chain of interceptors with given message and to dispatch
   * the message to subscribers.
   */
  void doContinue(Message message);

  /**
   * Invoke to swallow the intercepted message.
   * 
   * The chain of interceptors is not continued nor the message dispatched to
   * subscribers.
   */
  void doSwallow();

  /**
   * Invoke to reject the intercepted message and to report the passed error back to the
   * sender.
   * 
   * The chain of interceptors is not continued nor the message dispatched to
   * subscribers.
   */
  void doReject(String error);
}
