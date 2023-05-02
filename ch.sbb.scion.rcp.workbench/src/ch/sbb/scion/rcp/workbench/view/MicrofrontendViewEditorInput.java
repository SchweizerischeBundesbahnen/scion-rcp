package ch.sbb.scion.rcp.workbench.view;

import java.util.Objects;
import java.util.UUID;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import ch.sbb.scion.rcp.microfrontend.model.Capability;
import ch.sbb.scion.rcp.microfrontend.model.Capability.ParamDefinition;
import ch.sbb.scion.rcp.microfrontend.model.Intent;

/**
 * Represents the input for a {@link MicrofrontendViewEditorPart}.
 */
public class MicrofrontendViewEditorInput implements IEditorInput {

  public final String sciViewId;
  public final Intent intent;
  public final Capability capability;

  public MicrofrontendViewEditorInput(final Capability capability, final Intent intent) {
    this(UUID.randomUUID().toString(), capability, intent);
  }

  public MicrofrontendViewEditorInput(final String sciViewId, final Capability capability, final Intent intent) {
    this.sciViewId = sciViewId;
    this.capability = capability;
    this.intent = intent;
  }

  public MicrofrontendViewEditorInput copyWithIntent(final Intent intent) {
    return new MicrofrontendViewEditorInput(sciViewId, capability, intent);
  }

  /**
   * Tests whether given intent matches this input.
   */
  public boolean matches(final Capability capability, final Intent intent) {
    // Test whether the capability matches this input.
    if (!capability.metadata().id().equals(this.capability.metadata().id())) {
      return false;
    }

    // Test whether all "navigational" params match this input.
    return capability.params().stream().filter(ParamDefinition::required).map(ParamDefinition::name)
        .allMatch(param -> intent.params().get(param).equals(this.intent.params().get(param)));
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
  public int hashCode() {
    return Objects.hash(sciViewId);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MicrofrontendViewEditorInput other = (MicrofrontendViewEditorInput) obj;
    return Objects.equals(sciViewId, other.sciViewId);
  }
}
