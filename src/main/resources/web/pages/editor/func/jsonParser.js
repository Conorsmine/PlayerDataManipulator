function parseJSONToData(json) {
  return recursiveParse(json, [], 0);
}
  
function recursiveParse(jsonObject, list, indent) {
  const keys = Object.keys(jsonObject);
  if (keys.length == 0) return;

  const newIndent = (indent + 1);
  for (let i = 0; i < keys.length; i++) {
    let key = keys[i];
    const json = jsonObject[key];
    const dataType = json["type"];
    const path = json["absolute_path"];
    const val = json["value"];

    // Map
    if (DataTypes[dataType] == DataTypes.MAP) {
       if (Object.keys(val).length == 0) list.push(new Data(key, new Map(), path, dataType, indent, false));
       else list.push(new Data(key, recursiveParse(val, [], newIndent), path, dataType, indent, false));
    }

    // Array
    else if (DataTypes[dataType] == DataTypes.ARRAY) {
      if (val.length == 0) list.push(new Data(key, [], path, dataType, indent, false));
      else list.push(new Data(key, recursiveParse(val, [], newIndent), path, dataType, indent, false));
    }

    // primitives
    else {
      list.push(new Data(key, val, path, dataType, indent, true));
    }
  }

  return list;
}