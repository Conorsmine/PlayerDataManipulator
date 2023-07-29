let sectionButtons;

function reloadSectionButtons() {
    sectionButtons = document.querySelectorAll('.section');

    for (let i = 0; i < sectionButtons.length; i++) {
        sectionButtons[i].addEventListener('click', function (event) {
            const div = event.target.parentNode;
            toggleExpand(div);
        });
    }
}

function toggleExpand(div) {
    if (div.dataset.indexpath == undefined) return;

    let data = new Data("", showData, "", undefined, 0, false);
    const indexPathArr = div.dataset.indexpath.split("-");

    for (let i = 0; i < indexPathArr.length; i++) {
        const index = indexPathArr[i];

        data = data.value[index];
    }

    data.isExpanded = !data.isExpanded;
    renderPageElements();
}