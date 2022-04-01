package ch.sbb.scion.rcp.microfrontend;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import ch.sbb.scion.rcp.microfrontend.browser.BrowserCallback;
import ch.sbb.scion.rcp.microfrontend.browser.BrowserCallback.Options;
import ch.sbb.scion.rcp.microfrontend.browser.BrowserRxJsObservable;
import ch.sbb.scion.rcp.microfrontend.browser.BrowserScriptExecutor;
import ch.sbb.scion.rcp.microfrontend.host.MicrofrontendPlatformHostApp;
import ch.sbb.scion.rcp.microfrontend.internal.ParameterizedType;
import ch.sbb.scion.rcp.microfrontend.model.ISubscriber;
import ch.sbb.scion.rcp.microfrontend.model.ISubscription;
import ch.sbb.scion.rcp.microfrontend.model.TopicMessage;
import ch.sbb.scion.rcp.microfrontend.script.Script;
import ch.sbb.scion.rcp.microfrontend.script.Script.Flags;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Refs;

/**
 * @see https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html
 */
@Component(service = SciMessageClient.class)
public class SciMessageClient {

  @Reference
  private MicrofrontendPlatformHostApp microfrontendPlatformHostApp;

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html#publish
   */
  public CompletableFuture<Void> publish(String topic) {
    return publishJson(topic, null, null);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html#publish
   */
  public CompletableFuture<Void> publish(String topic, Object message) {
    return publish(topic, message, null);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html#publish
   */
  public CompletableFuture<Void> publish(String topic, PublishOptions options) {
    return publishJson(topic, null, options);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html#publish
   */
  public CompletableFuture<Void> publish(String topic, Object message, PublishOptions options) {
    return publishJson(topic, new Gson().toJson(message), options);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html#publish
   */
  public CompletableFuture<Void> publish(String topic, JsonElement jsonElement, PublishOptions options) {
    return publishJson(topic, new Gson().toJson(jsonElement), options);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html#observe_
   */
  public ISubscription subscribe(String topic, ISubscriber<TopicMessage<String>> listener) {
    return subscribe(topic, listener, String.class);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html#observe_
   */
  public <T> ISubscription subscribe(String topic, ISubscriber<TopicMessage<T>> subscriber, Class<T> clazz) {
    var script = new Script("""
        ${refs.MessageClient}.observe$(JSON.parse('${topic}'))
        """)
        .replacePlaceholder("refs.MessageClient", Refs.MessageClient)
        .replacePlaceholder("topic", topic, Flags.ToJson)
        .substitute();

    var observable = new BrowserRxJsObservable<TopicMessage<T>>(microfrontendPlatformHostApp.whenHostBrowser, script, new ParameterizedType(TopicMessage.class, clazz));
    return observable.subscribe(subscriber);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/MessageClient.html#publish
   */
  private CompletableFuture<Void> publishJson(String topic, String json, PublishOptions options) {
    options = Optional.ofNullable(options).orElse(new PublishOptions());
    var published = new CompletableFuture<Void>();
    var browserCallback = new BrowserCallback("__sci_messageclient$onpublish_" + UUID.randomUUID(), microfrontendPlatformHostApp.whenHostBrowser, new Options()
        .once()
        .onCallback(args -> {
          var error = args[0];
          if (error == null) {
            published.complete(null);
          }
          else {
            published.completeExceptionally(new RuntimeException((String) error));
          }
        }));

    new BrowserScriptExecutor(microfrontendPlatformHostApp.whenHostBrowser, """
        try {
          await ${refs.MessageClient}.publish(JSON.parse('${topic}'), JSON.parse('${message}') ?? null, {
            headers: JSON.parse('${options.headers}') ?? undefined,
            retain: ${options.retain} ?? undefined,
          });
          window['${callbackName}'](null);
        }
        catch (error) {
          window['${callbackName}'](error.message ?? `${error}` ?? 'ERROR');
        }
        """)
        .replacePlaceholder("callbackName", browserCallback.name)
        .replacePlaceholder("topic", topic, Flags.ToJson)
        .replacePlaceholder("message", json)
        .replacePlaceholder("options.headers", options.headers, Flags.ToJson)
        .replacePlaceholder("options.retain", options.retain)
        .replacePlaceholder("refs.MessageClient", Refs.MessageClient)
        .runInsideAsyncFunction()
        .execute();

    return published;
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/interfaces/PublishOptions.html
   */
  public static class PublishOptions {
    private Map<String, Object> headers;
    private Boolean retain;

    public Map<String, Object> getHeaders() {
      return headers;
    }

    public PublishOptions headers(Map<String, Object> headers) {
      this.headers = headers;
      return this;
    }

    public PublishOptions retain(boolean retain) {
      this.retain = retain;
      return this;
    }

    public PublishOptions retain() {
      retain(true);
      return this;
    }
  }
}
