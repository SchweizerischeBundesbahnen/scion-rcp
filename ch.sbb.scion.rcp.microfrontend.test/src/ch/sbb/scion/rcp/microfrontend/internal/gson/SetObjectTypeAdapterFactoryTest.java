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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

class SetObjectTypeAdapterFactoryTest {

  private static final Set<String> JAVA_SET = Set.of("J", "I", "H", "G", "F", "E", "D", "C", "B", "A");
  private static final String JSON_SET = "{\"__type\":\"Set\",\"__value\":[\"J\",\"I\",\"H\",\"G\",\"F\",\"E\",\"D\",\"C\",\"B\",\"A\"]}";
  private static final Pattern JSON_SET_PATTERN = Pattern.compile("^\\{\"__type\":\"Set\",\"__value\":\\[(?<value>.*)\\]\\}$");
  private static final String JSON_ARRAY = "[\"J\",\"I\",\"H\",\"G\",\"F\",\"E\",\"D\",\"C\",\"B\",\"A\"]";

  private SetObjectTypeAdapterFactory factory;

  private Gson gson;

  @BeforeEach
  void beforeEach() {
    factory = new SetObjectTypeAdapterFactory();
    gson = new GsonBuilder().registerTypeAdapterFactory(factory).create();
  }

  @Test
  void create_shouldReturnNull_whenTokenDoesNotRepresentSet() {
    TypeToken<Long> longToken = TypeToken.get(Long.class);
    assertNull(factory.create(gson, longToken));
  }

  @Test
  void create_shouldReturnTypeAdapter_whenTokenRepresentsSet() {
    var hashSetToken = TypeToken.getParameterized(HashSet.class, String.class);
    assertNotNull(factory.create(gson, hashSetToken));
  }

  @Test
  void typeAdapterRead_shouldReturnSet_whenIsJsonStringThatRepresentsSetObject() {
    // given
    var setOfStringType = TypeToken.getParameterized(Set.class, String.class).getType();

    // when
    Set<String> jsonSet = gson.fromJson(JSON_SET, setOfStringType);

    // then
    assertEquals(JAVA_SET, jsonSet);
  }

  @Test
  void typeAdapterRead_shouldReturnSet_whenIsJsonStringThatRepresentsSetAsArray() {
    // given
    var setOfStringType = TypeToken.getParameterized(Set.class, String.class).getType();

    // when
    // This will not use our type adapter but should properly delegate to the default Set type adapter provided by GSON:
    Set<String> jsonSet = gson.fromJson(JSON_ARRAY, setOfStringType);

    // then
    assertEquals(JAVA_SET, jsonSet);
  }

  @Test
  void typeAdapterRead_shouldReturnNull_whenJsonIsNull() {
    // given
    var setOfStringType = TypeToken.getParameterized(Set.class, String.class).getType();

    // when
    Set<String> jsonSet = gson.fromJson("null", setOfStringType);

    // then
    assertNull(jsonSet);
  }

  @Test
  void typeAdapterWrite_shouldReturnJsonRepresentingSetObject() {
    // given
    var setOfStringType = TypeToken.getParameterized(Set.class, String.class).getType();

    // when
    String json = gson.toJson(JAVA_SET, setOfStringType);

    // then
    var matcher = JSON_SET_PATTERN.matcher(json);
    assertTrue(matcher.matches());
    var value = matcher.group("value");
    var letters = value.split(",");
    assertEquals(10, letters.length);
    var actualSet = Stream.of(letters).map(l -> String.valueOf(l.charAt(1))).collect(Collectors.toSet());
    assertEquals(JAVA_SET, actualSet);
  }

  @Test
  void typeAdapterWrite_shouldWriteJsonNull_whenSetIsNull() {
    // given
    var setOfStringType = TypeToken.getParameterized(Set.class, String.class).getType();

    // when
    String json = gson.toJson(null, setOfStringType);

    // then
    assertEquals("null", json);
  }
}