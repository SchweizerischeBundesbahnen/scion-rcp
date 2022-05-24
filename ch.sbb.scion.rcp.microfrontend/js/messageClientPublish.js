try {
  await ${ refs.MessageClient }.publish(${ helpers.fromJson }('${topic}'), ${ helpers.fromJson }('${message}') ?? null, {
    headers: ${ helpers.fromJson }('${options.headers}') ?? undefined,
    retain: ${ options.retain } ?? undefined,
  });
  window['${callback}'](null);
}
catch (error) {
    window['${callback}'](error.message ?? `${error}` ?? 'ERROR');
}