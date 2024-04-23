/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.microfrontend.model;

/**
 * Identifies a capability
 */
public interface CapabilityIdentifier {

  /**
   * @return the id of the represented capability, not null
   */
  String capabilityId();
}
