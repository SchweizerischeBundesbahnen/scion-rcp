package ch.sbb.scion.rcp.microfrontend;

import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonElement;

import ch.sbb.scion.rcp.microfrontend.model.PublishOptions;
import ch.sbb.scion.rcp.microfrontend.model.RequestOptions;
import ch.sbb.scion.rcp.microfrontend.model.TopicMessage;
import ch.sbb.scion.rcp.microfrontend.subscriber.ISubscriber;
import ch.sbb.scion.rcp.microfrontend.subscriber.ISubscription;

/**
 * @see <a href="https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html">MessageClient</a>
 */
public interface MessageClient {

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html#publish"
   */
  CompletableFuture<Void> publish(final String topic);

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html#publish"
   */
  CompletableFuture<Void> publish(final String topic, final Object message);

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html#publish"
   */
  CompletableFuture<Void> publish(final String topic, final PublishOptions options);

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html#publish"
   */
  CompletableFuture<Void> publish(final String topic, final Object message, final PublishOptions options);

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html#publish"
   */
  CompletableFuture<Void> publish(final String topic, final JsonElement jsonElement, final PublishOptions options);

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html#request_"
   */
  <T> ISubscription request(final String topic, final Class<T> clazz, final ISubscriber<TopicMessage<T>> subscriber);

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html#request_"
   */
  <T> ISubscription request(final String topic, final Object body, final Class<T> clazz, final ISubscriber<TopicMessage<T>> subscriber);

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html#request_"
   */
  <T> ISubscription request(final String topic, final RequestOptions options, final Class<T> clazz,
      final ISubscriber<TopicMessage<T>> subscriber);

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html#request_"
   */
  <T> ISubscription request(final String topic, final Object body, final RequestOptions options, final Class<T> clazz,
      final ISubscriber<TopicMessage<T>> subscriber);

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html#request_"
   */
  <T> ISubscription request(final String topic, final JsonElement jsonElement, final RequestOptions options, final Class<T> clazz,
      final ISubscriber<TopicMessage<T>> subscriber);

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html#observe_"
   */
  ISubscription subscribe(final String topic, final ISubscriber<TopicMessage<String>> subscriber);

  /**
   * @see "https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html#observe_"
   */
  <T> ISubscription subscribe(final String topic, final Class<T> clazz, final ISubscriber<TopicMessage<T>> subscriber);

}
