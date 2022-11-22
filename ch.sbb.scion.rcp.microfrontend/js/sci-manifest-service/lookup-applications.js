// ======= SCRIPT PLACEHOLDERS BEGIN =======
const helpers = {toJson: /@@helpers.toJson@@/};
const callback = window['/@@callback@@/'];
const refs = {ManifestService: /@@refs.ManifestService@@/};
// ======= SCRIPT PLACEHOLDERS END =======

const applications = refs.ManifestService.applications;
callback(helpers.toJson(applications));
