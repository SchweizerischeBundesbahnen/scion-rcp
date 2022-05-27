try {
  console.log('Starting Microfrontend Platform...');
  const config = ${helpers.fromJson}('${platformConfig}');

  // Overwrite message origin as we forward messages from the client to the host under the host's origin and vice versa.
  config.applications.forEach(application => {
    application.messageOrigin = window.location.origin;
  });

  // Start the platform host.
  await ${MicrofrontendPlatform}.startHost(config);

  window['${callback}'](null);
}
catch (error) {
  console.log('Failed to start Microfrontend Platform', error);
  window['${callback}'](error.message || `${error}` || 'Failed to start Microfrontend Platform');
}