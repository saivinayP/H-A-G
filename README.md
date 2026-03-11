# H-A-G — Hybrid Automation Grid

> **One CSV. Any layer. Zero code.**

H-A-G is a unified, data-driven test automation platform. Write UI, API, and Database tests as plain **CSV files** and H-A-G orchestrates Selenium, RestAssured, and JDBC automatically — no programming required for testers.

```csv
Action,Recipient,Source,Key

# 1. Login via API — capture the auth token
SEND_REQUEST,templates/auth/login.json,testdata/users.json,adminUser
ASSERT_STATUS,,,200
STORE_DATA:RESPONSE,data.token,,authToken

# 2. Verify the session in the browser
NAVIGATE,,,${URL:application}/dashboard
ASSERT_VISIBLE,DashboardPage.welcomeBanner,,
ASSERT_TEXT:CONTAINS,DashboardPage.userName,,Admin

# 3. Confirm the session was written to the database
DB_QUERY:INLINE,,,SELECT * FROM sessions WHERE token = '${API:authToken}'
ASSERT_ROW_COUNT,,,1
ASSERT_COLUMN,status,,ACTIVE

FINALLY
NAVIGATE,,,${URL:application}/logout
```

---

## What's Built

| Layer | Status | Actions |
|---|---|---|
| **Core** | ✅ Ready | `CHANGE_DATA_STORE`, `COMPARE` (11 operators), `LOG`, `INCLUDE`, `FINALLY` |
| **UI** | ✅ Ready | 23 Selenium actions — click, input, select, wait, assert, frames, windows, drag-drop, JS |
| **API** | ✅ Ready | `SEND_REQUEST`, `ASSERT_STATUS`, `ASSERT_RESPONSE`, `STORE_DATA:RESPONSE` |
| **DB** | ✅ Ready | `DB_QUERY`, `DB_EXECUTE`, `ASSERT_ROW_COUNT`, `ASSERT_COLUMN`, `STORE_DATA:DB` |

---

## Prerequisites

| Tool | Version |
|---|---|
| Java | 17+ |
| Maven | 3.8+ |
| Chrome / Firefox / Edge | Latest |

---

## Quick Start

### 1. Clone and build

```bash
git clone <repo-url>
cd H-A-G
mvn clean compile
```

### 2. Configure (three files in the project root)

**`url.config.yml`** — environment URLs
```yaml
active-environment: dev
environments:
  dev:
    application: https://dev.yourapp.com
    api-base:    https://api-dev.yourapp.com
  staging:
    application: https://staging.yourapp.com
    api-base:    https://api-staging.yourapp.com
```

**`runner.config.yml`** — browser and execution settings
```yaml
browser:
  type: chrome
  headless: false
execution:
  timeout-seconds: 30
  retry-attempts: 1
screenshots:
  directory: target/screenshots
```

**`testdata.config.yml`** — file paths and optional database
```yaml
paths:
  locators:  src/main/resources/locators
  test-data: src/main/resources/testdata
  templates: src/main/resources/templates
  scripts:   src/main/resources/scripts
database:
  url:      ""   # jdbc:mysql://localhost:3306/testdb
  username: ""
  password: ""   # supports ${env.DB_PASSWORD}
```

### 3. Create a locator file

`src/main/resources/locators/LoginPage.json`
```json
{
  "usernameField": { "type": "id",   "value": "username" },
  "passwordField": { "type": "css",  "value": "input[type='password']" },
  "loginButton":   { "type": "id",   "value": "loginBtn" },
  "errorMessage":  { "type": "xpath","value": "//div[@class='error-msg']" }
}
```

### 4. Create a test data file

`src/main/resources/testdata/users.json`
```json
{
  "validUser":   { "username": "john@example.com", "password": "Secret@123" },
  "lockedUser":  { "username": "locked@example.com", "password": "Secret@123" },
  "adminUser":   { "username": "admin@example.com",  "password": "Admin@999"  }
}
```

### 5. Create an API request template

`src/main/resources/templates/auth/login.json`
```json
{
  "_method":   "POST",
  "_endpoint": "/api/v1/auth/login",
  "_headers":  { "Content-Type": "application/json" },
  "username":  "${username}",
  "password":  "${password}"
}
```

### 6. Write your first test

`tests/login/valid_login.csv`
```csv
Action,Recipient,Source,Key
NAVIGATE,,,${URL:application}/login
INPUT,LoginPage.usernameField,testdata/users.json,validUser
INPUT,LoginPage.passwordField,testdata/users.json,validUser
CLICK,LoginPage.loginButton,,
ASSERT_VISIBLE,HomePage.welcomeBanner,,
ASSERT_TEXT:CONTAINS,HomePage.welcomeBanner,,Welcome

FINALLY
NAVIGATE,,,${URL:application}/logout
```

### 7. Run

```bash
# Default (Chrome, non-headless)
mvn clean test -pl hag-runner -am

# Firefox, headless (CI)
mvn clean test -pl hag-runner -am -Dbrowser=firefox -Dheadless=true

# Parallel — 4 threads
mvn clean test -pl hag-runner -am -Dthread.count=4

# Different environment
mvn clean test -pl hag-runner -am -Denv=staging
```

---

## Framework Modules

H-A-G is designed with a modular architecture so components are loosely coupled. Here is the relevance of each module:

- **`hag-core`**: The brain of the framework. Contains the CSV parser, action dispatcher, shared `DataStore` for variables, configuration loaders, and the event-driven reporting engine (including custom HTML reports).
- **`hag-ui`**: The browser automation module. Acts as a wrapper around Selenium WebDriver, providing 23 built-in UI actions (click, input, select, wait, drag-drop, assert).
- **`hag-api`**: The REST automation module. Powered by RestAssured, it parses JSON templates to construct and execute API requests and assert responses.
- **`hag-db`**: The database automation module. Uses pure JDBC to connect to databases (MySQL, H2, etc.), execute SQL scripts, query data, and assert row counts or column values.
- **`hag-runner`**: The execution orchestrator. Boots up the TestNG suite, initializes configurations (`url.config.yml`, `runner.config.yml`), and runs the `BulkTestRunner` to dynamically execute all CSV test scenarios without any custom Java classes required.

```
H-A-G/
├── hag-core/               Core framework — parser, dispatcher, DataStore, config, reporting
├── hag-ui/                 Selenium UI adapter
├── hag-api/                REST adapter — RestAssured 5.4
├── hag-db/                 JDBC DB adapter
├── hag-runner/             TestNG runner, FrameworkBootstrap, BulkTestRunner
│
├── url.config.yml          ← Environment URLs (dev / staging / prod)
├── runner.config.yml       ← Browser, timeout, retry, screenshot dir
├── testdata.config.yml     ← File path roots, DB connection
│
├── tests/                  ← Your CSV test files go here
└── src/main/resources/
    ├── locators/           ← Element locator JSON files (one per page)
    ├── testdata/           ← Test data JSON files (named blocks)
    ├── templates/          ← API request template JSON files
    └── scripts/            ← SQL query files (.sql)
```

---

## Action Syntax

```
Action           → default behaviour
Action:SUBCASE   → specific variant (e.g. CLICK:DOUBLE, ASSERT_TEXT:CONTAINS)
```

Source column carries **modifiers** (pipe-separated, never commas):

```csv
WAIT:TEXT,StatusPage.label,timeout=60,Ready
ASSERT_TEXT:CONTAINS,ErrorPage.msg,ignore-case,required field
STORE_DATA,ProfilePage.name,trim|upper,savedName
```

---

## Full Action Reference

### UI Actions

| Action | Sub-cases | Description |
|---|---|---|
| `NAVIGATE` | `:BACK` `:FORWARD` `:REFRESH` | Browser navigation |
| `CLICK` | `:DOUBLE` `:RIGHT` `:JS` `:HOLD` | Mouse interactions |
| `INPUT` | `:KEY` `:FILE` | Type text / keyboard / file upload |
| `SELECT` | `:TEXT` `:INDEX` `:VALUE` | Dropdown selection |
| `HOVER` | | Move mouse to element |
| `SCROLL` | `:TOP` `:BOTTOM` `:INTO_VIEW` | Page/element scroll |
| `CLEAR` | | Clear input field |
| `DRAG_DROP` | | Drag source → target |
| `JS_CLICK` | | Click via JavaScript |
| `JS_EXECUTE` | | Run arbitrary JS |
| `GET_TEXT` | | Read element text → Key variable |
| `WAIT` | `:VISIBLE` `:INVISIBLE` `:TEXT` `:CLICKABLE` | Explicit waits |
| `ASSERT_TEXT` | `:CONTAINS` `:STARTS_WITH` `:ENDS_WITH` | Text assertions |
| `ASSERT_VISIBLE` | | Element is displayed |
| `ASSERT_HIDDEN` | | Element is not displayed |
| `ASSERT_ENABLED` / `ASSERT_DISABLED` | | Element state |
| `ASSERT_SELECTED` | | Checkbox/radio state |
| `ASSERT_ATTRIBUTE` | | DOM attribute match |
| `ASSERT_COUNT` | | Element count on page |
| `SWITCH_FRAME` | `:DEFAULT` | Switch iframe context |
| `SWITCH_WINDOW` | | Switch browser window/tab |

### API Actions

| Action | Sub-cases | Description |
|---|---|---|
| `SEND_REQUEST` | | Execute REST call from JSON template |
| `ASSERT_STATUS` | `:NOT` `:2XX` `:4XX` `:5XX` | HTTP status code assertion |
| `ASSERT_RESPONSE` | `:CONTAINS` `:NOT_EQUALS` `:NOT_NULL` `:NULL` `:HEADER` | Response body/header assertion |
| `STORE_DATA` | `:RESPONSE` `:HEADER` `:STATUS` | Extract response value → variable |

### DB Actions

| Action | Sub-cases | Description |
|---|---|---|
| `DB_QUERY` | `:INLINE` | Execute SQL SELECT |
| `DB_EXECUTE` | `:INLINE` | Execute SQL INSERT/UPDATE/DELETE |
| `ASSERT_ROW_COUNT` | `:AT_LEAST` `:AT_MOST` `:ZERO` `:NOT_ZERO` | Row count assertion |
| `ASSERT_COLUMN` | `:CONTAINS` `:NOT_EQUALS` `:NOT_NULL` `:NULL` | Cell value assertion |
| `STORE_DATA` | `:DB` `:DB_COUNT` | Extract DB value → variable |

### Core Actions

| Action | Sub-cases | Description |
|---|---|---|
| `CHANGE_DATA_STORE` | `:DELETE` | Set or delete a variable |
| `COMPARE` | `:EQUALS` `:NOT_EQUALS` `:CONTAINS` `:NOT_CONTAINS` `:STARTS_WITH` `:ENDS_WITH` `:GT` `:LT` `:GTE` `:LTE` `:REGEX` | Assert two values |
| `LOG` | | Write a message to the logs |
| `INCLUDE` | | Inline another CSV file |
| `FINALLY` | | Marker — steps below always run |

---

## Variable System

Variables are scoped per layer and resolved with `${SCOPE:varName}`:

```csv
# Store from UI
GET_TEXT,OrderPage.orderId,,capturedId
# Use in API template
SEND_REQUEST,templates/orders/get.json,,
# Store from API response
STORE_DATA:RESPONSE,data.status,,orderStatus
# Assert in DB
DB_QUERY:INLINE,,,SELECT * FROM orders WHERE id='${UI:capturedId}'
ASSERT_COLUMN,status,,${API:orderStatus}
```

| Syntax | Scope |
|---|---|
| `${varName}` | GLOBAL (default) |
| `${GLOBAL:varName}` | GLOBAL (explicit) |
| `${UI:varName}` | Browser layer only |
| `${API:varName}` | API layer only |
| `${DB:varName}` | Database layer only |
| `${URL:application}` | From `url.config.yml` active environment |

---

## Architecture

```
HagTestBase (TestNG)
    └─ FrameworkBootstrap
           ├─ CoreBootstrap      — CHANGE_DATA_STORE, COMPARE, LOG
           ├─ UiBootstrap        — 23 Selenium actions
           ├─ ApiBootstrap       — SEND_REQUEST, ASSERT_STATUS, ASSERT_RESPONSE
           └─ DbBootstrap        — DB_QUERY, DB_EXECUTE, ASSERT_ROW_COUNT, ASSERT_COLUMN

CsvTestParser (OpenCSV)
    └─ Step[]
           └─ DefaultActionDispatcher
                  ├─ ActionDescriptorParser  → ACTION:SUBCASE + Source modifiers
                  ├─ StepResolver            → ${VAR} substitution
                  └─ ActionRegistry.resolve(name)
                         └─ Action.execute(step, descriptor, context)
                                └─ ExecutionContext
                                       ├─ DataStore (scoped variables)
                                       ├─ UiAdapter  (Selenium WebDriver)
                                       ├─ ApiAdapter (RestAssured)
                                       └─ DbAdapter  (JDBC Connection)
```

---

## Documentation

| Document | Description |
|---|---|
| [HAG User Guide](docs/HAG_User_Guide.md) | Complete step-by-step guide for writing and running tests, setting up the framework, and detailed action reference |

---

## License

Internal project — Test Architecture Team.
