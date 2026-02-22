package com.hag.ui.selenium.ops;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

public class KeyboardOps {
    private final WebDriver driver;

    public KeyboardOps(WebDriver driver) {
        this.driver = driver;
    }

    public void pressKey(String keyName) {
        Keys key = Keys.valueOf(keyName.toUpperCase());
        new Actions(driver).sendKeys(key).perform();
    }
}