package ch.sbb.scion.rcp.microfrontend.app.demo.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ch.sbb.scion.rcp.microfrontend.OutletRouter;
import ch.sbb.scion.rcp.microfrontend.RouterOutlet;
import ch.sbb.scion.rcp.microfrontend.model.NavigationOptions;
import ch.sbb.scion.rcp.microfrontend.model.Qualifier;

public class DevToolsPart {

  private static final String PART_ID = "ch.sbb.scion.rcp.microfrontend.app.demo.part.devtools";

  @Inject
  private OutletRouter outletRouter;

  @PostConstruct
  public void createComposite(final Composite parent) {
    new RouterOutlet(parent, SWT.NONE, PART_ID);
    var navigation = outletRouter.navigate(new Qualifier().set("component", "devtools").set("vendor", "scion"),
        NavigationOptions.builder().outlet(PART_ID).build());

    navigation.exceptionally(e -> {
      Platform.getLog(DevToolsPart.class).error("Failed to navigate to SCION DevTools", e);
      return null;
    });
  }
}