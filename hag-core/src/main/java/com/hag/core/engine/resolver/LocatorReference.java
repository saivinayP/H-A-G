package com.hag.core.engine.resolver;

public final class LocatorReference {

    private final String locatorFile;
    private final String elementName;

    private LocatorReference(String locatorFile, String elementName) {
        this.locatorFile = locatorFile;
        this.elementName = elementName;
    }

    public static LocatorReference parse(String recipient) {

        if (recipient == null || !recipient.contains(".")) {
            throw new IllegalArgumentException(
                    "Invalid locator reference: " + recipient +
                            " (expected format: Resources/ui/page.ElementName)"
            );
        }

        int lastDot = recipient.lastIndexOf('.');
        String file = recipient.substring(0, lastDot);
        String element = recipient.substring(lastDot + 1);

        return new LocatorReference(file + ".json", element);
    }

    public String getLocatorFile() {
        return locatorFile;
    }

    public String getElementName() {
        return elementName;
    }
}
