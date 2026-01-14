package hag.core.engine;

import java.nio.file.Path;

public interface ExecutionEngine {
    void execute(String testName, Path testFile);
}
