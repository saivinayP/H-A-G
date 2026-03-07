package com.hag.ui.adapter;

import com.hag.core.adapter.UiAdapter;
import org.openqa.selenium.WebDriver;

import java.util.Objects;

public final class SeleniumUiAdapter implements UiAdapter {

    private final WebDriver driver;

    public SeleniumUiAdapter(WebDriver driver) {
        this.driver = Objects.requireNonNull(driver,
                "WebDriver must not be null");
    }

    public WebDriver driver() {
        return driver;
    }
}