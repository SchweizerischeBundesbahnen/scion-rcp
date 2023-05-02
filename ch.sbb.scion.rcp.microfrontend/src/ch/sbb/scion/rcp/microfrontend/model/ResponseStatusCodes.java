package ch.sbb.scion.rcp.microfrontend.model;

/**
 * @see <a href="https://scion-microfrontend-platform-api.vercel.app/enums/ResponseStatusCodes.html">ResponseStatusCodes</a>
 */
public enum ResponseStatusCodes {

  BAD_REQUEST(400),
  ERROR(500),
  NOT_FOUND(404),
  OK(200),
  TERMINAL(250);

  public final int value;

  private ResponseStatusCodes(final int value) {
    this.value = value;
  }

}
