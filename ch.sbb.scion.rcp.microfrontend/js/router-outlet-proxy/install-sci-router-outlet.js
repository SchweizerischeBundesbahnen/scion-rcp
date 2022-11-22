// ======= SCRIPT PLACEHOLDERS BEGIN =======
const refs = {OutletRouter: /@@refs.OutletRouter@@/};
const outletId = '/@@outletId@@/';
const outletToProxyKeystrokeCallback = window['/@@outletToProxyKeystrokeCallback@@/'];
// ======= SCRIPT PLACEHOLDERS END =======

// Append sci-router-outlet
const sciRouterOutlet = document.body.appendChild(document.createElement('sci-router-outlet'));

// Prepare content to be loaded into the router outlet
const outletContent = `
<!DOCTYPE html>
<html>
  <head>
    <script src="${location.origin}/js/helpers.js"></script>
    <script>
      // ======= SCRIPT PLACEHOLDERS BEGIN =======
      const toJson = /@@helpers.toJson@@/;
      const outletToProxyMessageCallback = window.parent['/@@outletToProxyMessageCallback@@/'];
      const outletLoadedCallback = window.parent['/@@outletLoadedCallback@@/'];
      // ======= SCRIPT PLACEHOLDERS END =======
    
      // Dispatch message to the host
      function __scion_rcp_postMessageToParentWindow(envelope) {
        window.parent.postMessage(envelope, location.origin);
      }

      // Dispatch messages from the host
      window.addEventListener('message', event => {
        if (event.data?.transport === 'sci://microfrontend-platform/broker-to-client') {
          // Encode as base64 so it can be safely inserted into a script as a string literal.
          // For example, the apostrophe character (U+0027) would terminate the string literal.
          const base64json = toJson(event.data, {encode: true});
          outletToProxyMessageCallback?.(base64json);
        }
      });
      outletLoadedCallback();
    </script>
  </head>
  <body>${outletId}</body>
</html>`;

// Load content into the outlet
const outletUrl = URL.createObjectURL(new Blob([outletContent], {type: 'text/html'}));
window.addEventListener('unload', () => URL.revokeObjectURL(outletUrl), {once: true});
sciRouterOutlet.name = outletId;
refs.OutletRouter.navigate(outletUrl, {outlet: outletId});

// Install keystroke dispatcher
sciRouterOutlet.addEventListener('keydown', event => {
  outletToProxyKeystrokeCallback('keydown', event.key, event.ctrlKey, event.shiftKey, event.altKey, event.metaKey);
});
sciRouterOutlet.addEventListener('keyup', event => {
  outletToProxyKeystrokeCallback('keyup', event.key, event.ctrlKey, event.shiftKey, event.altKey, event.metaKey);
});
