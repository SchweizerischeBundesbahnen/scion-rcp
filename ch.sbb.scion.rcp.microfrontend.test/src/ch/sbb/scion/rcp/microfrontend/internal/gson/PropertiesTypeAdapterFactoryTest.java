/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.microfrontend.internal.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import ch.sbb.scion.rcp.microfrontend.model.Properties;

class PropertiesTypeAdapterFactoryTest {

  private static final String JSON_PROPERTIES = "{\"entity\":\"product\",\"id\":42,\"active\":true}";

  private PropertiesTypeAdapterFactory factory;

  private Gson gson;

  @BeforeEach
  void beforeEach() {
    // PropertiesTypeAdapterFactory calls getDelegateAdapter(GsonFactory.MAP_OBJECT_TYPE_ADAPTER_FACTORY, ...) to skip past the
    // custom Map adapter and obtain Gson's built-in default Map adapter instead, so that exact singleton instance (not just any
    // MapObjectTypeAdapterFactory instance) must also be registered here for the skip-past lookup to succeed.
    factory = new PropertiesTypeAdapterFactory();
    gson = new GsonBuilder().registerTypeAdapterFactory(GsonFactory.MAP_OBJECT_TYPE_ADAPTER_FACTORY).registerTypeAdapterFactory(factory)
        .create();
  }

  @Test
  void create_shouldReturnNull_whenTokenDoesNotRepresentProperties() {
    var longToken = TypeToken.get(Long.class);
    assertNull(factory.create(gson, longToken));
  }

  @Test
  void create_shouldReturnTypeAdapter_whenTokenRepresentsProperties() {
    var propertiesToken = TypeToken.get(Properties.class);
    assertNotNull(factory.create(gson, propertiesToken));
  }

  @Test
  void typeAdapterRead_shouldReturnProperties_whenIsJsonStringThatRepresentsDictionary() {
    // when
    Properties properties = gson.fromJson(JSON_PROPERTIES, Properties.class);

    // then
    // Numeric values are deserialized as Double, since the delegate Map adapter has no target value type information:
    assertEquals(Map.of("entity", "product", "id", Double.valueOf(42), "active", Boolean.TRUE), properties.entries());
  }

  @Test
  void typeAdapterRead_shouldReturnEmptyProperties_whenJsonIsNull() {
    // when
    Properties properties = gson.fromJson("null", Properties.class);

    // then
    assertNotNull(properties);
    assertTrue(properties.entries().isEmpty());
  }

  @Test
  void typeAdapterWrite_shouldReturnJsonRepresentingDictionary() {
    // given
    var properties = new Properties().set("entity", "product").set("id", 42).set("active", true);

    // when
    var json = gson.toJson(properties, Properties.class);

    // then
    var jsonObject = gson.fromJson(json, JsonObject.class);
    assertEquals("product", jsonObject.get("entity").getAsString());
    assertEquals(42, jsonObject.get("id").getAsInt());
    assertTrue(jsonObject.get("active").getAsBoolean());
    assertEquals(3, jsonObject.size());
  }

  @Test
  void typeAdapterWrite_shouldWriteJsonNull_whenPropertiesIsNull() {
    // when
    var json = gson.toJson(null, Properties.class);

    // then
    assertEquals("null", json);
  }
}
