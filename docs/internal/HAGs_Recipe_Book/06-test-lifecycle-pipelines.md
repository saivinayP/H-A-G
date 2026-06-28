# 6. Test Lifecycle & Pipelines

The runner execution relies heavily on TestNG's parallel execution features and lifecycle annotations. The main entry point for running a test is the `HagTestBase` class, which is inherited by suite runners (like `TestRunner.java`).

## The Suite Lifecycle

```mermaid
sequenceDiagram
    participant TestNG
    participant Base as HagTestBase
    participant Boot as FrameworkBootstrap
    participant Engine as DefaultExecutionEngine
    participant Pub as EventPublisher

    TestNG->>Base: @BeforeSuite setUpSuite()
    Base->>Boot: createEngine()
    Boot->>Boot: Register Core, UI, API, DB Actions
    Boot->>Engine: return new Engine instance
    Base->>Engine: cache as static sharedEngine
    
    TestNG->>Base: @BeforeMethod setUpTest()
    Base->>Base: Spawn ThreadLocal WebDriver
    Base->>Base: Spawn ThreadLocal ExecutionContext
    Base->>Base: Register DbClientRegistry
    
    TestNG->>Runner: @Test runScenario()
    Runner->>Engine: execute("Test Name", "file.csv", threadContext)
    Engine->>Pub: publish(TestStartedEvent)
    Engine->>Engine: execute steps...
    
    TestNG->>Base: @AfterMethod tearDownTest()
    Base->>Base: driver.quit(), remove ThreadLocals
    Base->>DbClientRegistry: closeAll()
    
    TestNG->>Base: @AfterSuite tearDownSuite()
    Base->>Pub: endSuite() (Flushes reporters)
    Base->>Base: clear static Caches (DataResolver, Locators)
```

## Bootstrap Phase
The `FrameworkBootstrap` class is critical. When the JVM starts the suite, the Bootstrap class explicitly creates the `ActionRegistry` and calls `.register("CLICK", new ClickAction())` for every action in the framework.

> [!IMPORTANT]
> H-A-G purposely avoids Spring Boot or reflection-based classpath scanning (like Java Reflections API) to register actions. This was an architectural decision to keep startup times under 200ms and avoid "magic" behavior. You must manually register new actions in Bootstrap.
