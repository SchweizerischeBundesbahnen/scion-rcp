(() => {
  // ======= SCRIPT PLACEHOLDERS BEGIN =======
  const helpers = {fromJson: /@@helpers.fromJson@@/};
  const refs = {MessageClient: /@@refs.MessageClient@@/};
  const topic = '/@@topic@@/';
  const request = helpers.fromJson('/@@request@@/');
  const options = {
    headers: helpers.fromJson('/@@options.headers@@/'),
  };
  // ======= SCRIPT PLACEHOLDERS END =======

  return refs.MessageClient.request$(topic, request ?? null, {
    headers: options.headers ?? undefined,
  });
})()
