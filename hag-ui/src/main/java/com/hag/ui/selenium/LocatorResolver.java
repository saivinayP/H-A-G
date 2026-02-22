package com.hag.ui.selenium;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import java.io.File;
import java.util.Map;

public class LocatorResolver {
    private final ObjectMapper mapper = new ObjectMapper();

    public By resolve(String locatorFile, String elementName) {
        try {
            Map<String, Map<String, String>> locators = mapper.readValue(new File(locatorFile), Map.class);
            Map<String, String> locator = locators.get(elementName);
            if (locator == null) throw new IllegalArgumentException("Element not found: " + elementName);

            return buildBy(locator.get("type"), locator.get("value"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve locator: " + locatorFile, e);
        }
    }

    private By buildBy(String type, String value) {
        if (type == null || value == null) {
            throw new IllegalArgumentException("Invalid locator definition");
        }

        return switch (type.toLowerCase()) {
            case "id" -> By.id(value);
            case "name" -> By.name(value);
            case "xpath" -> By.xpath(value);
            case "css" -> By.cssSelector(value);
            case "classname" -> By.className(value);
            case "tag" -> By.tagName(value);
            case "linktext" -> By.linkText(value);
            case "partiallinktext" -> By.partialLinkText(value);
            default -> throw new IllegalArgumentException("Unsupported locator type: " + type);
        };
    }
}