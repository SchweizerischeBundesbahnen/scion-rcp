package ch.sbb.scion.rcp.microfrontend.e3.app.demo.menu;

import java.util.Optional;

import ch.sbb.scion.rcp.microfrontend.model.Capability;
import ch.sbb.scion.rcp.microfrontend.model.Properties;

public class ViewCapabilityMenuContributor extends CapabilityMenuContributor {

  @Override
  protected String getCapabilityType() {
    return "view";
  }

  @Override
  protected String getText(final Capability capability) {
    return (String) Optional.ofNullable(capability.properties()).orElseGet(Properties::new).get("title");
  }

  @Override
  protected String getToolTipText(final Capability capability) {
    return (String) Optional.ofNullable(capability.properties()).orElseGet(Properties::new).get("heading");
  }
}