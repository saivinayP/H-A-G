package com.hag.core.reporting.events;

import java.util.Objects;

public class ScreenshotCapturedEvent extends Event {

    private final int stepIndex;
    private final String imagePath;
    private final String thumbnailPath;

    public ScreenshotCapturedEvent(String testName, int stepIndex, String imagePath, String thumbnailPath) {
        super(EventType.SCREENSHOT_CAPTURED, testName);
        this.stepIndex = stepIndex;
        this.imagePath = Objects.requireNonNull(imagePath, "image path must not be null");
        this.thumbnailPath = Objects.requireNonNull(thumbnailPath, "thumbnail path must not be null");
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }
}
