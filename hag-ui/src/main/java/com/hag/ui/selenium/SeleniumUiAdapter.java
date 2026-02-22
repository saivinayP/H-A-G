package com.hag.ui.selenium;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hag.core.engine.adapter.UiAdapter;
import com.hag.ui.selenium.ops.WindowOps;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.File;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public class SeleniumUiAdapter implements UiAdapter {

    private final WebDriver driver;
    private final ObjectMapper mapper = new ObjectMapper();
    private final WebDriverWait wait;

    public SeleniumUiAdapter(WebDriver driver) {
        this.driver = Objects.requireNonNull(driver);
        this.windowOps = new WindowOps(driver, resolver);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /* =============================
       Public API
       ============================= */

    @Override
    public void click(String locatorFile, String elementName) {
        find(locatorFile, elementName).click();
    }

    @Override
    public void doubleClick(String locatorFile, String elementName) {
        WebElement element = find(locatorFile, elementName);
        new Actions(driver).doubleClick(element).perform();
    }

    @Override
    public void rightClick(String locatorFile, String elementName) {
        WebElement element = find(locatorFile, elementName);
        new Actions(driver).contextClick(element).perform();
    }

    @Override
    public void type(String locatorFile, String elementName, String value) {
        WebElement element = find(locatorFile, elementName);
        element.clear();
        element.sendKeys(value);
    }

    @Override
    public void hover(String locatorFile, String elementName) {
        WebElement element = find(locatorFile, elementName);
        new Actions(driver).moveToElement(element).perform();
    }

    @Override
    public boolean isDisplayed(String locatorFile, String elementName) {
        try {
            return find(locatorFile, elementName).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /* =============================
       Internal Locator Resolution
       ============================= */

    private WebElement find(String locatorFile, String elementName) {

        try {
            Map<String, Map<String, String>> locators =
                    mapper.readValue(
                            new File(locatorFile),
                            Map.class
                    );

            Map<String, String> locator =
                    locators.get(elementName);

            if (locator == null) {
                throw new IllegalArgumentException(
                        "Element not found in locator file: " + elementName
                );
            }

            By by = buildBy(locator.get("type"), locator.get("value"));

            return wait.until(ExpectedConditions.visibilityOfElementLocated(by));

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to resolve locator: " +
                            locatorFile + "." + elementName,
                    e
            );
        }
    }

    private By buildBy(String type, String value) {

        if (type == null || value == null) {
            throw new IllegalArgumentException(
                    "Invalid locator definition"
            );
        }

        return switch (type.toLowerCase()) {
            case "id" -> By.id(value);
            case "name" -> By.name(value);
            case "xpath" -> By.xpath(value);
            case "css" -> By.cssSelector(value);
            case "classname" -> By.className(value);
            case "tag" -> By.tagName(value);
            case "linktext" -> By.linkText(value);
            case "partiallinktext" -> By.partialLinkText(value);
            default -> throw new IllegalArgumentException(
                    "Unsupported locator type: " + type
            );
        };
    }
}