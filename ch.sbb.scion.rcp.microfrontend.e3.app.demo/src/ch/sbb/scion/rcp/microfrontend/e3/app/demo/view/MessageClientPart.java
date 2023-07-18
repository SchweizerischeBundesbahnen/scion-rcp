package ch.sbb.scion.rcp.microfrontend.e3.app.demo.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import java.text.SimpleDateFormat;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.widgets.ButtonFactory;
import org.eclipse.jface.widgets.GroupFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.jface.widgets.TextFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

import com.google.gson.Gson;

import ch.sbb.scion.rcp.microfrontend.MessageClient;
import ch.sbb.scion.rcp.microfrontend.model.MessageHeaders;
import ch.sbb.scion.rcp.microfrontend.model.PublishOptions;
import ch.sbb.scion.rcp.microfrontend.model.TopicMessage;
import ch.sbb.scion.rcp.microfrontend.subscriber.ISubscription;

public class MessageClientPart {

  private static final String NAN = "NaN";

  @Inject
  private MessageClient messageClient;

  @PostConstruct
  public void createComposite(final Composite parent) {
    var composite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.swtDefaults().spacing(5, 0).applyTo(composite);

    var publishGroup = createPublishGroup(composite);
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(publishGroup);

    var subscribeGroup = createSubscribeGroup(composite);
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(subscribeGroup);

    var subscriberCountGroup = createSubscriberCountGroup(composite);
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BOTTOM).grab(true, false).applyTo(subscriberCountGroup);
  }

  private Composite createPublishGroup(final Composite parent) {
    var group = GroupFactory.newGroup(SWT.NONE).text("Publish").create(parent);
    GridLayoutFactory.swtDefaults().numColumns(2).margins(5, 10).spacing(20, 7).applyTo(group);

    // Topic
    LabelFactory.newLabel(SWT.NONE).text("Topic")
        .layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).align(SWT.BEGINNING, SWT.CENTER).create()).create(group);
    var topic = TextFactory.newText(SWT.SINGLE | SWT.BORDER).layoutData(GridDataFactory.fillDefaults().grab(true, false).create())
        .create(group);

    // Message
    LabelFactory.newLabel(SWT.NONE).text("Message").layoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).create())
        .create(group);
    var message = TextFactory.newText(SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL)
        .layoutData(GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 100).create()).create(group);

    // Retain flag
    LabelFactory.newLabel(SWT.NONE).layoutData(GridDataFactory.fillDefaults().create()).create(group);
    var retain = ButtonFactory.newButton(SWT.CHECK).text("Retain").layoutData(GridDataFactory.fillDefaults().create()).create(group);

    // Publish button
    ButtonFactory.newButton(SWT.NONE).text("Publish")
        .onSelect(e -> messageClient.publish(topic.getText(), message.getText(), new PublishOptions(retain.getSelection())))
        .layoutData(GridDataFactory.fillDefaults().span(2, 1).create()).create(group);

    return group;
  }

  private Composite createSubscribeGroup(final Composite parent) {
    var group = GroupFactory.newGroup(SWT.NONE).text("Subscribe").create(parent);
    GridLayoutFactory.swtDefaults().numColumns(2).margins(5, 10).spacing(20, 7).applyTo(group);

    // Topic
    LabelFactory.newLabel(SWT.NONE).text("Topic")
        .layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).align(SWT.BEGINNING, SWT.CENTER).create()).create(group);
    var topic = TextFactory.newText(SWT.SINGLE | SWT.BORDER).layoutData(GridDataFactory.fillDefaults().grab(true, false).create())
        .create(group);

    // Subscribe button
    var subscribeButton = ButtonFactory.newButton(SWT.NONE).text("Subscribe").layoutData(GridDataFactory.fillDefaults().span(2, 1).create())
        .create(group);

    var messages = new ArrayList<TopicMessage<String>>();
    var messagesTableViewer = new TableViewer(group);
    messagesTableViewer.getTable().setHeaderVisible(true);
    messagesTableViewer.setContentProvider(ArrayContentProvider.getInstance());
    createMessageColumn(messagesTableViewer);
    createTimestampColumn(messagesTableViewer);
    createMessageHeadersColumn(messagesTableViewer);
    createTopicParamsColumn(messagesTableViewer);
    GridDataFactory.swtDefaults().span(2, 1).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(messagesTableViewer.getControl());

    messagesTableViewer.setInput(Collections.emptyList());

    var subscription = new AtomicReference<ISubscription>();
    subscribeButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        if (subscription.get() == null) {
          subscription.set(messageClient.subscribe(topic.getText(), message -> {
            messages.add(0, message);
            messagesTableViewer.setInput(new ArrayList<>(messages));
          }));
          subscribeButton.setText("Unsubscribe");
        }
        else {
          messages.clear();
          messagesTableViewer.setInput(Collections.emptyList());
          subscription.getAndSet(null).unsubscribe();
          subscribeButton.setText("Subscribe");
        }
        topic.setEnabled(subscription.get() == null);
      }
    });

    return group;
  }

  private void createMessageHeadersColumn(final TableViewer messagesTableViewer) {
    TableViewerColumn headersColumn = new TableViewerColumn(messagesTableViewer, SWT.NONE);
    headersColumn.getColumn().setText("Message Headers");
    headersColumn.getColumn().setWidth(200);
    headersColumn.setLabelProvider(new ColumnLabelProvider() {

      @Override
      public String getText(final Object object) {
        TopicMessage<String> topicMessage = getTopicMessageFromObject(object);
        var params = topicMessage.headers();
        return new Gson().toJson(params);
      }
    });
  }

  private void createTopicParamsColumn(final TableViewer messagesTableViewer) {
    TableViewerColumn paramsColumn = new TableViewerColumn(messagesTableViewer, SWT.NONE);
    paramsColumn.getColumn().setText("Topic Params");
    paramsColumn.getColumn().setWidth(200);
    paramsColumn.setLabelProvider(new ColumnLabelProvider() {

      @Override
      public String getText(final Object object) {
        TopicMessage<String> topicMessage = getTopicMessageFromObject(object);
        var params = topicMessage.params();
        return new Gson().toJson(params);
      }
    });
  }

  private void createTimestampColumn(final TableViewer messagesTableViewer) {
    TableViewerColumn timestampColumn = new TableViewerColumn(messagesTableViewer, SWT.NONE);
    timestampColumn.getColumn().setText("Timestamp");
    timestampColumn.getColumn().setWidth(150);
    timestampColumn.setLabelProvider(new ColumnLabelProvider() {

      @Override
      public String getText(final Object object) {
        TopicMessage<String> topicMessage = getTopicMessageFromObject(object);
        var timestamp = (Double) topicMessage.headers().get(MessageHeaders.TIMESTAMP.value);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Long.valueOf(timestamp.longValue()));
      }
    });
  }

  private void createMessageColumn(final TableViewer messagesTableViewer) {
    TableViewerColumn messageColumn = new TableViewerColumn(messagesTableViewer, SWT.NONE);
    messageColumn.getColumn().setText("Message");
    messageColumn.getColumn().setWidth(250);
    messageColumn.setLabelProvider(new ColumnLabelProvider() {

      @Override
      public String getText(final Object object) {
        TopicMessage<String> topicMessage = getTopicMessageFromObject(object);
        return topicMessage.body();
      }
    });
  }

  private Composite createSubscriberCountGroup(final Composite parent) {
    var group = GroupFactory.newGroup(SWT.NONE).text("Subscriber count").create(parent);
    GridLayoutFactory.swtDefaults().numColumns(2).margins(5, 10).spacing(20, 7).applyTo(group);

    // Topic
    LabelFactory.newLabel(SWT.NONE).text("Topic")
        .layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).align(SWT.BEGINNING, SWT.CENTER).create()).create(group);
    var topic = TextFactory.newText(SWT.SINGLE | SWT.BORDER).layoutData(GridDataFactory.fillDefaults().grab(true, false).create())
        .create(group);

    // Count
    LabelFactory.newLabel(SWT.NONE).text("Count")
        .layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).align(SWT.BEGINNING, SWT.CENTER).create()).create(group);
    var count = LabelFactory.newLabel(SWT.NONE).text(NAN)
        .layoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).align(SWT.BEGINNING, SWT.CENTER).create()).create(group);

    // Subscribe button
    var subscribeButton = ButtonFactory.newButton(SWT.NONE).text("Subscribe").layoutData(GridDataFactory.fillDefaults().span(2, 1).create())
        .create(group);

    var subscription = new AtomicReference<ISubscription>();
    subscribeButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        if (subscription.get() == null) {
          subscription.set(messageClient.subscribeToSubscriberCount(topic.getText(), countNumber -> {
            var countAsString = countNumber == null ? NAN : String.valueOf(countNumber.intValue());
            count.setText(countAsString);
          }));
          subscribeButton.setText("Unsubscribe from subscriber count");
        }
        else {
          count.setText(NAN);
          subscription.getAndSet(null).unsubscribe();
          subscribeButton.setText("Subscribe to subscriber count");
        }
        topic.setEnabled(subscription.get() == null);
      }
    });

    return group;
  }

  @SuppressWarnings("unchecked") // due to the fact that those ColumnLabelProviders are not type safe
  private static final TopicMessage<String> getTopicMessageFromObject(final Object o) {
    return (TopicMessage<String>) o;
  }
}