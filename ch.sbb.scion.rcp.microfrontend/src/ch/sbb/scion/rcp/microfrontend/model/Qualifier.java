package ch.sbb.scion.rcp.microfrontend.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @see "https://scion-microfrontend-platform-api.vercel.app/interfaces/Qualifier.html"
 */
public class Qualifier {

  public Map<String, Object> entries = new HashMap<>();

  public Qualifier set(final String key, final String value) {
    entries.put(key, value);
    return this;
  }

  public Qualifier set(final String key, final int value) {
    entries.put(key, Integer.valueOf(value));
    return this;
  }

  public Qualifier set(final String key, final boolean value) {
    entries.put(key, Boolean.valueOf(value));
    return this;
  }
}
