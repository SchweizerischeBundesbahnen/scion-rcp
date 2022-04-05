package ch.sbb.scion.rcp.microfrontend.model;

/**
 * @see https://scion-microfrontend-platform-api.vercel.app/enums/MessageHeaders.html
 */
public enum MessageHeaders {
  ClientId("ɵCLIENT_ID"),
  AppSymbolicName("ɵAPP_SYMBOLIC_NAME"),
  MessageId("ɵMESSAGE_ID"),
  ReplyTo("ɵREPLY_TO"),
  Timestamp("ɵTIMESTAMP");

  public final String value;

  private MessageHeaders(String value) {
    this.value = value;
  }
  
  @Override
  public String toString() {
    return value;
  }
}
