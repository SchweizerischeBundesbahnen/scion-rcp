try {
  const subscription = ${rxjsObservableIIFE}.subscribe({
    next: (next) => window['${callback}'](${helpers.toJson}({type: 'Next', next})),
    error: (error) => window['${callback}'](${helpers.toJson}({type: 'Error', error: error.message || `${error}` || 'ERROR'})),
    complete: () => window['${callback}'](${helpers.toJson}({type: 'Complete'})),
  });
  ${storage}['${callback}_subscription'] = subscription;
}
catch (error) {
  console.error(error);
  window['${callback}']({type: 'Error', message: error.message ?? `${error}` ?? 'ERROR'});
}