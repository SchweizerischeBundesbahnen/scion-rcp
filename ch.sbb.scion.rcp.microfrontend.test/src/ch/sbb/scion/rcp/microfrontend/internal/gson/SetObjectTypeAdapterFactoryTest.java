/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.microfrontend.internal.gson;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

@ExtendWith(MockitoExtension.class)
class SetObjectTypeAdapterFactoryTest {

  private SetObjectTypeAdapterFactory factory;

  @Mock
  private Gson gson;

  @BeforeEach
  void beforeEach() {
    MockitoAnnotations.openMocks(this);
    factory = new SetObjectTypeAdapterFactory();
  }

  @Test
  void create_shouldReturnNull_forTypeTokenNotSet() {
    TypeToken<Long> longToken = TypeToken.get(Long.class);
    TypeAdapter<Long> typeAdapter = factory.create(gson, longToken);

    assertNull(typeAdapter);
  }

  @Test
  void create_shouldReturnTypeAdapter_forTypeTokenAsHashSet() {
    var hashSetToken = TypeToken.getParameterized(HashSet.class, String.class);
    var typeAdapter = factory.create(gson, hashSetToken);

    assertNotNull(typeAdapter);
  }
}