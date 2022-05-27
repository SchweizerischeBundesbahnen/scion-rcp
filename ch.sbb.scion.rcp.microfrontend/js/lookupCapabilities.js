(() => ${refs.ManifestService}.lookupCapabilities$({
  id: ${helpers.fromJson}('${filter.id}') ?? undefined,
  type: ${helpers.fromJson}('${filter.type}') ?? undefined,
  qualifier: ${helpers.fromJson}('${filter.qualifier}') ?? undefined,
  appSymbolicName: ${helpers.fromJson}('${filter.appSymbolicName}') ?? undefined
}))()