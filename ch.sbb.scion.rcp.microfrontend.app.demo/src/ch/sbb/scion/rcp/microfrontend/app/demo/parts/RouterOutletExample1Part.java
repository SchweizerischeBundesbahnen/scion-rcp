package ch.sbb.scion.rcp.microfrontend.app.demo.parts;

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ch.sbb.scion.rcp.microfrontend.OutletRouter;
import ch.sbb.scion.rcp.microfrontend.OutletRouter.NavigationOptions;
import ch.sbb.scion.rcp.microfrontend.RouterOutlet;

public class RouterOutletExample1Part {

  private static final String PART_ID = "ch.sbb.scion.rcp.microfrontend.app.demo.part.router-outlet-example-1";

  @Inject
  private OutletRouter outletRouter;

  @PostConstruct
  public void createComposite(final Composite parent) {
    var routerOutlet = new RouterOutlet(parent, SWT.NONE, PART_ID);
    routerOutlet
        .registerKeystrokes(Set.of("keydown.m", "keydown.O", "keydown.escape", "keydown.control.alt.enter", "keydown.control.space"));

    outletRouter.navigate("http://localhost:4201", NavigationOptions.builder().outlet(PART_ID).build());

    routerOutlet.addListener(SWT.KeyDown, (e) -> {
      var isCtrlPressed = ((e.stateMask & SWT.CTRL) != 0);
      var isAltPressed = ((e.stateMask & SWT.ALT) != 0);
      var isShiftPressed = ((e.stateMask & SWT.SHIFT) != 0);
      var isCommandPressed = ((e.stateMask & SWT.COMMAND) != 0);
      System.out
          .println(String.format("onKeyEvent [character=%s, char=%s, stateMask=%s, keyCode=%s, ctrl=%b, alt=%b, shift=%b, command=%b]",
              Character.valueOf(e.character), Integer.valueOf(e.character), Integer.valueOf(e.stateMask), Integer.valueOf(e.keyCode),
              Boolean.valueOf(isCtrlPressed), Boolean.valueOf(isAltPressed), Boolean.valueOf(isShiftPressed),
              Boolean.valueOf(isCommandPressed)));
    });
  }
}