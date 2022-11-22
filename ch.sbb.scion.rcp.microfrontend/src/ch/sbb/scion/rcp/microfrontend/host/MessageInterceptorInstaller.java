package ch.sbb.scion.rcp.microfrontend.host;

import java.lang.reflect.Type;

import org.eclipse.swt.browser.Browser;
import org.osgi.service.component.annotations.Component;

import ch.sbb.scion.rcp.microfrontend.browser.JavaCallback;
import ch.sbb.scion.rcp.microfrontend.browser.JavaScriptExecutor;
import ch.sbb.scion.rcp.microfrontend.interceptor.MessageInterceptor;
import ch.sbb.scion.rcp.microfrontend.internal.GsonFactory;
import ch.sbb.scion.rcp.microfrontend.internal.ParameterizedType;
import ch.sbb.scion.rcp.microfrontend.internal.Resources;
import ch.sbb.scion.rcp.microfrontend.model.TopicMessage;
import ch.sbb.scion.rcp.microfrontend.script.Scripts;
import ch.sbb.scion.rcp.microfrontend.script.Script.Flags;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Helpers;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Refs;

/**
 * Installs an interceptor to intercept messages sent to a topic destination.
 * 
 * @see https://scion-microfrontend-platform-developer-guide.vercel.app/#chapter:message-interception
 */
@Component(service = MessageInterceptorInstaller.class)
public class MessageInterceptorInstaller {
  /**
   * Installs given message interceptor.
   */
  public <T> void install(MessageInterceptorDescriptor<T> interceptorDescriptor, Browser hostBrowser) {
    createJavaInterceptorCallback(interceptorDescriptor, hostBrowser)
        .install()
        .thenAccept(callback -> registerInterceptor(callback, interceptorDescriptor, hostBrowser));
  }

  /**
   * Registers the passed interceptor in the SCION Microfrontend Platform.
   * 
   * Intercepted messages are delegated to the passed callback.
   */
  private <T> void registerInterceptor(JavaCallback interceptorCallback, MessageInterceptorDescriptor<T> interceptorDescriptor, Browser hostBrowser) {
    new JavaScriptExecutor(hostBrowser, Resources.readString("js/host/register-message-interceptor.js"))
        .replacePlaceholder("interceptorCallback", interceptorCallback.name)
        .replacePlaceholder("topic", interceptorDescriptor.topic, Flags.ToJson)
        .replacePlaceholder("refs.Beans", Refs.Beans)
        .replacePlaceholder("refs.MessageInterceptor", Refs.MessageInterceptor)
        .replacePlaceholder("refs.TopicMatcher", Refs.TopicMatcher)
        .replacePlaceholder("refs.UUID", Refs.UUID)
        .replacePlaceholder("helpers.toJson", Helpers.toJson)
        .replacePlaceholder("helpers.fromJson", Helpers.fromJson)
        .replacePlaceholder("storage", Scripts.Storage)
        .execute();
  }

  /**
   * Creates the Java callback for intercepting messages.
   */
  private <T> JavaCallback createJavaInterceptorCallback(MessageInterceptorDescriptor<T> interceptorDescriptor, Browser hostBrowser) {
    return new JavaCallback(hostBrowser, args -> {
      TopicMessage<T> message = GsonFactory.create().fromJson((String) args[0], new ParameterizedType(TopicMessage.class, interceptorDescriptor.payloadClazz));
      interceptorDescriptor.interceptor.intercept(message, new InterceptorChainImpl(hostBrowser, (String) args[1]));
    });
  }

  public static class MessageInterceptorDescriptor<T> {
    public String topic;
    public MessageInterceptor<T> interceptor;
    public Type payloadClazz;

    public MessageInterceptorDescriptor(String topic, MessageInterceptor<T> interceptor, Type payloadClazz) {
      this.topic = topic;
      this.interceptor = interceptor;
      this.payloadClazz = payloadClazz;
    }
  }
}
