# 2. Project Anatomy & Navigation Guide

H-A-G is organized into highly decoupled Maven modules. This separation prevents dependency bloat (e.g., API test engineers shouldn't need Selenium WebDriver on their classpath).

## Directory Tree

```text
H-A-G/
├── pom.xml                   # Parent POM (Dependency management)
├── hag.yml                   # The Master Configuration File
├── docs/                     # User Guides and Internal Recipe Books
│
├── hag-core/                 # The Orchestrator
│   └── src/main/java/com/hag/core/
│       ├── data/             # DataGenerator
│       ├── engine/           # DefaultExecutionEngine
│       ├── executor/         # Base Action interfaces, Repeat/Each flows
│       └── reporting/        # Event interfaces, Repoters (JSON, RP)
│
├── hag-ui/                   # The Browser Module
│   └── src/main/java/com/hag/ui/
│       ├── action/           # All UI implementations (Click, Input)
│       └── locator/          # LocatorRepository (YAML parser)
│
├── hag-api/                  # The API Module
│   └── src/main/java/com/hag/api/
│       ├── action/           # SendRequestAction, etc.
│       └── template/         # TemplateMerger (JSON substitution)
│
├── hag-db/                   # The Database Module
│   └── src/main/java/com/hag/db/
│       ├── action/           # DbQueryAction, StoreDataDbAction
│       └── adapter/          # JdbcSqlClient
│
├── hag-runner/               # The Execution Module
│   └── src/main/java/com/hag/runner/
│       ├── HagTestBase.java  # ThreadLocal management, WebDriver creation
│       └── bootstrap/        # FrameworkBootstrap (registers Actions)
│
└── hag-resource/             # The Test Workspace
    ├── testdata/             # JSON files for ValueInterpolator
    ├── locators/             # Page object YAMLs
    ├── templates/            # API request body templates
    └── tests/                # CSV test scripts
```

## Quick Reference Navigation Table

| If you want to work on... | Start Here |
| :--- | :--- |
| **Parsing logic for CSVs** | `hag-core/.../parser/CsvTestParser.java` |
| **Variable resolution `${var}`** | `hag-core/.../resolver/ValueInterpolator.java` |
| **New dynamic data (e.g. `${RANDOM_UUID}`)** | `hag-core/.../data/DataGenerator.java` |
| **Adding a completely new UI action** | `hag-ui/.../action/MyNewAction.java` |
| **Fixing thread/webdriver leaks** | `hag-runner/.../HagTestBase.java` |
| **Registering a new module's Actions** | `hag-runner/.../bootstrap/FrameworkBootstrap.java` |
| **Config/hag.yml parsing** | `hag-runner/.../config/ConfigLoader.java` |
| **ReportPortal API logic** | `hag-core/.../reporting/engine/ReportPortalEngine.java` |

> [!TIP]
> If you create a new `Action` class in any module, it **will not work** until you register it in `FrameworkBootstrap.java`. The engine has no reflection-based auto-discovery mechanism by design (for speed and security).
