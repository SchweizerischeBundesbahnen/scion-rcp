/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.workbench.view;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.valueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.sbb.scion.rcp.microfrontend.model.Capability;
import ch.sbb.scion.rcp.microfrontend.model.Capability.Metadata;

@Disabled(value = "We need to find a way to run eclipse ui tests on github build infrastructure")
class MicrofrontendViewEditorInputTest {

  @Test
  void matches_shouldReturnFalse_ifMetadataDoesntMatch() {
    // given
    MicrofrontendViewEditorInput editorInput = new MicrofrontendViewEditorInput(createCapability("Id 1"), null);

    boolean matches = editorInput.matches(createCapability("Id 2"), null);

    assertThat(valueOf(matches), is(FALSE));
  }

  private Capability createCapability(final String metaDataId) {
    Capability capability = Capability.builder().metadata(Metadata.builder().id(metaDataId).build()).build();
    return capability;
  }
}