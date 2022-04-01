package ch.sbb.scion.rcp.microfrontend.app.demo.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ch.sbb.scion.rcp.microfrontend.SciOutletRouter;
import ch.sbb.scion.rcp.microfrontend.SciRouterOutlet;

public class DevToolsPart {

  private static final String PART_ID = "ch.sbb.scion.rcp.microfrontend.app.demo.part.devtools";
  
  @Inject
  private SciOutletRouter outletRouter;
  
  @PostConstruct
  public void createComposite(Composite parent) {
    new SciRouterOutlet(parent, SWT.NONE, PART_ID);
    outletRouter.navigate("https://scion-microfrontend-platform-devtools-v1-0-0-rc-3.vercel.app", PART_ID);    
  }
}