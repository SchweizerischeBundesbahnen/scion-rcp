package ch.sbb.scion.rcp.microfrontend.model;

import java.util.List;

/**
 * @see https://scion-microfrontend-platform-api.vercel.app/interfaces/Manifest.html
 */
public class Manifest {
  /**
   * The name of the application, e.g. displayed in the DevTools.
   */
  public String name;
  /**
   * URL to the application root. The URL can be fully qualified, or a path
   * relative to the origin under which serving the manifest file. If not
   * specified, the origin of the manifest file acts as the base URL. The platform
   * uses the base URL to resolve microfrontends such as activator endpoints. For
   * a Single Page Application that uses hash-based routing, you typically specify
   * the hash symbol (`#`) as the base URL.
   */
  public String baseUrl;
  /**
   * Functionality which this application intends to use.
   */
  public List<Intention> intentions;
  /**
   * Functionality which this application provides that qualified apps can call
   * via intent.
   */
  public List<Capability> capabilities;

  public Manifest name(String name) {
    this.name = name;
    return this;
  }

  public Manifest baseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  public Manifest intentions(List<Intention> intentions) {
    this.intentions = intentions;
    return this;
  }

  public Manifest capabilities(List<Capability> capabilities) {
    this.capabilities = capabilities;
    return this;
  }
}
