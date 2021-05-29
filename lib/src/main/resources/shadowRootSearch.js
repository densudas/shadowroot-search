function findElement(root, querySelector) {
  let element = root.querySelector(querySelector);
  if (element) {
    return element
  }
  const allDeepNodes = collectAllDeepNodes(root);
  return findElementInDeepNodes(querySelector, allDeepNodes)
}

function findElements(root, querySelector) {
  const allDeepNodes = collectAllDeepNodes(root);
  let NodeList;
  NodeList = [
    ...root.querySelectorAll(querySelector),
    ...findElementsInDeepNodes(querySelector, allDeepNodes)
  ];
  return NodeList
}

function findElementWithShadowPath(root, querySelector) {
  const allDeepNodes = collectAllDeepNodes(root);
  let element = root.querySelector(querySelector);
  if (element) {
    return {elementPath: '', element: element}
  }
  let elementWithPath = findElementWithShadowPathInDeepNodes(querySelector, allDeepNodes);
  if (elementWithPath && elementWithPath.element) {
    elementWithPath = fixElementPath(root, elementWithPath)
  }
  return elementWithPath
}

function findElementsWithShadowPath(root, querySelector) {
  const allDeepNodes = collectAllDeepNodes(root);
  let elementsWithPath = [];
  root.querySelectorAll(querySelector).forEach(element => {
    elementsWithPath.push({
      elementPath: '', element: element
    })
  });
  elementsWithPath = [...elementsWithPath, ...findElementsWithShadowPathInDeepNodes(querySelector, allDeepNodes)];
  return fixElementsWithPath(root, elementsWithPath)
}

function findElementByXpath(root, xpath) {
  let element = getElementByXpath(root, xpath);
  if (element) {
    return element
  }
  const allDeepNodes = collectAllDeepNodes(root);
  return findElementByXpathInDeepNodes(allDeepNodes, xpath)
}

function findElementsByXpath(root, xpath) {
  const allDeepNodes = collectAllDeepNodes(root);
  let NodeList;
  NodeList = [
    ...getElementsByXpath(root, xpath),
    ...findElementsByXpathInDeepNodes(allDeepNodes, xpath)
  ];
  return NodeList
}

function findElementWithShadowPathByXpath(root, xpath) {
  const allDeepNodes = collectAllDeepNodes(root);
  let element = getElementByXpath(root, xpath);
  if (element) {
    return {elementPath: '', element: element}
  }
  let elementWithPath = findElementWithShadowPathByXpathInDeepNodes(allDeepNodes, xpath);
  if (elementWithPath && elementWithPath.element) {
    elementWithPath = fixElementPath(root, elementWithPath)
  }
  return elementWithPath
}

function findElementsWithShadowPathByXpath(root, xpath) {
  const allDeepNodes = collectAllDeepNodes(root);
  let elementsWithPath = [];
  getElementsByXpath(root, xpath).forEach(element => {
    elementsWithPath.push({elementPath: '', element: element})
  });
  elementsWithPath = [...elementsWithPath, ...findElementsWithShadowPathByXpathInDeepNodes(allDeepNodes, xpath)];
  return fixElementsWithPath(root, elementsWithPath)
}

function fixElementPath(root, elementWithPath) {
  if (root !== document) {
    let rootLocator = getElementLocator(root);
    let stringMatch = elementWithPath.elementPath.match(/^\.querySelector\("(.*)"\)(.*)/);
    if (stringMatch && stringMatch[1] === rootLocator) {
      elementWithPath.elementPath = stringMatch[2]
    }
  }
  return elementWithPath
}

function fixElementsWithPath(root, elementsWithPath) {
  if (root !== document) {
    let rootLocator = getElementLocator(root);
    for (let i = 0; i < elementsWithPath.length; i++) {
      let stringMatch = elementsWithPath[i].elementPath.match(/^\.querySelector\("(.*?)"\)(.*)/);
      if (stringMatch && stringMatch[1] === rootLocator) {
        elementsWithPath[i].elementPath = stringMatch[2]
      }
    }
  }
  return elementsWithPath
}

function getElementByXpath(root, xpath) {
  return document
    .evaluate(xpath, root, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null)
    .singleNodeValue
}

function getElementsByXpath(root, xpath) {
  let NodeList = [];
  let nodesSnapshot = document.evaluate(xpath, root,
    null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);
  for (let i = 0; i < nodesSnapshot.snapshotLength; i++) {
    NodeList.push(nodesSnapshot.snapshotItem(i))
  }
  return NodeList
}

function findElementByXpathInDeepNodes(nodes, xpath) {
  if (Array.isArray(nodes)) {
    let element = null;
    for (let i = 0; i < nodes.length; i++) {
      element = findElementByXpathInDeepNodes(nodes[i], xpath);
      if (element) {
        return element
      }
    }
    return element
  } else {
    if (nodes) {
      let allNodes = nodes.shadowRoot.childNodes;
      for (let i = 0; i < allNodes.length; i++) {
        let element = getElementByXpath(allNodes[i], xpath);
        if (element) {
          return element
        }
      }
    }
    return null
  }
}

function findElementsByXpathInDeepNodes(nodes, xpath) {
  let NodeList = [];
  const searchInEachNode = function (nodes, xpath) {
    let NodeList = [];
    for (let i = 0; i < nodes.length; i++) {
      let elements = getElementsByXpath(nodes[i], xpath);
      if (elements.length !== 0) {
        NodeList.push(...elements)
      }
    }
    return NodeList
  };
  const searchForElements = function (nodes) {
    if (Array.isArray(nodes)) {
      for (let i = 0; i < nodes.length; i++) {
        searchForElements(nodes[i])
      }
    } else {
      if (nodes) {
        let allNodes = nodes.shadowRoot.childNodes;
        let foundElements = searchInEachNode(allNodes, xpath);
        if (foundElements.length !== 0) {
          NodeList.push(...foundElements)
        }
      }
    }
  };
  searchForElements(nodes);
  return NodeList
}


function findElementWithShadowPathByXpathInDeepNodes(nodes, xpath) {
  return searchForElementWithPathByXpathInDeepNodes(nodes, xpath, '');
}

function searchForElementWithPathByXpathInDeepNodes(nodes, xpath, shadowPath) {
  if (Array.isArray(nodes)) {
    let element = null;
    for (let i = 0; i < nodes.length; i++) {
      if ((nodes.length === 1 && !Array.isArray(nodes[i])) ||
        (i + 1 !== nodes.length && !Array.isArray(nodes[i]) && Array.isArray(nodes[i + 1]))) {
        element = searchForElementWithPathByXpathInDeepNodes(nodes[i], xpath,
          `${shadowPath}.querySelector("${getElementLocator(nodes[i])}").shadowRoot`)
      } else if (i === 1 && Array.isArray(nodes[i]) && !Array.isArray(nodes[i - 1])) {
        element = searchForElementWithPathByXpathInDeepNodes(nodes[i], xpath,
          `${shadowPath}.querySelector("${getElementLocator(nodes[i - 1])}").shadowRoot`)
      } else {
        element = searchForElementWithPathByXpathInDeepNodes(nodes[i], xpath, shadowPath)
      }
      if (element) {
        return element
      }
    }
    return null
  } else {
    if (nodes) {
      let allNodes = nodes.shadowRoot.childNodes;
      for (let i = 0; i < allNodes.length; i++) {
        let element = getElementByXpath(allNodes[i], xpath);
        if (element) {
          return {elementPath: shadowPath, element: element}
        }
      }
      return null
    } else {
      return null
    }
  }
}

function findElementsWithShadowPathByXpathInDeepNodes(nodes, xpath) {
  let elementsWithPath = [];
  const searchInEachNode = function (nodes, xpath) {
    let NodeList = [];
    for (let i = 0; i < nodes.length; i++) {
      let elements = getElementsByXpath(nodes[i], xpath);
      if (elements.length !== 0) {
        NodeList.push(...elements)
      }
    }
    return NodeList
  };
  const searchForElements = function (nodes, shadowPath) {
    if (Array.isArray(nodes)) {
      proceedSearchInNodes(nodes, shadowPath, searchForElements)
    } else {
      if (nodes) {
        let allNodes = nodes.shadowRoot.childNodes;
        let foundElements = searchInEachNode(allNodes, xpath);
        if (foundElements.length !== 0) {
          foundElements.forEach(node => {
            elementsWithPath.push({
              elementPath: shadowPath, element: node
            })
          });
        }
      }
    }
  };
  searchForElements(nodes, '');
  return elementsWithPath
}

function findElementInDeepNodes(querySelector, nodes) {
  if (Array.isArray(nodes)) {
    let element = null;
    for (let i = 0; i < nodes.length; i++) {
      element = findElementInDeepNodes(querySelector, nodes[i]);
      if (element) {
        return element
      }
    }
    return element
  } else {
    if (nodes) {
      return nodes.shadowRoot.querySelector(querySelector)
    } else {
      return null
    }
  }
}

function findElementsInDeepNodes(querySelector, nodes) {
  let NodeList = [];
  const searchForElements = function (nodes) {
    if (Array.isArray(nodes)) {
      for (let i = 0; i < nodes.length; i++) {
        searchForElements(nodes[i])
      }
    } else {
      if (nodes) {
        let foundElements = nodes.shadowRoot.querySelectorAll(querySelector);
        if (foundElements.length !== 0) {
          NodeList.push(...foundElements)
        }
      }
    }
  };
  searchForElements(nodes);
  return NodeList
}

function findElementWithShadowPathInDeepNodes(querySelector, nodes) {
  return searchForElementWithPathInDeepNodes(querySelector, nodes, '');
}

function searchForElementWithPathInDeepNodes(querySelector, nodes, shadowPath) {
  if (Array.isArray(nodes)) {
    let element = null;
    for (let i = 0; i < nodes.length; i++) {
      if (nodes.length === 1 && !Array.isArray(nodes[i])) {
        element = searchForElementWithPathInDeepNodes(querySelector, nodes[i],
          `${shadowPath}.querySelector("${getElementLocator(nodes[i])}").shadowRoot`)
      } else if (i + 1 !== nodes.length && !Array.isArray(nodes[i]) && Array.isArray(nodes[i + 1])) {
        element = searchForElementWithPathInDeepNodes(querySelector, nodes[i],
          `${shadowPath}.querySelector("${getElementLocator(nodes[i])}").shadowRoot`)
      } else if (i === 1 && Array.isArray(nodes[i]) && !Array.isArray(nodes[i - 1])) {
        element = searchForElementWithPathInDeepNodes(querySelector, nodes[i],
          `${shadowPath}.querySelector("${getElementLocator(nodes[i - 1])}").shadowRoot`)
      } else {
        element = searchForElementWithPathInDeepNodes(querySelector, nodes[i], shadowPath)
      }
      if (element) {
        return element
      }
    }
    return null
  } else {
    if (nodes) {
      let element = nodes.shadowRoot.querySelector(querySelector);
      if (element) {
        return {elementPath: shadowPath, element: element}
      } else {
        return null
      }
    } else {
      return null
    }
  }
}

function findElementsWithShadowPathInDeepNodes(querySelector, nodes) {
  let elementsWithPath = [];
  const searchForElements = function (nodes, shadowPath) {
    if (Array.isArray(nodes)) {
      proceedSearchInNodes(nodes, shadowPath, searchForElements)
    } else {
      if (nodes) {
        let foundElements = nodes.shadowRoot.querySelectorAll(querySelector);
        if (foundElements.length !== 0) {
          foundElements.forEach(node => {
            elementsWithPath.push({
              elementPath: shadowPath, element: node
            })
          });
        }
      }
    }
  };
  searchForElements(nodes, '');
  return elementsWithPath
}

function proceedSearchInNodes(nodes, shadowPath, searchForElements) {
  for (let i = 0; i < nodes.length; i++) {
    if ((nodes.length === 1 && !Array.isArray(nodes[i])) ||
      (i + 1 !== nodes.length && !Array.isArray(nodes[i]) && Array.isArray(nodes[i + 1]))) {
      searchForElements(nodes[i], `${shadowPath}.querySelector("${getElementLocator(nodes[i])}").shadowRoot`)
    } else if (i === 1 && Array.isArray(nodes[i]) && !Array.isArray(nodes[i - 1])) {
      searchForElements(nodes[i], `${shadowPath}.querySelector("${getElementLocator(nodes[i - 1])}").shadowRoot`)
    } else {
      searchForElements(nodes[i], shadowPath)
    }
  }
}

function getElementLocator(node) {
  let elementLocator;
  let childNodes = [...node.parentNode.childNodes]
    .filter(node => node.nodeType === Node.ELEMENT_NODE && node.tagName.toLowerCase() !== 'script');
  for (let i = 0; i < childNodes.length; i++) {
    if (childNodes[i] === node) {
      let tag = node.tagName.toLowerCase();
      let attributes = node.attributes;
      let attributesString = '';
      let checkAttributes = ['id', 'class', 'title', 'data-original-title'];
      if (attributes instanceof NamedNodeMap && attributes.length > 0) {
        for (let i = 0; i < checkAttributes.length; i++) {
          let attribute = attributes.getNamedItem(checkAttributes[i]);
          if (attribute) {
            attributesString += `[${checkAttributes[i]}='${attribute.value}']`
          }
        }
      }
      elementLocator = `${tag}${attributesString}:nth-child(${i + 1})`;
      break;
    }
  }
  return elementLocator
}

function collectAllDeepNodes(root) {
  const allDeepNodes = [];
  const findDeepNodes = function (nodes) {
    let newDeepNode = [];
    for (let i = 0; i < nodes.length; i++) {
      if (nodes[i] && nodes[i].shadowRoot) {
        let deepNodes = findDeepNodes(nodes[i].shadowRoot.querySelectorAll('*'));
        if (deepNodes.length === 0) {
          newDeepNode.push(nodes[i])
        } else {
          newDeepNode.push([nodes[i], deepNodes])
        }
      }
    }
    return newDeepNode
  };

  if (root && root.shadowRoot) {
    let deepNodes = findDeepNodes(root.shadowRoot.querySelectorAll('*'));
    if (deepNodes.length === 0) {
      allDeepNodes.push(root)
    } else {
      allDeepNodes.push([root, deepNodes])
    }
  }
  let deepNodes = findDeepNodes(root.querySelectorAll('*'));
  if (deepNodes.length !== 0) {
    allDeepNodes.push(...deepNodes)
  }
  return allDeepNodes
}