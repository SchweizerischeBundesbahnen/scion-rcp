package ch.sbb.scion.rcp.microfrontend.host;

import java.lang.reflect.Type;

import org.eclipse.swt.browser.Browser;
import org.osgi.service.component.annotations.Component;

import ch.sbb.scion.rcp.microfrontend.browser.JavaCallback;
import ch.sbb.scion.rcp.microfrontend.browser.JavaScriptExecutor;
import ch.sbb.scion.rcp.microfrontend.interceptor.IntentInterceptor;
import ch.sbb.scion.rcp.microfrontend.internal.ParameterizedType;
import ch.sbb.scion.rcp.microfrontend.internal.Resources;
import ch.sbb.scion.rcp.microfrontend.internal.gson.GsonFactory;
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
    createJavaInterceptorCallback(interceptorDescriptor, hostBrowser).install()
        .thenAccept(callback -> registerInterceptor(callback, interceptorDescriptor, hostBrowser));
  }

  /**
   * Registers the passed interceptor in the SCION Microfrontend Platform. Intercepted messages are delegated to the passed callback.
   */
  private <T> void registerInterceptor(JavaCallback interceptorCallback, IntentInterceptorDescriptor<T> interceptorDescriptor,
      Browser hostBrowser) {
    new JavaScriptExecutor(hostBrowser, Resources.readString("js/host/register-intent-interceptor.js"))
        .replacePlaceholder("interceptorCallback", interceptorCallback.name)
        .replacePlaceholder("type", interceptorDescriptor.type, Flags.ToJson)
        .replacePlaceholder("qualifier", interceptorDescriptor.qualifier, Flags.ToJson).replacePlaceholder("refs.Beans", Refs.Beans)
        .replacePlaceholder("refs.IntentInterceptor", Refs.IntentInterceptor)
        .replacePlaceholder("refs.QualifierMatcher", Refs.QualifierMatcher).replacePlaceholder("refs.UUID", Refs.UUID)
        .replacePlaceholder("helpers.fromJson", Helpers.fromJson).replacePlaceholder("helpers.toJson", Helpers.toJson)
        .replacePlaceholder("storage", Scripts.Storage).execute();
  }

  /**
   * Creates the Java callback for intercepting intents.
   */
  private <T> JavaCallback createJavaInterceptorCallback(IntentInterceptorDescriptor<T> interceptorDescriptor, Browser hostBrowser) {
    return new JavaCallback(hostBrowser, args -> {
      IntentMessage<T> intent = GsonFactory.create().fromJson((String) args[0],
          new ParameterizedType(IntentMessage.class, interceptorDescriptor.payloadClazz));
      interceptorDescriptor.interceptor.intercept(intent, new InterceptorChainImpl(hostBrowser, (String) args[1]));
    });
  }

  public static class IntentInterceptorDescriptor<T> {

    public String type;
    public Qualifier qualifier;
    public IntentInterceptor<T> interceptor;
    public Type payloadClazz;

    public IntentInterceptorDescriptor(String type, Qualifier qualifier, IntentInterceptor<T> interceptor, Type payloadClazz) {
      this.type = type;
      this.qualifier = qualifier;
      this.interceptor = interceptor;
      this.payloadClazz = payloadClazz;
    }
  }
}
