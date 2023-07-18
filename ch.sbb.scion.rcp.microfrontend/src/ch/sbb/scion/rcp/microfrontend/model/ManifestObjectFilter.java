package ch.sbb.scion.rcp.microfrontend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @see "https://scion-microfrontend-platform-api.vercel.app/interfaces/ManifestObjectFilter.html"
 */
@Accessors(fluent = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ManifestObjectFilter {

  /**
   * Manifest objects of the given identity.
   */
  private String id;
  /**
   * Manifest objects of the given function type.
   */
  private String type;
  /**
   * Manifest objects matching the given qualifier.
   */
  private Qualifier qualifier;
  /**
   * Manifest objects provided by the given app.
   */
  private String appSymbolicName;

}