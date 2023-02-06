package ch.sbb.scion.rcp.microfrontend.model;

import java.util.Map;

/**
 * @see https://scion-microfrontend-platform-api.vercel.app/interfaces/TopicMessage.html
 */
public class TopicMessage<T> extends Message {

  public String topic;
  public T body;
  public Map<String, Object> params;
  public boolean retain;
}
