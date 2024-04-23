(() => {
  // ======= SCRIPT PLACEHOLDERS BEGIN =======
  const helpers = {fromJson: /@@helpers.fromJson@@/};
  const refs = {ManifestService: /@@refs.ManifestService@@/};
  const appSymbolicName = helpers.fromJson('/@@appSymbolicName@@/')
  const qualifiedFor = {
    capabilityId: helpers.fromJson('/@@qualifiedFor.capabilityId@@/'),
  };
  // ======= SCRIPT PLACEHOLDERS END =======

  return refs.ManifestService.isApplicationQualified$(appSymbolicName, qualifiedFor);
})()
