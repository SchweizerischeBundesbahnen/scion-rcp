package ch.sbb.scion.rcp.microfrontend.model;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @see <a href="https://microfrontend-platform-api.scion.vercel.app/interfaces/TopicMessage.html">TopicMessage</a>
 */
@Accessors(fluent = true)
@Getter
@NoArgsConstructor
@ToString(callSuper = true)
public class TopicMessage<T> extends Message {

  private String topic;
  private T body;
  private Map<String, Object> params;
  private boolean retain;

  @Builder
  private TopicMessage(final Map<String, Object> headers, final String topic, final T body, final Map<String, Object> params,
      final boolean retain) {
    super(headers);
    this.topic = topic;
    this.body = body;
    this.params = params;
    this.retain = retain;
  }

}
