let sectionButtons = [];

function reloadSectionButtons() {
    removeButtonListeners();
    sectionButtons = document.querySelectorAll('#section_button');

    for (let i = 0; i < sectionButtons.length; i++) {
        sectionButtons[i].addEventListener('click', (event) => {
            const div = event.target.parentNode;
            toggleExpand(div);
        });
    }
}

function removeButtonListeners() {
    sectionButtons.forEach((button) => {
        button.removeEventListener('click', (event) => {});
    });
}

function toggleExpand(div) {
    if (div.dataset.indexpath == undefined) return;
    const indexPathArr = div.dataset.indexpath.split("-");

    let data = new Data("", showData, "", undefined, 0, false);
    for (let i = 0; i < indexPathArr.length; i++) {
        const index = indexPathArr[i];

        data = data.value[index];
    }

    const classes = data.htmlElement.classList;
    if (classes.contains("display_none")) {
        classes.remove("display_none");
        classes.add("display_inh");

        div.querySelector("img").src = '../../assets/opened_section_symbol.png';
    }
    else {
        classes.remove("display_inh");
        classes.add("display_none");

        div.querySelector("img").src = '../../assets/closed_section_symbol.png';
    }

    data.isExpanded = !data.isExpanded;

    redrawElements();
    reloadSectionButtons();
}