// ======= SCRIPT PLACEHOLDERS BEGIN =======
const helpers = {fromJson: /@@helpers.fromJson@@/};
const callback = window['/@@callback@@/'];
const url = helpers.fromJson('/@@url@@/');
const options = {
  outlet: helpers.fromJson('/@@options.outlet@@/'),
  relativeTo: helpers.fromJson('/@@options.relativeTo@@/'),
  params: helpers.fromJson('/@@options.params@@/'),
  pushStateToSessionHistoryStack: /@@options.pushStateToSessionHistoryStack@@/,
};
const refs = {OutletRouter: /@@refs.OutletRouter@@/};
// ======= SCRIPT PLACEHOLDERS END =======

try {
  await refs.OutletRouter.navigate(url ?? undefined, {
    outlet: options.outlet ?? undefined,
    relativeTo: options.relativeTo ?? undefined,
    params: options.params ?? undefined,
    pushStateToSessionHistoryStack: options.pushStateToSessionHistoryStack ?? undefined,
  });
  callback(null);
}
catch (error) {
  callback(error.message || `${error}` || 'ERROR');
}
