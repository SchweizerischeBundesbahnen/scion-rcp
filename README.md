# SCION Eclipse RCP Integration

This integration of the [SCION Microfrontend Platform](https://github.com/SchweizerischeBundesbahnen/scion-microfrontend-platform) with [Eclipse RCP](https://projects.eclipse.org/projects/eclipse.platform) allows embedding web-based microfrontends in native rich client applications.
Hereby an Eclipse RCP application serves as the SCION Microfrontend host providing the exact same host API to the microfrontends as they would be embedded in a web-based SCION workbench. To integrate with the existing RCP applications there is a Java API to interact with the SCION Microfrontend Platform.

This project aims to provide a gradual transition from a largescale Eclipse RCP application to web-based frontends while maintaining a single integrated application for the user.

## Project Status

The project is in a early prototype status. We're currently challenging it with key use cases at Swiss Federal Railroads (SBB). There are still open issues, the API and implementation will most likely change before a first release.

## Contributions

We don't have a formal contribution guideline yet. Please feel free to open an issue to discuss ideas and get in touch with the development team.

## License

This project is provided under the [Eclipse Public License 2.0](LICENSE)

## HowTo

### Set up the project in eclipse
Import repository clone root folder as "Existing Projects into Workspace" and select "Search for nested projects" before clicking "Finish".

#### Lombok
We use Lombok to generate boilerplate. Lombok is compatible with various IDEs, find the setup instructions here [Lombok Eclipse Setup](https://projectlombok.org/setup/eclipse).

:information_source: A remark about building the project from the command line with Maven: Lombok must be loaded as a Java Agent to do its magic. As we use Tycho for our build, this can be achieved either by setting an environment variable `MAVEN_OPTS=-javaagent:<path-to-lombok-jar>` or, since Maven 3.3.1, via a jvm.config file that contains `-javaagent:<path-to-lombok-jar>`, see [Configuring Apache Maven](https://maven.apache.org/configure.html). The Lombok jar can either be the one that was downloaded for setting up Lombok, or the one in the local m2 repository, assuming the project dependencies were already resolved.

### Build & Release
See [build.yml](.github/workflows/build.yml) for further infos about the build.
See [release.yml](.github/workflows/release.yml) for further infos about the release.
 

## Headless UI Testing with SWTBot (JUnit5)

Headless testing on github doesn't work. SWTBot test can only be run locally.
SWTTest bundles are organized in the ch.sbb.scion.rcp.swtbot.feature which must be 
added to the product to be tested.

### Launching on win32/x86_64

java -Xms256M -Xmx768M \
-jar ch.sbb.scion.rcp.microfrontend.e3.app.demo.product/target/products/ch.sbb.scion.rcp.microfrontend.e3.app.demo.product/win32/win32/x86_64/plugins/org.eclipse.equinox.launcher_1.6.400.v20210924-0641.jar \
-application org.eclipse.swtbot.eclipse.junit5.headless.swtbottestapplication \
-product ch.sbb.scion.rcp.microfrontend.e3.app.demo.product \
-testApplication ch.sbb.scion.rcp.microfrontend.e3.app.demo.application \
-data . \
-testPluginName ch.sbb.scion.rcp.microfrontend.e3.app.demo.test \
 formatter=org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter,./AllTests.xml \
 formatter=org.apache.tools.ant.taskdefs.optional.junit.PlainJUnitResultFormatter \
-classname ch.sbb.scion.rcp.microfrontend.e3.app.demo.test.PopupCapabilityTest \
-os win32 -ws win32 -arch x86_64 \
-consoleLog -debug

### Launching on linux/gtk/x86_64

java -Xms256M -Xmx768M \
-jar ch.sbb.scion.rcp.microfrontend.e3.app.demo.product/target/products/ch.sbb.scion.rcp.microfrontend.e3.app.demo.product/linux/gtk/x86_64/plugins/org.eclipse.equinox.launcher_1.6.400.v20210924-0641.jar \
-application org.eclipse.swtbot.eclipse.junit5.headless.swtbottestapplication \
-product ch.sbb.scion.rcp.microfrontend.e3.app.demo.product \
-testApplication ch.sbb.scion.rcp.microfrontend.e3.app.demo.application \
-data . \
-testPluginName ch.sbb.scion.rcp.microfrontend.e3.app.demo.test \
 formatter=org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter,./AllTests.xml \
 formatter=org.apache.tools.ant.taskdefs.optional.junit.PlainJUnitResultFormatter \ 
-classname ch.sbb.scion.rcp.microfrontend.e3.app.demo.test.PopupCapabilityTest \
-os linux -ws gtk -arch x86_64 \
-consoleLog -debug