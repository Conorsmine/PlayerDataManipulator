let changesPopup;
let showCmd;
let saveButton;

document.body.addEventListener('click', (event) => {
    togglePopup(event);
}, true);

function togglePopup(event) {
    updateCmdDom();

    if (changesPopup.classList.contains("display_none")) showPopup(event);
    else hidePopup(event);
}

function showPopup(event) {
    if (!saveButton.contains(event.target)) return;

    changesPopup.classList.remove("display_none");
    changesPopup.classList.add("display_inh");

    sendChangesToServer();
}

function hidePopup(event) {
    if (showCmd.contains(event.target)) return;

    changesPopup.classList.remove("display_inh");
    changesPopup.classList.add("display_none");
}

function updateCmdDom() {
    changesPopup = document.getElementById("changes_popup");
    showCmd = document.getElementById("show_cmd");
    saveButton = document.getElementById("save_button");
}

function sendChangesToServer() {
    // Todo;
    //  Should probably also make it, so:
    //  I send changes, server receives them, sends back confirmation AND ONLY then show the generated CMD code
    //  Currently I'm just assuming the time it takes you to copy/paste the CMD code is longer than it takes to send
    const xhr = new XMLHttpRequest();
    xhr.open("POST", `/${url_changes_prefix}/${ID}.json`, true);

    const metaDataPath = '${parsed_meta_data}';
    const metaUUIDPath = '${parsed_meta_uuid}';
    xhr.send(JSON.stringify({${parsed_meta_data}: {${parsed_meta_uuid}:`${json[metaDataPath][metaUUIDPath]}`, ${parsed_meta_separator}: `${MOJ_SEP}`}, ${url_changes_prefix}: dataChangesToJson()}));
}

function dataChangesToJson() {
    const arr = [];
    const mapIterator = dataChanges.keys();
    let key = mapIterator.next().value;

    while (key != undefined) {
        const val = dataChanges.get(key).value;
        let dataType = dataChanges.get(key).dataType;
        if (val == undefined) continue;
        if (dataType == undefined) dataType = DataTypes[${datatype_STR}];

        arr.push({${parsed_player_path}: `${key}`, ${parsed_player_value}: `${val}`, ${parsed_player_type}: `${dataType}`});
        key = mapIterator.next().value;
    }

    return arr;
}