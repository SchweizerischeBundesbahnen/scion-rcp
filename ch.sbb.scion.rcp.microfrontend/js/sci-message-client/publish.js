// ======= SCRIPT PLACEHOLDERS BEGIN =======
const helpers = {fromJson: /@@helpers.fromJson@@/};
const refs = {MessageClient: /@@refs.MessageClient@@/};
const callback = window['/@@callback@@/'];
const topic = helpers.fromJson('/@@topic@@/');
const message = helpers.fromJson('/@@message@@/');
const options = {
  headers: helpers.fromJson('/@@options.headers@@/'),
  retain: /@@options.retain@@/,
};
// ======= SCRIPT PLACEHOLDERS END =======

try {
  await refs.MessageClient.publish(topic, message ?? undefined, {
    headers: options.headers ?? undefined,
    retain: options.retain ?? undefined,
  });
  callback(null);
}
catch (error) {
  callback(error.message ?? `${error}` ?? 'ERROR');
}
