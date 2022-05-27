const sciRouterOutlet = document.querySelector('sci-router-outlet[name="${outletId}"]');

try {
  const envelope = ${helpers.fromJson}('${base64json}', { decode: true });
  sciRouterOutlet.iframe.contentWindow.__scion_rcp_postMessageToParentWindow(envelope);
}
catch (error) {
  console.error('[MessageDispatchError] Failed to dispatch message to outlet window. [outlet=${outletId}]', error);
}