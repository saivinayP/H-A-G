package com.hag.ui.util;

import com.hag.core.context.ExecutionContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Helper class for centralized smart explicit waits.
 */
public final class UiWaitHelper {

    private UiWaitHelper() {
        // Prevent instantiation
    }

    private static WebDriverWait getWait(WebDriver driver, ExecutionContext context) {
        int timeout = context.getConfig() != null ? context.getConfig().getDefaultWaitTimeoutSeconds() : 10;
        return new WebDriverWait(driver, Duration.ofSeconds(timeout));
    }

    /**
     * Waits for an element to be present, visible, and enabled (clickable).
     */
    public static WebElement awaitClickable(WebDriver driver, ExecutionContext context, By by) {
        return getWait(driver, context).until(ExpectedConditions.elementToBeClickable(by));
    }

    /**
     * Waits for an element to be present in the DOM and visible on the page.
     */
    public static WebElement awaitVisible(WebDriver driver, ExecutionContext context, By by) {
        return getWait(driver, context).until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    /**
     * Waits for an element to be present in the DOM (does not have to be visible).
     */
    public static WebElement awaitPresence(WebDriver driver, ExecutionContext context, By by) {
        return getWait(driver, context).until(ExpectedConditions.presenceOfElementLocated(by));
    }
}
