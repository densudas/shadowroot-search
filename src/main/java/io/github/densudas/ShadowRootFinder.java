package io.github.densudas;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

//TODO: Refactor the project to use it here
public class ShadowRootFinder {
    public static WebElement findElement(WebDriver driver, By shadowHostSelector, By shadowItemSelector) {
        WebElement shadowHost = driver.findElement(shadowHostSelector);
        return executeScript(driver, "return arguments[0].shadowRoot.querySelector(arguments[1])",
                shadowHost, shadowItemSelector);
    }

    private static WebElement executeScript(WebDriver driver, String script, Object... args) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        return (WebElement) js.executeScript(script, args);
    }
}