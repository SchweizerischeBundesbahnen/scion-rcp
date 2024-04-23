package ch.sbb.scion.rcp.microfrontend.e3.app.demo.view.model;

import java.util.Map.Entry;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;

import ch.sbb.scion.rcp.microfrontend.model.Intention;
import ch.sbb.scion.rcp.microfrontend.model.Qualifier;

public class IntentionModel {

  private final IObservableValue<String> type = new WritableValue<>("", String.class);

  private final IObservableList<Entry<String, String>> qualifiers = new WritableList<>();

  public IObservableValue<String> getType() {
    return type;
  }

  public IObservableList<Entry<String, String>> getQualifiers() {
    return qualifiers;
  }

  public void clearValues() {
    type.setValue("");
    qualifiers.clear();
  }

  public Intention getIntention() {
    var intentionBuilder = Intention.builder().type(type.getValue());
    if (!qualifiers.isEmpty()) {
      var qualifier = new Qualifier();
      qualifiers.stream().forEach(x -> qualifier.set(x.getKey(), x.getValue()));
      intentionBuilder.qualifier(qualifier);
    }
    return intentionBuilder.build();
  }
}
