package ch.sbb.scion.rcp.microfrontend.e3.app.demo.view;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.widgets.ButtonFactory;
import org.eclipse.jface.widgets.CompositeFactory;
import org.eclipse.jface.widgets.GroupFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.jface.widgets.TextFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import ch.sbb.scion.rcp.microfrontend.e3.app.demo.view.model.WorkbenchPopupIntentMessageModel;
import ch.sbb.scion.rcp.microfrontend.model.Qualifier;
import ch.sbb.scion.rcp.workbench.ISciWorkbenchPopupService;
import ch.sbb.scion.rcp.workbench.SciWorkbenchPopupConfig;

public class WorkbenchPopupServicePart {

  /**
   * This is a key to let swtbot identify our components more easily.
   */
  private static final String ORG_ECLIPSE_SWTBOT_WIDGET_KEY = "org.eclipse.swtbot.widget.key";

  @Inject
  private ISciWorkbenchPopupService popupService;

  private final DataBindingContext ctx = new DataBindingContext();

  private Text validationLabel;

  private final IObservableValue<String> validationMessage = new WritableValue<>("", String.class);

  private final WorkbenchPopupIntentMessageModel intentModel = new WorkbenchPopupIntentMessageModel();

  @PostConstruct
  public void createComposite(final Composite parent) {
    var composite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.swtDefaults().applyTo(composite);

    var publishGroup = createPublishGroup(composite);
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(publishGroup);

    var validationArea = createValidationArea(composite);
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(validationArea);
  }

  private Composite createPublishGroup(final Composite parent) {
    var group = GroupFactory.newGroup(SWT.NONE).text("Open a workbench popup").create(parent);
    GridLayoutFactory.swtDefaults().numColumns(5).applyTo(group);

    // View ID
    LabelFactory.newLabel(SWT.NONE).text("View ID")
        .layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).align(SWT.BEGINNING, SWT.TOP).create()).create(group);
    LabelFactory.newLabel(SWT.NONE).text(intentModel.getViewId())
        .layoutData(GridDataFactory.fillDefaults().span(4, 1).align(SWT.BEGINNING, SWT.TOP).grab(true, false).create()).create(group);

    // Qualifier
    LabelFactory.newLabel(SWT.NONE).text("Qualifier")
        .layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).align(SWT.BEGINNING, SWT.TOP).create()).create(group);

    var qualifierComposite = createKeyValuePairViewerComposite(group, intentModel.getQualifiers(), "qualifier");
    GridDataFactory.swtDefaults().span(4, 1).hint(SWT.DEFAULT, 100).align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(qualifierComposite);

    // Params
    LabelFactory.newLabel(SWT.NONE).text("Params")
        .layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).align(SWT.BEGINNING, SWT.TOP).create()).create(group);

    var paramComposite = createKeyValuePairViewerComposite(group, intentModel.getParams(), "params");
    GridDataFactory.swtDefaults().span(4, 1).hint(SWT.DEFAULT, 100).align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(paramComposite);

    // Options
    LabelFactory.newLabel(SWT.NONE).text("Options")
        .layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).align(SWT.BEGINNING, SWT.TOP).create()).create(group);

    var optionsComposite = createOptionsComposite(group);
    GridDataFactory.swtDefaults().span(4, 1).align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(optionsComposite);

    // Anchor
    LabelFactory.newLabel(SWT.NONE).text("Anchor")
        .layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).align(SWT.BEGINNING, SWT.TOP).create()).create(group);

    var anchorComposite = createAnchorComposite(group);
    GridDataFactory.swtDefaults().span(4, 1).align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(anchorComposite);

    // Publish button
    var openButton = ButtonFactory.newButton(SWT.NONE).text("Open").layoutData(GridDataFactory.fillDefaults().span(5, 1).create())
        .create(group);
    openButton.setData(ORG_ECLIPSE_SWTBOT_WIDGET_KEY, "popup-open");

    openButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        requestPopup(intentModel.getQualifier(), intentModel.getConfig());
      }
    });

    return group;
  }

  private Composite createKeyValuePairViewerComposite(final Composite parent, final IObservableList<Entry<String, String>> entryModel,
      final String swtBotDataValuePostfix) {
    var composite = CompositeFactory.newComposite(SWT.NONE).create(parent);
    GridLayoutFactory.swtDefaults().numColumns(4).margins(0, 0).applyTo(composite);

    var nameText = TextFactory.newText(SWT.SINGLE | SWT.BORDER)
        .layoutData(GridDataFactory.fillDefaults().hint(100, 20).align(SWT.BEGINNING, SWT.TOP).create()).message("name").create(composite);

    var valueText = TextFactory.newText(SWT.SINGLE | SWT.BORDER).layoutData(GridDataFactory.fillDefaults().grab(true, false).create())
        .message("value").create(composite);

    var addEntryButton = ButtonFactory.newButton(SWT.NONE).text("+").layoutData(GridDataFactory.fillDefaults().hint(30, 20).create())
        .create(composite);
    addEntryButton.setData(ORG_ECLIPSE_SWTBOT_WIDGET_KEY, "add-" + swtBotDataValuePostfix);

    var removeEntryButton = ButtonFactory.newButton(SWT.NONE).text("-").enabled(false)
        .layoutData(GridDataFactory.fillDefaults().hint(30, 20).create()).create(composite);
    removeEntryButton.setData(ORG_ECLIPSE_SWTBOT_WIDGET_KEY, "remove-" + swtBotDataValuePostfix);

    var tableViewer = createTableViewer(composite);
    tableViewer.getTable().setData(ORG_ECLIPSE_SWTBOT_WIDGET_KEY, "table-" + swtBotDataValuePostfix);
    tableViewer.setInput(entryModel);
    tableViewer.addSelectionChangedListener(event -> removeEntryButton.setEnabled(!tableViewer.getSelection().isEmpty()));

    addEntryButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        entryModel.add(Map.entry(nameText.getText(), valueText.getText()));
        nameText.setText("");
        valueText.setText("");
      }
    });

    removeEntryButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        var selectedEntry = ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
        entryModel.remove(selectedEntry);
      }
    });

    IObservableValue<Boolean> fieldsSet = ComputedValue
        .create(() -> Boolean.valueOf(!WidgetProperties.text(SWT.Modify).observe(nameText).getValue().isEmpty()
            && !WidgetProperties.text(SWT.Modify).observe(valueText).getValue().isEmpty()));

    ctx.bindValue(WidgetProperties.enabled().observe(addEntryButton), fieldsSet);

    return composite;
  }

  private Composite createOptionsComposite(final Composite parent) {
    var composite = CompositeFactory.newComposite(SWT.NONE).create(parent);
    RowLayoutFactory.fillDefaults().applyTo(composite);

    // Close on escape flag
    var closeOnEscape = ButtonFactory.newButton(SWT.CHECK).text("Close on escape").layoutData(RowDataFactory.swtDefaults().create())
        .create(composite);

    // Close on focus lost flag
    var closeOnFocusLost = ButtonFactory.newButton(SWT.CHECK).text("Close on focus lost").layoutData(RowDataFactory.swtDefaults().create())
        .create(composite);

    // Bind
    ctx.bindValue(WidgetProperties.buttonSelection().observe(closeOnEscape), intentModel.isCloseOnEscape());
    ctx.bindValue(WidgetProperties.buttonSelection().observe(closeOnFocusLost), intentModel.isCloseOnFocusLost());
    return composite;
  }

  private Composite createAnchorComposite(final Composite parent) {
    var composite = CompositeFactory.newComposite(SWT.NONE).create(parent);
    RowLayoutFactory.fillDefaults().applyTo(composite);

    // Resizable flag
    var useTopLeftAnchor = ButtonFactory.newButton(SWT.CHECK).text("Use top left anchor").layoutData(RowDataFactory.swtDefaults().create())
        .create(composite);

    // Coordinates
    var anchorTop = TextFactory.newText(SWT.SINGLE | SWT.BORDER).layoutData(RowDataFactory.swtDefaults().hint(50, 20).create())
        .message("Top").create(composite);
    var anchorLeft = TextFactory.newText(SWT.SINGLE | SWT.BORDER).layoutData(RowDataFactory.swtDefaults().hint(50, 20).create())
        .message("Left").create(composite);

    // Bind
    ctx.bindValue(WidgetProperties.buttonSelection().observe(useTopLeftAnchor), intentModel.isUseTopLeftAnchor());
    ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(anchorTop), intentModel.getAnchorTop());
    ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(anchorLeft), intentModel.getAnchorLeft());

    // Disable anchor coordinate inputs if useTopLeftAnchor is not checked:
    ctx.bindValue(WidgetProperties.enabled().observe(anchorLeft), intentModel.isUseTopLeftAnchor(), UpdateValueStrategy.never(), null);
    ctx.bindValue(WidgetProperties.enabled().observe(anchorTop), intentModel.isUseTopLeftAnchor(), UpdateValueStrategy.never(), null);
    return composite;
  }

  private void requestPopup(final Qualifier qualifier, final SciWorkbenchPopupConfig config) {
    popupService.open(qualifier, config, String.class).whenComplete((result, ex) -> {
      if (ex != null) {
        validationMessage.setValue(ex.getMessage());
        validationLabel.getParent().setBackground(new Color(245, 182, 182));
        return;
      }
      validationMessage.setValue(result == null ? "Canceled" : result);
      validationLabel.getParent().setBackground(new Color(202, 255, 230));
    });
  }

  private TableViewer createTableViewer(final Composite parent) {
    var viewer = new TableViewer(new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER));
    viewer.getTable().setHeaderVisible(true);
    var contentProvider = new ObservableListContentProvider<Entry<String, String>>();
    viewer.setContentProvider(contentProvider);
    createColumns(viewer);
    GridDataFactory.swtDefaults().span(4, 1).hint(SWT.DEFAULT, SWT.DEFAULT).align(SWT.FILL, SWT.FILL).grab(true, true)
        .applyTo(viewer.getControl());

    return viewer;
  }

  private void createColumns(final TableViewer viewer) {
    var nameColumn = new TableViewerColumn(viewer, SWT.NONE);
    nameColumn.getColumn().setText("Name");
    nameColumn.getColumn().setWidth(120);
    nameColumn.setLabelProvider(new ColumnLabelProvider() {

      @SuppressWarnings("unchecked")
      @Override
      public String getText(final Object message) {
        return ((Entry<String, String>) message).getKey();
      }
    });

    var valueColumn = new TableViewerColumn(viewer, SWT.NONE);
    valueColumn.getColumn().setText("Value");
    valueColumn.getColumn().setWidth(290);
    valueColumn.setLabelProvider(new ColumnLabelProvider() {

      @SuppressWarnings("unchecked")
      @Override
      public String getText(final Object message) {
        return ((Entry<String, String>) message).getValue();
      }
    });
  }

  private Composite createValidationArea(final Composite parent) {
    var group = GroupFactory.newGroup(SWT.NONE).text("Result").create(parent);
    GridLayoutFactory.swtDefaults().numColumns(1).applyTo(group);

    validationLabel = TextFactory.newText(SWT.READ_ONLY | SWT.MULTI | SWT.WRAP)
        .layoutData(GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 40).grab(true, false).align(SWT.FILL, SWT.TOP).create())
        .font(JFaceResources.getFontRegistry().get(JFaceResources.DIALOG_FONT)).create(group);

    ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(validationLabel), validationMessage);

    return group;
  }

}
