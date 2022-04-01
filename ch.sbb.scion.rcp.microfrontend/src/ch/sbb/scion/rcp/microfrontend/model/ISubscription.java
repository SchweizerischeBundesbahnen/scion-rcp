package ch.sbb.scion.rcp.microfrontend.model;

@FunctionalInterface
public interface ISubscription {

  public void unsubscribe();
}
