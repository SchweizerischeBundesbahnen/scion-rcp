package ch.sbb.scion.rcp.microfrontend.script;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

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
      return ((flags & Flags.UndefinedIfNull) > 0) ? "undefined" : "null";
    }
    if ((flags & Flags.ToJson) > 0) {
      return new Gson().toJson(value);
    }
    return value.toString();
  }

  public static class Flags {
    public static final int UndefinedIfNull = 1 << 1;
    public static final int ToJson = 1 << 2;
  }
}
