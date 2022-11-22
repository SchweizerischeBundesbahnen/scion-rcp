// ======= SCRIPT PLACEHOLDERS BEGIN =======
const outletId = '/@@outletId@@/';
const name = '/@@name@@/';
const callback = window['/@@callback@@/'];
// ======= SCRIPT PLACEHOLDERS END =======

const sciRouterOutlet = document.querySelector(`sci-router-outlet[name="${outletId}"]`);
const removed = sciRouterOutlet.removeContextValue(name);
callback(removed);
