package ch.sbb.scion.rcp.microfrontend.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @see <a href=
 *      "https://microfrontend-platform-api.scion.vercel.app/classes/MicrofrontendPlatformConfig.html">MicrofrontendPlatformConfig</a>
 */
@Accessors(fluent = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MicrofrontendPlatformConfig {

  /**
   * Lists the micro applications able to connect to the platform to interact with other micro applications.
   */
  private List<ApplicationConfig> applications;
  /**
   * Configures the interaction of the host application with the platform. As with micro applications, you can provide a manifest for the
   * host, allowing the host to contribute capabilities and declare intentions.
   */
  private HostConfig host;
  /**
   * Controls whether the Activator API is enabled. Activating the Activator API enables micro applications to contribute `activator`
   * microfrontends. Activator microfrontends are loaded at platform startup for the entire lifecycle of the platform. An activator is a
   * startup hook for micro applications to initialize or register message or intent handlers to provide functionality. By default, this API
   * is enabled.
   */
  private Boolean activatorApiDisabled;
  /**
   * Maximum time (in milliseconds) that the platform waits until the manifest of an application is loaded. You can set a different timeout
   * per application via {@link ApplicationConfig#manifestLoadTimeout}. If not set, by default, the browser's HTTP fetch timeout applies.
   * Consider setting this timeout if, for example, a web application firewall delays the responses of unavailable applications.
   */
  private Long manifestLoadTimeout;
  /**
   * Maximum time (in milliseconds) for each application to signal readiness. If specified and activating an application takes longer, the
   * host logs an error and continues startup. Has no effect for applications which provide no activator(s) or are not configured to signal
   * readiness. You can set a different timeout per application via {@link ApplicationConfig#activatorLoadTimeout}. By default, no timeout
   * is set, meaning that if an app fails to signal readiness, e.g., due to an error, that app would block the host startup process
   * indefinitely. It is therefore recommended to specify a timeout accordingly.
   */
  private Long activatorLoadTimeout;
  /**
   * Configures the liveness probe performed at regular intervals between host and clients to detect and dispose stale clients. Clients not
   * replying to the probe are removed.
   */
  private LivenessConfig liveness;
  /**
   * Defines user-defined properties which can be read by micro applications via <code>PlatformPropertyService</code>.
   */
  private Properties properties;

  @Accessors(fluent = true)
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @ToString
  public static class LivenessConfig {

    /**
     * Interval (in seconds) at which liveness probes are performed between host and connected clients. Note that the interval must not be 0
     * and be greater than twice the timeout period to give a probe enough time to complete before performing a new probe.
     * <p>
     * By default, if not set, an interval of 60s is used.
     */
    private Integer interval;
    /**
     * Timeout (in seconds) after which a client is unregistered if not replying to the probe. Note that timeout must not be 0 and be less
     * than half the interval period to give a probe enough time to complete before performing a new probe.
     * <p>
     * By default, if not set, a timeout of 10s is used.
     */
    private Integer timeout;
  }
}
