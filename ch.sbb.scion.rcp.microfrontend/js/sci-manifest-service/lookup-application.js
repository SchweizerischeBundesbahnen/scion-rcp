// ======= SCRIPT PLACEHOLDERS BEGIN =======
const helpers = {toJson: /@@helpers.toJson@@/};
const appSymbolicName = helpers.fromJson('/@@appSymbolicName@@/')
const callback = window['/@@callback@@/'];
const refs = {ManifestService: /@@refs.ManifestService@@/};
// ======= SCRIPT PLACEHOLDERS END =======

const options = {useInstead: null};
const application = refs.ManifestService.getApplication(appSymbolicName, options);
callback(helpers.toJson(application));