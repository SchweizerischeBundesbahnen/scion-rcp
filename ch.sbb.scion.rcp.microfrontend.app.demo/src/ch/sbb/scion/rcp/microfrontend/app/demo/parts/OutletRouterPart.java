package ch.sbb.scion.rcp.microfrontend.app.demo.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.widgets.ButtonFactory;
import org.eclipse.jface.widgets.CompositeFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.jface.widgets.TextFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ch.sbb.scion.rcp.microfrontend.OutletRouter;
import ch.sbb.scion.rcp.microfrontend.OutletRouter.NavigationOptions;

public class OutletRouterPart {

  @Inject
  private OutletRouter outletRouter;

  @PostConstruct
  public void createComposite(final Composite parent) {
    var group = CompositeFactory.newComposite(SWT.NONE).create(parent);
    GridLayoutFactory.swtDefaults().numColumns(2).margins(5, 10).spacing(20, 7).applyTo(group);

    // Outlet
    LabelFactory.newLabel(SWT.NONE).text("Outlet")
        .layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).align(SWT.BEGINNING, SWT.CENTER).create()).create(group);
    var outlet = TextFactory.newText(SWT.SINGLE | SWT.BORDER).layoutData(GridDataFactory.fillDefaults().grab(true, false).create())
        .create(group);

    // URL
    LabelFactory.newLabel(SWT.NONE).text("URL")
        .layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).align(SWT.BEGINNING, SWT.CENTER).create()).create(group);
    var url = TextFactory.newText(SWT.SINGLE | SWT.BORDER).layoutData(GridDataFactory.fillDefaults().grab(true, false).create())
        .create(group);

    // Push state to session history stack
    LabelFactory.newLabel(SWT.NONE).layoutData(GridDataFactory.fillDefaults().create()).create(group);
    var pushStateToSessionHistoryStack = ButtonFactory.newButton(SWT.CHECK).text("Push state to session history stack")
        .layoutData(GridDataFactory.fillDefaults().create()).create(group);

    // Navigate button
    ButtonFactory.newButton(SWT.NONE).text("Navigate")
        .onSelect(e -> outletRouter.navigate(url.getText(),
            NavigationOptions.builder().outlet(outlet.getText())
                .pushStateToSessionHistoryStack(Boolean.valueOf(pushStateToSessionHistoryStack.getSelection())).build()))
        .layoutData(GridDataFactory.fillDefaults().span(2, 1).create()).create(group);
  }
}