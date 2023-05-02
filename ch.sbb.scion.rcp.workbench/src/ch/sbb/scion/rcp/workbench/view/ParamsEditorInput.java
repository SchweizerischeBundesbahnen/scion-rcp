package ch.sbb.scion.rcp.workbench.view;

import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * Input passed to an Eclipse editor.
 */
public class ParamsEditorInput implements IEditorInput {

  public Map<String, ?> params;

  public ParamsEditorInput(Map<String, ?> params) {
    this.params = params;
  }

  @Override
  public <T> T getAdapter(Class<T> adapter) {
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
}
