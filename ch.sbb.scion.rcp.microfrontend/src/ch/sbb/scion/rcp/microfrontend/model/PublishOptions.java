package ch.sbb.scion.rcp.microfrontend.model;

import java.util.Map;

/**
 * @see https://scion-microfrontend-platform-api.vercel.app/interfaces/PublishOptions.html
 */
public class PublishOptions {
  private Map<String, ?> headers;
  private Boolean retain;

  public Map<String, ?> getHeaders() {
    return headers;
  }

  public Boolean isRetain() {
    return retain;
  }

  public PublishOptions headers(Map<String, ?> headers) {
    this.headers = headers;
    return this;
  }

  public PublishOptions retain(boolean retain) {
    this.retain = retain;
    return this;
  }

  public PublishOptions retain() {
    retain(true);
    return this;
  }
}
