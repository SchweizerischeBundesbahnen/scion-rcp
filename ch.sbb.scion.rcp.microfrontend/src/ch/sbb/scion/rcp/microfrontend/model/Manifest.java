package ch.sbb.scion.rcp.microfrontend.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @see <a href="https://microfrontend-platform-api.scion.vercel.app/interfaces/Manifest.html">Manifest</a>
 */
@Accessors(fluent = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Manifest {

  /**
   * The name of the application, e.g. displayed in the DevTools.
   */
  private String name;
  /**
   * URL to the application root. The URL can be fully qualified, or a path relative to the origin under which serving the manifest file. If
   * not specified, the origin of the manifest file acts as the base URL. The platform uses the base URL to resolve microfrontends such as
   * activator endpoints. For a Single Page Application that uses hash-based routing, you typically specify the hash symbol (`#`) as the
   * base URL.
   */
  private String baseUrl;
  /**
   * Functionality which this application intends to use.
   */
  private List<Intention> intentions;
  /**
   * Functionality which this application provides that qualified apps can call via intent.
   */
  private List<Capability> capabilities;

}
