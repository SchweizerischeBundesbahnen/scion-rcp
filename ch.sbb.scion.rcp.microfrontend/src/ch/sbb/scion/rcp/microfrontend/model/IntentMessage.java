package ch.sbb.scion.rcp.microfrontend.model;

/**
 * @see https://scion-microfrontend-platform-api.vercel.app/interfaces/IntentMessage.html
 */
public class IntentMessage<T> extends Message {
  public Intent intent;
  public T body;
  public Capability capability;
}
