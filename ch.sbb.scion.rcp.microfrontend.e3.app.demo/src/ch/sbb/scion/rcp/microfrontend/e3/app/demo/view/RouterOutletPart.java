package ch.sbb.scion.rcp.microfrontend.e3.app.demo.view;

import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.jface.widgets.TextFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ch.sbb.scion.rcp.microfrontend.SciRouterOutlet;

public class RouterOutletPart {

  private final String routerOutletName = UUID.randomUUID().toString();

  @PostConstruct
  public void createComposite(Composite parent) {
    var composite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.swtDefaults().numColumns(2).margins(5, 10).spacing(20, 7).applyTo(composite);

    // Name
    LabelFactory.newLabel(SWT.NONE).text("Name")
        .layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).align(SWT.BEGINNING, SWT.CENTER).create()).create(composite);
    TextFactory.newText(SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY).text(routerOutletName)
        .layoutData(GridDataFactory.fillDefaults().grab(true, false).create()).create(composite);

    // Router Outlet
    var routerOutlet = new SciRouterOutlet(composite, SWT.BORDER, routerOutletName);
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).span(2, 1).applyTo(routerOutlet);

    routerOutlet
        .registerKeystrokes(Set.of("keydown.m", "keydown.O", "keydown.escape", "keydown.control.alt.enter", "keydown.control.space"));
    routerOutlet.addListener(SWT.KeyDown, (e) -> {
      var isCtrlPressed = ((e.stateMask & SWT.CTRL) != 0);
      var isAltPressed = ((e.stateMask & SWT.ALT) != 0);
      var isShiftPressed = ((e.stateMask & SWT.SHIFT) != 0);
      var isCommandPressed = ((e.stateMask & SWT.COMMAND) != 0);
      System.out
          .println(String.format("onKeyEvent [character=%s, char=%s, stateMask=%s, keyCode=%s, ctrl=%b, alt=%b, shift=%b, command=%b]",
              e.character, (int) e.character, e.stateMask, e.keyCode, isCtrlPressed, isAltPressed, isShiftPressed, isCommandPressed));
    });
  }
}