package ch.sbb.scion.rcp.microfrontend.e3.app.demo.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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

import com.google.gson.Gson;

import ch.sbb.scion.rcp.microfrontend.ManifestService;
import ch.sbb.scion.rcp.microfrontend.e3.app.demo.view.model.CapabilityModel;
import ch.sbb.scion.rcp.microfrontend.e3.app.demo.view.model.ManifestObjectFilterModel;
import ch.sbb.scion.rcp.microfrontend.model.Capability;
import ch.sbb.scion.rcp.microfrontend.model.Capability.ParamDefinition;
import ch.sbb.scion.rcp.microfrontend.model.ManifestObjectFilter;
import ch.sbb.scion.rcp.microfrontend.model.TopicMessage;
import ch.sbb.scion.rcp.microfrontend.subscriber.ISubscription;

public class ManifestServicePart {

  @Inject
  private ManifestService manifestService;

  private final DataBindingContext dbc = new DataBindingContext();
  private Text validationText;
  private final IObservableValue<String> validationMessage = new WritableValue<>("", String.class);

  @PostConstruct
  public void createComposite(final Composite parent) {
    var composite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.swtDefaults().numColumns(2).applyTo(composite);

    var registerComposite = createRegisterComposite(composite);
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(registerComposite);

    var unregisterComposite = createUnregisterComposite(composite);
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(unregisterComposite);

    var validationArea = createValidationArea(composite);
    GridDataFactory.swtDefaults().span(2, 1).align(SWT.FILL, SWT.FILL).applyTo(validationArea);

    var subscribeGroup = createSubscribeGroup(composite);
    GridDataFactory.swtDefaults().span(2, 1).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(subscribeGroup);
  }

  private Composite createRegisterComposite(final Composite parent) {
    var capabilityModel = new CapabilityModel();
    var group = GroupFactory.newGroup(SWT.NONE).text("Register capability").create(parent);
    GridLayoutFactory.swtDefaults().numColumns(6).applyTo(group);

    // Type
    LabelFactory.newLabel(SWT.NONE).text("Type*:").layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).create()).create(group);
    var typeText = TextFactory.newText(SWT.SINGLE | SWT.BORDER)
        .layoutData(GridDataFactory.fillDefaults().span(5, 1).grab(true, false).create()).create(group);

    // Qualifier
    LabelFactory.newLabel(SWT.NONE).text("Qualifier:").layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).create())
        .create(group);

    var qualifierComp = CompositeFactory.newComposite(SWT.NONE).create(group);
    GridDataFactory.swtDefaults().span(5, 1).hint(0, 120).align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(qualifierComp);

    GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(4).applyTo(qualifierComp);

    var qualifierNameText = TextFactory.newText(SWT.SINGLE | SWT.BORDER)
        .layoutData(GridDataFactory.fillDefaults().hint(60, SWT.DEFAULT).create()).message("name").create(qualifierComp);

    var qualifierValueText = TextFactory.newText(SWT.SINGLE | SWT.BORDER)
        .layoutData(GridDataFactory.fillDefaults().grab(true, false).create()).message("value").create(qualifierComp);

    var addQualifierButton = ButtonFactory.newButton(SWT.NONE).text("+")
        .layoutData(GridDataFactory.fillDefaults().hint(25, SWT.DEFAULT).create()).create(qualifierComp);

    var removeQualifierButton = ButtonFactory.newButton(SWT.NONE).text("-").enabled(false)
        .layoutData(GridDataFactory.fillDefaults().hint(25, SWT.DEFAULT).create()).create(qualifierComp);

    var qualifierTableViewer = createQualifiersTableViewer(qualifierComp);
    qualifierTableViewer.setInput(capabilityModel.getQualifiers());
    qualifierTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      @Override
      public void selectionChanged(final SelectionChangedEvent event) {
        removeQualifierButton.setEnabled(!qualifierTableViewer.getSelection().isEmpty());
      }
    });

    addQualifierButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        capabilityModel.getQualifiers().add(Map.entry(qualifierNameText.getText(), qualifierValueText.getText()));
        qualifierNameText.setText("");
        qualifierValueText.setText("");
      }
    });

    removeQualifierButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        var selectedEntry = ((IStructuredSelection) qualifierTableViewer.getSelection()).getFirstElement();
        capabilityModel.getQualifiers().remove(selectedEntry);
      }
    });

    // Description
    LabelFactory.newLabel(SWT.NONE).text("Description:").layoutData(GridDataFactory.fillDefaults().hint(70, SWT.DEFAULT).create())
        .create(group);
    var descriptionText = TextFactory.newText(SWT.SINGLE | SWT.BORDER)
        .layoutData(GridDataFactory.fillDefaults().span(5, 1).grab(true, false).create()).create(group);

    LabelFactory.newLabel(SWT.NONE).text("Params:").layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).create()).create(group);

    var paramsComp = CompositeFactory.newComposite(SWT.NONE).create(group);
    GridDataFactory.swtDefaults().span(5, 1).hint(0, 120).align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(paramsComp);

    GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(5).applyTo(paramsComp);

    var paramNameText = TextFactory.newText(SWT.SINGLE | SWT.BORDER).layoutData(GridDataFactory.fillDefaults().hint(60, 20).create())
        .message("name").create(paramsComp);

    LabelFactory.newLabel(SWT.NONE).text("Required")
        .layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).align(SWT.BEGINNING, SWT.CENTER).create()).create(paramsComp);
    var isRequired = ButtonFactory.newButton(SWT.CHECK)
        .layoutData(GridDataFactory.fillDefaults().grab(true, false).align(SWT.BEGINNING, SWT.CENTER).create()).create(paramsComp);
    isRequired.setSelection(true);

    var addParamButton = ButtonFactory.newButton(SWT.NONE).text("+")
        .layoutData(GridDataFactory.fillDefaults().hint(25, SWT.DEFAULT).create()).create(paramsComp);

    var removeParamButton = ButtonFactory.newButton(SWT.NONE).text("-").enabled(false)
        .layoutData(GridDataFactory.fillDefaults().hint(25, SWT.DEFAULT).create()).create(paramsComp);

    var paramsTableViewer = createParamsTableViewer(paramsComp);
    paramsTableViewer.setInput(capabilityModel.getParams());
    paramsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      @Override
      public void selectionChanged(final SelectionChangedEvent event) {
        removeParamButton.setEnabled(!paramsTableViewer.getSelection().isEmpty());
      }
    });

    addParamButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        capabilityModel.getParams()
            .add(ParamDefinition.builder().name(paramNameText.getText()).required(Boolean.valueOf(isRequired.getSelection())).build());
        paramNameText.setText("");
        isRequired.setSelection(true);
      }
    });

    removeParamButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        var selectedRow = ((IStructuredSelection) paramsTableViewer.getSelection()).getFirstElement();
        capabilityModel.getParams().remove(selectedRow);
      }
    });

    // IsPrivate
    LabelFactory.newLabel(SWT.NONE).text("Private:").layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).create())
        .create(group);
    var isPrivate = ButtonFactory.newButton(SWT.CHECK).layoutData(GridDataFactory.fillDefaults().span(5, 1).grab(true, false).create())
        .create(group);

    // Register button
    var registerButton = ButtonFactory.newButton(SWT.NONE).text("Register").onSelect(e -> {
      registerCapability(capabilityModel.getCapability());
      capabilityModel.clearValues();
    }).layoutData(GridDataFactory.fillDefaults().span(6, 1).create()).create(group);

    var typeModel = new WritableValue<>();
    var typeWidgetValue = WidgetProperties.text(SWT.Modify).observe(typeText);
    var validator = new IValidator<String>() {

      @Override
      public IStatus validate(final String value) {
        String s = String.valueOf(value);
        if (!s.isBlank()) {
          return ValidationStatus.ok();
        }
        return ValidationStatus.warning("Field is mandatory");
      }
    };

    // add bindings
    dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(typeText), capabilityModel.getType());
    dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(descriptionText), capabilityModel.getDescription());
    dbc.bindValue(WidgetProperties.buttonSelection().observe(isPrivate), capabilityModel.getIsPrivate());

    var strategy = new UpdateValueStrategy<String, String>();
    strategy.setBeforeSetValidator(validator);

    var bindingType = dbc.bindValue(typeWidgetValue, typeModel, strategy, null);
    ControlDecorationSupport.create(bindingType, SWT.TOP | SWT.LEFT);

    IConverter<String, Boolean> stringToBooleanConverter = IConverter.create(String.class, Boolean.class,
        (o1) -> Boolean.valueOf(!o1.isEmpty()));

    IObservableValue<Boolean> qualifierFieldsSet = ComputedValue.create(() -> {
      return Boolean.valueOf(!WidgetProperties.text(SWT.Modify).observe(qualifierNameText).getValue().isEmpty()
          && !WidgetProperties.text(SWT.Modify).observe(qualifierValueText).getValue().isEmpty());
    });

    dbc.bindValue(WidgetProperties.enabled().observe(addQualifierButton), qualifierFieldsSet);

    dbc.bindValue(WidgetProperties.enabled().observe(addParamButton), WidgetProperties.text(SWT.Modify).observe(paramNameText), null,
        UpdateValueStrategy.create(stringToBooleanConverter));

    dbc.bindValue(WidgetProperties.enabled().observe(registerButton), typeWidgetValue, null,
        UpdateValueStrategy.create(stringToBooleanConverter));

    return group;
  }

  private Composite createUnregisterComposite(final Composite parent) {
    var filterModel = new ManifestObjectFilterModel();
    var group = GroupFactory.newGroup(SWT.NONE).text("Unregister capability(-ies)").create(parent);
    GridLayoutFactory.swtDefaults().applyTo(group);

    // ManifestObjectFilter composite
    var filterComposite = createManifestObjectFilterComposite(group, filterModel);
    GridDataFactory.swtDefaults().hint(50, SWT.DEFAULT).align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(filterComposite);

    // Unregister button
    ButtonFactory.newButton(SWT.NONE).text("Unregister").onSelect(e -> {
      unregisterCapability(filterModel.getFilter());
      filterModel.clearValues();
    }).layoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BOTTOM).grab(true, true).create()).create(group);

    return group;
  }

  private Composite createValidationArea(final Composite parent) {
    var group = GroupFactory.newGroup(SWT.NONE).text("Result").create(parent);
    GridLayoutFactory.swtDefaults().numColumns(1).applyTo(group);

    // validation text field
    validationText = TextFactory.newText(SWT.READ_ONLY | SWT.MULTI | SWT.WRAP)
        .layoutData(GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 40).grab(true, false).align(SWT.FILL, SWT.CENTER).create())
        .font(JFaceResources.getFontRegistry().get(JFaceResources.DIALOG_FONT)).create(group);
    dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(validationText), validationMessage);

    return group;
  }

  private Composite createManifestObjectFilterComposite(final Composite parent, final ManifestObjectFilterModel filterModel) {
    var comp = CompositeFactory.newComposite(SWT.NONE).create(parent);
    GridLayoutFactory.swtDefaults().numColumns(5).applyTo(comp);

    // ID
    LabelFactory.newLabel(SWT.NONE).text("ID:").layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).create()).create(comp);
    var idText = TextFactory.newText(SWT.SINGLE | SWT.BORDER)
        .layoutData(GridDataFactory.fillDefaults().span(4, 1).grab(true, false).create()).create(comp);

    // Type
    LabelFactory.newLabel(SWT.NONE).text("Type:").layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).create()).create(comp);
    var typeText = TextFactory.newText(SWT.SINGLE | SWT.BORDER)
        .layoutData(GridDataFactory.fillDefaults().span(4, 1).grab(true, false).create()).create(comp);

    // Qualifier
    LabelFactory.newLabel(SWT.NONE).text("Qualifier:").layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).create())
        .create(comp);

    var qualifierComp = CompositeFactory.newComposite(SWT.NONE).create(comp);
    GridDataFactory.swtDefaults().span(4, 1).hint(0, 120).align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(qualifierComp);

    GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(4).applyTo(qualifierComp);

    var qualifierNameText = TextFactory.newText(SWT.SINGLE | SWT.BORDER)
        .layoutData(GridDataFactory.fillDefaults().hint(60, SWT.DEFAULT).create()).message("name").create(qualifierComp);

    var qualifierValueText = TextFactory.newText(SWT.SINGLE | SWT.BORDER)
        .layoutData(GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).grab(true, false).create()).message("value")
        .create(qualifierComp);

    var addQualifierButton = ButtonFactory.newButton(SWT.NONE).text("+")
        .layoutData(GridDataFactory.fillDefaults().hint(25, SWT.DEFAULT).create()).create(qualifierComp);

    var removeQualifierButton = ButtonFactory.newButton(SWT.NONE).text("-").enabled(false)
        .layoutData(GridDataFactory.fillDefaults().hint(25, SWT.DEFAULT).create()).create(qualifierComp);

    var qualifierTableViewer = createQualifiersTableViewer(qualifierComp);
    qualifierTableViewer.setInput(filterModel.getQualifiers());
    qualifierTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      @Override
      public void selectionChanged(final SelectionChangedEvent event) {
        removeQualifierButton.setEnabled(!qualifierTableViewer.getSelection().isEmpty());
      }

    });
    addQualifierButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        filterModel.getQualifiers().add(Map.entry(qualifierNameText.getText(), qualifierValueText.getText()));
        qualifierNameText.setText("");
        qualifierValueText.setText("");
      }
    });

    removeQualifierButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        var selectedEntry = ((IStructuredSelection) qualifierTableViewer.getSelection()).getFirstElement();
        filterModel.getQualifiers().remove(selectedEntry);
      }
    });

    // Application
    LabelFactory.newLabel(SWT.NONE).text("Application:").layoutData(GridDataFactory.fillDefaults().hint(70, SWT.DEFAULT).create())
        .create(comp);
    var applicationText = TextFactory.newText(SWT.SINGLE | SWT.BORDER)
        .layoutData(GridDataFactory.fillDefaults().span(4, 1).grab(true, false).create()).create(comp);

    // add bindings
    dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(idText), filterModel.getId());
    dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(typeText), filterModel.getType());
    dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(applicationText), filterModel.getAppSymbolicName());

    final IObservableValue<Boolean> qualifierFieldsSet = ComputedValue.create(() -> {
      return Boolean.valueOf(!WidgetProperties.text(SWT.Modify).observe(qualifierNameText).getValue().isEmpty()
          && !WidgetProperties.text(SWT.Modify).observe(qualifierValueText).getValue().isEmpty());
    });

    dbc.bindValue(WidgetProperties.enabled().observe(addQualifierButton), qualifierFieldsSet);

    return comp;
  }

  private void registerCapability(final Capability newCapability) {
    var whenRegistered = manifestService.registerCapability(newCapability);
    whenRegistered.exceptionally(ex -> {
      validationMessage.setValue(ex.getMessage());
      validationText.getParent().setBackground(new Color(245, 182, 182));
      return ex.getMessage();
    });

    whenRegistered.thenAccept(s -> {
      validationMessage.setValue("Registered capability with ID: " + s);
      validationText.getParent().setBackground(new Color(202, 255, 230));
    });
  }

  private void unregisterCapability(final ManifestObjectFilter filter) {
    var whenUnregistered = manifestService.unregisterCapabilities(filter);
    whenUnregistered.exceptionally(ex -> {
      validationMessage.setValue(ex.getMessage());
      validationText.getParent().setBackground(new Color(245, 182, 182));
      return null;
    });

    whenUnregistered.thenAccept(s -> {
      validationMessage.setValue("Unregister OK.");
      validationText.getParent().setBackground(new Color(202, 255, 230));
    });
  }

  private TableViewer createQualifiersTableViewer(final Composite parent) {
    var viewer = new TableViewer(new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER));
    viewer.getTable().setHeaderVisible(true);
    var contentProvider = new ObservableListContentProvider<Entry<String, String>>();
    viewer.setContentProvider(contentProvider);
    createQualifierColumns(viewer);
    GridDataFactory.swtDefaults().span(4, 1).hint(0, SWT.DEFAULT).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(viewer.getControl());

    return viewer;
  }

  private void createQualifierColumns(final TableViewer viewer) {
    var nameColumn = new TableViewerColumn(viewer, SWT.NONE);
    nameColumn.getColumn().setText("Name");
    nameColumn.getColumn().setWidth(70);
    nameColumn.setLabelProvider(new ColumnLabelProvider() {

      @SuppressWarnings("unchecked")
      @Override
      public String getText(final Object message) {
        return ((Entry<String, String>) message).getKey();
      }
    });

    var valueColumn = new TableViewerColumn(viewer, SWT.NONE);
    valueColumn.getColumn().setText("Value");
    valueColumn.getColumn().setWidth(190);
    valueColumn.setLabelProvider(new ColumnLabelProvider() {

      @SuppressWarnings("unchecked")
      @Override
      public String getText(final Object message) {
        return ((Entry<String, String>) message).getValue();
      }
    });
  }

  private TableViewer createParamsTableViewer(final Composite parent) {
    var viewer = new TableViewer(new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER));
    viewer.getTable().setHeaderVisible(true);
    var contentProvider = new ObservableListContentProvider<ParamDefinition>();
    viewer.setContentProvider(contentProvider);
    createParamsColumns(viewer);
    GridDataFactory.swtDefaults().span(5, 1).hint(0, 100).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(viewer.getControl());

    return viewer;
  }

  private void createParamsColumns(final TableViewer viewer) {
    var nameColumn = new TableViewerColumn(viewer, SWT.NONE);
    nameColumn.getColumn().setText("Name");
    nameColumn.getColumn().setWidth(70);
    nameColumn.setLabelProvider(new ColumnLabelProvider() {

      @Override
      public String getText(final Object message) {
        return ((ParamDefinition) message).name();
      }
    });

    var requiredColumn = new TableViewerColumn(viewer, SWT.NONE);
    requiredColumn.getColumn().setText("Required");
    requiredColumn.getColumn().setWidth(100);
    requiredColumn.setLabelProvider(new ColumnLabelProvider() {

      @Override
      public String getText(final Object message) {
        return ((ParamDefinition) message).required().booleanValue() ? "True" : "False";
      }
    });
  }

  private Composite createSubscribeGroup(final Composite parent) {
    var filterModel = new ManifestObjectFilterModel();
    var capabilities = new ArrayList<Capability>();
    var group = GroupFactory.newGroup(SWT.NONE).text("Lookup Capabilites").create(parent);
    GridLayoutFactory.swtDefaults().numColumns(2).applyTo(group);

    // ManifestObjectFilter composite
    var filterComposite = createManifestObjectFilterComposite(group, filterModel);
    GridDataFactory.swtDefaults().span(2, 1).hint(50, SWT.DEFAULT).align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(filterComposite);

    // Subscribe button
    var subscribeButton = ButtonFactory.newButton(SWT.NONE).text("Subscribe").layoutData(GridDataFactory.fillDefaults().span(2, 1).create())
        .create(group);

    var messages = new ArrayList<TopicMessage<String>>();
    var capabilitiesTableViewer = new TableViewer(group);
    capabilitiesTableViewer.getTable().setHeaderVisible(true);
    capabilitiesTableViewer.setContentProvider(ArrayContentProvider.getInstance());
    createIdColumn(capabilitiesTableViewer);
    createTypeColumn(capabilitiesTableViewer);
    createQualifierColumn(capabilitiesTableViewer);
    createAccessibilityColumn(capabilitiesTableViewer);
    createDescriptionColumn(capabilitiesTableViewer);
    createParamsColumn(capabilitiesTableViewer);
    createPropertiesColumn(capabilitiesTableViewer);
    createApplicationColumn(capabilitiesTableViewer);
    GridDataFactory.swtDefaults().span(2, 1).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(capabilitiesTableViewer.getControl());

    capabilitiesTableViewer.setInput(Collections.EMPTY_LIST);

    var subscription = new AtomicReference<ISubscription>();
    subscribeButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        if (subscription.get() == null) {
          subscription.set(manifestService.lookupCapabilities(filterModel.getFilter(), cpyList -> {
            capabilities.clear();
            capabilities.addAll(cpyList);
            capabilitiesTableViewer.setInput(capabilities);
          }));
          subscribeButton.setText("Unsubscribe");
        }
        else {
          messages.clear();
          capabilitiesTableViewer.setInput(Collections.EMPTY_LIST);
          subscription.getAndSet(null).unsubscribe();
          subscribeButton.setText("Subscribe");
        }
        filterComposite.setEnabled(subscription.get() == null);
      }
    });

    return group;
  }

  private void createDescriptionColumn(final TableViewer messagesTableViewer) {
    var descriptionColumn = new TableViewerColumn(messagesTableViewer, SWT.NONE);
    descriptionColumn.getColumn().setText("Description");
    descriptionColumn.getColumn().setWidth(100);
    descriptionColumn.setLabelProvider(new ColumnLabelProvider() {

      @Override
      public String getText(final Object message) {
        return ((Capability) message).description();
      }
    });
  }

  private void createParamsColumn(final TableViewer messagesTableViewer) {
    var paramsColumn = new TableViewerColumn(messagesTableViewer, SWT.NONE);
    paramsColumn.getColumn().setText("Params");
    paramsColumn.getColumn().setWidth(100);
    paramsColumn.setLabelProvider(new ColumnLabelProvider() {

      @Override
      public String getText(final Object message) {
        var params = ((Capability) message).params();
        return new Gson().toJson(params);
      }
    });
  }

  private void createPropertiesColumn(final TableViewer messagesTableViewer) {
    var paramsColumn = new TableViewerColumn(messagesTableViewer, SWT.NONE);
    paramsColumn.getColumn().setText("Properties");
    paramsColumn.getColumn().setWidth(100);
    paramsColumn.setLabelProvider(new ColumnLabelProvider() {

      @Override
      public String getText(final Object message) {
        var params = ((Capability) message).properties();
        return new Gson().toJson(params);
      }
    });
  }

  private void createQualifierColumn(final TableViewer messagesTableViewer) {
    var qualifierColumn = new TableViewerColumn(messagesTableViewer, SWT.NONE);
    qualifierColumn.getColumn().setText("Qualifier");
    qualifierColumn.getColumn().setWidth(100);
    qualifierColumn.setLabelProvider(new ColumnLabelProvider() {

      @Override
      public String getText(final Object message) {
        var qualifier = ((Capability) message).qualifier();
        return new Gson().toJson(qualifier);
      }
    });
  }

  private void createAccessibilityColumn(final TableViewer messagesTableViewer) {
    var accessibilityColumn = new TableViewerColumn(messagesTableViewer, SWT.NONE);
    accessibilityColumn.getColumn().setText("Accessibility");
    accessibilityColumn.getColumn().setWidth(100);
    accessibilityColumn.setLabelProvider(new ColumnLabelProvider() {

      @Override
      public String getText(final Object message) {
        return ((Capability) message).isPrivate().booleanValue() ? "Private" : "Public";
      }
    });
  }

  private void createTypeColumn(final TableViewer messagesTableViewer) {
    var typeColumn = new TableViewerColumn(messagesTableViewer, SWT.NONE);
    typeColumn.getColumn().setText("Type");
    typeColumn.getColumn().setWidth(100);
    typeColumn.setLabelProvider(new ColumnLabelProvider() {

      @Override
      public String getText(final Object message) {
        return ((Capability) message).type();
      }
    });
  }

  private void createIdColumn(final TableViewer messagesTableViewer) {
    var idColumn = new TableViewerColumn(messagesTableViewer, SWT.NONE);
    idColumn.getColumn().setText("Id");
    idColumn.getColumn().setWidth(100);
    idColumn.setLabelProvider(new ColumnLabelProvider() {

      @Override
      public String getText(final Object message) {
        var capability = (Capability) message;
        return capability.metadata().id();
      }
    });
  }

  private void createApplicationColumn(final TableViewer messagesTableViewer) {
    var appColumn = new TableViewerColumn(messagesTableViewer, SWT.NONE);
    appColumn.getColumn().setText("App");
    appColumn.getColumn().setWidth(100);
    appColumn.setLabelProvider(new ColumnLabelProvider() {

      @Override
      public String getText(final Object message) {
        var capability = (Capability) message;
        return capability.metadata().appSymbolicName();
      }
    });
  }

}