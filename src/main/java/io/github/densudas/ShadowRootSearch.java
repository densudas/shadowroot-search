package io.github.densudas;

import org.openqa.selenium.By;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ShadowRootSearch {

    private static final String CSS_SELECTOR = "cssSelector";
    private static final String XPATH = "xpath";
    private static final String SELECTOR_REGEX = "^By\\.(\\w+): (.*)$";
    private static final String JS_FILE = "shadowRootSearch.js";
    private final WebDriver driver;

    public ShadowRootSearch(WebDriver driver) {
        this.driver = driver;
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

    public WebElement findElement(By selector) throws IOException {
        return findElement(getDocument(), selector);
    }

    /**
     * Finds an element using a CSS selector, traversing both the main document and any encapsulated shadow DOMs.
     * This method extends the search to penetrate shadow roots, providing a comprehensive query that includes
     * elements within the shadow trees. It is particularly useful when dealing with Web Components where elements
     * are often wrapped in shadow DOMs that standard finders cannot reach.
     *
     * @param rootNode The root element from which to begin the search. This can be the document or any element within it.
     * @param selector The locating mechanism to use when finding the element. It should be a By.cssSelector indicating
     *                 the CSS path to the element. Note that this should not be used with other By types, as this method
     *                 is specialized for CSS selection within shadow roots.
     * @return The first WebElement matching the specified selector within the normal DOM and shadow DOMs.
     * @throws NoSuchElementException If the element cannot be found using the provided selector across the normal and shadow DOMs.
     * @throws IOException            If there are issues in executing the underlying JavaScript or interacting with the elements.
     */
    public WebElement findElement(WebElement rootNode, By selector) throws IOException {
        WebElement element;
        ArrayList<String> selectorMatch = matchSelectorRegex(selector.toString());
        String locatorType = selectorMatch.get(1);
        String locator = selectorMatch.get(2);
        String script = getString(locatorType, "return findElement(arguments[0], \"%s\");", locator, "return findElementByXpath(arguments[0], \"%s\");");

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
     * Searches for an element using a CSS selector and provides a direct path to access the element,
     * considering shadow DOM encapsulation. The method returns a map containing the 'elementPath'—a string
     * that represents the DOM and shadow DOM navigation path to the element—and the 'element' itself as a WebElement.
     * <p>
     * The 'elementPath' can be used for direct access to the element in subsequent queries, bypassing the need
     * for repeated shadow root traversals. This is particularly useful for efficiently accessing deeply nested elements
     * within shadow DOMs.
     * <p>
     * Example usage:
     * WebElement foundElement = shadowRootSearch.findElementWithShadowPath(root, By.cssSelector("span")).get("element");
     * String elementPath = (String) shadowRootSearch.findElementWithShadowPath(root, By.cssSelector("span")).get("elementPath");
     * // The element can now be accessed directly using the elementPath in a JavaScript execution context.
     * <p>
     * Output example:<br>
     * {
     * <br>"elementPath": ".querySelector('div[id=\'one\']').shadowRoot.querySelector('span[id=\'two\']').shadowRoot",
     * <br>"element": WebElement (the found web element)<br>
     * }
     *
     * @param rootNode The WebElement that serves as the starting point for the search. It could be the document or any node within it.
     * @param selector A By object representing the CSS selector used to find the element.
     * @return A Map containing the 'elementPath' and the 'element'. The 'elementPath' is a string that details the steps to reach the
     * element within the shadow DOM, and 'element' is the located WebElement.
     * @throws NoSuchElementException If the element cannot be located with the provided selector across the shadow DOMs.
     * @throws IOException            If there's an error reading the JavaScript file required for execution.
     * @throws WebDriverException     If an error occurs during the execution of the JavaScript to find the element.
     */
    public Map<String, Object> findElementWithShadowPath(WebElement rootNode, By selector) throws IOException {
        Map<String, Object> foundElementWithPath;
        WebElement element;
        ArrayList<String> selectorMatch = matchSelectorRegex(selector.toString());
        String locatorType = selectorMatch.get(1);
        String locator = selectorMatch.get(2);
        String script = getString(locatorType, "return findElementWithShadowPath(arguments[0], \"%s\");", locator, "return findElementWithShadowPathByXpath(arguments[0], \"%s\");");

        foundElementWithPath = (Map<String, Object>) executeJsFunction(rootNode, script);
        if (foundElementWithPath == null) {
            throw new NoSuchElementException(
                    "Unable to locate elementWithPath by " + locatorType + ": " + locator);
        }
        element = (WebElement) foundElementWithPath.get("element");
        fixLocator(locator, locatorType, element);

        return Map.of("element", element, "elementPath", foundElementWithPath.get("elementPath"));
    }

    private String getString(String locatorType, String format, String locator, String format1) {
        String script;

        if (CSS_SELECTOR.equals(locatorType)) {
            script =
                    String.format(
                            format, escapeQuotes(locator));
        } else if (XPATH.equals(locatorType)) {
            script =
                    String.format(
                            format1,
                            escapeQuotes(locator));
        } else {
            script =
                    String.format(
                            format,
                            escapeQuotes(locatorToCss(locatorType, locator)));
        }
        return script;
    }

    public List<WebElement> findElements(By selector) throws Exception {
        return findElements(getDocument(), selector);
    }

    /**
     * Retrieves a collection of elements matched by a CSS selector across all shadow DOMs.
     *
     * @param rootNode The root node to initiate the search from.
     * @param selector The CSS selector used to find elements.
     * @return A List containing the matched elements.
     * @throws Exception If an error occurs during the search process.
     */
    public List<WebElement> findElements(WebElement rootNode, By selector) throws Exception {
        ArrayList<WebElement> elements;
        ArrayList<String> selectorMatch = matchSelectorRegex(selector.toString());
        String locatorType = selectorMatch.get(1);
        String locator = selectorMatch.get(2);
        String script = getString(locatorType, "return findElements(arguments[0], \"%s\");", locator, "return findElementsByXpath(arguments[0], \"%s\");");

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
    public List<Map<String, Object>> findElementsWithShadowPath(WebElement rootNode, By selector)
            throws IOException {
        ArrayList<Map<String, Object>> elementsWithShadowPath;
        ArrayList<String> selectorMatch = matchSelectorRegex(selector.toString());
        String locatorType = selectorMatch.get(1);
        String locator = selectorMatch.get(2);
        String script = getString(locatorType, "return findElementsWithShadowPath(arguments[0], \"%s\");", locator, "return findElementsWithShadowPathByXpath(arguments[0], \"%s\");");

        elementsWithShadowPath = (ArrayList<Map<String, Object>>) executeJsFunction(rootNode, script);
        if (elementsWithShadowPath == null) {
            return new ArrayList<>();
        }

        return getElementsWithFixedLocators(locator, locatorType, elementsWithShadowPath);
    }

    private List<Map<String, Object>> getElementsWithFixedLocators(
            String locator, String locatorType, List<Map<String, Object>> elements) {
        List<Map<String, Object>> fixedElements = new ArrayList<>();
        for (Map<String, Object> element : elements) {
            WebElement webElement = (WebElement) element.get("element");
            fixLocator(locator, locatorType, webElement);
            fixedElements.add(Map.of(
                    "element", webElement,
                    "elementPath", element.get("elementPath")
            ));
        }
        return fixedElements;
    }

    private void fixLocator(String locator, String locatorType, WebElement element) {
        if (element instanceof RemoteWebElement) {
            try {
                Class<?>[] parameterTypes = new Class[]{SearchContext.class, String.class, String.class};
                Method m = element.getClass().getDeclaredMethod("setFoundBy", parameterTypes);
                Object[] parameters = new Object[]{driver, locatorType, locator};
                m.invoke(element, parameters);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private String locatorToCss(String type, String locator) {
        locator = escapeQuotes(locator);

        return switch (type) {
            case "id" -> "#" + locator;
            case "className" -> "." + locator;
            case "linkText" -> "[href=\"" + locator + "\"]";
            case "partialLinkText" -> "[href*=\"" + locator + "\"]";
            case "name" -> "[name=\"" + locator + "\"]";
            case "tagName" -> locator;
            default -> throw new InvalidArgumentException("There is no such locator type: " + type);
        };
    }

    private WebElement getDocument() {
        return (WebElement) executeScript("return document;");
    }

    private Object executeJsFunction(WebElement rootNode, String script) throws IOException {
        script = injectScript(script);
        return executeScript(script, rootNode);
    }

    private Object executeScript(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    private String injectScript(String script) throws IOException {
        return getJSFile() + script;
    }

    private String getJSFile() throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(JS_FILE)) {
            if (is == null) {
                throw new IOException("Resource " + JS_FILE + " was not found.");
            }
            try (InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }

    private String escapeQuotes(String str) {
        return str.replace("\"", "\\\"");
    }

    public WebDriver driver() {
        return driver;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ShadowRootSearch) obj;
        return Objects.equals(this.driver, that.driver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driver);
    }

    @Override
    public String toString() {
        return "ShadowRootSearch[" +
                "driver=" + driver + ']';
    }

}
