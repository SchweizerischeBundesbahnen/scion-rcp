package ch.sbb.scion.rcp.workbench.view;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.sbb.scion.rcp.microfrontend.model.Capability;
import ch.sbb.scion.rcp.microfrontend.model.Intent;
import ch.sbb.scion.rcp.microfrontend.model.IntentMessage;
import ch.sbb.scion.rcp.workbench.ISciWorkbenchViewInput;
import ch.sbb.scion.rcp.workbench.ParamsEditorInput;

/**
 * Handles SCION Workbench view intents, instructing the Eclipse Workbench to open an Eclipse editor that displays the associated
 * microfrontend. The RCP host can also provide view capabilities. Optionally, the host can associate the capability with an Eclipse view or
 * Eclipse editor by setting the properties "eclipseViewId" or "eclipseEditorId". If not specified, the intent can be handled via
 * <a href="https://scion-microfrontend-platform-api.vercel.app/classes/IntentClient.html">IntentClient</a>
 */
@Component(service = ViewIntentInterceptor.class)
public class ViewIntentInterceptor {

  @Reference
  private IWorkbench workbench;

  public boolean handle(final IntentMessage<Map<String, ?>> intentMessage) throws PartInitException {
    var properties = intentMessage.capability.properties;

    // Handle view intent related to a microfrontend.
    if (properties.has("path")) {
      openMicrofrontendEditor(intentMessage);
      return true;
    }

    // Handle view intent related to an Eclipse view.
    if (properties.has("eclipseViewId")) {
      var activePage = getActivePage();
      var context = activePage.getWorkbenchWindow().getService(IEclipseContext.class);
      context.set(ISciWorkbenchViewInput.class, getViewInput(intentMessage));
      activePage.showView((String) properties.get("eclipseViewId"));
      context.remove(ISciWorkbenchViewInput.class);
      return true;
    }

    // Handle view intent related to an Eclipse part.
    if (properties.has("eclipseEditorId")) {
      var editorId = (String) properties.get("eclipseEditorId");
      var editorInput = new ParamsEditorInput(intentMessage.intent.params);
      getActivePage().openEditor(editorInput, editorId, true, IWorkbenchPage.MATCH_ID);
      return true;
    }
    return false;
  }

  private ISciWorkbenchViewInput getViewInput(final IntentMessage<Map<String, ?>> intentMessage) {
    return new ViewPartInput().intent(intentMessage.intent);
  }

  private void openMicrofrontendEditor(final IntentMessage<Map<String, ?>> intentMessage) throws PartInitException {
    var activePage = getActivePage();
    var capability = intentMessage.capability;
    var intent = intentMessage.intent;
    var target = Optional.ofNullable(intentMessage.body).map(body -> (String) body.get("target")).orElse("auto");

    switch (target) {
    case "blank": {
      // Open new editor.
      activePage.openEditor(new MicrofrontendViewEditorInput(capability, intent), MicrofrontendViewEditorPart.ID, true,
          IWorkbenchPage.MATCH_NONE);
      return;
    }
    case "auto": {
      // Activate editor if already opened, otherwise open a new editor.
      var editorRef = findMicrofrontendViewEditor(capability, intent);
      var editor = editorRef != null ? (IReusableEditor) editorRef.getEditor(true) : null;
      if (editor == null || editorRef == null) {
        activePage.openEditor(new MicrofrontendViewEditorInput(capability, intent), MicrofrontendViewEditorPart.ID, true,
            IWorkbenchPage.MATCH_NONE);
      }
      else {
        var editorInput = (MicrofrontendViewEditorInput) editorRef.getEditorInput();
        activePage.reuseEditor(editor, editorInput.copyWithIntent(intent));
        activePage.activate(editor);
      }
      return;
    }
    default: {
      var editorInput = new MicrofrontendViewEditorInput(target, capability, intent);
      var editor = (IReusableEditor) activePage.openEditor(editorInput, MicrofrontendViewEditorPart.ID, true,
          IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
      activePage.reuseEditor(editor, editorInput);
    }
    }
  }

  private IEditorReference findMicrofrontendViewEditor(final Capability capability, final Intent intent) throws PartInitException {
    for (var editor : findMicrofrontendViewEditors()) {
      var editorInput = (MicrofrontendViewEditorInput) editor.getEditorInput();
      if (editorInput.matches(capability, intent)) {
        return editor;
      }
    }
    return null;
  }

  private Set<IEditorReference> findMicrofrontendViewEditors() {
    return Stream.of(workbench.getWorkbenchWindows()).flatMap(window -> Stream.of(window.getPages()))
        .flatMap(page -> Stream.of(page.getEditorReferences())).filter(this::hasMicrofrontendViewEditorInput).collect(Collectors.toSet());
  }

  private IWorkbenchPage getActivePage() {
    var window = workbench.getActiveWorkbenchWindow();
    if (window == null) {
      window = workbench.getWorkbenchWindows()[0];
    }

    var page = window.getActivePage();
    if (page == null) {
      page = window.getPages()[0];
    }
    return page;
  }

  private boolean hasMicrofrontendViewEditorInput(final IEditorReference editor) {
    try {
      return editor.getEditorInput() instanceof MicrofrontendViewEditorInput;
    }
    catch (PartInitException e) {
      throw new RuntimeException(e);
    }
  }
}
