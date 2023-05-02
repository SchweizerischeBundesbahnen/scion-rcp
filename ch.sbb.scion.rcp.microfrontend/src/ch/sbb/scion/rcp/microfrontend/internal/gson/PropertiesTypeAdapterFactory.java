package ch.sbb.scion.rcp.microfrontend.internal.gson;

import java.util.Map;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import ch.sbb.scion.rcp.microfrontend.model.Properties;

/**
 * Converts a {@link Properties} object to a JavaScript dictionary and vice versa.
 */
public class PropertiesTypeAdapterFactory implements TypeAdapterFactory {

  @SuppressWarnings("unchecked")
  @Override
  public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
    if (!Properties.class.isAssignableFrom(type.getRawType())) {
      return null;
    }

    var defaultMapAdapter = gson.getDelegateAdapter(GsonFactory.MAP_OBJECT_TYPE_ADAPTER_FACTORY, TypeToken.get(Map.class));

    return (TypeAdapter<T>) new TypeAdapter<Properties>() {

      @Override
      public Properties read(final JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
          reader.nextNull();
          return new Properties();
        }

        return new Properties(defaultMapAdapter.read(reader));
      }

      @Override
      public void write(final JsonWriter writer, final Properties properties) throws IOException {
        if (properties == null) {
          writer.nullValue();
          return;
        }

        defaultMapAdapter.write(writer, properties.entries());
      }
    };
  }
}
