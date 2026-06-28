# 5. Core Capabilities & Utilities

These are the internal tools and services that Actions use to do their jobs.

## ValueInterpolator & DataStore
Before an Action executes, the Engine intercepts the raw CSV values and passes them through the `ValueInterpolator`.

1. **`DataStore` Resolution**: If it sees `${user_id}`, it pulls it from the thread's `DataStore`.
2. **JSON Test Data**: If it sees `${users.admin.password}`, it calls `DefaultTestDataResolver`, which loads `testdata/users/admin.json` from the resources folder and extracts the `password` field.
3. **Dynamic Generation**: If it sees `${RANDOM_EMAIL}`, it delegates to `DataGenerator`.

## DataGenerator
A static utility class that generates fake data. It is integrated directly into the `ValueInterpolator`, meaning dynamic data can be injected into **any column, for any action** automatically. 

> [!TIP]
> **Extension Point:** To add a new generator token (like `${RANDOM_SSN}`), simply add it to the switch statement inside `DataGenerator.generate()`. No other framework changes are needed.

## LocatorRepository
Used exclusively by `hag-ui`. Instead of putting messy XPath strings in CSVs, the `Target` column contains keys like `LoginPage.submitButton`. 

The `LocatorRepository` intercepts this, parses `locators/LoginPage.yml`, and returns a standard Selenium `By` object (e.g. `By.id("submit")`). It aggressively caches these YAML reads in a static `ConcurrentHashMap` to keep UI execution lightning fast.

## TemplateMerger
Used exclusively by `hag-api`. It reads a JSON file from `templates/`, searches the raw text for `${VAR}` tokens, replaces them using the current thread's `DataStore`, and then parses the resulting string into a Jackson `JsonNode`.

> [!WARNING]
> Because `TemplateMerger` does string-replacement BEFORE parsing JSON, it is safe to use tokens inside JSON values: `"email": "${random_email}"` or JSON keys: `"${dynamic_key}": "value"`. But if the token resolves to a string with unescaped quotes, it will corrupt the JSON and cause a parse failure.
