package com.hag.core.engine.adapter;

public interface UiAdapter {

    void click(String locator, String elementName);
    void type(String locator, String elementName, String value);
    void hover(String locator, String elementName);
    boolean isDisplayed(String locatorFile, String elementName);
}
