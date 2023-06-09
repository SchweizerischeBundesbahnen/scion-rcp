package ch.sbb.scion.rcp.microfrontend.app.demo.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ch.sbb.scion.rcp.microfrontend.OutletRouter;
import ch.sbb.scion.rcp.microfrontend.RouterOutlet;
import ch.sbb.scion.rcp.microfrontend.model.NavigationOptions;

public class RouterOutletExample2Part {

  private static final String PART_ID = "ch.sbb.scion.rcp.microfrontend.app.demo.part.router-outlet-example-2";

  @Inject
  private OutletRouter outletRouter;

  @PostConstruct
  public void createComposite(final Composite parent) {
    var routerOutlet = new RouterOutlet(parent, SWT.NONE, PART_ID);
    outletRouter.navigate("http://localhost:4201", NavigationOptions.builder().outlet(PART_ID).build());

    routerOutlet.setContextValue("viewId", PART_ID);
  }
}