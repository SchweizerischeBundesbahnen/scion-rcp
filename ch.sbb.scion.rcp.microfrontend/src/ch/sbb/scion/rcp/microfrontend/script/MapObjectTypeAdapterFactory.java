package ch.sbb.scion.rcp.microfrontend.script;

import java.io.IOException;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Custom GSON adapter to convert a Java {@link Map} to a JavaScript object
 * literal of the following form: `{__type: 'Map', __value: [...[key, value]]}`.
 * 
 * This custom conversion allows the {@link Map} to be restored in JavaScript
 * when unmarshalling JSON, which is necessary because JSON does not have a Map
 * representation.
 * 
 * GSON would convert a Java {@link Map} to an object literal, so there is no
 * way to determine in JavaScript whether it is a Map or an object literal.
 * 
 * This adapter is to be used together with {@link helpers.js#fromJson} and {@link helpers.js#toJson}
 * in JavaScript.
 * 
 * @see MapTypeAdapterFactory
 * @see helpers.js#fromJson
 * @see helpers.js#toJson
 */
public class MapObjectTypeAdapterFactory implements TypeAdapterFactory {

  @SuppressWarnings("unchecked")
  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    if (!Map.class.isAssignableFrom(type.getRawType())) {
      return null;
    }

    return (TypeAdapter<T>) new TypeAdapter<Map<?, ?>>() {

      @Override
      public Map<?, ?> read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
          reader.nextNull();
          return null;
        }

        Map<?, ?> map = null;

        reader.beginObject();
        while (reader.hasNext()) {
          var property = reader.nextName();
          switch (property) {
            case "__type": {
              var type = reader.nextString();
              if (!"Map".equals(type)) {
                throw new IllegalArgumentException("Expected field '__type' to be 'Map', but was '" + type + "'");
              }
              break;
            }
            case "__value": {
              // Let GSON unmarshall the JSON using {@link MapTypeAdapterFactory}.
              map = getDefaultAdapter().read(reader);
              break;
            }
            default:
              throw new IllegalArgumentException("Expected property to be '__type' or '__value', but was '" + property + "'");
          }
        }
        reader.endObject();

        return map;
      }

      @Override
      public void write(JsonWriter writer, Map<?, ?> map) throws IOException {
        if (map == null) {
          writer.nullValue();
          return;
        }

        writer.beginObject();

        writer.name("__type");
        writer.value("Map");

        writer.name("__value");
        writer.beginArray();
        for (var entry : map.entrySet()) {
          writer.beginArray();
          var keyClazz = (Class<Object>) entry.getKey().getClass();
          gson.getAdapter(keyClazz).write(writer, entry.getKey());

          var valueClazz = (Class<Object>) entry.getValue().getClass();
          gson.getAdapter(valueClazz).write(writer, entry.getValue());
          writer.endArray();
        }
        writer.endArray();
        writer.endObject();
      }

      private TypeAdapter<Map<?, ?>> getDefaultAdapter() {
        return gson.getDelegateAdapter(MapObjectTypeAdapterFactory.this, (TypeToken<Map<?, ?>>) type);
      }
    };
  }
}
