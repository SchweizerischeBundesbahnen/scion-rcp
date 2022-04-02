package ch.sbb.scion.rcp.microfrontend.browser;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.browser.Browser;

import com.google.gson.Gson;

import ch.sbb.scion.rcp.microfrontend.SciMessageClient;
import ch.sbb.scion.rcp.microfrontend.browser.BrowserCallback.Options;
import ch.sbb.scion.rcp.microfrontend.internal.ParameterizedType;
import ch.sbb.scion.rcp.microfrontend.model.IDisposable;
import ch.sbb.scion.rcp.microfrontend.model.ISubscriber;
import ch.sbb.scion.rcp.microfrontend.model.ISubscription;
import ch.sbb.scion.rcp.microfrontend.script.Scripts;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.JsonHelpers;

/**
 * Subscribes to an RxJS Observable in the browser.
 */
public class BrowserRxJsObservable<T> {

  private CompletableFuture<Browser> whenBrowser;
  private Type clazz;
  private String observableScript;

  public BrowserRxJsObservable(Browser browser, String observableScript, Type clazz) {
    this(CompletableFuture.completedFuture(browser), observableScript, clazz);
  }

  public BrowserRxJsObservable(CompletableFuture<Browser> browser, String rxJsObservableScript, Type clazz) {
    this.whenBrowser = browser;
    this.observableScript = rxJsObservableScript;
    this.clazz = clazz;
  }

  public ISubscription subscribe(ISubscriber<T> observer) {
    var subscriptionUuid = UUID.randomUUID();
    var disposables = new ArrayList<IDisposable>();

    var callback = new BrowserCallback(whenBrowser, new Options()
        .onCallback(args -> {
          try {
            var emission = new Gson().<Emission<T>>fromJson((String) args[0], new ParameterizedType(Emission.class, clazz));
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
        }));

    new BrowserScriptExecutor(whenBrowser, """
        try {
          const subscription = ${observable}.subscribe({
            next: (next) => window['${callback}'](${helpers.stringify}({type: 'Next', next})),
            error: (error) => window['${callback}'](${helpers.stringify}({type: 'Error', error: error.message || `${error}` || 'ERROR'})),
            complete: () => window['${callback}'](${helpers.stringify}({type: 'Complete'})),
          });
          ${storage}['${subscriptionUuid}'] = subscription;
        }
        catch (error) {
          console.error(error);
          window['${callback}']({type: 'Error', message: error.message ?? `${error}` ?? 'ERROR'});
        }
        """)
        .replacePlaceholder("callback", callback)
        .replacePlaceholder("helpers.stringify", JsonHelpers.stringify)
        .replacePlaceholder("storage", Scripts.Storage)
        .replacePlaceholder("subscriptionUuid", subscriptionUuid)
        .replacePlaceholder("observable", observableScript)
        .execute();

    disposables.add(callback);
    disposables.add(() -> {
      new BrowserScriptExecutor(whenBrowser, """
          ${storage}?.['${subscriptionUuid}']?.unsubscribe();
          delete ${storage}?.['${subscriptionUuid}']
          """)
          .replacePlaceholder("subscriptionUuid", subscriptionUuid)
          .replacePlaceholder("storage", Scripts.Storage)
          .execute();
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
