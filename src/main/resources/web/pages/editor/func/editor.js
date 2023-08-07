const UUID_REGEX = "${uuid_regex.regexp}";
const ID = new URL(window.location.href).searchParams.get("${url_id}");
if (ID == null || !ID.match(UUID_REGEX)) console.log("LOAD ERROR PAGE");

const DataTypes = {
    ${datatype_ARRAY}: "array.png",
    ${datatype_MAP}: "map.png",

    ${datatype_BYTE}: "byte.png",
    ${datatype_SHORT}: "short.png",
    ${datatype_INT}: "integer.png",
    ${datatype_LONG}: "long.png",

    ${datatype_FLOAT}: "float.png",
    ${datatype_DOUBLE}: "double.png",

    ${datatype_STR}: "string.png"
}

class Data {

  constructor(key, value, path, dataType, indent, isExpanded) {
    this.key = key;
    this.value = value;
    this.path = path;
    this.dataType = dataType;
    this.indent = indent;
    this.isExpanded = isExpanded;
    this.htmlElement;
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
    showData = parseJSONToData(json["${parsed_player_data}"]);
    renderPageElements();

    document.getElementById("cmd").innerHTML = `/pdm apply ${bytesToCmdCode(calcAllBytes(ID))}`;
});