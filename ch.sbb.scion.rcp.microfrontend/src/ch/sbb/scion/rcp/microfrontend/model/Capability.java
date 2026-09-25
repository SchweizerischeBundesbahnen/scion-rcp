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
   */
  private Metadata metadata;
  // todo: set default values?
  /**
   * Controls whether this capability is inactive. Defaults to <code>false</code>.
   * <p>
   * Capabilities can be marked as inactive in a capability interceptor, for example, based on user permissions. Inactive capabilities are
   * unavailable to applications but still visible in the SCION DevTools for discovery.
   * <p>
   * Note: Applications configured with <code>capabilityActiveCheckDisabled</code> can still access inactive capabilities (discouraged).
   */
  @SerializedName("inactive")
  private Boolean isInactive;

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
    private boolean isRequired;
    /**
     * Allows deprecating the parameter. Can either be {@code null}, {@code Boolean#TRUE} or an instance of {@code DeprecationInfo}.
     * <p>
     * It is good practice to explain the deprecation, provide the date of removal, and how to migrate. Use the {@link DeprecationInfo} for
     * this purpose.
     */
    private Object deprecated;
    /**
     * Allows the declaration of additional metadata that can be interpreted in an interceptor, for example.
     */
    private Properties properties;
    /**
     * Defines a default value. Only applies to optional parameters.
     * <p>
     * The default value is used when the parameter is not provided.
     */
    private Object defaultValue;
  }

  /**
   * Provides information about the deprecation of a capability parameter.
   *
   * @see "https://microfrontend-platform-api.scion.vercel.app/interfaces/ParamDefinition.html"
   */
  @Accessors(fluent = true)
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @ToString
  public static class DeprecationInfo {

    /**
     * Explanation of the deprecation. It is good practice to mention the date of removal, and describe how to migrate.
     */
    private String message;
    /**
     * Specifies the name of the parameter the should be used instead, if there is any. At runtime, this will map the parameter to the
     * specified replacement, allowing for straightforward migration on the provider side.
     */
    private String useInstead;
  }

}