package ch.sbb.scion.rcp.microfrontend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @see <a href="https://microfrontend-platform-api.scion.vercel.app/classes/HostConfig.html">HostConfig</a>
 */
@Accessors(fluent = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class HostConfig {

  /**
   * Symbolic name of the host. If not set, 'host' is used as the symbolic name of the host. The symbolic name must be unique and contain
   * only lowercase alphanumeric characters and hyphens.
   */
  private String symbolicName;
  /**
   * The manifest of the host. The manifest can be passed either as an {@link Manifest object literal} or specified as a URL to be loaded
   * over the network. Providing a manifest lets the host contribute capabilities or declare intentions.
   */
  private Object manifest;
  /**
   * Allows the host to access private capabilities of other applications.
   * <p>
   * Disabling this check is discouraged. Enabled by default.
   */
  private Boolean scopeCheckDisabled;
  /**
   * Allows the host to access public capabilities of other applications without declaring an intention.
   * <p>
   * Disabling this check is discouraged. Enabled by default.
   */
  private Boolean intentionCheckDisabled;
  /**
   * Allows the host to register and unregister intentions at runtime.
   * <p>
   * Enabling this API is discouraged. Disabled by default.
   */
  private Boolean intentionRegisterApiDisabled;
  /**
   * Allows the host to access inactive capabilities.
   * <p>
   * Disabling this check is discouraged. Enabled by default.
   */
  private Boolean capabilityActiveCheckDisabled;
  /**
   * Maximum time (in milliseconds) that the platform waits to receive dispatch confirmation for messages sent by the host until rejecting
   * the publishing Promise. By default, a timeout of 10s is used.
   */
  private Long messageDeliveryTimeout;

}
