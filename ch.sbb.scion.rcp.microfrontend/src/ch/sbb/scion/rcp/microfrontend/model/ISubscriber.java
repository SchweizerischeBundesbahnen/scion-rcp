package ch.sbb.scion.rcp.microfrontend.model;

import org.eclipse.core.runtime.Platform;

@FunctionalInterface
public interface ISubscriber<T> {

  void onNext(T next);

  default void onError(Exception e) {
    Platform.getLog(ISubscriber.class).warn("Unhandled error in subscriber: " + e.getMessage(), e);
  }

  default void onComplete() {
    // NOOP
  }
}