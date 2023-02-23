package ch.sbb.scion.rcp.microfrontend.e3.app.demo;

import java.util.List;

import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;

import ch.sbb.scion.rcp.microfrontend.MicrofrontendPlatform;
import ch.sbb.scion.rcp.microfrontend.SciIntentClient;
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

public class ApplicationStartup {

  @PostContextCreate
  public void postContextCreate(final MicrofrontendPlatform microfrontendPlatform, final SciIntentClient intentClient) {
    microfrontendPlatform.startHost(new MicrofrontendPlatformConfig().host(new HostConfig().symbolicName("rcp-host")
        .manifest(new Manifest().name("RCP Application")
            .intentions(List.of(
                new Intention().type("microfrontend").qualifier(new Qualifier().set("component", "devtools").set("vendor", "scion")),
                new Intention().type("view").qualifier(new Qualifier().set("*", "*")),
                new Intention().type("tile").qualifier(new Qualifier().set("*", "*")),
                new Intention().type("popup").qualifier(new Qualifier().set("*", "*"))))
            .capabilities(List.of(
                new Capability().type("view").qualifier(new Qualifier().set("component", "messageClient")).isPrivate(Boolean.FALSE)
                    .properties(new Properties().set("title", "Message Client").set("heading", "Microfrontend Platform Test Page").set(
                        "eclipseViewId", "ch.sbb.scion.rcp.views.messageClient")),
                new Capability().type("view").qualifier(new Qualifier().set("component", "intentClient")).isPrivate(Boolean.FALSE)
                    .properties(new Properties().set("title", "Intent Client").set("heading", "Microfrontend Platform Test Page")
                        .set("eclipseViewId", "ch.sbb.scion.rcp.views.intentClient")),
                new Capability().type("view").qualifier(new Qualifier().set("component", "manifestService")).isPrivate(Boolean.FALSE)
                    .properties(new Properties().set("title", "Manifest Service").set("heading", "Microfrontend Platform Test Page")
                        .set("eclipseViewId", "ch.sbb.scion.rcp.views.manifestService")),
                new Capability().type("view").qualifier(new Qualifier().set("component", "outletRouter")).isPrivate(Boolean.FALSE)
                    .properties(new Properties().set("title", "Outlet Router").set("heading", "Microfrontend Platform Test Page")
                        .set("eclipseViewId", "ch.sbb.scion.rcp.views.outletRouter")),
                new Capability().type("view").qualifier(new Qualifier().set("component", "sciRouterOutlet"))
                    .properties(new Properties().set("title", "Router Outlet").set("heading", "Microfrontend Platform Test Page")
                        .set("eclipseViewId", "ch.sbb.scion.rcp.views.routerOutlet")),
                new Capability().type("view").qualifier(new Qualifier().set("component", "workbenchPopupService"))
                    .properties(new Properties().set("title", "Workbench Popup Service").set("heading", "Microfrontend Platform Test Page")
                        .set("eclipseViewId", "ch.sbb.scion.rcp.views.workbenchPopupService")),
                new Capability().type("popup").qualifier(new Qualifier().set("test", "eclipse-popup")).isPrivate(Boolean.FALSE).properties(
                    new Properties().set("title", "Eclipse Test Popup").set("eclipsePopupId", "ch.sbb.scion.rcp.popups.TestPopupDialog")
                        .set("size", new Properties().set("width", "500px").set("height", "600px"))),
                new Capability().type("view").qualifier(new Qualifier().set("test", "eclipse-view")).isPrivate(Boolean.FALSE)
                    .params(List.of(new ParamDefinition().name("example").required(Boolean.FALSE).description("An example parameter")))
                    .properties(new Properties().set("title", "Eclipse Test View").set("heading", "Eclipse Test View with input")
                        .set("eclipseViewId", "ch.sbb.scion.rcp.views.testView"))
            //
            ))))
        .applications(List.of(new ApplicationConfig().symbolicName("client-app").manifestUrl("http://localhost:4201/manifest.json"),
            new ApplicationConfig().symbolicName("tms-kast-demo").manifestUrl("https://localhost:4200/assets/manifest.json"),
            new ApplicationConfig().symbolicName("devtools")
                .manifestUrl("https://scion-microfrontend-platform-devtools-v1-0-0-rc-12.vercel.app/assets/manifest.json")
                .intentionCheckDisabled(Boolean.TRUE).scopeCheckDisabled(Boolean.TRUE)))
        .manifestLoadTimeout(Long.valueOf(2000L)).activatorLoadTimeout(Long.valueOf(5000L)));

    // Open SCION DevTools
    intentClient.publish(new Intent().type("view").qualifier(new Qualifier().set("component", "devtools").set("vendor", "scion")));
  }
}
