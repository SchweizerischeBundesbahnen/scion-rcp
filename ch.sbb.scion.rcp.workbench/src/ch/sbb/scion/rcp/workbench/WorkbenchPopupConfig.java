package ch.sbb.scion.rcp.workbench;

import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Configures the popup to display a microfrontend in an Eclipse dialog using the {@link IWorkbenchPopupService}
 */
@Accessors(fluent = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class WorkbenchPopupConfig {

  /**
   * Controls the initial location of the popup. Currently only a top left anchor is supported; i.e., the top and left fields of the anchor
   * specify the location of the top left corner of the Eclipse dialog in parent shell coordinates. The parent shell is the shell of the
   * view that the popup belongs to.
   */
  private WorkbenchPopupOrigin anchor;

  /**
   * Allows passing data to the popup microfrontend. The popup provider can declare mandatory and optional parameters. No additional
   * parameters may be included. Refer to the documentation of the capability for more information.
   */
  private Map<String, Object> params;

  /**
   * Indicates whether the popup should close if the escape key is pressed.
   */
  private Boolean closeOnEscape;

  /**
   * Indicates whether the popup should close if it loses focus.
   */
  private Boolean closeOnFocusLost;

  /**
   * Specifies the view the popup belongs to.
   */
  private String viewId;

}
