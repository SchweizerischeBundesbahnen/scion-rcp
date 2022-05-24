const sciRouterOutlet = document.querySelector('sci-router-outlet[name="${outletId}"]');
const removed = sciRouterOutlet.removeContextValue('${name}');
window['${callback}'](removed);