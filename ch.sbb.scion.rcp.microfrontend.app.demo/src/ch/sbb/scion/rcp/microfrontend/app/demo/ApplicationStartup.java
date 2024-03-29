package ch.sbb.scion.rcp.microfrontend.app.demo;

import java.util.List;

import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;

import ch.sbb.scion.rcp.microfrontend.MicrofrontendPlatform;
import ch.sbb.scion.rcp.microfrontend.model.ApplicationConfig;
import ch.sbb.scion.rcp.microfrontend.model.HostConfig;
import ch.sbb.scion.rcp.microfrontend.model.Intention;
import ch.sbb.scion.rcp.microfrontend.model.Manifest;
import ch.sbb.scion.rcp.microfrontend.model.MicrofrontendPlatformConfig;
import ch.sbb.scion.rcp.microfrontend.model.Qualifier;

public class ApplicationStartup {

  @PostContextCreate
  public void postContextCreate(final MicrofrontendPlatform microfrontendPlatform) {
    microfrontendPlatform.startHost(MicrofrontendPlatformConfig.builder()
        .host(HostConfig.builder().symbolicName("rcp-host")
            .manifest(Manifest.builder().name("RCP Application")
                .intentions(List.of(Intention.builder().type("microfrontend")
                    .qualifier(new Qualifier().set("component", "devtools").set("vendor", "scion")).build())))
            .build())
        .applications(
            List.of(ApplicationConfig.builder().symbolicName("client-app").manifestUrl("http://localhost:4201/manifest.json").build(),
                ApplicationConfig.builder().symbolicName("devtools")
                    .manifestUrl("https://scion-microfrontend-platform-devtools-v1-0-0-rc-12.vercel.app/assets/manifest.json")
                    .intentionCheckDisabled(Boolean.TRUE).scopeCheckDisabled(Boolean.TRUE).build()))
        .manifestLoadTimeout(Long.valueOf(2000L)).activatorLoadTimeout(Long.valueOf(5000L)).build());
  }
}
