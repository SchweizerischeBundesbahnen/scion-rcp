package ch.sbb.scion.rcp.microfrontend.model;

import java.util.Map;

import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @see <a href="https://scion-microfrontend-platform-api.vercel.app/interfaces/RequestOptions.html">RequestOptions</a>
 */
@NoArgsConstructor
@ToString(callSuper = true)
public final class RequestOptions extends PublishOptions {

  public RequestOptions(final Map<String, ?> headers) {
    super(headers);
  }

  public RequestOptions(final Map<String, ?> headers, final Boolean retain) {
    super(headers, retain);
  }

}