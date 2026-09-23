# SCION Workbench Client Upgrade — Breaking Change Analysis

`ch.sbb.scion.rcp.workbench` implements the **host side** of the `@scion/workbench-client` wire
protocol directly in Java (it does not depend on `@scion/workbench-client` as a library — it
hand-rolls the messaging topics and message classes defined in `ɵworkbench-commands.ts` and the
various `ɵWorkbench*` internal model files). It currently targets a protocol version around
`1.0.0-beta.23`. The client is now at `1.0.0-beta.43`. Since the protocol is duplicated rather than
imported, every change to topics, payload shapes, or field names in the client is a potential silent
breakage (wrong/missing messages, failed JSON deserialization, or features that simply never
trigger) — not a compile error.

Only the **view** and **popup** features are in scope, matching what `ch.sbb.scion.rcp.workbench`
implements.

Reference files compared:
- rcp: `ch.sbb.scion.rcp.workbench/src/ch/sbb/scion/rcp/workbench/view/**`, `.../popup/**`
- client: `scion-workbench/projects/scion/workbench-client/src/lib/ɵworkbench-commands.ts`, `view/ɵworkbench-view.model.ts`, `popup/ɵworkbench-popup.model.ts`, `popup/ɵworkbench-popup.service.ts`, `popup/workbench-popup-command.ts`, `workbench.model.ts`
- changelog: `scion-workbench/CHANGELOG_WORKBENCH_CLIENT.md`

## 1. Confirmed breaking changes (wire-format incompatible)

### 1.1 Popup `context` changed from object to string literal
- **rcp today**: `PopupCommand.context` (JSON field `context`) is serialized/deserialized as
  `PopupReferrer { viewId: string }`, i.e. `{"context": {"viewId": "view.1"}}`
  (`PopupCommand.java`, `PopupReferrer.java`, `PopupService.getContext()`).
- **client now**: `ɵWorkbenchPopupCommand.context` is `ViewId | PartId | DialogId | PopupId | NotificationId | null`
  — a **plain string**, e.g. `{"context": "view.1"}` (`workbench-popup-command.ts`).
  Changed in `1.0.0-beta.34` (2025-11-11): *"Signature of `context` option in popup config has
  changed from object literal to string literal. Migrate `{context: {viewId: 'view.x'}}` to
  `{context: 'view.x'}`."*
- **Impact**: Gson will fail to deserialize a JSON string into `PopupReferrer` (an object) →
  `IntentMessage<PopupCommand>` deserialization throws, and **opening a popup from a current client
  will break entirely** (or silently fail depending on how the interceptor is wired).
- **Fix**: Change `PopupCommand.context`/`referrer` to a plain `String` holding the opener's context
  id (view/part/dialog/popup/notification id). Drop `PopupReferrer.viewId`.

### 1.2 `Referrer` shape changed (no more `viewId`/`viewCapabilityId`)
- **client now**: `Referrer` only exposes `appSymbolicName` (`workbench.model.ts`). The properties
  `viewId` and `viewCapabilityId` were **removed** in `1.0.0-beta.42` (2026-06-16): *"Deprecated
  referrer properties `viewId` and `viewCapabilityId` have been removed from `WorkbenchPopup`
  handle."* (deprecated already since `1.0.0-beta.34`).
- **rcp today**: `IWorkbenchPopup.getReferrerViewId()` returns `Optional<String>` derived from
  `PopupReferrer.viewId`, and the popup microfrontend context (`ɵworkbench.popup` context value,
  `MicrofrontendPopupDialog.createDialogArea`) embeds `popup.input` which carries this old referrer
  shape.
- **Impact**: The `referrer` object handed to the popup microfrontend via the `ɵworkbench.popup`
  context (consumed as `ɵPopupContext.referrer` on the client) no longer matches
  `{appSymbolicName: string}`. Client code reading `referrer.appSymbolicName` will get `undefined`.
- **Fix**: Compute `appSymbolicName` from the requesting intent's message headers
  (`MessageHeaders.APP_SYMBOLIC_NAME` or equivalent) instead of from the (now removed) referrer
  object, and expose only `appSymbolicName` going forward.

### 1.3 `WorkbenchPopup.setResult()` not implemented
- **client now**: popup microfrontends can call `setResult(result)` to stage a result via
  `popupResultTopic` — *"Sets a result that will be passed to the popup opener when the popup is
  closed on focus loss."* — separate from `close(result)`, which publishes to `popupCloseTopic`
  directly (`ɵworkbench-popup.model.ts`).
- **rcp today**: only subscribes to the close topic (`ɵworkbench/popups/{id}/close}` in
  `MicrofrontendPopupDialog.installCloseListener()`); there is no subscriber for
  `ɵworkbench/popups/{id}/result`. When the dialog auto-closes on focus lost
  (`SWT.Deactivate` → `popup.close(null)`), any result previously staged via `setResult()` is lost —
  the opener always receives `null`.
- **Fix**: Subscribe to `popupResultTopic`, retain the last received value, and use it as the result
  when auto-closing on focus loss instead of hardcoding `null`.

## 2. Confirmed functional gaps (new topics never implemented — cause hangs, not just missing features)

### 2.1 View "part" identity never published → `WorkbenchView.whenProperties` hangs
- **client now**: `ɵWorkbenchView` derives `partId$` from `ɵWorkbenchCommands.partIdTopic`
  (`ɵworkbench/views/{viewId}/part/id`), and `whenProperties` is
  `firstValueFrom(combineLatest([partId$, params$, capability$]))` — i.e. it **waits for a value on
  `partId$` before resolving** (`ɵworkbench-view.model.ts`).
- **rcp today**: `MicrofrontendViewEditorPart` never publishes to `.../part/id`.
- **Impact**: Any microfrontend view awaiting `WorkbenchView.whenProperties` (a documented,
  recommended pattern) will **hang indefinitely** when hosted in the RCP workbench. This is a
  functional regression, introduced by the "docked parts" feature (`1.0.0-beta.34`).
- **Fix**: Publish a stable, retained part id per view (e.g. a synthetic/constant part id, since RCP
  has no docked-parts concept) to `.../part/id` so `whenProperties` resolves.

### 2.2 View "closable" / programmatic close / close-confirmation not implemented
- **client now** additionally defines, none of which are handled by
  `MicrofrontendViewEditorPart`/`ViewIntentInterceptor` today:
  - `viewClosableTopic` (`.../closable`) — lets the client mark a view (non-)closable.
  - `viewCloseTopic` (`.../close`) — lets the client programmatically close its own view.
  - `canCloseTopic` (`.../canClose`) — host asks the view for closing confirmation
    (`WorkbenchView.canClose(fn)`).
  - `viewFocusedTopic` (`.../focused`) — focused state (separate from `active`).
- **Impact**: not wire-incompatible (additive, opt-in from the client's perspective), but any app
  relying on `canClose` guards, programmatic `view.close()`, or `focused$`/`closable` will silently
  not work under the RCP host.

### 2.3 Popup `align`
- **client now**: `ɵWorkbenchPopupCommand.align` (`'east'|'west'|'north'|'south'`) requests a
  preferred alignment relative to the anchor.
- **rcp today**: `PopupCommand` has no `align` field; `PopupIntentInterceptor`/`Popup.Builder` never
  reads/positions relative to it (only `origin` and the fixed `size` capability property are used).
- **Impact**: additive/non-breaking, but preferred alignment requests from current clients are
  silently ignored.

## 3. Checked and confirmed unchanged (no action needed)

- `viewTitleTopic`, `viewHeadingTopic`, `viewDirtyTopic`, `viewParamsTopic`, `viewActiveTopic`,
  `viewUnloadingTopic`, `viewParamsUpdateTopic`, `popupOriginTopic`, `popupCloseTopic` paths are
  identical to what rcp already implements.
- `ɵVIEW_CAPABILITY_ID_PARAM_NAME` is still `'ɵViewCapabilityId'` — matches
  `MicrofrontendViewEditorPart`'s hardcoded param key.
- The view-navigation intent body (`{target: 'blank' | 'auto' | <viewId>}`) used by
  `ɵWorkbenchRouterService` is unchanged; `ViewIntentInterceptor.openMicrofrontendEditor` still
  matches it correctly.
- `MicrofrontendPopupDialog`'s error-close protocol (header `ɵWORKBENCH-POPUP:CLOSE_WITH_ERROR` on
  the close-topic message) is unchanged — still matches `ɵWorkbenchPopupMessageHeaders.CLOSE_WITH_ERROR`.
- Popup opening still round-trips through `IntentClient.request$()` on the opener side
  (`ɵWorkbenchPopupService.open()`), matching rcp's `PopupService.open()` — the reply-based result
  delivery mechanism (`PopupIntentInterceptor.onClose` → reply to `REPLY_TO` topic) is still correct.

## 4. Recommended migration steps

1. Change `PopupCommand`/`PopupReferrer` wire format: `context` becomes a plain `String` (opener's
   context id), not an object — update Gson (de)serialization accordingly (§1.1).
2. Rework referrer computation for popups to derive `appSymbolicName` from intent message headers
   instead of the (removed) `PopupReferrer.viewId`/`viewCapabilityId` (§1.2).
3. Implement `popupResultTopic` subscription in `MicrofrontendPopupDialog` and use the staged result
   when auto-closing on focus loss (§1.3).
4. Publish a part id (retained) per view to `.../part/id` so `WorkbenchView.whenProperties` resolves
   (§2.1) — critical, do this even if RCP has no real "parts" concept.
5. Decide whether to implement `viewClosableTopic`, `viewCloseTopic`, `canCloseTopic`, and
   `viewFocusedTopic` (§2.2) — at minimum, document as unsupported if not implemented.
6. Consider implementing popup `align` (§2.3) for parity, or explicitly document it as unsupported.
7. Since the protocol is hand-duplicated rather than consumed from `@scion/workbench-client`,
   re-diff `ɵworkbench-commands.ts` and the `ɵWorkbench*` model files against rcp's Java
   implementation as part of every future workbench-client upgrade — there is no compiler to catch
   drift here.
8. Add/extend the SWTBot-based integration tests (`ch.sbb.scion.rcp.swtbot.feature`,
   `ch.sbb.scion.rcp.workbench.test`) to cover: popup opened with `context`, popup result set via
   `setResult()` then closed on focus loss, and a view awaiting `whenProperties`.
