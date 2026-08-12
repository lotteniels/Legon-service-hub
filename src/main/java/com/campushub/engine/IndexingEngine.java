package com.campushub.engine;

import com.campushub.db.LocationRepository;
import com.campushub.db.RequestRepository;
import com.campushub.db.ResourceRepository;
import com.campushub.model.Location;
import com.campushub.model.ServiceRequest;
import com.campushub.model.Resource;
import com.campushub.structures.priority.HashTable;
import com.campushub.structures.tree.BST;
import java.sql.SQLException;
import com.campushub.structures.linear.DynamicArray;

public class IndexingEngine {

    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final ResourceRepository resourceRepository;

    private BST locationIndex;
    private HashTable<Integer, ServiceRequest> requestIndex;
    private HashTable<Integer, Resource> resourceIndex;

    private int locationCount;

    public IndexingEngine() {
        this.locationRepository = new LocationRepository();
        this.requestRepository = new RequestRepository();
        this.resourceRepository = new ResourceRepository();
    }

    // Build all three indexes from the database
    public String buildIndex() {
        try {
            locationIndex = new BST();
            requestIndex = new HashTable<>();
            resourceIndex = new HashTable<>();

            DynamicArray<Location> locations = locationRepository.getAllLocations();
            locationCount = locations.size();
            for (int i = 0; i < locations.size(); i++) {
                Location loc = locations.get(i);
                String locJson = String.format(
                    "{\"locationId\": %d, \"name\": \"%s\", \"area\": \"%s\", \"type\": \"%s\", \"coordinates\": \"%s\"}",
                    loc.getLocationId(), loc.getName(), loc.getArea(), loc.getType(), loc.getCoordinates()
                );
                locationIndex.insert(loc.getLocationId(), locJson);
            }

            DynamicArray<ServiceRequest> requests = requestRepository.getAllRequests();
            for (int i = 0; i < requests.size(); i++) {
                ServiceRequest req = requests.get(i);
                requestIndex.put(req.getRequestId(), req);
            }

            DynamicArray<Resource> resources = resourceRepository.getAllResources();
            for (int i = 0; i < resources.size(); i++) {
                Resource res = resources.get(i);
                resourceIndex.put(res.getResourceId(), res);
            }

            return String.format(
                "{\"status\": \"Index built\", \"locations\": %d, \"requests\": %d, \"resources\": %d}",
                locationCount, requestIndex.size(), resourceIndex.size()
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
                String locJson = locationIndex.search(id);
                if (locJson == null) return "{\"error\": \"Location " + id + " not found.\"}";
                return locJson;
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


