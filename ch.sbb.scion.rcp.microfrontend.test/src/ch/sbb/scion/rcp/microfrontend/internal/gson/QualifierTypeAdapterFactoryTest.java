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

import ch.sbb.scion.rcp.microfrontend.model.Qualifier;

class QualifierTypeAdapterFactoryTest {

  private static final String JSON_QUALIFIER = "{\"entity\":\"product\",\"id\":42,\"active\":true}";

  private QualifierTypeAdapterFactory factory;

  private Gson gson;

  @BeforeEach
  void beforeEach() {
    factory = new QualifierTypeAdapterFactory();
    gson = new GsonBuilder().registerTypeAdapterFactory(factory).create();
  }

  @Test
  void create_shouldReturnNull_whenTokenDoesNotRepresentQualifier() {
    var longToken = TypeToken.get(Long.class);
    assertNull(factory.create(gson, longToken));
  }

  @Test
  void create_shouldReturnTypeAdapter_whenTokenRepresentsQualifier() {
    var qualifierToken = TypeToken.get(Qualifier.class);
    assertNotNull(factory.create(gson, qualifierToken));
  }

  @Test
  void typeAdapterRead_shouldReturnQualifier_whenIsJsonStringThatRepresentsDictionary() {
    // when
    Qualifier qualifier = gson.fromJson(JSON_QUALIFIER, Qualifier.class);

    // then
    assertEquals(Map.of("entity", "product", "id", Integer.valueOf(42), "active", Boolean.TRUE), qualifier.entries());
  }

  @Test
  void typeAdapterRead_shouldReturnNull_whenJsonIsNull() {
    Qualifier qualifier = gson.fromJson("null", Qualifier.class);
    assertNull(qualifier);
  }

  @Test
  void typeAdapterWrite_shouldReturnJsonRepresentingDictionary() {
    // given
    var qualifier = new Qualifier().set("entity", "product").set("id", 42).set("active", true);

    // when
    var json = gson.toJson(qualifier, Qualifier.class);

    // then
    var jsonObject = gson.fromJson(json, JsonObject.class);
    assertEquals("product", jsonObject.get("entity").getAsString());
    assertEquals(42, jsonObject.get("id").getAsInt());
    assertTrue(jsonObject.get("active").getAsBoolean());
    assertEquals(3, jsonObject.size());
  }

  @Test
  void typeAdapterWrite_shouldWriteJsonNull_whenQualifierIsNull() {
    var json = gson.toJson(null, Qualifier.class);
    assertEquals("null", json);
  }
}
