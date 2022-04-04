package ch.sbb.scion.rcp.microfrontend;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import ch.sbb.scion.rcp.microfrontend.browser.RxJsObservable;
import ch.sbb.scion.rcp.microfrontend.browser.JavaScriptExecutor;
import ch.sbb.scion.rcp.microfrontend.browser.JavaScriptCallback;
import ch.sbb.scion.rcp.microfrontend.host.MicrofrontendPlatformRcpHost;
import ch.sbb.scion.rcp.microfrontend.internal.ParameterizedType;
import ch.sbb.scion.rcp.microfrontend.model.ISubscriber;
import ch.sbb.scion.rcp.microfrontend.model.ISubscription;
import ch.sbb.scion.rcp.microfrontend.model.Intent;
import ch.sbb.scion.rcp.microfrontend.model.IntentMessage;
import ch.sbb.scion.rcp.microfrontend.model.TopicMessage;
import ch.sbb.scion.rcp.microfrontend.script.Script;
import ch.sbb.scion.rcp.microfrontend.script.Script.Flags;
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
    return publishJson(intent, new Gson().toJson(body), null);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#publish
   */
  public CompletableFuture<Void> publish(Intent intent, IntentOptions options) {
    return publishJson(intent, null, options);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#publish
   */
  public CompletableFuture<Void> publish(Intent intent, Object body, IntentOptions options) {
    return publishJson(intent, new Gson().toJson(body), options);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#publish
   */
  public CompletableFuture<Void> publish(Intent intent, JsonElement jsonElement, IntentOptions options) {
    return publishJson(intent, new Gson().toJson(jsonElement), options);
  }

  public ISubscription subscribe(IntentSelector selector, ISubscriber<IntentMessage<Void>> subscriber) {
    return subscribe(selector, Void.class, subscriber);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#observe_
   */
  public <T> ISubscription subscribe(IntentSelector selector, Class<T> clazz, ISubscriber<IntentMessage<T>> subscriber) {
    selector = Optional.ofNullable(selector).orElse(new IntentSelector());
    var observeIIFE = new Script("""
        (() => ${refs.IntentClient}.observe$({
          type: JSON.parse('${selector.type}') ?? undefined,
          qualifier: JSON.parse('${selector.qualifier}') ?? undefined,
        }))()
        """)
        .replacePlaceholder("refs.IntentClient", Refs.IntentClient)
        .replacePlaceholder("selector.type", selector.type, Flags.ToJson)
        .replacePlaceholder("selector.qualifier", selector.qualifier, Flags.ToJson)
        .replacePlaceholder("selector", selector, Flags.ToJson | Flags.UndefinedIfNull)
        .substitute();

    var observable = new RxJsObservable<IntentMessage<T>>(microfrontendPlatformRcpHost.whenHostBrowser, observeIIFE, new ParameterizedType(IntentMessage.class, clazz));
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
    return requestJson(intent, new Gson().toJson(body), null, clazz, subscriber);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#request_
   */
  public <T> ISubscription request(Intent intent, IntentOptions options, Class<T> clazz, ISubscriber<TopicMessage<T>> subscriber) {
    return requestJson(intent, null, options, clazz, subscriber);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#request_
   */
  public <T> ISubscription request(Intent intent, Object body, IntentOptions options, Class<T> clazz, ISubscriber<TopicMessage<T>> subscriber) {
    return requestJson(intent, new Gson().toJson(body), options, clazz, subscriber);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#publish
   */
  public <T> ISubscription request(Intent intent, JsonElement jsonElement, IntentOptions options, Class<T> clazz, ISubscriber<TopicMessage<T>> subscriber) {
    return requestJson(intent, new Gson().toJson(jsonElement), options, clazz, subscriber);
  }
  
  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#observe_
   */
  private <T> ISubscription requestJson(Intent intent, String json, IntentOptions options, Class<T> clazz, ISubscriber<TopicMessage<T>> subscriber) {
    options = Optional.ofNullable(options).orElse(new IntentOptions());
    var requestIIFE = new Script("""
        (() => {
          const intent = JSON.parse('${intent}');
          const body = JSON.parse('${body}') ?? null;
          const options = {
            headers: JSON.parse('${options.headers}') ?? undefined,
          };
          return ${refs.IntentClient}.request$(intent, body, options);
        }()
        """)
        .replacePlaceholder("refs.IntentClient", Refs.IntentClient)
        .replacePlaceholder("intent", intent, Flags.ToJson)
        .replacePlaceholder("body", json)
        .replacePlaceholder("options.headers", options.headers, Flags.ToJson)
        .substitute();

    var observable = new RxJsObservable<TopicMessage<T>>(microfrontendPlatformRcpHost.whenHostBrowser, requestIIFE, new ParameterizedType(TopicMessage.class, clazz));
    return observable.subscribe(subscriber);
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html#publish
   */
  private CompletableFuture<Void> publishJson(Intent intent, String json, IntentOptions options) {
    options = Optional.ofNullable(options).orElse(new IntentOptions());
    var published = new CompletableFuture<Void>();
    var callback = new JavaScriptCallback(microfrontendPlatformRcpHost.whenHostBrowser, args -> {
      var error = args[0];
      if (error == null) {
        published.complete(null);
      }
      else {
        published.completeExceptionally(new RuntimeException((String) error));
      }
    }).installOnce();

    new JavaScriptExecutor(microfrontendPlatformRcpHost.whenHostBrowser, """
        try {
          await ${refs.IntentClient}.publish(JSON.parse('${intent}'), JSON.parse('${body}') ?? null, {
            headers: JSON.parse('${options.headers}') ?? undefined
          });
          window['${callback}'](null);
        }
        catch (error) {
          window['${callback}'](error.message ?? `${error}` ?? 'ERROR');
        }
        """)
        .replacePlaceholder("callback", callback.name)
        .replacePlaceholder("intent", intent, Flags.ToJson)
        .replacePlaceholder("body", json)
        .replacePlaceholder("options.headers", options.headers, Flags.ToJson)
        .replacePlaceholder("refs.IntentClient", Refs.IntentClient)
        .runInsideAsyncFunction()
        .execute();

    return published;
  }

  /**
   * @see https://scion-microfrontend-platform-api.vercel.app/interfaces/IntentOptions.html
   */
  public static class IntentOptions {

    private Map<String, Object> headers;

    public Map<String, Object> getHeaders() {
      return headers;
    }

    public IntentOptions headers(Map<String, Object> headers) {
      this.headers = headers;
      return this;
    }
  }

  public static class IntentSelector {

    private String type;
    private Map<String, Object> qualifier;

    public String getType() {
      return type;
    }

    public IntentSelector type(String type) {
      this.type = type;
      return this;
    }

    public Map<String, Object> getQualifier() {
      return qualifier;
    }

    public IntentSelector qualifier(Map<String, Object> qualifier) {
      this.qualifier = qualifier;
      return this;
    }
  }
}
