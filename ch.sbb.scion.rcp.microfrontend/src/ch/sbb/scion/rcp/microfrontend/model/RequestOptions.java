package ch.sbb.scion.rcp.microfrontend.model;

import java.util.Map;

/**
 * @see https://scion-microfrontend-platform-api.vercel.app/interfaces/RequestOptions.html
 */
public class RequestOptions extends PublishOptions {

  @Override
  public RequestOptions headers(Map<String, ?> headers) {
    return (RequestOptions) super.headers(headers);
  }

  @Override
  public RequestOptions retain(boolean retain) {
    return (RequestOptions) super.retain(retain);
  }

  @Override
  public RequestOptions retain() {
    return (RequestOptions) super.retain();
  }
}