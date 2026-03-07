# H-A-G — Hybrid Automation Grid
## Business Requirements Document & Gap Analysis
> Version 1.0 | Date: 2026-03-07 | Status: Draft

---

## Part 1 — Gap Analysis: Requirements vs. Current Implementation

### 1.1 Requirements Alignment Scorecard

| Requirement Area | Status | Notes |
|---|---|---|
| CSV-driven test execution | ✅ Implemented | Engine, parser, dispatcher all working |
| UI automation (Selenium) | ✅ Implemented | 18 actions registered |
| API automation (REST) | ❌ **NOT STARTED** | `hag-api` has zero source files |
| API automation (SOAP) | ❌ **NOT STARTED** | Not planned in current code |
| DB automation | ❌ **NOT STARTED** | `hag-db` has zero source files |
| Locators in JSON | ✅ Implemented | `LocatorRepository` + `LocatorResolver` |
| Locator format (flat per page) | ⚠️ **Partial** | Current format requires `Page.Element` notation — see §1.2 |
| Test data in JSON | ✅ Implemented | `DefaultTestDataResolver` loads and caches JSON blocks |
| HTML Reports | ❌ **NOT STARTED** | Only `ConsoleReportEngine` exists |
| `INCLUDE` action | ✅ Implemented | Full recursive include with event publishing |
| `FINALLY` block | ✅ Implemented | Run-regardless-of-failure block |
| `CHANGE_DATA_STORE` | ⚠️ **Mismatch** | Exists as `SET` action — wrong name |
| `STORE_DATA` (with modifiers) | ⚠️ **Partial** | `GET_TEXT` stores UI text; no trim/substring/regex modifiers; no API/DB variant |
| `COMPARE_VALUES` (multi-operator) | ⚠️ **Partial** | `ASSERT` supports EQUALS/NOT_EQUALS/CONTAINS; missing GT, LT, GTE, LTE, NOT_CONTAINS, REGEX |
| Time Zone / Date validations | ❌ **NOT STARTED** | No date/time action exists |
| `##variable##` API template interpolation | ❌ **NOT STARTED** | Core uses `${VAR}` syntax; API template merging not built |
| Parallel execution / Grid | ❌ **NOT STARTED** | No parallel runner, no Selenium Grid config |
| CI/CD integration | ❌ **NOT STARTED** | No TestNG XML suite, no Maven Surefire config |
| `SEND_REQUEST` (REST) | ❌ **NOT STARTED** | |
| `SEND_SOAP_REQUEST` (SOAP) | ❌ **NOT STARTED** | |
| DB `STORE_DATA` (query result) | ❌ **NOT STARTED** | |

---

### 1.2 Identified Gaps & Required Code Work

#### 🔴 P0 — Blockers (Framework Cannot Fulfil Its Core Purpose Without These)

---

**GAP-01: `hag-api` module is completely empty**
- **Impact:** API testing impossible; `ExecutionContext.validateConfiguration()` will throw because `apiAdapter == null`
- **Required:**
  - `ApiBootstrap` — registers `SEND_REQUEST`, `SEND_SOAP_REQUEST`, `STORE_RESPONSE`, `ASSERT_RESPONSE_STATUS`, `ASSERT_JSON_PATH`, `ASSERT_XPATH` (SOAP)
  - `RestAssuredApiAdapter` — implements `ApiAdapter` interface
  - `RequestTemplateLoader` — loads JSON/XML template files from classpath
  - `TemplateMerger` — replaces `##variable##` placeholders from test data JSON blocks
  - `SoapRequestBuilder` — builds SOAP envelopes from XML templates
  - Wire `ApiBootstrap` into `FrameworkBootstrap`

---

**GAP-02: `hag-db` module is completely empty**
- **Impact:** DB testing impossible; same `validateConfiguration()` blocker
- **Required:**
  - `DbBootstrap` — registers `DB_QUERY`, `DB_EXECUTE`, `ASSERT_ROW_COUNT`, `STORE_QUERY_RESULT`
  - `JdbcDbAdapter` — implements `DbAdapter`; factory-based (MySQL, MSSQL based on config)
  - `SqlScriptLoader` — reads `.sql` files from paths given in Recipient column
  - Wire `DbBootstrap` into `FrameworkBootstrap`

---

**GAP-03: `FrameworkBootstrap` crashes for mixed-mode tests**
- **Impact:** Any test that includes DB or API steps will crash at `validateConfiguration()` because `apiAdapter` and `dbAdapter` are always null
- **Required revamp:** `validateConfiguration()` should validate only the adapters required by the steps actually loaded. OR the bootstrap should provide no-op stub adapters for layers not in use. Simplest fix: make each adapter check optional, only failing at step-execute time.

---

**GAP-04: CSV parser breaks on quoted commas**
- **Impact:** Any test data, URL, or SQL path containing a comma causes row misparse
- **Current code:** `line.split(",", -1)` — crude
- **Required:** Replace with OpenCSV's `CSVReader` (already a dependency in `hag-core`)

---

#### 🟠 P1 — High Priority (Required for Spec Compliance)

---

**GAP-05: Action name mismatches vs. spec terminology**

| Spec Name | Current Name | Impact |
|---|---|---|
| `CHANGE_DATA_STORE` | `SET` | Test CSVs using spec name will fail |
| `STORE_DATA` | `GET_TEXT` (UI only) | Incomplete; name differs; no API/DB variant |
| `COMPARE_VALUES` | `ASSERT` (limited) | Operators incomplete |

- **Required:** Register aliases OR rename action `name()` returns to match spec exactly

---

**GAP-06: `STORE_DATA` is incomplete**
- **Current:** `GET_TEXT` stores raw element text; no transformation options
- **Required modifiers** (via flags/params):
  - `trim` — strip leading/trailing whitespace
  - `substring=start,end` — extract substring
  - `remove-special` — strip non-alphanumeric characters
  - `regex=pattern` — extract pattern match group 1
  - `lower` / `upper` — case conversion
- **Also required:** API variant (store from JSON path in response) and DB variant (store result of query)

---

**GAP-07: `COMPARE_VALUES` / `ASSERT` operators incomplete**
- **Missing operators vs. spec:**
  - `GREATER_THAN`, `LESS_THAN`, `GREATER_THAN_OR_EQUAL`, `LESS_THAN_OR_EQUAL`
  - `NOT_CONTAINS`, `STARTS_WITH`, `ENDS_WITH`
  - `REGEX_MATCH`
- **Required:** Extend `AssertAction.evaluate()` switch to include all the above

---

**GAP-08: No `COMPARE_VALUES` cross-variable comparison**
- **Current `ASSERT`:** Compares `${recipient}` vs `${key}` — both must be DataStore variables or literals
- **Required:** Also support comparing: element text vs stored value, API JSON path value vs stored value, DB query result vs stored value (cross-layer comparison)

---

**GAP-09: Locator format mismatch**
- **Spec format** (flat, single JSON file per page):
```json
{
  "login-button": { "type": "id", "value": "login-btn" },
  "user-name": { "type": "xpath", "value": "//input[@id='username']" }
}
```
- **Current format in `LocatorResolver`:** Uses `Page.Element` → loads `locators/Page.json` → looks up `Element` key → expects `{ "type": ..., "value": ... }` structure
- **Assessment:** ✅ Actually **compatible** — if you name your locator file `LoginPage.json` and write `LoginPage.login-button` in the CSV, it works. The spec's flat JSON format is exactly what `LocatorRepository` expects inside the file.
- **Gap:** No documentation explaining this. Users will be confused.

---

**GAP-10: API template variable interpolation (RESOLVED — using `${VAR}` syntax)**
- **Decision:** `${VAR}` / `${SCOPE:VAR}` syntax (already implemented in core) is the standard for both CSV steps AND API templates. The `##TOKEN##` pattern is NOT required.
- **Still required:** `TemplateMerger` class in `hag-api` that reads a REST/SOAP template file, resolves the test data block from `Source + Key` columns, and replaces all `${FIELDNAME}` tokens with values from that data block before sending the request.

---

**GAP-11: No HTML Report Engine**
- **Current:** Only `ConsoleReportEngine` — logs to console via SLF4J
- **Required:** `HtmlReportEngine` implementing `ReportEngine` — subscribes to all events and writes an HTML test results page (with pass/fail summary, step-by-step log, screenshots embedded)
- **Recommended:** ExtentReports or a hand-built HTML writer

---

#### 🟡 P2 — Medium Priority (Important for Enterprise Use)

---

**GAP-12: No parallel execution support**
- **Required:**
  - TestNG `@DataProvider` based runner that runs multiple test CSVs in parallel
  - Thread-local `ExecutionContext` (currently done via `ExecutionContextHolder` — good)
  - Selenium Grid / RemoteWebDriver config in `FrameworkConfig`

---

**GAP-13: No CI/CD integration artefacts**
- **Required:**
  - `testng.xml` suite file in `hag-runner`
  - Maven Surefire plugin config pointing to `testng.xml`
  - Environment override via system properties (e.g., `-DbaseUrl=https://staging.example.com`)

---

**GAP-14: No Time Zone / Date Validation action**
- **Spec requirement:** Handle timestamp validation, date format conversion
- **Required action:** `DATE_ASSERT` or `DATE_FORMAT` action supporting:
  - Parse date strings with given format patterns
  - Compare dates with tolerance (±N days/hours)
  - Convert between time zones
  - Store formatted date strings into DataStore

---

**GAP-15: `DataScope` enum is missing scope granularity**
- **Current scopes:** `UI, API, DB, GLOBAL`
- **Problem:** `UI, API, DB` are layer-scoped, not test-scoped. Two parallel UI tests would share the same `UI` scope DataStore entries and collide.
- **Required:** Add `TEST` scope (scoped per test execution) and `STEP` scope (cleared after each step). `GLOBAL` stays for session-wide values. The layer scopes `UI/API/DB` can be kept for inter-layer data passing.

---

**GAP-16: `TestDataResolver` source expression is brittle**
- **Current:** Expects `folder.subfolder.filename` dot-notation. Splits by `.` then reconstructs path — breaks on filenames with dots.
- **Spec:** `source` column = file path like `testdata\login\userData.json`
- **Required:** Allow direct file path notation. Parse `source` as a relative or classpath path instead of custom dot-notation.

---

**GAP-17: No `TestNG @Test` runner class exists**
- **Required:** A base `HagTestBase` class that test methods extend, which handles `ExecutionEngine.execute()` and wires all adapters via `FrameworkBootstrap`

---

#### 🟢 P3 — Polish / Quality (Recommended Before v1.0 Release)

- **GAP-18:** No unit tests anywhere in the project (no `src/test/java`)
- **GAP-19:** `FrameworkConfig` has no environment-awareness (no property file / env var loading)
- **GAP-20:** No locator hot-reload — `LocatorRepository` caches forever; no way to refresh during test run
- **GAP-21:** `CsvTestParser` silently skips unreadable rows — should report them clearly
- **GAP-22:** No request/response logging for API calls (essential for debugging)
- **GAP-23:** Screenshot directory is configured but no auto-cleanup or size limit

---

#### 🔴 P0 — New Gaps Identified from Design Review

---

**GAP-24: `ActionDescriptorParser` uses commas inside brackets — conflicts with CSV format**
- **Root cause:** `ActionDescriptor` content is parsed by splitting `[flag, param=value]` on commas — but the entire CSV row is ALSO comma-split, so `CLICK[double, retry=3]` is parsed as: Action=`CLICK[double`, Recipient=`retry=3]`
- **Required:** Complete redesign of action sub-case syntax. **Decision: colon notation + Source column for modifiers** — see §2.10.

---

**GAP-25: `ASSERT_VISIBLE` conflates visibility and element state**
- **Current:** A single action with `[hidden]`, `[enabled]`, `[disabled]`, `[selected]` flags
- **Required:** Split into standalone actions: `ASSERT_VISIBLE`, `ASSERT_HIDDEN`, `ASSERT_ENABLED`, `ASSERT_DISABLED`, `ASSERT_SELECTED`
- **Reason:** Clarity — a test step should read like a sentence, not require knowledge of flag semantics

---

**GAP-26: `SEND_REQUEST` requires HTTP method as a CSV parameter**
- **Current plan:** `SEND_REQUEST[method=GET]` — conflicts with comma issue above
- **Decision:** HTTP method defined inside the request template file (`"_method": "GET"`). CSV stays clean.

---

**GAP-27: No configuration file system**
- **Current:** `FrameworkConfig` is hardcoded in `FrameworkBootstrap`. No external config files.
- **Required:** Three YAML config files: `url.config.yml`, `runner.config.yml`, `testdata.config.yml` — see §2.16.

---

## Part 2 — Business Requirements Document (BRD)

---

### 2.1 Executive Summary

**H-A-G (Hybrid Automation Grid)** is a unified, data-driven test automation platform built on Java 17 and Maven, capable of executing UI, API, and Database validation workflows through a single orchestration layer.

Test flows are authored in plain **CSV files** — no programming knowledge required to write tests. All technical concerns (browser drivers, HTTP connections, database connections) are handled by the framework's pluggable adapter layer.

---

### 2.2 Business Objectives

| # | Objective |
|---|---|
| 1 | Unify UI, API, and DB testing under one framework |
| 2 | Enable test authoring via structured CSV — no code required |
| 3 | Modular, maintainable architecture with clear responsibility separation |
| 4 | Support cross-channel test flows (UI → API → DB in a single test) |
| 5 | Reusable step libraries via `INCLUDE` directive |
| 6 | Scale to parallel execution and CI/CD pipelines |
| 7 | Produce HTML test reports with screenshots |

---

### 2.3 Stakeholders

| Role | Responsibility |
|---|---|
| QA Engineers | Author test CSVs, manage locators and test data JSONs |
| Test Architects | Design framework extensions, new actions, adapters |
| DevOps | Integrate into CI/CD, manage environment configurations |
| Developers | Provide API/service contracts, DB schemas |

---

### 2.4 System Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                        CSV Test File                         │
│    Action | Recipient | Source | Key                         │
└───────────────────────────┬──────────────────────────────────┘
                            │ parsed by
                ┌───────────▼────────────────┐
                │     hag-core               │
                │  ┌──────────────────────┐  │
                │  │  CsvTestParser       │  │
                │  │  IncludeResolver     │  │
                │  │  DefaultExecEngine   │  │
                │  │  ActionDispatcher    │  │
                │  │  ExecutionContext    │  │
                │  │  DataStore          │  │
                │  │  EventPublisher     │  │
                │  └──────────────────────┘  │
                └────────┬────────┬────────┬─┘
                         │        │        │
          ┌──────────────▼──┐  ┌──▼───┐  ┌─▼──────┐
          │   hag-ui        │  │hag-  │  │hag-db  │
          │  (Selenium)     │  │ api  │  │(JDBC)  │
          │  18 UI Actions  │  │(REST │  │        │
          │                 │  │SOAP) │  │        │
          └─────────────────┘  └──────┘  └────────┘
                         │
          ┌──────────────▼──────────────┐
          │   hag-runner                │
          │  FrameworkBootstrap         │
          │  TestNG Runner              │
          └─────────────────────────────┘
```

---

### 2.5 File Structure & Conventions

```
project-root/
├── tests/                          ← Test scenario CSV files
│   ├── login/
│   │   └── login_flow.csv
│   └── checkout/
│       └── purchase_test.csv
│
├── src/main/resources/
│   ├── locators/                   ← UI locator JSON files (one per page)
│   │   ├── LoginPage.json
│   │   └── CheckoutPage.json
│   │
│   ├── testdata/                   ← Test data JSON files
│   │   ├── login/
│   │   │   └── userData.json
│   │   └── checkout/
│   │       └── productData.json
│   │
│   └── templates/                  ← API request templates
│       ├── login/
│       │   └── authenticate.json   ← REST template
│       └── payment/
│           └── processPayment.xml  ← SOAP template
```

---

### 2.6 Test CSV Format

**File Headers (mandatory, in order):**

| Column | Purpose |
|---|---|
| `Action` | The operation to execute (e.g. `NAVIGATE`, `SEND_REQUEST`, `DB_QUERY`) |
| `Recipient` | Target: locator key (UI), template file path (API), SQL file path (DB) |
| `Source` | Test data JSON file path (relative to `testdata/`) |
| `Key` | Data block name inside the JSON file to use for this step |

**Special rows:**
- Lines starting with `#` → **commented out**, skipped
- `FINALLY` marker → all rows after this line run regardless of earlier failures
- `INCLUDE` rows → inline another CSV as a sub-flow

**Example: Login Test (`tests/login/login_flow.csv`)**
```csv
Action,Recipient,Source,Key
NAVIGATE,,,https://myapp.com/login
WAIT,LoginPage.username,,
INPUT,LoginPage.username,testdata/login/userData.json,validUser
INPUT,LoginPage.password,testdata/login/userData.json,validUser
CLICK,LoginPage.loginButton,,
ASSERT_VISIBLE,HomePage.dashboardHeader,,
STORE_DATA,HomePage.welcomeText,,welcomeMsg
COMPARE_VALUES,${welcomeMsg},,Hello Test User
FINALLY
NAVIGATE,,,https://myapp.com/logout
```

---

### 2.7 Locator Files

Stored at `src/main/resources/locators/<PageName>.json`.

**Format:**
```json
{
  "login-button": {
    "type": "id",
    "value": "login-btn"
  },
  "user-name": {
    "type": "xpath",
    "value": "//input[@id='username']"
  },
  "password": {
    "type": "css",
    "value": "input[type='password']"
  }
}
```

**Supported locator types:** `id`, `css`, `xpath`, `name`, `classname`, `tag`, `linktext`, `partiallinktext`

**CSV usage:** `PageName.element-key` (e.g. `LoginPage.user-name`)

---

### 2.8 Test Data Files

Stored at `src/main/resources/testdata/<folder>/<file>.json`.

**Format (nested blocks):**
```json
{
  "validUser": {
    "username": "john.doe@example.com",
    "password": "Secure@123",
    "expectedName": "John Doe"
  },
  "invalidUser": {
    "username": "bad@user.com",
    "password": "wrong"
  }
}
```

**Referencing in CSV:**
- `Source` = `testdata/login/userData.json`
- `Key` = `validUser`

The framework resolves `userData.json → validUser block → injects all fields` for placeholder replacement.

---

### 2.9 API Templates

Templates use `${FIELDNAME}` placeholders — identical to the rest of H-A-G. The **HTTP method and endpoint** are defined inside the template, not the CSV.

**REST Template** (`templates/login/authenticate.json`):
```json
{
  "_method": "POST",
  "_endpoint": "/api/v1/auth/login",
  "username": "${username}",
  "password": "${password}"
}
```

**SOAP Template** (`templates/payment/getBalance.xml`):
```xml
<!-- _method: POST -->
<!-- _endpoint: /payment/service -->
<!-- _soap-action: urn:getBalance -->
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
  <soapenv:Body>
    <getBalance>
      <accountId>${accountId}</accountId>
    </getBalance>
  </soapenv:Body>
</soapenv:Envelope>
```

---

### 2.10 Action Syntax

The H-A-G CSV action column follows a **conflict-free** syntax designed to avoid any collision with the four CSV column separators.

#### Format

```
ACTION:SUBCASE
```

- **`ACTION`** — the primary action name (e.g. `CLICK`, `WAIT`, `ASSERT_TEXT`)
- **`SUBCASE`** — optional, colon-separated variant (e.g. `DOUBLE`, `INVISIBLE`, `CONTAINS`)
- **No brackets or parameter strings in the Action column** — this eliminates all CSV comma conflicts

#### Modifier Flags via Source Column

When the `Source` column is not used for a test data file, it carries **modifier flags** for the step. Multiple flags are separated by a **pipe** `|` (never a comma):

```csv
STORE_DATA,OrderPage.name,trim|upper,capturedName
ASSERT_TEXT:CONTAINS,ErrorPage.msg,ignore-case,required field
WAIT:TEXT,StatusPage.label,timeout=60,Ready
```

#### Summary Rules

| Column | Primary Purpose | Secondary Purpose |
|---|---|---|
| Action | `ACTION:SUBCASE` | — |
| Recipient | Target locator / JSON path / SQL file | Variable name (for store/compare) |
| Source | Test data file path | Modifier flags `flag\|flag2\|param=val` |
| Key | Expected value / data block name | Variable name to store into |

---

### 2.11 Complete Action Sub-case Reference

> **✅ = implemented** | **📋 = planned**

---

#### 🔵 Core Actions

| Action:Subcase | Recipient | Source (modifiers) | Key | Description |
|---|---|---|---|---|
| `INCLUDE` ✅ | path to CSV | | | Run another CSV as sub-flow |
| `FINALLY` ✅ | | | | Steps after this always run |
| `CHANGE_DATA_STORE` 📋 | variable name | `scope=GLOBAL\|UI\|API\|DB` | value or `${VAR}` | Set / update a DataStore variable |
| `CHANGE_DATA_STORE:DELETE` 📋 | variable name | | | Remove a variable from store |
| `COMPARE:EQUALS` 📋 | actual `${VAR}` or literal | | expected value | Exact equality check |
| `COMPARE:NOT_EQUALS` 📋 | actual | | expected | Inequality check |
| `COMPARE:CONTAINS` 📋 | actual | | expected | Substring present |
| `COMPARE:NOT_CONTAINS` 📋 | actual | | expected | Substring absent |
| `COMPARE:STARTS_WITH` 📋 | actual | | expected | Starts with value |
| `COMPARE:ENDS_WITH` 📋 | actual | | expected | Ends with value |
| `COMPARE:GT` 📋 | actual | | expected | Numeric greater-than |
| `COMPARE:LT` 📋 | actual | | expected | Numeric less-than |
| `COMPARE:GTE` 📋 | actual | | expected | Numeric greater-or-equal |
| `COMPARE:LTE` 📋 | actual | | expected | Numeric less-or-equal |
| `COMPARE:REGEX` 📋 | actual | | regex pattern | Regex match |
| `DATE_ASSERT:EQUALS` 📋 | date value or `${VAR}` | `format=dd/MM/yyyy` | expected date | Dates must match |
| `DATE_ASSERT:BEFORE` 📋 | date value or `${VAR}` | `format=dd/MM/yyyy` | expected date | Actual is before expected |
| `DATE_ASSERT:AFTER` 📋 | date value or `${VAR}` | `format=dd/MM/yyyy` | expected date | Actual is after expected |
| `DATE_ASSERT:WITHIN` 📋 | date value or `${VAR}` | `days=N\|tz=UTC` | expected date | Within ±N days |
| `DATE_FORMAT` 📋 | date value or `${VAR}` | `format=yyyy-MM-dd\|tz=IST` | variable name | Convert format and store |
| `LOG` 📋 | | | message text | Write message to report |

**Examples:**
```csv
INCLUDE,tests/common/login.csv,,
CHANGE_DATA_STORE,envUrl,scope=GLOBAL,https://staging.example.com
COMPARE:GT,${API:resultCount},,0
COMPARE:CONTAINS,${UI:welcomeMsg},,Hello
DATE_ASSERT:WITHIN,${API:createdAt},days=1|tz=UTC,${GLOBAL:today}
LOG,,,Starting checkout flow for ${GLOBAL:userId}
```

---

#### 🟢 UI Actions — Browser (Selenium)

##### Navigation

| Action:Subcase | Recipient | Source | Key | Description |
|---|---|---|---|---|
| `NAVIGATE` ✅ | | | URL or relative path | Open a URL in the browser |
| `NAVIGATE:BACK` 📋 | | | | Browser back button |
| `NAVIGATE:FORWARD` 📋 | | | | Browser forward button |
| `NAVIGATE:REFRESH` 📋 | | | | Reload current page |

```csv
NAVIGATE,,,https://app.example.com/login
NAVIGATE,,,/dashboard
NAVIGATE:BACK,,,
NAVIGATE:REFRESH,,,
```

---

##### Click & Mouse

| Action:Subcase | Recipient | Source | Key | Description |
|---|---|---|---|---|
| `CLICK` ✅ | `Page.Element` | | | Standard left click |
| `CLICK:DOUBLE` ✅ | `Page.Element` | | | Double click |
| `CLICK:RIGHT` 📋 | `Page.Element` | | | Right / context-menu click |
| `CLICK:HOLD` 📋 | `Page.Element` | | | Click and hold |
| `JS_CLICK` ✅ | `Page.Element` | | | JavaScript click — bypasses overlays |
| `HOVER` ✅ | `Page.Element` | | | Mouse over element |
| `DRAG_DROP` ✅ | Source `Page.Element` | | Target `Page.Element` | Drag and drop |

```csv
CLICK,LoginPage.loginButton,,
CLICK:DOUBLE,GridPage.rowItem,,
CLICK:RIGHT,TablePage.row,,
DRAG_DROP,KanbanPage.todoCard,,KanbanPage.doneColumn
```

---

##### Forms & Input

| Action:Subcase | Recipient | Source | Key | Description |
|---|---|---|---|---|
| `INPUT` ✅ | `Page.Element` | | literal text | Type literal text into field (appends to existing) |
| `INPUT` ✅ | `Page.Element` | `clear` | literal text | Clear field first, then type |
| `INPUT` ✅ | `Page.Element` | `testdata/file.json` | data block name | Type value resolved from test data file |
| `INPUT` ✅ | `Page.Element` | `clear\|testdata/file.json` | data block name | Clear field, then type value from test data file |
| `INPUT:KEY` 📋 | `Page.Element` | | key name | Send keyboard key (Enter, Tab, Escape, ArrowDown…) |
| `INPUT:FILE` 📋 | `Page.Element` | | file path | File upload — set file input path |
| `CLEAR` ✅ | `Page.Element` | | | Clear field contents |
| `SELECT` ✅ | `Page.Element` | | option value | Dropdown — select by value attribute (literal) |
| `SELECT` ✅ | `Page.Element` | `testdata/file.json` | data block name | Dropdown — value resolved from test data file |
| `SELECT:TEXT` ✅ | `Page.Element` | | visible label | Dropdown — select by visible text (literal) |
| `SELECT:TEXT` ✅ | `Page.Element` | `testdata/file.json` | data block name | Dropdown — visible text from test data file |
| `SELECT:INDEX` ✅ | `Page.Element` | | 0-based index | Dropdown — select by position |
| `SELECT:DESELECT_ALL` 📋 | `Page.Element` | | | Multi-select — clear all selections |

> **Test data binding rule:** When `Source` contains a file path (ends in `.json`), the framework loads that file, finds the named block in `Key`, and makes all its fields available as `${fieldName}` variables for that step. The action then uses the appropriate field (e.g. `${username}` for an input bound to a field named `username`).

```csv
# Literal value in Key
INPUT,LoginPage.username,,john@example.com
INPUT,SearchPage.box,clear,new search term

# From test data file (Source = JSON path, Key = block name)
INPUT,LoginPage.username,testdata/login/users.json,validUser
INPUT,LoginPage.password,testdata/login/users.json,validUser
INPUT,LoginPage.username,clear|testdata/login/users.json,validUser

# Keyboard key
INPUT:KEY,SearchPage.box,,Enter

# Dropdown selections — literal
SELECT,CheckoutPage.country,,US
SELECT:TEXT,CheckoutPage.country,,United States
SELECT:INDEX,MonthPicker.month,,2

# Dropdown — from test data file
SELECT:TEXT,CheckoutPage.country,testdata/checkout/shipping.json,ukAddress
```


---

##### Scroll & Frame

| Action:Subcase | Recipient | Source | Key | Description |
|---|---|---|---|---|
| `SCROLL` ✅ | `Page.Element` | | | Scroll element into view |
| `SCROLL:TOP` ✅ | | | | Scroll to page top |
| `SCROLL:BOTTOM` ✅ | | | | Scroll to page bottom |
| `SWITCH_FRAME` ✅ | `Page.IFrame` | | | Switch into an iframe |
| `SWITCH_FRAME:DEFAULT` ✅ | | | | Return to main document |
| `SWITCH_FRAME:PARENT` 📋 | | | | Switch to parent frame |
| `SWITCH_WINDOW:NEW` ✅ | | | | Switch to newest tab/window |
| `SWITCH_WINDOW:TITLE` ✅ | | | page title | Switch to window with matching title |
| `SWITCH_WINDOW:CLOSE` 📋 | | | | Close current window |

```csv
SCROLL,TablePage.lastRow,,
SCROLL:BOTTOM,,,
SWITCH_FRAME,PaymentPage.cardIframe,,
SWITCH_FRAME:DEFAULT,,,
SWITCH_WINDOW:NEW,,,
SWITCH_WINDOW:TITLE,,,Payment Confirmation
```

---

##### Wait

| Action:Subcase | Recipient | Source | Key | Description |
|---|---|---|---|---|
| `WAIT:VISIBLE` ✅ | `Page.Element` | `timeout=N` | | Wait until element is displayed |
| `WAIT:INVISIBLE` ✅ | `Page.Element` | `timeout=N` | | Wait until element disappears |
| `WAIT:PRESENCE` ✅ | `Page.Element` | `timeout=N` | | Wait until element exists in DOM |
| `WAIT:CLICKABLE` ✅ | `Page.Element` | `timeout=N` | | Wait until element can be clicked |
| `WAIT:TEXT` ✅ | `Page.Element` | `timeout=N` | expected text | Wait until element contains text |

```csv
WAIT:VISIBLE,DashboardPage.mainMenu,,
WAIT:INVISIBLE,LoadingPage.spinner,timeout=60,
WAIT:TEXT,StatusPage.label,timeout=30,Ready
```

---

##### Assertions

**Text assertions:**

| Action:Subcase | Recipient | Source | Key | Description |
|---|---|---|---|---|
| `ASSERT_TEXT` ✅ | `Page.Element` | | expected text | Exact text match |
| `ASSERT_TEXT:CONTAINS` ✅ | `Page.Element` | | expected text | Element text contains value |
| `ASSERT_TEXT:CONTAINS` ✅ | `Page.Element` | `ignore-case` | expected text | Case-insensitive contains |
| `ASSERT_TEXT:NOT_CONTAINS` 📋 | `Page.Element` | | value | Text must NOT contain value |
| `ASSERT_TEXT:STARTS_WITH` 📋 | `Page.Element` | | prefix | Text starts with prefix |
| `ASSERT_TEXT:ENDS_WITH` 📋 | `Page.Element` | | suffix | Text ends with suffix |
| `ASSERT_TEXT:REGEX` 📋 | `Page.Element` | | pattern | Text matches regex pattern |

```csv
ASSERT_TEXT,OrderPage.status,,Confirmed
ASSERT_TEXT:CONTAINS,ErrorPage.message,,required field
ASSERT_TEXT:CONTAINS,ProfilePage.name,ignore-case,john doe
ASSERT_TEXT:REGEX,InvoicePage.ref,,INV-\d{8}
```

**Visibility assertions** — one action per state:

| Action | Recipient | Key | Description |
|---|---|---|---|
| `ASSERT_VISIBLE` ✅ | `Page.Element` | | Element must be displayed on screen |
| `ASSERT_HIDDEN` 📋 | `Page.Element` | | Element must be absent or hidden |
| `ASSERT_ENABLED` 📋 | `Page.Element` | | Element must be interactable (not disabled) |
| `ASSERT_DISABLED` 📋 | `Page.Element` | | Element must have disabled attribute |
| `ASSERT_SELECTED` 📋 | `Page.Element` | | Checkbox or radio must be checked |

```csv
ASSERT_VISIBLE,SuccessPage.confirmationBanner,,
ASSERT_HIDDEN,ErrorPage.errorBanner,,
ASSERT_ENABLED,CheckoutPage.placeOrderBtn,,
ASSERT_DISABLED,FormPage.submitBtn,,
ASSERT_SELECTED,PrefsPage.darkModeToggle,,
```

**Count and attribute assertions:**

| Action:Subcase | Recipient | Source | Key | Description |
|---|---|---|---|---|
| `ASSERT_COUNT` ✅ | `Page.Element` | | expected count | Exact element count |
| `ASSERT_COUNT:AT_LEAST` 📋 | `Page.Element` | | min count | Count must be ≥ Key |
| `ASSERT_ATTRIBUTE` ✅ | `Page.Element` | attribute name | expected value | Exact attribute value |
| `ASSERT_ATTRIBUTE:CONTAINS` ✅ | `Page.Element` | attribute name | partial value | Attribute contains value |

```csv
ASSERT_COUNT,CartPage.lineItems,,3
ASSERT_COUNT:AT_LEAST,ResultPage.rows,,1
ASSERT_ATTRIBUTE,NavPage.homeLink,href,/home
ASSERT_ATTRIBUTE:CONTAINS,NavPage.activeTab,class,active
```

---

##### Store & JavaScript

| Action:Subcase | Recipient | Source (modifiers, pipe-separated) | Key | Description |
|---|---|---|---|---|
| `STORE_DATA` ✅ | `Page.Element` | *(none)* | variable name | Store raw element text |
| `STORE_DATA` ✅ | `Page.Element` | `trim` | variable name | Store trimmed text |
| `STORE_DATA` ✅ | `Page.Element` | `upper` or `lower` | variable name | Store case-converted text |
| `STORE_DATA` ✅ | `Page.Element` | `trim\|upper` | variable name | Chain multiple modifiers |
| `STORE_DATA` 📋 | `Page.Element` | `substring=0\|8` | variable name | Substring from index 0 to 8 |
| `STORE_DATA` 📋 | `Page.Element` | `remove-special` | variable name | Strip non-alphanumeric chars |
| `STORE_DATA` 📋 | `Page.Element` | `regex=pattern` | variable name | Store first regex match group |
| `STORE_DATA` 📋 | `Page.Element` | `scope=GLOBAL` | variable name | Save to GLOBAL scope |
| `JS_EXECUTE` ✅ | | | JavaScript code | Execute JS snippet |
| `JS_EXECUTE:STORE` ✅ | | variable name | JavaScript code | Store JS return value |

```csv
STORE_DATA,OrderPage.orderId,,rawOrderId
STORE_DATA,ProfilePage.name,trim|upper,upperName
STORE_DATA,InvoicePage.ref,regex=ORD-\d+,invoiceNumber
STORE_DATA,OrderPage.version,substring=0|8|scope=GLOBAL,appVersion
JS_EXECUTE,,,window.scrollTo(0, 0)
JS_EXECUTE:STORE,pageTitle,,return document.title;
```

---

#### 🟡 API Actions — REST & SOAP (`hag-api`) 📋

##### Sending Requests

| Action | Recipient | Source | Key | Description |
|---|---|---|---|---|
| `SEND_REQUEST` | template `.json` path | test data file path | data block name | Execute REST call (method from template `_method` field) |
| `SEND_SOAP` | template `.xml` path | test data file path | data block name | Execute SOAP call |

```csv
SEND_REQUEST,templates/auth/login.json,testdata/auth/creds.json,validUser
SEND_SOAP,templates/payment/balance.xml,testdata/payment/accounts.json,premiumAccount
```

---

##### Assertions on Last Response

| Action:Subcase | Recipient | Source | Key | Description |
|---|---|---|---|---|
| `ASSERT_STATUS` | | | expected code | Assert HTTP status code |
| `ASSERT_STATUS:RANGE` | | | `200\|299` (pipe-separated) | Status code within range |
| `ASSERT_RESPONSE` | JSON path expression | | expected value | Assert JSON response field |
| `ASSERT_RESPONSE:CONTAINS` | JSON path expression | | partial value | Substring match on field |
| `ASSERT_RESPONSE:NOT_NULL` | JSON path expression | | | Field must exist and be non-null |
| `ASSERT_SOAP` | XPath expression | | expected value | Assert SOAP XML field |

```csv
ASSERT_STATUS,,,201
ASSERT_STATUS:RANGE,,,200|299
ASSERT_RESPONSE,data.user.email,,john@example.com
ASSERT_RESPONSE:CONTAINS,data.message,,success
ASSERT_RESPONSE:NOT_NULL,data.token,,
ASSERT_SOAP,//ns:balance/text(),,500.00
```

---

##### Store from Response

| Action:Subcase | Recipient | Source | Key | Description |
|---|---|---|---|---|
| `STORE_DATA:RESPONSE` | JSON path expression | | variable name | Store response field value |
| `STORE_DATA:HEADER` | header name | | variable name | Store response header value |
| `STORE_DATA:STATUS` | | | variable name | Store HTTP status code |

```csv
STORE_DATA:RESPONSE,data.user.id,,userId
STORE_DATA:HEADER,Authorization,,authToken
STORE_DATA:STATUS,,,lastStatusCode
```

---

#### 🟤 DB Actions — Database (`hag-db`) 📋

Two modes are supported:
- **File-based** — `Recipient` = path to a `.sql` file (recommended for complex queries)
- **Inline** — use `:INLINE` sub-case, SQL written directly in the `Key` column (for simple, one-off queries)

##### Query & Execute

| Action:Subcase | Recipient | Source | Key | Description |
|---|---|---|---|---|
| `DB_QUERY` | path to `.sql` file | test data file (for params) | data block | Run SELECT — stores result set |
| `DB_QUERY:INLINE` | | | SQL string | Run SELECT with inline SQL |
| `DB_QUERY:SINGLE` | path to `.sql` file | | | Same as `DB_QUERY` but fails if > 1 row |
| `DB_EXECUTE` | path to `.sql` file | test data file (for params) | data block | Run INSERT / UPDATE / DELETE |
| `DB_EXECUTE:INLINE` | | | SQL string | Run DML with inline SQL |

```csv
DB_QUERY,scripts/user/findByEmail.sql,testdata/user/data.json,testUser
DB_QUERY:INLINE,,,SELECT id FROM users WHERE email = '${GLOBAL:testEmail}'
DB_EXECUTE,scripts/cleanup/deleteTestUser.sql,,
DB_EXECUTE:INLINE,,,DELETE FROM sessions WHERE user_id = '${UI:userId}'
```

---

##### Assertions on Last Query Result

| Action:Subcase | Recipient | Source | Key | Description |
|---|---|---|---|---|
| `ASSERT_ROW_COUNT` | | | expected count | Exact row count |
| `ASSERT_ROW_COUNT:AT_LEAST` | | | min count | Row count must be ≥ Key |
| `ASSERT_COLUMN` | column name | | expected value | Assert a column value in first row |

```csv
ASSERT_ROW_COUNT,,,1
ASSERT_ROW_COUNT,,,0
ASSERT_ROW_COUNT:AT_LEAST,,,5
ASSERT_COLUMN,email,,john@example.com
```

---

##### Store from Query Result

| Action:Subcase | Recipient | Source | Key | Description |
|---|---|---|---|---|
| `STORE_DATA:DB` | column name | | variable name | Store column value from first row |
| `STORE_DATA:ROW_COUNT` | | | variable name | Store the result row count |

```csv
STORE_DATA:DB,email,,dbUserEmail
STORE_DATA:ROW_COUNT,,,totalRecords
```

---

### 2.12 Data Store & Variable System

Variables stored in one step can be referenced in later steps using `${VAR}` syntax:

| Expression | Looks up in |
|---|---|
| `${username}` | GLOBAL scope |
| `${GLOBAL:username}` | GLOBAL scope (explicit) |
| `${UI:capturedText}` | UI scope |
| `${API:authToken}` | API scope |
| `${DB:userId}` | DB scope |

**Scopes:**

| Scope | Lifetime | Typical Use |
|---|---|---|
| `GLOBAL` | Entire session | Suite-wide values (env URL, auth tokens) |
| `UI` | Per test | Values read from page elements |
| `API` | Per test | Values read from API responses |
| `DB` | Per test | Values read from query results |

---

### 2.13 Cross-Channel Test Flow Example

```csv
Action,Recipient,Source,Key

# ─── Create account via API ─────────────────────────────────────
SEND_REQUEST,templates/user/create.json,testdata/user/newUser.json,testAccount
ASSERT_STATUS,,,201
STORE_DATA:RESPONSE,data.userId,,createdUserId

# ─── Verify in UI ───────────────────────────────────────────────
NAVIGATE,,,https://admin.myapp.com/users
INPUT,AdminPage.searchBox,clear,${API:createdUserId}
CLICK,AdminPage.searchBtn,,
ASSERT_TEXT,AdminPage.resultName,,Test Account User
ASSERT_VISIBLE,AdminPage.resultRow,,

# ─── Verify in DB ────────────────────────────────────────────────
DB_QUERY:INLINE,,,SELECT * FROM users WHERE id = '${API:createdUserId}'
ASSERT_ROW_COUNT,,,1
STORE_DATA:DB,email,,dbEmail
COMPARE:EQUALS,${DB:dbEmail},,testaccount@example.com

FINALLY
# ─── Cleanup ────────────────────────────────────────────────────
SEND_REQUEST,templates/user/delete.json,testdata/user/newUser.json,testAccount
```

---

### 2.14 Configuration Files

H-A-G uses three YAML configuration files stored in the project root. This separates environment-specific settings, execution parameters, and file locations into clearly named, independently editable files.

---

#### `url.config.yml` — Environment URLs

Defines all application URLs for each environment. Switch environments with a single config change or a command-line flag (`-Denv=staging`).

```yaml
# Active environment (can be overridden by -Denv=staging on command line)
active-environment: dev

environments:
  dev:
    application:   https://dev.myapp.com
    api-base:      https://api-dev.myapp.com
    admin-portal:  https://admin-dev.myapp.com

  staging:
    application:   https://staging.myapp.com
    api-base:      https://api-staging.myapp.com
    admin-portal:  https://admin-staging.myapp.com

  production:
    application:   https://www.myapp.com
    api-base:      https://api.myapp.com
    admin-portal:  https://admin.myapp.com
```

**Usage in CSV:** `NAVIGATE,,,${URL:application}/login`

---

#### `runner.config.yml` — Test Execution Settings

Defines which tests to run, browser setup, parallelism, timeouts, and output folders.

```yaml
# Which tests to run  (paths relative to project root)
test-suite:
  - tests/login/
  - tests/checkout/purchase_flow.csv
  # Prefix with '!' to exclude
  - "!tests/checkout/wip_test.csv"

# Browser settings
browser:
  type: chrome              # chrome | firefox | edge | safari
  headless: false
  window: maximize          # maximize | 1920x1080
  remote-url:               # Leave blank for local; set for Selenium Grid

# Execution settings
execution:
  parallel-threads: 3       # Number of tests to run simultaneously
  retry-attempts: 2         # Retry failing steps N times
  stop-on-failure: false    # Stop the entire suite on first failure

# Timeouts
timeouts:
  default-wait-seconds: 30
  page-load-seconds: 60
  script-seconds: 30

# Output
output:
  report-dir:      target/reports/
  screenshot-dir:  target/screenshots/

# Date & time defaults
formats:
  date:      dd/MM/yyyy
  datetime:  dd/MM/yyyy HH:mm:ss
  timezone:  IST
```

---

#### `testdata.config.yml` — File Locations

Defines where locators, test data, API templates, SQL scripts, and test CSVs live.

```yaml
# All paths are relative to the project root
paths:
  locators:        src/main/resources/locators/
  test-data:       src/main/resources/testdata/
  api-templates:   src/main/resources/templates/
  sql-scripts:     src/main/resources/scripts/
  test-scenarios:  tests/

# Database connection (can reference environment variables: ${env.DB_PASSWORD})
database:
  driver:    com.mysql.cj.jdbc.Driver
  url:       jdbc:mysql://localhost:3306/testdb
  username:  testuser
  password:  ${env.DB_PASSWORD}

# API defaults
api:
  base-url:           ${URL:api-base}
  default-timeout-ms: 10000
  ssl-validation:     true
```

---

### 2.15 Implementation Roadmap

| Priority | Item | Module | Notes |
|---|---|---|---|
| P0 | REST & SOAP API adapter | `hag-api` | `SEND_REQUEST`, `ASSERT_RESPONSE`, `STORE_DATA:RESPONSE` |
| P0 | DB adapter (JDBC) | `hag-db` | File-based and inline SQL |
| P0 | Fix `validateConfiguration()` | `hag-core` | Adapter-optional validation |
| P0 | Replace `split(",")` with OpenCSV | `hag-core` | Prevent CSV parse errors |
| P0 | Redesign `ActionDescriptorParser` | `hag-core` | Adopt colon sub-case syntax; Source column for modifiers |
| P1 | Rename actions to spec names | `hag-core` / `hag-ui` | `CHANGE_DATA_STORE`, `COMPARE`, `STORE_DATA` |
| P1 | Add `ASSERT_HIDDEN`, `ASSERT_ENABLED`, `ASSERT_DISABLED`, `ASSERT_SELECTED` | `hag-ui` | Split from `ASSERT_VISIBLE` |
| P1 | `STORE_DATA` modifiers (trim, upper, substring, regex) | `hag-core` / `hag-ui` | |
| P1 | Extended `COMPARE` operators | `hag-core` | GT, LT, GTE, LTE, STARTS_WITH, REGEX |
| P1 | HTML Report Engine | `hag-core` | ExtentReports or custom |
| P1 | Config file loader (`url.config.yml`, `runner.config.yml`, `testdata.config.yml`) | `hag-core` | |
| P2 | `DATE_ASSERT` & `DATE_FORMAT` | `hag-core` | |
| P2 | Parallel execution + Selenium Grid | `hag-runner` | |
| P2 | TestNG base runner + `testng.xml` | `hag-runner` | |
| P3 | Unit tests | All | |

---

### 2.16 Non-Functional Requirements

| Requirement | Target |
|---|---|
| CSV conflict-free syntax | No special characters inside Action column beyond the colon sub-case separator |
| Parallel thread-safety | `ExecutionContext` and `DataStore` isolated per test thread |
| Extensibility | New action = one class + one `register()` call; no framework changes |
| Portability | Environment switch via `active-environment` in `url.config.yml` or `-Denv=` flag |
| Maintainability | Locators and test data in JSON; zero code changes for element/data updates |
| Traceability | Every step logged with: index, action, target, status, duration |

---

*Document prepared as part of H-A-G v1.1 specification. For contributions, raise a pull request against the H-A-G repository.*


**REST Template** (`templates/login/authenticate.json`):
```json
{
  "username": "${username}",
  "password": "${password}"
}
```

**SOAP Template** (`templates/payment/getBalance.xml`):
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
  <soapenv:Body>
    <getBalance>
      <accountId>${accountId}</accountId>
    </getBalance>
  </soapenv:Body>
</soapenv:Envelope>
```

**CSV row that uses the above:**
```csv
SEND_REQUEST[method=POST],templates/login/authenticate.json,testdata/login/userData.json,validUser
```
The framework loads `userData.json → validUser block` and replaces `${username}` / `${password}` before sending.

---

### 2.10 Action Syntax

The `Action` column supports **bracket notation** for sub-cases, flags, and parameters:

```
ACTION_NAME[flag1, flag2, param=value, param2=value2]
```

- **Flags** — boolean switches (e.g. `double`, `clear`, `trim`)
- **Parameters** — key=value pairs (e.g. `method=POST`, `timeout=10`)
- **Multiple** items are comma-separated inside `[ ]`
- **No brackets** = default behaviour of the action

---

### 2.11 Complete Action Sub-case Reference

> **✅ = implemented** | **📋 = planned**

---

#### 🔵 Core Actions — available across all test layers

---

##### `INCLUDE` ✅
Inlines another CSV test file as a reusable sub-flow.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Default | `INCLUDE` | Recipient = relative path to target CSV |

```csv
INCLUDE,tests/common/login.csv,,
```

---

##### `FINALLY` ✅
Marker row — all steps that follow run regardless of previous failures (cleanup block).

```csv
FINALLY
NAVIGATE,,,/logout
```

---

##### `CHANGE_DATA_STORE` 📋 *(currently registered as `SET`)*
Adds or updates a variable in the DataStore.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Set GLOBAL | `CHANGE_DATA_STORE` | Recipient = var name, Key = value or `${VAR}` |
| Set scoped | `CHANGE_DATA_STORE[scope=UI]` | Write to a specific scope |
| Delete key | `CHANGE_DATA_STORE[delete]` | Remove variable from store |

```csv
CHANGE_DATA_STORE,userId,,user_001
CHANGE_DATA_STORE[scope=GLOBAL],envUrl,,https://staging.example.com
CHANGE_DATA_STORE[delete],tempToken,,
```

---

##### `COMPARE_VALUES` 📋 *(currently registered as `ASSERT` with limited operators)*
Compares two values using a specified operator. Both sides can be literals or `${VAR}` references.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Equals (default) | `COMPARE_VALUES` | Exact equality |
| Not equals | `COMPARE_VALUES[op=not-equals]` | Inequality check |
| Contains | `COMPARE_VALUES[op=contains]` | Substring check |
| Not contains | `COMPARE_VALUES[op=not-contains]` | Does not contain |
| Greater than | `COMPARE_VALUES[op=gt]` | Numeric: actual > expected |
| Less than | `COMPARE_VALUES[op=lt]` | Numeric: actual < expected |
| Greater or equal | `COMPARE_VALUES[op=gte]` | Numeric: actual >= expected |
| Less or equal | `COMPARE_VALUES[op=lte]` | Numeric: actual <= expected |
| Starts with | `COMPARE_VALUES[op=starts-with]` | String starts with |
| Ends with | `COMPARE_VALUES[op=ends-with]` | String ends with |
| Regex match | `COMPARE_VALUES[op=regex]` | Key = regex pattern |

```csv
COMPARE_VALUES,${UI:price},,99.99
COMPARE_VALUES[op=gt],${API:count},,0
COMPARE_VALUES[op=contains],${UI:welcomeMsg},,Hello
COMPARE_VALUES[op=regex],${UI:orderId},,ORD-\d{6}
```

---

##### `STORE_DATA` 📋 *(UI variant currently `GET_TEXT`; API/DB variants planned)*
Reads a value from a source (UI element, API response field, or DB column) and stores it in the DataStore, with optional text transformations.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Raw text (UI) | `STORE_DATA` | Stores element's visible text as-is |
| Trimmed | `STORE_DATA[trim]` | Strips leading/trailing whitespace |
| Uppercase | `STORE_DATA[upper]` | Converts to uppercase |
| Lowercase | `STORE_DATA[lower]` | Converts to lowercase |
| Substring | `STORE_DATA[substring=0,5]` | Extracts characters 0–5 |
| Remove special chars | `STORE_DATA[remove-special]` | Keeps only alphanumeric + spaces |
| Regex capture | `STORE_DATA[regex=ORD-\d+]` | Extracts first regex match group |
| Scoped store | `STORE_DATA[scope=GLOBAL]` | Saves to GLOBAL instead of default UI scope |
| From API JSON | `STORE_DATA[source=api, path=response.user.id]` | Stores JSON path value from last API response |
| From DB column | `STORE_DATA[source=db]` | Stores value from last DB query result column |

```csv
STORE_DATA,OrderPage.orderId,,capturedOrderId
STORE_DATA[trim, upper],ProfilePage.name,,upperName
STORE_DATA[regex=\d{10}],InvoicePage.reference,,invoiceNumber
STORE_DATA[source=api, path=data.token],,,authToken
```

---

##### `LOG` ✅
Writes a message to the console and test report.

```csv
LOG,,,Starting checkout flow with user ${GLOBAL:userId}
```

---

##### `DATE_ASSERT` 📋
Parses and validates date/time values with timezone and format awareness.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Exact match | `DATE_ASSERT[op=equals]` | Dates must match exactly |
| Before | `DATE_ASSERT[op=before]` | Actual date is before expected |
| After | `DATE_ASSERT[op=after]` | Actual date is after expected |
| Within N days | `DATE_ASSERT[op=within, days=2]` | Actual is within ±N days of expected |
| Format + timezone | `DATE_ASSERT[format=dd/MM/yyyy, tz=IST]` | Apply format and timezone before comparison |
| Format & store | `DATE_FORMAT[format=yyyy-MM-dd, store-as=fmtDate]` | Convert and save formatted date |

```csv
DATE_ASSERT[op=after, format=dd/MM/yyyy],${UI:shipDate},,${GLOBAL:orderDate}
DATE_ASSERT[op=within, days=1, tz=UTC],${API:createdAt},,${GLOBAL:now}
```

---

#### 🟢 UI Actions — Selenium layer (`hag-ui`)

---

##### `NAVIGATE` ✅
Opens a URL. Relative paths are prepended with `baseUrl` from config.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Open URL | `NAVIGATE` | Key = absolute or relative URL |
| Browser back | `NAVIGATE[back]` | Equivalent to pressing the Back button 📋 |
| Browser forward | `NAVIGATE[forward]` | Equivalent to pressing the Forward button 📋 |
| Refresh page | `NAVIGATE[refresh]` | Reload the current page 📋 |

```csv
NAVIGATE,,,https://app.example.com/login
NAVIGATE,,,/dashboard
NAVIGATE[back],,,
NAVIGATE[refresh],,,
```

---

##### `CLICK` ✅
Clicks an element. Recipient = `PageName.elementKey`.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Standard click | `CLICK` | Normal left click |
| Double click | `CLICK[double]` | Double-click |
| Right click | `CLICK[right]` | Context menu click 📋 |
| Click + hold | `CLICK[hold]` | Press and hold without releasing 📋 |
| With retry | `CLICK[retry=3]` | Retry up to 3 times on failure |

```csv
CLICK,LoginPage.loginButton,,
CLICK[double],GridPage.rowItem,,
CLICK[right],ContextPage.menuTrigger,,
CLICK[retry=3],FlakeyPage.submitBtn,,
```

---

##### `JS_CLICK` ✅
JavaScript-based click. Use when standard `CLICK` is intercepted.

```csv
JS_CLICK,OverlayPage.hiddenButton,,
```

---

##### `INPUT` ✅
Types text into an input field.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Type text | `INPUT` | Types Key value (appends to existing) |
| Clear then type | `INPUT[clear]` | Clears field first, then types |
| Send keyboard key | `INPUT[key]` 📋 | Key = key name (Enter, Tab, Escape, ArrowDown) |
| File upload | `INPUT[file]` 📋 | Key = file path for file input elements |

```csv
INPUT,LoginPage.username,,john.doe@example.com
INPUT[clear],SearchPage.box,,new search term
INPUT[key],SearchPage.box,,Enter
```

---

##### `CLEAR` ✅
Clears the content of an input or textarea.

```csv
CLEAR,FormPage.phoneNumber,,
```

---

##### `SELECT` ✅
Selects an option from a `<select>` dropdown.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| By value (default) | `SELECT` | Selects by `value` attribute |
| By visible text | `SELECT[text]` | Selects by visible label |
| By index | `SELECT[index]` | Selects by 0-based index |
| Deselect all | `SELECT[deselect-all]` 📋 | For multi-select — clears all |

```csv
SELECT,CheckoutPage.country,,US
SELECT[text],CheckoutPage.country,,United States
SELECT[index],MonthPicker.month,,2
```

---

##### `HOVER` ✅
Moves the mouse over an element (triggers tooltips, hover menus).

```csv
HOVER,NavPage.productsMenu,,
```

---

##### `DRAG_DROP` ✅
Drags from source element and drops onto target element.

- Recipient = source locator key
- Key = target locator key

```csv
DRAG_DROP,KanbanPage.todoCard,,KanbanPage.doneColumn
```

---

##### `SCROLL` ✅
Scrolls the page or to an element.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| To element | `SCROLL` | Scrolls element into viewport center |
| Page top | `SCROLL[top]` | Scrolls to very top of page |
| Page bottom | `SCROLL[bottom]` | Scrolls to very bottom of page |
| By pixels | `SCROLL[by=0,500]` 📋 | Scrolls by x,y pixel offset |

```csv
SCROLL,TablePage.lastRow,,
SCROLL[bottom],,,
SCROLL[top],,,
```

---

##### `WAIT` ✅
Explicit wait until a condition is met. Recipient = locator key.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Visible (default) | `WAIT[condition=visible]` | Wait until element is displayed |
| Present in DOM | `WAIT[condition=presence]` | Wait until element exists (may be hidden) |
| Clickable | `WAIT[condition=clickable]` | Wait until element can be clicked |
| Invisible | `WAIT[condition=invisible]` | Wait until element disappears |
| Text present | `WAIT[condition=text-present]` | Wait until element contains Key text |
| Custom timeout | `WAIT[condition=visible, timeout=60]` | Override default wait timeout |

```csv
WAIT,DashboardPage.spinner,,
WAIT[condition=invisible],DashboardPage.spinner,,
WAIT[condition=text-present, timeout=30],StatusPage.label,,Ready
WAIT[condition=clickable],FormPage.submitBtn,,
```

---

##### `ASSERT_TEXT` ✅
Asserts the visible text of an element.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Exact match | `ASSERT_TEXT` | Full text equality |
| Contains | `ASSERT_TEXT[contains]` | Substring match |
| Case-insensitive | `ASSERT_TEXT[ignore-case]` | Case-insensitive exact match |
| Contains + ignore-case | `ASSERT_TEXT[contains, ignore-case]` | Combined |
| Not contains 📋 | `ASSERT_TEXT[not-contains]` | Text must NOT contain value |
| Starts with 📋 | `ASSERT_TEXT[starts-with]` | Text starts with value |
| Ends with 📋 | `ASSERT_TEXT[ends-with]` | Text ends with value |
| Regex 📋 | `ASSERT_TEXT[regex]` | Key = pattern to match against |

```csv
ASSERT_TEXT,OrderPage.status,,Confirmed
ASSERT_TEXT[contains],ErrorPage.message,,required field
ASSERT_TEXT[ignore-case],ProfilePage.name,,john doe
ASSERT_TEXT[regex],InvoicePage.ref,,INV-\d{8}
```

---

##### `ASSERT_VISIBLE` ✅
Asserts whether an element is visible or hidden.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Must be visible | `ASSERT_VISIBLE` | Element is present and displayed |
| Must be hidden | `ASSERT_VISIBLE[hidden]` | Element is absent or not displayed |
| Must be enabled 📋 | `ASSERT_VISIBLE[enabled]` | Element is interactable (not disabled) |
| Must be disabled 📋 | `ASSERT_VISIBLE[disabled]` | Element has disabled attribute |
| Must be selected 📋 | `ASSERT_VISIBLE[selected]` | Checkbox or radio is checked |

```csv
ASSERT_VISIBLE,SuccessPage.banner,,
ASSERT_VISIBLE[hidden],ErrorPage.banner,,
ASSERT_VISIBLE[enabled],FormPage.submitBtn,,
ASSERT_VISIBLE[selected],PrefsPage.darkModeChk,,
```

---

##### `ASSERT_COUNT` ✅
Asserts the total number of elements matching a locator.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Exact count | `ASSERT_COUNT` | Count must equal Key value |
| Greater than 📋 | `ASSERT_COUNT[op=gt]` | Count must be greater than Key |
| At least 📋 | `ASSERT_COUNT[op=gte]` | Count must be ≥ Key |

```csv
ASSERT_COUNT,CartPage.lineItems,,3
ASSERT_COUNT[op=gte],ResultPage.rows,,1
```

---

##### `ASSERT_ATTRIBUTE` ✅
Asserts the value of an HTML attribute on an element.

- Recipient = locator key
- Source = attribute name (`class`, `href`, `value`, `aria-label`, etc.)
- Key = expected attribute value

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Exact match | `ASSERT_ATTRIBUTE` | Attribute value must equal Key |
| Contains | `ASSERT_ATTRIBUTE[contains]` | Attribute value contains Key |

```csv
ASSERT_ATTRIBUTE,NavPage.homeLink,href,/home
ASSERT_ATTRIBUTE[contains],NavPage.activeTab,class,active
```

---

##### `SWITCH_FRAME` ✅
Switches WebDriver focus to an iframe or back to the main document.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Into iframe | `SWITCH_FRAME` | Recipient = locator key of the iframe element |
| Back to main | `SWITCH_FRAME` | Recipient = `DEFAULT` |
| To parent 📋 | `SWITCH_FRAME[parent]` | Switch to parent frame (from nested iframe) |

```csv
SWITCH_FRAME,PaymentPage.cardIframe,,
SWITCH_FRAME,DEFAULT,,
```

---

##### `SWITCH_WINDOW` ✅
Switches browser focus to another tab or window.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Latest window | `SWITCH_WINDOW` | Key = `new` — switches to most recently opened tab |
| By title | `SWITCH_WINDOW` | Key = page title to match |
| Close current first | `SWITCH_WINDOW[close-current]` | Close current tab before switching |
| Maximize 📋 | `SWITCH_WINDOW[maximize]` | Maximize the switched-to window |

```csv
SWITCH_WINDOW,,,new
SWITCH_WINDOW,,,Payment Confirmation
SWITCH_WINDOW[close-current],,,new
```

---

##### `JS_CLICK` ✅
Fires a JavaScript `.click()` on the element — bypasses overlay interception.

```csv
JS_CLICK,OverlayPage.confirmBtn,,
```

---

##### `JS_EXECUTE` ✅
Runs an arbitrary JavaScript snippet. Optionally stores the return value.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Run script | `JS_EXECUTE` | Key = JS code |
| Store result | `JS_EXECUTE[store-as=VAR]` | Saves JS return value to DataStore |
| Scoped store | `JS_EXECUTE[store-as=VAR, scope=GLOBAL]` | Save to explicit scope |

```csv
JS_EXECUTE,,,window.scrollTo(0, 0)
JS_EXECUTE[store-as=pageTitle],,,return document.title;
JS_EXECUTE[store-as=badge, scope=UI],,,return document.querySelector('#notif-count').textContent;
```

---

##### `STORE_DATA` (UI) ✅ — partially implemented as `GET_TEXT`
Reads element visible text and stores it with optional transformations.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Raw text | `STORE_DATA` | Stores as-is |
| Trimmed | `STORE_DATA[trim]` | Strips whitespace |
| Uppercase | `STORE_DATA[upper]` | Converts to uppercase |
| Lowercase | `STORE_DATA[lower]` | Converts to lowercase |
| Substring | `STORE_DATA[substring=0,8]` | Extracts chars at positions 0–8 |
| Remove special chars | `STORE_DATA[remove-special]` | Keeps letters, digits, spaces only |
| Regex capture | `STORE_DATA[regex=ORD-\\d+]` | Extracts first regex match |
| GLOBAL scope | `STORE_DATA[scope=GLOBAL]` | Saves to GLOBAL instead of UI |
| Combined | `STORE_DATA[trim, upper, scope=GLOBAL]` | Chain multiple transforms |

```csv
STORE_DATA,OrderPage.orderId,,rawOrderId
STORE_DATA[trim, upper],ProfilePage.name,,upperName
STORE_DATA[regex=\\d{10}],InvoicePage.refNo,,invoiceNumber
STORE_DATA[substring=0,8, scope=GLOBAL],HomePage.version,,appVersion
```

---

#### 🟡 API Actions — REST layer (`hag-api`) 📋

---

##### `SEND_REQUEST`
Executes a REST API call using a JSON template and test data.

- Recipient = template file path
- Source = test data file path
- Key = data block name

| Sub-case | CSV Syntax | Description |
|---|---|---|
| POST (default) | `SEND_REQUEST` | HTTP POST |
| GET | `SEND_REQUEST[method=GET]` | HTTP GET |
| PUT | `SEND_REQUEST[method=PUT]` | HTTP PUT |
| DELETE | `SEND_REQUEST[method=DELETE]` | HTTP DELETE |
| PATCH | `SEND_REQUEST[method=PATCH]` | HTTP PATCH |
| With auth header | `SEND_REQUEST[auth=bearer]` | Injects `Authorization: Bearer ${token}` |
| No-body GET | `SEND_REQUEST[method=GET, no-body]` | Skips template body (params only) |

```csv
SEND_REQUEST[method=POST],templates/auth/login.json,testdata/auth/creds.json,validUser
SEND_REQUEST[method=GET, auth=bearer],templates/user/getProfile.json,,
SEND_REQUEST[method=DELETE, auth=bearer],templates/user/delete.json,testdata/user/ids.json,testUser
```

---

##### `SEND_SOAP_REQUEST`
Executes a SOAP call using an XML template.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Default | `SEND_SOAP_REQUEST` | HTTP POST with `text/xml` content-type |
| Custom SOAPAction | `SEND_SOAP_REQUEST[action=urn:getBalance]` | Overrides SOAPAction header |

```csv
SEND_SOAP_REQUEST,templates/payment/balance.xml,testdata/payment/accounts.json,premiumAccount
```

---

##### `ASSERT_STATUS`
Asserts the HTTP status code of the last API response.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Exact code | `ASSERT_STATUS` | Key = expected code (200, 201, 404…) |
| Range | `ASSERT_STATUS[range=200,299]` | Key blank; range in flag |

```csv
ASSERT_STATUS,,,200
ASSERT_STATUS,,,201
ASSERT_STATUS[range=400,499],,,
```

---

##### `ASSERT_JSON_PATH`
Asserts a field value in the last REST JSON response using JSONPath.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Exact match | `ASSERT_JSON_PATH` | Recipient = JSON path, Key = expected |
| Contains | `ASSERT_JSON_PATH[contains]` | Substring match on extracted value |
| Not null | `ASSERT_JSON_PATH[not-null]` | Value at path must exist and be non-null |

```csv
ASSERT_JSON_PATH,data.user.email,,john@example.com
ASSERT_JSON_PATH[contains],data.message,,success
ASSERT_JSON_PATH[not-null],data.token,,
```

---

##### `ASSERT_XPATH` *(SOAP)*
Asserts a field in the last SOAP XML response using XPath.

```csv
ASSERT_XPATH,//ns:balance/text(),,500.00
```

---

##### `STORE_DATA` (API)
Extracts and stores a value from the last API response.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| JSON path | `STORE_DATA[source=api]` | Recipient = JSONPath, Key = var name |
| Response header | `STORE_DATA[source=api, from=header]` | Recipient = header name |
| Status code | `STORE_DATA[source=api, from=status]` | Stores HTTP status code |

```csv
STORE_DATA[source=api],data.user.id,,userId
STORE_DATA[source=api, from=header],Authorization,,authToken
STORE_DATA[source=api, from=status],,,lastStatusCode
```

---

#### 🟤 DB Actions — Database layer (`hag-db`) 📋

---

##### `DB_QUERY`
Executes a SQL SELECT statement from a `.sql` file.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Default | `DB_QUERY` | Recipient = path to `.sql` file |
| Expect single row | `DB_QUERY[single]` | Fails if more than 1 row returned |
| Parameterised | `DB_QUERY` | Source + Key = data block with SQL params |

```csv
DB_QUERY,scripts/user/findByEmail.sql,testdata/user/data.json,testUser
DB_QUERY[single],scripts/order/getLatest.sql,,
```

---

##### `DB_EXECUTE`
Executes a non-SELECT SQL statement (INSERT, UPDATE, DELETE).

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Default | `DB_EXECUTE` | Recipient = path to `.sql` file |
| Assert affected rows | `DB_EXECUTE[expect-rows=1]` | Fails if affected row count differs |

```csv
DB_EXECUTE,scripts/cleanup/deleteTestUser.sql,,
DB_EXECUTE[expect-rows=1],scripts/order/updateStatus.sql,testdata/order/data.json,pending
```

---

##### `ASSERT_ROW_COUNT`
Asserts the number of rows returned by the last `DB_QUERY`.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Exact | `ASSERT_ROW_COUNT` | Key = expected row count |
| At least | `ASSERT_ROW_COUNT[op=gte]` | Count must be ≥ Key |
| Zero rows | `ASSERT_ROW_COUNT` | Key = `0` (record must not exist) |

```csv
ASSERT_ROW_COUNT,,,1
ASSERT_ROW_COUNT,,,0
ASSERT_ROW_COUNT[op=gte],,,5
```

---

##### `STORE_DATA` (DB)
Stores a value from the last DB query result.

| Sub-case | CSV Syntax | Description |
|---|---|---|
| Column value | `STORE_DATA[source=db]` | Recipient = column name, Key = var name |
| Row count | `STORE_DATA[source=db, from=count]` | Stores row count as a variable |

```csv
STORE_DATA[source=db],email,,dbUserEmail
STORE_DATA[source=db, from=count],,,totalRecords
```

---

### 2.11 Data Store & Variable System

The `DataStore` is an in-memory store scoped per execution. Variables stored in one step can be referenced in later steps using:

- `${VARIABLE_NAME}` — looks up from `GLOBAL` scope
- `${SCOPE:VARIABLE_NAME}` — explicitly scoped (e.g. `${UI:welcomeText}`)

**Scopes:**

| Scope | Lifetime | Use Case |
|---|---|---|
| `GLOBAL` | Entire test session | Suite-level data, environment URLs |
| `UI` | Test execution | Data captured from page elements |
| `API` | Test execution | Data extracted from API responses |
| `DB` | Test execution | Data from database queries |

**Example — storing and reusing a value:**
```csv
STORE_DATA,LoginPage.userId,,savedUserId
SEND_REQUEST,templates/user/getProfile.json,testdata/user/data.json,session
ASSERT_JSON_PATH,response.id,,${UI:savedUserId}
```

---

### 2.12 Cross-Channel Test Flow Example

```csv
# Step 1: Create account via API
SEND_REQUEST,templates/user/create.json,testdata/user/newUser.json,testAccount
ASSERT_STATUS,,,201
STORE_DATA,response.userId,,createdUserId

# Step 2: Verify in UI
NAVIGATE,,,https://admin.myapp.com/users
INPUT,AdminPage.searchBox,,${GLOBAL:createdUserId}
CLICK,AdminPage.searchBtn,,
ASSERT_TEXT,AdminPage.resultName,,Test Account User

# Step 3: Verify in DB
DB_QUERY,scripts/verifyUser.sql,,
ASSERT_ROW_COUNT,,,1
STORE_DATA,username,,dbUsername
COMPARE_VALUES,${GLOBAL:createdUserId},,${DB:dbUsername}

FINALLY
# Cleanup: delete via API
SEND_REQUEST,templates/user/delete.json,testdata/user/newUser.json,testAccount
```

---

### 2.13 Configuration (`FrameworkConfig`)

| Property | Default | Description |
|---|---|---|
| `baseUrl` | (none) | Base URL prepended to relative `NAVIGATE` paths |
| `defaultWaitTimeoutSeconds` | 30 | Default explicit wait timeout |
| `defaultRetryAttempts` | 1 | Retry count for any action |
| `screenshotDirectory` | `screenshots/` | Where failure screenshots are saved |
| *(planned)* `browser` | `chrome` | Browser type for Selenium |
| *(planned)* `gridUrl` | (none) | Selenium Grid hub URL |
| *(planned)* `dbDriver` | (none) | JDBC driver class |
| *(planned)* `dbUrl` | (none) | JDBC connection string |

---

### 2.14 Implementation Roadmap

| Priority | Item | Module | Estimated Complexity |
|---|---|---|---|
| P0 | REST & SOAP API adapter (`hag-api`) | `hag-api` | High |
| P0 | DB adapter (`hag-db`) | `hag-db` | Medium |
| P0 | Fix `validateConfiguration()` to be optional-adapter-aware | `hag-core` | Low |
| P0 | Replace `split(",")` with OpenCSV in `CsvTestParser` | `hag-core` | Low |
| P1 | `CHANGE_DATA_STORE` / `STORE_DATA` action name alignment | `hag-core` | Low |
| P1 | `STORE_DATA` extraction modifiers (trim, substring, regex) | `hag-core` + modules | Medium |
| P1 | `COMPARE_VALUES` extended operators (GT, LT, STARTS_WITH, REGEX) | `hag-core` | Low |
| P1 | HTML Report Engine | `hag-core` | Medium |
| P1 | `##TOKEN##` template merger for API | `hag-api` | Medium |
| P2 | `DATE_ASSERT` / date utility action | `hag-core` | Medium |
| P2 | Parallel execution + Selenium Grid config | `hag-runner` | High |
| P2 | TestNG base runner class + `testng.xml` | `hag-runner` | Medium |
| P2 | `DataScope` — add `TEST` and `STEP` scopes | `hag-core` | Low |
| P3 | Unit tests for all core components | All | Medium |
| P3 | Property file / env var config loading | `hag-core` | Low |

---

### 2.15 Non-Functional Requirements

| Requirement | Target |
|---|---|
| Parallel thread-safety | All adapters and DataStore must be thread-local per test |
| Performance | Test step overhead (excluding action time) < 5ms |
| Extensibility | New action = one class + one `register()` call; no framework edits |
| Portability | Tests must be environment-portable via `baseUrl` / config overrides |
| Maintainability | Locator changes require only JSON edits, zero code changes |
| Traceability | Every step produces a log entry with step index, action, duration, status |

---

*Document prepared as part of H-A-G v1.0 specification. Contact the Test Architecture team for clarifications.*
