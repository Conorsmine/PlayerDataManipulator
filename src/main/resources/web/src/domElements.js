function createUUIDDomElement(uuid) {
  const divStructure = `
    <div class="data_line">
      <div class="data_image"> <img src="assets/data_types/${DataTypes["MAP"]}" /> </div>
      <div class="data_text"> UUID: </div>
      <div class="data_value"> ${uuid} </div>
    </div>
  `;

  document.getElementById("editor_lines").insertAdjacentHTML("beforebegin", divStructure);
}
  
function createUnmodifieableElementFrom(data) {
    const divStructure = `
        <div class="data_line">
          <div class="indent" style="--indent:${data.indent}"> </div>
          <div class="data_image"> <img src="assets/data_types/${DataTypes[data.dataType]}" /> </div>
          <div class="data_text"> ${data.key}: </div>
          <div class="data_value prevent-select"> ${data.value} </div>
        </div>
    `;
  
    document.getElementById("editor_lines").insertAdjacentHTML("beforeend", divStructure);
}

function createPrimitiveElementToDom(data) {
    const divStructure = `
        <div class="data_line">
          <div class="indent" style="--indent:${data.indent}"> </div>
          <div class="data_image"> <img src="assets/data_types/${DataTypes[data.dataType]}" /> </div>
          <div class="data_text"> ${data.key}: </div>
          <div class="data_value" id="changeable">
            <input type="text" id="dynamicText" data-path=${data.path} value=${data.value}>
            <span> </span>
          </div>
        </div>
    `;
  
    document.getElementById("editor_lines").insertAdjacentHTML("beforeend", divStructure);
}

function addBarLine(y, indent, len) {
  document.getElementById("connect_lines").insertAdjacentHTML("beforeend", `<div class="bar_line" id="bar_line" style="--height:${len};--y:${y};--indent:${indent}"> </div>`);
}

function addConnectLine(y, indent) {
  document.getElementById("bar_line").insertAdjacentHTML("beforeend", `<div class="connect_line" style="--height:${y};--indent:${indent}"> </div>`);
}

function addSectionButton(yOffset, indexPath, isExpanded) {
  let sectionImg;

  if (isExpanded) {
    sectionImg = "assets/opened_section_symbol.png";
  }
  else {
    sectionImg = "assets/closed_section_symbol.png";
  }

  document.getElementById("sidebar_lines").insertAdjacentHTML("beforeend", `<div class="section" style="--y:${yOffset}" data-indexpath="${indexPath}"> <img src=${sectionImg} /> </div>`);
}