package ch.sbb.scion.rcp.microfrontend;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.JsonElement;

import ch.sbb.scion.rcp.microfrontend.browser.JavaCallback;
import ch.sbb.scion.rcp.microfrontend.browser.JavaScriptExecutor;
import ch.sbb.scion.rcp.microfrontend.browser.RxJsObservable;
import ch.sbb.scion.rcp.microfrontend.host.MicrofrontendPlatformRcpHost;
import ch.sbb.scion.rcp.microfrontend.internal.ParameterizedType;
import ch.sbb.scion.rcp.microfrontend.internal.Resources;
import ch.sbb.scion.rcp.microfrontend.internal.gson.GsonFactory;
import ch.sbb.scion.rcp.microfrontend.model.ISubscriber;
import ch.sbb.scion.rcp.microfrontend.model.ISubscription;
import ch.sbb.scion.rcp.microfrontend.model.Intent;
import ch.sbb.scion.rcp.microfrontend.model.IntentMessage;
import ch.sbb.scion.rcp.microfrontend.model.PublishOptions;
import ch.sbb.scion.rcp.microfrontend.model.Qualifier;
import ch.sbb.scion.rcp.microfrontend.model.RequestOptions;
import ch.sbb.scion.rcp.microfrontend.model.TopicMessage;
import ch.sbb.scion.rcp.microfrontend.script.Script;
import ch.sbb.scion.rcp.microfrontend.script.Script.Flags;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Helpers;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Refs;

/**
 * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html
 */
@Component(service = SciIntentClient.class)
public class SciIntentClient {

  @Reference
  private MicrofrontendPlatformRcpHost microfrontendPlatformRcpHost;

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#publish
   */
  public CompletableFuture<Void> publish(Intent intent) {
    return publishJson(intent, null, null);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#publish
   */
  public CompletableFuture<Void> publish(Intent intent, Object body) {
    return publishJson(intent, GsonFactory.create().toJson(body), null);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#publish
   */
  public CompletableFuture<Void> publish(Intent intent, PublishOptions options) {
    return publishJson(intent, null, options);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#publish
   */
  public CompletableFuture<Void> publish(Intent intent, Object body, PublishOptions options) {
    return publishJson(intent, GsonFactory.create().toJson(body), options);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#publish
   */
  public CompletableFuture<Void> publish(Intent intent, JsonElement jsonElement, PublishOptions options) {
    return publishJson(intent, GsonFactory.create().toJson(jsonElement), options);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#publish
   */
  public ISubscription subscribe(IntentSelector selector, ISubscriber<IntentMessage<Void>> subscriber) {
    return subscribe(selector, Void.class, subscriber);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#observe_
   */
  public <T> ISubscription subscribe(IntentSelector selector, Class<T> clazz, ISubscriber<IntentMessage<T>> subscriber) {
    selector = Optional.ofNullable(selector).orElse(new IntentSelector());
    var observeIIFE = new Script(Resources.readString("js/sci-intent-client/observe.iife.js"))
        .replacePlaceholder("refs.IntentClient", Refs.IntentClient)
        .replacePlaceholder("selector.type", selector.type, Flags.ToJson)
        .replacePlaceholder("selector.qualifier", selector.qualifier, Flags.ToJson)
        .replacePlaceholder("helpers.fromJson", Helpers.fromJson)
        .substitute();

    var type = (Void.class.equals(clazz) ? IntentMessage.class : new ParameterizedType(IntentMessage.class, clazz));
    var observable = new RxJsObservable<IntentMessage<T>>(microfrontendPlatformRcpHost.whenHostBrowser, observeIIFE, type);
    return observable.subscribe(subscriber);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#request_
   */
  public <T> ISubscription request(Intent intent, Class<T> clazz, ISubscriber<TopicMessage<T>> subscriber) {
    return requestJson(intent, null, null, clazz, subscriber);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#request_
   */
  public <T> ISubscription request(Intent intent, Object body, Class<T> clazz, ISubscriber<TopicMessage<T>> subscriber) {
    return requestJson(intent, GsonFactory.create().toJson(body), null, clazz, subscriber);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#request_
   */
  public <T> ISubscription request(Intent intent, RequestOptions options, Class<T> clazz, ISubscriber<TopicMessage<T>> subscriber) {
    return requestJson(intent, null, options, clazz, subscriber);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#request_
   */
  public <T> ISubscription request(Intent intent, Object body, RequestOptions options, Class<T> clazz, ISubscriber<TopicMessage<T>> subscriber) {
    return requestJson(intent, GsonFactory.create().toJson(body), options, clazz, subscriber);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#publish
   */
  public <T> ISubscription request(Intent intent, JsonElement jsonElement, RequestOptions options, Class<T> clazz, ISubscriber<TopicMessage<T>> subscriber) {
    return requestJson(intent, GsonFactory.create().toJson(jsonElement), options, clazz, subscriber);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#observe_
   */
  private <T> ISubscription requestJson(Intent intent, String json, RequestOptions options, Class<T> clazz, ISubscriber<TopicMessage<T>> subscriber) {
    options = Optional.ofNullable(options).orElse(new RequestOptions());
    var requestIIFE = new Script(Resources.readString("js/sci-intent-client/request.iife.js"))
        .replacePlaceholder("refs.IntentClient", Refs.IntentClient)
        .replacePlaceholder("intent", intent, Flags.ToJson)
        .replacePlaceholder("body", json)
        .replacePlaceholder("options.headers", options.getHeaders(), Flags.ToJson)
        .replacePlaceholder("options.retain", options.isRetain())
        .replacePlaceholder("helpers.fromJson", Helpers.fromJson)
        .substitute();

    var observable = new RxJsObservable<TopicMessage<T>>(microfrontendPlatformRcpHost.whenHostBrowser, requestIIFE, new ParameterizedType(TopicMessage.class, clazz));
    return observable.subscribe(subscriber);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#publish
   */
  private CompletableFuture<Void> publishJson(Intent intent, String json, PublishOptions intentOptions) {
    var options = Optional.ofNullable(intentOptions).orElse(new PublishOptions());
    var published = new CompletableFuture<Void>();
    new JavaCallback(microfrontendPlatformRcpHost.whenHostBrowser, args -> {
      var error = args[0];
      if (error == null) {
        published.complete(null);
      }
      else {
        published.completeExceptionally(new RuntimeException((String) error));
      }
    })
        .installOnce()
        .thenAccept(callback -> {
          new JavaScriptExecutor(microfrontendPlatformRcpHost.hostBrowser, Resources.readString("js/sci-intent-client/publish.js"))
              .replacePlaceholder("callback", callback.name)
              .replacePlaceholder("intent", intent, Flags.ToJson)
              .replacePlaceholder("body", json)
              .replacePlaceholder("options.headers", options.getHeaders(), Flags.ToJson)
              .replacePlaceholder("options.retain", options.isRetain())
              .replacePlaceholder("refs.IntentClient", Refs.IntentClient)
              .replacePlaceholder("helpers.fromJson", Helpers.fromJson)
              .runInsideAsyncFunction()
              .execute();
        });

    return published;
  }

  public static class IntentSelector {

    private String type;
    private Qualifier qualifier;

    public String getType() {
      return type;
    }

    public IntentSelector type(String type) {
      this.type = type;
      return this;
    }

    public Qualifier getQualifier() {
      return qualifier;
    }

    public IntentSelector qualifier(Qualifier qualifier) {
      this.qualifier = qualifier;
      return this;
    }
  }
}
