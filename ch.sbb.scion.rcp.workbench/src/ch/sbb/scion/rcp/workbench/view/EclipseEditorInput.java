package ch.sbb.scion.rcp.workbench.view;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import ch.sbb.scion.rcp.workbench.IWorkbenchViewInput;

/**
 * Input passed to an Eclipse editor.
 */
public class EclipseEditorInput implements IEditorInput, IWorkbenchViewInput {

  private final String capabilityId;
  private final Map<String, Object> params;

  public EclipseEditorInput(final String capabilityId, final Map<String, Object> params) {
    this.capabilityId = Objects.requireNonNull(capabilityId);
    this.params = Map.copyOf(params);
  }

  @Override
  public <T> T getAdapter(final Class<T> adapter) {
    return null;
  }

  @Override
  public boolean exists() {
    return false;
  }

  @Override
  public ImageDescriptor getImageDescriptor() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public IPersistableElement getPersistable() {
    return null;
  }

  @Override
  public String getToolTipText() {
    return null;
  }

  @Override
  public String getCapabilityId() {
    return capabilityId;
  }

  @Override
  public Map<String, Object> getParams() {
    return params;
  }
}
