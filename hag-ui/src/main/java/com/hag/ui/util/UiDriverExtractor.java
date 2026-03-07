package com.hag.ui.util;

import com.hag.core.adapter.UiAdapter;
import com.hag.ui.adapter.SeleniumUiAdapter;
import org.openqa.selenium.WebDriver;

public final class UiDriverExtractor {

    private UiDriverExtractor() {}

    public static WebDriver requireDriver(UiAdapter adapter) {

        if (!(adapter instanceof SeleniumUiAdapter seleniumAdapter)) {
            throw new IllegalStateException(
                    "SeleniumUiAdapter not configured in ExecutionContext"
            );
        }

        return seleniumAdapter.driver();
    }
}