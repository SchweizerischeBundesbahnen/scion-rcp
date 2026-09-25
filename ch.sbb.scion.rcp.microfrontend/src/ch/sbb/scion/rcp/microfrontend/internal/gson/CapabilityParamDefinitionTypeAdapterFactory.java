/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.microfrontend.internal.gson;

import java.util.Set;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import ch.sbb.scion.rcp.microfrontend.model.Capability.DeprecationInfo;
import ch.sbb.scion.rcp.microfrontend.model.Capability.ParamDefinition;
import ch.sbb.scion.rcp.microfrontend.model.Properties;

/**
 * Provides a custom {@link TypeAdapter} for the {@link ParamDefinition} class.
 */
public class CapabilityParamDefinitionTypeAdapterFactory implements TypeAdapterFactory {

  private static final String DEFAULT_KEY = "default";
  private static final String DEPRECATED_KEY = "deprecated";
  private static final String DESCRIPTION_KEY = "description";
  private static final String NAME_KEY = "name";
  private static final String REQUIRED_KEY = "required";
  private static final Set<String> KEYS = Set.of(DEFAULT_KEY, DEPRECATED_KEY, DESCRIPTION_KEY, NAME_KEY, REQUIRED_KEY);

  @Override
  public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
    if (!ParamDefinition.class.isAssignableFrom(type.getRawType())) {
      return null;
    }

    @SuppressWarnings("unchecked")
    var typeAdapter = (TypeAdapter<T>) new TypeAdapter<ParamDefinition>() {

      @Override
      public ParamDefinition read(final JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
          reader.nextNull();
          return null;
        }

        var builder = ParamDefinition.builder();
        var properties = new Properties();
        reader.beginObject();
        while (reader.hasNext()) {
          var key = reader.nextName();
          if (DEFAULT_KEY.equals(key) && reader.peek() != JsonToken.NULL) {
            builder.defaultValue(gson.fromJson(reader, Object.class));
          }
          else if (DEPRECATED_KEY.equals(key) && reader.peek() != JsonToken.NULL) {
            builder.deprecated(readDeprecated(reader));
          }
          else if (DESCRIPTION_KEY.equals(key) && reader.peek() != JsonToken.NULL) {
            builder.description(reader.nextString());
          }
          else if (NAME_KEY.equals(key)) {
            builder.name(reader.nextString());
          }
          else if (REQUIRED_KEY.equals(key)) {
            builder.isRequired(reader.nextBoolean());
          }
          else if (!KEYS.contains(key)) {
            // Read additional metadata, the JavaScript ParamDefinition is indexable:
            properties.set(key, readPropertyNullable(reader));
          }
          else {
            // Skip optional, defined properties that are null:
            reader.skipValue();
          }
        }
        reader.endObject();

        // Always add properties even if they are empty:
        return builder.properties(properties).build();
      }

      private Object readDeprecated(final JsonReader reader) throws IOException {
        var token = reader.peek();
        // 'deprecated' can either be null, 'true', or '{ message?: string, useInstead?: string }'; null is already handled:
        if (token == JsonToken.BOOLEAN) {
          // No deprecation info, just deprecated:
          reader.nextBoolean(); // still, we need to consume the 'true' value
          return Boolean.TRUE;
        }
        // It's a JSON object, not a string, so read it directly from the stream rather than via fromJson(String):
        return gson.getAdapter(DeprecationInfo.class).read(reader);
      }

      private Object readPropertyNullable(final JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
          reader.nextNull();
          return null;
        }
        return gson.fromJson(reader, Object.class);
      }

      @Override
      public void write(final JsonWriter writer, final ParamDefinition paramDefinition) throws IOException {
        if (paramDefinition == null) {
          writer.nullValue();
          return;
        }

        writer.beginObject();
        if (paramDefinition.defaultValue() != null) {
          writer.name(DEFAULT_KEY);
          writeValue(writer, paramDefinition.defaultValue());
        }
        if (paramDefinition.deprecated() != null) {
          writer.name(DEPRECATED_KEY);
          writeDeprecated(writer, paramDefinition.deprecated());
        }
        if (paramDefinition.description() != null) {
          writer.name(DESCRIPTION_KEY).value(paramDefinition.description());
        }
        if (paramDefinition.properties() != null && !paramDefinition.properties().entries().isEmpty()) {
          writeProperties(writer, paramDefinition.properties());
        }
        // Non-optional fields:
        writer.name(NAME_KEY).value(paramDefinition.name());
        writer.name(REQUIRED_KEY).value(paramDefinition.isRequired());

        writer.endObject();
      }

      private void writeDeprecated(final JsonWriter writer, final Object deprecated) throws IOException {
        if (deprecated instanceof Boolean b && Boolean.TRUE.equals(b)) {
          // Only 'true' is allowed by type constraint:
          writer.value(true);
        }
        else if (deprecated instanceof DeprecationInfo di) {
          gson.getAdapter(DeprecationInfo.class).write(writer, di);
        }
        else {
          // Unknown value type:
          writer.nullValue();
        }
      }

      private void writeProperties(final JsonWriter writer, final Properties properties) throws IOException {
        for (var entry : properties.entries().entrySet()) {
          var key = entry.getKey();
          var value = entry.getValue();

          writer.name(key);
          if (value == null) {
            writer.nullValue();
            continue;
          }
          writeValue(writer, value);
        }
      }

      private void writeValue(final JsonWriter writer, final Object value) throws IOException {
        var valueAdapter = gson.getAdapter((Class<Object>) value.getClass());
        valueAdapter.write(writer, value);
      }
    };
    return typeAdapter;
  }

}
