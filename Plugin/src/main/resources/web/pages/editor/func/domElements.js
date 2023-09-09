function createUUIDDomElement(playerName, uuid) {
    const UUID_DOM_IMG = DataTypes['${datatype_MAP}'];
  const divStructure = `
    <div id="uuid">
      <div class="data_line section">
        <div class="data_image prevent-select"> <img src='../../assets/data_types/${UUID_DOM_IMG}' /> </div>
        <div class="data_text">  ${playerName}</div>
        <div class="data_value"> [${uuid}]</div>
      </div>

      <div id="sub_data" class="display_inh"> </div>
    </div>
  `;
  
  document.getElementById("editor_lines").insertAdjacentHTML("afterbegin", divStructure);
}
    
function createUnmodifieableElementFrom(data) {
  return `
    <div>
      <div class="data_line section">
        <div class="indent" style="--indent:${data.indent}"> </div>
        <div class="data_image prevent-select"> <img src="../../assets/data_types/${DataTypes[data.dataType]}" /> </div>
        <div class="data_text"> ${data.key}: </div>
        <div class="data_value prevent-select"> ${data.value} </div>
      </div>

      <div id="sub_data" class="display_none"> </div>
    </div>
  `;
}
  
function createPrimitiveElementToDom(data) {
  return `
    <div class="section data_line">
      <div class="indent" style="--indent:${data.indent}"> </div>
      <div class="data_image prevent-select"> <img src="../../assets/data_types/${DataTypes[data.dataType]}" /> </div>
      <div class="data_text"> ${data.key}: </div>
      <div class="data_value" id="changeable">
        <input type="text" id="dynamicText" data-path=${data.path} value=${data.value} data-type=${data.dataType}> </input>
        <span> </span>
      </div>
    </div>
  `;
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
    sectionImg = "../../assets/opened_section_symbol.png";
  }
  else {
    sectionImg = "../../assets/closed_section_symbol.png";
  }

  document.getElementById("sidebar_lines").insertAdjacentHTML("beforeend", `<div id="section_button" class="section" style="--y:${yOffset}" data-indexpath="${indexPath}"> <img src=${sectionImg} /> </div>`);
}