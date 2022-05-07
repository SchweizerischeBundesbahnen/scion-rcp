package ch.sbb.scion.rcp.microfrontend.script;

import java.util.HashMap;
import java.util.Map;

import ch.sbb.scion.rcp.microfrontend.internal.GsonFactory;

public class Script {

  private String script;
  private Map<String, String> placeholders = new HashMap<>();

  public Script(String script) {
    this.script = script;
  }

  public Script replacePlaceholder(String name, Object value) {
    return replacePlaceholder(name, value, 0);
  }

  public Script replacePlaceholder(String name, Object value, int flags) {
    placeholders.put(name, toPlaceholderValue(value, flags));
    return this;
  }

  public String substitute() {
    return placeholders.entrySet().stream().reduce(script, (acc, placeholder) -> {
      return acc.replace("${" + placeholder.getKey() + "}", placeholder.getValue());
    }, String::concat);
  }

  private String toPlaceholderValue(Object value, int flags) {
    if (value == null) {
      return "null";
    }
    if ((flags & Flags.ToJson) > 0) {
      return GsonFactory.create().toJson(value);
    }
    return value.toString();
  }

  public static class Flags {
    public static final int ToJson = 1 << 2;
  }
}
