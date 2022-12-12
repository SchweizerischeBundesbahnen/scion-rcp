package ch.sbb.scion.rcp.microfrontend.e3.app.demo.view;

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

  public static final String ID = "ch.sbb.scion.rcp.microfrontend.e3.app.demo.devtools";

  @Inject
  private SciOutletRouter outletRouter;

  @PostConstruct
  public void createComposite(Composite parent) {
    new SciRouterOutlet(parent, SWT.NONE, ID);
    var navigation = outletRouter.navigate(
        new Qualifier()
            .add("component", "devtools")
            .add("vendor", "scion"),
        new NavigationOptions().outlet(ID));

    navigation.exceptionally(e -> {
      Platform.getLog(DevToolsPart.class).error("Failed to navigate to SCION DevTools", e);
      return null;
    });
  }
}
