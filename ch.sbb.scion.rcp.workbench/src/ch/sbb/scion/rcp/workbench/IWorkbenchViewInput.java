package ch.sbb.scion.rcp.workbench;

import java.util.Map;

/**
 * Allows injecting intent information into an Eclipse IViewPart that is opened through a Scion workbench view intent. An instance of this
 * interface will be available in the dependency injection context of the workbench window that contains the view part.
 */
public interface IWorkbenchViewInput {

  String getCapabilityId();

  Map<String, Object> getParams();
}
