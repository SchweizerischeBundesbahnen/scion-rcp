package ch.sbb.scion.rcp.microfrontend.app.demo.parts;

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ch.sbb.scion.rcp.microfrontend.SciOutletRouter;
import ch.sbb.scion.rcp.microfrontend.SciRouterOutlet;

public class RouterOutletExample1Part {

  private static final String PART_ID = "ch.sbb.scion.rcp.microfrontend.app.demo.part.router-outlet-example-1";

  @Inject
  private SciOutletRouter outletRouter;

  @PostConstruct
  public void createComposite(Composite parent) {
    var routerOutlet = new SciRouterOutlet(parent, SWT.NONE, PART_ID);
    routerOutlet.registerKeystrokes(Set.of("keydown.m", "keydown.O", "keydown.escape", "keydown.control.alt.enter", "keydown.control.space"));
   
    outletRouter.navigate("http://localhost:4201", PART_ID);

    routerOutlet.addListener(SWT.KeyDown, (e) -> {
      var isCtrlPressed = ((e.stateMask & SWT.CTRL) != 0);
      var isAltPressed = ((e.stateMask & SWT.ALT) != 0);
      var isShiftPressed = ((e.stateMask & SWT.SHIFT) != 0);
      var isCommandPressed = ((e.stateMask & SWT.COMMAND) != 0);
      System.out.println(String.format("onKeyEvent [character=%s, char=%s, stateMask=%s, keyCode=%s, ctrl=%b, alt=%b, shift=%b, command=%b]", e.character, (int) e.character, e.stateMask, e.keyCode, isCtrlPressed, isAltPressed, isShiftPressed, isCommandPressed));
    });
  }
}