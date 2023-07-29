let changesPopup;
let showCmd;
let saveButton;

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
    const xhr = new XMLHttpRequest();
    xhr.open("POST", `/changes/${ID}.json`, true);

    xhr.send(JSON.stringify({meta_data: {uuid:`${json["meta_data"]["uuid"]}`}, changes: dataChangesToJson()}));
}

function dataChangesToJson() {
    const arr = [];
    const mapIterator = dataChanges.keys();
    let key = mapIterator.next().value;

    while (key != undefined) {
        const val = dataChanges.get(key).value;
        if (val == undefined) continue;

        arr.push({path: `${key}`, value: `${val}`});
        key = mapIterator.next().value;
    }

    return arr;
}