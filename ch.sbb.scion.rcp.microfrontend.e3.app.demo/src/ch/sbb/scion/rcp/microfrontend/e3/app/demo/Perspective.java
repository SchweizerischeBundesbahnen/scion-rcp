package ch.sbb.scion.rcp.microfrontend.e3.app.demo;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

  @Override
  public void createInitialLayout(IPageLayout layout) {
    layout.setEditorAreaVisible(true);
    layout.setFixed(true);
  }
}
