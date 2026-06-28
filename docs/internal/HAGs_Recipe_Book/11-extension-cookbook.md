# 11. Extension Cookbook

The most common task for a framework developer is adding new capabilities. Here is the step-by-step recipe.

## Recipe 1: Adding a New Action

**Goal:** Create a `LOG_WARNING` action that prints a warning to the console.

**Step 1: Create the Class**
Create the class in `hag-core` (or `hag-ui` if it requires a browser). Implement the `Action` interface.

```java
package com.hag.core.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogWarningAction implements Action {
    private static final Logger LOG = LoggerFactory.getLogger(LogWarningAction.class);

    @Override
    public ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context) {
        String message = step.getTarget(); // Reading from the Target column
        
        if (message == null || message.isBlank()) {
            return ExecutionResult.failure("LOG_WARNING requires a message in the Target column.");
        }
        
        LOG.warn("HAG WARNING: " + message);
        return ExecutionResult.success();
    }
}
```

**Step 2: Register the Action**
Open `hag-runner/src/main/java/com/hag/runner/bootstrap/FrameworkBootstrap.java`.
Add it to the `registerCoreActions()` method.

```java
registry.register("LOG_WARNING", new LogWarningAction());
```

**Step 3: Test It**
Create a CSV file with:
```csv
LOG_WARNING, This is a warning test,,
```
Run `mvn clean test` and check the console.

---

## Recipe 2: Adding a New Dynamic Token

**Goal:** Add `${RANDOM_SSN}` support.

**Step 1: Update DataGenerator**
Open `hag-core/src/main/java/com/hag/core/data/DataGenerator.java`.
Find the `generate(String token)` method and add to the `switch` statement:

```java
case "RANDOM_SSN":
    return String.format("%03d-%02d-%04d", 
        random.nextInt(900) + 100, 
        random.nextInt(90) + 10, 
        random.nextInt(9000) + 1000);
```

**Step 2: Done.**
Because `ValueInterpolator` automatically delegates to `DataGenerator`, this token is instantly available to every action in the framework.
