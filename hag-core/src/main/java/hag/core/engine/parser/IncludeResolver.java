package hag.core.engine.parser;

import hag.core.engine.model.Step;
import hag.core.reporting.engine.EventPublisher;
import hag.core.reporting.events.IncludeExpandedEvent;

import java.nio.file.Path;
import java.util.List;

public class IncludeResolver {

    private final CsvTestParser parser;
    private final EventPublisher eventPublisher;

    public IncludeResolver(CsvTestParser parser, EventPublisher eventPublisher) {
        this.parser = parser;
        this.eventPublisher = eventPublisher;
    }

    public List<Step> resolve(
            String testName,
            Step includeStep,
            Path baseDir
    ) {
        Path includePath = baseDir.resolve(includeStep.getKey());
        List<Step> includedSteps = parser.parse(includePath);

        eventPublisher.publish(
                new IncludeExpandedEvent(
                        testName,
                        includePath.toString(),
                        includedSteps.size()
                )
        );

        return includedSteps;
    }
}
