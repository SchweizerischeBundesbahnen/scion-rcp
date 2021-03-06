package ch.sbb.scion.rcp.microfrontend.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.sbb.scion.rcp.microfrontend.script.MapObjectTypeAdapterFactory;
import ch.sbb.scion.rcp.microfrontend.script.QualifierTypeAdapterFactory;
import ch.sbb.scion.rcp.microfrontend.script.SetObjectTypeAdapterFactory;

/**
 * Use to obtain a {@link Gson} instance configured with library-specific
 * adapters for marshalling/unmarshalling JSON.
 * 
 * This factory installs the following adapters:
 * <ul>
 * <li>{@link MapObjectTypeAdapterFactory}</li>
 * <li>{@link SetObjectTypeAdapterFactory}</li>
 * <li>{@link QualifierTypeAdapterFactory}</li>
 * </ul>
 */
public interface GsonFactory {

  /**
   * Use to create a {@link Gson} instance to be used in conjunction with
   * {@link helpers.js#fromJson} and {@link helpers.js#toJson} in JavaScript.
   */
  public static Gson create() {
    return new GsonBuilder()
        .registerTypeAdapterFactory(new MapObjectTypeAdapterFactory())
        .registerTypeAdapterFactory(new SetObjectTypeAdapterFactory())
        .registerTypeAdapterFactory(new QualifierTypeAdapterFactory())
        .serializeNulls()
        .create();
  }
}