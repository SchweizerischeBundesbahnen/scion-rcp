const onmessage = event => {
  if (event.data?.transport === 'sci://microfrontend-platform/client-to-broker' || event.data?.transport === 'sci://microfrontend-platform/microfrontend-to-outlet') {
    const sender = event.data.message?.headers?.get('${headers.AppSymbolicName}');
    // Encode as base64 so it can be safely inserted into a script as a string literal.
    // For example, the apostrophe character (U+0027) would terminate the string literal.
    const base64json = ${helpers.toJson}(event.data, { encode: true });
    window['${callback}'](base64json, event.origin, sender);
  }
};

window.addEventListener('message', onmessage);
${storage} ['${callback}_dispose'] = () => window.removeEventListener('message', onmessage);