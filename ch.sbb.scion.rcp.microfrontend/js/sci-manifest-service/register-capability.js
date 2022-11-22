// ======= SCRIPT PLACEHOLDERS BEGIN =======
const helpers = {fromJson: /@@helpers.fromJson@@/};
const callback = window['/@@callback@@/'];
const capability = helpers.fromJson('/@@capability@@/');
const refs = {ManifestService: /@@refs.ManifestService@@/};
// ======= SCRIPT PLACEHOLDERS END =======

try {
  const id = await refs.ManifestService.registerCapability(capability);
  callback(null, id);
}
catch (error) {
  callback(error.message || `${error}` || 'ERROR');
}
