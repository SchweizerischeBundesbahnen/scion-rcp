package ch.sbb.scion.rcp.microfrontend.script;

import java.io.IOException;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Custom GSON adapter to convert a Java {@link Set} to a JavaScript object
 * literal of the following form: `{__type: 'Set', __value: [...values]}`.
 * 
 * This custom conversion allows the {@link Set} to be restored in JavaScript
 * when unmarshalling JSON, which is necessary because JSON does not have a Set
 * representation.
 * 
 * GSON would convert a Java {@link Set} to an array, so there is no way to
 * determine in JavaScript whether it is an Array or Set.
 * 
 * Use in conjunction with {@link helpers.js#fromJson} and {@link helpers.js#toJson} in JavaScript.
 * 
 * @see CollectionTypeAdapterFactory
 * @see helpers.js#fromJson
 * @see helpers.js#toJson
 */
public class SetObjectTypeAdapterFactory implements TypeAdapterFactory {

  @SuppressWarnings("unchecked")
  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    if (!Set.class.isAssignableFrom(type.getRawType())) {
      return null;
    }

    return (TypeAdapter<T>) new TypeAdapter<Set<?>>() {

      @Override
      public Set<?> read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
          reader.nextNull();
          return null;
        }

        Set<?> set = null;

        reader.beginObject();
        while (reader.hasNext()) {
          var property = reader.nextName();
          switch (property) {
            case "__type": {
              var type = reader.nextString();
              if (!"Set".equals(type)) {
                throw new IllegalArgumentException("Expected field '__type' to be 'Set', but was '" + type + "'");
              }
              break;
            }
            case "__value": {
              // Let GSON unmarshall the JSON using {@link CollectionTypeAdapterFactory}.
              set = getDefaultAdapter().read(reader);
              break;
            }
            default:
              throw new IllegalArgumentException("Expected property to be '__type' or '__value', but was '" + property + "'");
          }
        }
        reader.endObject();

        return set;
      }

      @Override
      public void write(JsonWriter writer, Set<?> set) throws IOException {
        if (set == null) {
          writer.nullValue();
          return;
        }

        writer.beginObject();

        writer.name("__type");
        writer.value("Set");

        writer.name("__value");
        writer.beginArray();
        for (var element : set) {
          var valueClazz = (Class<Object>) element.getClass();
          gson.getAdapter(valueClazz).write(writer, element);
        }
        writer.endArray();
        writer.endObject();
      }

      private TypeAdapter<Set<?>> getDefaultAdapter() {
        return gson.getDelegateAdapter(SetObjectTypeAdapterFactory.this, (TypeToken<Set<?>>) type);
      }
    };
  }
}
