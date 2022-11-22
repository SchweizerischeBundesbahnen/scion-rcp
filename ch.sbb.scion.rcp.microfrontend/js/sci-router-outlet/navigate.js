// ======= SCRIPT PLACEHOLDERS BEGIN =======
const pushStateToSessionHistoryStack = /@@pushStateToSessionHistoryStack@@/;
const url = '/@@url@@/';
// ======= SCRIPT PLACEHOLDERS END =======

if (pushStateToSessionHistoryStack) {
  window.location.assign(url);
}
else {
  window.location.replace(url);
}
