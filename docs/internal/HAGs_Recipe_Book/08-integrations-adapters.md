# 8. Integrations & Adapters

H-A-G abstracts external libraries behind "Adapters" so the Core Engine never directly references third-party execution tools.

## UI: Selenium WebDriver
The `SeleniumUiAdapter` wraps the raw `WebDriver`. 
- **Creation:** `HagTestBase` creates the WebDriver using `WebDriverManager` (Bonigarcia) based on config settings (browser type, headless mode).
- **Execution:** It supports local execution or remote execution via Selenium Grid.
- **Waits:** Actions rarely call `driver.findElement` directly. Instead, they call `UiWaitHelper`, which applies a FluentWait (handling `StaleElementReferenceException` and `NoSuchElementException` automatically) before returning the element.

## API: RestAssured
The `RestAssuredApiAdapter` handles HTTP calls.
- **Stateful:** It maintains a cached `lastResponse` object. This allows you to chain a `SEND_REQUEST` action followed by an `ASSERT_STATUS` action without passing the response object explicitly in the CSV.

## DB: JDBC
The `DbClientRegistry` allows connecting to multiple databases in one test.
- **Creation:** `DbBootstrap` reads `hag.yml` and instantiates a `JdbcSqlClient` for each defined database profile.
- **Execution:** Connections are created lazily. The `JdbcSqlClient` opens the socket only when the first query is fired, not during suite startup.
- **Stateful:** Similar to API, it caches the `lastQueryResult` and `lastAffectedRows`.

## Reporting: Event-Driven Sinks
- **ReportPortalEngine:** Listens for `TestStarted` and `StepFinished`/`StepFailed`. Uses the official ReportPortal REST API to post live logs. Contains logic to strip ANSI color codes from errors and map them to RP's log level format.
- **JsonEventsReporter:** Streams raw JSON objects to an NDJSON file. Excellent for debugging or shipping logs to Datadog/Splunk/ELK stacks.
