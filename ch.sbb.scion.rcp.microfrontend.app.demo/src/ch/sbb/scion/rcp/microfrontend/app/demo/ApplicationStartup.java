package ch.sbb.scion.rcp.microfrontend.app.demo;

import java.util.List;

import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;

import ch.sbb.scion.rcp.microfrontend.MicrofrontendPlatform;
import ch.sbb.scion.rcp.microfrontend.model.ApplicationConfig;
import ch.sbb.scion.rcp.microfrontend.model.MicrofrontendPlatformConfig;

public class ApplicationStartup {

  @PostContextCreate
  public void postContextCreate(MicrofrontendPlatform microfrontendPlatform) {
    microfrontendPlatform.startHost(new MicrofrontendPlatformConfig()
        .applications(List.of(
            new ApplicationConfig()
                .symbolicName("client-app")
                .manifestUrl("http://localhost:4201/manifest.json"),
            new ApplicationConfig()
                .symbolicName("devtools")
                .manifestUrl("http://localhost:5200/assets/manifest.json")
                .intentionCheckDisabled(true)
                .scopeCheckDisabled(true))));
  }
}
