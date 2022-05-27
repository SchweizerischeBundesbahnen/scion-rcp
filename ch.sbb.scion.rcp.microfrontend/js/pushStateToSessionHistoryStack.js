if (${pushStateToSessionHistoryStack}) {
  window.location.assign('${url}');
}
else {
  window.location.replace('${url}');
}