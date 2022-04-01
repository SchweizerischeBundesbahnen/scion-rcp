package ch.sbb.scion.rcp.microfrontend.keyboard;

public record JavaScriptKeyboardEvent (String type, String key, boolean ctrlKey, boolean shiftKey, boolean altKey, boolean metaKey) {
}
