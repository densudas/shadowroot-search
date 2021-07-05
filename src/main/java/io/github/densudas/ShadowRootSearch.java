package io.github.densudas;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShadowRootSearch {

  private final WebDriver DRIVER;
  private static final String CSS_SELECTOR = "cssSelector";
  private static final String XPATH = "xpath";
  private static final String SELECTOR_REGEX = "^By\\.(\\w+): (.*)$";
  private static final String JS_FILE = "shadowRootSearch.js";

  public ShadowRootSearch(WebDriver driver) {
    DRIVER = driver;
  }

  public WebDriver getDriver() {
    return DRIVER;
  }

  public WebElement findElement(By selector) throws Exception {
    return findElement(getDocument(), selector);
  }

  /**
   * Finds element by cssSelector. It also searches in every shadowRoot
   *
   * @param rootNode {@link WebElement} search from node
   * @param selector {@link By} selector
   * @return {@link WebElement} element
   * @throws Exception exception
   */
  public WebElement findElement(WebElement rootNode, By selector) throws Exception {
    WebElement element;
    ArrayList<String> selectorMatch = matchSelectorRegex(selector.toString());
    String locatorType = selectorMatch.get(1);
    String locator = selectorMatch.get(2);
    String script;

    if (CSS_SELECTOR.equals(locatorType)) {
      script = String.format("return findElement(arguments[0], \"%s\");", escapeQuotes(locator));
    } else if (XPATH.equals(locatorType)) {
      script =
          String.format("return findElementByXpath(arguments[0], \"%s\");", escapeQuotes(locator));
    } else {
      script =
          String.format(
              "return findElement(arguments[0], \"%s\");",
              escapeQuotes(locatorToCss(locatorType, locator)));
    }

    element = (WebElement) executeJsFunction(rootNode, script);
    if (element == null) {
      throw new NoSuchElementException(
          "Unable to locate element by " + locatorType + ": " + locator);
    }
    fixLocator(locator, locatorType, element);

    return element;
  }

  public Map<String, Object> findElementWithShadowPath(By selector) throws Exception {
    return findElementWithShadowPath(getDocument(), selector);
  }

  /**
   * Finds element by cssSelector. Returns with shadowPath, which can be used to straight access to
   * found element: with elementPath: ".querySelector("div").shadowRoot" and querySelector: "span"
   * element can be found by: root.querySelector("div").shadowRoot.querySelector("span")
   *
   * <pre>output example:{@code
   * [
   * "elementPath" -> ".querySelector('div[id=\'one\']').shadowRoot.querySelector('span[id=\'two\']').shadowRoot",
   * "element" -> WebElement
   * ]
   * }</pre>
   *
   * @param rootNode {@link WebElement} search from node
   * @param selector {@link By} selector
   * @return {@link Map} ["elementPath": String, "element": WebElement]
   * @throws Exception exception
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> findElementWithShadowPath(WebElement rootNode, By selector)
      throws Exception {
    Map<String, Object> foundElementWithPath;
    WebElement element;
    ArrayList<String> selectorMatch = matchSelectorRegex(selector.toString());
    String locatorType = selectorMatch.get(1);
    String locator = selectorMatch.get(2);
    String script;

    if (CSS_SELECTOR.equals(locatorType)) {
      script =
          String.format(
              "return findElementWithShadowPath(arguments[0], \"%s\");", escapeQuotes(locator));
    } else if (XPATH.equals(locatorType)) {
      script =
          String.format(
              "return findElementWithShadowPathByXpath(arguments[0], \"%s\");",
              escapeQuotes(locator));
    } else {
      script =
          String.format(
              "return findElementWithShadowPath(arguments[0], \"%s\");",
              escapeQuotes(locatorToCss(locatorType, locator)));
    }

    foundElementWithPath = (Map<String, Object>) executeJsFunction(rootNode, script);
    if (foundElementWithPath == null) {
      throw new NoSuchElementException(
          "Unable to locate elementWithPath by " + locatorType + ": " + locator);
    }
    element = (WebElement) foundElementWithPath.get("element");
    fixLocator(locator, locatorType, element);

    return new HashMap<String, Object>() {
      {
        put("element", element);
        put("elementPath", foundElementWithPath.get("elementPath"));
      }
    };
  }

  public ArrayList<WebElement> findElements(By selector) throws Exception {
    return findElements(getDocument(), selector);
  }

  /**
   * Finds elements by cssSelector. It also searches in every shadowRoot
   *
   * @param rootNode {@link WebElement} search from node
   * @param selector {@link By} selector
   * @return list of elements
   * @throws Exception exception
   */
  @SuppressWarnings("unchecked")
  public ArrayList<WebElement> findElements(WebElement rootNode, By selector) throws Exception {
    ArrayList<WebElement> elements;
    ArrayList<String> selectorMatch = matchSelectorRegex(selector.toString());
    String locatorType = selectorMatch.get(1);
    String locator = selectorMatch.get(2);
    String script;

    if (CSS_SELECTOR.equals(locatorType)) {
      script = String.format("return findElements(arguments[0], \"%s\");", escapeQuotes(locator));
    } else if (XPATH.equals(locatorType)) {
      script =
          String.format("return findElementsByXpath(arguments[0], \"%s\");", escapeQuotes(locator));
    } else {
      script =
          String.format(
              "return findElements(arguments[0], \"%s\");",
              escapeQuotes(locatorToCss(locatorType, locator)));
    }

    elements = (ArrayList<WebElement>) executeJsFunction(rootNode, script);
    if (elements == null) {
      elements = new ArrayList<>();
    }
    elements.forEach(webElement -> fixLocator(locator, locatorType, webElement));

    return elements;
  }

  public List<Map<String, Object>> findElementsWithShadowPath(By selector) throws Exception {
    return findElementsWithShadowPath(getDocument(), selector);
  }

  /**
   * Finds elements in each shadowRoot and return them with path to theirs shadowRoot ancestor
   *
   * <pre>output: {@code
   * [
   * {"elementPath" -> ".querySelector('div[id=\'one\']').shadowRoot.querySelector('span[id=\'two\']').shadowRoot",
   *  "element" -> WebElement},
   * {"elementPath" -> ".querySelector('div[id=\'three\']').shadowRoot.querySelector('span[id=\'four\']').shadowRoot",
   *  "element" -> WebElement}
   * ]
   * }</pre>
   *
   * @param rootNode {@link WebElement} search from node
   * @param selector {@link By} selector
   * @return list of elements with path
   * @throws Exception exception
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> findElementsWithShadowPath(WebElement rootNode, By selector)
      throws Exception {
    ArrayList<Map<String, Object>> elementsWithShadowPath;
    ArrayList<String> selectorMatch = matchSelectorRegex(selector.toString());
    String locatorType = selectorMatch.get(1);
    String locator = selectorMatch.get(2);
    String script;

    if (CSS_SELECTOR.equals(locatorType)) {
      script =
          String.format(
              "return findElementsWithShadowPath(arguments[0], \"%s\");", escapeQuotes(locator));
    } else if (XPATH.equals(locatorType)) {
      script =
          String.format(
              "return findElementsWithShadowPathByXpath(arguments[0], \"%s\");",
              escapeQuotes(locator));
    } else {
      script =
          String.format(
              "return findElementsWithShadowPath(arguments[0], \"%s\");",
              escapeQuotes(locatorToCss(locatorType, locator)));
    }

    elementsWithShadowPath = (ArrayList<Map<String, Object>>) executeJsFunction(rootNode, script);
    if (elementsWithShadowPath == null) {
      elementsWithShadowPath = new ArrayList<>();
    }
    return getElementsWithFixedLocators(locator, locatorType, elementsWithShadowPath);
  }

  private List<Map<String, Object>> getElementsWithFixedLocators(
      String locator, String locatorType, List<Map<String, Object>> elements) {
    List<Map<String, Object>> fixedElements = new ArrayList<>();
    for (Map<String, Object> element : elements) {
      WebElement webElement = (WebElement) element.get("element");
      fixLocator(locator, locatorType, webElement);
      fixedElements.add(
          new HashMap<String, Object>() {
            {
              put("element", webElement);
              put("elementPath", element.get("elementPath"));
            }
          });
    }
    return fixedElements;
  }

  private void fixLocator(String locator, String locatorType, WebElement element) {
    if (element instanceof RemoteWebElement) {
      try {
        Class<?>[] parameterTypes = new Class[] {SearchContext.class, String.class, String.class};
        Method m = element.getClass().getDeclaredMethod("setFoundBy", parameterTypes);
        m.setAccessible(true);
        Object[] parameters = new Object[] {DRIVER, locatorType, locator};
        m.invoke(element, parameters);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private String locatorToCss(String type, String locator) throws Exception {
    String cssLocator;
    locator = escapeQuotes(locator);

    switch (type) {
      case "id":
        cssLocator = "#" + locator;
        break;
      case "className":
        cssLocator = "." + locator;
        break;
      case "linkText":
        cssLocator = "[href=\"" + locator + "\"]";
        break;
      case "partialLinkText":
        cssLocator = "[href*=\"" + locator + "\"]";
        break;
      case "name":
        cssLocator = "[name=\"" + locator + "\"]";
        break;
      case "tagName":
        cssLocator = locator;
        break;
      default:
        throw new Exception("There is no such locator type: " + type);
    }

    return cssLocator;
  }

  private WebElement getDocument() {
    return (WebElement) executeScript("return document;");
  }

  private Object executeJsFunction(WebElement rootNode, String script) throws Exception {
    script = injectScript(script);
    return executeScript(script, rootNode);
  }

  private Object executeScript(String script, Object... args) {
    return ((JavascriptExecutor) DRIVER).executeScript(script, args);
  }

  private String injectScript(String script) throws Exception {
    return getJSFile() + script;
  }

  private StringBuilder getJSFile() throws URISyntaxException {
    URL resource = ShadowRootSearch.class.getClassLoader().getResource(JS_FILE);
    File jsFile;
    if (resource == null) {
      throw new IllegalArgumentException("file not found!");
    } else {
      jsFile = new File(resource.toURI());
    }

    StringBuilder text = new StringBuilder();
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(jsFile));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    try {
      assert reader != null;
      while (reader.ready()) {
        text.append(reader.readLine());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return text;
  }

  private static ArrayList<String> matchSelectorRegex(String selector) {
    ArrayList<String> groups = new ArrayList<>();
    Matcher matcher = Pattern.compile(SELECTOR_REGEX).matcher(selector);
    if (matcher.find()) {
      for (int i = 0; i <= matcher.groupCount(); i++) {
        groups.add(matcher.group(i));
      }
    }
    return groups;
  }

  private String escapeQuotes(String str) {
    return str.replace("\"", "\\\"");
  }
}
