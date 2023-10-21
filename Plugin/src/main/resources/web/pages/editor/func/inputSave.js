function reloadSaveInputs() {
    inputs = document.querySelectorAll('#dynamicText');

    for (let i = 0; i < inputs.length; i++) {
    
        inputs[i].addEventListener('keyup', function (event) {
            const input = event.target;
            submitChange(input);
        });

        inputs[i].addEventListener('keypress', function (event) {
            if (!(event.key === "Enter")) return;
            event.target.blur();
        });
    }
}

function submitChange(input) {
    const path = input.dataset.path;
    const type = input.dataset.type;
    dataChanges.set(path, new ChangeData(input.value, path, type));
}