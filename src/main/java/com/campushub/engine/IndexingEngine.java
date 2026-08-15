package com.campushub.engine;

import com.campushub.db.LocationRepository;
import com.campushub.db.RequestRepository;
import com.campushub.db.ResourceRepository;
import com.campushub.model.Location;
import com.campushub.model.ServiceRequest;
import com.campushub.model.Resource;
import com.campushub.structures.priority.HashTable;
import com.campushub.structures.tree.BST;
import com.campushub.structures.tree.BTree;
import com.campushub.structures.tree.RedBlackTree;

import java.sql.SQLException;
import com.campushub.structures.linear.DynamicArray;

/**
 * Indexing engine (M6).
 *
 * <p>Three independent indexes are built over locations, demonstrating the
 * required tree structures from Section 6 of the brief:
 * <ul>
 *   <li>{@link BST} — primary search index, O(h) average where h is tree height</li>
 *   <li>{@link RedBlackTree} — self-balancing, O(log n) guaranteed height</li>
 *   <li>{@link BTree} — simulates a B-tree page index as required by M6</li>
 * </ul>
 *
 * <p>Service requests and resources are indexed in a {@link HashTable} for
 * O(1) average lookup by ID. All indexes are rebuilt from the database on
 * demand and re-used for subsequent searches.
 */
public class IndexingEngine {

    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final ResourceRepository resourceRepository;

    // --- Location indexes (BST, RedBlackTree, BTree) ---
    private BST locationBst;
    private RedBlackTree locationRbt;
    private BTree<Integer, String> locationBTree;

    // --- Request / Resource indexes (HashTable) ---
    private HashTable<Integer, ServiceRequest> requestIndex;
    private HashTable<Integer, Resource> resourceIndex;

    private int locationCount;

    public IndexingEngine() {
        this.locationRepository  = new LocationRepository();
        this.requestRepository   = new RequestRepository();
        this.resourceRepository  = new ResourceRepository();
    }

    // -------------------------------------------------------------------------
    // Index build
    // -------------------------------------------------------------------------

    /**
     * Loads all records from the database and populates all three location
     * indexes plus the hash-table indexes for requests and resources.
     *
     * @return JSON summary of record counts and index types built.
     */
    public String buildIndex() {
        try {
            // --- Build location indexes ---
            locationBst    = new BST();
            locationRbt    = new RedBlackTree();
            // BTree minimum degree t=3 gives nodes of 2–5 keys — a reasonable
            // page size for 58 locations, matching the brief's B-tree page simulation.
            locationBTree  = new BTree<>(3);

            DynamicArray<Location> locations = locationRepository.getAllLocations();
            locationCount = locations.size();

            for (int i = 0; i < locations.size(); i++) {
                Location loc = locations.get(i);
                String locJson = locationToJson(loc);
                locationBst.insert(loc.getLocationId(), locJson);
                locationRbt.insert(loc.getLocationId(), locJson);
                locationBTree.put(loc.getLocationId(), locJson);
            }

            // --- Build request index ---
            requestIndex = new HashTable<>();
            DynamicArray<ServiceRequest> requests = requestRepository.getAllRequests();
            for (int i = 0; i < requests.size(); i++) {
                ServiceRequest req = requests.get(i);
                requestIndex.put(req.getRequestId(), req);
            }

            // --- Build resource index ---
            resourceIndex = new HashTable<>();
            DynamicArray<Resource> resources = resourceRepository.getAllResources();
            for (int i = 0; i < resources.size(); i++) {
                Resource res = resources.get(i);
                resourceIndex.put(res.getResourceId(), res);
            }

            return String.format(
                "{\"status\":\"Index built\",\"locations\":%d,"
                    + "\"requests\":%d,\"resources\":%d,"
                    + "\"locationIndexes\":[\"BST\",\"RedBlackTree\",\"BTree(t=3)\"],"
                    + "\"requestIndex\":\"HashTable\",\"resourceIndex\":\"HashTable\"}",
                locationCount, requestIndex.size(), resourceIndex.size());

        } catch (SQLException e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    // -------------------------------------------------------------------------
    // Search
    // -------------------------------------------------------------------------

    /**
     * Searches by type and ID. Location lookups use all three tree structures
     * and return BST's result (all three hold the same data).
     *
     * <p>Valid types: {@code location}, {@code location/rbt}, {@code location/btree},
     * {@code request}, {@code resource}.
     *
     * @param type  record type (case-insensitive)
     * @param id    primary key
     * @return JSON string of the found record, or an error object.
     */
    public String search(String type, int id) {
        if (locationBst == null) buildIndex();

        switch (type.toLowerCase()) {
            // --- Location searches via each tree structure ---
            case "location": {
                String result = locationBst.search(id);
                if (result == null) return "{\"error\":\"Location " + id + " not found.\"}";
                return result;
            }
            case "location/rbt": {
                String result = locationRbt.search(id);
                if (result == null) return "{\"error\":\"Location " + id + " not found.\"}";
                return result;
            }
            case "location/btree": {
                String result = locationBTree.get(id);
                if (result == null) return "{\"error\":\"Location " + id + " not found.\"}";
                return result;
            }
            // --- Request / Resource via HashTable ---
            case "request": {
                ServiceRequest req = requestIndex.get(id);
                if (req == null) return "{\"error\":\"Request " + id + " not found.\"}";
                return String.format(
                    "{\"requestId\":%d,\"category\":\"%s\",\"urgency\":\"%s\","
                        + "\"status\":\"%s\",\"fineAmountGHS\":%.2f}",
                    req.getRequestId(), req.getCategory(),
                    req.getUrgency(), req.getStatus(), req.getFineAmountGHS());
            }
            case "resource": {
                Resource res = resourceIndex.get(id);
                if (res == null) return "{\"error\":\"Resource " + id + " not found.\"}";
                return String.format(
                    "{\"resourceId\":%d,\"name\":\"%s\",\"type\":\"%s\","
                        + "\"availabilityStatus\":\"%s\"}",
                    res.getResourceId(), res.getName(),
                    res.getType(), res.getAvailabilityStatus());
            }
            default:
                return "{\"error\":\"Unknown type. Use: location, location/rbt, "
                    + "location/btree, request, or resource\"}";
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private static String locationToJson(Location loc) {
        return String.format(
            "{\"locationId\":%d,\"name\":\"%s\",\"area\":\"%s\","
                + "\"type\":\"%s\",\"coordinates\":\"%s\"}",
            loc.getLocationId(),
            esc(loc.getName()), esc(loc.getArea()),
            esc(loc.getType()), esc(loc.getCoordinates()));
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
