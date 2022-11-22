// ======= SCRIPT PLACEHOLDERS BEGIN =======
const storage = /@@storage@@/;
const subscriptionStorageKey = '/@@subscriptionStorageKey@@/';
// ======= SCRIPT PLACEHOLDERS END =======

storage[subscriptionStorageKey].unsubscribe();
delete storage[subscriptionStorageKey];
