package ch.sbb.scion.rcp.microfrontend.e3.app.demo.view.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;

import ch.sbb.scion.rcp.microfrontend.model.Qualifier;
import ch.sbb.scion.rcp.workbench.SciWorkbenchPopupConfig;
import ch.sbb.scion.rcp.workbench.SciWorkbenchPopupOrigin;

public class WorkbenchPopupIntentMessageModel {

  private final String viewId = UUID.randomUUID().toString();

  private final IObservableList<Entry<String, String>> qualifiers = new WritableList<>();

  private final IObservableList<Entry<String, String>> params = new WritableList<>();

  private final IObservableValue<Boolean> closeOnEscape = new WritableValue<>();

  private final IObservableValue<Boolean> closeOnFocusLost = new WritableValue<>();

  private final IObservableValue<Boolean> useTopLeftAnchor = new WritableValue<>();

  private final IObservableValue<String> anchorTop = new WritableValue<>();

  private final IObservableValue<String> anchorLeft = new WritableValue<>();

  public WorkbenchPopupIntentMessageModel() {
    // Add some initial values for convenience:
    qualifiers.add(Map.entry("test", "workbench-popup"));
    params.add(Map.entry("id", "123"));
    closeOnEscape.setValue(Boolean.TRUE);
    closeOnFocusLost.setValue(Boolean.TRUE);
    useTopLeftAnchor.setValue(Boolean.FALSE);
  }

  public String getViewId() {
    return viewId;
  }

  public IObservableList<Entry<String, String>> getQualifiers() {
    return qualifiers;
  }

  public IObservableList<Entry<String, String>> getParams() {
    return params;
  }

  public IObservableValue<Boolean> isCloseOnEscape() {
    return closeOnEscape;
  }

  public IObservableValue<Boolean> isCloseOnFocusLost() {
    return closeOnFocusLost;
  }

  public IObservableValue<Boolean> isUseTopLeftAnchor() {
    return useTopLeftAnchor;
  }

  public IObservableValue<String> getAnchorTop() {
    return anchorTop;
  }

  public IObservableValue<String> getAnchorLeft() {
    return anchorLeft;
  }

  public Qualifier getQualifier() {
    var qualifier = new Qualifier();
    if (!qualifiers.isEmpty()) {
      qualifiers.stream().forEach(x -> qualifier.set(x.getKey(), x.getValue()));
    }
    return qualifier;
  }

  public SciWorkbenchPopupConfig getConfig() {
    var config = new SciWorkbenchPopupConfig();
    config.params(computeParams());
    config.closeOnEscape(closeOnEscape.getValue());
    config.closeOnFocusLost(closeOnFocusLost.getValue());
    config.viewId(viewId);
    config.anchor(getAnchor());
    return config;
  }

  private HashMap<String, Object> computeParams() {
    var paramsDict = new HashMap<String, Object>();
    if (!params.isEmpty()) {
      params.forEach(entry -> paramsDict.put(entry.getKey(), entry.getValue()));
    }
    return paramsDict;
  }

  private SciWorkbenchPopupOrigin getAnchor() {
    var anchor = new SciWorkbenchPopupOrigin();
    if (useTopLeftAnchor.getValue().booleanValue()) {
      anchor.top = safeToInteger(anchorTop.getValue());
      anchor.left = safeToInteger(anchorLeft.getValue());
    }
    return anchor;
  }

  private static Integer safeToInteger(final String valueAsString) {
    if (valueAsString == null) {
      return null;
    }
    try {
      return Integer.valueOf(valueAsString);
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

}
