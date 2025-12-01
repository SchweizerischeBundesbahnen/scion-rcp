package ch.sbb.scion.rcp.microfrontend.model;

/**
 * @see <a href="https://microfrontend-platform-api.scion.vercel.app/enums/MessageHeaders.html">MessageHeaders</a>
 */
public enum MessageHeaders {

  CLIENT_ID("ɵCLIENT_ID"),
  APP_SYMBOLIC_NAME("ɵAPP_SYMBOLIC_NAME"),
  MESSAGE_ID("ɵMESSAGE_ID"),
  REPLY_TO("ɵREPLY_TO"),
  STATUS("ɵSTATUS"),
  TIMESTAMP("ɵTIMESTAMP");

  public final String value;

  private MessageHeaders(final String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }
}
