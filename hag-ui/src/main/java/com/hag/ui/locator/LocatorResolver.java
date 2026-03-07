package com.hag.ui.locator;

import org.openqa.selenium.By;

import java.util.Map;
import java.util.Objects;

public final class LocatorResolver {

    private LocatorResolver() {}

    public static By resolve(String locatorExpression) {

        if (locatorExpression == null
                || locatorExpression.isBlank()) {

            throw new IllegalArgumentException(
                    "Locator expression must not be null or blank"
            );
        }

        String trimmed = locatorExpression.trim();

        String[] parts = trimmed.split("\\.");

        if (parts.length != 2
                || parts[0].isBlank()
                || parts[1].isBlank()) {

            throw new IllegalArgumentException(
                    "Invalid locator format. Expected 'Page.Element' but got: "
                            + locatorExpression
            );
        }

        String pageName = parts[0].trim();
        String elementKey = parts[1].trim();

        Map<String, Object> page =
                LocatorRepository.loadPage(pageName);

        if (page == null) {
            throw new IllegalStateException(
                    "Locator page not found: " + pageName
            );
        }

        Object elementObj = page.get(elementKey);

        if (!(elementObj instanceof Map<?, ?> rawElement)) {
            throw new IllegalStateException(
                    "Locator element not found: "
                            + locatorExpression
            );
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> element =
                (Map<String, Object>) rawElement;

        String type = requireField(
                element,
                "type",
                locatorExpression
        );

        String value = requireField(
                element,
                "value",
                locatorExpression
        );

        return buildBy(type, value, locatorExpression);
    }

    private static String requireField(
            Map<String, Object> element,
            String field,
            String locatorExpression
    ) {

        Object value = element.get(field);

        if (value == null || value.toString().isBlank()) {
            throw new IllegalStateException(
                    "Missing '" + field +
                            "' in locator definition: "
                            + locatorExpression
            );
        }

        return value.toString().trim();
    }

    private static By buildBy(
            String type,
            String value,
            String locatorExpression
    ) {

        return switch (type.toLowerCase()) {

            case "id" -> By.id(value);
            case "css" -> By.cssSelector(value);
            case "xpath" -> By.xpath(value);
            case "name" -> By.name(value);
            case "classname" -> By.className(value);
            case "tag" -> By.tagName(value);
            case "linktext" -> By.linkText(value);
            case "partiallinktext" -> By.partialLinkText(value);

            default -> throw new IllegalArgumentException(
                    "Unsupported locator type '" +
                            type +
                            "' for locator: " +
                            locatorExpression
            );
        };
    }
}