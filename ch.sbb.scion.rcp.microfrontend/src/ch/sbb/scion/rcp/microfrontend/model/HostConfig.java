package ch.sbb.scion.rcp.microfrontend.model;

/**
 * @see https://scion-microfrontend-platform-api.vercel.app/classes/HostConfig.html
 */
public class HostConfig {

  /**
   * Symbolic name of the host. If not set, 'host' is used as the symbolic name of
   * the host.
   *
   * The symbolic name must be unique and contain only lowercase alphanumeric
   * characters and hyphens.
   */
  public String symbolicName;
  /**
   * The manifest of the host.
   *
   * The manifest can be passed either as an {@link Manifest object literal} or
   * specified as a URL to be loaded over the network. Providing a manifest lets
   * the host contribute capabilities or declare intentions.
   */
  public Object manifest;
  /**
   * Controls whether the host can interact with private capabilities of other
   * micro applications.
   *
   * By default, scope check is enabled. Disabling scope check is strongly
   * discouraged.
   */
  public Boolean scopeCheckDisabled;
  /**
   * Controls whether the host can interact with the capabilities of other apps
   * without having to declare respective intentions.
   *
   * By default, intention check is enabled. Disabling intention check is strongly
   * discouraged.
   */
  public Boolean intentionCheckDisabled;
  /**
   * Controls whether the host can register and unregister intentions dynamically
   * at runtime.
   *
   * By default, this API is disabled. Enabling this API is strongly discouraged.
   */
  public Boolean intentionRegisterApiDisabled;
  /**
   * Maximum time (in milliseconds) that the platform waits to receive dispatch
   * confirmation for messages sent by the host until rejecting the publishing
   * Promise. By default, a timeout of 10s is used.
   */
  public Long messageDeliveryTimeout;

  public HostConfig symbolicName(String symbolicName) {
    this.symbolicName = symbolicName;
    return this;
  }

  public HostConfig manifest(Manifest manifest) {
    this.manifest = manifest;
    return this;
  }

  public HostConfig manifest(String manifestUrl) {
    this.manifest = manifestUrl;
    return this;
  }

  public HostConfig scopeCheckDisabled(Boolean scopeCheckDisabled) {
    this.scopeCheckDisabled = scopeCheckDisabled;
    return this;
  }

  public HostConfig intentionCheckDisabled(Boolean intentionCheckDisabled) {
    this.intentionCheckDisabled = intentionCheckDisabled;
    return this;
  }

  public HostConfig intentionRegisterApiDisabled(Boolean intentionRegisterApiDisabled) {
    this.intentionRegisterApiDisabled = intentionRegisterApiDisabled;
    return this;
  }

  public HostConfig messageDeliveryTimeout(Long messageDeliveryTimeout) {
    this.messageDeliveryTimeout = messageDeliveryTimeout;
    return this;
  }
}
