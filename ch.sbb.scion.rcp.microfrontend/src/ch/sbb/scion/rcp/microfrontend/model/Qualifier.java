package ch.sbb.scion.rcp.microfrontend.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @see https://scion-microfrontend-platform-api.vercel.app/interfaces/Qualifier.html
 */
public class Qualifier {

  public Map<String, Object> entries = new HashMap<>();

  public Qualifier add(String key, String value) {
    entries.put(key, value);
    return this;
  }

  public Qualifier add(String key, int value) {
    entries.put(key, value);
    return this;
  }

  public Qualifier add(String key, boolean value) {
    entries.put(key, value);
    return this;
  }
}
