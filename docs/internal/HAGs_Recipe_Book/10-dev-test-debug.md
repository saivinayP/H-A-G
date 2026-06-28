# 10. Development, Testing & Debugging

When extending the framework, here is how you test and debug your changes safely.

## Running Tests Locally
To verify your changes haven't broken the engine, always run the demo suite:

```bash
mvn clean test
```
This executes all CSV scripts inside `hag-resource/tests/` (which includes UI, API, and DB scenarios).

## Debugging

### 1. Engine Halts (Exceptions vs ExecutionResult)
If the console shows `TEST FAILED` gracefully with an error like "Element not found", this means your Action returned an `ExecutionResult.failure()`. This is expected behavior.

If the engine completely crashes with a stack trace (e.g., `NullPointerException`), this means your Action code threw an unhandled runtime exception. **You must wrap dangerous code in try/catch blocks and return `ExecutionResult.failure()` instead.**

### 2. Using NDJSON for Tracing
If you can't figure out why a variable isn't resolving, check the NDJSON logs generated in your `target/` directory (if `reporting.json` is enabled). The JSON payload contains the `rawStep` and the `resolvedStep`. 

You can compare `rawStep.Value` (e.g. `${RANDOM_EMAIL}`) against `resolvedStep.Value` (e.g. `test@example.com`) to see exactly what the `ValueInterpolator` did right before your Action was called.

### 3. Debugging Maven Classpath Issues
Because H-A-G is multi-module, ensure you run `mvn clean install -DskipTests` from the root directory if you add a new dependency to `hag-core` that `hag-ui` relies on. Otherwise, your IDE might see the class, but the command-line runner will throw a `ClassNotFoundException`.
