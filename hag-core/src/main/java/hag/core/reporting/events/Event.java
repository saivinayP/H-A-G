package hag.core.reporting.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Event {

    private final String eventId;
    private final EventType eventType;
    private final long timestamp;
    private final String testName;
    private final String threadId;

    protected Event(EventType evenType, String testName) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = Objects.requireNonNull(evenType, "Event type cannot be null");
        this.testName = Objects.requireNonNull(testName, "Test name cannot be null");
        this.timestamp = Instant.now().toEpochMilli();
        this.threadId = Thread.currentThread().getName();
    }

    public String getEventId() {
        return eventId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTestName() {
        return testName;
    }

    public String getThreadId() {
        return threadId;
    }
}