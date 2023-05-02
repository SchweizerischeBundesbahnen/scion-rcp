package ch.sbb.scion.rcp.microfrontend.subscriber;

@FunctionalInterface
public interface ISubscription {

  public void unsubscribe();
}
