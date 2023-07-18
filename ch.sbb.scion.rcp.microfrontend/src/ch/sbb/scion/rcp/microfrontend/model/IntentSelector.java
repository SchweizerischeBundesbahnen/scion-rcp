package ch.sbb.scion.rcp.microfrontend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @see <a href="https://scion-microfrontend-platform-api.vercel.app/interfaces/IntentSelector.html">IntentSelector</a>
 */
@Accessors(fluent = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class IntentSelector {

  /**
   * If specified, filters intents of the given type.
   */
  private String type;
  /**
   * If specified, filters intents matching the given qualifier. You can use the asterisk wildcard (*) to match multiple intents.
   * <ul>
   * <li>Asterisk wildcard character (*) matches intents with such a qualifier property no matter of its value (except null). Use it like
   * this: <code>qualifier.set("property", "*")</code>.</li>
   * <li>Partial wildcard (**) Matches intents even if having additional properties. Use it like this:
   * <code>qualifier.set("*", "*")</code>.</li>
   * </ul>
   */
  private Qualifier qualifier;

}
