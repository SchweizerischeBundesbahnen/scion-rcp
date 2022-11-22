(() => {
  // ======= SCRIPT PLACEHOLDERS BEGIN =======
  const helpers = {fromJson: /@@helpers.fromJson@@/};
  const refs = {MessageClient: /@@refs.MessageClient@@/};
  const topic = helpers.fromJson('/@@topic@@/');
  // ======= SCRIPT PLACEHOLDERS END =======

  return refs.MessageClient.observe$(topic);
})()
