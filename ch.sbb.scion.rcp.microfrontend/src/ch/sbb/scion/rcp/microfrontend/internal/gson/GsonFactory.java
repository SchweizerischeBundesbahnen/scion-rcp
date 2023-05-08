package ch.sbb.scion.rcp.microfrontend.internal.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Use to obtain a {@link Gson} instance configured with library-specific adapters for marshalling/unmarshalling JSON. This factory installs
 * the following adapters:
 * <ul>
 * <li>{@link MapObjectTypeAdapterFactory}</li>
 * <li>{@link SetObjectTypeAdapterFactory}</li>
 * <li>{@link QualifierTypeAdapterFactory}</li>
 * <li>{@link PropertiesTypeAdapterFactory}</li>
 * </ul>
 */
public interface GsonFactory {

  public static final MapObjectTypeAdapterFactory MAP_OBJECT_TYPE_ADAPTER_FACTORY = new MapObjectTypeAdapterFactory();
  public static final SetObjectTypeAdapterFactory SET_OBJECT_TYPE_ADAPTER_FACTORY = new SetObjectTypeAdapterFactory();
  public static final QualifierTypeAdapterFactory QUALIFIER_TYPE_ADAPTER_FACTORY = new QualifierTypeAdapterFactory();
  public static final PropertiesTypeAdapterFactory PROPERTIES_TYPE_ADAPTER_FACTORY = new PropertiesTypeAdapterFactory();

  /**
   * Use to create a {@link Gson} instance to be used in conjunction with <a href="file:../../../../../../../js/helper.js">helper.js</a> to-
   * and FromJson in Javascript.
   */
  public static Gson create() {
    return new GsonBuilder().registerTypeAdapterFactory(MAP_OBJECT_TYPE_ADAPTER_FACTORY)
        .registerTypeAdapterFactory(SET_OBJECT_TYPE_ADAPTER_FACTORY).registerTypeAdapterFactory(QUALIFIER_TYPE_ADAPTER_FACTORY)
        .registerTypeAdapterFactory(PROPERTIES_TYPE_ADAPTER_FACTORY).serializeNulls().create();
  }
}