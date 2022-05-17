package ch.sbb.scion.rcp.microfrontend.model;

/**
 * @see https://scion-microfrontend-platform-api.vercel.app/enums/ResponseStatusCodes.html
 */
public enum ResponseStatusCodes {
  BAD_REQUEST(400),
  ERROR (500),
  NOT_FOUND(404),
  OK(200),
  TERMINAL(250);

  public final int value;

  private ResponseStatusCodes(int value) {
    this.value = value;
  }
  
}
