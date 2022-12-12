package ch.sbb.scion.rcp.microfrontend.e3.app.demo.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.list.IObservableList;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import com.google.gson.Gson;

import ch.sbb.scion.rcp.microfrontend.SciIntentClient;
import ch.sbb.scion.rcp.microfrontend.SciMessageClient;
import ch.sbb.scion.rcp.microfrontend.e3.app.demo.view.model.IntentMessageModel;
import ch.sbb.scion.rcp.microfrontend.e3.app.demo.view.model.IntentSelectorModel;
import ch.sbb.scion.rcp.microfrontend.model.ISubscriber;
import ch.sbb.scion.rcp.microfrontend.model.ISubscription;
import ch.sbb.scion.rcp.microfrontend.model.Intent;
import ch.sbb.scion.rcp.microfrontend.model.IntentMessage;
import ch.sbb.scion.rcp.microfrontend.model.MessageHeaders;
import ch.sbb.scion.rcp.microfrontend.model.PublishOptions;
import ch.sbb.scion.rcp.microfrontend.model.RequestOptions;
import ch.sbb.scion.rcp.microfrontend.model.ResponseStatusCodes;
import ch.sbb.scion.rcp.microfrontend.model.TopicMessage;

public class IntentClientPart {

  @Inject
  private SciIntentClient intentClient;

  @Inject
  private SciMessageClient messageClient;

  private final DataBindingContext ctx = new DataBindingContext();

  private Button publishButton;

  private ISubscription requestSubscription;

  private Text validationLabel;

  private IObservableValue<String> validationMessage = new WritableValue<>("", String.class);

  @PostConstruct
  public void createComposite(Composite parent) {
    var composite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.swtDefaults().applyTo(composite);

    var publishGroup = createPublishGroup(composite);
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(publishGroup);

    var validationArea = createValidationArea(composite);
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(validationArea);

    var subscribeGroup = createSubscribeGroup(composite);
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(subscribeGroup);
  }

  private Composite createPublishGroup(final Composite parent) {
    var intentModel = new IntentMessageModel();
    var group = GroupFactory.newGroup(SWT.NONE).text("Publish an intent").create(parent);
    GridLayoutFactory.swtDefaults().numColumns(5).applyTo(group);

    // Type
    LabelFactory.newLabel(SWT.NONE)
        .text("Type*:")
        .layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).align(SWT.BEGINNING, SWT.TOP).create())
        .create(group);
    var typeText = TextFactory.newText(SWT.SINGLE | SWT.BORDER)
        .layoutData(GridDataFactory.fillDefaults().span(4, 1).grab(true, false).create())
        .create(group);

    // Qualifier
    LabelFactory.newLabel(SWT.NONE)
        .text("Qualifier:")
        .layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).align(SWT.BEGINNING, SWT.TOP).create())
        .create(group);

    var qualifierComposite = createKeyValuePairViewerComposite(group, intentModel.getQualifiers());
    GridDataFactory.swtDefaults()
        .span(4, 1)
        .hint(SWT.DEFAULT, 100)
        .align(SWT.FILL, SWT.FILL)
        .grab(true, false)
        .applyTo(qualifierComposite);

    // Params
    LabelFactory.newLabel(SWT.NONE)
        .text("Params:")
        .layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).align(SWT.BEGINNING, SWT.TOP).create())
        .create(group);

    var paramComposite = createKeyValuePairViewerComposite(group, intentModel.getParams());
    GridDataFactory.swtDefaults()
        .span(4, 1)
        .hint(SWT.DEFAULT, 100)
        .align(SWT.FILL, SWT.FILL)
        .grab(true, false)
        .applyTo(paramComposite);

    // Message
    LabelFactory.newLabel(SWT.NONE)
        .text("Message:")
        .layoutData(GridDataFactory.fillDefaults().hint(70, SWT.DEFAULT).align(SWT.BEGINNING, SWT.TOP).create())
        .create(group);
    var messageText = TextFactory.newText(SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL)
        .layoutData(GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 50).span(4, 1).grab(true, false).create())
        .create(group);

    // Headers
    LabelFactory.newLabel(SWT.NONE)
        .text("Headers:")
        .layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).align(SWT.BEGINNING, SWT.TOP).create())
        .create(group);

    var headersComposite = createKeyValuePairViewerComposite(group, intentModel.getHeaders());
    GridDataFactory.swtDefaults()
        .span(4, 1)
        .hint(SWT.DEFAULT, 100)
        .align(SWT.FILL, SWT.FILL)
        .grab(true, false)
        .applyTo(headersComposite);

    // Retain
    LabelFactory.newLabel(SWT.NONE)
        .text("Retain:")
        .layoutData(GridDataFactory.fillDefaults()
            .hint(SWT.DEFAULT, SWT.DEFAULT)
            .align(SWT.BEGINNING, SWT.TOP)
            .create())
        .create(group);
    var retain = ButtonFactory.newButton(SWT.CHECK)
        .layoutData(GridDataFactory.fillDefaults().span(4, 1).grab(true, false).create())
        .create(group);

    // Request-Reply
    LabelFactory.newLabel(SWT.NONE)
        .text("Request-Reply:")
        .layoutData(GridDataFactory.fillDefaults()
            .hint(SWT.DEFAULT, SWT.DEFAULT)
            .align(SWT.BEGINNING, SWT.TOP)
            .create())
        .create(group);
    var requestReply = ButtonFactory.newButton(SWT.CHECK)
        .layoutData(GridDataFactory.fillDefaults().span(4, 1).grab(true, false).create())
        .create(group);

    // Publish button
    publishButton = ButtonFactory.newButton(SWT.NONE)
        .text("Publish")
        .layoutData(GridDataFactory.fillDefaults().span(5, 1).create())
        .create(group);

    publishButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        var body = intentModel.getMessage().getValue().isEmpty() ? null : intentModel.getMessage().getValue();
        var headers = intentModel.getHeaders()
            .stream()
            .collect(Collectors.toMap(x -> x.getKey(), x -> (Object) x.getValue()));
        if (requestSubscription != null) {
          cancelRequest();
        }
        else if (intentModel.isRequestReply().getValue()) {
          publishButton.setText("Cancel");
          var options = new RequestOptions().headers(headers).retain(intentModel.isRetain().getValue());
          requestSubscription = request(intentModel.getIntent(), body, options);
        }
        else {
          var options = new PublishOptions().headers(headers).retain(intentModel.isRetain().getValue());
          publish(intentModel.getIntent(), body, options);
        }
      }
    });

    var typeModel = new WritableValue<>();
    var typeWidgetValue = WidgetProperties.text(SWT.Modify).observe(typeText);
    var validator = new IValidator<String>() {
      @Override
      public IStatus validate(String value) {
        String s = String.valueOf(value);
        if (!s.isBlank()) {
          return ValidationStatus.ok();
        }
        return ValidationStatus.warning("Field is mandatory");
      }
    };

    // add bindings
    ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(typeText), intentModel.getType());
    ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(messageText), intentModel.getMessage());
    ctx.bindValue(WidgetProperties.buttonSelection().observe(retain), intentModel.isRetain());
    ctx.bindValue(WidgetProperties.buttonSelection().observe(requestReply), intentModel.isRequestReply());

    var strategy = new UpdateValueStrategy<String, String>();
    strategy.setBeforeSetValidator(validator);

    var bindingType = ctx.bindValue(typeWidgetValue, typeModel, strategy, null);
    ControlDecorationSupport.create(bindingType, SWT.TOP | SWT.LEFT);

    IConverter<String, Boolean> stringToBooleanConverter = IConverter
        .create(String.class, Boolean.class, (o1) -> !o1.isEmpty());

    ctx.bindValue(WidgetProperties.enabled().observe(publishButton), typeWidgetValue, null, UpdateValueStrategy
        .create(stringToBooleanConverter));

    return group;
  }

  private void cancelRequest() {
    requestSubscription.unsubscribe();
    requestSubscription = null;
    publishButton.setText("Publish");
  }

  private Composite createKeyValuePairViewerComposite(final Composite parent,
      final IObservableList<Entry<String, String>> entryModel) {
    var composite = CompositeFactory.newComposite(SWT.NONE).create(parent);
    GridLayoutFactory.swtDefaults().numColumns(4).margins(0, 0).applyTo(composite);

    var nameText = TextFactory.newText(SWT.SINGLE | SWT.BORDER)
        .layoutData(GridDataFactory.fillDefaults().hint(100, 20).align(SWT.BEGINNING, SWT.TOP).create())
        .message("name")
        .create(composite);

    var valueText = TextFactory.newText(SWT.SINGLE | SWT.BORDER)
        .layoutData(GridDataFactory.fillDefaults().grab(true, false).create())
        .message("value")
        .create(composite);

    var addEntryButton = ButtonFactory.newButton(SWT.NONE)
        .text("+")
        .layoutData(GridDataFactory.fillDefaults().hint(30, 20).create())
        .create(composite);

    var removeEntryButton = ButtonFactory.newButton(SWT.NONE)
        .text("-")
        .enabled(false)
        .layoutData(GridDataFactory.fillDefaults().hint(30, 20).create())
        .create(composite);

    var tableViewer = createTableViewer(composite);
    tableViewer.setInput(entryModel);
    tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        removeEntryButton.setEnabled(!tableViewer.getSelection().isEmpty());
      }
    });

    addEntryButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        entryModel.add(Map.entry(nameText.getText(), valueText.getText()));
        nameText.setText("");
        valueText.setText("");
      }
    });

    removeEntryButton.addSelectionListener(new SelectionAdapter() {
      @SuppressWarnings("unchecked")
      @Override
      public void widgetSelected(final SelectionEvent e) {
        var selectedEntry = ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
        entryModel.remove((Entry<String, String>) selectedEntry);
      }
    });

    IObservableValue<Boolean> fieldsSet = ComputedValue.create(() -> {
      return !WidgetProperties.text(SWT.Modify).observe(nameText).getValue().isEmpty()
          && !WidgetProperties.text(SWT.Modify).observe(valueText).getValue().isEmpty();
    });

    ctx.bindValue(WidgetProperties.enabled().observe(addEntryButton), fieldsSet);

    return composite;
  }

  private void publish(final Intent intent, final String body, final PublishOptions publishOptions) {
    var whenPublished = intentClient.publish(intent, body, publishOptions);
    whenPublished.exceptionally(ex -> {
      validationMessage.setValue(ex.getMessage());
      validationLabel.getParent().setBackground(new Color(245, 182, 182));
      return null;
    });

    whenPublished.thenAccept(s -> {
      validationMessage.setValue("Intent published.");
      validationLabel.getParent().setBackground(new Color(202, 255, 230));
    });
  }

  private ISubscription request(final Intent intent, final String body, final RequestOptions requestOptions) {
    return intentClient.request(intent, body, requestOptions, String.class, new ISubscriber<TopicMessage<String>>() {

      @Override
      public void onNext(TopicMessage<String> next) {
        var replyBox = new MessageBox(validationLabel.getShell(), SWT.ICON_INFORMATION | SWT.OK);
        replyBox.setText("Reply from application: " + next.headers.get(MessageHeaders.AppSymbolicName.value));
        replyBox.setMessage(next.body);
        if (replyBox.open() == SWT.OK) {
          cancelRequest();
        }
      }

      @Override
      public void onError(Exception e) {
        validationMessage.setValue(e.getMessage());
        validationLabel.getParent().setBackground(new Color(245, 182, 182));
        cancelRequest();
      }
    });
  }

  private TableViewer createTableViewer(Composite parent) {
    var viewer = new TableViewer(new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER));
    viewer.getTable().setHeaderVisible(true);
    var contentProvider = new ObservableListContentProvider<Entry<String, String>>();
    viewer.setContentProvider(contentProvider);
    createColumns(viewer);
    GridDataFactory.swtDefaults()
        .span(4, 1)
        .hint(SWT.DEFAULT, SWT.DEFAULT)
        .align(SWT.FILL, SWT.FILL)
        .grab(true, true)
        .applyTo(viewer.getControl());

    return viewer;
  }

  private void createColumns(TableViewer viewer) {
    var nameColumn = new TableViewerColumn(viewer, SWT.NONE);
    nameColumn.getColumn().setText("Name");
    nameColumn.getColumn().setWidth(120);
    nameColumn.setLabelProvider(new ColumnLabelProvider() {

      @SuppressWarnings("unchecked")
      @Override
      public String getText(Object message) {
        return ((Entry<String, String>) message).getKey();
      }
    });

    var valueColumn = new TableViewerColumn(viewer, SWT.NONE);
    valueColumn.getColumn().setText("Value");
    valueColumn.getColumn().setWidth(290);
    valueColumn.setLabelProvider(new ColumnLabelProvider() {

      @SuppressWarnings("unchecked")
      @Override
      public String getText(Object message) {
        return ((Entry<String, String>) message).getValue();
      }
    });
  }

  private Composite createValidationArea(Composite parent) {
    var group = GroupFactory.newGroup(SWT.NONE).text("Result").create(parent);
    GridLayoutFactory.swtDefaults().numColumns(1).applyTo(group);

    validationLabel = TextFactory.newText(SWT.READ_ONLY | SWT.MULTI | SWT.WRAP)
        .layoutData(GridDataFactory.fillDefaults()
            .hint(SWT.DEFAULT, 40)
            .grab(true, false)
            .align(SWT.FILL, SWT.TOP)
            .create())
        .font(JFaceResources.getFontRegistry().get(JFaceResources.DIALOG_FONT))
        .create(group);

    ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(validationLabel), validationMessage);

    return group;
  }

  private Composite createSubscribeGroup(final Composite parent) {
    var selectorModel = new IntentSelectorModel();
    var group = GroupFactory.newGroup(SWT.NONE).text("Subscribe").create(parent);
    GridLayoutFactory.swtDefaults().numColumns(2).applyTo(group);

    // Type
    LabelFactory.newLabel(SWT.NONE)
        .text("Type:")
        .layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).align(SWT.BEGINNING, SWT.TOP).create())
        .create(group);
    var typeText = TextFactory.newText(SWT.SINGLE | SWT.BORDER)
        .layoutData(GridDataFactory.fillDefaults().grab(true, false).create())
        .create(group);

    // Qualifier
    LabelFactory.newLabel(SWT.NONE)
        .text("Qualifier:")
        .layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).align(SWT.BEGINNING, SWT.TOP).create())
        .create(group);

    var qualifierComposite = createKeyValuePairViewerComposite(group, selectorModel.getQualifiers());
    GridDataFactory.swtDefaults()
        .hint(SWT.DEFAULT, 100)
        .align(SWT.FILL, SWT.FILL)
        .grab(true, false)
        .applyTo(qualifierComposite);

    // Subscribe button
    var subscribeButton = ButtonFactory.newButton(SWT.NONE)
        .text("Subscribe")
        .layoutData(GridDataFactory.fillDefaults().span(2, 1).create())
        .create(group);

    // add bindings
    ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(typeText), selectorModel.getType());

    var messages = new ArrayList<IntentMessage<?>>();
    var intentsTableViewer = new TableViewer(new Table(group, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION
        | SWT.BORDER));
    var table = intentsTableViewer.getTable();
    table.setHeaderVisible(true);
    intentsTableViewer.setContentProvider(ArrayContentProvider.getInstance());
    createContextMenu(table);
    createTypeColumn(intentsTableViewer);
    createQualifierColumn(intentsTableViewer);
    createParamsColumn(intentsTableViewer);
    createBodyColumn(intentsTableViewer);
    createHeadersColumn(intentsTableViewer);
    createCapabilityIdColumn(intentsTableViewer);
    GridDataFactory.fillDefaults()
        .span(2, 1)
        .align(SWT.FILL, SWT.FILL)
        .grab(true, true)
        .applyTo(intentsTableViewer.getControl());

    intentsTableViewer.setInput(Collections.EMPTY_LIST);

    var subscription = new AtomicReference<ISubscription>();
    subscribeButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        if (subscription.get() == null) {
          subscription.set(intentClient.subscribe(selectorModel.getSelector(), intentMessage -> {
            messages.add(0, intentMessage);
            intentsTableViewer.setInput(new ArrayList<>(messages));
          }));
          subscribeButton.setText("Unsubscribe");
        }
        else {
          messages.clear();
          intentsTableViewer.setInput(Collections.EMPTY_LIST);
          subscription.getAndSet(null).unsubscribe();
          subscribeButton.setText("Subscribe");
        }
        typeText.setEnabled(subscription.get() == null);
      }
    });

    return group;
  }

  private void createContextMenu(final Table table) {
    var contextMenu = new Menu(table);
    table.setMenu(contextMenu);
    var menuItem = new MenuItem(contextMenu, SWT.None);
    menuItem.setText("Reply");
    menuItem.addSelectionListener(new SelectionAdapter() {
      @SuppressWarnings("rawtypes")
      @Override
      public void widgetSelected(SelectionEvent evnt) {
        var selectedIntentMessage = (IntentMessage) table.getSelection()[0].getData();
        var replyTo = selectedIntentMessage.headers.get(MessageHeaders.ReplyTo.value).toString();
        var headers = Map.of(MessageHeaders.Status.value, ResponseStatusCodes.TERMINAL.value);
        messageClient.publish(replyTo, "This is a reply.", new PublishOptions().headers(headers));
      }
    });

    table.addListener(SWT.MenuDetect, new Listener() {
      @SuppressWarnings("rawtypes")
      @Override
      public void handleEvent(Event event) {
        if (table.getSelectionCount() <= 0) {
          event.doit = false;
        }
        if (table.getSelectionCount() > 0) {
          var selectedIntentMessage = (IntentMessage) table.getSelection()[0].getData();
          if (selectedIntentMessage.headers == null
              || selectedIntentMessage.headers.get(MessageHeaders.ReplyTo.value) == null) {
            event.doit = false;
          }
        }
      }
    });
  }

  private void createBodyColumn(TableViewer messagesTableViewer) {
    var descriptionColumn = new TableViewerColumn(messagesTableViewer, SWT.NONE);
    descriptionColumn.getColumn().setText("Body");
    descriptionColumn.getColumn().setWidth(120);
    descriptionColumn.setLabelProvider(new ColumnLabelProvider() {

      @SuppressWarnings("unchecked")
      @Override
      public String getText(Object message) {
        return ((IntentMessage<String>) message).body;
      }
    });
  }

  private void createParamsColumn(TableViewer messagesTableViewer) {
    var paramsColumn = new TableViewerColumn(messagesTableViewer, SWT.NONE);
    paramsColumn.getColumn().setText("Params");
    paramsColumn.getColumn().setWidth(120);
    paramsColumn.setLabelProvider(new ColumnLabelProvider() {

      @SuppressWarnings("unchecked")
      @Override
      public String getText(Object message) {
        var params = ((IntentMessage<Void>) message).intent.params;
        return new Gson().toJson(params);
      }
    });
  }

  private void createQualifierColumn(TableViewer messagesTableViewer) {
    var qualifierColumn = new TableViewerColumn(messagesTableViewer, SWT.NONE);
    qualifierColumn.getColumn().setText("Qualifier");
    qualifierColumn.getColumn().setWidth(120);
    qualifierColumn.setLabelProvider(new ColumnLabelProvider() {

      @SuppressWarnings("unchecked")
      @Override
      public String getText(Object message) {
        var qualifier = ((IntentMessage<Void>) message).intent.qualifier;
        return new Gson().toJson(qualifier);
      }
    });
  }

  private void createTypeColumn(TableViewer messagesTableViewer) {
    var typeColumn = new TableViewerColumn(messagesTableViewer, SWT.NONE);
    typeColumn.getColumn().setText("Type");
    typeColumn.getColumn().setWidth(120);
    typeColumn.setLabelProvider(new ColumnLabelProvider() {

      @SuppressWarnings("unchecked")
      @Override
      public String getText(Object message) {
        return ((IntentMessage<Void>) message).intent.type;
      }
    });
  }

  private void createCapabilityIdColumn(TableViewer messagesTableViewer) {
    var idColumn = new TableViewerColumn(messagesTableViewer, SWT.NONE);
    idColumn.getColumn().setText("Capability Id");
    idColumn.getColumn().setWidth(120);
    idColumn.setLabelProvider(new ColumnLabelProvider() {

      @SuppressWarnings("unchecked")
      @Override
      public String getText(Object message) {
        var capability = ((IntentMessage<Void>) message).capability;
        return capability.metadata.id;
      }
    });
  }

  private void createHeadersColumn(TableViewer messagesTableViewer) {
    var appColumn = new TableViewerColumn(messagesTableViewer, SWT.NONE);
    appColumn.getColumn().setText("Headers");
    appColumn.getColumn().setWidth(120);
    appColumn.setLabelProvider(new ColumnLabelProvider() {

      @SuppressWarnings("unchecked")
      @Override
      public String getText(Object message) {
        var headers = ((IntentMessage<Void>) message).headers;
        return new Gson().toJson(headers);
      }
    });
  }

}