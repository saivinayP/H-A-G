# 7. Memory & State Management

Test automation frameworks commonly fail at scale due to leaky state between tests (e.g. Test A stores a variable, and Test B accidentally reads it, causing flakes). 

H-A-G completely eliminates this through a strict `ThreadLocal` architecture.

## The ExecutionContext

The `ExecutionContext` is a POJO that holds:
- `DataStore`: A `ConcurrentHashMap` for storing variables during a test.
- `SeleniumUiAdapter`: The active browser session for this thread.
- `RestAssuredApiAdapter`: The API client for this thread.
- `DbClientRegistry`: The active database connections for this thread.

When a test method starts (via TestNG `@BeforeMethod`), `HagTestBase` creates a brand new `ExecutionContext` and sets it in a `ThreadLocal` called `threadContext`.

When `Engine.execute()` is called, this context is passed down into the engine, and then passed into every single `Action`.

## DataStore and Variable Scoping
- **Scope:** Variables stored via `STORE_DATA` live entirely inside the `DataStore` of the current thread's `ExecutionContext`.
- **Lifetime:** They are destroyed the moment the `@AfterMethod` fires.
- **Cross-Test Sharing:** By design, H-A-G does NOT support sharing variables between different CSV test files running in parallel. If Test A needs data from Test B, they must be combined into a single CSV or use an external database.

## Engine State
The `DefaultExecutionEngine` is a singleton created during `@BeforeSuite`. **It has no instance variables that hold test data.** The only instance variable it holds is the `ActionRegistry` and the `EventPublisher`. This makes it 100% thread-safe to share across 50 parallel TestNG threads.

## Clean Up (Preventing Memory Leaks)
Because ThreadLocals can leak memory if a thread is returned to a thread pool (like TestNG's thread pool) without being cleared, H-A-G strictly enforces cleanup:
1. `HagTestBase.tearDownTest()` explicitly calls `threadContext.remove()` and `threadDriver.remove()`.
2. Static caches (`LocatorRepository` and `DefaultTestDataResolver`) are explicitly cleared in `@AfterSuite`.
