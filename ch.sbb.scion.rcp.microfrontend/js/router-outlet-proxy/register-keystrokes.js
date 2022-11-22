// ======= SCRIPT PLACEHOLDERS BEGIN =======
const helpers = {fromJson: /@@helpers.fromJson@@/};
const outletId = '/@@outletId@@/';
const keystrokes = helpers.fromJson('/@@keystrokes@@/');
// ======= SCRIPT PLACEHOLDERS END =======

const sciRouterOutlet = document.querySelector(`sci-router-outlet[name="${outletId}"]`);
sciRouterOutlet.keystrokes = keystrokes;
