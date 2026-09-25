package ch.sbb.scion.rcp.microfrontend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @see <a href="https://microfrontend-platform-api.scion.vercel.app/interfaces/Application.html">Application</a>
 */
@Accessors(fluent = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Application {

  /**
   * Unique symbolic name of the application.
   */
  private String symbolicName;
  /**
   * Name of the application as specified in the manifest.
   */
  private String name;
  /**
   * URL to the application root.
   */
  private String baseUrl;
  /**
   * URL to the manifest of this application.
   */
  private String manifestUrl;
  /**
   * Maximum time (in milliseconds) that the host waits until the manifest for this application is loaded. This is the effective timeout,
   * i.e, either the application-specific timeout as defined in {@link ApplicationConfig#manifestLoadTimeout}, or the global timeout as
   * defined in {@link MicrofrontendPlatformConfig#manifestLoadTimeout}, otherwise `undefined`.
   */
  private Integer manifestLoadTimeout;
  /**
   * Maximum time (in milliseconds) that the host waits for this application to signal readiness. This is the effective timeout, i.e, either
   * the application-specific timeout as defined in {@link ApplicationConfig#activatorLoadTimeout}, or the global timeout as defined in
   * {@link MicrofrontendPlatformConfig#activatorLoadTimeout}, otherwise `undefined`.
   */
  private Integer activatorLoadTimeout;
  /**
   * Indicates whether or not capability scope check is disabled for this application.
   */
  private boolean scopeCheckDisabled;
  /**
   * Indicates whether or not this application can issue intents for which it has not declared a respective intention.
   */
  private boolean intentionCheckDisabled;
  /**
   * Indicates whether or not 'Intention Registration API' is disabled for this application.
   */
  private boolean intentionRegisterApiDisabled;
  /**
   * Indicates whether this application is allowed to access inactive capabilities.
   */
  private boolean capabilityActiveCheckDisabled;

  // todo: platformVersion? <- is this information even relevant to the Java side?
}
