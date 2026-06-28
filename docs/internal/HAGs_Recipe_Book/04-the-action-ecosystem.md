# 4. The Action Ecosystem

In H-A-G, an "Action" is a discrete capability. If the engine is the conductor, the Actions are the musicians. 

## Action Base Interface
Every action implements `com.hag.core.executor.Action`.

```java
public interface Action {
    ExecutionResult execute(Step step, ActionDescriptor descriptor, ExecutionContext context);
}
```
- `Step`: The raw CSV row data (Action, Target, Value, Key).
- `ActionDescriptor`: The parsed primary action and sub-case (e.g. `STORE_DATA:DB` becomes `primary=STORE_DATA`, `subCase=DB`).
- `ExecutionContext`: Thread-safe container holding adapters (WebDriver, DB connection) and the `DataStore`.

## Returning ExecutionResults
Actions **must not throw exceptions** for business logic failures (like an element not being found, or an API returning a 404).

Instead, they return `ExecutionResult.failure("reason")`. 

If an action throws an unhandled RuntimeException (like a `NullPointerException`), the `DefaultExecutionEngine` will catch it, halt the entire test immediately, and report a catastrophic failure.

## Sub-Modules & Adapters

### `hag-ui`
Actions here (e.g. `ClickAction`, `InputAction`) rely on the `SeleniumUiAdapter`. The adapter wraps WebDriver interactions to provide built-in explicit waits (via `UiWaitHelper`) and stale element retries.

### `hag-api`
Actions here (e.g. `SendRequestAction`) rely on `RestAssuredApiAdapter`. Instead of passing JSON directly in the CSV, the test passes a template file path. `SendRequestAction` uses the `TemplateMerger` to compile the JSON, fires the request, and caches the response inside the `RestAssuredApiAdapter` instance for subsequent `ASSERT_RESPONSE` actions.

### `hag-db`
Actions here rely on `DbClientRegistry`. Because a test might need to query multiple databases (e.g. an Orders DB and a User Profile DB), the CSV syntax uses `CHANGE_DATA_STORE, ProfileDB` to switch the active client inside the registry before executing a `DB_QUERY` action.

### `hag-core` (Control Flows)
Special actions like `RepeatAction` and `ExecuteEachAction` live in core. They don't interact with external systems. Instead, they instruct the `ExecutionEngine` to load another CSV file and recursively execute it inside a loop.
