/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.microfrontend.internal.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

class MapObjectTypeAdapterFactoryTest {

  private static final Map<String, String> JAVA_MAP = Map.of("A", "1", "B", "2", "C", "3");
  private static final String JSON_MAP = "{\"__type\":\"Map\",\"__value\":[[\"A\",\"1\"],[\"B\",\"2\"],[\"C\",\"3\"]]}";
  private static final String JSON_OBJECT = "{\"A\":\"1\",\"B\":\"2\",\"C\":\"3\"}";

  private MapObjectTypeAdapterFactory factory;

  private Gson gson;

  @BeforeEach
  void beforeEach() {
    factory = new MapObjectTypeAdapterFactory();
    gson = new GsonBuilder().registerTypeAdapterFactory(factory).create();
  }

  @Test
  void create_shouldReturnNull_whenTokenDoesNotRepresentMap() {
    TypeToken<Long> longToken = TypeToken.get(Long.class);
    assertNull(factory.create(gson, longToken));
  }

  @Test
  void create_shouldReturnTypeAdapter_whenTokenRepresentsMap() {
    var hashMapToken = TypeToken.getParameterized(HashMap.class, String.class, String.class);
    assertNotNull(factory.create(gson, hashMapToken));
  }

  @Test
  void typeAdapterRead_shouldReturnMap_whenIsJsonStringThatRepresentsMapObject() {
    // given
    var mapOfStringType = TypeToken.getParameterized(Map.class, String.class, String.class).getType();

    // when
    Map<String, String> jsonMap = gson.fromJson(JSON_MAP, mapOfStringType);

    // then
    assertEquals(JAVA_MAP, jsonMap);
  }

  @Test
  void typeAdapterRead_shouldReturnMap_whenIsJsonStringThatRepresentsPlainObject() {
    // given
    var mapOfStringType = TypeToken.getParameterized(Map.class, String.class, String.class).getType();

    // when
    // This will not use our type adapter but should properly delegate to the default Map type adapter provided by GSON:
    Map<String, String> jsonMap = gson.fromJson(JSON_OBJECT, mapOfStringType);

    // then
    assertEquals(JAVA_MAP, jsonMap);
  }

  @Test
  void typeAdapterRead_shouldReturnNull_whenJsonIsNull() {
    // given
    var mapOfStringType = TypeToken.getParameterized(Map.class, String.class, String.class).getType();

    // when
    Map<String, String> jsonMap = gson.fromJson("null", mapOfStringType);

    // then
    assertNull(jsonMap);
  }

  @Test
  void typeAdapterWrite_shouldReturnJsonRepresentingMapObject() {
    // given
    var mapOfStringType = TypeToken.getParameterized(Map.class, String.class, String.class).getType();

    // when
    String json = gson.toJson(JAVA_MAP, mapOfStringType);

    // then
    var jsonObject = gson.fromJson(json, JsonObject.class);
    assertEquals("Map", jsonObject.get("__type").getAsString());
    var actualMap = new HashMap<String, String>();
    jsonObject.getAsJsonArray("__value").forEach(entry -> {
      var pair = entry.getAsJsonArray();
      actualMap.put(pair.get(0).getAsString(), pair.get(1).getAsString());
    });
    assertEquals(JAVA_MAP, actualMap);
  }

  @Test
  void typeAdapterWrite_shouldWriteJsonNull_whenMapIsNull() {
    // given
    var mapOfStringType = TypeToken.getParameterized(Map.class, String.class, String.class).getType();

    // when
    String json = gson.toJson(null, mapOfStringType);

    // then
    assertEquals("null", json);
  }
}
