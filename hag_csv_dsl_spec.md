# H-A-G CSV Script Format — Definitive Specification

> This is the standard going forward.

---

## The Four Columns

```
Action, Recipient, Source, Key
```

| Column | Role |
|---|---|
| `Action` | Keyword `[:subcase]` — optionally followed by `\|option=value` pairs |
| `Recipient` | First positional argument — adapts meaning per action type |
| `Source` | Data input: file reference and/or pipe-separated options |
| `Key` | Primary value — literal, expected, SQL, log message, variable name |

### Action Column Grammar

```
ACTION
ACTION:SUBCASE
ACTION|opt=val|opt=val
ACTION:SUBCASE|opt=val|opt=val
```

### Per-Step Options (Action Column Pipe Suffix)

| Option | Values | Meaning |
|---|---|---|
| `onfail` | `stop` / `continue` / `warn` / `retry:N` | Failure behaviour. Default: `stop` |
| `timeout` | integer (seconds) | Override step timeout |
| `retry` | integer | Override retry count |
| `masked` | `true` / `false` | Hide Key value in logs and report |
| `screenshot` | `true` / `false` | Force/suppress screenshot |
| `delay` | integer (ms) | Wait N ms before step executes |
| `driver` | `web` / `mobile` | Route to Selenium or Appium session |

---

## Variables — Flat Global Namespace

```
${variable_name}
```

One flat store per test. Set from anywhere, read from anywhere.

Variables come from:
- `CHANGE_DATA_STORE:SET` steps
- `STORE_DATA` (from API response, UI element, DB result)
- Data generators (resolved at interpolation time)

---

## CHANGE_DATA_STORE — Variable Management

Three subcases covering all variable operations:

| Subcase | Behaviour |
|---|---|
| `:SET` | Create or overwrite (always succeeds) |
| `:UPDATE` | Update only if already exists; fails if not set |
| `:DELETE` | Remove the variable |

```csv
Action,Recipient,Source,Key
CHANGE_DATA_STORE:SET,test_email,,${RANDOM_EMAIL}
CHANGE_DATA_STORE:SET,test_pass,,TestPass@123!
CHANGE_DATA_STORE:SET,product_id,,PROD-${RANDOM_NUMBER:4}
CHANGE_DATA_STORE:UPDATE,retry_count,,3
CHANGE_DATA_STORE:DELETE,temp_token,,
```

**Use `CHANGE_DATA_STORE:SET` at the top of every test under a `#!SECTION: Test Data Setup`.**
This makes test data visible, auditable, and directly traceable in the report.

---

## Data Generators

Resolved inline anywhere `${...}` is valid.

### Identity & Personal

| Token | Example | Notes |
|---|---|---|
| `${RANDOM_EMAIL}` | `k4f2a1b@hag-gen.io` | |
| `${RANDOM_EMAIL:domain.com}` | `k4f2a1b@domain.com` | Custom domain |
| `${RANDOM_FIRST_NAME}` | `James` | |
| `${RANDOM_LAST_NAME}` | `Henderson` | |
| `${RANDOM_FULL_NAME}` | `James Henderson` | |
| `${RANDOM_USERNAME}` | `jhen_4f2a` | URL-safe alphanumeric |
| `${RANDOM_PASSWORD}` | `Kp9!mQr2` | Upper+lower+digit+special |
| `${RANDOM_PASSWORD:16}` | 16-char password | Length param |
| `${RANDOM_PHONE}` | `+1-555-0172-4829` | E.164 |
| `${RANDOM_PHONE_IN}` | `+91-9876543210` | India format |
| `${RANDOM_DOB}` | `1990-07-14` | Age 18–65 |
| `${RANDOM_GENDER}` | `Male` / `Female` / `Non-binary` | |

### Geographic

| Token | Example |
|---|---|
| `${RANDOM_CITY}` | `Austin` |
| `${RANDOM_STATE}` | `Texas` |
| `${RANDOM_COUNTRY}` | `Germany` |
| `${RANDOM_COUNTRY_CODE}` | `DE` |
| `${RANDOM_ZIP}` | `78701` |
| `${RANDOM_ADDRESS}` | `142 Oak Street` |

### Identifiers

| Token | Example | Notes |
|---|---|---|
| `${RANDOM_UUID}` | `550e8400-e29b-41d4-...` | UUID v4 |
| `${RANDOM_ID:PREFIX-}` | `PREFIX-4f2a1b` | Custom prefix + random suffix |
| `${RANDOM_ALPHANUMERIC:12}` | `4fG2aB7kR9mZ` | Mixed case |
| `${RANDOM_ALPHA:8}` | `KrQmBpZf` | Letters only |
| `${RANDOM_NUMERIC:6}` | `847291` | Digits only |
| `${SEQ_ID}` | `1`, `2`, `3`... | Thread-safe auto-increment |

### Numbers

| Token | Example | Notes |
|---|---|---|
| `${RANDOM_NUMBER}` | `847291` | Default 6 digits |
| `${RANDOM_NUMBER:4}` | `8472` | N-digit |
| `${RANDOM_INTEGER:1:100}` | `73` | Range min:max |
| `${RANDOM_PRICE}` | `49.99` | Realistic price |
| `${RANDOM_PERCENTAGE}` | `73` | 0–100 |
| `${RANDOM_DECIMAL:2}` | `18.42` | N decimal places |

### Dates & Times

| Token | Example | Notes |
|---|---|---|
| `${TODAY}` | `2026-04-01` | ISO date |
| `${DATETIME}` | `2026-04-01T07:48:25` | ISO datetime |
| `${TIMESTAMP}` | `1743521305000` | Epoch ms |
| `${DATE_PLUS:7}` | `2026-04-08` | Today + N days |
| `${DATE_MINUS:30}` | `2026-03-02` | Today − N days |
| `${RANDOM_PAST_DATE}` | `2024-11-03` | Last 2 years |
| `${RANDOM_FUTURE_DATE}` | `2027-09-17` | Next 2 years |
| `${RANDOM_DATE:2020-01-01:2024-12-31}` | `2022-06-14` | Range |
| `${RANDOM_YEAR}` | `2019` | Last 10 years |

### Finance

| Token | Example |
|---|---|
| `${RANDOM_CREDIT_CARD}` | `4242 4242 4242 4242` (Stripe test) |
| `${RANDOM_IBAN}` | `GB82WEST12345698765432` |
| `${RANDOM_CURRENCY_CODE}` | `EUR` |
| `${RANDOM_AMOUNT}` | `127.50` |

### Network & Technical

| Token | Example |
|---|---|
| `${RANDOM_IP}` | `192.168.42.13` |
| `${RANDOM_IPV6}` | `2001:db8::8a2e:370:7334` |
| `${RANDOM_MAC}` | `00:1B:44:11:3A:B7` |
| `${RANDOM_URL}` | `https://test-4f2a.example.com` |
| `${RANDOM_DOMAIN}` | `test-4f2a.example.com` |

### Business & Text

| Token | Example |
|---|---|
| `${RANDOM_COMPANY}` | `Nexbridge Solutions` |
| `${RANDOM_JOB_TITLE}` | `Senior Engineer` |
| `${RANDOM_DEPARTMENT}` | `Marketing` |
| `${RANDOM_WORD}` | `serendipity` |
| `${RANDOM_SENTENCE}` | `The quick fox jumps over the lazy dog.` |

---

## The `#!` Directive System

```
#    plain comment — fully ignored
#!   machine-readable directive — metadata only
```

### Test-Level Metadata (before the header row)

```csv
#!NAME: Checkout Flow — Guest User
#!TAGS: regression, ui, checkout, smoke
#!PRIORITY: critical
#!AUTHOR: qa-core
#!TIMEOUT: 180
#!RETRY: 1
#!DESCRIPTION: Guest user adds product, enters delivery details, completes payment
#!ENVIRONMENT: staging, uat
#!SKIP: false
#!DRIVER: web
Action,Recipient,Source,Key
```

| Directive | Purpose |
|---|---|
| `#!NAME:` | Display name in reports. Default: filename. |
| `#!TAGS:` | Comma-separated → TestNG groups. CLI: `-Dgroups=smoke`. |
| `#!PRIORITY:` | `critical / high / medium / low`. |
| `#!AUTHOR:` | Owning team or person. |
| `#!TIMEOUT:` | Total timeout in seconds. Overrides config. |
| `#!RETRY:` | Full-test retry count on failure. |
| `#!DESCRIPTION:` | Free text — report tooltip / documentation. |
| `#!ENVIRONMENT:` | Run only in listed envs. Skipped otherwise. |
| `#!SKIP: true` | Unconditionally skip this test. |
| `#!DRIVER:` | `web / mobile / api-only / db-only`. |

### Section Directives

```csv
#!SECTION: Test Data Setup
CHANGE_DATA_STORE:SET,test_email,,${RANDOM_EMAIL}

#!SECTION: Login
NAVIGATE,,,/login
INPUT,LoginPage.email,,${test_email}

#!SECTION: Verification
ASSERT_VISIBLE,Dashboard.panel,,
```

Creates named, collapsible groupings in the HTML report — easy to pinpoint which
phase failed without reading every step.

---

## Control Flow Actions

All implemented as registered `Action` classes — dispatched by the engine normally.
Each delegates its body to a **subscript CSV file** that the action handler executes by
calling back into the engine. The engine itself is unchanged — it always runs a flat step list.

---

### `EXECUTE:IF` — Conditional Branch

```
Recipient  = condition expression
Source     = CSV file to run when condition is TRUE
Key        = CSV file to run when condition is FALSE (optional)
```

```csv
EXECUTE:IF,${user_role}=ADMIN,flows/admin_check.csv,flows/user_check.csv
EXECUTE:IF,${email_verified}=false,flows/trigger_verification.csv,
```

#### What Is Being Compared?

A **stored variable** against a **literal value**. The variable can be:

- A field from the last API JSON response (`STORE_DATA,role,,user_role`)
- A UI element's text read off the page (`STORE_DATA,LoginPage.otpModal,,otp_visible`)
- A DB column value from the last query result (`STORE_DATA,email_verified,,email_verified`)
- A value set explicitly at setup (`CHANGE_DATA_STORE:SET,environment,,staging`)

#### Condition Operators

| Operator | Example | Meaning |
|---|---|---|
| `=` | `${status}=ACTIVE` | Equals (case-insensitive) |
| `!=` | `${status}!=LOCKED` | Not equals |
| `>` | `${item_count}>0` | Numeric greater than |
| `<` | `${price}<100` | Numeric less than |
| `>=` | `${retry_count}>=3` | Greater or equal |
| `<=` | `${stock}<=5` | Less or equal |
| `contains` | `${page_title} contains Error` | String contains |
| `not_contains` | `${message} not_contains failed` | String does not contain |
| `starts_with` | `${order_id} starts_with ORD-` | Prefix check |
| `ends_with` | `${file} ends_with .pdf` | Suffix check |
| `matches` | `${email} matches ^[^@]+@.+` | Regex match |
| `is_blank` | `${token} is_blank` | Null or empty |
| `is_not_blank` | `${token} is_not_blank` | Non-null, non-empty |

#### Real-World Scenarios

**1. User role branching after login**
```csv
SEND_REQUEST,,login.json|scope=API,LoginBlock
ASSERT_STATUS,,,200
STORE_DATA,role,,user_role
EXECUTE:IF,${user_role}=ADMIN,flows/admin_dashboard.csv,flows/user_dashboard.csv
```
`${user_role}` was pulled from the login API response `{"role": "ADMIN"}`.
Admin dashboard flow runs for ADMIN; user flow for everyone else.

---

**2. OTP / Two-Factor Auth handling**
```csv
CLICK|screenshot=true,LoginPage.signInBtn,,
STORE_DATA,LoginPage.otpModal,,otp_modal_present
EXECUTE:IF,${otp_modal_present}=true,flows/handle_otp.csv,
```
`${otp_modal_present}` stores whether the OTP dialog appeared on screen.
Dedicated OTP interaction flow runs only when needed.

---

**3. Feature flag gating across environments**
```csv
SEND_REQUEST,,get_feature_flags.json|scope=API,GetFlags
STORE_DATA,flags.checkout_v2,,checkout_v2_enabled
EXECUTE:IF,${checkout_v2_enabled}=true,flows/checkout_v2.csv,flows/checkout_v1.csv
```
The entire checkout flow branches based on which version is live in this environment.

---

**4. Payment 3D Secure redirect**
```csv
CLICK,PaymentPage.payBtn,,
STORE_DATA,PaymentPage.statusText,,payment_status
EXECUTE:IF,${payment_status} contains 3D Secure,flows/handle_3ds.csv,flows/confirm_order.csv
```
`${payment_status}` read from a UI element. If the bank triggers 3DS, the OTP redirect
flow runs. Otherwise, verify the order confirmation directly.

---

**5. Environment-specific extra teardown**
```csv
FINALLY,,,
IGNORE_FAIL,DB_EXECUTE:INLINE,,"DELETE FROM users WHERE email='${test_email}'"
EXECUTE:IF,${environment}=staging,flows/staging_extra_cleanup.csv,
```
`${environment}` was set at test setup. Extra cleanup only on staging — prod is untouched.

---

### `EXECUTE:EACH` — Per-Item Iteration

```
Recipient  = variable holding a JSON array or comma-separated list
Source     = CSV file to execute once per item
Key        = alias for current item, available as ${alias} in subscript
```

```csv
EXECUTE:EACH,order_ids,flows/verify_order.csv,current_order
```

Built-in variables inside subscript: `${EACH_INDEX}` (1-based), `${EACH_TOTAL}`

#### Real-World Scenarios

**1. Verify all pending orders**
```csv
SEND_REQUEST,,get_pending_orders.json|scope=API,GetOrders
ASSERT_STATUS,,,200
STORE_DATA,data.orderIds,,order_ids
EXECUTE:EACH,order_ids,flows/verify_order_detail.csv,order_id
```
`flows/verify_order_detail.csv`:
```csv
Action,Recipient,Source,Key
LOG,,,Checking order ${EACH_INDEX}/${EACH_TOTAL}: ${order_id}
NAVIGATE,,,/orders/${order_id}
ASSERT_VISIBLE,OrderPage.status,,
ASSERT_TEXT:not_blank,OrderPage.createdDate,,
ASSERT_NO_FAILURES,,,Order detail assertions for ${order_id}
```

---

**2. Permission matrix — test all roles**
```csv
CHANGE_DATA_STORE:SET,roles,,ADMIN,EDITOR,VIEWER,GUEST
EXECUTE:EACH,roles,flows/test_role_access.csv,role
```
`flows/test_role_access.csv`:
```csv
Action,Recipient,Source,Key
LOG,,,Testing access for role: ${role}
SEND_REQUEST,,login_as_role.json|scope=API,LoginBlock
NAVIGATE,,,/admin/settings
EXECUTE:IF,${role}=ADMIN,flows/assert_access_granted.csv,flows/assert_access_denied.csv
```

---

**3. Dismiss all notifications**
```csv
SEND_REQUEST,,get_notifications.json|scope=API,GetNotifs
STORE_DATA,data.ids,,notification_ids
EXECUTE:EACH,notification_ids,flows/dismiss_notification.csv,notif_id
ASSERT_TEXT,NotificationsPage.emptyState,,No new notifications
```

---

### `REPEAT:N` — Fixed Count Repetition

```
Action   = REPEAT:<N>  (N is the subcase — integer or ${variable})
Source   = CSV file to repeat N times
Key      = (optional) label shown in report per iteration
```

Built-in variables inside subscript: `${LOOP_INDEX}` (1-based), `${LOOP_TOTAL}`

#### Real-World Scenarios

**1. Cart quantity increment**
```csv
NAVIGATE,,,/products/SKU-100
REPEAT:5,,flows/add_one_item.csv,
ASSERT_TEXT,CartPage.quantityBadge,,5
```
`flows/add_one_item.csv`:
```csv
Action,Recipient,Source,Key
CLICK,ProductPage.addToCartBtn,,
LOG,,,Cart quantity: ${LOOP_INDEX}
ASSERT_TEXT,CartPage.quantityBadge,,${LOOP_INDEX}
```

**2. Account lockout after N failed logins**
```csv
CHANGE_DATA_STORE:SET,bad_pass,,WrongPassword!
REPEAT:5,,flows/attempt_bad_login.csv,
ASSERT_TEXT:contains,LoginPage.errorMsg,,Account locked
```

**3. Pagination traversal**
```csv
NAVIGATE,,,/orders?page=1
REPEAT:4,,flows/go_next_page.csv,
ASSERT_TEXT,PaginationBar.currentPage,,5
```

---

### `SKIP:IF` / `SKIP:UNLESS` — Single-Step Skip Gate

```
Recipient  = condition expression
Key        = (optional) reason shown in report when skipped
```

Marks the **immediately following step** as conditionally skipped.
For skipping whole flows, use `EXECUTE:IF` with an empty else branch.

```csv
SKIP:IF,${environment}=production,Skip cleanup on prod
DB_EXECUTE:INLINE,,,"DELETE FROM temp_sessions WHERE user='${test_user}'"

SKIP:UNLESS,${mobile_driver_active}=true,No mobile driver configured
CLICK|driver=mobile,MobileHome.profileBtn,,
```

---

## `IGNORE_FAIL` — Best-Effort Wrapper

Executes the action named in `Recipient` using `Key` as its primary value.
On failure: step is marked **WARN** — test continues, test is not failed.

```csv
IGNORE_FAIL,DB_EXECUTE:INLINE,,"DELETE FROM temp_sessions WHERE email='${test_email}'"
IGNORE_FAIL,DB_EXECUTE:INLINE,,"DELETE FROM staged_carts WHERE user_id='${user_id}'"
IGNORE_FAIL,SEND_REQUEST,delete_user.json|scope=API,DeleteBlock
IGNORE_FAIL,CLICK,CookieBanner.acceptBtn,,
IGNORE_FAIL,CLICK,ModalOverlay.closeBtn,,
```

Report: `IGNORE_FAIL  →  DB_EXECUTE:INLINE  ⚠ WARN — 0 rows affected  12ms`

---

## `ASSERT_NO_FAILURES` — Soft Assertion Gate

Fails the test at this point if any `|onfail=continue` steps above it failed.

```csv
ASSERT_TEXT|onfail=continue,Profile.firstName,,John
ASSERT_TEXT|onfail=continue,Profile.lastName,,Doe
ASSERT_TEXT|onfail=continue,Profile.email,,john@test.com
ASSERT_VISIBLE|onfail=continue,Profile.avatarImg,,
ASSERT_NO_FAILURES,,,All profile field assertions
```

---

## Reporting

The **Action keyword** is the step label in reports. Step number is secondary.

| Step | Report Shows |
|---|---|
| `NAVIGATE,,,/login` | `NAVIGATE  →  /login` |
| `INPUT,LoginPage.email,,admin` | `INPUT  →  LoginPage.email` |
| `ASSERT_TEXT:contains,...` | `ASSERT_TEXT:contains  →  LoginPage.msg` |
| `LOG,,,Starting checkout for ${test_email}` | *Starting checkout for k4f2@hag.io* |
| `#!SECTION: Login` | **── Login ──** (collapsible section header) |
| `IGNORE_FAIL,DB_EXECUTE:INLINE,,DELETE...` | `IGNORE_FAIL  →  DB_EXECUTE:INLINE  ⚠` |

`LOG` steps use the interpolated `Key` value as the label — use LOG for human narrative.

---

## Complete Examples

### A — UI: Login

```csv
#!NAME: Internet Login — Positive Path
#!TAGS: smoke, regression, auth
#!PRIORITY: critical
#!TIMEOUT: 60
Action,Recipient,Source,Key

#!SECTION: Test Data Setup
CHANGE_DATA_STORE:SET,test_user,,tomsmith
CHANGE_DATA_STORE:SET,test_pass,,SuperSecretPassword!

#!SECTION: Navigation
LOG,,,Navigate to login page
NAVIGATE,,,/login

#!SECTION: Credential Entry
INPUT,InternetLogin.username,,${test_user}
INPUT|masked=true,InternetLogin.password,,${test_pass}
CLICK|screenshot=true,InternetLogin.loginButton,,

#!SECTION: Verification
ASSERT_TEXT:contains|retry=3,InternetLogin.flashMessage,,You logged into a secure area!
STORE_DATA,InternetLogin.flashMessage,,login_msg
LOG,,,Login confirmed: ${login_msg}

FINALLY,,,
LOG,,,Teardown done
```

---

### B — API: Post CRUD

```csv
#!NAME: Post CRUD via JSONPlaceholder
#!TAGS: regression, api
#!DRIVER: api-only
Action,Recipient,Source,Key

#!SECTION: Test Data Setup
CHANGE_DATA_STORE:SET,post_title,,HAG-${RANDOM_ALPHANUMERIC:6}

#!SECTION: Create Post
SEND_REQUEST,,create_post.json|scope=API,CreateBlock
ASSERT_STATUS,,,201
ASSERT_RESPONSE,title,,${post_title}
STORE_DATA,id,,created_post_id
LOG,,,Post created: ${created_post_id}

#!SECTION: Fetch and Verify
SEND_REQUEST,,get_post.json|scope=API,GetBlock
ASSERT_STATUS|retry=3,,,200
ASSERT_RESPONSE,id,,${created_post_id}
ASSERT_RESPONSE,title,,${post_title}

FINALLY,,,
LOG,,,API test done — post ID: ${created_post_id}
```

---

### C — DB: Users Table CRUD

```csv
#!NAME: Users Table — CRUD
#!TAGS: regression, db
#!DRIVER: db-only
Action,Recipient,Source,Key

#!SECTION: Test Data Setup
CHANGE_DATA_STORE:SET,user_a,,Alice_${RANDOM_NUMBER:4}
CHANGE_DATA_STORE:SET,user_b,,Bob_${RANDOM_NUMBER:4}

#!SECTION: Schema Setup
IGNORE_FAIL,DB_EXECUTE:INLINE,,"DROP TABLE IF EXISTS demo_users"
DB_EXECUTE:INLINE,,,"CREATE TABLE demo_users (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50), status VARCHAR(20))"

#!SECTION: Insert and Verify Alice
DB_EXECUTE:INLINE,,,"INSERT INTO demo_users (username, status) VALUES ('${user_a}', 'ACTIVE')"
DB_QUERY:INLINE,,,"SELECT * FROM demo_users WHERE username = '${user_a}'"
ASSERT_ROW_COUNT,,,1
ASSERT_COLUMN,status,,ACTIVE

#!SECTION: Insert and Verify Bob
DB_EXECUTE:INLINE,,,"INSERT INTO demo_users (username, status) VALUES ('${user_b}', 'PENDING')"
DB_QUERY:INLINE,,,"SELECT * FROM demo_users WHERE username = '${user_b}'"
ASSERT_ROW_COUNT,,,1
ASSERT_COLUMN,status,,PENDING
STORE_DATA,id,,bob_id
LOG,,,Bob's DB row ID: ${bob_id}

FINALLY,,,
IGNORE_FAIL,DB_EXECUTE:INLINE,,"DROP TABLE IF EXISTS demo_users"
LOG,,,DB teardown complete
```

---

### D — E2E: UI + API + DB + Conditional + Mobile

```csv
#!NAME: End-to-End User Registration
#!TAGS: e2e, regression, smoke
#!PRIORITY: critical
#!TIMEOUT: 180
Action,Recipient,Source,Key

#!SECTION: Test Data Setup
CHANGE_DATA_STORE:SET,test_email,,${RANDOM_EMAIL}
CHANGE_DATA_STORE:SET,test_pass,,P@ss${RANDOM_NUMBER:4}!
CHANGE_DATA_STORE:SET,first_name,,${RANDOM_FIRST_NAME}

#!SECTION: UI — Registration
NAVIGATE,,,/register
INPUT,RegisterPage.email,,${test_email}
INPUT,RegisterPage.firstName,,${first_name}
INPUT|masked=true,RegisterPage.password,,${test_pass}
INPUT|masked=true,RegisterPage.confirmPassword,,${test_pass}
CLICK|screenshot=true,RegisterPage.submitBtn,,
ASSERT_TEXT:contains|retry=3,RegisterPage.successBanner,,Account created
STORE_DATA,RegisterPage.userId,,ui_user_id

#!SECTION: API — Verify
SEND_REQUEST,,get_user.json|scope=API,GetUserBlock
ASSERT_STATUS|retry=3,,,200
ASSERT_RESPONSE,email,,${test_email}
ASSERT_RESPONSE,status,,ACTIVE

#!SECTION: DB — Confirm Persistence
DB_QUERY:INLINE,,,"SELECT * FROM users WHERE email = '${test_email}'"
ASSERT_ROW_COUNT,,,1
ASSERT_COLUMN,status,,ACTIVE

#!SECTION: Conditional Email Verification
STORE_DATA,email_verified,,email_verified_flag
EXECUTE:IF,${email_verified_flag}=false,flows/trigger_email_verification.csv,

#!SECTION: Mobile Smoke Login
SKIP:UNLESS,${mobile_driver_active}=true,No mobile driver
NAVIGATE|driver=mobile,,,/m/login
INPUT|driver=mobile,MobileLogin.email,,${test_email}
INPUT|driver=mobile|masked=true,MobileLogin.password,,${test_pass}
CLICK|driver=mobile|screenshot=true,MobileLogin.signInBtn,,
ASSERT_VISIBLE|driver=mobile|onfail=continue,MobileHome.welcomePanel,,

FINALLY,,,
IGNORE_FAIL,SEND_REQUEST,delete_user.json|scope=API,DeleteBlock
IGNORE_FAIL,DB_EXECUTE:INLINE,,"DELETE FROM users WHERE email='${test_email}'"
LOG,,,E2E complete — ${test_email}
```

---

### E — EXECUTE:EACH: Product Catalogue Sweep

`verify_catalogue.csv`:
```csv
#!NAME: Product Catalogue Sweep
#!TAGS: regression, api
Action,Recipient,Source,Key

SEND_REQUEST,,list_products.json|scope=API,GetProducts
ASSERT_STATUS,,,200
STORE_DATA,products,,all_products

EXECUTE:EACH,all_products,flows/verify_product.csv,product_id

LOG,,,Catalogue sweep complete
```

`flows/verify_product.csv`:
```csv
Action,Recipient,Source,Key
LOG,,,Checking ${EACH_INDEX}/${EACH_TOTAL}: ${product_id}
NAVIGATE,,,/products/${product_id}
ASSERT_VISIBLE|onfail=continue,ProductPage.title,,
ASSERT_VISIBLE|onfail=continue,ProductPage.priceLabel,,
ASSERT_VISIBLE|onfail=continue,ProductPage.addToCartBtn,,
ASSERT_NO_FAILURES,,,Product page assertions for ${product_id}
```

---

## Format Quick Reference

```
 Action                    Recipient              Source                Key
 ─────────────────────────────────────────────────────────────────────────────
 KEYWORD[:SUB][|opts]      first argument         data / options        value

 Core Actions
 NAVIGATE                                                               /path
 INPUT[|masked]            Page.element                                 text
 CLICK[|screenshot]        Page.element
 HOVER                     Page.element
 WAIT:VISIBLE[|timeout]    Page.element
 ASSERT_TEXT[:op][|opts]   Page.element                                 expected
 ASSERT_VISIBLE            Page.element
 ASSERT_ATTRIBUTE:op       Page.element                                 expected
 ASSERT_STATUS[|retry]                                                  200
 ASSERT_RESPONSE[:op]      json.path                                    expected
 ASSERT_ROW_COUNT                                                        N
 ASSERT_COLUMN             column_name                                  expected
 ASSERT_NO_FAILURES                                                      label
 SEND_REQUEST                                     file.json|scope=X     block_name
 DB_QUERY:INLINE                                                         SQL
 DB_EXECUTE:INLINE                                                       SQL
 STORE_DATA                field_path                                   var_name
 CHANGE_DATA_STORE:SET     var_name                                     value
 CHANGE_DATA_STORE:UPDATE  var_name                                     new_value
 CHANGE_DATA_STORE:DELETE  var_name
 LOG                                                                     message (= report label)
 INCLUDE                                                                 subscript.csv

 Control Flow
 EXECUTE:IF               ${var} op value         true.csv              false.csv
 EXECUTE:EACH             ${list_var}             subscript.csv         item_alias
 REPEAT:N                                         subscript.csv         label
 SKIP:IF                  ${var} op value                               reason
 SKIP:UNLESS              ${var} op value                               reason
 IGNORE_FAIL              INNER_ACTION[:sub]       (inner Source)       (inner Key)
 SECTION                                                                 Title
 FINALLY

 Per-step option suffixes on Action:
   |onfail=stop|continue|warn|retry:N
   |timeout=60  |retry=5  |masked=true  |screenshot=true
   |delay=500   |driver=web|mobile

 Data Generators:
   ${RANDOM_EMAIL[:domain]}  ${RANDOM_FIRST_NAME}  ${RANDOM_LAST_NAME}
   ${RANDOM_FULL_NAME}  ${RANDOM_USERNAME}  ${RANDOM_PASSWORD[:N]}
   ${RANDOM_PHONE}  ${RANDOM_PHONE_IN}  ${RANDOM_DOB}  ${RANDOM_GENDER}
   ${RANDOM_CITY}  ${RANDOM_STATE}  ${RANDOM_COUNTRY}  ${RANDOM_COUNTRY_CODE}
   ${RANDOM_ZIP}  ${RANDOM_ADDRESS}
   ${RANDOM_UUID}  ${RANDOM_ID:PREFIX-}  ${RANDOM_ALPHANUMERIC:N}
   ${RANDOM_ALPHA:N}  ${RANDOM_NUMERIC:N}  ${SEQ_ID}
   ${RANDOM_NUMBER[:N]}  ${RANDOM_INTEGER:min:max}
   ${RANDOM_PRICE}  ${RANDOM_PERCENTAGE}  ${RANDOM_DECIMAL:N}
   ${TODAY}  ${DATETIME}  ${TIMESTAMP}
   ${DATE_PLUS:N}  ${DATE_MINUS:N}
   ${RANDOM_PAST_DATE}  ${RANDOM_FUTURE_DATE}
   ${RANDOM_DATE:YYYY-MM-DD:YYYY-MM-DD}
   ${RANDOM_CREDIT_CARD}  ${RANDOM_IBAN}  ${RANDOM_CURRENCY_CODE}  ${RANDOM_AMOUNT}
   ${RANDOM_IP}  ${RANDOM_URL}  ${RANDOM_DOMAIN}  ${RANDOM_MAC}
   ${RANDOM_COMPANY}  ${RANDOM_JOB_TITLE}  ${RANDOM_DEPARTMENT}
   ${RANDOM_WORD}  ${RANDOM_SENTENCE}

 Condition operators: =  !=  >  <  >=  <=
   contains  not_contains  starts_with  ends_with  matches  is_blank  is_not_blank

 Test directives (before header row):
   #!NAME:  #!TAGS:  #!PRIORITY:  #!AUTHOR:  #!TIMEOUT:
   #!RETRY:  #!DRIVER:  #!ENVIRONMENT:  #!SKIP:  #!DESCRIPTION:

 Section directives (between steps):
   #!SECTION: Section Title

 Comments: # plain comment — fully ignored
```
