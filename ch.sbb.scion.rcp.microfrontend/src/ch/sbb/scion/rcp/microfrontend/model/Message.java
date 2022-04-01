package ch.sbb.scion.rcp.microfrontend.model;

import java.util.Map;

/**
 * @see https://scion-microfrontend-platform-api.vercel.app/interfaces/Message.html
 */
public abstract class Message {
  public Map<String, Object> headers;
}
