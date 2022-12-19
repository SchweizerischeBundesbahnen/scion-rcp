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

import ch.sbb.scion.rcp.microfrontend.SciManifestService;
import ch.sbb.scion.rcp.microfrontend.SciMessageClient;
import ch.sbb.scion.rcp.microfrontend.SciOutletRouter;
import ch.sbb.scion.rcp.microfrontend.SciOutletRouter.NavigationOptions;
import ch.sbb.scion.rcp.microfrontend.SciRouterOutlet;
import ch.sbb.scion.rcp.microfrontend.model.Application;
import ch.sbb.scion.rcp.microfrontend.model.ISubscription;
import ch.sbb.scion.rcp.microfrontend.model.MessageHeaders;
import ch.sbb.scion.rcp.microfrontend.model.PublishOptions;
import ch.sbb.scion.rcp.microfrontend.model.ResponseStatusCodes;
import ch.sbb.scion.rcp.workbench.internal.ContextInjectors;

/**
 * Embeds the microfrontend of a view capability.
 * 
 * See `MicrofrontendViewComponent` in SCION Workbench.
 */
public class MicrofrontendViewEditorPart extends EditorPart implements IReusableEditor, IPartListener2 {

  public static final String ID = "ch.sbb.scion.rcp.workbench.editors.MicrofrontendViewEditor";

  @Inject
  private SciOutletRouter outletRouter;

  @Inject
  private SciManifestService manifestService;

  @Inject
  private SciMessageClient messageClient;

  private SciRouterOutlet sciRouterOutlet;
  private CompletableFuture<Map<String, Application>> applications;
  private boolean dirty;

  private final Set<ISubscription> subscriptions = new HashSet<>();

  public MicrofrontendViewEditorPart() {
    ContextInjectors.inject(this);
  }

  @Override
  public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
    applications = manifestService.getApplications().thenApply(applications -> {
      return applications.stream().collect(Collectors.toMap(a -> a.symbolicName, Function.identity()));
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
    if (prevCapability == null || !prevCapability.metadata.id.equals(capability.metadata.id)) {
      setPartName(capability.properties.get("title"));
      // TODO [ISW] Tooltip not displayed on the Eclipse tab
      setTitleToolTip(capability.properties.get("heading"));
    }

    // Provide params and qualifier to the microfrontend.
    var params = new HashMap<String, Object>();
    params.putAll(intent.params);
    params.putAll(intent.qualifier.entries);
    params.put("ɵViewCapabilityId", capability.metadata.id);
    messageClient.publish(computeViewParamsTopic(), params, new PublishOptions().retain(true));

    // When navigating to another view capability of the same app, wait until transported the params to consumers before loading the
    // new microfrontend into the iframe, allowing the currently loaded microfrontend to cleanup subscriptions. Params include the
    // capability id.
    if (prevCapability != null
        && prevCapability.metadata.appSymbolicName.equals(capability.metadata.appSymbolicName)
        && !prevCapability.metadata.id.equals(capability.metadata.id)) {
      waitForCapabilityParam(capability.metadata.id);
    }

    // Signal that the currently loaded microfrontend, if any, is about to be replaced by a microfrontend of another application.
    if (prevCapability != null && !prevCapability.metadata.appSymbolicName.equals(capability.metadata.appSymbolicName)) {
      var topic = String.format("ɵworkbench/views/%s/unloading", getSciViewId());
      CompletableFutures.await(messageClient.publish(topic));
    }

    // Load the microfrontend
    var applications = CompletableFutures.await(this.applications);
    var appSymbolicName = capability.metadata.appSymbolicName;
    var path = (String) capability.properties.get("path");
    outletRouter.navigate(path, new NavigationOptions()
        .outlet(getSciViewId())
        .relativeTo(applications.get(appSymbolicName).baseUrl)
        .params(params)
        .pushStateToSessionHistoryStack(false));
  }

  @Override
  public void createPartControl(final Composite parent) {
    sciRouterOutlet = new SciRouterOutlet(parent, SWT.NONE, getSciViewId());
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
  public void partVisible(IWorkbenchPartReference partRef) {
    if (partRef.getPart(false) == this) {
      messageClient.publish(computeViewActiveTopic(), true, new PublishOptions().retain(true));
    }
  }

  @Override
  public void partHidden(IWorkbenchPartReference partRef) {
    if (partRef.getPart(false) == this) {
      messageClient.publish(computeViewActiveTopic(), false, new PublishOptions().retain(true));
    }
  }

  @Override
  public MicrofrontendViewEditorInput getEditorInput() {
    return (MicrofrontendViewEditorInput) super.getEditorInput();
  }

  private void installViewTitleUpdater() {
    var topic = String.format("ɵworkbench/views/%s/title", getSciViewId());
    subscriptions.add(messageClient.subscribe(topic, message -> setPartName(message.body)));
  }

  private void installViewHeadingUpdater() {
    var topic = String.format("ɵworkbench/views/%s/heading", getSciViewId());
    subscriptions.add(messageClient.subscribe(topic, message -> setTitleToolTip(message.body)));
  }

  private void installViewDirtyUpdater() {
    var topic = String.format("ɵworkbench/views/%s/dirty", getSciViewId());
    subscriptions.add(messageClient.subscribe(topic, message -> {
      dirty = message.body;
      firePropertyChange(IEditorPart.PROP_DIRTY);
    }, Boolean.class));
  }

  private void installParamsUpdater() {
    var topic = String.format("ɵworkbench/views/%s/capabilities/:capabilityId/params/update", getSciViewId());

    subscriptions.add(messageClient.subscribe(topic, message -> {
      var replyTo = (String) message.headers.get(MessageHeaders.ReplyTo.value);
      var error = "Self navigation is not supported by the SCION RCP Workbench. This feature is expected to be removed from the SCION Workbench.";
      messageClient.publish(replyTo, error, new PublishOptions().headers(Map.of(MessageHeaders.Status.value, ResponseStatusCodes.ERROR.value)));
    }, Map.class));
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

  private void waitForCapabilityParam(String capabilityId) {
    var future = new CompletableFuture<Void>();
    var subscription = messageClient.subscribe(computeViewParamsTopic(), message -> {
      if (capabilityId.equals(message.body.get("ɵViewCapabilityId"))) {
        future.complete(null);
      }
    }, Map.class);
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
    messageClient.publish(computeViewParamsTopic(), null, new PublishOptions().retain(true));
    messageClient.publish(computeViewActiveTopic(), null, new PublishOptions().retain(true));
  }
}
