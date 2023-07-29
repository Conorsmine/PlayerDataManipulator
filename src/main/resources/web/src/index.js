const ID = new URL(window.location.href).searchParams.get("id");

const DataTypes = {
    ARRAY: "array.png",
    MAP: "map.png",

    BYTE: "byte.png",
    SHORT: "short.png",
    INT: "integer.png",
    LONG: "long.png",

    FLOAT: "float.png",
    DOUBLE: "double.png",

    STR: "string.png"
}

class Data {

  constructor(key, value, path, dataType, indent, isExpanded) {
    this.key = key;
    this.value = value;
    this.path = path;
    this.dataType = dataType;
    this.indent = indent;
    this.isExpanded = isExpanded;
  }
}

class ChangeData {

    constructor(value, path) {
        this.value = value;
        this.path = path;
    }

    toJson() {
        return `{
            "path": "${path}",
            "value": "${value}"
        }`;
    }
}

const loadJSONFile = (file, callback) => {
    const xhr = new XMLHttpRequest();
    xhr.overrideMimeType('application/json');
    xhr.open('GET', file, true);
    xhr.onreadystatechange = () => {
        if (xhr.readyState === 4 && xhr.status === 200) {
            const jsonData = JSON.parse(xhr.responseText);
            callback(jsonData);
        }
    };
    xhr.send(null);
};


let showData;
let json;
const dataChanges = new Map();

loadJSONFile(`playerData/${ID}.json`, (jsonData) => {
    json = jsonData;
    showData = parseJSONToData(json["player_data"]);
    renderPageElements();
});