package com.hag.ui.adapter;

import com.hag.core.adapter.UiAdapter;
import org.openqa.selenium.WebDriver;

import java.util.Objects;
import java.util.function.Supplier;

public final class SeleniumUiAdapter implements UiAdapter {

    private final Supplier<WebDriver> driverSupplier;

    public SeleniumUiAdapter(Supplier<WebDriver> driverSupplier) {
        this.driverSupplier = Objects.requireNonNull(driverSupplier,
                "WebDriver supplier must not be null");
    }

    public WebDriver driver() {
        return driverSupplier.get();
    }
}