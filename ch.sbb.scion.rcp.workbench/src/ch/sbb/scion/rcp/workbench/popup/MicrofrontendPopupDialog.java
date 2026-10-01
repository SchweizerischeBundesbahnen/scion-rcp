package ch.sbb.scion.rcp.workbench.popup;

import static ch.sbb.scion.rcp.microfrontend.util.CompletableFutures.logOnException;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.widgets.CompositeFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.sbb.scion.rcp.microfrontend.ManifestService;
import ch.sbb.scion.rcp.microfrontend.MessageClient;
import ch.sbb.scion.rcp.microfrontend.OutletRouter;
import ch.sbb.scion.rcp.microfrontend.RouterOutlet;
import ch.sbb.scion.rcp.microfrontend.model.NavigationOptions;
import ch.sbb.scion.rcp.microfrontend.model.PublishOptions;
import ch.sbb.scion.rcp.microfrontend.subscriber.ISubscription;
import ch.sbb.scion.rcp.microfrontend.util.CompletableFutures;
import ch.sbb.scion.rcp.workbench.IWorkbenchPopupWindow;
import ch.sbb.scion.rcp.workbench.WorkbenchPopupOrigin;
import ch.sbb.scion.rcp.workbench.internal.ContextInjectors;
import ch.sbb.scion.rcp.workbench.internal.WorkbenchCommands;

public class MicrofrontendPopupDialog extends Dialog implements IWorkbenchPopupWindow {

  private static final String CLOSE_WITH_ERROR = "ɵWORKBENCH-POPUP:CLOSE_WITH_ERROR";

  @Inject
  private OutletRouter outletRouter;

  @Inject
  private ManifestService manifestService;

  @Inject
  private MessageClient messageClient;

  private final Popup popup;

  private final Set<ISubscription> subscriptions = new HashSet<>();

  private boolean activated = false;

  private final CompletableFuture<WorkbenchPopupOrigin> whenInitialOrigin = new CompletableFuture<>();

  public MicrofrontendPopupDialog(final Shell parentShell, final Popup popup) {
    super(parentShell);
    ContextInjectors.inject(this);

    this.popup = popup;
    configureShellStyle();
  }

  @Override
  public void init() {
    // Install workbench-client popup listeners, before navigation:
    subscriptions.add(installCloseListener());
    subscriptions.add(installOriginListener());
    subscriptions.add(installResultListener());

    // Navigate:
    var capability = popup.getCapability();
    var application = CompletableFutures.await(manifestService.getApplication(capability.metadata().appSymbolicName()));
    var path = (String) capability.properties().get("path");
    // todo: if showSplash is added to NavigationOptions, then set it based on capability property (how is this tied to signal-ready?)
    outletRouter.navigate(path, NavigationOptions.builder().outlet(getPopupId()).relativeTo(application.baseUrl()).params(popup.getParams())
        .pushStateToSessionHistoryStack(Boolean.FALSE).build()).whenComplete(logOnException(MicrofrontendPopupDialog.class));

  }

  private void configureShellStyle() {
    var style = getShellStyle();
    // Not resizable:
    style = style & ~SWT.RESIZE;
    // Not modal:
    style = style & ~SWT.APPLICATION_MODAL;
    setShellStyle(style);
  }

  private ISubscription installCloseListener() {
    return messageClient.subscribe(WorkbenchCommands.popupCloseTopic(getPopupId()), Object.class, closeMessage -> {
      if (closeMessage.headers().containsKey(CLOSE_WITH_ERROR) && ((Boolean) closeMessage.headers().get(CLOSE_WITH_ERROR)).booleanValue()) {
        popup.closeWithException(new PopupException((String) closeMessage.body()));
        return;
      }
      popup.close(closeMessage.body());
    });
  }

  private ISubscription installOriginListener() {
    return popup.observePopupOrigin(whenInitialOrigin::complete);
  }

  private ISubscription installResultListener() {
    return messageClient.subscribe(WorkbenchCommands.popupResultTopic(getPopupId()), Object.class,
        resultMessage -> popup.setResult(resultMessage.body()));
  }

  @Override
  protected Control createDialogArea(final Composite parent) {
    var area = (Composite) super.createDialogArea(parent);
    GridLayoutFactory.fillDefaults().spacing(new Point(0, 0)).applyTo(area);

    // Create invisible keystroke target:
    var keystrokeTarget = CompositeFactory.newComposite(SWT.NONE).layout(GridLayoutFactory.fillDefaults().create())
        .layoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).create()).create(area);

    // Create router outlet for hosting the microfrontend:
    var sciRouterOutlet = new RouterOutlet(area, SWT.NONE, getPopupId(), keystrokeTarget);
    var initialSize = popup.getInitialSize().orElse(new Point(SWT.DEFAULT, SWT.DEFAULT));
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).hint(initialSize).grab(true, true).applyTo(sciRouterOutlet);

    // Provide popup context:
    sciRouterOutlet.setContextValue(PopupContext.POPUP_CONTEXT, new PopupContext().popupId(popup.getPopupId())
        .capability(popup.getCapability()).params(popup.getParams()).referrer(popup.getReferrer()));

    // Forward escape keystroke from browser window to dialog which will close the dialog:
    if (popup.closeOnEscape()) {
      sciRouterOutlet.registerKeystroke("keydown.escape");
    }
    return area;
  }

  private String getPopupId() {
    return popup.getPopupId();
  }

  @Override
  protected void configureShell(final Shell newShell) {
    super.configureShell(newShell);
    var properties = popup.getCapability().properties();
    newShell.setText(properties.has("title") ? properties.get("title") : "");

    // Track active state:
    newShell.addListener(SWT.Deactivate, event -> this.onDeactivate());
    newShell.addListener(SWT.Activate, event -> this.onActivate());

    // Clean up on disposal:
    newShell.addDisposeListener(event -> this.dispose());
  }

  private void onDeactivate() {
    messageClient.publish(WorkbenchCommands.popupFocusedTopic(getPopupId()), Boolean.FALSE, new PublishOptions(true))
        .whenComplete(logOnException(MicrofrontendPopupDialog.class));
    if (activated && popup.closeOnFocusLost()) {
      popup.close(popup.getResult());
    }
  }

  private void onActivate() {
    activated = true;
    messageClient.publish(WorkbenchCommands.popupFocusedTopic(getPopupId()), Boolean.TRUE, new PublishOptions(true))
        .whenComplete(logOnException(MicrofrontendPopupDialog.class));
  }

  private void dispose() {
    // todo: should we attempt to close the popup on dispose?!
    // Clear outlet:
    outletRouter.navigate((String) null, NavigationOptions.builder().outlet(getPopupId()).build())
        .whenComplete(logOnException(MicrofrontendPopupDialog.class));
    subscriptions.forEach(ISubscription::unsubscribe);
    // Delete retained message:
    messageClient.publish(WorkbenchCommands.popupFocusedTopic(getPopupId()), new PublishOptions(true))
        .whenComplete(logOnException(MicrofrontendPopupDialog.class));
  }

  @Override
  protected Point getInitialLocation(final Point initialSize) {
    var initialOrigin = CompletableFutures.await(whenInitialOrigin);
    var parentShell = getParentShell();
    if (initialOrigin == null || parentShell == null) {
      return super.getInitialLocation(initialSize);
    }
    // If x and y are set, then the anchor is an Element. Currently, using an Element as anchor is not supported.
    // Note that in this case top and left are also set, hence we need to check x and y first:
    if (initialOrigin.x() != null && initialOrigin.y() != null) {
      return super.getInitialLocation(initialSize);
    }
    // Support TopLeftPoint as anchor:
    if (initialOrigin.top() != null && initialOrigin.left() != null) {
      return parentShell.toDisplay(initialOrigin.left().intValue(), initialOrigin.top().intValue());
    }
    // Any other anchors are not supported:
    return super.getInitialLocation(initialSize);
  }

  @Override
  protected void createButtonsForButtonBar(final Composite parent) {
    GridLayout layout = (GridLayout) parent.getLayout();
    layout.marginHeight = 0;
  }
}
