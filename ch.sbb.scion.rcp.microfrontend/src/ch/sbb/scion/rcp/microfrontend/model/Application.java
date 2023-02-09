package ch.sbb.scion.rcp.microfrontend.model;

/**
 * @see "https://scion-microfrontend-platform-api.vercel.app/interfaces/Application.html"
 */
public class Application {

  /**
   * Unique symbolic name of the application.
   */
  public String symbolicName;
  /**
   * Name of the application as specified in the manifest.
   */
  public String name;
  /**
   * URL to the application root.
   */
  public String baseUrl;
  /**
   * Specifies the origin where message from this application must originate from. Messages of a different origin will be rejected.
   */
  public String messageOrigin;
  /**
   * URL to the manifest of this application.
   */
  public String manifestUrl;
  /**
   * Maximum time (in milliseconds) that the host waits until the manifest for this application is loaded. This is the effective timeout,
   * i.e, either the application-specific timeout as defined in {@link ApplicationConfig#manifestLoadTimeout}, or the global timeout as
   * defined in {@link MicrofrontendPlatformConfig#manifestLoadTimeout}, otherwise `undefined`.
   */
  public Integer manifestLoadTimeout;
  /**
   * Maximum time (in milliseconds) that the host waits for this application to signal readiness. This is the effective timeout, i.e, either
   * the application-specific timeout as defined in {@link ApplicationConfig#activatorLoadTimeout}, or the global timeout as defined in
   * {@link MicrofrontendPlatformConfig#activatorLoadTimeout}, otherwise `undefined`.
   */
  public Integer activatorLoadTimeout;
  /**
   * Indicates whether or not capability scope check is disabled for this application.
   */
  public boolean scopeCheckDisabled;
  /**
   * Indicates whether or not this application can issue intents for which it has not declared a respective intention.
   */
  public boolean intentionCheckDisabled;
  /**
   * Indicates whether or not 'Intention Registration API' is disabled for this application.
   */
  public boolean intentionRegisterApiDisabled;
}
