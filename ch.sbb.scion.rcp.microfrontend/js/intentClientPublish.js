try {
  await ${ refs.IntentClient }.publish(${ helpers.fromJson }('${intent}'), ${ helpers.fromJson }('${body}') ?? null, {
    headers: ${ helpers.fromJson }('${options.headers}') ?? undefined
  });
  window['${callback}'](null);
}
  catch (error) {
  window['${callback}'](error.message ?? `${error}` ?? 'ERROR');
}