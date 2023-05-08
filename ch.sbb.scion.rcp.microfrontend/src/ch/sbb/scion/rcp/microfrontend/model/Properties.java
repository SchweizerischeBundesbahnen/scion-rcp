package ch.sbb.scion.rcp.microfrontend.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.ToString;

/**
 * Represents an arbitrary collection of properties. <br>
 * <br>
 * Use this class if the corresponding JavaScript object is defined as a TypeScript string index signature; i.e.,
 *
 * <pre>
 * [key: string]: any
 * </pre>
 *
 * Do not use Map&lt;String, ?&gt; in these cases as a Java Map will be deserialized to a JavaScript Map. The Typescript string index
 * signature represents an ordinary JavaScript object, however.
 */
@ToString
public class Properties {

  private final Map<String, Object> entries;

  public Properties() {
    this.entries = new HashMap<>();
  }

  public Properties(final Map<String, Object> entries) {
    this.entries = new HashMap<>(entries);
  }

  /**
   * @return an unmodifiable view of the inserted entries
   */
  public Map<String, Object> entries() {
    return Collections.unmodifiableMap(entries);
  }

  public Properties set(final String key, final Object value) {
    entries.put(key, value);
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(final String key) {
    return (T) entries.get(key);
  }

  public boolean has(final String key) {
    return entries.containsKey(key);
  }
}
