package io.github.densudas;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.How;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShadowRootSearch {

  private static final String JS_FILE = "shadowRootSearch.js";
  private final WebDriver DRIVER;

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
    LocatorMatcher locatorMatcher = new LocatorMatcher(selector);
    How locatorType = locatorMatcher.getLocatorType();
    String locator = locatorMatcher.getLocator();
    String script;

    switch (locatorType) {
      case CSS:
        script = String.format("return findElement(arguments[0], \"%s\");", escapeQuotes(locator));
        break;
      case XPATH:
        script =
            String.format(
                "return findElementByXpath(arguments[0], \"%s\");", escapeQuotes(locator));
        break;
      default:
        script =
            String.format(
                "return findElement(arguments[0], \"%s\");",
                escapeQuotes(locatorToCss(locatorType, locator)));
        break;
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
    LocatorMatcher locatorMatcher = new LocatorMatcher(selector);
    How locatorType = locatorMatcher.getLocatorType();
    String locator = locatorMatcher.getLocator();
    String script;

    switch (locatorType) {
      case CSS:
        script =
            String.format(
                "return findElementWithShadowPath(arguments[0], \"%s\");", escapeQuotes(locator));
        break;
      case XPATH:
        script =
            String.format(
                "return findElementWithShadowPathByXpath(arguments[0], \"%s\");",
                escapeQuotes(locator));
        break;
      default:
        script =
            String.format(
                "return findElementWithShadowPath(arguments[0], \"%s\");",
                escapeQuotes(locatorToCss(locatorType, locator)));
        break;
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
    LocatorMatcher locatorMatcher = new LocatorMatcher(selector);
    How locatorType = locatorMatcher.getLocatorType();
    String locator = locatorMatcher.getLocator();
    String script;

    switch (locatorType) {
      case CSS:
        script = String.format("return findElements(arguments[0], \"%s\");", escapeQuotes(locator));

        break;
      case XPATH:
        script =
            String.format(
                "return findElementsByXpath(arguments[0], \"%s\");", escapeQuotes(locator));
        break;
      default:
        script =
            String.format(
                "return findElements(arguments[0], \"%s\");",
                escapeQuotes(locatorToCss(locatorType, locator)));
        break;
    }

    elements = (ArrayList<WebElement>) executeJsFunction(rootNode, script);
    if (elements == null) {
      return new ArrayList<>();
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
    LocatorMatcher locatorMatcher = new LocatorMatcher(selector);
    How locatorType = locatorMatcher.getLocatorType();
    String locator = locatorMatcher.getLocator();
    String script;

    switch (locatorType) {
      case CSS:
        script =
            String.format(
                "return findElementsWithShadowPath(arguments[0], \"%s\");", escapeQuotes(locator));
        break;
      case XPATH:
        script =
            String.format(
                "return findElementsWithShadowPathByXpath(arguments[0], \"%s\");",
                escapeQuotes(locator));
        break;
      default:
        script =
            String.format(
                "return findElementsWithShadowPath(arguments[0], \"%s\");",
                escapeQuotes(locatorToCss(locatorType, locator)));
        break;
    }

    elementsWithShadowPath = (ArrayList<Map<String, Object>>) executeJsFunction(rootNode, script);
    if (elementsWithShadowPath == null) {
      return new ArrayList<>();
    }

    return getElementsWithFixedLocators(locator, locatorType, elementsWithShadowPath);
  }

  private List<Map<String, Object>> getElementsWithFixedLocators(
      String locator, How locatorType, List<Map<String, Object>> elements) {
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

  private void fixLocator(String locator, How locatorType, WebElement element) {
    if (element instanceof RemoteWebElement) {
      try {
        Class<?>[] parameterTypes = new Class[] {SearchContext.class, String.class, String.class};
        Method m = element.getClass().getDeclaredMethod("setFoundBy", parameterTypes);
        m.setAccessible(true);
        Object[] parameters = new Object[] {DRIVER, locatorType.toString(), locator};
        m.invoke(element, parameters);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private String locatorToCss(How type, String locator) throws Exception {
    String cssLocator;
    locator = escapeQuotes(locator);

    switch (type) {
      case ID:
        cssLocator = "#" + locator;
        break;
      case CLASS_NAME:
        cssLocator = "." + locator;
        break;
      case LINK_TEXT:
        cssLocator = "[href=\"" + locator + "\"]";
        break;
      case PARTIAL_LINK_TEXT:
        cssLocator = "[href*=\"" + locator + "\"]";
        break;
      case NAME:
        cssLocator = "[name=\"" + locator + "\"]";
        break;
      case TAG_NAME:
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

  private String getJSFile() throws IOException {
    ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    try (InputStream is = classLoader.getResourceAsStream(JS_FILE)) {
      if (is == null) return null;
      try (InputStreamReader isr = new InputStreamReader(is);
          BufferedReader reader = new BufferedReader(isr)) {
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
      }
    }
  }

  private String escapeQuotes(String str) {
    return str.replace("\"", "\\\"");
  }
}
