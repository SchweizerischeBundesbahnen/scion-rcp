package ch.sbb.scion.rcp.workbench.popup;

import java.util.Objects;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.sbb.scion.rcp.workbench.IWorkbenchPopupWindowProvider;

@Component(service = PopupRegistry.class)
public class PopupRegistry {

  private static final String EXTENSION_POINT_ID = "ch.sbb.scion.rcp.workbench.popupWindowProviders";

  @Reference
  private IExtensionRegistry extensionRegistry;

  public IWorkbenchPopupWindowProvider get(final String eclipsePopupId) {
    Objects.requireNonNull(eclipsePopupId);
    for (var configurationElement : extensionRegistry.getConfigurationElementsFor(EXTENSION_POINT_ID)) {
      if (eclipsePopupId.equals(configurationElement.getAttribute("popupId"))) {
        try {
          return (IWorkbenchPopupWindowProvider) configurationElement.createExecutableExtension("class");
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
    return null;
  }

}
