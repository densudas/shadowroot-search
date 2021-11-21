[![Build Status](https://www.travis-ci.com/densudas/shadowroot-search.svg?branch=main)](https://travis-ci.com/github/densudas/shadowroot-search "Travis CI")
[![badge-jdk](https://img.shields.io/badge/jdk-8-green.svg)](https://www.oracle.com/java/technologies/javase-downloads.html)
[![codecov](https://codecov.io/gh/densudas/shadowroot-search/branch/main/graph/badge.svg?token=3BMuAiVfbk)](https://codecov.io/gh/densudas/shadowroot-search)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# shadowroot-search

shadowroot-search helps to find elements hidden inside [Shadow DOM](https://www.w3.org/TR/shadow-dom/) roots with simple
Css or Xpath locators using Selenium

## Examples

Find element by CSS, Xpath, tagName, class, etc:

```
ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
WebElement element = shadowRootSearch.findElement(By.cssSelector("button"));
```

Find elements by CSS, Xpath, tagName, class, etc:

```
ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
List<WebElement> elements = shadowRootSearch.findElements(By.xpath("//button"));
```

### Elements with JS executable element path

Following methods return found WebElements with it's js executable path. Element Path can be used to direct access a
shadow dom element next time:

```
String script = "return document" + 
".querySelector('div[id=\'one\']').shadowRoot.querySelector('span[id=\'two\']').shadowRoot"

WebElement shadowRootSection = (WebElement) ((JavascriptExecutor) driver).executeScript(script);
WebElement element = shadowRootSection.findElement(By.cssSelector("button"));
```

Find element with path by CSS, Xpath, tagName, class, etc:

```
ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
Map<String, Object> elementWithPath = shadowRootSearch.findElementWithShadowPath(By.cssSelector("button"));

Returns a Map:

[
"elementPath" -> ".querySelector('div[id=\'one\']').shadowRoot.querySelector('span[id=\'two\']').shadowRoot",
"element" -> WebElement
]
```

Find elements with path by CSS, Xpath, tagName, class, etc:

```
ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
List<Map<String, Object>> elementsWithPath = shadowRootSearch.findElementsWithShadowPath(By.xpath("//button"));

Returns a List<Map<String, Object>>:

[
{"elementPath" -> ".querySelector('div[id=\'one\']').shadowRoot.querySelector('span[id=\'two\']').shadowRoot",
 "element" -> WebElement},
{"elementPath" -> ".querySelector('div[id=\'three\']').shadowRoot.querySelector('span[id=\'four\']').shadowRoot",
 "element" -> WebElement}
]
```

## Limitations

```
<div>
  #shadow-root
    <button id="inside-shadow-root"></button>
  <button id="outside-shadow-root"></button>
</div>
```

- Elements can not be found by Xpath at the first level of shadow-root element:
    - Button with id="inside-shadow-root" inside shadow-root `//button[@id='inside-shadow-root']` **will not** be found
      by xpath

- Elements using different level nods (inside and outside shadowRoot) can not be found by CSS or Xpath:
    - Button with id="inside-shadow-root" inside shadow-root **will not** be found by
      css `div button#inside-shadow-root` or xpath `//div//button[@id='inside-shadow-root']` 
