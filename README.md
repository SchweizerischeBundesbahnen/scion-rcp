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

### Build & Release
See [build.yml](.github/workflows/build.yml) for further infos about the build.
See [release.yml](.github/workflows/release.yml) for further infos about the release.
 