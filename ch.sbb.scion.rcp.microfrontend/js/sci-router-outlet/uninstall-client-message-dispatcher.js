// ======= SCRIPT PLACEHOLDERS BEGIN =======
const uninstallStorageKey = '/@@uninstallStorageKey@@/';
const storage = /@@storage@@/;
// ======= SCRIPT PLACEHOLDERS END =======

storage[uninstallStorageKey]();
delete storage[uninstallStorageKey];
