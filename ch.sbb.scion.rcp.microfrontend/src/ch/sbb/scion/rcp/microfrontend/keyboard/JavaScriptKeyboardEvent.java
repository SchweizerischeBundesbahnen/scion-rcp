package ch.sbb.scion.rcp.microfrontend.keyboard;

public class JavaScriptKeyboardEvent {
  public String type;
  public String key;
  public boolean ctrlKey;
  public boolean shiftKey;
  public boolean altKey;
  public boolean metaKey;

  public JavaScriptKeyboardEvent(String type, String key, boolean ctrlKey, boolean shiftKey, boolean altKey, boolean metaKey) {
    this.type = type;
    this.key = key;
    this.ctrlKey = ctrlKey;
    this.shiftKey = shiftKey;
    this.altKey = altKey;
    this.metaKey = metaKey;
  }

  public String type() {
    return type;
  }

  public String key() {
    return key;
  }

  public boolean ctrlKey() {
    return ctrlKey;
  }

  public boolean shiftKey() {
    return shiftKey;
  }

  public boolean altKey() {
    return altKey;
  }

  public boolean metaKey() {
    return metaKey;
  }
}
