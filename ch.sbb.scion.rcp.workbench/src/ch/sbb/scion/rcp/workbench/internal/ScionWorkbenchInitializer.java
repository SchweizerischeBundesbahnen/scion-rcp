package ch.sbb.scion.rcp.workbench.internal;

import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.sbb.scion.rcp.microfrontend.MicrofrontendPlatform;
import ch.sbb.scion.rcp.microfrontend.SciMessageClient;
import ch.sbb.scion.rcp.microfrontend.interceptor.InterceptorChain;
import ch.sbb.scion.rcp.microfrontend.model.IntentMessage;
import ch.sbb.scion.rcp.workbench.popup.PopupCommand;
import ch.sbb.scion.rcp.workbench.popup.PopupIntentInterceptor;
import ch.sbb.scion.rcp.workbench.view.ViewIntentInterceptor;

@Component(service = ScionWorkbenchInitializer.class, immediate = true)
public class ScionWorkbenchInitializer {

  @Reference
  private MicrofrontendPlatform microfrontendPlatform;

  @Reference
  private SciMessageClient messageClient;

  @Activate
  private void activate() {
    installViewIntentInterceptor();
    installPopupIntentInterceptor();
  }

  /**
   * Installs an interceptor to handle view intents, i.e., to open Eclipse Workbench views.
   */
  private void installViewIntentInterceptor() {
    microfrontendPlatform.registerIntentInterceptor("view",
        (final IntentMessage<Map<String, ?>> intentMessage, final InterceptorChain chain) -> {
          try {
            final var handled = PlatformUI.getWorkbench().getService(ViewIntentInterceptor.class).handle(intentMessage);
            if (handled) {
              chain.doSwallow();
            }
            else {
              chain.doContinue(intentMessage);
            }
          }
          catch (final PartInitException e) {
            chain.doReject(e.getMessage());
            Platform.getLog(ScionWorkbenchInitializer.class).error("Failed to intercept view intent", e);
          }
        });
  }

  /**
   * Installs an interceptor to handle popup intents, i.e., to open Eclipse Workbench dialogs.
   */
  private void installPopupIntentInterceptor() {
    microfrontendPlatform.registerIntentInterceptor("popup",
        (final IntentMessage<PopupCommand> intentMessage, final InterceptorChain chain) -> {
          try {
            final var handled = PlatformUI.getWorkbench().getService(PopupIntentInterceptor.class).handle(intentMessage);
            if (handled) {
              chain.doSwallow();
            }
            else {
              chain.doContinue(intentMessage);
            }
          }
          catch (final Exception e) {
            chain.doReject(e.getMessage());
            Platform.getLog(ScionWorkbenchInitializer.class).error("Failed to intercept popup intent", e);
          }
        }, PopupCommand.class);
  }
}
