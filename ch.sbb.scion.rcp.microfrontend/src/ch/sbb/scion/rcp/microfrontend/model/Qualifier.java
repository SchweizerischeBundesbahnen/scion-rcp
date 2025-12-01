package ch.sbb.scion.rcp.microfrontend.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.ToString;

/**
 * @see <a href="https://microfrontend-platform-api.scion.vercel.app/interfaces/Qualifier.html">Qualifier</a>
 */
@ToString
public class Qualifier {

  private final Map<String, Object> entries = new HashMap<>();

  /**
   * @return an unmodifiable view of the inserted entries
   */
  public Map<String, Object> entries() {
    return Collections.unmodifiableMap(entries);
  }

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
