// ======= SCRIPT PLACEHOLDERS BEGIN =======
const helpers = {fromJson: /@@helpers.fromJson@@/};
const outletId = '/@@outletId@@/';
const name = '/@@name@@/';
const value = helpers.fromJson('/@@value@@/');
// ======= SCRIPT PLACEHOLDERS END =======

const sciRouterOutlet = document.querySelector(`sci-router-outlet[name="${outletId}"]`);
sciRouterOutlet.setContextValue(name, value);
