package ch.sbb.scion.rcp.microfrontend.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.sbb.scion.rcp.microfrontend.script.QualifierTypeAdapterFactory;

public interface GsonFactory {

  /**
   * Use to create a {@link Gson} instance to be used in conjunction with
   * {@link helpers.js#fromJson} and {@link helpers.js#toJson} in JavaScript.
   */
  public static Gson create() {
    return new GsonBuilder()
        .registerTypeAdapterFactory(new QualifierTypeAdapterFactory())
        .serializeNulls()
        .create();
  }
}