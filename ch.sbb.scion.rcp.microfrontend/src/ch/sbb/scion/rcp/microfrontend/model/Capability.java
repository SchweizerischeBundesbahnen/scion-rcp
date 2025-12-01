package ch.sbb.scion.rcp.microfrontend.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @see <a href="https://microfrontend-platform-api.scion.vercel.app/interfaces/Capability.html">Capability</a>
 */
@Accessors(fluent = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Capability {

  /**
   * Categorizes the capability in terms of its functional semantics (e.g., `microfrontend` if providing a microfrontend). It can be an
   * arbitrary `string` literal and has no meaning to the platform.
   */
  private String type;
  /**
   * The qualifier is a dictionary of arbitrary key-value pairs to differentiate capabilities of the same `type` and is like an abstract
   * description of the capability. It should include enough information to uniquely identify the capability. Intents must exactly match the
   * qualifier of the capability, if any. The capability qualifier allows using wildcards (such as `*` or `?`) to match multiple intents
   * simultaneously. - **Asterisk wildcard character (`*`):** Intents must contain such a property, but any value is allowed (except `null`
   * or `undefined`). Use it like this: `{property: '*'}` - **Optional wildcard character (?):**\ Intents can contain such a property. Use
   * it like this: `{property: '?'}`.
   */
  private Qualifier qualifier;
  /**
   * Specifies parameters which the intent issuer can/must pass along with the intent. Parameters are part of the contract between the
   * intent publisher and the capability provider. They do not affect the intent routing, unlike the qualifier.
   */
  private List<ParamDefinition> params;
  /**
   * Controls if this capability is visible to other micro applications. If private, which is by default, the capability is not visible to
   * other micro applications; thus, it can only be invoked or looked up by the providing micro application.
   */
  @SerializedName("private")
  private Boolean isPrivate;
  /**
   * A short description to explain the capability.
   */
  private String description;
  /**
   * Arbitrary metadata to be associated with the capability.
   */
  private Properties properties;
  /**
   * Metadata about the capability (read-only, exclusively managed by the platform).
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

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/interfaces/ParamDefinition.html"
   */
  @Accessors(fluent = true)
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @ToString
  public static class ParamDefinition {

    /**
     * Specifies the name of the parameter.
     */
    private String name;
    /**
     * Describes the parameter and its usage in more detail.
     */
    private String description;
    /**
     * Specifies whether the parameter must be passed along with the intent.
     */
    private Boolean required;

  }

}