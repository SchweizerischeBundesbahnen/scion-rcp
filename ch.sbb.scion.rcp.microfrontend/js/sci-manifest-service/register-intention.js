// ======= SCRIPT PLACEHOLDERS BEGIN =======
const helpers = {fromJson: /@@helpers.fromJson@@/};
const callback = window['/@@callback@@/'];
const intention = helpers.fromJson('/@@intention@@/');
const refs = {ManifestService: /@@refs.ManifestService@@/};
// ======= SCRIPT PLACEHOLDERS END =======

try {
  const id = await refs.ManifestService.registerIntention(intention);
  callback(null, id);
}
catch (error) {
  callback(error.message || `${error}` || 'ERROR');
}
