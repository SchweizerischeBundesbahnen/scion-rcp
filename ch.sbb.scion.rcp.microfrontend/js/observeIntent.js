(() => ${ refs.IntentClient }.observe$({
  type: ${ helpers.fromJson }('${selector.type}') ?? undefined,
  qualifier: ${ helpers.fromJson }('${selector.qualifier}') ?? undefined,
})) ()