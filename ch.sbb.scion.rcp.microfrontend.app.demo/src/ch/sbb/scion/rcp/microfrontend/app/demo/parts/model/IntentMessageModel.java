package ch.sbb.scion.rcp.microfrontend.app.demo.parts.model;

import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;

import ch.sbb.scion.rcp.microfrontend.model.Intent;
import ch.sbb.scion.rcp.microfrontend.model.Qualifier;

public class IntentMessageModel {

  private final IObservableValue<String> type = new WritableValue<>("", String.class);

  private final IObservableList<Entry<String, String>> qualifiers = new WritableList<>();

  private final IObservableList<Entry<String, String>> params = new WritableList<>();

  private final IObservableList<Entry<String, String>> headers = new WritableList<>();

  private final IObservableValue<String> message = new WritableValue<>("", String.class);

  private final IObservableValue<Boolean> requestReply = new WritableValue<>(Boolean.FALSE, Boolean.class);

  private final IObservableValue<Boolean> retain = new WritableValue<>(Boolean.FALSE, Boolean.class);

  public IObservableValue<String> getType() {
    return type;
  }

  public IObservableValue<String> getMessage() {
    return message;
  }

  public IObservableValue<Boolean> isRequestReply() {
    return requestReply;
  }

  public IObservableValue<Boolean> isRetain() {
    return retain;
  }

  public IObservableList<Entry<String, String>> getQualifiers() {
    return qualifiers;
  }

  public IObservableList<Entry<String, String>> getParams() {
    return params;
  }

  public IObservableList<Entry<String, String>> getHeaders() {
    return headers;
  }

  public Intent getIntent() {
    var intentBuilder = Intent.builder().type(type.getValue());
    if (!qualifiers.isEmpty()) {
      var qualifier = new Qualifier();
      qualifiers.stream().forEach(x -> qualifier.set(x.getKey(), x.getValue()));
      intentBuilder.qualifier(qualifier);
    }

    if (!params.isEmpty()) {
      intentBuilder.params(params.stream().collect(Collectors.toMap(x -> x.getKey(), x -> (Object) x.getValue())));
    }

    return intentBuilder.build();
  }

}
