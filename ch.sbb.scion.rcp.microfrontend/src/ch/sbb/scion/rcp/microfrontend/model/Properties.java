package ch.sbb.scion.rcp.microfrontend.model;

import java.util.HashMap;
import java.util.Map;

public class Properties {

  public Map<String, Object> entries;

  public Properties() {
    entries = new HashMap<>();
  }

  public Properties(Map<String, Object> entries) {
    this.entries = new HashMap<String, Object>(entries);
  }

  public Properties set(String key, Object value) {
    entries.put(key, value);
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String key) {
    return (T) entries.get(key);
  }

  public boolean has(String key) {
    return entries.containsKey(key);
  }
}
