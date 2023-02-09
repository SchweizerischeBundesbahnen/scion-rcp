package ch.sbb.scion.rcp.microfrontend.internal.gson;

import java.util.Set;

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
 * Custom GSON adapter to convert a Java `Set` to a JSON object literal and vice versa, which is necessary because JSON has no
 * representation for the Set data type. By default, GSON converts a Java `Set` to a JSON array, so there is no way to determine in
 * JavaScript whether it is an array or a Set. Format of the JSON object literal: `{__type: 'Set', __value: [...values]}`
 *
 * @see "com.google.gson.internal.bind.CollectionTypeAdapterFactory"
 * @see "helpers.js#fromJson"
 * @see "helpers.js#toJson"
 */

public class SetObjectTypeAdapterFactory implements TypeAdapterFactory {

  private static final String CUSTOM_OBJECT_TYPE_FIELD = "__type";
  private static final String CUSTOM_OBJECT_VALUE_FIELD = "__value";
  private static final String CUSTOM_OBJECT_TYPE = "Set";

  @SuppressWarnings("unchecked")
  @Override
  public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
    if (!Set.class.isAssignableFrom(type.getRawType())) {
      return null;
    }

    var jsonElementAdapter = gson.getAdapter(JsonElement.class);
    var defaultSetAdapter = gson.getDelegateAdapter(this, (TypeToken<Set<?>>) type);

    return (TypeAdapter<T>) new TypeAdapter<Set<?>>() {

      @Override
      public Set<?> read(final JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
          reader.nextNull();
          return null;
        }

        var jsonObject = jsonElementAdapter.read(reader).getAsJsonObject();
        var typeElement = jsonObject.get(CUSTOM_OBJECT_TYPE_FIELD);

        if (typeElement != null && typeElement.isJsonPrimitive() && CUSTOM_OBJECT_TYPE.equals(typeElement.getAsString())) {
          return defaultSetAdapter.fromJsonTree(jsonObject.get(CUSTOM_OBJECT_VALUE_FIELD));
        }
        else {
          return defaultSetAdapter.fromJsonTree(jsonObject);
        }
      }

      @Override
      public void write(final JsonWriter writer, final Set<?> set) throws IOException {
        if (set == null) {
          writer.nullValue();
          return;
        }

        writer.beginObject();

        writer.name(CUSTOM_OBJECT_TYPE_FIELD);
        writer.value(CUSTOM_OBJECT_TYPE);

        writer.name(CUSTOM_OBJECT_VALUE_FIELD);
        writer.beginArray();
        for (var element : set) {
          var valueClazz = (Class<Object>) element.getClass();
          gson.getAdapter(valueClazz).write(writer, element);
        }
        writer.endArray();
        writer.endObject();
      }
    };
  }
}
