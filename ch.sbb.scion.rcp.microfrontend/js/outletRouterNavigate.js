try {
  await ${refs.OutletRouter}.navigate(${helpers.fromJson}('${url}') ?? undefined, {
    outlet: ${helpers.fromJson}('${options.outlet}') ?? undefined,
    relativeTo: ${helpers.fromJson}('${options.relativeTo}') ?? undefined,
    params: ${helpers.fromJson}('${options.params}') ?? undefined,
    pushStateToSessionHistoryStack: ${options.pushStateToSessionHistoryStack} ?? undefined,
  });
  window['${callback}'](null);
}
catch (error) {
  window['${callback}'](error.message ?? `${error}` ?? 'ERROR');
}