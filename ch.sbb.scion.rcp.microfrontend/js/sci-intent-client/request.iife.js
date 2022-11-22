(() => {
  // ======= SCRIPT PLACEHOLDERS BEGIN =======
  const refs = {IntentClient: /@@refs.IntentClient@@/};
  const helpers = {fromJson: /@@helpers.fromJson@@/};
  const intent = helpers.fromJson('/@@intent@@/');
  const body = helpers.fromJson('/@@body@@/');
  const options = {
    headers: helpers.fromJson('/@@options.headers@@/'),
  };
  // ======= SCRIPT PLACEHOLDERS END =======

  return refs.IntentClient.request$(intent, body ?? null, {
    headers: options.headers ?? undefined,
  });
})()
