package ch.sbb.scion.rcp.workbench.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
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

import ch.sbb.scion.rcp.microfrontend.OutletRouter;
import ch.sbb.scion.rcp.microfrontend.ManifestService;
import ch.sbb.scion.rcp.microfrontend.MessageClient;
import ch.sbb.scion.rcp.microfrontend.RouterOutlet;
import ch.sbb.scion.rcp.microfrontend.model.Application;
import ch.sbb.scion.rcp.microfrontend.model.MessageHeaders;
import ch.sbb.scion.rcp.microfrontend.model.NavigationOptions;
import ch.sbb.scion.rcp.microfrontend.model.PublishOptions;
import ch.sbb.scion.rcp.microfrontend.model.ResponseStatusCodes;
import ch.sbb.scion.rcp.microfrontend.subscriber.ISubscription;
import ch.sbb.scion.rcp.workbench.internal.CompletableFutures;
import ch.sbb.scion.rcp.workbench.internal.ContextInjectors;

/**
 * Embeds the microfrontend of a view capability. See `MicrofrontendViewComponent` in SCION Workbench.
 */
public class MicrofrontendViewEditorPart extends EditorPart implements IReusableEditor, IPartListener2 {

  public static final String ID = "ch.sbb.scion.rcp.workbench.editors.MicrofrontendViewEditor";

  @Inject
  private OutletRouter outletRouter;

  @Inject
  private ManifestService manifestService;

  @Inject
  private MessageClient messageClient;

  private RouterOutlet sciRouterOutlet;
  private CompletableFuture<Map<String, Application>> applications;
  private boolean dirty;

  private final Set<ISubscription> subscriptions = new HashSet<>();

  public MicrofrontendViewEditorPart() {
    ContextInjectors.inject(this);
  }

  @Override
  public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
    applications = manifestService.getApplications().thenApply(applications -> {
      return applications.stream().collect(Collectors.toMap(Application::symbolicName, Function.identity()));
    });
    setSite(site);
    setInput(input);
    installViewTitleUpdater();
    installViewHeadingUpdater();
    installViewDirtyUpdater();
    installParamsUpdater();

    getSite().getPage().addPartListener(this);
  }

  @Override
  public void setInput(final IEditorInput input) {
    var prevCapability = getEditorInput() != null ? getEditorInput().capability : null;
    super.setInput(input);

    var intent = getEditorInput().intent;
    var capability = getEditorInput().capability;

    // Check if navigating to a new microfrontend.
    if (prevCapability == null || !prevCapability.metadata().id().equals(capability.metadata().id())) {
      setPartName(capability.properties().get("title"));
      // TODO [ISW] Tooltip not displayed on the Eclipse tab
      setTitleToolTip(capability.properties().get("heading"));
    }

    // Provide params and qualifier to the microfrontend.
    var params = new HashMap<String, Object>();
    params.putAll(intent.params());
    params.putAll(intent.qualifier().entries());
    params.put("ɵViewCapabilityId", capability.metadata().id());
    messageClient.publish(computeViewParamsTopic(), params, new PublishOptions(true));

    // When navigating to another view capability of the same app, wait until transported the params to consumers before loading the
    // new microfrontend into the iframe, allowing the currently loaded microfrontend to cleanup subscriptions. Params include the
    // capability id.
    if (prevCapability != null && prevCapability.metadata().appSymbolicName().equals(capability.metadata().appSymbolicName())
        && !prevCapability.metadata().id().equals(capability.metadata().id())) {
      waitForCapabilityParam(capability.metadata().id());
    }

    // Signal that the currently loaded microfrontend, if any, is about to be replaced by a microfrontend of another application.
    if (prevCapability != null && !prevCapability.metadata().appSymbolicName().equals(capability.metadata().appSymbolicName())) {
      var topic = String.format("ɵworkbench/views/%s/unloading", getSciViewId());
      CompletableFutures.await(messageClient.publish(topic));
    }

    // Load the microfrontend
    var applications = CompletableFutures.await(this.applications);
    var appSymbolicName = capability.metadata().appSymbolicName();
    var path = (String) capability.properties().get("path");
    outletRouter.navigate(path, NavigationOptions.builder().outlet(getSciViewId()).relativeTo(applications.get(appSymbolicName).baseUrl())
        .params(params).pushStateToSessionHistoryStack(Boolean.FALSE).build());
  }

  @Override
  public void createPartControl(final Composite parent) {
    sciRouterOutlet = new RouterOutlet(parent, SWT.NONE, getSciViewId());
    sciRouterOutlet.setContextValue("ɵworkbench.view.id", getSciViewId());
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
      messageClient.publish(computeViewActiveTopic(), Boolean.TRUE, new PublishOptions(true));
    }
  }

  @Override
  public void partHidden(final IWorkbenchPartReference partRef) {
    if (partRef.getPart(false) == this) {
      messageClient.publish(computeViewActiveTopic(), Boolean.FALSE, new PublishOptions(true));
    }
  }

  @Override
  public MicrofrontendViewEditorInput getEditorInput() {
    return (MicrofrontendViewEditorInput) super.getEditorInput();
  }

  private void installViewTitleUpdater() {
    var topic = String.format("ɵworkbench/views/%s/title", getSciViewId());
    subscriptions.add(messageClient.subscribe(topic, message -> setPartName(message.body())));
  }

  private void installViewHeadingUpdater() {
    var topic = String.format("ɵworkbench/views/%s/heading", getSciViewId());
    subscriptions.add(messageClient.subscribe(topic, message -> setTitleToolTip(message.body())));
  }

  private void installViewDirtyUpdater() {
    var topic = String.format("ɵworkbench/views/%s/dirty", getSciViewId());
    subscriptions.add(messageClient.subscribe(topic, Boolean.class, message -> {
      dirty = message.body().booleanValue();
      firePropertyChange(IEditorPart.PROP_DIRTY);
    }));
  }

  private void installParamsUpdater() {
    var topic = String.format("ɵworkbench/views/%s/capabilities/:capabilityId/params/update", getSciViewId());

    subscriptions.add(messageClient.subscribe(topic, Map.class, message -> {
      var replyTo = (String) message.headers().get(MessageHeaders.REPLY_TO.value);
      var error = "Self navigation is not supported by the SCION RCP Workbench. This feature is expected to be removed from the SCION Workbench.";
      messageClient.publish(replyTo, error,
          new PublishOptions(Map.of(MessageHeaders.STATUS.value, Integer.valueOf(ResponseStatusCodes.ERROR.value))));
    }));
  }

  private String computeViewParamsTopic() {
    return String.format("ɵworkbench/views/%s/params", getSciViewId());
  }

  private String computeViewActiveTopic() {
    return String.format("ɵworkbench/views/%s/active", getSciViewId());
  }

  public String getSciViewId() {
    return getEditorInput().sciViewId;
  }

  private void waitForCapabilityParam(final String capabilityId) {
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
    // Delete retained messages
    messageClient.publish(computeViewParamsTopic(), null, new PublishOptions(true));
    messageClient.publish(computeViewActiveTopic(), null, new PublishOptions(true));
  }
}
