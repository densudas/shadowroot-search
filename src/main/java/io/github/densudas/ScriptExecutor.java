package io.github.densudas;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

//TODO: Refactor the project to use it here
public class ScriptExecutor {
    private final JavascriptExecutor jsExecutor;

    public ScriptExecutor(WebDriver driver) {
        if (!(driver instanceof JavascriptExecutor)) {
            throw new IllegalArgumentException("Driver must support JavaScript execution.");
        }
        this.jsExecutor = (JavascriptExecutor) driver;
    }

    public WebElement executeScriptForElement(String script, Object... args) {
        return (WebElement) jsExecutor.executeScript(script, args);
    }

    // ... additional methods for script execution if needed
}
