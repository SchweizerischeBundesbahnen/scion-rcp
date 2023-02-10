package ch.sbb.scion.rcp.workbench;

/**
 * A union of coordinates expressed with respect to the parent shell of the Eclipse dialog that contains the popup.
 */
public class SciWorkbenchPopupOrigin {

  public Integer x;

  public Integer y;

  public Integer top;

  public Integer right;

  public Integer bottom;

  public Integer left;

  public Integer width;

  public Integer height;

  public SciWorkbenchPopupOrigin x(final Integer x) {
    this.x = x;
    return this;
  }

  public SciWorkbenchPopupOrigin y(final Integer y) {
    this.y = y;
    return this;
  }

  public SciWorkbenchPopupOrigin top(final Integer top) {
    this.top = top;
    return this;
  }

  public SciWorkbenchPopupOrigin right(final Integer right) {
    this.right = right;
    return this;
  }

  public SciWorkbenchPopupOrigin bottom(final Integer bottom) {
    this.bottom = bottom;
    return this;
  }

  public SciWorkbenchPopupOrigin left(final Integer left) {
    this.left = left;
    return this;
  }

  public SciWorkbenchPopupOrigin width(final Integer width) {
    this.width = width;
    return this;
  }

  public SciWorkbenchPopupOrigin height(final Integer height) {
    this.height = height;
    return this;
  }

  @Override
  public String toString() {
    return String.format("{x=%d, y=%d, top=%d, right=%d, bottom=%d, left=%d, width=%d, height=%d}", x, y, top, right, bottom, left, width,
        height);
  }

}
