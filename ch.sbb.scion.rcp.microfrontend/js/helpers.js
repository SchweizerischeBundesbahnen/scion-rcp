(() => {
  window['__SCION_RCP'] = window['__SCION_RCP'] || {};
  window['__SCION_RCP'].storage = {};
  window['__SCION_RCP'].helpers = {toJson, fromJson};

  /**
   * JavaScript JSON serialization does not support {Map} and {Set} collections.
   *
   * This converter serializes a map and set into a custom object of the following form:
   *
   * - Map: `{__type: 'Map', __value: [...[key, value]]}`
   * - Set: `{__type: 'Set', __value: [...values]}`
   *
   * Use in conjunction with {@link #fromJson}, and in Java with {@link MapObjectTypeAdapterFactory} and {@link SetObjectTypeAdapterFactory}.
   */
  function toJson(object, options) {
    const json = JSON.stringify(object, (key, value) => {
      // Convert the map to a custom map object that contains the map's values in the field '__value' as array with arrays of map entries.
      // Each map entry is a two element array containing a key and a value.
      if (isMapLike(value)) {
        return {__type: 'Map', __value: [...value]};
      }
      // Convert the set to a custom map object that contains the set's values in the field '__value' as array.
      if (isSetLike(value)) {
        return {__type: 'Set', __value: [...value]};
      }
      return value;
    });

    if (options?.encode) {
      return btoa(encodeURIComponent(json));
    }
    return json;
  }

  /**
   * JavaScript JSON serialization does not support {Map} and {Set} collections.
   *
   * Use in conjunction with {@link #toJson}, and in Java with {@link MapObjectTypeAdapterFactory} and {@link SetObjectTypeAdapterFactory}.
   */
  function fromJson(json, options) {
    if (options?.decode) {
      json = decodeURIComponent(atob(json));
    }

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
})();


