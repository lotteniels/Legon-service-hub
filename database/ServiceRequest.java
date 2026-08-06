/**
 * Plain model class matching the `service_requests` table.
 * These will feed the Priority Structures team (e.g. a priority queue/heap).
 * Rename fields to match schema.sql if they differ.
 */
public class ServiceRequest {

    private int requestId;
    private int locationId;
    private String description;
    private int priority;       // e.g. 1 (highest) - 5 (lowest)
    private String status;      // e.g. "PENDING", "IN_PROGRESS", "RESOLVED"
    private String timestamp;   // stored as TEXT in SQLite; parse to LocalDateTime if needed

    public ServiceRequest(int requestId, int locationId, String description,
                           int priority, String status, String timestamp) {
        this.requestId = requestId;
        this.locationId = locationId;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.timestamp = timestamp;
    }

    public int getRequestId() { return requestId; }
    public int getLocationId() { return locationId; }
    public String getDescription() { return description; }
    public int getPriority() { return priority; }
    public String getStatus() { return status; }
    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "ServiceRequest{" +
                "requestId=" + requestId +
                ", locationId=" + locationId +
                ", description='" + description + '\'' +
                ", priority=" + priority +
                ", status='" + status + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
