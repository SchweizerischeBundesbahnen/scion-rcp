// ======= SCRIPT PLACEHOLDERS BEGIN =======
const helpers = {fromJson: /@@helpers.fromJson@@/};
const callback = window['/@@callback@@/'];
const refs = {ManifestService: /@@refs.ManifestService@@/};
const filter = {
  id: helpers.fromJson('/@@filter.id@@/'),
  type: helpers.fromJson('/@@filter.type@@/'),
  qualifier: helpers.fromJson('/@@filter.qualifier@@/'),
  appSymbolicName: helpers.fromJson('/@@filter.appSymbolicName@@/'),
};
// ======= SCRIPT PLACEHOLDERS END =======

try {
  await refs.ManifestService.unregisterIntentions({
    id: filter.id ?? undefined,
    type: filter.type ?? undefined,
    qualifier: filter.qualifier ?? undefined,
    appSymbolicName: filter.appSymbolicName ?? undefined,
  });
  callback(null);
}
catch (error) {
  callback(error.message || `${error}` || 'ERROR');
}
