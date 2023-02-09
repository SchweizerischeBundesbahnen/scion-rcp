package ch.sbb.scion.rcp.microfrontend.internal.gson;

import java.util.Map;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Custom GSON adapter to convert a Java `Map` to a JSON object literal and vice versa, which is necessary because JSON has no
 * representation for the Map data type. By default, GSON converts a Java `Map` to a JSON object literal, so there is no way to determine in
 * JavaScript whether it is a Map or a dictionary. Format of the JSON object literal: `{__type: 'Map', __value: [...[key, value]]}` This
 * adapter is to be used together with {@link "helpers.js#fromJson"} and {@link "helpers.js#toJson"} in JavaScript.
 *
 * @see "com.google.gson.internal.bind.MapTypeAdapterFactory"
 * @see "helpers.js#fromJson"
 * @see "helpers.js#toJson"
 */
public class MapObjectTypeAdapterFactory implements TypeAdapterFactory {

  private static final String CUSTOM_OBJECT_TYPE_FIELD = "__type";
  private static final String CUSTOM_OBJECT_VALUE_FIELD = "__value";
  private static final String CUSTOM_OBJECT_TYPE = "Map";

  @SuppressWarnings("unchecked")
  @Override
  public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
    if (!Map.class.isAssignableFrom(type.getRawType())) {
      return null;
    }

    var jsonElementAdapter = gson.getAdapter(JsonElement.class);
    var defaultMapAdapter = gson.getDelegateAdapter(this, (TypeToken<Map<?, ?>>) type);

    return (TypeAdapter<T>) new TypeAdapter<Map<?, ?>>() {

      @Override
      public Map<?, ?> read(final JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
          reader.nextNull();
          return null;
        }

        var jsonObject = jsonElementAdapter.read(reader).getAsJsonObject();
        var typeElement = jsonObject.get(CUSTOM_OBJECT_TYPE_FIELD);

        if (typeElement != null && typeElement.isJsonPrimitive() && CUSTOM_OBJECT_TYPE.equals(typeElement.getAsString())) {
          return defaultMapAdapter.fromJsonTree(jsonObject.get(CUSTOM_OBJECT_VALUE_FIELD));
        }
        else {
          return defaultMapAdapter.fromJsonTree(jsonObject);
        }
      }

      @Override
      public void write(final JsonWriter writer, final Map<?, ?> map) throws IOException {
        if (map == null) {
          writer.nullValue();
          return;
        }

        writer.beginObject();

        writer.name(CUSTOM_OBJECT_TYPE_FIELD);
        writer.value(CUSTOM_OBJECT_TYPE);

        writer.name(CUSTOM_OBJECT_VALUE_FIELD);
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
    };
  }
}
