package ch.sbb.scion.rcp.microfrontend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @see <a href="https://microfrontend-platform-api.scion.vercel.app/interfaces/ApplicationConfig.html">ApplicationConfig</a>
 */
@Accessors(fluent = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ApplicationConfig {

  /**
   * Unique symbolic name of this micro application. The symbolic name must be unique and contain only lowercase alphanumeric characters and
   * hyphens.
   */
  private String symbolicName;

  /**
   * URL to the application manifest.
   */
  private String manifestUrl;

  /**
   * Specifies an additional origin (in addition to the origin of the application) from which the application is allowed to connect to the
   * platform.
   * <p>
   * By default, if not set, the application is allowed to connect from the origin of the manifest URL or the base URL as specified in the
   * manifest file. Setting an additional origin may be necessary if, for example, integrating microfrontends into a rich client, enabling
   * an integrator to bridge messages between clients and host across browser boundaries.
   */
  private String secondaryOrigin;

  /**
   * Maximum time (in milliseconds) that the host waits until the manifest for this application is loaded. If set, overrides the global
   * timeout as configured in {@link MicrofrontendPlatformConfig#manifestLoadTimeout}.
   */
  private Long manifestLoadTimeout;

  /**
   * Maximum time (in milliseconds) for this application to signal readiness. If activating this application takes longer, the host logs an
   * error and continues startup. If set, overrides the global timeout as configured in
   * {@link MicrofrontendPlatformConfig#activatorLoadTimeout}.
   */
  private Long activatorLoadTimeout;

  /**
   * Excludes this micro application from registration, e.g. to not register it in a specific environment.
   */
  private Boolean exclude;

  /**
   * Allows this application to access private capabilities of other applications.
   * <p>
   * Disabling this check is discouraged. Enabled by default.
   */
  private Boolean scopeCheckDisabled;

  /**
   * Allows this application to access public capabilities of other applications without declaring an intention.
   * <p>
   * Disabling this check is discouraged. Enabled by default.
   */
  private Boolean intentionCheckDisabled;

  /**
   * Allows this application to register and unregister intentions at runtime.
   * <p>
   * Enabling this API is discouraged. Disabled by default.
   */
  private Boolean intentionRegisterApiDisabled;

  /**
   * Allows this application to access inactive capabilities.
   * <p>
   * Disabling this check is discouraged. Enabled by default.
   */
  private Boolean capabilityActiveCheckDisabled;

}
