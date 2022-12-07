// ======= SCRIPT PLACEHOLDERS BEGIN =======
const refs = {MicrofrontendPlatform: /@@refs.MicrofrontendPlatform@@/};
const helpers = {fromJson: /@@helpers.fromJson@@/};
const callback = window['/@@callback@@/'];
const config = helpers.fromJson('/@@platformConfig@@/');
// ======= SCRIPT PLACEHOLDERS END =======

try {
  console.log('Starting Microfrontend Platform...', config);

  // Overwrite message origin as we forward messages from the client to the host under the host's origin and vice versa.
  config.applications.forEach(application => {
    application.secondaryOrigin = window.location.origin;
  });

  // Start the platform host.
  await refs.MicrofrontendPlatform.startHost(config);

  callback(null);
}
catch (error) {
  console.error('Failed to start Microfrontend Platform', error);
  callback(error.message || `${error}` || 'Failed to start Microfrontend Platform');
}
