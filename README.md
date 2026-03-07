# H-A-G — Hybrid Automation Grid

**H-A-G** is a unified, data-driven test automation platform that lets teams write UI, API, and Database tests in plain **CSV files** — no programming required. A single orchestration layer handles Selenium, REST/SOAP, and JDBC automatically based on your test steps.

```csv
Action,Recipient,Source,Key

# Login via API — get a token
SEND_REQUEST,templates/auth/login.json,testdata/users.json,adminUser
ASSERT_STATUS,,,200
STORE_DATA:RESPONSE,data.token,,authToken

# Verify in the browser
NAVIGATE,,,${URL:application}/dashboard
ASSERT_VISIBLE,DashboardPage.welcomeBanner,,

# Confirm in the database
DB_QUERY:INLINE,,,SELECT * FROM sessions WHERE token = '${API:authToken}'
ASSERT_ROW_COUNT,,,1
```

---

## Features

| Layer | Status | Description |
|---|---|---|
| **UI** | ✅ Ready | Selenium-based — 18 actions registered |
| **API** | 🔜 Planned | REST (RestAssured) + SOAP |
| **DB** | 🔜 Planned | JDBC — file-based `.sql` + inline SQL |

- **Declarative CSV tests** — write tests like a spreadsheet
- **Data-driven** — JSON test data blocks, no hardcoded values
- **Cross-channel variables** — share data between UI, API, and DB steps
- **Reusable sub-flows** — `INCLUDE` to compose test scenarios
- **Safe cleanup** — `FINALLY` block always runs even on failure
- **Environment switching** — one config change to run on dev/staging/prod

---

## Quick Start

### 1. Prerequisites

| Tool | Version |
|---|---|
| Java | 17+ |
| Maven | 3.8+ |
| Chrome / Firefox | Latest |

### 2. Clone and build

```bash
git clone <repo-url>
cd H-A-G
mvn clean compile
```

### 3. Create config files in the project root

**`url.config.yml`**
```yaml
active-environment: dev
environments:
  dev:
    application: https://dev.myapp.com
    api-base:    https://api-dev.myapp.com
```

**`runner.config.yml`**
```yaml
test-suite:
  - tests/
browser:
  type: chrome
  headless: false
```

**`testdata.config.yml`**
```yaml
paths:
  locators:   src/main/resources/locators/
  test-data:  src/main/resources/testdata/
```

### 4. Create a locator file

`src/main/resources/locators/LoginPage.json`
```json
{
  "username-field": { "type": "id",  "value": "username" },
  "password-field": { "type": "css", "value": "input[type='password']" },
  "login-button":   { "type": "id",  "value": "loginBtn" }
}
```

### 5. Create a test data file

`src/main/resources/testdata/login/users.json`
```json
{
  "validUser": {
    "username": "john@example.com",
    "password": "Secret@123"
  }
}
```

### 6. Write your first test

`tests/login/login_test.csv`
```csv
Action,Recipient,Source,Key
NAVIGATE,,,${URL:application}/login
INPUT,LoginPage.username-field,testdata/login/users.json,validUser
INPUT,LoginPage.password-field,testdata/login/users.json,validUser
CLICK,LoginPage.login-button,,
ASSERT_VISIBLE,HomePage.dashboard,,
```

### 7. Run it

```bash
mvn test -pl hag-runner
```

---

## Project Structure

```
H-A-G/
├── hag-core/        Core framework — dispatcher, parser, DataStore, config
├── hag-ui/          Selenium UI adapter — 18 actions
├── hag-api/         REST/SOAP adapter (planned)
├── hag-db/          JDBC DB adapter (planned)
├── hag-runner/      TestNG runner + suite wiring
│
├── url.config.yml       ← Environment URLs (create this)
├── runner.config.yml    ← Browser, threads, test paths (create this)
├── testdata.config.yml  ← File locations, DB config (create this)
│
├── tests/               ← Your CSV test files
├── src/main/resources/
│   ├── locators/        ← Page JSON locator files
│   ├── testdata/        ← JSON test data blocks
│   ├── templates/       ← API request templates (JSON/XML)
│   └── scripts/         ← SQL query files
```

---

## Action Syntax

```
ACTION           → default behaviour
ACTION:SUBCASE   → specific variant
```

Modifier flags go in the **Source column** (pipe-separated — never a comma):

```csv
STORE_DATA,ProfilePage.name,trim|upper,savedName
WAIT:TEXT,StatusPage.label,timeout=60,Ready
ASSERT_TEXT:CONTAINS,ErrorPage.msg,ignore-case,required field
```

---

## Key Actions Reference

| Action | Description |
|---|---|
| `NAVIGATE` | Open a URL |
| `CLICK` / `CLICK:DOUBLE` / `CLICK:RIGHT` | Mouse interactions |
| `INPUT` | Type text (literal or from test data file) |
| `SELECT` / `SELECT:TEXT` / `SELECT:INDEX` | Dropdown |
| `WAIT:VISIBLE` / `WAIT:INVISIBLE` / `WAIT:TEXT` | Explicit waits |
| `ASSERT_TEXT` / `ASSERT_TEXT:CONTAINS` | Text assertions |
| `ASSERT_VISIBLE` / `ASSERT_HIDDEN` | Visibility checks |
| `ASSERT_ENABLED` / `ASSERT_DISABLED` | Element state |
| `ASSERT_COUNT` / `ASSERT_ATTRIBUTE` | DOM assertions |
| `STORE_DATA` | Read element text → variable |
| `CHANGE_DATA_STORE` | Set/update a variable |
| `COMPARE:EQUALS` / `COMPARE:GT` / `COMPARE:CONTAINS` | Value comparisons |
| `INCLUDE` | Inline another CSV as a sub-flow |
| `FINALLY` | Steps here always run (cleanup) |
| `SEND_REQUEST` | HTTP REST call (method in template) |
| `SEND_SOAP` | SOAP call |
| `ASSERT_STATUS` / `ASSERT_RESPONSE` | API assertions |
| `DB_QUERY` / `DB_QUERY:INLINE` | SQL SELECT |
| `DB_EXECUTE` / `DB_EXECUTE:INLINE` | SQL DML |
| `ASSERT_ROW_COUNT` / `ASSERT_COLUMN` | DB assertions |

For the full reference with all sub-cases and examples, see [HAG_User_Guide.md](docs/HAG_User_Guide.md).

---

## Variable System

```csv
STORE_DATA,OrderPage.orderId,,capturedId          # stores in UI scope
COMPARE:EQUALS,${UI:capturedId},,ORD-12345         # reads from UI scope
SEND_REQUEST,templates/order.json,,
STORE_DATA:RESPONSE,data.id,,orderId               # stores in API scope
DB_QUERY:INLINE,,,SELECT * FROM orders WHERE id='${API:orderId}'
```

| Syntax | Scope |
|---|---|
| `${varName}` | GLOBAL |
| `${GLOBAL:varName}` | GLOBAL (explicit) |
| `${UI:varName}` | Browser layer |
| `${API:varName}` | API layer |
| `${DB:varName}` | Database layer |
| `${URL:application}` | From `url.config.yml` |

---

## Architecture

```
hag-runner
    └─ FrameworkBootstrap
           ├─ CoreBootstrap      (CHANGE_DATA_STORE, COMPARE, LOG …)
           ├─ UiBootstrap        (CLICK, INPUT, WAIT … 18 actions)
           ├─ ApiBootstrap       (SEND_REQUEST, ASSERT_RESPONSE …)
           └─ DbBootstrap        (DB_QUERY, ASSERT_ROW_COUNT …)

CSV parser (OpenCSV)
    └─ CsvTestParser → Step[]
           └─ DefaultActionDispatcher
                  ├─ ActionDescriptorParser.parse()         → ACTION:SUBCASE
                  ├─ ActionDescriptorParser.parseModifiers() → Source modifiers
                  └─ ActionRegistry.resolve(name)
                         └─ Action.execute(step, descriptor, context)
```

---

## Documentation

| Document | Description |
|---|---|
| [HAG User Guide](docs/HAG_User_Guide.md) | Step-by-step guide to writing and running H-A-G tests |
| [BRD & Gap Analysis](docs/HAG_BRD_and_Gap_Analysis.md) | Business requirements, action taxonomy, implementation roadmap |

---

## License

Internal project — Test Architecture Team.
