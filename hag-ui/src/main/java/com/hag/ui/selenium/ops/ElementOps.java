package com.hag.ui.selenium.ops;

import com.hag.ui.selenium.LocatorResolver;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ElementOps {
    private final WebDriver driver;
    private final LocatorResolver resolver;

    public ElementOps(WebDriver driver, LocatorResolver resolver) {
        this.driver = driver;
        this.resolver = resolver;
    }

    public void click(String file, String element) {
        find(file, element).click();
    }

    public void doubleClick(String file, String element) {
        new Actions(driver).doubleClick(find(file, element)).perform();
    }

    public void rightClick(String file, String element) {
        new Actions(driver).contextClick(find(file, element)).perform();
    }

    public void type(String file, String element, String value) {
        WebElement el = find(file, element);
        el.clear();
        el.sendKeys(value);
    }

    public void hover(String file, String element) {
        new Actions(driver).moveToElement(find(file, element)).perform();
    }

    public boolean isDisplayed(String file, String element) {
        try {
            return find(file, element).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void waitFor(String file, String element, String condition, int timeout) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        By by = resolver.resolve(file, element);
        if ("VISIBLE".equalsIgnoreCase(condition)) {
            wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        } else {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
        }
    }

    private WebElement find(String file, String element) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(resolver.resolve(file, element)));
    }
}