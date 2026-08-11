package com.campushub.model;

// Owner: Database and Data
// TODO: implement AuditEvent
public class AuditEvent {
    private int eventId;
    private String eventType;
    private String description;
    private String timestamp;

    public AuditEvent(int eventId, String eventType, String description, String timestamp) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.description = description;
        this.timestamp = timestamp;
    }

    public int getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getDescription() {
        return description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "AuditEvent{" +
                "eventId=" + eventId +
                ", eventType='" + eventType + '\'' +
                ", description='" + description + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
