package ch.sbb.scion.rcp.microfrontend.model;

/**
 * @see "https://scion-microfrontend-platform-api.vercel.app/interfaces/ApplicationConfig.html"
 */
public class ApplicationConfig {

  public String symbolicName;

  public String manifestUrl;

  public String messageOrigin;

  public Long manifestLoadTimeout;

  public Long activatorLoadTimeout;

  public Boolean exclude;

  public Boolean scopeCheckDisabled;

  public Boolean intentionCheckDisabled;

  public Boolean intentionRegisterApiDisabled;

  /**
   * Unique symbolic name of this micro application. The symbolic name must be unique and contain only lowercase alphanumeric characters and
   * hyphens.
   */
  public ApplicationConfig symbolicName(final String symbolicName) {
    this.symbolicName = symbolicName;
    return this;
  }

  /**
   * URL to the application manifest.
   */
  public ApplicationConfig manifestUrl(final String manifestUrl) {
    this.manifestUrl = manifestUrl;
    return this;
  }

  /**
   * Specifies the origin that messages from this application must have. Messages of a different origin will be rejected. If not explicitly
   * set, the origin is derived from the manifest URL or the base URL as specified in the manifest file. The explicit setting of an origin
   * is required, for example, when bridging messages.
   */
  public ApplicationConfig messageOrigin(final String messageOrigin) {
    this.messageOrigin = messageOrigin;
    return this;
  }

  /**
   * Maximum time (in milliseconds) that the host waits until the manifest for this application is loaded. If set, overrides the global
   * timeout as configured in {@link MicrofrontendPlatformConfig#manifestLoadTimeout}.
   */
  public ApplicationConfig manifestLoadTimeout(final Long manifestLoadTimeout) {
    this.manifestLoadTimeout = manifestLoadTimeout;
    return this;
  }

  /**
   * Maximum time (in milliseconds) for this application to signal readiness. If activating this application takes longer, the host logs an
   * error and continues startup. If set, overrides the global timeout as configured in
   * {@link MicrofrontendPlatformConfig#activatorLoadTimeout}.
   */
  public ApplicationConfig activatorLoadTimeout(final Long activatorLoadTimeout) {
    this.activatorLoadTimeout = activatorLoadTimeout;
    return this;
  }

  /**
   * Excludes this micro application from registration, e.g. to not register it in a specific environment.
   */
  public ApplicationConfig exclude(final Boolean exclude) {
    this.exclude = exclude;
    return this;
  }

  /**
   * Controls whether this micro application can interact with private capabilities of other micro applications. By default, scope check is
   * enabled. Disabling scope check is strongly discouraged.
   */
  public ApplicationConfig scopeCheckDisabled(final Boolean scopeCheckDisabled) {
    this.scopeCheckDisabled = scopeCheckDisabled;
    return this;
  }

  /**
   * Controls whether this micro application can interact with the capabilities of other apps without having to declare respective
   * intentions. By default, intention check is enabled. Disabling intention check is strongly discouraged.
   */
  public ApplicationConfig intentionCheckDisabled(final Boolean intentionCheckDisabled) {
    this.intentionCheckDisabled = intentionCheckDisabled;
    return this;
  }

  /**
   * Controls whether this micro application can register and unregister intentions dynamically at runtime. By default, this API is
   * disabled. Enabling this API is strongly discouraged.
   */
  public ApplicationConfig intentionRegisterApiDisabled(final Boolean intentionRegisterApiDisabled) {
    this.intentionRegisterApiDisabled = intentionRegisterApiDisabled;
    return this;
  }
}
