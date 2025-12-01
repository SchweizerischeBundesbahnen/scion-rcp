package ch.sbb.scion.rcp.microfrontend.model;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @see <a href="https://microfrontend-platform-api.scion.vercel.app/interfaces/PublishOptions.html">PublishOptions</a>
 */
@Accessors(fluent = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PublishOptions {

  private Map<String, ?> headers;

  @SerializedName("retain")
  private Boolean isRetain;

  public PublishOptions(final Map<String, ?> headers) {
    this.headers = headers;
  }

  public PublishOptions(final boolean retain) {
    this.isRetain = Boolean.valueOf(retain);
  }

}
