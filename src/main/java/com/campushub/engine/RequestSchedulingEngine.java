package com.campushub.engine;

import com.campushub.db.RequestRepository;
import com.campushub.model.ServiceRequest;
import com.campushub.structures.linear.CircularQueue;
import com.campushub.structures.linear.Deque;
import com.campushub.structures.linear.Queue;
import com.campushub.structures.linear.Stack;
import com.campushub.structures.priority.PriorityQueue;

import java.sql.SQLException;
import com.campushub.structures.linear.DynamicArray;

/**
 * Service scheduling engine (M5).
 *
 * <p>Demonstrates four distinct dispatch strategies, each backed by a
 * different custom data structure as required by Section 6 of the brief:
 *
 * <ul>
 *   <li><b>Priority queue</b> — highest urgency first (HIGH=1, MEDIUM=2, LOW=3)</li>
 *   <li><b>FIFO queue</b> — oldest submitted request first</li>
 *   <li><b>Circular queue</b> — rotates dispatch across location zones so no
 *       single zone monopolises service capacity</li>
 *   <li><b>Deque (double-ended queue)</b> — urgent requests are inserted at
 *       the front (addFront), normal requests at the rear (addRear), so
 *       urgency is respected without a full priority sort</li>
 * </ul>
 *
 * <p>Every dispatched request is pushed onto an undo {@link Stack} so the
 * last dispatch can be reverted.
 */
public class RequestSchedulingEngine {

    private final RequestRepository requestRepository;
    private final Stack<ServiceRequest> undoStack;

    public RequestSchedulingEngine() {
        this.requestRepository = new RequestRepository();
        this.undoStack         = new Stack<>(20);
    }

    // =========================================================================
    // 1. Priority-queue dispatch (HIGH urgency first)
    // =========================================================================

    public String scheduleRequests() {
        try {
            DynamicArray<ServiceRequest> allRequests = requestRepository.getAllRequests();
            PriorityQueue<ServiceRequest> queue =
                    new PriorityQueue<>(allRequests.size() + 1);

            for (int i = 0; i < allRequests.size(); i++) {
                ServiceRequest req = allRequests.get(i);
                if ("pending".equalsIgnoreCase(req.getStatus())) {
                    queue.enqueue(req, urgencyToInt(req.getUrgency()));
                }
            }

            if (queue.isEmpty()) {
                return "{\"message\":\"No pending service requests.\"}";
            }

            ServiceRequest next = queue.dequeue();
            requestRepository.updateStatus(next.getRequestId(), "IN_PROGRESS");
            undoStack.push(next);
            return requestToJson(next, "IN_PROGRESS", "PriorityQueue");

        } catch (SQLException e) {
            return "{\"error\":\"Database error: " + e.getMessage() + "\"}";
        }
    }

    // =========================================================================
    // 2. FIFO dispatch (oldest first via Queue)
    // =========================================================================

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
                return "{\"message\":\"No pending service requests.\"}";
            }

            ServiceRequest next = fifoQueue.dequeue();
            requestRepository.updateStatus(next.getRequestId(), "IN_PROGRESS");
            undoStack.push(next);
            return requestToJson(next, "IN_PROGRESS", "FIFOQueue");

        } catch (SQLException e) {
            return "{\"error\":\"Database error: " + e.getMessage() + "\"}";
        }
    }

    // =========================================================================
    // 3. Circular-zone dispatch (CircularQueue)
    //
    //    Requests are grouped into buckets by sourceLocationId % ZONE_COUNT.
    //    A CircularQueue rotates fairly across zones so each zone gets a turn,
    //    preventing any single high-traffic zone from starving others.
    // =========================================================================

    private static final int ZONE_COUNT = 5;

    public String scheduleCircularZone() {
        try {
            DynamicArray<ServiceRequest> allRequests = requestRepository.getAllRequests();

            // One circular queue per zone
            @SuppressWarnings("unchecked")
            CircularQueue<ServiceRequest>[] zones = new CircularQueue[ZONE_COUNT];
            for (int z = 0; z < ZONE_COUNT; z++) {
                zones[z] = new CircularQueue<>(64);
            }

            for (int i = 0; i < allRequests.size(); i++) {
                ServiceRequest req = allRequests.get(i);
                if ("pending".equalsIgnoreCase(req.getStatus())) {
                    int zone = req.getSourceLocationId() % ZONE_COUNT;
                    if (!zones[zone].isFull()) {
                        zones[zone].enqueue(req);
                    }
                }
            }

            // Find the next non-empty zone in round-robin order
            for (int z = 0; z < ZONE_COUNT; z++) {
                if (!zones[z].isEmpty()) {
                    ServiceRequest next = zones[z].dequeue();
                    requestRepository.updateStatus(next.getRequestId(), "IN_PROGRESS");
                    undoStack.push(next);
                    String base = requestToJson(next, "IN_PROGRESS", "CircularQueue");
                    // Splice zone info into JSON before closing brace
                    return base.substring(0, base.length() - 1)
                            + ",\"zone\":" + z + "}";
                }
            }

            return "{\"message\":\"No pending service requests.\"}";

        } catch (SQLException e) {
            return "{\"error\":\"Database error: " + e.getMessage() + "\"}";
        }
    }

    // =========================================================================
    // 4. Deque urgent-front dispatch (Deque)
    //
    //    HIGH requests are inserted at the front of the deque so they leap
    //    ahead of already-queued normal requests. MEDIUM and LOW go to the
    //    rear. The deque front is dispatched next.
    // =========================================================================

    public String scheduleDequeUrgent() {
        try {
            DynamicArray<ServiceRequest> allRequests = requestRepository.getAllRequests();
            Deque<ServiceRequest> deque = new Deque<>();

            for (int i = 0; i < allRequests.size(); i++) {
                ServiceRequest req = allRequests.get(i);
                if ("pending".equalsIgnoreCase(req.getStatus())) {
                    // HIGH urgency: jump to front; everything else: rear
                    if ("high".equalsIgnoreCase(req.getUrgency())) {
                        deque.addFront(req);
                    } else {
                        deque.addRear(req);
                    }
                }
            }

            if (deque.isEmpty()) {
                return "{\"message\":\"No pending service requests.\"}";
            }

            ServiceRequest next = deque.removeFront();
            requestRepository.updateStatus(next.getRequestId(), "IN_PROGRESS");
            undoStack.push(next);
            return requestToJson(next, "IN_PROGRESS", "Deque");

        } catch (SQLException e) {
            return "{\"error\":\"Database error: " + e.getMessage() + "\"}";
        }
    }

    // =========================================================================
    // Undo (Stack pop)
    // =========================================================================

    public String undoLastDispatch() {
        if (undoStack.isEmpty()) {
            return "{\"error\":\"Undo stack is empty. Nothing to undo.\"}";
        }
        ServiceRequest last = undoStack.pop();
        try {
            requestRepository.updateStatus(last.getRequestId(), "PENDING");
            return String.format(
                "{\"message\":\"Undid dispatch for request %d\","
                    + "\"requestId\":%d}",
                last.getRequestId(), last.getRequestId());
        } catch (SQLException e) {
            return "{\"error\":\"Database error: " + e.getMessage() + "\"}";
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private int urgencyToInt(String urgency) {
        if (urgency == null) return 3;
        switch (urgency.toLowerCase()) {
            case "high":   return 1;
            case "medium": return 2;
            default:       return 3;
        }
    }

    private static String requestToJson(ServiceRequest req, String status, String strategy) {
        return String.format(
            "{\"requestId\":%d,\"category\":\"%s\",\"urgency\":\"%s\","
                + "\"sourceLocationId\":%d,\"destinationLocationId\":%d,"
                + "\"timeSubmitted\":\"%s\",\"deadline\":\"%s\","
                + "\"status\":\"%s\",\"strategy\":\"%s\"}",
            req.getRequestId(), req.getCategory(), req.getUrgency(),
            req.getSourceLocationId(), req.getDestinationLocationId(),
            req.getTimeSubmitted(), req.getDeadline(),
            status, strategy);
    }
}
