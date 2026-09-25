/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.microfrontend.internal.gson;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import ch.sbb.scion.rcp.microfrontend.model.Capability.DeprecationInfo;
import ch.sbb.scion.rcp.microfrontend.model.Capability.ParamDefinition;
import ch.sbb.scion.rcp.microfrontend.model.Properties;

class CapabilityParamDefinitionTypeAdapterFactoryTest {

  private CapabilityParamDefinitionTypeAdapterFactory factory;

  private Gson gson;

  @BeforeEach
  void beforeEach() {
    factory = new CapabilityParamDefinitionTypeAdapterFactory();
    gson = new GsonBuilder().registerTypeAdapterFactory(factory).create();
  }

  @Test
  void create_shouldReturnNull_whenTokenDoesNotRepresentParamDefinition() {
    var longToken = TypeToken.get(Long.class);
    assertNull(factory.create(gson, longToken));
  }

  @Test
  void create_shouldReturnTypeAdapter_whenTokenRepresentsParamDefinition() {
    var paramDefinitionToken = TypeToken.get(ParamDefinition.class);
    assertNotNull(factory.create(gson, paramDefinitionToken));
  }

  @Test
  void typeAdapterRead_shouldReturnParamDefinition_whenJsonHasOnlyRequiredFields() {
    // when
    ParamDefinition paramDefinition = gson.fromJson("{\"name\":\"id\",\"required\":true}", ParamDefinition.class);

    // then
    assertEquals("id", paramDefinition.name());
    assertTrue(paramDefinition.isRequired());
    assertNull(paramDefinition.description());
    assertNull(paramDefinition.deprecated());
    assertNull(paramDefinition.defaultValue());
    // Properties are always set, even if empty:
    assertNotNull(paramDefinition.properties());
    assertTrue(paramDefinition.properties().entries().isEmpty());
  }

  @Test
  void typeAdapterRead_shouldSetDescription_whenJsonHasDescription() {
    // given
    var json = minimalRequiredWith("description", "\"Entity id\"");

    // when
    ParamDefinition paramDefinition = gson.fromJson(json, ParamDefinition.class);

    // then
    assertFalse(paramDefinition.isRequired());
    assertEquals("Entity id", paramDefinition.description());
  }

  @Test
  void typeAdapterRead_shouldSetDeprecatedTrue_whenJsonHasDeprecatedAsBoolean() {
    // given
    var json = minimalRequiredWith("deprecated", "true");

    // when
    ParamDefinition paramDefinition = gson.fromJson(json, ParamDefinition.class);

    // then
    assertEquals(Boolean.TRUE, paramDefinition.deprecated());
  }

  @ParameterizedTest
  @MethodSource("readDefaultValueCases")
  void typeAdapterRead_shouldSetDefaultValue_whenJsonHasDefault(final String json, final Object expectedDefaultValue) {
    // when
    ParamDefinition paramDefinition = gson.fromJson(json, ParamDefinition.class);

    // then
    assertEquals(expectedDefaultValue, paramDefinition.defaultValue());
  }

  private static Stream<Arguments> readDefaultValueCases() {
    return Stream.of(
        // 'default' is an arbitrary, raw JSON value, not a JSON-encoded string.
        // A JSON object/array has no embedded type information, so it is read back as a Map/List, not as a custom type:
        Arguments.of(minimalRequiredWith("default", "\"foo\""), "foo"),
        Arguments.of(minimalRequiredWith("default", "true"), Boolean.TRUE),
        Arguments.of(minimalRequiredWith("default", "42"), Double.valueOf(42)),
        Arguments.of(minimalRequiredWith("default", "[\"a\",\"b\"]"), List.of("a", "b")),
        Arguments.of(minimalRequiredWith("default", "{\"name\":\"bar\",\"count\":7}"),
            Map.of("name", "bar", "count", Double.valueOf(7))));
  }

  /**
   * Builds a minimal ParamDefinition JSON literal ({@code name}/{@code required}) plus one additional property.
   */
  private static String minimalRequiredWith(final String key, final String rawValueJson) {
    return "{\"name\":\"id\",\"required\":false,\"" + key + "\":" + rawValueJson + "}";
  }

  @Test
  void typeAdapterRead_shouldSetAdditionalProperties_whenJsonHasUnknownKeys() {
    // given
    var json = "{\"name\":\"id\",\"required\":true,\"label\":\"Identifier\",\"min\":1}";

    // when
    ParamDefinition paramDefinition = gson.fromJson(json, ParamDefinition.class);

    // then
    assertEquals("Identifier", paramDefinition.properties().get("label"));
    assertEquals(Double.valueOf(1), paramDefinition.properties().get("min"));
  }

  @Test
  void typeAdapterRead_shouldReturnNull_whenJsonIsNull() {
    ParamDefinition paramDefinition = gson.fromJson("null", ParamDefinition.class);
    assertNull(paramDefinition);
  }

  @Test
  void typeAdapterWrite_shouldReturnJsonRepresentingMinimalParamDefinition() {
    // given
    var paramDefinition = ParamDefinition.builder().name("id").isRequired(true).build();

    // when
    var json = gson.toJson(paramDefinition, ParamDefinition.class);

    // then
    var jsonObject = gson.fromJson(json, JsonObject.class);
    assertEquals("id", jsonObject.get("name").getAsString());
    assertTrue(jsonObject.get("required").getAsBoolean());
    // Optional, unset fields must be omitted rather than written as null:
    assertEquals(2, jsonObject.size());
  }

  @Test
  void typeAdapterWrite_shouldIncludeDescription_whenSet() {
    // given
    var paramDefinition = ParamDefinition.builder().name("id").isRequired(true).description("Entity id").build();

    // when
    var json = gson.toJson(paramDefinition, ParamDefinition.class);

    // then
    var jsonObject = gson.fromJson(json, JsonObject.class);
    assertEquals("Entity id", jsonObject.get("description").getAsString());
  }

  @ParameterizedTest
  @MethodSource("writeDefaultValueCases")
  void typeAdapterWrite_shouldIncludeDefaultValue_whenSet(final Object defaultValue, final String expectedDefaultJson) {
    // given
    var paramDefinition = ParamDefinition.builder().name("id").isRequired(true).defaultValue(defaultValue).build();

    // when
    var json = gson.toJson(paramDefinition, ParamDefinition.class);

    // then
    var jsonObject = gson.fromJson(json, JsonObject.class);
    assertEquals(JsonParser.parseString(expectedDefaultJson), jsonObject.get("default"));
  }

  private static Stream<Arguments> writeDefaultValueCases() {
    return Stream.of(Arguments.of("foo", "\"foo\""), Arguments.of(Boolean.TRUE, "true"), Arguments.of(Integer.valueOf(42), "42"),
        Arguments.of(List.of("a", "b"), "[\"a\",\"b\"]"), Arguments.of(new SampleValue("bar", 7), "{\"name\":\"bar\",\"count\":7}"));
  }

  @Test
  void typeAdapterWrite_shouldIncludeAdditionalProperties_whenSet() {
    // given
    var properties = new Properties().set("label", "Identifier");
    var paramDefinition = ParamDefinition.builder().name("id").isRequired(true).properties(properties).build();

    // when
    var json = gson.toJson(paramDefinition, ParamDefinition.class);

    // then
    var jsonObject = gson.fromJson(json, JsonObject.class);
    assertEquals("Identifier", jsonObject.get("label").getAsString());
  }

  @Test
  void typeAdapterWrite_shouldWriteJsonNull_whenParamDefinitionIsNull() {
    var json = gson.toJson(null, ParamDefinition.class);
    assertEquals("null", json);
  }

  @Test
  void typeAdapterReadWrite_shouldRoundTripDeprecatedTrue() {
    // given
    var paramDefinition = ParamDefinition.builder().name("id").isRequired(false).deprecated(Boolean.TRUE).build();

    // when
    var json = gson.toJson(paramDefinition, ParamDefinition.class);
    var roundTripped = gson.fromJson(json, ParamDefinition.class);

    // then
    assertEquals(Boolean.TRUE, roundTripped.deprecated());
  }

  @Test
  void typeAdapterReadWrite_shouldRoundTripDeprecatedAsDeprecationInfo() {
    // given
    var deprecationInfo = DeprecationInfo.builder().message("Use 'newId' instead.").useInstead("newId").build();
    var paramDefinition = ParamDefinition.builder().name("id").isRequired(false).deprecated(deprecationInfo).build();

    // when
    var json = gson.toJson(paramDefinition, ParamDefinition.class);
    var roundTripped = gson.fromJson(json, ParamDefinition.class);

    // then
    assertEquals(deprecationInfo.message(), ((DeprecationInfo) roundTripped.deprecated()).message());
    assertEquals(deprecationInfo.useInstead(), ((DeprecationInfo) roundTripped.deprecated()).useInstead());
  }

  /**
   * Stand-in for an arbitrary, JavaScript-provided default value that is not one of the well-known scalar/collection types.
   */
  private static final class SampleValue {

    private final String name;
    private final int count;

    SampleValue(final String name, final int count) {
      this.name = name;
      this.count = count;
    }
  }
}
