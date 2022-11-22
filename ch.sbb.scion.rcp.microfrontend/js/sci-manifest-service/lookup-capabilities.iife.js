(() => {
  // ======= SCRIPT PLACEHOLDERS BEGIN =======
  const helpers = {fromJson: /@@helpers.fromJson@@/};
  const refs = {ManifestService: /@@refs.ManifestService@@/};
  const filter = {
    id: helpers.fromJson('/@@filter.id@@/'),
    type: helpers.fromJson('/@@filter.type@@/'),
    qualifier: helpers.fromJson('/@@filter.qualifier@@/'),
    appSymbolicName: helpers.fromJson('/@@filter.appSymbolicName@@/'),
  };
  // ======= SCRIPT PLACEHOLDERS END =======

  return refs.ManifestService.lookupCapabilities$({
    id: filter.id ?? undefined,
    type: filter.type ?? undefined,
    qualifier: filter.qualifier ?? undefined,
    appSymbolicName: filter.appSymbolicName ?? undefined,
  });
})()
