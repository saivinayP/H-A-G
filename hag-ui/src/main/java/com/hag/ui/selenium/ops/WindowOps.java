package com.hag.ui.selenium.ops;

import com.hag.ui.selenium.LocatorResolver;
import org.openqa.selenium.WebDriver;

public class WindowOps {
    private final WebDriver driver;
    private final LocatorResolver resolver;

    public WindowOps(WebDriver driver, LocatorResolver resolver) {
        this.driver = driver;
        this.resolver = resolver;
    }

    public void switchToFrame(String file, String element) {
        driver.switchTo().frame(driver.findElement(resolver.resolve(file, element)));
    }

    public void switchToDefault() {
        driver.switchTo().defaultContent();
    }

    public void switchToWindow(String title) {
        for (String handle : driver.getWindowHandles()) {
            driver.switchTo().window(handle);
            if (driver.getTitle().contains(title)) return;
        }
    }
}