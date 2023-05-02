package ch.sbb.scion.rcp.microfrontend.model;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @see <a href="https://scion-microfrontend-platform-api.vercel.app/interfaces/IntentMessage.html">IntentMessage</a>
 */
@Accessors(fluent = true)
@Getter
@NoArgsConstructor
@ToString(callSuper = true)
public class IntentMessage<T> extends Message {

  private Intent intent;
  private T body;
  private Capability capability;

  @Builder
  public IntentMessage(final Map<String, Object> headers, final Intent intent, final T body, final Capability capability) {
    super(headers);
    this.intent = intent;
    this.body = body;
    this.capability = capability;
  }

}
