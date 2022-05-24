const sciRouterOutlet = document.body.appendChild(document.createElement('sci-router-outlet'));

const outletContent = `<html>
                        <head>
                          <script src="${location.origin}/js/helpers.js"></script>
                          <script>
                            // Dispatch message to the host
                            function __scion_rcp_postMessageToParentWindow(envelope) {
                              window.parent.postMessage(envelope, location.origin);
                            }
            
                            // Dispatch messages from the host
                            window.addEventListener('message', event => {
                              if (event.data?.transport === 'sci://microfrontend-platform/broker-to-client') {
                                // Encode as base64 so it can be safely inserted into a script as a string literal.
                                // For example, the apostrophe character (U+0027) would terminate the string literal.                    
                                const base64json = ${helpers.toJson}(event.data, {encode: true});
                                window.parent['${outletToProxyMessageCallback}']?.(base64json);
                              }
                            });
                          </script>
                        </head>
                        <body>${outletId}</body>
                      </html>`;

// Load outlet content
const outletUrl = URL.createObjectURL(new Blob([outletContent], { type: 'text/html' }));
window.addEventListener('unload', () => URL.revokeObjectURL(outletUrl), { once: true });
sciRouterOutlet.name = '${outletId}';
          ${ refs.OutletRouter }.navigate(outletUrl, { outlet: '${outletId}' });

// Install keystroke dispatcher
sciRouterOutlet.addEventListener('keydown', event => {
  window['${outletToProxyKeystrokeCallback}']('keydown', event.key, event.ctrlKey, event.shiftKey, event.altKey, event.metaKey);
});
sciRouterOutlet.addEventListener('keyup', event => {
  window['${outletToProxyKeystrokeCallback}']('keyup', event.key, event.ctrlKey, event.shiftKey, event.altKey, event.metaKey);
});