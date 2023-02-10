package ch.sbb.scion.rcp.workbench.popup;

import ch.sbb.scion.rcp.workbench.SciWorkbenchPopupOrigin;

public class PopupOrigin {

  public Double x;

  public Double y;

  public Double top;

  public Double right;

  public Double bottom;

  public Double left;

  public Double width;

  public Double height;

  public SciWorkbenchPopupOrigin toSciWorkbenchPopupOrigin() {
    return new SciWorkbenchPopupOrigin().x(intValueOrNull(x)).y(intValueOrNull(y)).top(intValueOrNull(top)).bottom(intValueOrNull(bottom))
        .left(intValueOrNull(left)).width(intValueOrNull(width)).height(intValueOrNull(height));
  }

  @Override
  public String toString() {
    return String.format("{x=%.4f, y=%.4f, top=%.4f, right=%.4f, bottom=%.4f, left=%.4f, width=%.4f, height=%.4f}", x, y, top, right,
        bottom, left, width, height);
  }

  private static Integer intValueOrNull(final Double doubleValue) {
    return doubleValue == null ? null : Integer.valueOf(doubleValue.intValue());
  }

}
