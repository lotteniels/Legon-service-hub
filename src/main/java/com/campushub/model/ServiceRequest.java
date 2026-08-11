package com.campushub.model;

// Owner: Database and Data
// TODO: implement ServiceRequest
public class ServiceRequest {
    private int requestId;
    private int sourceLocationId;
    private int destinationLocationId;
    private String category;
    private String urgency;
    private String timeSubmitted;
    private String deadline;
    private String status;
    private double fineAmountGHS;

    public ServiceRequest(int requestId, int sourceLocationId, int destinationLocationId,
                          String category, String urgency, String timeSubmitted,
                          String deadline, String status, double fineAmountGHS) {
        this.requestId = requestId;
        this.sourceLocationId = sourceLocationId;
        this.destinationLocationId = destinationLocationId;
        this.category = category;
        this.urgency = urgency;
        this.timeSubmitted = timeSubmitted;
        this.deadline = deadline;
        this.status = status;
        this.fineAmountGHS = fineAmountGHS;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getSourceLocationId() {
        return sourceLocationId;
    }

    public int getDestinationLocationId() {
        return destinationLocationId;
    }

    public String getCategory() {
        return category;
    }

    public String getUrgency() {
        return urgency;
    }

    public String getTimeSubmitted() {
        return timeSubmitted;
    }

    public String getDeadline() {
        return deadline;
    }

    public String getStatus() {
        return status;
    }

    public double getFineAmountGHS() {
        return fineAmountGHS;
    }

    @Override
    public String toString() {
        return "ServiceRequest{" +
                "requestId=" + requestId +
                ", sourceLocationId=" + sourceLocationId +
                ", destinationLocationId=" + destinationLocationId +
                ", category='" + category + '\'' +
                ", urgency='" + urgency + '\'' +
                ", timeSubmitted='" + timeSubmitted + '\'' +
                ", deadline='" + deadline + '\'' +
                ", status='" + status + '\'' +
                ", fineAmountGHS=" + fineAmountGHS +
                '}';
    }
}
