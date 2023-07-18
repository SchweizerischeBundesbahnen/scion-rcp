package ch.sbb.scion.rcp.microfrontend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @see <a href="https://scion-microfrontend-platform-api.vercel.app/interfaces/ApplicationConfig.html">ApplicationConfig</a>
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
   * Specifies the origin that messages from this application must have. Messages of a different origin will be rejected. If not explicitly
   * set, the origin is derived from the manifest URL or the base URL as specified in the manifest file. The explicit setting of an origin
   * is required, for example, when bridging messages.
   */
  private String messageOrigin;

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
   * Controls whether this micro application can interact with private capabilities of other micro applications. By default, scope check is
   * enabled. Disabling scope check is strongly discouraged.
   */
  private Boolean scopeCheckDisabled;

  /**
   * Controls whether this micro application can interact with the capabilities of other apps without having to declare respective
   * intentions. By default, intention check is enabled. Disabling intention check is strongly discouraged.
   */
  private Boolean intentionCheckDisabled;

  /**
   * Controls whether this micro application can register and unregister intentions dynamically at runtime. By default, this API is
   * disabled. Enabling this API is strongly discouraged.
   */
  private Boolean intentionRegisterApiDisabled;

}
