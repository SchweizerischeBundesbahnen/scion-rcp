package ch.sbb.scion.rcp.microfrontend.internal.gson;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import ch.sbb.scion.rcp.microfrontend.model.Qualifier;

/**
 * Converts a {@link Qualifier} object to a JavaScript dictionary and vice versa.
 */
public class QualifierTypeAdapterFactory implements TypeAdapterFactory {

  @SuppressWarnings("unchecked")
  @Override
  public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
    if (!Qualifier.class.isAssignableFrom(type.getRawType())) {
      return null;
    }

    return (TypeAdapter<T>) new TypeAdapter<Qualifier>() {

      @Override
      public Qualifier read(final JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
          reader.nextNull();
          return null;
        }

        var qualifier = new Qualifier();
        reader.beginObject();
        while (reader.hasNext()) {
          var key = reader.nextName();

          var token = reader.peek();
          if (token.equals(JsonToken.STRING)) {
            qualifier.set(key, reader.nextString());
          }
          else if (token.equals(JsonToken.NUMBER)) {
            qualifier.set(key, reader.nextInt());
          }
          else if (token.equals(JsonToken.BOOLEAN)) {
            qualifier.set(key, reader.nextBoolean());
          }
        }
        reader.endObject();

        return qualifier;
      }

      @Override
      public void write(final JsonWriter writer, final Qualifier qualifier) throws IOException {
        if (qualifier == null) {
          writer.nullValue();
          return;
        }

        writer.beginObject();
        for (var entry : qualifier.entries().entrySet()) {
          var key = entry.getKey();
          var value = entry.getValue();
          var valueAdapter = gson.getAdapter((Class<Object>) value.getClass());

          writer.name(key);
          valueAdapter.write(writer, value);
        }
        writer.endObject();
      }
    };
  }
}
