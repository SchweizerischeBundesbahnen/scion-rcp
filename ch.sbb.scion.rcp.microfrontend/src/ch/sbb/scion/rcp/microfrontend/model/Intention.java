package ch.sbb.scion.rcp.microfrontend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @see <a href="https://scion-microfrontend-platform-api.vercel.app/interfaces/Intention.html">Intention</a>
 */
@Accessors(fluent = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Intention {

  /**
   * The type of capability to interact with.
   */
  private String type;
  /**
   * Qualifies the capability which to interact with. The qualifier is a dictionary of arbitrary key-value pairs to differentiate
   * capabilities of the same `type`. The intention must exactly match the qualifier of the capability, if any. The intention qualifier
   * allows using wildcards (such as `*` or `?`) to match multiple capabilities simultaneously. In the intention, the following wildcards
   * are supported: - **Asterisk wildcard character (`*`):**\ Matches capabilities with such a qualifier property no matter of its value
   * (except `null` or `undefined`). Use it like this: `{property: '*'}`. - **Optional wildcard character (?):**\ Matches capabilities
   * regardless of having or not having such a property. Use it like this: `{property: '?'}`. - **Partial wildcard (`**`):** Matches
   * capabilities even if having additional properties. Use it like this: `{'*': '*'}`.
   */
  private Qualifier qualifier;
  /**
   * Metadata about this intention (read-only, exclusively managed by the platform).
   * <p>
   * ignore
   */
  private Metadata metadata;

  @Accessors(fluent = true)
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @ToString
  public static class Metadata {

    /**
     * Unique identity of this intent declaration.
     */
    private String id;

    /**
     * Symbolic name of the application which declares this intention.
     */
    private String appSymbolicName;
  }
}
