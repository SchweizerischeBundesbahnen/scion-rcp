// ======= SCRIPT PLACEHOLDERS BEGIN ========
const helpers = {toJson: /@@helpers.toJson@@/};
const storage = /@@storage@@/;
const callback = window['/@@callback@@/'];
const subscriptionStorageKey = '/@@subscriptionStorageKey@@/';
const request = '/@@request@@/';
const rxjsObservableIIFE = /@@rxjsObservableIIFE@@/;
// ======= SCRIPT PLACEHOLDERS END =======

try {
  const subscription = rxjsObservableIIFE.subscribe({
    next: (next) => callback(helpers.toJson({type: 'Next', next})),
    error: (error) => callback(helpers.toJson({type: 'Error', error: error.message || `${error}` || 'ERROR'})),
    complete: () => callback(helpers.toJson({type: 'Complete'})),
  });
  storage[subscriptionStorageKey] = subscription;
}
catch (error) {
  console.error(error);
  callback({type: 'Error', message: error.message || `${error}` || 'ERROR'});
}
