package ch.sbb.scion.rcp.microfrontend.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * @see "https://scion-microfrontend-platform-api.vercel.app/interfaces/Capability.html"
 */
public class Capability {

  /**
   * Categorizes the capability in terms of its functional semantics (e.g., `microfrontend` if providing a microfrontend). It can be an
   * arbitrary `string` literal and has no meaning to the platform.
   */
  public String type;
  /**
   * The qualifier is a dictionary of arbitrary key-value pairs to differentiate capabilities of the same `type` and is like an abstract
   * description of the capability. It should include enough information to uniquely identify the capability. Intents must exactly match the
   * qualifier of the capability, if any. The capability qualifier allows using wildcards (such as `*` or `?`) to match multiple intents
   * simultaneously. - **Asterisk wildcard character (`*`):** Intents must contain such a property, but any value is allowed (except `null`
   * or `undefined`). Use it like this: `{property: '*'}` - **Optional wildcard character (?):**\ Intents can contain such a property. Use
   * it like this: `{property: '?'}`.
   */
  public Qualifier qualifier;
  /**
   * Specifies parameters which the intent issuer can/must pass along with the intent. Parameters are part of the contract between the
   * intent publisher and the capability provider. They do not affect the intent routing, unlike the qualifier.
   */
  public List<ParamDefinition> params;
  /**
   * Controls if this capability is visible to other micro applications. If private, which is by default, the capability is not visible to
   * other micro applications; thus, it can only be invoked or looked up by the providing micro application.
   */
  @SerializedName("private")
  public Boolean isPrivate;
  /**
   * A short description to explain the capability.
   */
  public String description;
  /**
   * Arbitrary metadata to be associated with the capability.
   */
  public Properties properties;
  /**
   * Metadata about the capability (read-only, exclusively managed by the platform).
   *
   * @ignore
   */
  public Metadata metadata;

  public Capability type(final String type) {
    this.type = type;
    return this;
  }

  public Capability qualifier(final Qualifier qualifier) {
    this.qualifier = qualifier;
    return this;
  }

  public Capability params(final List<ParamDefinition> params) {
    this.params = params;
    return this;
  }

  public Capability isPrivate(final Boolean isPrivate) {
    this.isPrivate = isPrivate;
    return this;
  }

  public Capability description(final String description) {
    this.description = description;
    return this;
  }

  public Capability properties(final Properties properties) {
    this.properties = properties;
    return this;
  }

  public static class Metadata {

    /**
     * Unique identity of this intent declaration.
     */
    public String id;
    /**
     * Symbolic name of the application which declares this intention.
     */
    public String appSymbolicName;
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/interfaces/ParamDefinition.html"
   */
  public static class ParamDefinition {

    /**
     * Specifies the name of the parameter.
     */
    public String name;
    /**
     * Describes the parameter and its usage in more detail.
     */
    public String description;
    /**
     * Specifies whether the parameter must be passed along with the intent.
     */
    public Boolean required;

    public ParamDefinition name(final String name) {
      this.name = name;
      return this;
    }

    public ParamDefinition description(final String description) {
      this.description = description;
      return this;
    }

    public ParamDefinition required(final Boolean required) {
      this.required = required;
      return this;
    }

  }

}