# 13. Glossary & Mental Models

Common terminology used within the H-A-G codebase.

| Term | Definition |
| :--- | :--- |
| **Action** | A single Java class implementing `Action.java` representing a discrete execution capability (e.g. `ClickAction`). |
| **Action Descriptor** | The parsed breakdown of an Action string. `STORE_DATA:DB` parses into `primary=STORE_DATA` and `subCase=DB`. |
| **Step** | A single row in a CSV file, containing Action, Target, Value, and Key columns. |
| **DataStore** | A `ConcurrentHashMap` living inside the `ExecutionContext` that holds variables for the duration of a single test thread. |
| **Value Interpolator** | The core engine class that intercepts `${VAR}` strings and replaces them with actual data before passing them to Actions. |
| **Locator Repository** | A static cache in the UI module that reads YAML files to convert plain text keys (`LoginPage.btn`) into Selenium `By` objects. |
| **Preamble Directive** | Metadata lines at the top of a CSV file starting with `#!` (e.g. `#! retry:3`), parsed before execution begins. |
| **Adapter** | A wrapper class (like `SeleniumUiAdapter` or `JdbcSqlClient`) that hides third-party library complexity from the core engine. |
| **Event Publisher** | The decoupled mechanism the Engine uses to broadcast progress (`StepFinished`, `TestStarted`). Reporters listen to this publisher. |

## The "Conductor & Musician" Mental Model
Always remember: The `ExecutionEngine` is the conductor. It reads the sheet music (CSV) but doesn't play any instruments. The `Actions` are the musicians. They know how to play the instruments (WebDriver, JDBC), but they don't know the song. They just wait for the conductor to point at them and hand them their notes (Target, Value).
