# 12. Gotchas, Tech Debt & Pitfalls

When developing for H-A-G, beware of these historical pitfalls and architectural trade-offs.

## 1. The ThreadLocal Leak Pitfall
Because H-A-G relies heavily on `ThreadLocal` for parallel execution (via `ExecutionContext` and Repoter stacks), you must be extremely careful when writing code that runs at the end of a test.
- **The Gotcha:** If a thread completes a test but fails to call `.remove()` or `.clear()` on its ThreadLocal objects, those objects remain in memory when TestNG returns the thread to the pool.
- **Past Fixes:** In V2.0, a massive memory leak was fixed in `ReportPortalEngine` where the `itemStack` ThreadLocal was not being removed. Another leak was fixed in `DefaultTestDataResolver` where the static JSON map grew infinitely.
- **Rule:** If you introduce a cache, you MUST provide a way to clear it in `HagTestBase.tearDownSuite()`. If you introduce a ThreadLocal, you MUST clear it in `HagTestBase.tearDownTest()`.

## 2. OpenCSV Preamble Directives
The CSV parser (`CsvTestParser`) does a two-pass read. 
1. It reads raw lines with a `BufferedReader` to find `#!` directives.
2. It restarts and uses OpenCSV for the actual data.
- **The Gotcha:** If you add a new CSV directive feature, ensure you don't consume the InputStream permanently on the first pass, otherwise the second pass will parse an empty file.

## 3. TemplateMerger JSON Corruption
The `TemplateMerger` performs regex string substitution on the raw JSON string BEFORE parsing it into an object.
- **The Gotcha:** If a substituted value contains unescaped quotes (e.g. `He said "Hello"`), the resulting JSON string will be invalid and Jackson will throw a parse error. Currently, there is no automatic JSON escaping in the interpolator.

## 4. No Reflection Discovery
H-A-G intentionally does not use reflection (like Reflections library or Spring) to auto-discover Action classes. 
- **The Gotcha:** Developers often write an Action and wonder why the engine says "Unknown Action". You must manually register it in `FrameworkBootstrap.java`. This tech debt was chosen as a feature to keep startup times lightning fast.
