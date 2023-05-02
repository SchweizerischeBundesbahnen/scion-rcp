package ch.sbb.scion.rcp.microfrontend.e3.app.demo.menu;

import ch.sbb.scion.rcp.microfrontend.model.Capability;

public class TileCapabilityMenuContributor extends CapabilityMenuContributor {

  @Override
  protected String getCapabilityType() {
    return "tile";
  }

  @Override
  protected String getText(final Capability capability) {
    return (String) capability.properties().get("title");
  }

  @Override
  protected String getToolTipText(final Capability capability) {
    return (String) capability.properties().get("description");
  }
}