/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.workbench.view;

import java.util.Map;
import java.util.Objects;

import ch.sbb.scion.rcp.workbench.IWorkbenchViewInput;

public class EclipseViewInput implements IWorkbenchViewInput {

  private final String capabilityId;
  private final Map<String, Object> params;

  public EclipseViewInput(final String capabilityId, final Map<String, Object> params) {
    this.capabilityId = Objects.requireNonNull(capabilityId);
    this.params = Map.copyOf(params);
  }

  @Override
  public String getCapabilityId() {
    return this.capabilityId;
  }

  @Override
  public Map<String, Object> getParams() {
    return this.params;
  }
}
