package ch.sbb.scion.rcp.microfrontend.app.demo.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ch.sbb.scion.rcp.microfrontend.SciOutletRouter;
import ch.sbb.scion.rcp.microfrontend.SciOutletRouter.NavigationOptions;
import ch.sbb.scion.rcp.microfrontend.SciRouterOutlet;

public class RouterOutletExample2Part {

  private static final String PART_ID = "ch.sbb.scion.rcp.microfrontend.app.demo.part.router-outlet-example-2";

  @Inject
  private SciOutletRouter outletRouter;

  @PostConstruct
  public void createComposite(Composite parent) {
    var routerOutlet = new SciRouterOutlet(parent, SWT.NONE, PART_ID);
    outletRouter.navigate("http://localhost:4201", new NavigationOptions().outlet(PART_ID));

    routerOutlet.setContextValue("viewId", PART_ID);
  }
}