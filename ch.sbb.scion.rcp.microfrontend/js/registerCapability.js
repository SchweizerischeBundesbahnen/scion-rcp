try {
  const id = await ${refs.ManifestService}.registerCapability(${helpers.fromJson}('${capability}'));
  window['${callback}'](null, id);
}
catch (error) {
  window['${callback}'](error.message || `${error}` || 'ERROR');
}