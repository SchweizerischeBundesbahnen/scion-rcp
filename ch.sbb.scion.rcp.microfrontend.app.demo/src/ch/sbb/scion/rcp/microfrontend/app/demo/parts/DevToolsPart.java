package ch.sbb.scion.rcp.microfrontend.app.demo.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ch.sbb.scion.rcp.microfrontend.SciOutletRouter;
import ch.sbb.scion.rcp.microfrontend.SciOutletRouter.NavigationOptions;
import ch.sbb.scion.rcp.microfrontend.SciRouterOutlet;
import ch.sbb.scion.rcp.microfrontend.model.Qualifier;

public class DevToolsPart {

  private static final String PART_ID = "ch.sbb.scion.rcp.microfrontend.app.demo.part.devtools";

  @Inject
  private SciOutletRouter outletRouter;

  @PostConstruct
  public void createComposite(Composite parent) {
    new SciRouterOutlet(parent, SWT.NONE, PART_ID);
    var navigation = outletRouter.navigate(new Qualifier().set("component", "devtools").set("vendor", "scion"),
        new NavigationOptions().outlet(PART_ID));

    navigation.exceptionally(e -> {
      Platform.getLog(DevToolsPart.class).error("Failed to navigate to SCION DevTools", e);
      return null;
    });
  }
}