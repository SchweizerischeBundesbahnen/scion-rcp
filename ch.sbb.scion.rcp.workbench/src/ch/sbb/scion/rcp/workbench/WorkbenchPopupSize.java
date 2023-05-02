package ch.sbb.scion.rcp.workbench;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Represents the fixed size of a popup window. Currently, only width and height values are supported.
 */
@Accessors(fluent = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class WorkbenchPopupSize {

  /**
   * The fixed width of the popup window. Currently, only pixels are supported, e.g., 500px
   */
  private String width;

  /**
   * The fixed height of the popup window. Currently, only pixels are supported, e.g., 500px
   */
  private String height;

}
