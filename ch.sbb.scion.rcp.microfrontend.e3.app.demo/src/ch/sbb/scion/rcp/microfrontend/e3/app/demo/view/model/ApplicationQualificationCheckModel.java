package ch.sbb.scion.rcp.microfrontend.e3.app.demo.view.model;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;

public class ApplicationQualificationCheckModel {

  private final IObservableValue<String> capabilityId = new WritableValue<>("", String.class);

  private final IObservableValue<String> appSymbolicName = new WritableValue<>("", String.class);

  public IObservableValue<String> getCapabilityId() {
    return capabilityId;
  }

  public IObservableValue<String> getAppSymbolicName() {
    return appSymbolicName;
  }

  public void clearValues() {
    capabilityId.setValue("");
    appSymbolicName.setValue("");
  }

  public AppSymbolicNameAndCapabilityId getAppSymbolicNameAndCapabilityId() {
    return new AppSymbolicNameAndCapabilityId(appSymbolicName.getValue(), capabilityId.getValue());
  }

  public record AppSymbolicNameAndCapabilityId(String appSymbolicName, String capabilityId) {

  }
}
