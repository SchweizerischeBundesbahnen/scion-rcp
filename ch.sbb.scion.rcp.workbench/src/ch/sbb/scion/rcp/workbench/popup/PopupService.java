package ch.sbb.scion.rcp.workbench.popup;

import java.util.concurrent.CompletableFuture;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.sbb.scion.rcp.microfrontend.SciIntentClient;
import ch.sbb.scion.rcp.microfrontend.SciMessageClient;
import ch.sbb.scion.rcp.microfrontend.model.ISubscriber;
import ch.sbb.scion.rcp.microfrontend.model.Intent;
import ch.sbb.scion.rcp.microfrontend.model.PublishOptions;
import ch.sbb.scion.rcp.microfrontend.model.Qualifier;
import ch.sbb.scion.rcp.microfrontend.model.TopicMessage;
import ch.sbb.scion.rcp.workbench.ISciWorkbenchPopupService;
import ch.sbb.scion.rcp.workbench.SciWorkbenchPopupConfig;

@Component
public class PopupService implements ISciWorkbenchPopupService {

  private static final String POPUP_TYPE = "popup";

  @Reference
  private SciIntentClient intentClient;

  @Reference
  private SciMessageClient messageClient;

  @Override
  public <T> CompletableFuture<T> open(final Qualifier qualifier, final SciWorkbenchPopupConfig config, final Class<T> resultClazz) {
    // Create command:
    var closeStrategy = getCloseStrategy(config);
    var context = getContext(config);
    var command = new PopupCommand().closeStrategy(closeStrategy).context(context);

    // Create intent:
    var intent = new Intent().type(POPUP_TYPE).qualifier(qualifier).params(config.params);

    // Publish initial anchor location; tracking the anchor location is not supported currently:
    messageClient.publish(computeOriginTopic(command), config.anchor, new PublishOptions().retain(true));

    // Request popup:
    var future = new CompletableFuture<T>();
    var subscription = intentClient.request(intent, command, resultClazz, new ISubscriber<TopicMessage<T>>() {

      @Override
      public void onNext(final TopicMessage<T> next) {
        future.complete(next.body);
      }

      @Override
      public void onError(final Exception e) {
        future.completeExceptionally(e);
      }

      @Override
      public void onComplete() {
        // Complete on complete, case: no returned message, e.g., due to cancellation of the popup.
        future.complete(null);
      }
    });

    return future.whenComplete((result, ex) -> {
      if (subscription != null) {
        subscription.unsubscribe();
      }
      messageClient.publish(computeOriginTopic(command), null, new PublishOptions().retain(true));
    });
  }

  private static String computeOriginTopic(final PopupCommand command) {
    return String.format("ɵworkbench/popups/%s/origin", command.popupId);
  }

  private static PopupReferrer getContext(final SciWorkbenchPopupConfig config) {
    return config.viewId == null ? null : new PopupReferrer().viewId(config.viewId);
  }

  private static PopupCloseStrategy getCloseStrategy(final SciWorkbenchPopupConfig config) {
    var empty = true;
    var closeStrategy = new PopupCloseStrategy();
    if (config.closeOnEscape != null) {
      closeStrategy.onEscape(config.closeOnEscape);
      empty = false;
    }
    if (config.closeOnFocusLost != null) {
      closeStrategy.onFocusLost(config.closeOnFocusLost);
      empty = false;
    }
    return empty ? null : closeStrategy;
  }

}
