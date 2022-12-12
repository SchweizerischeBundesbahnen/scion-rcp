package ch.sbb.scion.rcp.microfrontend.e3.app.demo.view.model;

import java.util.Map.Entry;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;

import ch.sbb.scion.rcp.microfrontend.SciManifestService.ManifestObjectFilter;
import ch.sbb.scion.rcp.microfrontend.model.Qualifier;

public class ManifestObjectFilterModel {

  private final IObservableValue<String> id = new WritableValue<>("", String.class);

  private final IObservableValue<String> type = new WritableValue<>("", String.class);

  private final IObservableList<Entry<String, String>> qualifiers = new WritableList<>();

  private final IObservableValue<String> appSymbolicName = new WritableValue<>("", String.class);

  public IObservableValue<String> getType() {
    return type;
  }

  public IObservableValue<String> getId() {
    return id;
  }

  public IObservableList<Entry<String, String>> getQualifiers() {
    return qualifiers;
  }

  public IObservableValue<String> getAppSymbolicName() {
    return appSymbolicName;
  }

  public void clearValues() {
    id.setValue("");
    type.setValue("");
    qualifiers.clear();
    appSymbolicName.setValue("");
  }

  public ManifestObjectFilter getFilter() {
    var filter = new ManifestObjectFilter();
    if (!id.getValue().isEmpty()) {
      filter.id(id.getValue());
    }
    if (!type.getValue().isEmpty()) {
      filter.type(type.getValue());
    }
    if(!qualifiers.isEmpty()) {
      var qualifier = new Qualifier();
      qualifiers.stream().forEach(x -> qualifier.add(x.getKey(), x.getValue()));
      filter.qualifier(qualifier);
    }
    if(!appSymbolicName.getValue().isEmpty()) {
      filter.appSymbolicName(appSymbolicName.getValue());
    }

    return filter;
  }

}
