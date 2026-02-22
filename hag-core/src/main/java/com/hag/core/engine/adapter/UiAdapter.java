package com.hag.core.engine.adapter;

public interface UiAdapter {
    // Basic Interactions
    void click(String locatorFile, String elementName);
    void doubleClick(String locatorFile, String elementName);
    void rightClick(String locatorFile, String elementName);
    void type(String locatorFile, String elementName, String value);
    void hover(String locatorFile, String elementName);
    boolean isDisplayed(String locatorFile, String elementName);

    // New: Navigation & SwitchTo
    void switchToFrame(String locatorFile, String elementName);
    void switchToDefaultContent();
    void switchToWindow(String titleOrHandle);
    void handleAlert(String action, String text);

    // New: Keyboard & Synchronization
    void pressKey(String keyName);
    void waitFor(String locatorFile, String elementName, String condition, int timeoutSeconds);

    // New: Capture
    byte[] takeScreenshot();
}