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

## 9. Java model/service gaps vs. current platform API (`ch.sbb.scion.rcp.microfrontend`)

Beyond wire-compatibility, the Java model and service classes in `ch.sbb.scion.rcp.microfrontend`
mirror the TypeScript API by hand (à la the workbench protocol) and have drifted from it. None of
these fail to compile — they are silent gaps: new properties are simply never sent/read, and new
API surface is simply missing.

### 9.1 Missing properties on existing model classes

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

### 9.2 Stale / renamed properties

- `ApplicationConfig.java` and `Application.java` both still declare `messageOrigin`. The host-config
  property was renamed to `secondaryOrigin` back in `1.0.0-rc.11` (pre-dates the 1.2.2 baseline);
  `messageOrigin` is dead code in `ApplicationConfig.java` (nothing ever sets it — the bundler's
  `start-host.js` hardcodes `application.secondaryOrigin = window.location.origin` for every app
  instead). The `Application` (read-only registry) interface no longer has `messageOrigin` at all,
  so that field always deserializes to `null` today. Rename to `secondaryOrigin` and wire it through
  properly, or remove if intentionally superseded by the hardcoded `start-host.js` behavior.

### 9.3 Missing service methods

| Java interface | Missing method | Introduced in | Effect |
|---|---|---|---|
| `ManifestService.java` | `getApplication(symbolicName)` (single lookup, with `orElse: null` option) | 1.3.0 ("provide method to get a specific application") | Java callers must fetch all applications and filter manually (as `MicrofrontendPopupDialog.getApplication()` already does) instead of using the platform's built-in lookup |

### 9.4 Missing extensibility point

- **`CapabilityInterceptor`** (host-side, register via `Beans.register(CapabilityInterceptor, {multi: true})`) —
  lets the host intercept, transform, or reject (`null`) capabilities at registration time. Introduced
  alongside the 2.0.0 change that made `ManifestService.registerCapability` resolve to `null` when
  rejected. `ch.sbb.scion.rcp.microfrontend.interceptor` only has `IntentInterceptor` and
  `MessageInterceptor` — there is no Java equivalent, so RCP-hosted applications cannot register a
  capability interceptor at all. Add a `CapabilityInterceptor` Java interface + bridging JS snippet
  analogous to the existing `IntentInterceptorInstaller`/`MessageInterceptorInstaller`, if this
  extensibility point should be exposed to Java host code.

### 9.5 Recommended additions to the upgrade steps (§ above)

- Add `inactive`, param `default`/`deprecated` to `Capability`/`ParamDefinition`.
- Add `capabilityActiveCheckDisabled` to `ApplicationConfig`, `HostConfig`, `Application`.
- Replace `MicrofrontendPlatformConfig.heartbeatInterval` with a `LivenessConfig` (`interval`/`timeout`).
- Rename/rewire `messageOrigin` → `secondaryOrigin` (or drop, given `start-host.js` already hardcodes it).
- Add `ManifestService.getApplication(String symbolicName, ...)`.
- Decide whether to implement `CapabilityInterceptor` support end-to-end (Java interface, JS bridging
  script, registration API on `MicrofrontendPlatform`).
- Add `Application.platformVersion` if useful for diagnostics/support tooling.
