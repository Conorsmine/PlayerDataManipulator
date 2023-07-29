let cmdTextDiv;
let copyPopups;

function copyToClipboard() {
    updateDomsCopy();

    const copyText = cmdTextDiv.getElementsByTagName("p")[0].innerHTML;
    const clipboard = navigator.clipboard;
    
    if (clipboard == undefined) return;
    clipboard.writeText(copyText);
    copyPopups.innerHTML = `<div id="copy_popup" class="rounded_edges hide_me" style="--px:4px"> Copied to clipboard! </div>`;
}

function updateDomsCopy() {
    cmdTextDiv = document.getElementById("cmd");
    copyPopups = document.getElementById("copy_popup_container");
}