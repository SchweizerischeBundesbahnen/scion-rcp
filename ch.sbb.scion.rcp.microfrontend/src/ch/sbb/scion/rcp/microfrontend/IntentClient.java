package ch.sbb.scion.rcp.microfrontend;

import java.util.Objects;
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
import ch.sbb.scion.rcp.microfrontend.subscriber.ISubscriber;
import ch.sbb.scion.rcp.microfrontend.subscriber.ISubscription;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @see <a href="https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html">IntentClient</a>
 */
@Component(service = IntentClient.class)
public class IntentClient {

  @Reference
  private MicrofrontendPlatformRcpHost microfrontendPlatformRcpHost;

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#publish"
   */
  public CompletableFuture<Void> publish(final Intent intent) {
    return publishJson(intent, null, null);
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#publish"
   */
  public CompletableFuture<Void> publish(final Intent intent, final Object body) {
    return publishJson(intent, GsonFactory.create().toJson(body), null);
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#publish"
   */
  public CompletableFuture<Void> publish(final Intent intent, final PublishOptions options) {
    return publishJson(intent, null, options);
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#publish"
   */
  public CompletableFuture<Void> publish(final Intent intent, final Object body, final PublishOptions options) {
    return publishJson(intent, GsonFactory.create().toJson(body), options);
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#publish"
   */
  public CompletableFuture<Void> publish(final Intent intent, final JsonElement jsonElement, final PublishOptions options) {
    return publishJson(intent, GsonFactory.create().toJson(jsonElement), options);
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#publish"
   */
  public ISubscription subscribe(final IntentSelector selector, final ISubscriber<IntentMessage<Void>> subscriber) {
    return subscribe(selector, Void.class, subscriber);
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#observe_"
   */
  public <T> ISubscription subscribe(IntentSelector selector, final Class<T> clazz, final ISubscriber<IntentMessage<T>> subscriber) {
    Objects.requireNonNull(clazz);
    Objects.requireNonNull(subscriber);
    selector = Optional.ofNullable(selector).orElse(new IntentSelector());
    var observeIIFE = new Script(Resources.readString("js/sci-intent-client/observe.iife.js"))
        .replacePlaceholder("refs.IntentClient", Refs.IntentClient).replacePlaceholder("selector.type", selector.type, Flags.ToJson)
        .replacePlaceholder("selector.qualifier", selector.qualifier, Flags.ToJson).replacePlaceholder("helpers.fromJson", Helpers.fromJson)
        .substitute();

    var type = (Void.class.equals(clazz) ? IntentMessage.class : new ParameterizedType(IntentMessage.class, clazz));
    var observable = new RxJsObservable<IntentMessage<T>>(microfrontendPlatformRcpHost.whenHostBrowser, observeIIFE, type);
    return observable.subscribe(subscriber);
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#request_"
   */
  public <T> ISubscription request(final Intent intent, final Class<T> clazz, final ISubscriber<TopicMessage<T>> subscriber) {
    return requestJson(intent, null, null, clazz, subscriber);
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#request_"
   */
  public <T> ISubscription request(final Intent intent, final Object body, final Class<T> clazz,
      final ISubscriber<TopicMessage<T>> subscriber) {
    return requestJson(intent, GsonFactory.create().toJson(body), null, clazz, subscriber);
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#request_"
   */
  public <T> ISubscription request(final Intent intent, final RequestOptions options, final Class<T> clazz,
      final ISubscriber<TopicMessage<T>> subscriber) {
    return requestJson(intent, null, options, clazz, subscriber);
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#request_"
   */
  public <T> ISubscription request(final Intent intent, final Object body, final RequestOptions options, final Class<T> clazz,
      final ISubscriber<TopicMessage<T>> subscriber) {
    return requestJson(intent, GsonFactory.create().toJson(body), options, clazz, subscriber);
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#publish"
   */
  public <T> ISubscription request(final Intent intent, final JsonElement jsonElement, final RequestOptions options, final Class<T> clazz,
      final ISubscriber<TopicMessage<T>> subscriber) {
    return requestJson(intent, GsonFactory.create().toJson(jsonElement), options, clazz, subscriber);
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#observe_"
   */
  private <T> ISubscription requestJson(final Intent intent, final String json, RequestOptions options, final Class<T> clazz,
      final ISubscriber<TopicMessage<T>> subscriber) {
    Objects.requireNonNull(intent);
    Objects.requireNonNull(clazz);
    Objects.requireNonNull(subscriber);
    options = Optional.ofNullable(options).orElse(new RequestOptions());
    var requestIIFE = new Script(Resources.readString("js/sci-intent-client/request.iife.js"))
        .replacePlaceholder("refs.IntentClient", Refs.IntentClient).replacePlaceholder("intent", intent, Flags.ToJson)
        .replacePlaceholder("body", json).replacePlaceholder("options.headers", options.headers(), Flags.ToJson)
        .replacePlaceholder("options.retain", options.isRetain()).replacePlaceholder("helpers.fromJson", Helpers.fromJson).substitute();

    var observable = new RxJsObservable<TopicMessage<T>>(microfrontendPlatformRcpHost.whenHostBrowser, requestIIFE,
        new ParameterizedType(TopicMessage.class, clazz));
    return observable.subscribe(subscriber);
  }

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#publish"
   */
  private CompletableFuture<Void> publishJson(final Intent intent, final String json, final PublishOptions intentOptions) {
    Objects.requireNonNull(intent);
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
    }).installOnce().thenAccept(callback -> {
      new JavaScriptExecutor(microfrontendPlatformRcpHost.hostBrowser, Resources.readString("js/sci-intent-client/publish.js"))
          .replacePlaceholder("callback", callback.name).replacePlaceholder("intent", intent, Flags.ToJson).replacePlaceholder("body", json)
          .replacePlaceholder("options.headers", options.headers(), Flags.ToJson).replacePlaceholder("options.retain", options.isRetain())
          .replacePlaceholder("refs.IntentClient", Refs.IntentClient).replacePlaceholder("helpers.fromJson", Helpers.fromJson)
          .runInsideAsyncFunction().execute();
    });

    return published;
  }

  @Accessors(fluent = true)
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @Builder
  @ToString
  public static class IntentSelector {

    private String type;
    private Qualifier qualifier;

  }
}
