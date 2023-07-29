let changeables;

function reloadFitInputs() {
    changeables = document.querySelectorAll('#changeable');

    for (let i = 0; i < changeables.length; i++) {
        inputFit(changeables[i]);
    
        changeables[i].addEventListener('input', function (event) {
            const div = event.target.parentNode;
            inputFit(div);
        });
    }
}

function inputFit(div) {
    const input = div.querySelector("input");
    const span = div.querySelector("span");

    span.innerHTML = input.value.replace(/\s/g, '&nbsp;');
    input.style.width = span.offsetWidth + 'px';
}

