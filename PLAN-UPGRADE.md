# Upgrade Plan: `@scion/microfrontend-platform` 1.2.2 → 3.0.0

## Architecture recap

- `scion-rcp-microfrontend-host-dependency-bundler/` uses parcel to bundle `@scion/microfrontend-platform` + `@scion/toolkit` + `rxjs` (via `src/refs.ts`) into `ch.sbb.scion.rcp.microfrontend/js/refs.js`, which exposes the platform API on `window['__SCION_RCP'].refs`.
- Hand-written JS snippets under `ch.sbb.scion.rcp.microfrontend/js/` (e.g. `sci-manifest-service/*`, `sci-message-client/*`, `host/start-host.js`) call into `refs.*` and are executed from Java via `JavaScriptExecutor`/`Script` (see e.g. `ManifestServiceImpl.java`).
- A second, independent copy of the same dependency exists in `scion-rcp-microfrontend-client-demo-app/package.json` (also pinned to 1.2.2), used for the demo client app, not bundled into Java.

## Relevant breaking changes (1.2.2 → 3.0.0)

Source: `scion-microfrontend-platform/CHANGELOG.md`

| Version | Change | Impact on us |
|---|---|---|
| 1.4.0 | Requires `@scion/toolkit` `^1.6.0` (later `^1.6.0 \|\| ^2.0.0`) | Must bump `@scion/toolkit` from `1.4.1` in both `package.json`s |
| 2.0.0 | `any` → `unknown` in public API types | Only affects TypeScript compile-time checks in `refs.ts`; verify it still compiles |
| 2.0.0 | `ManifestService.registerCapability` may resolve `null` if an interceptor rejects registration | `ManifestServiceImpl.java` already tolerates a nullable id (`args[1]` passed straight to `complete`), so no code change strictly required — but worth a conscious check/comment |
| 3.0.0 | `MicrofrontendPlatform.destroy()` now returns `void` instead of `Promise`; `whenState()` renamed to `onState()` with callback | No direct usage of `destroy`/`whenState`/`PlatformState` found in our own JS snippets or Java code — the only occurrences are inside the *generated* `refs.js` bundle itself (internal library implementation), which will regenerate correctly when rebundled |

No other breaking API usage was found for `ManifestService`, `MessageClient`, `IntentClient`, `OutletRouter`, `QualifierMatcher`, `TopicMatcher`, or the router-outlet proxy scripts — these surfaces are additive/stable across the range.

## Steps

1. **Bump dependencies** in both:
   - `scion-rcp-microfrontend-host-dependency-bundler/package.json`: `@scion/microfrontend-platform` → `3.0.0`, `@scion/toolkit` → latest `2.x` compatible version.
   - `scion-rcp-microfrontend-client-demo-app/package.json`: same bumps (kept in sync since it's a demo client of the same host).
2. Run `npm install` in both folders, resolve any peer-dependency conflicts (rxjs stays `^7.5.0`, already satisfied).
3. **Rebuild the bundle**: run `npm run bundle` in `scion-rcp-microfrontend-host-dependency-bundler`, verify `refs.ts` compiles cleanly (watch for TS errors from the `any`→`unknown` tightening) and regenerates `ch.sbb.scion.rcp.microfrontend/js/refs.js` + `.map`.
4. **Sanity-check generated bundle**: confirm `refs.js` still exposes all members Java depends on (`MicrofrontendPlatform`, `MicrofrontendPlatformHost`, `MicrofrontendPlatformClient`, `MessageClient`, `IntentClient`, `OutletRouter`, `ManifestService`, `Beans`, `MessageInterceptor`, `IntentInterceptor`, `TopicMatcher`, `QualifierMatcher`, `UUID`).
5. **Review `registerCapability` null-id path** in `ManifestServiceImpl.java` / `ManifestService.java` — decide if a null id from an interceptor rejection should surface differently than before (currently just silently completes with `null`).
6. **Run existing tests**: `ch.sbb.scion.rcp.microfrontend.test`, `ch.sbb.scion.rcp.microfrontend.e3.app.demo.test`, and the SWTBot feature, to exercise host start/stop, capability registration, messaging, routing.
7. **Manual smoke test** via the demo apps (`ch.sbb.scion.rcp.microfrontend.app.demo` / e3 variant): host start/stop (exercises the new synchronous `destroy`/`onState` internals on page unload), capability registration/lookup, messaging, intent handling, router-outlet navigation.
8. Update `CHANGELOG.md` of scion-rcp noting the platform version bump and any behavioral notes (e.g., synchronous shutdown).

## Java model/service gaps vs. current platform API (`ch.sbb.scion.rcp.microfrontend`)

Beyond wire-compatibility, the Java model and service classes in `ch.sbb.scion.rcp.microfrontend`
mirror the TypeScript API by hand (à la the workbench protocol) and have drifted from it. None of
these fail to compile — they are silent gaps: new properties are simply never sent/read, and new
API surface is simply missing.

### Missing properties on existing model classes

| Java class | Missing property | Introduced in | Effect |
|---|---|---|---|
| `Capability.java` | `inactive?: boolean` | 2.0.0 ("support excluding capability") | Cannot mark/read a capability as inactive; can't support the `capabilityActiveCheckDisabled` DevTools workflow described in the 1.6.0 changelog recommendation |
| `Capability.ParamDefinition` | `default?: unknown` | 2.0.0 ("support default value for optional capability parameter") | Cannot declare a default value for an optional param when registering a capability from Java |
| `Capability.ParamDefinition` | `deprecated?: true \| {message?, useInstead?}` | ~2.x | Cannot deprecate/rename params from Java-registered capabilities |
| `ApplicationConfig.java` | `capabilityActiveCheckDisabled?: boolean` | 1.6.0/2.0.0 | Cannot disable the capability-active check per application when configuring the host |
| `HostConfig.java` | `capabilityActiveCheckDisabled?: boolean` | 1.6.0/2.0.0 | Same, but for the host application itself |
| `Application.java` | `capabilityActiveCheckDisabled: boolean` | 1.6.0/2.0.0 | Cannot read back whether the check is disabled for a registered application |
| `Application.java` | `platformVersion: Promise<string>` | later 1.x | Cannot read which platform version a connected app uses |
| `MicrofrontendPlatformConfig.java` | `liveness?: {interval, timeout}` | **1.0.0-rc.11**, i.e. already before the 1.2.2 baseline | `heartbeatInterval` (still the only field in Java) was replaced by `liveness` — this field has been a no-op for the entire time rcp has depended on the library; must be replaced regardless of the 3.0.0 upgrade |

### Stale / renamed properties

- `ApplicationConfig.java` and `Application.java` both still declare `messageOrigin`. The host-config
  property was renamed to `secondaryOrigin` back in `1.0.0-rc.11` (pre-dates the 1.2.2 baseline);
  `messageOrigin` is dead code in `ApplicationConfig.java` (nothing ever sets it — the bundler's
  `start-host.js` hardcodes `application.secondaryOrigin = window.location.origin` for every app
  instead). The `Application` (read-only registry) interface no longer has `messageOrigin` at all,
  so that field always deserializes to `null` today. Rename to `secondaryOrigin` and wire it through
  properly, or remove if intentionally superseded by the hardcoded `start-host.js` behavior.

### Missing service methods

| Java interface | Missing method | Introduced in | Effect |
|---|---|---|---|
| `ManifestService.java` | `getApplication(symbolicName)` (single lookup, with `orElse: null` option) | 1.3.0 ("provide method to get a specific application") | Java callers must fetch all applications and filter manually (as `MicrofrontendPopupDialog.getApplication()` already does) instead of using the platform's built-in lookup |

### Missing extensibility point

- **`CapabilityInterceptor`** (host-side, register via `Beans.register(CapabilityInterceptor, {multi: true})`) —
  lets the host intercept, transform, or reject (`null`) capabilities at registration time. Introduced
  alongside the 2.0.0 change that made `ManifestService.registerCapability` resolve to `null` when
  rejected. `ch.sbb.scion.rcp.microfrontend.interceptor` only has `IntentInterceptor` and
  `MessageInterceptor` — there is no Java equivalent, so RCP-hosted applications cannot register a
  capability interceptor at all. Add a `CapabilityInterceptor` Java interface + bridging JS snippet
  analogous to the existing `IntentInterceptorInstaller`/`MessageInterceptorInstaller`, if this
  extensibility point should be exposed to Java host code.
- **`HostManifestInterceptor`** (host-side, register via `Beans.register(HostManifestInterceptor, {multi: true})`) —
  mutates the host manifest before the platform registers it, for example to add intentions or
  capabilities supplied by an integrating library. The exported `MicrofrontendPlatform` Java API
  only registers message and intent interceptors; it has no host-manifest hook. This is distinct
  from `CapabilityInterceptor`, which runs when a capability is registered. A Java hook would need
  to be installed before the TypeScript host starts.

### Missing outlet context read-back

- **`SciRouterOutletElement.contextValues$`** — an Observable exposing all context values
  currently set on a specific outlet, including ones the library sets autonomously (not just what
  the host explicitly pushes via `setContextValue`). Two confirmed cases of autonomous, library-internal
  context writes: `installOutletContext()` auto-sets `OUTLET_CONTEXT` whenever the outlet's name
  changes, and `registerKeystroke()` auto-sets a keystroke context entry (RCP's own
  `MicrofrontendPopupDialog.java` triggers this via `sciRouterOutlet.registerKeystroke("keydown.escape")`
  without controlling the resulting key/value itself). `RouterOutlet.java`/`RouterOutletProxy.java`
  expose only the write side (`setContextValue`/`removeContextValue`) — there is no Java bridge for
  `contextValues$`, so Java's view of an outlet's context can silently diverge from the actual
  JS/DOM-side state, with no way to detect or read it back.

### Recommended additions to the upgrade steps (§ above)

- Add `inactive`, param `default`/`deprecated` to `Capability`/`ParamDefinition`.
- Add `capabilityActiveCheckDisabled` to `ApplicationConfig`, `HostConfig`, `Application`.
- Replace `MicrofrontendPlatformConfig.heartbeatInterval` with a `LivenessConfig` (`interval`/`timeout`).
- Rename/rewire `messageOrigin` → `secondaryOrigin` (or drop, given `start-host.js` already hardcodes it).
- Add `ManifestService.getApplication(String symbolicName, ...)`.
- Decide whether to implement `CapabilityInterceptor` support end-to-end (Java interface, JS bridging
  script, registration API on `MicrofrontendPlatform`).
- Decide whether to expose `HostManifestInterceptor` to Java host integrations before host startup.
- Add `Application.platformVersion` if useful for diagnostics/support tooling.
- Add a Java-side read-back for outlet context (`contextValues$`) to `RouterOutlet.java`/
  `RouterOutletProxy.java`, so Java can observe context values the library itself sets
  autonomously (`OUTLET_CONTEXT`, keystroke entries) rather than only what it explicitly pushed.

### Current API comparison (addendum)

The entries above describe gaps identified during the original review; several have since been
implemented. This comparison uses the TypeScript library's `src/public-api.ts` and its host/client
`public_api.ts` barrels, and the Java packages exported by `ch.sbb.scion.rcp.microfrontend/META-INF/MANIFEST.MF`
(`microfrontend`, `interceptor`, `model`, `subscriber`). Java services in the first package include
`MicrofrontendPlatform`, `ManifestService`, `MessageClient`, `IntentClient`, `OutletRouter`, and the
`RouterOutlet` SWT widget. The host runs the TypeScript library in an SWT Browser; a public TypeScript
export is not automatically callable from Java. In particular, `scion-rcp-microfrontend-host-dependency-bundler/src/refs.ts`
only exposes selected exports to the JavaScript bridge.

**Already present in Java (the earlier recommendations are historical, not outstanding work):**

- `ManifestService.getApplication` and `getApplicationOrNull` are implemented using the application
  cache in `ManifestServiceImpl`. The earlier missing-method row is no longer accurate.
- `Capability.isInactive`, `Capability.ParamDefinition.defaultValue`/`deprecated` (including the
  `default`/`deprecated` JSON adapter), and `capabilityActiveCheckDisabled` on `ApplicationConfig`,
  `HostConfig`, and `Application` are present.
- `ApplicationConfig.secondaryOrigin` and `MicrofrontendPlatformConfig.liveness` with nested
  `LivenessConfig.interval`/`timeout` are present. `start-host.js` still overrides each configured
  application's `secondaryOrigin` with the host origin for the RCP message bridge, so the Java
  setting cannot take effect as supplied. `Application.platformVersion` is still absent.

**Additional host-side services and hooks without a Java-facing equivalent:**

| TypeScript public API | Current Java surface / consequence |
|---|---|
| `MicrofrontendPlatform.state`, `state$`, `onState(...)`, `destroy()` | `MicrofrontendPlatform` only starts the host and registers interceptors; Java cannot observe lifecycle state or explicitly stop the TypeScript platform. Disposing the OSGi host shell is not an equivalent public lifecycle API. |
| `MicrofrontendPlatformHost.startupProgress$` | `startHost(...)` returns a future for the completed startup, but Java cannot report manifest/activator startup progress to an RCP progress monitor. |
| `Logger` bean | The TypeScript logger is replaceable through `Beans`; Java has no registration hook to route platform logs through Eclipse logging. This is separate from logging errors in Java's host startup callback. |
| `RouterOutletUrlAssigner` / `RelativePathResolver` beans | TypeScript consumers can replace iframe URL assignment and relative-path resolution. The exported Java API offers no corresponding strategy hook; RCP's `RouterOutletProxy` creates the DOM outlet internally. |

The `HostManifestInterceptor` described above is another host-side extension hook not registered
by the current `MicrofrontendPlatform` Java API. Both it and `CapabilityInterceptor` would require
adding the TypeScript token to `refs.ts`, then installing a Java-to-JS adapter before host startup;
the existing message/intent installer pattern alone cannot access an unexposed token.

**Browser-client APIs not mirrored by the Java bundle (scope decision, not necessarily defects):**

- `PlatformPropertyService` reads host-defined properties, while Java's
  `MicrofrontendPlatformConfig.properties` only supplies them. `ContextService` looks up and observes
  inherited outlet context, while Java `RouterOutlet` only writes its own context. Neither service
  has a Java facade; embedded TypeScript microfrontends can continue using them directly.
- `FocusMonitor.focus$`/`focusWithin$` observe focus of the *current microfrontend* across iframe
  boundaries. Java `RouterOutlet.onFocusWithin` is scoped to one embedded outlet, not equivalent.
  `PreferredSizeService` is used by embedded content to report its size; Java's outlet has no
  matching client-side service. `MicrofrontendPlatformClient.connect`, `isConnected`, and
  `signalReady` likewise remain browser-client APIs, not Java services.
- `MicrofrontendPlatformStopper` is a replaceable TypeScript bean for page-unload shutdown. Java
  cannot configure it through the exported bundle API; decide whether controlling shutdown from
  RCP is required before adding a bridge.

**Additional differences on existing Java services:** `OutletRouter.navigate(null, options)`
clears a target outlet and its retained navigation in TypeScript, but Java's
`OutletRouterImpl.navigateInternal` rejects a null target. `RouterOutlet.installRouter` already maps
an absent URL message body to `about:blank`; enabling explicit clearing needs a Java API/bridge path
that permits null and verification that the retained navigation is removed.
`SciRouterOutletElement` also exposes `empty$`, `scrollable`, `preferredSize`, and
`resetPreferredSize()` in addition to the already noted `contextValues$`; Java's SWT outlet has no
equivalent read-back/control for these properties. Treat these as separate outlet feature choices,
not as missing `ContextService` methods. `NavigationOptions.showSplash` is also absent from the
Java options model even though the TypeScript router supports it; enabling it would require a
client to call `MicrofrontendPlatformClient.signalReady()`.
