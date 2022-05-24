const applications = ${ refs.ManifestService }.applications;
window['${callback}'](${ helpers.toJson }(applications));