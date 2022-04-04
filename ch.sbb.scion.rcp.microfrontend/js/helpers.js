(() => {
  window['__SCION_RCP'] = window['__SCION_RCP'] || {};
  window['__SCION_RCP'].storage = {};
  window['__SCION_RCP'].helpers = {
    JSON: {stringify, parse},
  };

  /**
   * JavaScript JSON serialization does not support {Map} and {Set} collections. By default, this utility serializes a
   * {Map} to a dictionary and a {Set} to an array.
   *
   * To retain the collection type, you can pass following rules:
   * - 'Map=>MapObject'
   *   Converts {Map} objects into custom objects that can be unmarshalled using {@link #parse}.
   * - 'Set=>SetObject'
   *   Converts {Set} objects into custom objects that can be unmarshalled using {@link #parse}.
   */
  function stringify(object, ...rules) {
    return JSON.stringify(object, (key, value) => {
      // Marshalling of Maps: JSON does not yet support maps. Therefore, we convert Map objects to custom objects containing the contents of the map as an array of tuples.
      if (isMapLike(value)) {
        if (rules?.includes('Map=>MapObject')) {
          return {__type: 'Map', __value: [...value]};
        }
        return toDictionary(value);
      }
      if (isSetLike(value)) {
        if (rules?.includes('Set=>SetObject')) {
          return {__type: 'Set', __value: [...value]};
        }
        return [...value];
      }
      return value;
    });
  }

  function parse(json) {
    return JSON.parse(json, (key, value) => {
      if (value?.__type === 'Map' && Array.isArray(value.__value)) {
        return new Map(value.__value);
      }
      if (value?.__type === 'Set' && Array.isArray(value.__value)) {
        return new Set(value.__value);
      }
      return value;
    });
  }

  function isMapLike(object) {
    if (object instanceof Map) {
      return true;
    }
    // Data sent from one JavaScript realm to another is serialized with the structured clone algorithm.
    // Although the algorithm supports the 'Map' data type, a deserialized map object cannot be checked to be instance of 'Map'.
    if (object && typeof object === 'object' && object.constructor.toString().includes('function Map()')) {
      return true;
    }
    return false;
  }

  function isSetLike(object) {
    if (object instanceof Set) {
      return true;
    }
    // Data sent from one JavaScript realm to another is serialized with the structured clone algorithm.
    // Although the algorithm supports the 'Map' data type, a deserialized map object cannot be checked to be instance of 'Map'.
    if (object && typeof object === 'object' && object.constructor.toString().includes('function Set()')) {
      return true;
    }
    return false;
  }

  function toDictionary(map) {
    if (map === null || map === undefined) {
      return map;
    }
    return Array
    .from(map.entries())
    .reduce(
        (dictionary, [key, value]) => {
          dictionary[key] = value;
          return dictionary;
        },
        {},
    );
  }
})();


