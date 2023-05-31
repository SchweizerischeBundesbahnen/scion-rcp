# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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


