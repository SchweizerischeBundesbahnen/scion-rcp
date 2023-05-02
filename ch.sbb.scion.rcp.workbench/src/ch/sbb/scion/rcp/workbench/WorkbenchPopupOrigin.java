package ch.sbb.scion.rcp.workbench;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * A union of coordinates expressed with respect to the parent shell of the Eclipse dialog that contains the popup.
 */
@Accessors(fluent = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class WorkbenchPopupOrigin {

  private Integer x;

  private Integer y;

  private Integer top;

  private Integer right;

  private Integer bottom;

  private Integer left;

  private Integer width;

  private Integer height;

}
