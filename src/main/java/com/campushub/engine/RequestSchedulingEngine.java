package com.campushub.engine;

import com.campushub.db.RequestRepository;
import com.campushub.model.ServiceRequest;
import com.campushub.structures.priority.PriorityQueue;
import java.sql.SQLException;
import java.util.List;
import java.util.LinkedList;

public class RequestSchedulingEngine {

    private final RequestRepository requestRepository;

    public RequestSchedulingEngine() {
        this.requestRepository = new RequestRepository();
    }

    // Priority mode: highest urgency job first (HIGH=1, MEDIUM=2, LOW=3)
    public String scheduleRequests() {
        try {
            List<ServiceRequest> allRequests = requestRepository.getAllRequests();

            PriorityQueue<ServiceRequest> queue = new PriorityQueue<>(allRequests.size() + 1);

            for (ServiceRequest req : allRequests) {
                if ("pending".equalsIgnoreCase(req.getStatus())) {
                    int priority = urgencyToInt(req.getUrgency());
                    queue.enqueue(req, priority);
                }
            }

            if (queue.isEmpty()) {
                return "{\"message\": \"No pending service requests.\"}";
            }

            ServiceRequest next = queue.dequeue();
            requestRepository.updateStatus(next.getRequestId(), "IN_PROGRESS");

            return String.format(
                "{\"requestId\": %d, \"category\": \"%s\", \"urgency\": \"%s\", " +
                "\"sourceLocationId\": %d, \"destinationLocationId\": %d, " +
                "\"timeSubmitted\": \"%s\", \"deadline\": \"%s\", \"status\": \"IN_PROGRESS\"}",
                next.getRequestId(), next.getCategory(), next.getUrgency(),
                next.getSourceLocationId(), next.getDestinationLocationId(),
                next.getTimeSubmitted(), next.getDeadline()
            );

        } catch (SQLException e) {
            return "{\"error\": \"Database error: " + e.getMessage() + "\"}";
        }
    }

    // FIFO mode: oldest submitted job first
    public String scheduleRequestsFIFO() {
        try {
            List<ServiceRequest> allRequests = requestRepository.getAllRequests();
            LinkedList<ServiceRequest> fifoQueue = new LinkedList<>();

            for (ServiceRequest req : allRequests) {
                if ("pending".equalsIgnoreCase(req.getStatus())) {
                    fifoQueue.addLast(req);
                }
            }

            if (fifoQueue.isEmpty()) {
                return "{\"message\": \"No pending service requests.\"}";
            }

            ServiceRequest next = fifoQueue.removeFirst();
            requestRepository.updateStatus(next.getRequestId(), "IN_PROGRESS");

            return String.format(
                "{\"requestId\": %d, \"category\": \"%s\", \"urgency\": \"%s\", " +
                "\"sourceLocationId\": %d, \"destinationLocationId\": %d, " +
                "\"timeSubmitted\": \"%s\", \"deadline\": \"%s\", \"status\": \"IN_PROGRESS\"}",
                next.getRequestId(), next.getCategory(), next.getUrgency(),
                next.getSourceLocationId(), next.getDestinationLocationId(),
                next.getTimeSubmitted(), next.getDeadline()
            );

        } catch (SQLException e) {
            return "{\"error\": \"Database error: " + e.getMessage() + "\"}";
        }
    }

    private int urgencyToInt(String urgency) {
        if (urgency == null) return 3;
        switch (urgency.toLowerCase()) {
            case "high":   return 1;
            case "medium": return 2;
            default:       return 3; // low or unknown
        }
    }
}

