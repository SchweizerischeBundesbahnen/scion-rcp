# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Make headless microfrontend platform host browser context API

## [0.0.7] - 2024-04-25

### Added

- Support intention registration and lookup functionalities as well as application qualification check

### Changed

- Upgrade integrated @scion/microfrontend-platform to version 1.2.2

## [0.0.6] - 2024-04-22

### Added

- Support tracking subscriber count of messaging topic. This change allows cleaning up unsubscribed intent reply subscriptions. Previously, there were no means to detect whether the intent issuer is still subscribed to the reply observable.
- Support listening to focus-within events on the router outlet.
- Introduce minimal public API for the microfrontend view part.
  - Allow identifying a microfrontend view part.
  - Support listening to focus-within events of the embedded router outlet.
  - Provide a selection provider that can be used to inform the Eclipse context about selections in the embedded microfrontend. In particular, this selection provider integrates with the Eclipse selection service.

### Changed

- Make all argument constructors of model classes public. This change allows to extend these classes, which was effectively inhibited previously - there is no decision against allowing the extension of model classes, currently.
- Upgrade Eclipse release from 2022-06 to 2023-09
- Use Java 17

## [0.0.5] - 2023-06-19

### Changed

- Extract interfaces from Scion microfrontend services, and move nested classes to model

## [0.0.4] - 2023-05-31

### Changed

- Compile with Java Runtime version 55

## [0.0.3] - 2023-05-31

### Changed

- Reduce required execution environment of the Scion RCP microfrontend and the Scion RCP workbench bundles from Java 17 to Java 11.

## [0.0.2] - 2023-05-23

### Added

- This CHANGELOG file.

### Changed

- Downgrade Eclipse release and Eclipse Orbit release from 2023-03 to 2022-03.
- Replace packaging type with implied packaging type - i.e., jar - in the tycho consumer pom, which is the final pom that will be deployed.
- Skip closing the remotely staged repository during the upload step. Recently, the connection between the Github build server and the Nexus Repository timed out while waiting for the closing result, frequently. Close the staged repository manually, directly in the Nexus Repository.

## [0.0.1] - 2023-05-17

### Added

- The Scion RCP microfrontend bundle.
- The Scion RCP microfrontend test fragment.
- The Scion RCP workbench bundle.
- The Scion RCP workbench test fragment.
- The target platform bundle.
- The e3 demo app bundle.
- The e3 demo app product bundle.
- The e3 demo app test bundle.
- The (e4) demo app bundle.
- The (e4) demo app product bundle.
- The main feature bundle, to allow feature-based products.
- The test feature bundle.
- The Eclipse setup bundle.
- The code coverage bundle.
- The SWT bot feature bundle.
- The client demo app, a TypeScript demo microfrontend app.
- The Scion microfrontend host dependency bundler project, a utility to transpile the Scion Microfrontend Platform into JavaScript.


