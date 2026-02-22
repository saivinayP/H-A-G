package com.hag.ui.selenium.ops;

import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;

public class AlertOps {
    private final WebDriver driver;

    public AlertOps(WebDriver driver) {
        this.driver = driver;
    }

    public void handle(String action, String text) {
        Alert alert = driver.switchTo().alert();
        if (text != null && !text.isBlank()) {
            alert.sendKeys(text);
        }
        if ("ACCEPT".equalsIgnoreCase(action)) {
            alert.accept();
        } else {
            alert.dismiss();
        }
    }
}