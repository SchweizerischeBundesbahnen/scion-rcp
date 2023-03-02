/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.microfrontend.internal.gson;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Set;

import java.lang.reflect.Type;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

class SetObjectTypeAdapterFactoryTestIT {

  private Gson gson;

  private final String jsonString = "{\"__type\":\"Set\",\"__value\":[\"J\",\"I\",\"H\",\"G\",\"F\",\"E\",\"D\",\"C\",\"B\",\"A\"]}";

  @BeforeEach
  void beforeEach() {
    gson = new GsonBuilder().registerTypeAdapterFactory(new SetObjectTypeAdapterFactory()).create();
  }

  @Test
  void typeAdapterRead_shouldReturnSet_whenTypeFieldIsSet() {
    // given
    Type setOfStringType = TypeToken.getParameterized(Set.class, String.class).getType();

    // when
    Set<String> jsonSet = gson.fromJson(jsonString, setOfStringType);

    // then
    assertThat(jsonSet, hasItems("A", "B", "C", "D", "E", "F", "G", "H", "I", "J"));
  }
}