package io.github.densudas;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ShadowRootSearchTest {

  private WebDriver driver;

  @Test
  public void testFindElementInside() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String elementCss = ".inside";
    WebElement element = shadowRootSearch.findElement(By.cssSelector(elementCss));

    assert element.getText().contains("Inside Shadow DOM");
  }

  @Test
  public void testFindElementInsideByXpath() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String elementXpath = ".//*[@class='inside']";
    WebElement element = shadowRootSearch.findElement(By.xpath(elementXpath));

    assert element.getText().contains("Inside Shadow DOM");
  }

  @Test
  public void testFindElementOutside() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String elementCss = ".outside";
    WebElement element = shadowRootSearch.findElement(By.cssSelector(elementCss));

    assert element.getText().contains("DOM element");
  }

  @Test
  public void testFindElementOutsideByXpath() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String elementXpath = ".//*[@class='outside']";
    WebElement element = shadowRootSearch.findElement(By.xpath(elementXpath));

    assert element.getText().contains("DOM element");
  }

  @Test
  public void testFindElementsInside() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String elementCss = ".inside";
    List<WebElement> elements = shadowRootSearch.findElements(By.cssSelector(elementCss));

    for (WebElement element : elements) {
      assert element.getText().contains("Inside Shadow DOM");
    }
  }

  @Test
  public void testFindElementsInsideByXpath() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String elementXpath = ".//*[@class='inside']";
    List<WebElement> elements = shadowRootSearch.findElements(By.xpath(elementXpath));

    for (WebElement element : elements) {
      assert element.getText().contains("Inside Shadow DOM");
    }
  }

  @Test
  public void testFindElementsOutside() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String elementCss = ".outside";
    List<WebElement> elements = shadowRootSearch.findElements(By.cssSelector(elementCss));

    for (WebElement element : elements) {
      assert element.getText().contains("DOM element");
    }
  }

  @Test
  public void testFindElementsOutsideByXpath() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String elementXpath = ".//*[@class='outside']";
    List<WebElement> elements = shadowRootSearch.findElements(By.xpath(elementXpath));

    for (WebElement element : elements) {
      assert element.getText().contains("DOM element");
    }
  }

  @Test
  public void testFindElementFromNode() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    waitUntilPageLoaded();
    String elementCss = "#shadow-dom-container";
    WebElement shadowDomContainer = driver.findElement(By.cssSelector(elementCss));

    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    elementCss = ".inside";
    WebElement element =
        shadowRootSearch.findElement(shadowDomContainer, By.cssSelector(elementCss));

    assert element.getText().contains("Inside Shadow DOM");
  }

  @Test
  public void testFindElementsFromNode() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    waitUntilPageLoaded();
    String elementCss = "#shadow-dom-container";
    WebElement shadowDomContainer = driver.findElement(By.cssSelector(elementCss));

    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    elementCss = ".inside";
    List<WebElement> elements =
        shadowRootSearch.findElements(shadowDomContainer, By.cssSelector(elementCss));

    for (WebElement element : elements) {
      assert element.getText().contains("Inside Shadow DOM");
    }
  }

  @Test
  public void testFindElementWithShadowPathInside() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String elementCss = ".inside";
    Map<String, Object> element =
        shadowRootSearch.findElementWithShadowPath(By.cssSelector(elementCss));
    String shadowPathJs = (String) element.get("elementPath");
    WebElement webElement = (WebElement) element.get("element");
    WebElement shadowPath =
        (WebElement) jsExecutor(driver).executeScript("return document" + shadowPathJs);

    assert shadowPath.findElement(By.cssSelector(elementCss)).equals(webElement);
  }

  @Test
  public void testFindElementWithShadowPathInsideByXpath() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String elementXpath = ".//*[@class='inside']";
    Map<String, Object> element =
        shadowRootSearch.findElementWithShadowPath(By.xpath(elementXpath));
    String shadowPathJs = (String) element.get("elementPath");
    WebElement webElement = (WebElement) element.get("element");
    WebElement shadowPath =
        (WebElement) jsExecutor(driver).executeScript("return document" + shadowPathJs);

    assert shadowPath.findElement(By.cssSelector(".inside")).equals(webElement);
  }

  @Test
  public void testFindElementsWithShadowPathInside() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String elementCss = ".inside";
    List<Map<String, Object>> elements =
        shadowRootSearch.findElementsWithShadowPath(By.cssSelector(elementCss));

    for (Map<String, Object> element : elements) {
      String shadowPathJs = (String) element.get("elementPath");
      WebElement webElement = (WebElement) element.get("element");
      WebElement shadowPath =
          (WebElement) jsExecutor(driver).executeScript("return document" + shadowPathJs);

      assert shadowPath.findElement(By.cssSelector(elementCss)).equals(webElement);
    }
  }

  @Test
  public void testFindElementsWithShadowPathInsideByXpath() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String elementXpath = ".//*[@class='inside']";
    List<Map<String, Object>> elements =
        shadowRootSearch.findElementsWithShadowPath(By.xpath(elementXpath));

    for (Map<String, Object> element : elements) {
      String shadowPathJs = (String) element.get("elementPath");
      WebElement webElement = (WebElement) element.get("element");
      WebElement shadowPath =
          (WebElement) jsExecutor(driver).executeScript("return document" + shadowPathJs);

      assert shadowPath.findElement(By.cssSelector(".inside")).equals(webElement);
    }
  }

  @Test
  public void testFindElementWithShadowPathOutside() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String elementCss = ".outside";
    Map<String, Object> element =
        shadowRootSearch.findElementWithShadowPath(By.cssSelector(elementCss));

    assert element.get("elementPath").equals("");
  }

  @Test
  public void testFindElementWithShadowPathOutsideByXpath() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String elementXpath = ".//*[@class='outside']";
    Map<String, Object> element =
        shadowRootSearch.findElementWithShadowPath(By.xpath(elementXpath));

    assert element.get("elementPath").equals("");
  }

  @Test
  public void testFindElementsWithShadowPathOutside() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String elementCss = ".outside";
    List<Map<String, Object>> elements =
        shadowRootSearch.findElementsWithShadowPath(By.cssSelector(elementCss));

    for (Map<String, Object> element : elements) {
      assert element.get("elementPath").equals("");
    }
  }

  @Test
  public void testFindElementsWithShadowPathOutsideByXpath() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String elementXpath = ".//*[@class='outside']";
    List<Map<String, Object>> elements =
        shadowRootSearch.findElementsWithShadowPath(By.xpath(elementXpath));

    for (Map<String, Object> element : elements) {
      assert element.get("elementPath").equals("");
    }
  }

  @Test
  public void testFindElementWithShadowPathFromNode() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    waitUntilPageLoaded();
    String elementCss = "#shadow-dom-container";
    WebElement shadowDomContainer = driver.findElement(By.cssSelector(elementCss));

    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    elementCss = ".inside";
    Map<String, Object> element =
        shadowRootSearch.findElementWithShadowPath(shadowDomContainer, By.cssSelector(elementCss));
    String shadowPathJs = (String) element.get("elementPath");
    WebElement webElement = (WebElement) element.get("element");
    WebElement shadowPath =
        (WebElement)
            jsExecutor(driver)
                .executeScript("return arguments[0]" + shadowPathJs, shadowDomContainer);

    assert shadowPath.findElement(By.cssSelector(elementCss)).equals(webElement);
  }

  @Test
  public void testFindElementsWithShadowPathFromNode() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    waitUntilPageLoaded();
    String elementCss = "#shadow-dom-container";
    WebElement shadowDomContainer = driver.findElement(By.cssSelector(elementCss));

    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    elementCss = ".inside";
    List<Map<String, Object>> elements =
        shadowRootSearch.findElementsWithShadowPath(shadowDomContainer, By.cssSelector(elementCss));

    for (Map<String, Object> element : elements) {
      String shadowPathJs = (String) element.get("elementPath");
      WebElement webElement = (WebElement) element.get("element");
      WebElement shadowPath =
          (WebElement)
              jsExecutor(driver)
                  .executeScript("return arguments[0]" + shadowPathJs, shadowDomContainer);

      assert shadowPath.findElement(By.cssSelector(elementCss)).equals(webElement);
    }
  }

  @Test
  public void testFindElementById() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String elementClass = "outside-1";
    WebElement element = shadowRootSearch.findElement(By.id(elementClass));

    assert element.getText().equals("DOM element #1");
  }

  @Test
  public void testFindElementByClassName() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String elementClass = "outside";
    WebElement element = shadowRootSearch.findElement(By.className(elementClass));

    assert element.getText().equals("DOM element #1");
  }

  @Test
  public void testFindElementByLinkText() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String link = "index.html";
    WebElement element = shadowRootSearch.findElement(By.linkText(link));

    assert element.getText().equals("Link");
  }

  @Test
  public void testFindElementByPartialLinkText() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String link = "index";
    WebElement element = shadowRootSearch.findElement(By.partialLinkText(link));

    assert element.getText().equals("Link");
  }

  @Test
  public void testFindElementByName() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String name = "outside-2";
    WebElement element = shadowRootSearch.findElement(By.name(name));

    assert element.getText().equals("DOM element #2");
  }

  @Test
  public void testFindElementByTagName() throws Exception {
    driver = getChromeDriver();
    driver.get(getPageContent());
    ShadowRootSearch shadowRootSearch = new ShadowRootSearch(driver);
    waitUntilPageLoaded();
    String tag = "a";
    WebElement element = shadowRootSearch.findElement(By.tagName(tag));

    assert element.getText().equals("Link");
  }

  @BeforeAll
  public static void setUp() {
    WebDriverManager.chromedriver().setup();
  }

  @AfterEach
  public void shutdown() {
    driverQuit();
  }

  private void waitUntilPageLoaded() throws InterruptedException {
    new WebDriverWait(driver, 10)
        .until(
            webDriver ->
                jsExecutor(webDriver)
                    .executeScript("return document.readyState")
                    .equals("complete"));
    Thread.sleep(1000);
  }

  private JavascriptExecutor jsExecutor(WebDriver webDriver) {
    return (JavascriptExecutor) webDriver;
  }

  private void driverQuit() {
    if (driver != null) {
      driver.quit();
    }
  }

  private static RemoteWebDriver getChromeDriver() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless");
    return new ChromeDriver(options);
  }

  private static String getPageContent() throws Exception {
    String fileName = "index.html";
    URL url = ShadowRootSearchTest.class.getClassLoader().getResource(fileName);
    if (url == null) {
      throw new Exception("No such file: " + fileName);
    }
    return "file://" + Objects.requireNonNull(url).getPath();
  }
}
