package ch.sbb.scion.rcp.microfrontend.host;

import java.lang.reflect.Type;

import org.eclipse.swt.browser.Browser;
import org.osgi.service.component.annotations.Component;

import ch.sbb.scion.rcp.microfrontend.browser.JavaCallback;
import ch.sbb.scion.rcp.microfrontend.browser.JavaScriptExecutor;
import ch.sbb.scion.rcp.microfrontend.interceptor.IntentInterceptor;
import ch.sbb.scion.rcp.microfrontend.internal.GsonFactory;
import ch.sbb.scion.rcp.microfrontend.internal.ParameterizedType;
import ch.sbb.scion.rcp.microfrontend.model.IntentMessage;
import ch.sbb.scion.rcp.microfrontend.model.Qualifier;
import ch.sbb.scion.rcp.microfrontend.script.Script.Flags;
import ch.sbb.scion.rcp.microfrontend.script.Scripts;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Helpers;
import ch.sbb.scion.rcp.microfrontend.script.Scripts.Refs;

@Component(service = IntentInterceptorInstaller.class)
public class IntentInterceptorInstaller {

  /**
   * Installs given intent interceptor.
   */
  public <T> void install(IntentInterceptorDescriptor<T> interceptorDescriptor, Browser hostBrowser) {
    createJavaInterceptorCallback(interceptorDescriptor, hostBrowser)
        .install()
        .thenAccept(callback -> registerInterceptor(callback, interceptorDescriptor, hostBrowser));
  }

  /**
   * Registers the passed interceptor in the SCION Microfrontend Platform.
   * 
   * Intercepted messages are delegated to the passed callback.
   */
  private <T> void registerInterceptor(JavaCallback interceptorCallback, IntentInterceptorDescriptor<T> interceptorDescriptor, Browser hostBrowser) {
    new JavaScriptExecutor(hostBrowser, """
        const type = ${helpers.fromJson}('${type}');
        const qualifier = ${helpers.fromJson}('${qualifier}');
        const qualifierMatcher = qualifier && new ${QualifierMatcher}(qualifier, {evalAsterisk: true, evalOptional: true});

        // Register the interceptor in the SCION Microfrontend Platform.
        ${Beans}.register(${IntentInterceptor}, {useValue: new class {
          intercept(message, next) {
            // Test intent type
            if (message.intent.type !== type) {
              next.handle(message); // continue the chain of interceptors
              return;
            }

            // Test intent qualifier, but only if passed a qualifier
            if (qualifierMatcher && !qualifierMatcher.matches(message.intent.qualifier)) {
              next.handle(message); // continue the chain of interceptors
              return;
            }

            new Promise(resolve => {
              const nextCallbackName = '${interceptorCallback}_nextCallback';
              ${storage}[nextCallbackName] = resolve;
              window['${interceptorCallback}'](${helpers.toJson}(message), nextCallbackName);
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
        .replacePlaceholder("type", interceptorDescriptor.type, Flags.ToJson)
        .replacePlaceholder("qualifier", interceptorDescriptor.qualifier, Flags.ToJson)
        .replacePlaceholder("Beans", Refs.Beans)
        .replacePlaceholder("IntentInterceptor", Refs.IntentInterceptor)
        .replacePlaceholder("QualifierMatcher", Refs.QualifierMatcher)
        .replacePlaceholder("helpers.fromJson", Helpers.fromJson)
        .replacePlaceholder("helpers.toJson", Helpers.toJson)
        .replacePlaceholder("storage", Scripts.Storage)
        .execute();
  }

  /**
   * Creates the Java callback for intercepting intents.
   */
  private <T> JavaCallback createJavaInterceptorCallback(IntentInterceptorDescriptor<T> interceptorDescriptor, Browser hostBrowser) {
    return new JavaCallback(hostBrowser, args -> {
      IntentMessage<T> intent = GsonFactory.create().fromJson((String) args[0], new ParameterizedType(IntentMessage.class, interceptorDescriptor.payloadClazz));
      interceptorDescriptor.interceptor.intercept(intent, new InterceptorChainImpl(hostBrowser, (String) args[1]));
    });
  }

  public record IntentInterceptorDescriptor<T> (String type, Qualifier qualifier, IntentInterceptor<T> interceptor, Type payloadClazz) {
  }
}
