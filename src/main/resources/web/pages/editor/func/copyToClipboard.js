function copyToClipboard() {
    const uuidCode = document.getElementById("cmd").innerHTML;

    copyTextToClipboard(uuidCode);
}

function showPopupMsg() {
        document.getElementById("copy_popup_container").innerHTML = `<div id="copy_popup" class="rounded_edges hide_me" style="--px:4px"> Copied to clipboard! </div>`;
}

// https://stackoverflow.com/questions/400212/how-do-i-copy-to-the-clipboard-in-javascript
function fallbackCopyTextToClipboard(text) {
  try {
    document.getElementById("cmd").select();
    var successful = document.execCommand('copy');
    var msg = successful ? 'successful' : 'unsuccessful';
    showPopupMsg();
  } catch (err) { console.error('Fallback: Oops, unable to copy', err); }
}

function copyTextToClipboard(text) {
  if (!navigator.clipboard) {
    fallbackCopyTextToClipboard(text);
    return;
  }
  navigator.clipboard.writeText(text).then(
    function() { showPopupMsg(); },
    function(err) { console.error('Async: Could not copy text: ', err); }
  );
}