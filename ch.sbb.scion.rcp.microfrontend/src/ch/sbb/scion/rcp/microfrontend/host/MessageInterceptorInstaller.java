package ch.sbb.scion.rcp.microfrontend.host;

import java.lang.reflect.Type;

import org.eclipse.swt.browser.Browser;
import org.osgi.service.component.annotations.Component;

import ch.sbb.scion.rcp.microfrontend.browser.JavaCallback;
import ch.sbb.scion.rcp.microfrontend.browser.JavaScriptExecutor;
import ch.sbb.scion.rcp.microfrontend.interceptor.MessageInterceptor;
import ch.sbb.scion.rcp.microfrontend.internal.GsonFactory;
import ch.sbb.scion.rcp.microfrontend.internal.ParameterizedType;
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
    new JavaScriptExecutor(hostBrowser, """
        const topic = ${helpers.fromJson}('${topic}');
        const topicMatcher = new ${TopicMatcher}(topic);

        // Register the interceptor in the SCION Microfrontend Platform.
        ${Beans}.register(${MessageInterceptor}, {useValue: new class {
          intercept(message, next) {
            const topicMatch = topicMatcher.match(message.topic);
            if (!topicMatch.matches) {
              next.handle(message); // continue the chain of interceptors
              return;
            }

            new Promise(resolve => {
              const nextCallbackName = '${interceptorCallback}_nextCallback';
              ${storage}[nextCallbackName] = resolve;
              window['${interceptorCallback}'](${helpers.toJson}({...message, params: topicMatch.params}), nextCallbackName);
            })
            .then(result => {
              if (result instanceof Error) {
                // TODO: https://github.com/SchweizerischeBundesbahnen/scion-microfrontend-platform/issues/147
                //       Error is not reported back to the caller.
                throw result;
              }
              else if (result !== null) {
                next.handle(result); // continue the chain of interceptors
              }
            });
          }
        }, multi: true});

        """)
        .replacePlaceholder("interceptorCallback", interceptorCallback.name)
        .replacePlaceholder("topic", interceptorDescriptor.topic, Flags.ToJson)
        .replacePlaceholder("Beans", Refs.Beans)
        .replacePlaceholder("MessageInterceptor", Refs.MessageInterceptor)
        .replacePlaceholder("TopicMatcher", Refs.TopicMatcher)
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

  public record MessageInterceptorDescriptor<T> (String topic, MessageInterceptor<T> interceptor, Type payloadClazz) {
  }
}
