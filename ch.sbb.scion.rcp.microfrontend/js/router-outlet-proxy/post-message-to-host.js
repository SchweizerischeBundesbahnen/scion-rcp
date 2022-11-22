// ======= SCRIPT PLACEHOLDERS BEGIN =======
const helpers = {fromJson: /@@helpers.fromJson@@/};
const outletId = '/@@outletId@@/';
const envelope = helpers.fromJson('/@@base64json@@/', {decode: true});
// ======= SCRIPT PLACEHOLDERS END =======

const sciRouterOutlet = document.querySelector(`sci-router-outlet[name="${outletId}"]`);

try {
  sciRouterOutlet.iframe.contentWindow.__scion_rcp_postMessageToParentWindow(envelope);
}
catch (error) {
  console.error(`[MessageDispatchError] Failed to dispatch message to outlet window. [outlet=${outletId}]`, error);
}
