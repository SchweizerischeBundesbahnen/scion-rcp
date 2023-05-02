package ch.sbb.scion.rcp.microfrontend.app.demo.parts.model;

import java.util.Map.Entry;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;

import ch.sbb.scion.rcp.microfrontend.IntentClient.IntentSelector;
import ch.sbb.scion.rcp.microfrontend.model.Qualifier;

public class IntentSelectorModel {

  private final IObservableValue<String> type = new WritableValue<>("", String.class);

  private final IObservableList<Entry<String, String>> qualifiers = new WritableList<>();

  public IObservableValue<String> getType() {
    return type;
  }

  public IObservableList<Entry<String, String>> getQualifiers() {
    return qualifiers;
  }

  public IntentSelector getSelector() {
    var selector = IntentSelector.builder();
    if (!type.getValue().isEmpty()) {
      selector.type(type.getValue());
    }
    if (!qualifiers.isEmpty()) {
      var qualifier = new Qualifier();
      qualifiers.stream().forEach(x -> qualifier.set(x.getKey(), x.getValue()));
      selector.qualifier(qualifier);
    }

    return selector.build();
  }

}
