function isMapOrArray(data) {
  return (DataTypes[data.dataType] == DataTypes.ARRAY || DataTypes[data.dataType] == DataTypes.MAP);
}

function calcLenOfSubData(dataArr) {
  let len = dataArr.length;

  for (let i = 0; i < dataArr.length; i++) {
    const data = dataArr[i];

    if (data.isExpanded == false) continue;
    if (isMapOrArray(data)) len += calcLenOfSubData(data.value);
  }

  return len;
}

function showDataToDomElements(domElement, dataArr) {
  const allSubDataElements = domElement.querySelectorAll("#sub_data");

  for (let i = 0; i < dataArr.length; i++) {
    const data = dataArr[i];
    const subDataElement = allSubDataElements[allSubDataElements.length - 1];
    
    if (isMapOrArray(data)) {
      const entriesLen = Object.keys(data.value).length;
      const val = (DataTypes[data.dataType] == DataTypes.ARRAY) ? `[ ${entriesLen} Entries ]` : `{ ${entriesLen} Entries }`;
    
      subDataElement.insertAdjacentHTML("beforeend", createUnmodifieableElementFrom(new Data(data.key, val, data.path, data.dataType, data.indent, data.isExpanded)));
      const sel = subDataElement.querySelectorAll("#sub_data");
      data.htmlElement = sel[sel.length - 1];

      if (entriesLen == 0) continue;
      showDataToDomElements(subDataElement, data.value);
    }

    // Primitive
    else { subDataElement.insertAdjacentHTML("beforeend", createPrimitiveElementToDom(data)); }
  }
}


 
function clearLines() {
  document.getElementById("connect_lines").innerHTML = '';
}

function renderLinesToDomElements(dataArr) {
    renderLengthData(dataArr, 0, 0);
}

function renderLengthData(dataArr, indent, yOffset) {
    let subDataLen = calcLenOfSubData(dataArr);
    const lastData = dataArr[(dataArr.length - 1)];

    if (lastData == undefined) return;
    if (lastData.isExpanded && isMapOrArray(lastData)) subDataLen -= calcLenOfSubData(lastData.value);
  
    addBarLine(yOffset, indent, subDataLen);
    for(let i = 0; i < dataArr.length; i++) {
      const data = dataArr[i];
      const offset = (yOffset + i);
      addConnectLine(offset, indent);
  
  
      if (isMapOrArray(data)) {
        
        if (data.isExpanded == false) continue;
        subDataLen -= calcLenOfSubData(lastData.value);
        renderLengthData(data.value, (indent + 1), (offset + 1));
        yOffset += calcLenOfSubData(data.value);
      }
    }
}



function clearSectionButtons() {
  document.getElementById("sidebar_lines").innerHTML = '<div class="section" style="--y:0"> </div>';
}

function renderSectionButtons(dataArr) {
    recursiveRenderSectionButtons(dataArr, []);
}

function recursiveRenderSectionButtons(dataArr, indexPathArr) {
    let previousOffset = 0;
  
    for (let i = 0; i < dataArr.length; i++) {
      const data = dataArr[i];
  
      if (!isMapOrArray(data)) continue;
      const copyPathArr = [...indexPathArr];
  
      copyPathArr.push(i);
      addSectionButton((i - previousOffset), copyPathArr.join('-'), data.isExpanded);
      previousOffset = (i + 1);
  
  
      if (data.isExpanded == false) continue;
      previousOffset += recursiveRenderSectionButtons(data.value, copyPathArr);
    }
  
    return (previousOffset - dataArr.length);
}



function renderPageElements() {
  createUUIDDomElement(json["${parsed_meta_data}"]["${parsed_meta_name}"], json["${parsed_meta_data}"]["${parsed_meta_uuid}"])
  showDataToDomElements(document, showData);
  renderLinesToDomElements(showData);
  renderSectionButtons(showData);

  reloadFitInputs();
  reloadSectionButtons();
  reloadSaveInputs();

  document.getElementById("cmd_text").innerHTML = `/pdm apply ${bytesToCmdCode(calcAllBytes(ID))}`;
}

function redrawElements() {
  clearSectionButtons();
  renderSectionButtons(showData);

  clearLines();
  renderLinesToDomElements(showData);

  reloadFitInputs();
}