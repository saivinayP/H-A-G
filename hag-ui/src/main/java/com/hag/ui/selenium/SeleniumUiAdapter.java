package com.hag.ui.selenium;

import com.hag.core.engine.adapter.UiAdapter;
import com.hag.ui.selenium.ops.*;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.util.Objects;

public class SeleniumUiAdapter implements UiAdapter {

    private final WebDriver driver;
    private final ElementOps elementOps;
    private final WindowOps windowOps;
    private final KeyboardOps keyboardOps;
    private final AlertOps alertOps;

    public SeleniumUiAdapter(WebDriver driver) {
        this.driver = Objects.requireNonNull(driver);
        LocatorResolver resolver = new LocatorResolver();

        this.elementOps = new ElementOps(driver, resolver);
        this.windowOps = new WindowOps(driver, resolver);
        this.keyboardOps = new KeyboardOps(driver);
        this.alertOps = new AlertOps(driver);
    }

    @Override public void click(String f, String e) { elementOps.click(f, e); }
    @Override public void doubleClick(String f, String e) { elementOps.doubleClick(f, e); }
    @Override public void rightClick(String f, String e) { elementOps.rightClick(f, e); }
    @Override public void type(String f, String e, String v) { elementOps.type(f, e, v); }
    @Override public void hover(String f, String e) { elementOps.hover(f, e); }
    @Override public boolean isDisplayed(String f, String e) { return elementOps.isDisplayed(f, e); }

    @Override public void switchToFrame(String f, String e) { windowOps.switchToFrame(f, e); }
    @Override public void switchToDefaultContent() { windowOps.switchToDefault(); }
    @Override public void switchToWindow(String t) { windowOps.switchToWindow(t); }
    @Override public void handleAlert(String a, String t) { alertOps.handle(a, t); }

    @Override public void pressKey(String k) { keyboardOps.pressKey(k); }
    @Override public void waitFor(String f, String e, String c, int t) { elementOps.waitFor(f, e, c, t); }

    @Override
    public byte[] takeScreenshot() {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }
}