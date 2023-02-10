package ch.sbb.scion.rcp.workbench.popup;

public class PopupException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public PopupException(final Throwable throwable) {
    super(throwable);
  }

  public PopupException(final String message) {
    super(message);
  }

}
