package ch.sbb.scion.rcp.microfrontend.app.demo.parts.model;

import java.util.Map.Entry;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;

import ch.sbb.scion.rcp.microfrontend.model.Capability;
import ch.sbb.scion.rcp.microfrontend.model.Capability.ParamDefinition;
import ch.sbb.scion.rcp.microfrontend.model.Qualifier;

public class CapabilityModel {

  private final IObservableValue<String> type = new WritableValue<>("", String.class);

  private final IObservableValue<String> description = new WritableValue<>("", String.class);

  private final IObservableValue<Boolean> isPrivate = new WritableValue<>(Boolean.TRUE, Boolean.class);

  private final IObservableList<Entry<String, String>> qualifiers = new WritableList<>();

  private final IObservableList<ParamDefinition> params = new WritableList<>();

  public IObservableValue<String> getType() {
    return type;
  }

  public IObservableValue<String> getDescription() {
    return description;
  }

  public IObservableValue<Boolean> getIsPrivate() {
    return isPrivate;
  }

  public IObservableList<Entry<String, String>> getQualifiers() {
    return qualifiers;
  }

  public IObservableList<ParamDefinition> getParams() {
    return params;
  }

  public void clearValues() {
    type.setValue("");
    description.setValue("");
    isPrivate.setValue(Boolean.TRUE);
    qualifiers.clear();
    params.clear();
  }

  public Capability getCapability() {
    var capabilityBuilder = Capability.builder().type(type.getValue());
    if (!description.getValue().isEmpty()) {
      capabilityBuilder.description(description.getValue());
    }
    capabilityBuilder.isPrivate(isPrivate.getValue());
    if (!qualifiers.isEmpty()) {
      var qualifier = new Qualifier();
      qualifiers.stream().forEach(x -> qualifier.set(x.getKey(), x.getValue()));
      capabilityBuilder.qualifier(qualifier);
    }
    if (!params.isEmpty()) {
      capabilityBuilder.params(params);
    }
    return capabilityBuilder.build();
  }
}
