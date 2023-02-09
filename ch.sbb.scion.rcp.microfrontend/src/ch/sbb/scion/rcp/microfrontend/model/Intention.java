package ch.sbb.scion.rcp.microfrontend.model;

/**
 * @see "https://scion-microfrontend-platform-api.vercel.app/interfaces/Intention.html"
 */
public class Intention {

  /**
   * The type of capability to interact with.
   */
  public String type;
  /**
   * Qualifies the capability which to interact with. The qualifier is a dictionary of arbitrary key-value pairs to differentiate
   * capabilities of the same `type`. The intention must exactly match the qualifier of the capability, if any. The intention qualifier
   * allows using wildcards (such as `*` or `?`) to match multiple capabilities simultaneously. In the intention, the following wildcards
   * are supported: - **Asterisk wildcard character (`*`):**\ Matches capabilities with such a qualifier property no matter of its value
   * (except `null` or `undefined`). Use it like this: `{property: '*'}`. - **Optional wildcard character (?):**\ Matches capabilities
   * regardless of having or not having such a property. Use it like this: `{property: '?'}`. - **Partial wildcard (`**`):** Matches
   * capabilities even if having additional properties. Use it like this: `{'*': '*'}`.
   */
  public Qualifier qualifier;
  /**
   * Metadata about this intention (read-only, exclusively managed by the platform).
   *
   * @ignore
   */
  public Metadata metadata;

  public Intention type(final String type) {
    this.type = type;
    return this;
  }

  public Intention qualifier(final Qualifier qualifier) {
    this.qualifier = qualifier;
    return this;
  }

  public static class Metadata {

    /**
     * Unique identity of this intent declaration.
     */
    String id;
    /**
     * Symbolic name of the application which declares this intention.
     */
    String appSymbolicName;
  }
}
