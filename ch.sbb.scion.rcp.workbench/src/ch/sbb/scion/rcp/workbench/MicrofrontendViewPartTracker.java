/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.workbench;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.sbb.scion.rcp.workbench.view.MicrofrontendViewEditorPart;

/**
 * TODO Klasse dokumentieren
 */
@Component
public class MicrofrontendViewPartTracker {

  @Reference
  private IWorkbench workbench;

  private ActiveMicrofrontendViewManager activeMicrofrontendViewManager;

  @Activate
  private void activate() {
    activeMicrofrontendViewManager = new ActiveMicrofrontendViewManager();
    activeMicrofrontendViewManager.install();
    workbench.addWindowListener(new IWindowListener() {

      @Override
      public void windowOpened(final IWorkbenchWindow window) {
        window.getPartService().addPartListener(new IPartListener() {

          @Override
          public void partOpened(final IWorkbenchPart part) {
            if (part instanceof MicrofrontendViewEditorPart mfPart) {
              activeMicrofrontendViewManager.registerFocusListener(mfPart);
            }
          }

          @Override
          public void partDeactivated(final IWorkbenchPart part) {
            //            Platform.getLog(getClass()).info("Deactivated part=%s".formatted(part));
            activeMicrofrontendViewManager.setActiveWorkbenchPart(part);
          }

          @Override
          public void partClosed(final IWorkbenchPart part) {
            if (part instanceof MicrofrontendViewEditorPart mfPart) {
              activeMicrofrontendViewManager.unregisterFocusListener(mfPart);
            }
          }

          @Override
          public void partBroughtToTop(final IWorkbenchPart part) {
            // TODO Auto-generated method stub

          }

          @Override
          public void partActivated(final IWorkbenchPart part) {
            //            Platform.getLog(getClass()).info("Activated part=%s".formatted(part));
            activeMicrofrontendViewManager.unsetActiveWorkbenchPart(part);
          }
        });
      }

      @Override
      public void windowDeactivated(final IWorkbenchWindow window) {

      }

      @Override
      public void windowClosed(final IWorkbenchWindow window) {

      }

      @Override
      public void windowActivated(final IWorkbenchWindow window) {

      }
    });
  }
}
