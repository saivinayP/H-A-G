# H-A-G User Guide
## How to Write and Run Tests with Hybrid Automation Grid
> Version 1.0 | For QA Engineers, Business Analysts & New Team Members

---

## Table of Contents

1. [What is H-A-G?](#1-what-is-h-a-g)
2. [Prerequisites](#2-prerequisites)
3. [Project Folder Structure](#3-project-folder-structure)
4. [Step 1 — Set Up Configuration Files](#4-step-1--set-up-configuration-files)
5. [Step 2 — Create Locator Files (UI only)](#5-step-2--create-locator-files-ui-only)
6. [Step 3 — Create Test Data Files](#6-step-3--create-test-data-files)
7. [Step 4 — Write Your Test CSV](#7-step-4--write-your-test-csv)
8. [Step 5 — Understanding Action Syntax](#8-step-5--understanding-action-syntax)
9. [Step 6 — Providing Test Data to Actions](#9-step-6--providing-test-data-to-actions)
10. [Step 7 — Using Variables Between Steps](#10-step-7--using-variables-between-steps)
11. [Step 8 — Reusing Tests with INCLUDE](#11-step-8--reusing-tests-with-include)
12. [Step 9 — Cleanup with FINALLY](#12-step-9--cleanup-with-finally)
13. [Step 10 — Create API Request Templates](#13-step-10--create-api-request-templates)
14. [Step 11 — Run Your Tests](#14-step-11--run-your-tests)
15. [Complete Worked Example](#15-complete-worked-example)
16. [Full Action Reference](#16-full-action-reference)
17. [Quick Reference Card](#17-quick-reference-card)

---

## 1. What is H-A-G?

H-A-G (Hybrid Automation Grid) lets you write automated tests in plain **CSV files** — no programming required. You describe your test steps in a spreadsheet-like format and H-A-G handles all the underlying technology: opening browsers, calling APIs, and querying databases.

**A test step looks like this:**

```
CLICK,LoginPage.loginButton,,
```

That's it. Four columns separated by commas. H-A-G reads it and clicks the login button.

---

## 2. Prerequisites

| Requirement | Version | Notes |
|---|---|---|
| Java | 17 or later | `java -version` to check |
| Maven | 3.8 or later | `mvn -version` to check |
| Chrome/Firefox | Latest | For UI tests |
| H-A-G project | Cloned from repo | `git clone <repo-url>` |

---

## 3. Project Folder Structure and Modules

H-A-G is designed with a modular architecture so components are loosely coupled. Here is the relevance of each module:

- **`hag-core`**: The brain of the framework. Contains the CSV parser, action dispatcher, shared `DataStore` for variables, configuration loaders, and the event-driven reporting engine (including custom HTML reports).
- **`hag-ui`**: The browser automation module. Acts as a wrapper around Selenium WebDriver, providing 23 built-in UI actions (click, input, select, wait, drag-drop, assert).
- **`hag-api`**: The REST automation module. Powered by RestAssured, it parses JSON templates to construct and execute API requests and assert responses.
- **`hag-db`**: The database automation module. Uses pure JDBC to connect to databases (MySQL, H2, etc.), execute SQL scripts, query data, and assert row counts or column values.
- **`hag-runner`**: The execution orchestrator. Boots up the TestNG suite, initializes configurations (`url.config.yml`, `runner.config.yml`), and runs the `BulkTestRunner` to dynamically execute all CSV test scenarios.

### Folder Structure
After cloning, your project should look like this. You will be creating files in the areas marked `← YOU CREATE THIS`.

```
H-A-G/
│
├── url.config.yml          ← YOU CREATE THIS (environment URLs)
├── runner.config.yml       ← YOU CREATE THIS (test execution settings)
├── testdata.config.yml     ← YOU CREATE THIS (file paths & DB/API config)
│
├── tests/                  ← YOU CREATE THIS (your test CSV files)
│   ├── common/
│   │   └── login.csv
│   └── checkout/
│       └── purchase_flow.csv
│
├── src/main/resources/
│   ├── locators/           ← YOU CREATE THIS (JSON page object files)
│   │   ├── LoginPage.json
│   │   └── CheckoutPage.json
│   │
│   ├── testdata/           ← YOU CREATE THIS (JSON test data files)
│   │   ├── login/
│   │   │   └── users.json
│   │   └── checkout/
│   │       └── products.json
│   │
│   └── templates/          ← YOU CREATE THIS (API request templates)
│       └── auth/
│           └── login.json
│
└── src/main/resources/
    └── scripts/            ← YOU CREATE THIS (SQL script files)
        └── user/
            └── findUser.sql
```

---

## 4. Step 1 — Set Up Configuration Files

Create these three files in the project root before writing any tests.

### `url.config.yml`

Maps friendly names to environment URLs. You switch environments by changing `active-environment` or running with `-Denv=staging`.

```yaml
active-environment: dev

environments:
  dev:
    application:  https://dev.myapp.com
    api-base:     https://api-dev.myapp.com

  staging:
    application:  https://staging.myapp.com
    api-base:     https://api-staging.myapp.com

  production:
    application:  https://www.myapp.com
    api-base:     https://api.myapp.com
```

### `runner.config.yml`

Controls which tests run, what browser to use, parallelism, and output locations.

```yaml
test-suite:
  - tests/login/
  - tests/checkout/purchase_flow.csv
  - "!tests/wip/"               # exclude folder

browser:
  type: chrome                   # chrome | firefox | edge
  headless: false
  window: maximize

execution:
  parallel-threads: 1            # increase for parallel runs
  retry-attempts: 1
  stop-on-failure: false

timeouts:
  default-wait-seconds: 30

output:
  report-dir:     target/reports/
  screenshot-dir: target/screenshots/

formats:
  date:     dd/MM/yyyy
  timezone: IST
```

### `testdata.config.yml`

Tells H-A-G where your locators, data files, templates, and SQL scripts live.

```yaml
paths:
  locators:       src/main/resources/locators/
  test-data:      src/main/resources/testdata/
  api-templates:  src/main/resources/templates/
  sql-scripts:    src/main/resources/scripts/

database:
  driver:   com.mysql.cj.jdbc.Driver
  url:      jdbc:mysql://localhost:3306/testdb
  username: testuser
  password: ${env.DB_PASSWORD}

api:
  base-url:           ${URL:api-base}
  default-timeout-ms: 10000
```

---

## 5. Step 2 — Create Locator Files (UI only)

Locator files tell H-A-G how to find elements on each page.

- One file per page
- Stored in `src/main/resources/locators/`
- Named `<PageName>.json` — the name becomes the prefix in your CSV

### Format

```json
{
  "element-key": {
    "type": "id | css | xpath | name | classname | tag | linktext",
    "value": "your selector here"
  }
}
```

### Example: `LoginPage.json`

```json
{
  "username-field": {
    "type": "id",
    "value": "username"
  },
  "password-field": {
    "type": "css",
    "value": "input[type='password']"
  },
  "login-button": {
    "type": "xpath",
    "value": "//button[text()='Login']"
  },
  "error-message": {
    "type": "css",
    "value": ".alert-danger"
  }
}
```

### How to reference a locator in CSV

```
Page name (no .json) + dot + element key
```

```csv
CLICK,LoginPage.login-button,,
INPUT,LoginPage.username-field,,john@example.com
ASSERT_TEXT,LoginPage.error-message,,Invalid credentials
```

---

## 6. Step 3 — Create Test Data Files

Test data files let you store your test inputs in one place and reuse them across many test steps.

- Stored in `src/main/resources/testdata/<folder>/<file>.json`
- Organised in **named blocks** (one block per test scenario/user/dataset)
- Reference a block by its name in the CSV `Key` column

### Format

```json
{
  "blockName": {
    "field1": "value1",
    "field2": "value2"
  },
  "anotherBlock": {
    "field1": "otherValue"
  }
}
```

### Example: `testdata/login/users.json`

```json
{
  "validUser": {
    "username": "john.doe@company.com",
    "password": "Secret@123",
    "expectedWelcome": "Welcome, John"
  },
  "lockedUser": {
    "username": "locked@company.com",
    "password": "Pass@123",
    "expectedError": "Your account has been locked"
  },
  "adminUser": {
    "username": "admin@company.com",
    "password": "Admin@456"
  }
}
```

### Example: `testdata/checkout/products.json`

```json
{
  "basicProduct": {
    "name": "Laptop Stand",
    "qty": "2",
    "expectedTotal": "49.98"
  }
}
```

---

## 7. Step 4 — Write Your Test CSV

Every test is a CSV file. Open any spreadsheet tool or plain text editor and create a file with exactly **four columns**:

```
Action, Recipient, Source, Key
```

> **Rule:** Always include the header row on line 1. Lines starting with `#` are comments.

### Understanding the Four Columns

| Column | What goes here |
|---|---|
| **Action** | What to do: `CLICK`, `INPUT`, `ASSERT_TEXT`, `NAVIGATE`, etc. Add `:SUBCASE` for variants |
| **Recipient** | *UI:* locator key (`Page.element`) *API:* template file path *DB:* SQL file path |
| **Source** | *Data-driven steps:* path to test data JSON file *Modifier steps:* pipe-separated flags like `trim\|upper` |
| **Key** | *Data-driven steps:* data block name in the JSON *Assertion steps:* expected value *Store steps:* variable name to save into |

### Minimal Example

```csv
Action,Recipient,Source,Key
NAVIGATE,,,https://dev.myapp.com/login
CLICK,LoginPage.login-button,,
ASSERT_VISIBLE,LoginPage.dashboard,,
```

---

## 8. Step 5 — Understanding Action Syntax

Actions use a **colon sub-case** format. No commas or brackets in the Action column.

```
ACTION            ← default behaviour
ACTION:SUBCASE    ← specific variant
```

### Examples

| You write | What happens |
|---|---|
| `CLICK` | Standard left click |
| `CLICK:DOUBLE` | Double click |
| `CLICK:RIGHT` | Right click / context menu |
| `SELECT` | Pick dropdown option by value attribute |
| `SELECT:TEXT` | Pick dropdown option by its visible label |
| `SELECT:INDEX` | Pick dropdown option by position (0-based) |
| `WAIT:VISIBLE` | Wait until element appears |
| `WAIT:INVISIBLE` | Wait until element disappears |
| `ASSERT_TEXT` | Check exact element text |
| `ASSERT_TEXT:CONTAINS` | Check element text contains a substring |
| `NAVIGATE:BACK` | Click browser Back button |
| `COMPARE:GT` | Compare two numbers, assert actual > expected |
| `STORE_DATA:RESPONSE` | Store value from last API response |
| `DB_QUERY:INLINE` | Run SQL written directly in the Key column |

### Adding Modifiers via Source Column

When you need to tweak how an action works (without a separate sub-case), put the modifier in the **Source** column. Use **pipe `|`** to separate multiple modifiers — never a comma.

```csv
# Store the text trimmed and uppercased
STORE_DATA,ProfilePage.name,trim|upper,savedName

# Wait with a longer timeout
WAIT:TEXT,StatusPage.label,timeout=60,Ready

# Assert case-insensitively
ASSERT_TEXT:CONTAINS,ErrorPage.msg,ignore-case,required field
```

---

## 9. Step 6 — Providing Test Data to Actions

### Option A — Literal value (simple cases)

Put the value directly in the `Key` column. Leave `Source` empty.

```csv
INPUT,LoginPage.username-field,,john@example.com
SELECT,CheckoutPage.country,,US
ASSERT_TEXT,OrderPage.status,,Confirmed
```

### Option B — From a test data file (data-driven, recommended)

Put the **file path** in `Source` and the **data block name** in `Key`. H-A-G loads the JSON, finds the block, and injects all its fields as variables for that step.

```csv
# Source = path from testdata root; Key = block name inside the file
INPUT,LoginPage.username-field,login/users.json,validUser
INPUT,LoginPage.password-field,login/users.json,validUser
```

When H-A-G processes this step, it:
1. Loads `testdata/login/users.json`
2. Reads the `validUser` block
3. Makes all fields available as `${username}`, `${password}`, etc.
4. The `INPUT` action uses `${username}` as the text to type

> **Why use test data files?** You only need to change the data in one place if credentials or product details change. You can also swap which block to use (`validUser` → `lockedUser`) to test a different scenario without touching the test steps.

### Option B Example with SELECT

```csv
# Select the country from test data
SELECT:TEXT,CheckoutPage.country-dropdown,checkout/shippingDetails.json,ukAddress

# shippingDetails.json:
# { "ukAddress": { "country": "United Kingdom", "city": "London" } }
```

---

## 10. Step 7 — Using Variables Between Steps

H-A-G has a built-in **DataStore** that lets steps share information.

### Storing a value

```csv
# Read text from a page element and store it as 'capturedOrderId'
STORE_DATA,OrderPage.order-number,,capturedOrderId
```

### Using a stored value later

```csv
# Reference it with ${variableName}
ASSERT_TEXT,ConfirmPage.ref-number,,${capturedOrderId}
CHANGE_DATA_STORE,orderId,,${capturedOrderId}
```

### Scope prefixes

When data comes from different layers, use explicit scopes to avoid confusion:

| Prefix | Where it's stored | Example |
|---|---|---|
| *(none)* | GLOBAL | `${authToken}` |
| `GLOBAL:` | GLOBAL explicitly | `${GLOBAL:authToken}` |
| `UI:` | Browser layer | `${UI:capturedText}` |
| `API:` | API layer | `${API:userId}` |
| `DB:` | Database layer | `${DB:emailFromDb}` |

### Example — Passing data between layers

```csv
# API creates a user and stores its ID
SEND_REQUEST,templates/user/create.json,testdata/users.json,newUser
STORE_DATA:RESPONSE,data.id,,newUserId

# UI search uses that stored ID
INPUT,AdminPage.searchBox,clear,${API:newUserId}
CLICK,AdminPage.searchBtn,,

# DB confirms the record
DB_QUERY:INLINE,,,SELECT * FROM users WHERE id = '${API:newUserId}'
ASSERT_ROW_COUNT,,,1
```

---

## 11. Step 8 — Reusing Tests with INCLUDE

The `INCLUDE` directive runs another CSV file as a sub-flow inline. Use this to share common sequences (like login) across many tests without duplicating steps.

### Create a reusable login CSV (`tests/common/login.csv`)

```csv
Action,Recipient,Source,Key
NAVIGATE,,,${URL:application}/login
WAIT:VISIBLE,LoginPage.username-field,,
INPUT,LoginPage.username-field,login/users.json,defaultTestUser
INPUT,LoginPage.password-field,login/users.json,defaultTestUser
CLICK,LoginPage.login-button,,
WAIT:VISIBLE,HomePage.dashboard,,
```

### Use it in any other test

```csv
Action,Recipient,Source,Key
# Login first (shared)
INCLUDE,tests/common/login.csv,,

# Then continue with the actual test
NAVIGATE,,,${URL:application}/my-orders
ASSERT_VISIBLE,OrdersPage.orderList,,
```

---

## 12. Step 9 — Cleanup with FINALLY

Steps placed after a `FINALLY` marker **always run**, regardless of whether earlier steps passed or failed. Use this for cleanup tasks like logout, deleting test data, or closing connections.

```csv
Action,Recipient,Source,Key
INCLUDE,tests/common/login.csv,,
NAVIGATE,,,${URL:application}/create-order
CLICK,OrderPage.placeOrderBtn,,
ASSERT_TEXT,OrderPage.confirmation,,Order placed successfully
STORE_DATA,OrderPage.orderId,,createdOrderId

FINALLY
# These always run even if the test above fails
NAVIGATE,,,${URL:application}/admin/orders/${UI:createdOrderId}
CLICK,AdminPage.deleteOrderBtn,,
CLICK,AdminPage.confirmDeleteBtn,,
NAVIGATE,,,${URL:application}/logout
```

---

## 13. Step 10 — Create API Request Templates

API templates are JSON (REST) or XML (SOAP) files that define the request body. The HTTP method and endpoint are defined inside the template.

### REST template format (`templates/auth/login.json`)

```json
{
  "_method":   "POST",
  "_endpoint": "/api/v1/auth/login",
  "username":  "${username}",
  "password":  "${password}"
}
```

The `${username}` and `${password}` tokens are filled in from the test data block you specify in the CSV.

### Using API templates in CSV

```csv
# Send POST login request, use credentials from 'validUser' block
SEND_REQUEST,templates/auth/login.json,testdata/login/users.json,validUser
ASSERT_STATUS,,,200
ASSERT_RESPONSE:NOT_NULL,data.token,,
STORE_DATA:RESPONSE,data.token,,authToken
```

### SOAP template format (`templates/payment/getBalance.xml`)

```xml
<!-- _method: POST -->
<!-- _endpoint: /payment/soap/service -->
<!-- _soap-action: urn:GetAccountBalance -->
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
  <soapenv:Body>
    <GetAccountBalance>
      <accountId>${accountId}</accountId>
      <currency>${currency}</currency>
    </GetAccountBalance>
  </soapenv:Body>
</soapenv:Envelope>
```

---

## 14. Step 11 — Run Your Tests

### From the command line

```bash
# Run all tests defined in runner.config.yml
mvn clean test -pl hag-runner -am

# Override the environment
mvn clean test -pl hag-runner -am -Denv=staging

# Run a single specific test file (bypassing the folder scan)
mvn clean test -pl hag-runner -am "-Dhag.test.root=hag-resource/tests/demo-ui/internet_login.csv"
```

### Understanding results

- **Console** — step-by-step pass/fail log appears during the run
- **HTML Report** — opens `target/reports/index.html` after the run
- **Screenshots** — saved to `target/screenshots/` for any failed step

### Exit codes

| Code | Meaning |
|---|---|
| 0 | All tests passed |
| 1 | One or more tests failed |
| 2 | Framework/configuration error |

---

## 15. Complete Worked Example

This example tests a full e-commerce checkout flow across UI, API, and DB layers.

### Folder setup

```
tests/checkout/
└── place_order_test.csv

testdata/
└── checkout/
    └── orderData.json

locators/
├── LoginPage.json
├── ProductPage.json
├── CartPage.json
└── CheckoutPage.json

templates/
└── orders/
    └── create_order.json

scripts/
└── orders/
    └── verify_order.sql
```

### `testdata/checkout/orderData.json`

```json
{
  "premiumUser": {
    "username": "premium@test.com",
    "password": "Test@123"
  },
  "laptopOrder": {
    "productSku": "LAPTOP-PRO-001",
    "quantity": "1",
    "shippingCountry": "India",
    "expectedTotal": "₹89,999"
  }
}
```

### `tests/checkout/place_order_test.csv`

```csv
Action,Recipient,Source,Key

# ── Setup: Login ─────────────────────────────────────────────────────
INCLUDE,tests/common/login.csv,,

# ── Search and add product ────────────────────────────────────────────
NAVIGATE,,,${URL:application}/products
INPUT,ProductPage.search-box,clear,checkout/orderData.json,laptopOrder
CLICK,ProductPage.search-btn,,
WAIT:VISIBLE,ProductPage.result-card,,
CLICK,ProductPage.add-to-cart-btn,,
ASSERT_TEXT:CONTAINS,ProductPage.cart-badge,,1

# ── Checkout ──────────────────────────────────────────────────────────
NAVIGATE,,,${URL:application}/cart
SELECT:TEXT,CartPage.quantity-dropdown,checkout/orderData.json,laptopOrder
CLICK,CartPage.checkout-btn,,
WAIT:VISIBLE,CheckoutPage.order-summary,,
ASSERT_TEXT,CheckoutPage.total-amount,checkout/orderData.json,laptopOrder
CLICK,CheckoutPage.place-order-btn,,
WAIT:VISIBLE,CheckoutPage.confirmation-number,,
STORE_DATA,CheckoutPage.confirmation-number,,orderConfirmation
LOG,,,Order placed. Ref: ${UI:orderConfirmation}

# ── Verify via API ────────────────────────────────────────────────────
SEND_REQUEST,templates/orders/getOrder.json,,
ASSERT_STATUS,,,200
ASSERT_RESPONSE,data.reference,,${UI:orderConfirmation}
ASSERT_RESPONSE,data.status,,PENDING

# ── Verify via DB ─────────────────────────────────────────────────────
DB_QUERY,scripts/orders/verify_order.sql,,
ASSERT_ROW_COUNT,,,1
ASSERT_COLUMN,status,,PENDING
STORE_DATA:DB,order_id,,dbOrderId
COMPARE:EQUALS,${UI:orderConfirmation},,${DB:dbOrderId}

FINALLY
# ── Always cleanup ────────────────────────────────────────────────────
DB_EXECUTE:INLINE,,,DELETE FROM orders WHERE reference = '${UI:orderConfirmation}'
NAVIGATE,,,${URL:application}/logout
```

---

## 16. Full Action Reference

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

## 17. Quick Reference Card

### CSV Column Summary

```
Action:Subcase   |  Recipient          |  Source               |  Key
─────────────────────────────────────────────────────────────────────────────
NAVIGATE         |                     |                       |  URL/path
CLICK            |  Page.element       |                       |
CLICK:DOUBLE     |  Page.element       |                       |
INPUT            |  Page.element       |  testdata/x.json (*)  |  text or block
INPUT            |  Page.element       |  clear                |  text to type
SELECT:TEXT      |  Page.element       |  testdata/x.json (*)  |  option or block
WAIT:VISIBLE     |  Page.element       |  timeout=N            |
WAIT:TEXT        |  Page.element       |  timeout=N            |  expected text
ASSERT_TEXT      |  Page.element       |                       |  expected text
ASSERT_VISIBLE   |  Page.element       |                       |
ASSERT_HIDDEN    |  Page.element       |                       |
STORE_DATA       |  Page.element       |  trim|upper (flags)   |  variable name
COMPARE:EQUALS   |  ${VAR} or value    |                       |  expected value
COMPARE:GT       |  ${VAR} or number   |                       |  threshold
CHANGE_DATA_STORE|  variable name      |  scope=GLOBAL         |  new value
INCLUDE          |  tests/path.csv     |                       |
FINALLY          |                     |                       |
SEND_REQUEST     |  templates/x.json   |  testdata/x.json      |  data block
ASSERT_STATUS    |                     |                       |  200/201/404
ASSERT_RESPONSE  |  json.path.expr     |                       |  expected value
STORE_DATA:RESPONSE | json.path.expr   |                       |  variable name
DB_QUERY         |  scripts/x.sql      |  testdata/x.json (*)  |  data block
DB_QUERY:INLINE  |                     |                       |  SQL string
ASSERT_ROW_COUNT |                     |                       |  expected count
STORE_DATA:DB    |  column name        |                       |  variable name

(*) Source + Key together = load test data file and use named block
```

### Variable Syntax

```
${varName}            look up in GLOBAL scope
${GLOBAL:varName}     same, explicit
${UI:varName}         from browser layer
${API:varName}        from API layer
${DB:varName}         from database layer
${URL:application}    URL from url.config.yml (active environment)
```

### Common Mistakes to Avoid

| ❌ Wrong | ✅ Right |
|---|---|
| `CLICK[double]` | `CLICK:DOUBLE` |
| Commas inside flags | Use pipe `trim,upper` → `trim\|upper` |
| `ASSERT_VISIBLE[hidden]` | `ASSERT_HIDDEN` |
| `SEND_REQUEST[method=GET]` | Define `"_method": "GET"` in template |
| Hardcoding URLs | Use `${URL:application}` from `url.config.yml` |
| Inline credentials | Use test data JSON blocks |

---

*H-A-G User Guide v1.0*
