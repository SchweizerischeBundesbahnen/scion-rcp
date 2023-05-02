package ch.sbb.scion.rcp.workbench.popup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.sbb.scion.rcp.microfrontend.MessageClient;
import ch.sbb.scion.rcp.microfrontend.model.Intent;
import ch.sbb.scion.rcp.microfrontend.model.IntentMessage;
import ch.sbb.scion.rcp.microfrontend.model.MessageHeaders;
import ch.sbb.scion.rcp.microfrontend.model.PublishOptions;
import ch.sbb.scion.rcp.microfrontend.model.ResponseStatusCodes;
import ch.sbb.scion.rcp.workbench.IWorkbenchPopupWindow;
import ch.sbb.scion.rcp.workbench.IWorkbenchPopupWindowProvider;

/**
 * Handles SCION Workbench popup intents, instructing the Eclipse Workbench to open an Eclipse dialog that displays the associated
 * microfrontend.
 */
@Component(service = PopupIntentInterceptor.class)
public class PopupIntentInterceptor {

  private static final String PATH = "path";
  private static final String ECLIPSE_POPUP_ID = "eclipsePopupId";

  private final Set<String> openPopupDialogs = new HashSet<>();

  @Reference
  private IWorkbench workbench;

  @Reference
  private MessageClient messageClient;

  @Reference
  private PopupRegistry registry;

  public boolean handle(final IntentMessage<PopupCommand> intentMessage) {
    var popupId = intentMessage.body().popupId;
    if (openPopupDialogs.contains(popupId)) {
      return true;
    }

    var properties = intentMessage.capability().properties();
    if (properties.has(PATH)) {
      consumePopupIntent(intentMessage, popup -> new MicrofrontendPopupDialog(getActiveShell(), popup));
      return true;
    }
    if (properties.has(ECLIPSE_POPUP_ID)) {
      consumePopupIntent(intentMessage, popup -> getEclipseDialog(getActiveShell(), popup, properties.get(ECLIPSE_POPUP_ID)));
      return true;
    }
    return false;
  }

  private Shell getActiveShell() {
    var window = workbench.getActiveWorkbenchWindow();
    if (window == null) {
      window = workbench.getWorkbenchWindows()[0];
    }
    return window.getShell();
  }

  private IWorkbenchPopupWindow getEclipseDialog(final Shell parentShell, final Popup popup, final String eclipsePopupId) {
    var provider = getPopupDialogProvider(eclipsePopupId);
    return provider.create(parentShell, popup);
  }

  private IWorkbenchPopupWindowProvider getPopupDialogProvider(final String eclipsePopupId) {
    var provider = registry.get(eclipsePopupId);
    if (provider == null) {
      throw new IllegalStateException(String.format("No ISciWorkbenchPopupWindowProvider registered by id=%s", eclipsePopupId));
    }
    return provider;
  }

  private void consumePopupIntent(final IntentMessage<PopupCommand> intentMessage,
      final Function<Popup, IWorkbenchPopupWindow> popupDialogSupplier) {
    // Prepare popup attributes:
    var popupId = intentMessage.body().popupId;
    var closeStrategy = resolveCloseStrategy(intentMessage.body().closeStrategy);
    var referrer = intentMessage.body().referrer;
    var params = getParams(intentMessage.intent());

    // Create popup dialog:
    var popup = Popup.builder().popupId(popupId).capability(intentMessage.capability()).params(params).closeStrategy(closeStrategy)
        .referrer(referrer).build();
    var popupDialog = popupDialogSupplier.apply(popup);

    // Set up on close callback:
    var replyTo = (String) intentMessage.headers().get(MessageHeaders.REPLY_TO.value);
    popup.whenClose.whenComplete((result, ex) -> this.onClose(result, ex, popupId, popupDialog, replyTo));

    // Initialize and open popup dialog asynchronously to not block the intent handling:
    openPopupDialogs.add(popupId);
    Display.getCurrent().asyncExec(() -> openPopupDialog(popupDialog, popup));
  }

  private void onClose(final Object result, final Throwable ex, final String popupId, final IWorkbenchPopupWindow popupDialog,
      final String replyTo) {
    popupDialog.close();
    openPopupDialogs.remove(popupId);
    if (ex != null) {
      replyWithError(replyTo, ex.getMessage());
      return;
    }
    if (result instanceof PopupException) {
      replyWithError(replyTo, ((PopupException) result).getMessage());
      return;
    }
    reply(replyTo, result);
  }

  private void reply(final String replyTo, final Object result) {
    logOnException(messageClient.publish(replyTo, result,
        new PublishOptions(Map.of(MessageHeaders.STATUS.value, Integer.valueOf(ResponseStatusCodes.TERMINAL.value)))));
  }

  private void replyWithError(final String replyTo, final String errorMessage) {
    logOnException(messageClient.publish(replyTo, errorMessage,
        new PublishOptions(Map.of(MessageHeaders.STATUS.value, Integer.valueOf(ResponseStatusCodes.ERROR.value)))));
  }

  private void openPopupDialog(final IWorkbenchPopupWindow popupDialog, final Popup popup) {
    try {
      popupDialog.init();
      popupDialog.setBlockOnOpen(false);
      popupDialog.open();
      // Handle dialog cancellation; shell is only available after call to open:
      var shell = popupDialog.getShell();
      if (shell == null || shell.isDisposed()) {
        popup.whenClose.complete(null);
      }
      else {
        shell.addDisposeListener(e -> popup.whenClose.complete(null));
      }
    }
    catch (Exception e) {
      popup.whenClose.completeExceptionally(e);
    }
  }

  private static Map<String, Object> getParams(final Intent intent) {
    var params = new HashMap<String, Object>();
    if (intent.params() != null) {
      params.putAll(intent.params());
    }
    params.putAll(intent.qualifier().entries());
    return params;
  }

  private static PopupCloseStrategy resolveCloseStrategy(final PopupCloseStrategy closeStrategy) {
    if (closeStrategy == null) {
      return new PopupCloseStrategy().onEscape(Boolean.valueOf(PopupDialogDefaults.CLOSE_ON_ESCAPE))
          .onFocusLost(Boolean.valueOf(PopupDialogDefaults.CLOSE_ON_FOCUS_LOST));
    }
    var onEscape = Boolean.valueOf(PopupDialogDefaults.CLOSE_ON_ESCAPE);
    var onFocusLost = Boolean.valueOf(PopupDialogDefaults.CLOSE_ON_FOCUS_LOST);
    if (closeStrategy.onEscape != null) {
      onEscape = closeStrategy.onEscape;
    }
    if (closeStrategy.onFocusLost != null) {
      onFocusLost = closeStrategy.onFocusLost;
    }
    return new PopupCloseStrategy().onEscape(onEscape).onFocusLost(onFocusLost);
  }

  private static void logOnException(final CompletableFuture<Void> future) {
    future.whenComplete((none, ex) -> {
      if (ex != null) {
        Platform.getLog(PopupIntentInterceptor.class).error(ex.getMessage());
      }
    });
  }

}
