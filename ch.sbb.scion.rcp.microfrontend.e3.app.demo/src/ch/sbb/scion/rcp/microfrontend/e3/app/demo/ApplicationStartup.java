package ch.sbb.scion.rcp.microfrontend.e3.app.demo;

import java.util.List;

import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;

import ch.sbb.scion.rcp.microfrontend.MicrofrontendPlatform;
import ch.sbb.scion.rcp.microfrontend.IntentClient;
import ch.sbb.scion.rcp.microfrontend.model.ApplicationConfig;
import ch.sbb.scion.rcp.microfrontend.model.Capability;
import ch.sbb.scion.rcp.microfrontend.model.Capability.ParamDefinition;
import ch.sbb.scion.rcp.microfrontend.model.HostConfig;
import ch.sbb.scion.rcp.microfrontend.model.Intent;
import ch.sbb.scion.rcp.microfrontend.model.Intention;
import ch.sbb.scion.rcp.microfrontend.model.Manifest;
import ch.sbb.scion.rcp.microfrontend.model.MicrofrontendPlatformConfig;
import ch.sbb.scion.rcp.microfrontend.model.Properties;
import ch.sbb.scion.rcp.microfrontend.model.Qualifier;
import ch.sbb.scion.rcp.workbench.WorkbenchPopupSize;

public class ApplicationStartup {

  @PostContextCreate
  public void postContextCreate(final MicrofrontendPlatform microfrontendPlatform, final IntentClient intentClient) {
    microfrontendPlatform.startHost(MicrofrontendPlatformConfig.builder()
        .host(HostConfig.builder().symbolicName("rcp-host")
            .manifest(Manifest.builder().name("RCP Application")
                .intentions(List.of(Intention.builder().type("microfrontend")
                    .qualifier(new Qualifier().set("component", "devtools").set("vendor", "scion")).build(),
                    Intention.builder().type("view").qualifier(new Qualifier().set("*", "*")).build(),
                    Intention.builder().type("tile").qualifier(new Qualifier().set("*", "*")).build(),
                    Intention.builder().type("popup").qualifier(new Qualifier().set("*", "*")).build()))
                .capabilities(List.of(
                    Capability
                        .builder().type("view").qualifier(new Qualifier().set("component", "messageClient")).isPrivate(Boolean.FALSE)
                        .properties(new Properties().set("title", "Message Client").set("heading", "Microfrontend Platform Test Page")
                            .set("eclipseViewId", "ch.sbb.scion.rcp.views.messageClient"))
                        .build(),
                    Capability.builder().type("view").qualifier(new Qualifier().set("component", "intentClient")).isPrivate(Boolean.FALSE)
                        .properties(new Properties()
                            .set("title", "Intent Client").set("heading", "Microfrontend Platform Test Page")
                            .set("eclipseViewId", "ch.sbb.scion.rcp.views.intentClient"))
                        .build(),
                    Capability.builder().type("view").qualifier(new Qualifier().set("component", "manifestService"))
                        .isPrivate(Boolean.FALSE)
                        .properties(new Properties().set("title", "Manifest Service").set("heading", "Microfrontend Platform Test Page")
                            .set("eclipseViewId", "ch.sbb.scion.rcp.views.manifestService"))
                        .build(),
                    Capability.builder().type("view").qualifier(new Qualifier().set("component", "outletRouter")).isPrivate(Boolean.FALSE)
                        .properties(new Properties().set("title", "Outlet Router").set("heading", "Microfrontend Platform Test Page")
                            .set("eclipseViewId", "ch.sbb.scion.rcp.views.outletRouter"))
                        .build(),
                    Capability.builder().type("view").qualifier(new Qualifier().set("component", "sciRouterOutlet"))
                        .properties(new Properties().set("title", "Router Outlet").set("heading", "Microfrontend Platform Test Page")
                            .set("eclipseViewId", "ch.sbb.scion.rcp.views.routerOutlet"))
                        .build(),
                    Capability.builder().type("view").qualifier(new Qualifier().set("component", "workbenchPopupService"))
                        .properties(
                            new Properties().set("title", "Workbench Popup Service").set("heading", "Microfrontend Platform Test Page")
                                .set("eclipseViewId", "ch.sbb.scion.rcp.views.workbenchPopupService"))
                        .build(),
                    Capability.builder().type("popup").qualifier(new Qualifier().set("test", "eclipse-popup")).isPrivate(Boolean.FALSE)
                        .properties(new Properties().set("title", "Eclipse Test Popup")
                            .set("eclipsePopupId", "ch.sbb.scion.rcp.popups.TestPopupDialog")
                            .set("size", WorkbenchPopupSize.builder().width("500px").height("600px").build()))
                        .build(),
                    Capability.builder().type("view").qualifier(new Qualifier().set("test", "eclipse-view")).isPrivate(Boolean.FALSE)
                        .params(List.of(
                            ParamDefinition.builder().name("example").required(Boolean.FALSE).description("An example parameter").build()))
                        .properties(new Properties().set("title", "Eclipse Test View").set("heading", "Eclipse Test View with input")
                            .set("eclipseViewId", "ch.sbb.scion.rcp.views.testView"))
                        .build()
                //
                ))).build())
        .applications(
            List.of(ApplicationConfig.builder().symbolicName("client-app").manifestUrl("http://localhost:4201/manifest.json").build(),
                ApplicationConfig.builder().symbolicName("tms-kast-demo")
                    .manifestUrl("https://tms-kast-demo-dev.sbb-cloud.net/assets/manifest.json").build(),
                ApplicationConfig.builder().symbolicName("devtools")
                    .manifestUrl("https://scion-microfrontend-platform-devtools-v1-0-0-rc-12.vercel.app/assets/manifest.json")
                    .intentionCheckDisabled(Boolean.TRUE).scopeCheckDisabled(Boolean.TRUE).build()))
        .manifestLoadTimeout(Long.valueOf(2000L)).activatorLoadTimeout(Long.valueOf(5000L)).build());

    // Open SCION DevTools
    intentClient
        .publish(Intent.builder().type("view").qualifier(new Qualifier().set("component", "devtools").set("vendor", "scion")).build());
  }
}
