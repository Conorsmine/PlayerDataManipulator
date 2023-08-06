function reloadSaveInputs() {
    inputs = document.querySelectorAll('#dynamicText');

    for (let i = 0; i < inputs.length; i++) {
    
        inputs[i].addEventListener('keypress', function (event) {
            if (!(event.key === "Enter")) return;

            const input = event.target;
            submitChange(input);
            input.blur();
        });
    }
}

function submitChange(input) {
    const path = input.dataset.path;
    dataChanges.set(path, new ChangeData(input.value, path));
}