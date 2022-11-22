// ======= SCRIPT PLACEHOLDERS BEGIN =======
const refs = {IntentClient: /@@refs.IntentClient@@/};
const helpers = {fromJson: /@@helpers.fromJson@@/};
const callback = window['/@@callback@@/'];
const intent = helpers.fromJson('/@@intent@@/');
const body = helpers.fromJson('/@@body@@/');
const options = {
  headers: helpers.fromJson('/@@options.headers@@/'),
};
// ======= SCRIPT PLACEHOLDERS END =======

try {
  await refs.IntentClient.publish(intent, body ?? null, {
    headers: options.headers ?? undefined,
  });
  callback(null);
}
catch (error) {
  callback(error.message ?? `${error}` ?? 'ERROR');
}
