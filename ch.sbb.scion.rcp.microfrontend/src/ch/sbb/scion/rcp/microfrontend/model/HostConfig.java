package ch.sbb.scion.rcp.microfrontend.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @see <a href="https://scion-microfrontend-platform-api.vercel.app/classes/HostConfig.html">HostConfig</a>
 */
@Accessors(fluent = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
   * Controls whether the host can interact with private capabilities of other micro applications. By default, scope check is enabled.
   * Disabling scope check is strongly discouraged.
   */
  private Boolean scopeCheckDisabled;
  /**
   * Controls whether the host can interact with the capabilities of other apps without having to declare respective intentions. By default,
   * intention check is enabled. Disabling intention check is strongly discouraged.
   */
  private Boolean intentionCheckDisabled;
  /**
   * Controls whether the host can register and unregister intentions dynamically at runtime. By default, this API is disabled. Enabling
   * this API is strongly discouraged.
   */
  private Boolean intentionRegisterApiDisabled;
  /**
   * Maximum time (in milliseconds) that the platform waits to receive dispatch confirmation for messages sent by the host until rejecting
   * the publishing Promise. By default, a timeout of 10s is used.
   */
  private Long messageDeliveryTimeout;

}
