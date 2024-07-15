package ch.sbb.scion.rcp.microfrontend.internal;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import java.lang.reflect.Type;

import org.eclipse.swt.browser.Browser;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.sbb.scion.rcp.microfrontend.MicrofrontendPlatform;
import ch.sbb.scion.rcp.microfrontend.host.MicrofrontendPlatformRcpHost;
import ch.sbb.scion.rcp.microfrontend.interceptor.IntentInterceptor;
import ch.sbb.scion.rcp.microfrontend.interceptor.MessageInterceptor;
import ch.sbb.scion.rcp.microfrontend.model.MicrofrontendPlatformConfig;
import ch.sbb.scion.rcp.microfrontend.model.Qualifier;

@Component
public class MicrofrontendPlatformImpl implements MicrofrontendPlatform {

  @Reference
  private MicrofrontendPlatformRcpHost microfrontendPlatformRcpHost;

  @Override
  public CompletableFuture<Browser> startHost(final MicrofrontendPlatformConfig config) {
    return startHost(config, true);
  }

  @Override
  public CompletableFuture<Browser> startHost(final MicrofrontendPlatformConfig config, final boolean headless) {
    Objects.requireNonNull(config);
    return microfrontendPlatformRcpHost.start(config, headless);
  }

  @Override
  public <T> void registerMessageInterceptor(final String topic, final MessageInterceptor<T> interceptor) {
    registerMessageInterceptor(topic, interceptor, Object.class);
  }

  @Override
  public <T> void registerMessageInterceptor(final String topic, final MessageInterceptor<T> interceptor, final Type payloadClazz) {
    Objects.requireNonNull(topic);
    Objects.requireNonNull(interceptor);
    Objects.requireNonNull(payloadClazz);
    microfrontendPlatformRcpHost.registerMessageInterceptor(topic, interceptor, payloadClazz);
  }

  @Override
  public <T> void registerIntentInterceptor(final String type, final IntentInterceptor<T> interceptor) {
    registerIntentInterceptor(type, null, interceptor);
  }

  @Override
  public <T> void registerIntentInterceptor(final String type, final IntentInterceptor<T> interceptor, final Type payloadClazz) {
    registerIntentInterceptor(type, null, interceptor, payloadClazz);
  }

  @Override
  public <T> void registerIntentInterceptor(final String type, final Qualifier qualifier, final IntentInterceptor<T> interceptor) {
    registerIntentInterceptor(type, qualifier, interceptor, Object.class);
  }

  @Override
  public <T> void registerIntentInterceptor(final String type, final Qualifier qualifier, final IntentInterceptor<T> interceptor,
      final Type payloadClazz) {
    Objects.requireNonNull(type);
    Objects.requireNonNull(interceptor);
    Objects.requireNonNull(payloadClazz);
    microfrontendPlatformRcpHost.registerIntentInterceptor(type, qualifier, interceptor, payloadClazz);
  }
}
