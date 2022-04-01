package ch.sbb.scion.rcp.microfrontend.model;

import java.util.List;
import java.util.Map;

/**
 * @see https://scion-microfrontend-platform-api.vercel.app/classes/MicrofrontendPlatformConfig.html
 */
public class MicrofrontendPlatformConfig {
  /**
   * Lists the micro applications able to connect to the platform to interact with
   * other micro applications.
   */
  public List<ApplicationConfig> applications;
  /**
   * Configures the interaction of the host application with the platform.
   *
   * As with micro applications, you can provide a manifest for the host, allowing
   * the host to contribute capabilities and declare intentions.
   */
  public HostConfig host;
  /**
   * Controls whether the Activator API is enabled.
   *
   * Activating the Activator API enables micro applications to contribute
   * `activator` microfrontends. Activator microfrontends are loaded at platform
   * startup for the entire lifecycle of the platform. An activator is a startup
   * hook for micro applications to initialize or register message or intent
   * handlers to provide functionality.
   *
   * By default, this API is enabled.
   */
  public Boolean activatorApiDisabled;;
  /**
   * Maximum time (in milliseconds) that the platform waits until the manifest of
   * an application is loaded. You can set a different timeout per application via
   * {@link ApplicationConfig.manifestLoadTimeout}. If not set, by default, the
   * browser's HTTP fetch timeout applies.
   *
   * Consider setting this timeout if, for example, a web application firewall
   * delays the responses of unavailable applications.
   */
  public Long manifestLoadTimeout;;
  /**
   * Maximum time (in milliseconds) for each application to signal readiness.
   *
   * If specified and activating an application takes longer, the host logs an
   * error and continues startup. Has no effect for applications which provide no
   * activator(s) or are not configured to signal readiness. You can set a
   * different timeout per application via
   * {@link ApplicationConfig.activatorLoadTimeout}.
   *
   * By default, no timeout is set, meaning that if an app fails to signal
   * readiness, e.g., due to an error, that app would block the host startup
   * process indefinitely. It is therefore recommended to specify a timeout
   * accordingly.
   */
  public Long activatorLoadTimeout;;
  /**
   * Interval (in seconds) at which connected clients must send a heartbeat to
   * indicate connectivity to the host.
   *
   * By default, if not set, a heartbeat interval of 60s is used.
   */
  public Long heartbeatInterval;;
  /**
   * Defines user-defined properties which can be read by micro applications via
   * {@link PlatformPropertyService}.
   */
  public Map<String, ?> properties;

  public MicrofrontendPlatformConfig applications(List<ApplicationConfig> applications) {
    this.applications = applications;
    return this;
  }

  public MicrofrontendPlatformConfig host(HostConfig host) {
    this.host = host;
    return this;
  }

  public MicrofrontendPlatformConfig activatorApiDisabled(Boolean activatorApiDisabled) {
    this.activatorApiDisabled = activatorApiDisabled;
    return this;
  }

  public MicrofrontendPlatformConfig manifestLoadTimeout(Long manifestLoadTimeout) {
    this.manifestLoadTimeout = manifestLoadTimeout;
    return this;
  }

  public MicrofrontendPlatformConfig activatorLoadTimeout(Long activatorLoadTimeout) {
    this.activatorLoadTimeout = activatorLoadTimeout;
    return this;
  }

  public MicrofrontendPlatformConfig heartbeatInterval(Long heartbeatInterval) {
    this.heartbeatInterval = heartbeatInterval;
    return this;
  }

  public MicrofrontendPlatformConfig properties(Map<String, ?> properties) {
    this.properties = properties;
    return this;
  }
}
