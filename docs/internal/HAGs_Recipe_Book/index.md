# H-A-G's Recipe Book

**Subtitle:** Zero-to-Hero Internal Developer Guide for the H-A-G Framework  
**Target Audience:** Architects, SDETs, and Framework Developers extending or debugging the H-A-G codebase.

---

> [!NOTE]
> This is **NOT** a user guide. If you are a QA engineer looking for syntax on how to write a CSV test, please refer to the [HAG User Guide](../../HAG_User_Guide.md). This Recipe Book explains **how the engine works under the hood** and **how to modify the source code**.

## Philosophy & Design Goals
H-A-G (Hybrid Automation Grid) was built to achieve zero-code, multi-layer testing (UI, API, and Database) through a highly extensible, modular CSV parser and event-driven engine.

**Core Principles:**
1. **Modularity via Actions:** Every capability (Click, Assert, SendRequest) is a standalone `Action` class that registers itself dynamically.
2. **Thread-Safe by Design:** The engine is stateless. All state is isolated in ThreadLocal `ExecutionContext` containers.
3. **Data Agnostic:** The engine doesn't care if data comes from a JSON file, a database query, or the built-in `DataGenerator`. It is all resolved seamlessly via the `ValueInterpolator`.
4. **Pluggable Reporting:** The core engine emits `StepFinishedEvent` / `StepFailedEvent` via an `EventPublisher`. Adapters (like ReportPortal or JSON Loggers) simply listen.

---

## Table of Contents

1. [The Big Picture](01-the-big-picture.md) - Architecture, module interaction, and core invariants.
2. [Project Anatomy](02-project-anatomy.md) - Annotated directory tree and navigation guide.
3. [Core Execution Engine](03-core-execution-engine.md) - Deep dive into `ExecutionEngine`, `CsvTestParser`, and Event Publishing.
4. [The Action Ecosystem](04-the-action-ecosystem.md) - Complete catalog of Core, UI, API, and DB Actions, execution flow, and step results.
5. [Core Capabilities & Utilities](05-core-capabilities-utilities.md) - `DataGenerator`, `ValueInterpolator`, `LocatorRepository`, and `TemplateMerger`.
6. [Test Lifecycle & Pipelines](06-test-lifecycle-pipelines.md) - Sequence diagrams of the TestNG lifecycle, FrameworkBootstrap, and suite setup/teardown.
7. [Memory & State Management](07-memory-state-management.md) - How `ExecutionContext`, `DataStore`, and ThreadLocal variables pass data between rows and isolate parallel threads safely.
8. [Integrations & Adapters](08-integrations-adapters.md) - WebDriver/Selenium bindings, RestAssured API adapters, JDBC clients, ReportPortal, and NDJSON streaming.
9. [Configuration & Secrets](09-config-secrets.md) - The `hag.yml` schema, `ConfigLoader`, environment variables, and fallback properties.
10. [Development, Testing & Debugging](10-dev-test-debug.md) - Running `mvn clean test`, debugging execution halts, and understanding Maven profiles.
11. [Extension Cookbook](11-extension-cookbook.md) - Step-by-step recipes for extending the framework (Adding Actions, Reporters, DB Clients).
12. [Gotchas, Tech Debt & Pitfalls](12-gotchas-tech-debt.md) - Known limitations, concurrency anti-patterns, and past memory leak patches.
13. [Glossary & Mental Models](13-glossary.md) - H-A-G specific acronyms and mental models.
