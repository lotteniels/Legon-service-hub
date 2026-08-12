package com.campushub.engine;

import com.campushub.db.LocationRepository;
import com.campushub.db.RequestRepository;
import com.campushub.db.ResourceRepository;
import com.campushub.model.Location;
import com.campushub.model.ServiceRequest;
import com.campushub.model.Resource;
import com.campushub.structures.priority.HashTable;
import java.sql.SQLException;
import java.util.List;

public class IndexingEngine {

    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final ResourceRepository resourceRepository;

    private HashTable<Integer, Location> locationIndex;
    private HashTable<Integer, ServiceRequest> requestIndex;
    private HashTable<Integer, Resource> resourceIndex;

    public IndexingEngine() {
        this.locationRepository = new LocationRepository();
        this.requestRepository = new RequestRepository();
        this.resourceRepository = new ResourceRepository();
    }

    // Build all three indexes from the database
    public String buildIndex() {
        try {
            locationIndex = new HashTable<>();
            requestIndex = new HashTable<>();
            resourceIndex = new HashTable<>();

            List<Location> locations = locationRepository.getAllLocations();
            for (Location loc : locations) {
                locationIndex.put(loc.getLocationId(), loc);
            }

            List<ServiceRequest> requests = requestRepository.getAllRequests();
            for (ServiceRequest req : requests) {
                requestIndex.put(req.getRequestId(), req);
            }

            List<Resource> resources = resourceRepository.getAllResources();
            for (Resource res : resources) {
                resourceIndex.put(res.getResourceId(), res);
            }

            return String.format(
                "{\"status\": \"Index built\", \"locations\": %d, \"requests\": %d, \"resources\": %d}",
                locationIndex.size(), requestIndex.size(), resourceIndex.size()
            );

        } catch (SQLException e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    // Search for a record by type and ID
    public String search(String type, int id) {
        if (locationIndex == null) buildIndex();

        switch (type.toLowerCase()) {
            case "location": {
                Location loc = locationIndex.get(id);
                if (loc == null) return "{\"error\": \"Location " + id + " not found.\"}";
                return String.format(
                    "{\"locationId\": %d, \"name\": \"%s\", \"area\": \"%s\", \"type\": \"%s\", \"coordinates\": \"%s\"}",
                    loc.getLocationId(), loc.getName(), loc.getArea(), loc.getType(), loc.getCoordinates()
                );
            }
            case "request": {
                ServiceRequest req = requestIndex.get(id);
                if (req == null) return "{\"error\": \"Request " + id + " not found.\"}";
                return String.format(
                    "{\"requestId\": %d, \"category\": \"%s\", \"urgency\": \"%s\", \"status\": \"%s\"}",
                    req.getRequestId(), req.getCategory(), req.getUrgency(), req.getStatus()
                );
            }
            case "resource": {
                Resource res = resourceIndex.get(id);
                if (res == null) return "{\"error\": \"Resource " + id + " not found.\"}";
                return String.format(
                    "{\"resourceId\": %d, \"name\": \"%s\", \"type\": \"%s\", \"availabilityStatus\": \"%s\"}",
                    res.getResourceId(), res.getName(), res.getType(), res.getAvailabilityStatus()
                );
            }
            default:
                return "{\"error\": \"Unknown type. Use: location, request, or resource\"}";
        }
    }
}

