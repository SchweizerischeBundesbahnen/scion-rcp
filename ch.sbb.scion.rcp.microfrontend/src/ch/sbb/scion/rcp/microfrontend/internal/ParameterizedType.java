package ch.sbb.scion.rcp.microfrontend.internal;

import java.lang.reflect.Type;

public class ParameterizedType implements java.lang.reflect.ParameterizedType {

  private Type rawType;
  private Type[] actualTypeArguments;

  public ParameterizedType(Type rawType, Type actualTypeArgument) {
    this(rawType, new Type[] { actualTypeArgument });
  }

  public ParameterizedType(Type rawType, Type[] actualTypeArguments) {
    this.rawType = rawType;
    this.actualTypeArguments = actualTypeArguments;
  }

  @Override
  public Type getRawType() {
    return rawType;
  }

  @Override
  public Type getOwnerType() {
    return null;
  }

  @Override
  public Type[] getActualTypeArguments() {
    return actualTypeArguments;
  }
}
