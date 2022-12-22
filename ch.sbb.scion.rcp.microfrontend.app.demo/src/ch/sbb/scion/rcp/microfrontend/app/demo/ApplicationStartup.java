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
  public void postContextCreate(MicrofrontendPlatform microfrontendPlatform) {
    microfrontendPlatform.startHost(new MicrofrontendPlatformConfig()
        .host(new HostConfig()
            .symbolicName("rcp-host")
            .manifest(new Manifest()
                .name("RCP Application")
                .intentions(List.of(new Intention()
                    .type("microfrontend")
                    .qualifier(new Qualifier().set("component", "devtools").set("vendor", "scion"))))))
        .applications(List.of(
            new ApplicationConfig()
                .symbolicName("client-app")
                .manifestUrl("http://localhost:4201/manifest.json"),
            new ApplicationConfig()
                .symbolicName("devtools")
                .manifestUrl("https://scion-microfrontend-platform-devtools-v1-0-0-rc-12.vercel.app/assets/manifest.json")
                .intentionCheckDisabled(true)
                .scopeCheckDisabled(true)))
        .manifestLoadTimeout(2000L)
        .activatorLoadTimeout(5000L));
  }
}
