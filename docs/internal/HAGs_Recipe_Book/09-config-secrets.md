# 9. Configuration & Secrets

All configuration in H-A-G is driven by a single source of truth: `hag.yml`.

## The `hag.yml` File
Located in the project root, this file dictates the behavior of the runner, database connections, paths, and reporters.

```yaml
runner:
  browser: "chrome"
  headless: false
  execution-mode: "local"

databases:
  default:
    url: "jdbc:mysql://localhost/testdb"
    username: "user"
    password: "${DB_PASSWORD}"  # Environment variable fallback
```

## `ConfigLoader`
The `ConfigLoader` class is responsible for reading `hag.yml`. It uses Jackson YAML parser.

### Environment Variable Substitution
`ConfigLoader` supports replacing `${ENV_VAR}` strings with actual environment variables or Java System Properties. 
- If a CI/CD pipeline sets `export DB_PASSWORD=secret`, `ConfigLoader` will inject it before returning the configuration POJO.
- This prevents hardcoding secrets inside the repository.

## Overrides
When running via CLI, System Properties passed via Maven (`-Dbrowser=firefox`) will override the defaults set in `hag.yml`. This logic is implemented inside `HagTestBase.setUpTest()`.
