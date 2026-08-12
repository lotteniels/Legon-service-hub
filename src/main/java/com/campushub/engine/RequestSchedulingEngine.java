package com.campushub.engine;

import com.campushub.db.RequestRepository;
import com.campushub.model.ServiceRequest;
import com.campushub.structures.priority.PriorityQueue;
import com.campushub.structures.linear.Queue;
import com.campushub.structures.linear.Stack;
import java.sql.SQLException;
import com.campushub.structures.linear.DynamicArray;

public class RequestSchedulingEngine {

    private final RequestRepository requestRepository;
    private final Stack<ServiceRequest> undoStack;

    public RequestSchedulingEngine() {
        this.requestRepository = new RequestRepository();
        this.undoStack = new Stack<>(20);
    }

    // Priority mode: highest urgency job first (HIGH=1, MEDIUM=2, LOW=3)
    public String scheduleRequests() {
        try {
            DynamicArray<ServiceRequest> allRequests = requestRepository.getAllRequests();

            PriorityQueue<ServiceRequest> queue = new PriorityQueue<>(allRequests.size() + 1);

            for (int i = 0; i < allRequests.size(); i++) {
                ServiceRequest req = allRequests.get(i);
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
            undoStack.push(next); // Save for undo

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
            DynamicArray<ServiceRequest> allRequests = requestRepository.getAllRequests();
            Queue<ServiceRequest> fifoQueue = new Queue<>();

            for (int i = 0; i < allRequests.size(); i++) {
                ServiceRequest req = allRequests.get(i);
                if ("pending".equalsIgnoreCase(req.getStatus())) {
                    fifoQueue.enqueue(req);
                }
            }

            if (fifoQueue.isEmpty()) {
                return "{\"message\": \"No pending service requests.\"}";
            }

            ServiceRequest next = fifoQueue.dequeue();
            requestRepository.updateStatus(next.getRequestId(), "IN_PROGRESS");
            undoStack.push(next); // Save for undo

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

    public String undoLastDispatch() {
        if (undoStack.isEmpty()) {
            return "{\"error\": \"Undo stack is empty. Nothing to undo.\"}";
        }
        ServiceRequest last = undoStack.pop();
        try {
            requestRepository.updateStatus(last.getRequestId(), "PENDING");
            return String.format(
                "{\"message\": \"Undid dispatch for request %d\", \"requestId\": %d}", 
                last.getRequestId(), last.getRequestId()
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


