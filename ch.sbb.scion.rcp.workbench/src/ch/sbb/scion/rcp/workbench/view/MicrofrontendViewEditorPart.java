package ch.sbb.scion.rcp.workbench.view;

import static ch.sbb.scion.rcp.microfrontend.util.CompletableFutures.logOnException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import ch.sbb.scion.rcp.microfrontend.IDisposable;
import ch.sbb.scion.rcp.microfrontend.ManifestService;
import ch.sbb.scion.rcp.microfrontend.MessageClient;
import ch.sbb.scion.rcp.microfrontend.OutletRouter;
import ch.sbb.scion.rcp.microfrontend.RouterOutlet;
import ch.sbb.scion.rcp.microfrontend.RouterOutlet.FocusWithinListener;
import ch.sbb.scion.rcp.microfrontend.model.Application;
import ch.sbb.scion.rcp.microfrontend.model.MessageHeaders;
import ch.sbb.scion.rcp.microfrontend.model.NavigationOptions;
import ch.sbb.scion.rcp.microfrontend.model.PublishOptions;
import ch.sbb.scion.rcp.microfrontend.model.ResponseStatusCodes;
import ch.sbb.scion.rcp.microfrontend.subscriber.ISubscription;
import ch.sbb.scion.rcp.microfrontend.util.CompletableFutures;
import ch.sbb.scion.rcp.workbench.IMicrofrontendViewPart;
import ch.sbb.scion.rcp.workbench.internal.ContextInjectors;
import ch.sbb.scion.rcp.workbench.internal.SelectionProvider;
import ch.sbb.scion.rcp.workbench.internal.WorkbenchCommands;

/**
 * Embeds the microfrontend of a view capability. See `MicrofrontendViewComponent` in SCION Workbench.
 */
public class MicrofrontendViewEditorPart extends EditorPart implements IReusableEditor, IPartListener2, IMicrofrontendViewPart {

  private static final String CONTEXT_WORKBENCH_VIEW_ID = "ɵworkbench.view.id";

  // TODO: Remove and use ID defined on interface?
  public static final String ID = IMicrofrontendViewPart.ID;

  @Inject
  private OutletRouter outletRouter;

  @Inject
  private ManifestService manifestService;

  @Inject
  private MessageClient messageClient;

  private RouterOutlet sciRouterOutlet;
  private final CompletableFuture<RouterOutlet> whenSciRouterOutlet = new CompletableFuture<>();
  private CompletableFuture<Map<String, Application>> whenApplications;
  private boolean dirty;

  private final Set<ISubscription> subscriptions = new HashSet<>();

  public MicrofrontendViewEditorPart() {
    ContextInjectors.inject(this);
  }

  @Override
  public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
    whenApplications = manifestService.getApplications().thenApply(applications -> {
      return applications.stream().collect(Collectors.toMap(Application::symbolicName, Function.identity()));
    });
    setSite(site);

    // Install view message listeners before navigation, which happens in setInput:
    var viewId = ((MicrofrontendViewEditorInput) input).sciViewId;
    installViewTitleUpdater(viewId);
    installViewHeadingUpdater(viewId);
    installViewDirtyUpdater(viewId);
    installParamsUpdater(viewId);

    // Set input:
    setInput(input);

    // Set selection provider during initialization, otherwise we are late for accepting the selection changed listener of the selection service.
    getSite().setSelectionProvider(new SelectionProvider());
    getSite().getPage().addPartListener(this);
  }

  private void installViewTitleUpdater(final String viewId) {
    // todo (later): run if privileged
    subscriptions.add(messageClient.subscribe(WorkbenchCommands.viewTitleTopic(viewId), message -> setPartName(message.body())));
  }

  private void installViewHeadingUpdater(final String viewId) {
    subscriptions.add(messageClient.subscribe(WorkbenchCommands.viewHeadingTopic(viewId), message -> setTitleToolTip(message.body())));
  }

  private void installViewDirtyUpdater(final String viewId) {
    subscriptions.add(messageClient.subscribe(WorkbenchCommands.viewDirtyTopic(viewId), Boolean.class, message -> {
      dirty = message.body().booleanValue();
      firePropertyChange(IEditorPart.PROP_DIRTY);
    }));
  }

  private void installParamsUpdater(final String viewId) {
    subscriptions.add(messageClient.subscribe(WorkbenchCommands.viewParamsUpdateTopic(viewId, ":capabilityId"), Map.class, message -> {
      var replyTo = (String) message.headers().get(MessageHeaders.REPLY_TO.value);
      // todo (later): Is this still true? The latest scion-workbench implementation still supports self-navigation, without any deprecation notice...
      var error = "Self navigation is not supported by the SCION RCP Workbench. This feature is expected to be removed from the SCION Workbench.";
      messageClient
          .publish(replyTo, error,
              new PublishOptions(Map.of(MessageHeaders.STATUS.value, Integer.valueOf(ResponseStatusCodes.ERROR.value))))
          .whenComplete(logOnException(MicrofrontendViewEditorPart.class));
    }));
  }

  @Override
  public void setInput(final IEditorInput input) {
    var prevCapability = getEditorInput() != null ? getEditorInput().capability : null;
    super.setInput(input);

    var intent = getEditorInput().intent;
    var capability = getEditorInput().capability;

    // Signal that the currently loaded microfrontend, if any, is about to be replaced by a microfrontend of another application.
    if (prevCapability != null && !prevCapability.metadata().appSymbolicName().equals(capability.metadata().appSymbolicName())) {
      CompletableFutures.await(messageClient.publish(WorkbenchCommands.viewUnloadingTopic(getViewId())));
    }
    // then unload
    this.unload();

    // todo: do properly
    messageClient.publish(WorkbenchCommands.viewPartIdTopic(getViewId()), "part.unknown", new PublishOptions(true))
        .whenComplete(logOnException(MicrofrontendViewEditorPart.class));

    // Check if navigating to a new microfrontend.
    if (prevCapability == null || !prevCapability.metadata().id().equals(capability.metadata().id())) {
      setPartName(capability.properties().get("title"));
      // TODO [ISW] Tooltip not displayed on the Eclipse tab
      setTitleToolTip(capability.properties().get("heading"));
    }

    // Provide params and qualifier to the microfrontend.
    var params = new HashMap<String, Object>();
    params.putAll(intent.params());
    // todo (later): Why do we add the qualifier values to the params? I could not find similar logic in the scion-workbench implementation
    params.putAll(intent.qualifier().entries());
    params.put("ɵViewCapabilityId", capability.metadata().id());
    messageClient.publish(computeViewParamsTopic(), params, new PublishOptions(true))
        .whenComplete(logOnException(MicrofrontendViewEditorPart.class));

    // When navigating to another view capability of the same app, wait until transported the params to consumers before loading the
    // new microfrontend into the iframe, allowing the currently loaded microfrontend to cleanup subscriptions. Params include the
    // capability id.
    if (prevCapability != null && prevCapability.metadata().appSymbolicName().equals(capability.metadata().appSymbolicName())
        && !prevCapability.metadata().id().equals(capability.metadata().id())) {
      waitForCapabilityParams(capability.metadata().id());
    }

    // Load the microfrontend
    var applications = CompletableFutures.await(this.whenApplications);
    var appSymbolicName = capability.metadata().appSymbolicName();
    var path = (String) capability.properties().get("path");
    var relativeTo = applications.get(appSymbolicName).baseUrl();
    // If a view was already loaded into this outlet, previously, then we can directly navigate.
    if (prevCapability != null) {
      navigate(path, relativeTo, params);
      return;
    }

    // Otherwise wait for the outlet to be created, and the context to become available. When the microfrontend gets activated,
    // the context is expected to be available, otherwise certain, context-dependent services won't get provided.
    CompletableFuture<Void> whenContextAvailable = new CompletableFuture<>();
    AtomicReference<ISubscription> contextValuesSubscriptionRef = new AtomicReference<>();
    whenSciRouterOutlet.thenCompose(s -> {
      contextValuesSubscriptionRef.set(s.subscribeToContextValues(c -> {
        if (c.containsKey(CONTEXT_WORKBENCH_VIEW_ID)) {
          whenContextAvailable.complete(null);
        }
      }));
      return s.setContextValue(CONTEXT_WORKBENCH_VIEW_ID, getViewId());
    }).whenComplete(CompletableFutures.logOnException(MicrofrontendViewEditorPart.class));
    whenContextAvailable.thenRun(() -> {
      contextValuesSubscriptionRef.get().unsubscribe();
      navigate(path, relativeTo, params);
    }).whenComplete(CompletableFutures.logOnException(MicrofrontendViewEditorPart.class));
  }

  private void navigate(final String path, final String relativeTo, final Map<String, ?> params) {
    outletRouter.navigate(path, NavigationOptions.builder().outlet(getViewId()).relativeTo(relativeTo).params(params)
        .pushStateToSessionHistoryStack(Boolean.FALSE).build()).whenComplete(logOnException(MicrofrontendViewEditorPart.class));
  }

  @Override
  public void createPartControl(final Composite parent) {
    sciRouterOutlet = new RouterOutlet(parent, SWT.NONE, getViewId());
    whenSciRouterOutlet.complete(sciRouterOutlet);
  }

  @Override
  public void doSave(final IProgressMonitor monitor) {
  }

  @Override
  public void doSaveAs() {
  }

  @Override
  public boolean isDirty() {
    return dirty;
  }

  @Override
  public boolean isSaveAsAllowed() {
    return false;
  }

  @Override
  public void setFocus() {
    sciRouterOutlet.setFocus();
  }

  @Override
  public void partVisible(final IWorkbenchPartReference partRef) {
    if (partRef.getPart(false) == this) {
      messageClient.publish(computeViewActiveTopic(), Boolean.TRUE, new PublishOptions(true))
          .whenComplete(logOnException(MicrofrontendViewEditorPart.class));
    }
  }

  @Override
  public void partHidden(final IWorkbenchPartReference partRef) {
    if (partRef.getPart(false) == this) {
      messageClient.publish(computeViewActiveTopic(), Boolean.FALSE, new PublishOptions(true))
          .whenComplete(logOnException(MicrofrontendViewEditorPart.class));
    }
  }

  @Override
  public MicrofrontendViewEditorInput getEditorInput() {
    return (MicrofrontendViewEditorInput) super.getEditorInput();
  }

  private String computeViewParamsTopic() {
    return WorkbenchCommands.viewParamsTopic(getViewId());
  }

  private String computeViewActiveTopic() {
    return WorkbenchCommands.viewActiveTopic(getViewId());
  }

  @Override
  public String getViewId() {
    // Note: The editor input is only available after it was set via setInput!
    return getEditorInput().sciViewId;
  }

  private void waitForCapabilityParams(final String capabilityId) {
    var future = new CompletableFuture<Void>();
    var subscription = messageClient.subscribe(computeViewParamsTopic(), Map.class, message -> {
      if (capabilityId.equals(message.body().get("ɵViewCapabilityId"))) {
        future.complete(null);
      }
    });
    try {
      CompletableFutures.await(future);
    }
    finally {
      subscription.unsubscribe();
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    getSite().getPage().removePartListener(this);
    subscriptions.forEach(ISubscription::unsubscribe);
    unload();
  }

  private void unload() {
    // Delete retained messages
    messageClient.publish(computeViewParamsTopic(), null, new PublishOptions(true))
        .whenComplete(logOnException(MicrofrontendViewEditorPart.class));
    messageClient.publish(computeViewActiveTopic(), null, new PublishOptions(true))
        .whenComplete(logOnException(MicrofrontendViewEditorPart.class));
    messageClient.publish(WorkbenchCommands.viewPartIdTopic(getViewId()), null, new PublishOptions(true))
        .whenComplete(logOnException(MicrofrontendViewEditorPart.class));
    // Clear outlet if it exists.
    if (sciRouterOutlet != null) {
      outletRouter.navigate((String) null, NavigationOptions.builder().outlet(getViewId()).build())
          .whenComplete(logOnException(MicrofrontendViewEditorPart.class));
    }
  }

  @Override
  public IDisposable onFocusWithin(final FocusWithinListener listener) {
    return sciRouterOutlet.onFocusWithin(listener);
  }
}
