import com.campushub.db.DatabaseConnection;
import com.campushub.model.Location;
import com.campushub.model.Resource;
import com.campushub.model.Road;
import com.campushub.model.ServiceRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Read side of the data layer (mirrors DataGenerator.java, which only writes).
 * One load method per table. Each method:
 *  - queries the table
 *  - validates each row before adding it to the result list
 *  - logs rejected rows (and a summary on completion) to audit_events
 */
public class DataLoader {

    // ---------- LOCATIONS ----------

    public static List<Location> loadLocations(Connection conn) throws SQLException {
        List<Location> locations = new ArrayList<>();
        int rejected = 0;

        String sql = "SELECT locationId, name, area, type, coordinates FROM locations";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Integer locationId = getNullableInt(rs, "locationId");
                String name = rs.getString("name");
                String area = rs.getString("area");
                String type = rs.getString("type");
                String coordinates = rs.getString("coordinates");

                if (!isValidLocation(locationId, name, coordinates)) {
                    rejected++;
                    logAudit(conn, "LOCATION_REJECTED",
                            "Rejected location row with locationId=" + locationId + " (missing/invalid required field)");
                    continue;
                }

                locations.add(new Location(locationId, name, area, type, coordinates));
            }
        }

        logAudit(conn, "LOCATIONS_LOADED",
                "Loaded " + locations.size() + " locations (" + rejected + " rejected)");
        return locations;
    }

    private static boolean isValidLocation(Integer locationId, String name, String coordinates) {
        return locationId != null && locationId > 0
                && name != null && !name.trim().isEmpty()
                && coordinates != null && !coordinates.trim().isEmpty();
    }

    // ---------- ROADS ----------

    public static List<Road> loadRoads(Connection conn) throws SQLException {
        List<Road> roads = new ArrayList<>();
        int rejected = 0;

        String sql = "SELECT fromLocationId, toLocationId, distance_m, travelTime_min, roadConditionWeight FROM roads";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Integer fromLocationId = getNullableInt(rs, "fromLocationId");
                Integer toLocationId = getNullableInt(rs, "toLocationId");
                Double distance_m = getNullableDouble(rs, "distance_m");
                Double travelTime_min = getNullableDouble(rs, "travelTime_min");
                Double roadConditionWeight = getNullableDouble(rs, "roadConditionWeight");

                if (!isValidRoad(fromLocationId, toLocationId, distance_m, travelTime_min, roadConditionWeight)) {
                    rejected++;
                    logAudit(conn, "ROAD_REJECTED",
                            "Rejected road row with fromLocationId=" + fromLocationId
                                    + ", toLocationId=" + toLocationId + " (missing/invalid required field)");
                    continue;
                }

                roads.add(new Road(fromLocationId, toLocationId,
                        distance_m, travelTime_min, roadConditionWeight));
            }
        }

        logAudit(conn, "ROADS_LOADED",
                "Loaded " + roads.size() + " roads (" + rejected + " rejected)");
        return roads;
    }

    private static boolean isValidRoad(Integer fromLocationId, Integer toLocationId,
                                       Double distance_m, Double travelTime_min,
                                       Double roadConditionWeight) {
        return fromLocationId != null && fromLocationId > 0
                && toLocationId != null && toLocationId > 0
                && !fromLocationId.equals(toLocationId)
                && distance_m != null && distance_m >= 0
                && travelTime_min != null && travelTime_min >= 0
                && roadConditionWeight != null && roadConditionWeight >= 0;
    }

    // ---------- RESOURCES ----------

    public static List<Resource> loadResources(Connection conn) throws SQLException {
        List<Resource> resources = new ArrayList<>();
        int rejected = 0;

        String sql = "SELECT resourceId, type, name, homeLocationId, capacity, availabilityStatus FROM resources";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Integer resourceId = getNullableInt(rs, "resourceId");
                String type = rs.getString("type");
                String name = rs.getString("name");
                Integer homeLocationId = getNullableInt(rs, "homeLocationId");
                Integer capacity = getNullableInt(rs, "capacity");
                String availabilityStatus = rs.getString("availabilityStatus");

                if (!isValidResource(resourceId, type, name, homeLocationId, capacity, availabilityStatus)) {
                    rejected++;
                    logAudit(conn, "RESOURCE_REJECTED",
                            "Rejected resource row with resourceId=" + resourceId + " (missing/invalid required field)");
                    continue;
                }

                resources.add(new Resource(resourceId, type, name,
                        homeLocationId, capacity, availabilityStatus));
            }
        }

        logAudit(conn, "RESOURCES_LOADED",
                "Loaded " + resources.size() + " resources (" + rejected + " rejected)");
        return resources;
    }

    private static boolean isValidResource(Integer resourceId, String type, String name,
                                           Integer homeLocationId, Integer capacity,
                                           String availabilityStatus) {
        return resourceId != null && resourceId > 0
                && type != null && !type.trim().isEmpty()
                && name != null && !name.trim().isEmpty()
                && homeLocationId != null && homeLocationId > 0
                && capacity != null && capacity >= 0
                && availabilityStatus != null && !availabilityStatus.trim().isEmpty();
    }

    // ---------- SERVICE REQUESTS ----------

    public static List<ServiceRequest> loadServiceRequests(Connection conn) throws SQLException {
        List<ServiceRequest> requests = new ArrayList<>();
        int rejected = 0;

        String sql = "SELECT requestId, sourceLocationId, destinationLocationId, category, urgency, timeSubmitted, deadline, status, fineAmountGHS FROM service_requests";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Integer requestId = getNullableInt(rs, "requestId");
                Integer sourceLocationId = getNullableInt(rs, "sourceLocationId");
                Integer destinationLocationId = getNullableInt(rs, "destinationLocationId");
                String category = rs.getString("category");
                String urgency = rs.getString("urgency");
                String timeSubmitted = rs.getString("timeSubmitted");
                String deadline = rs.getString("deadline");
                String status = rs.getString("status");
                Double fineAmountGHS = getNullableDouble(rs, "fineAmountGHS");

                if (!isValidServiceRequest(requestId, sourceLocationId, destinationLocationId,
                        category, urgency, timeSubmitted, deadline, status, fineAmountGHS)) {
                    rejected++;
                    logAudit(conn, "SERVICE_REQUEST_REJECTED",
                            "Rejected service_request row with requestId=" + requestId + " (missing/invalid required field)");
                    continue;
                }

                requests.add(new ServiceRequest(requestId, sourceLocationId, destinationLocationId,
                        category, urgency, timeSubmitted, deadline, status, fineAmountGHS));
            }
        }

        logAudit(conn, "SERVICE_REQUESTS_LOADED",
                "Loaded " + requests.size() + " service requests (" + rejected + " rejected)");
        return requests;
    }

    private static boolean isValidServiceRequest(Integer requestId,
                                                 Integer sourceLocationId, Integer destinationLocationId,
                                                 String category, String urgency, String timeSubmitted,
                                                 String deadline, String status, Double fineAmountGHS) {
        return requestId != null && requestId > 0
                && sourceLocationId != null && sourceLocationId > 0
                && destinationLocationId != null && destinationLocationId > 0
                && category != null && !category.trim().isEmpty()
                && urgency != null && !urgency.trim().isEmpty()
                && timeSubmitted != null && !timeSubmitted.trim().isEmpty()
                && deadline != null && !deadline.trim().isEmpty()
                && status != null && !status.trim().isEmpty()
                && fineAmountGHS != null && fineAmountGHS >= 0;
    }

    private static Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static Double getNullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    // ---------- AUDIT LOGGING ----------

    private static void logAudit(Connection conn, String eventType, String description) throws SQLException {
        String sql = "INSERT INTO audit_events (eventType, description, timestamp) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, eventType);
            stmt.setString(2, description);
            stmt.setString(3, LocalDateTime.now().toString());
            stmt.executeUpdate();
        }
    }

    // ---------- TEST / DEMO ----------

    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Location> locations = loadLocations(conn);
            List<Road> roads = loadRoads(conn);
            List<Resource> resources = loadResources(conn);
            List<ServiceRequest> serviceRequests = loadServiceRequests(conn);

            System.out.println("locations: " + locations.size() + " rows");
            System.out.println("roads: " + roads.size() + " rows");
            System.out.println("resources: " + resources.size() + " rows");
            System.out.println("service_requests: " + serviceRequests.size() + " rows");

        } catch (SQLException e) {
            System.err.println("Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
