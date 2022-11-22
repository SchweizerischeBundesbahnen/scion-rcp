(() => {
  // ======= SCRIPT PLACEHOLDERS BEGIN =======
  const refs = {IntentClient: /@@refs.IntentClient@@/};
  const helpers = {fromJson: /@@helpers.fromJson@@/};
  const selector = {
    type: helpers.fromJson('/@@selector.type@@/'),
    qualifier: helpers.fromJson('/@@selector.qualifier@@/'),
  };
  // ======= SCRIPT PLACEHOLDERS END =======

  return refs.IntentClient.observe$({
    type: selector.type ?? undefined,
    qualifier: selector.qualifier ?? undefined,
  })
})()
