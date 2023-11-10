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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShadowRootSearch {

    private static final String JS_FILE = "shadowRootSearch.js";
    private final WebDriver driver;

    public ShadowRootSearch(WebDriver driver) {
        this.driver = driver;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public WebElement findElement(By selector) throws Exception {
        return findElement(getDocument(), selector);
    }

    /**
     * Recursively searches for an element matching the provided CSS selector, traversing through nested shadow DOMs.
     * This method begins the search from the specified root node and probes into shadow roots to locate the desired element.
     * It effectively pierces through multiple levels of shadow DOM encapsulation to find elements that are not directly accessible via traditional DOM querying methods.
     *
     * @param rootNode The starting {@link WebElement} from which to begin the search, typically the document root or a known shadow-hosting element.
     * @param selector The {@link By} instance encapsulating the CSS selector used to identify the target element.
     * @return The first {@link WebElement} found that matches the selector criteria, or null if no such element is found.
     * @throws Exception Throws a generic Exception if an error occurs during the search process. Specific exceptions, such as {@link NoSuchElementException}, may be thrown to indicate the absence of a matching element.
     */
    public WebElement findElement(WebElement rootNode, By selector) throws Exception {
        WebElement element;
        LocatorMatcher locatorMatcher = new LocatorMatcher(selector);
        How locatorType = locatorMatcher.getLocatorType();
        String locator = locatorMatcher.getLocator();
        String script = switch (locatorType) {
            case CSS -> String.format("return findElement(arguments[0], \"%s\");", escapeQuotes(locator));
            case XPATH -> String.format(
                    "return findElementByXpath(arguments[0], \"%s\");", escapeQuotes(locator));
            default -> String.format(
                    "return findElement(arguments[0], \"%s\");",
                    escapeQuotes(locatorToCss(locatorType, locator)));
        };

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
     * Searches for a web element using a CSS selector, scanning through any nested shadow DOM structures.
     * This method not only finds the element but also provides the "shadowPath" — a string representation of the
     * DOM query needed to directly access the element. This path can be used for direct element access in future operations.
     * <p>
     * For instance, if an element is located inside multiple shadow roots, the method returns a map containing
     * the "elementPath" which is a chain of querySelector calls reaching into the respective shadowRoots, and
     * the "element" itself. This allows for re-querying the element without repeating the entire search process.
     * <p>
     * The output is a map where "elementPath" is a string that describes how to locate the element from the root,
     * using JavaScript query selectors and shadowRoot property accesses, and "element" is the found WebElement.
     * <p>
     * Example of the output structure provided in the map:
     * <pre>{@code
     * [
     *   "elementPath" -> ".querySelector('div[id=\'one\']').shadowRoot.querySelector('span[id=\'two\']').shadowRoot",
     *   "element" -> WebElement
     * ]
     * }</pre>
     * <p>
     * This method improves efficiency by encapsulating the detailed path for element location, which simplifies
     * repeated access to that element in complex DOM structures.
     *
     * @param rootNode The starting {@link WebElement} from which to commence the search.
     * @param selector The {@link By} instance defining the CSS selector for finding the element.
     * @return A {@link Map} with keys "elementPath" and "element", mapping to the path string and the found WebElement respectively.
     * @throws Exception A general exception is thrown if the search or retrieval of the element fails.
     */
    public Map<String, Object> findElementWithShadowPath(WebElement rootNode, By selector)
            throws Exception {
        Map<String, Object> foundElementWithPath;
        WebElement element;
        LocatorMatcher locatorMatcher = new LocatorMatcher(selector);
        How locatorType = locatorMatcher.getLocatorType();
        String locator = locatorMatcher.getLocator();
        String script = switch (locatorType) {
            case CSS -> String.format(
                    "return findElementWithShadowPath(arguments[0], \"%s\");", escapeQuotes(locator));
            case XPATH -> String.format(
                    "return findElementWithShadowPathByXpath(arguments[0], \"%s\");",
                    escapeQuotes(locator));
            default -> String.format(
                    "return findElementWithShadowPath(arguments[0], \"%s\");",
                    escapeQuotes(locatorToCss(locatorType, locator)));
        };

        foundElementWithPath = (Map<String, Object>) executeJsFunction(rootNode, script);
        if (foundElementWithPath == null) {
            throw new NoSuchElementException(
                    "Unable to locate elementWithPath by " + locatorType + ": " + locator);
        }
        element = (WebElement) foundElementWithPath.get("element");
        fixLocator(locator, locatorType, element);

        return Map.of("element", element, "elementPath", foundElementWithPath.get("elementPath"));
    }

    public List<WebElement> findElements(By selector) throws Exception {
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
    public List<WebElement> findElements(WebElement rootNode, By selector) throws Exception {
        ArrayList<WebElement> elements;
        LocatorMatcher locatorMatcher = new LocatorMatcher(selector);
        How locatorType = locatorMatcher.getLocatorType();
        String locator = locatorMatcher.getLocator();
        String script = switch (locatorType) {
            case CSS -> String.format("return findElements(arguments[0], \"%s\");", escapeQuotes(locator));
            case XPATH -> String.format(
                    "return findElementsByXpath(arguments[0], \"%s\");", escapeQuotes(locator));
            default -> String.format(
                    "return findElements(arguments[0], \"%s\");",
                    escapeQuotes(locatorToCss(locatorType, locator)));
        };

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
            throws Exception {
        ArrayList<Map<String, Object>> elementsWithShadowPath;
        LocatorMatcher locatorMatcher = new LocatorMatcher(selector);
        How locatorType = locatorMatcher.getLocatorType();
        String locator = locatorMatcher.getLocator();
        String script = switch (locatorType) {
            case CSS -> String.format(
                    "return findElementsWithShadowPath(arguments[0], \"%s\");", escapeQuotes(locator));
            case XPATH -> String.format(
                    "return findElementsWithShadowPathByXpath(arguments[0], \"%s\");",
                    escapeQuotes(locator));
            default -> String.format(
                    "return findElementsWithShadowPath(arguments[0], \"%s\");",
                    escapeQuotes(locatorToCss(locatorType, locator)));
        };

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
            fixedElements.add(Map.of(
                    "element", webElement,
                    "elementPath", element.get("elementPath")
            ));
        }
        return fixedElements;
    }

    private void fixLocator(String locator, How locatorType, WebElement element) {
        if (element instanceof RemoteWebElement) {
            try {
                Class<?>[] parameterTypes = new Class[]{SearchContext.class, String.class, String.class};
                Method m = element.getClass().getDeclaredMethod("setFoundBy", parameterTypes);

                //TODO: revise the following setting
                m.setAccessible(true);

                Object[] parameters = new Object[]{driver, locatorType.toString(), locator};
                m.invoke(element, parameters);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String locatorToCss(How type, String locator) {
        locator = escapeQuotes(locator);

        return switch (type) {
            case ID -> "#" + locator;
            case CLASS_NAME -> "." + locator;
            case LINK_TEXT -> "[href=\"" + locator + "\"]";
            case PARTIAL_LINK_TEXT -> "[href*=\"" + locator + "\"]";
            case NAME -> "[name=\"" + locator + "\"]";
            case TAG_NAME -> locator;
            default -> throw new IllegalArgumentException("There is no such locator type: " + type);
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
}
