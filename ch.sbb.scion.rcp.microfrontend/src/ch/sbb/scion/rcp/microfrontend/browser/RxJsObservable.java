package ch.sbb.scion.rcp.microfrontend.browser;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.browser.Browser;

import ch.sbb.scion.rcp.microfrontend.SciMessageClient;
import ch.sbb.scion.rcp.microfrontend.internal.GsonFactory;
import ch.sbb.scion.rcp.microfrontend.internal.ParameterizedType;
import ch.sbb.scion.rcp.microfrontend.model.IDisposable;
import ch.sbb.scion.rcp.microfrontend.model.ISubscriber;
import ch.sbb.scion.rcp.microfrontend.model.ISubscription;
import ch.sbb.scion.rcp.microfrontend.script.Scripts;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Helpers;

/**
 * Subscribes to an RxJS Observable in the browser.
 */
public class RxJsObservable<T> {

  private CompletableFuture<Browser> whenBrowser;
  private Type clazz;
  private String rxjsObservableIIFE;

  public RxJsObservable(Browser browser, String observableScript, Type clazz) {
    this(CompletableFuture.completedFuture(browser), observableScript, clazz);
  }

  public RxJsObservable(CompletableFuture<Browser> browser, String rxjsObservableIIFE, Type clazz) {
    this.whenBrowser = browser;
    this.rxjsObservableIIFE = rxjsObservableIIFE;
    this.clazz = clazz;
  }

  public ISubscription subscribe(ISubscriber<T> observer) {
    var disposables = new ArrayList<IDisposable>();

    new JavaScriptCallback(whenBrowser, args -> {
      try {
        var emission = GsonFactory.create().<Emission<T>>fromJson((String) args[0], new ParameterizedType(Emission.class, clazz));
        switch (emission.type) {
          case Next: {
            observer.onNext(emission.next);
            break;
          }
          case Error: {
            observer.onError(new RuntimeException(emission.error));
            disposables.forEach(IDisposable::dispose);
            break;
          }
          case Complete: {
            observer.onComplete();
            disposables.forEach(IDisposable::dispose);
            break;
          }
        }
      }
      catch (RuntimeException e) {
        Platform.getLog(SciMessageClient.class).error("Unhandled error in callback", e);
      }
    })
        .addTo(disposables)
        .install()
        .thenAccept(callback -> {
          new JavaScriptExecutor(whenBrowser, """
              try {
                const subscription = ${rxjsObservableIIFE}.subscribe({
                  next: (next) => window['${callback}'](${helpers.toJson}({type: 'Next', next})),
                  error: (error) => window['${callback}'](${helpers.toJson}({type: 'Error', error: error.message || `${error}` || 'ERROR'})),
                  complete: () => window['${callback}'](${helpers.toJson}({type: 'Complete'})),
                });
                ${storage}['${callback}_subscription'] = subscription;
              }
              catch (error) {
                console.error(error);
                window['${callback}']({type: 'Error', message: error.message ?? `${error}` ?? 'ERROR'});
              }
              """)
              .replacePlaceholder("callback", callback.name)
              .replacePlaceholder("helpers.toJson", Helpers.toJson)
              .replacePlaceholder("storage", Scripts.Storage)
              .replacePlaceholder("rxjsObservableIIFE", rxjsObservableIIFE)
              .execute();

          disposables.add(() -> new JavaScriptExecutor(whenBrowser, """
              ${storage}['${callback}_subscription'].unsubscribe();
              delete ${storage}['${callback}_subscription']
              """)
              .replacePlaceholder("callback", callback.name)
              .replacePlaceholder("storage", Scripts.Storage)
              .execute());
        });

    return () -> disposables.forEach(IDisposable::dispose);
  }

  private static class Emission<T> {
    public static enum Type {
      Next, Error, Complete
    };

    public Type type;
    public T next;
    public String error;
  }
}
