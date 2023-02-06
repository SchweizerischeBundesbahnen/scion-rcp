package ch.sbb.scion.rcp.workbench.view;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.sbb.scion.rcp.microfrontend.model.IntentMessage;
import ch.sbb.scion.rcp.workbench.IScionWorkbenchView;
import ch.sbb.scion.rcp.workbench.ParamsEditorInput;

/**
 * Handles SCION Workbench view intents, instructing the Eclipse Workbench to open an Eclipse editor that
 * displays the associated microfrontend.
 * 
 * The RCP host can also provide view capabilities. Optionally, the host can associate the capability with an
 * Eclipse view or Eclipse editor by setting the properties "eclipseViewId" or "eclipseEditorId". If not specified,
 * the intent can be handled via {@link IntentClient}.
 */
@Component(service = ViewIntentInterceptor.class)
public class ViewIntentInterceptor {

  @Reference
  private IWorkbench workbench;

  public boolean handle(IntentMessage<Map<String, ?>> intentMessage) throws PartInitException {
    var properties = intentMessage.capability.properties;

    // Handle view intent related to a microfrontend.
    if (properties.has("path")) {
      openMicrofrontendEditor(intentMessage);
      return true;
    }

    // Handle view intent related to an Eclipse view.
    if (properties.has("eclipseViewId")) {
      var view = getActivePage().showView((String) properties.get("eclipseViewId"));
      // TODO [ISW] For E4 views, we get a wrapper instead of the actual view.
      // Find another way to pass the intent to the view, e.g., via injection or similar.
      if (view instanceof IScionWorkbenchView) {
        ((IScionWorkbenchView) view).setInput(intentMessage.intent);
      }
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

  private void openMicrofrontendEditor(IntentMessage<Map<String, ?>> intentMessage) throws PartInitException {
    var activePage = getActivePage();
    var capability = intentMessage.capability;
    var intent = intentMessage.intent;
    var target = Optional.ofNullable(intentMessage.body).map(body -> (String) body.get("target")).orElse("default");

    switch (target) {
      case "blank": {
        activePage.openEditor(new MicrofrontendViewEditorInput(capability, intent), MicrofrontendViewEditorPart.ID, true,
            IWorkbenchPage.MATCH_NONE);
        return;
      }
      case "self": {
        var targetViewId = Optional.ofNullable(intentMessage.body).map(body -> (String) body.get("selfViewId")).orElse(null);
        var editorInput = new MicrofrontendViewEditorInput(targetViewId, capability, intent);
        var editor = (IReusableEditor) activePage.openEditor(editorInput, MicrofrontendViewEditorPart.ID, true,
            IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
        activePage.reuseEditor(editor, editorInput);
        return;
      }
      default:
        // Activate editor if already opened.
        for (var editorRef : findMicrofrontendViewEditors()) {
          var editorInput = (MicrofrontendViewEditorInput) editorRef.getEditorInput();
          if (editorInput.matches(capability, intent)) {
            var editor = (IReusableEditor) editorRef.getEditor(true);
            if (editor != null) {
              activePage.reuseEditor(editor, editorInput.copyWithIntent(intent));
              activePage.activate(editor);
              return;
            }
          }
        }
        // Open new editor.
        activePage.openEditor(new MicrofrontendViewEditorInput(capability, intent), MicrofrontendViewEditorPart.ID, true,
            IWorkbenchPage.MATCH_NONE);
    }
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

  private boolean hasMicrofrontendViewEditorInput(IEditorReference editor) {
    try {
      return editor.getEditorInput() instanceof MicrofrontendViewEditorInput;
    }
    catch (PartInitException e) {
      throw new RuntimeException(e);
    }
  }
}
