package ch.sbb.scion.rcp.microfrontend;

import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonElement;

import ch.sbb.scion.rcp.microfrontend.model.Intent;
import ch.sbb.scion.rcp.microfrontend.model.IntentMessage;
import ch.sbb.scion.rcp.microfrontend.model.IntentSelector;
import ch.sbb.scion.rcp.microfrontend.model.PublishOptions;
import ch.sbb.scion.rcp.microfrontend.model.RequestOptions;
import ch.sbb.scion.rcp.microfrontend.model.TopicMessage;
import ch.sbb.scion.rcp.microfrontend.subscriber.ISubscriber;
import ch.sbb.scion.rcp.microfrontend.subscriber.ISubscription;

/**
 * @see <a href="https://microfrontend-platform-api.scion.vercel.app/classes/IntentClient.html">IntentClient</a>
 */
public interface IntentClient {

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/IntentClient.html#publish"
   */
  CompletableFuture<Void> publish(final Intent intent);

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/IntentClient.html#publish"
   */
  CompletableFuture<Void> publish(final Intent intent, final Object body);

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/IntentClient.html#publish"
   */
  CompletableFuture<Void> publish(final Intent intent, final PublishOptions options);

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/IntentClient.html#publish"
   */
  CompletableFuture<Void> publish(final Intent intent, final Object body, final PublishOptions options);

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/IntentClient.html#publish"
   */
  CompletableFuture<Void> publish(final Intent intent, final JsonElement jsonElement, final PublishOptions options);

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/IntentClient.html#publish"
   */
  ISubscription subscribe(final IntentSelector selector, final ISubscriber<IntentMessage<Void>> subscriber);

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/IntentClient.html#observe_"
   */
  <T> ISubscription subscribe(IntentSelector selector, final Class<T> clazz, final ISubscriber<IntentMessage<T>> subscriber);

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/IntentClient.html#request_"
   */
  <T> ISubscription request(final Intent intent, final Class<T> clazz, final ISubscriber<TopicMessage<T>> subscriber);

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/IntentClient.html#request_"
   */
  <T> ISubscription request(final Intent intent, final Object body, final Class<T> clazz, final ISubscriber<TopicMessage<T>> subscriber);

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/IntentClient.html#request_"
   */
  <T> ISubscription request(final Intent intent, final RequestOptions options, final Class<T> clazz,
      final ISubscriber<TopicMessage<T>> subscriber);

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/IntentClient.html#request_"
   */
  <T> ISubscription request(final Intent intent, final Object body, final RequestOptions options, final Class<T> clazz,
      final ISubscriber<TopicMessage<T>> subscriber);

  /**
   * @see "https://microfrontend-platform-api.scion.vercel.app/classes/IntentClient.html#publish"
   */
  <T> ISubscription request(final Intent intent, final JsonElement jsonElement, final RequestOptions options, final Class<T> clazz,
      final ISubscriber<TopicMessage<T>> subscriber);

}
