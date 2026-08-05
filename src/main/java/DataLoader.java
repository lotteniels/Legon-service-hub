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
 *
 * NOTE: adjust the SQL column names below if they differ from your schema.sql.
 */
public class DataLoader {

    // ---------- LOCATIONS ----------

    public static List<Location> loadLocations(Connection conn) throws SQLException {
        List<Location> locations = new ArrayList<>();
        int rejected = 0;

        String sql = "SELECT location_id, name, type, latitude, longitude FROM locations";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("location_id");
                String name = rs.getString("name");
                String type = rs.getString("type");
                double lat = rs.getDouble("latitude");
                double lng = rs.getDouble("longitude");

                if (!isValidLocation(id, name)) {
                    rejected++;
                    logAudit(conn, "LOCATION_REJECTED",
                            "Rejected location row with id=" + id + " (missing/invalid required field)");
                    continue;
                }

                locations.add(new Location(id, name, type, lat, lng));
            }
        }

        logAudit(conn, "LOCATIONS_LOADED",
                "Loaded " + locations.size() + " locations (" + rejected + " rejected)");
        return locations;
    }

    private static boolean isValidLocation(int id, String name) {
        return id > 0 && name != null && !name.trim().isEmpty();
    }

    // ---------- ROADS ----------

    public static List<Road> loadRoads(Connection conn) throws SQLException {
        List<Road> roads = new ArrayList<>();
        int rejected = 0;

        String sql = "SELECT road_id, from_location_id, to_location_id, distance, walkable FROM roads";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("road_id");
                int from = rs.getInt("from_location_id");
                int to = rs.getInt("to_location_id");
                double distance = rs.getDouble("distance");
                boolean walkable = rs.getBoolean("walkable");

                if (!isValidRoad(id, from, to, distance)) {
                    rejected++;
                    logAudit(conn, "ROAD_REJECTED",
                            "Rejected road row with id=" + id + " (missing/invalid required field)");
                    continue;
                }

                roads.add(new Road(id, from, to, distance, walkable));
            }
        }

        logAudit(conn, "ROADS_LOADED",
                "Loaded " + roads.size() + " roads (" + rejected + " rejected)");
        return roads;
    }

    private static boolean isValidRoad(int id, int from, int to, double distance) {
        return id > 0 && from > 0 && to > 0 && from != to && distance >= 0;
    }

    // ---------- RESOURCES ----------

    public static List<Resource> loadResources(Connection conn) throws SQLException {
        List<Resource> resources = new ArrayList<>();
        int rejected = 0;

        String sql = "SELECT resource_id, name, category, location_id, available FROM resources";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("resource_id");
                String name = rs.getString("name");
                String category = rs.getString("category");
                int locationId = rs.getInt("location_id");
                boolean available = rs.getBoolean("available");

                if (!isValidResource(id, name, locationId)) {
                    rejected++;
                    logAudit(conn, "RESOURCE_REJECTED",
                            "Rejected resource row with id=" + id + " (missing/invalid required field)");
                    continue;
                }

                resources.add(new Resource(id, name, category, locationId, available));
            }
        }

        logAudit(conn, "RESOURCES_LOADED",
                "Loaded " + resources.size() + " resources (" + rejected + " rejected)");
        return resources;
    }

    private static boolean isValidResource(int id, String name, int locationId) {
        return id > 0 && name != null && !name.trim().isEmpty() && locationId > 0;
    }

    // ---------- SERVICE REQUESTS ----------

    public static List<ServiceRequest> loadServiceRequests(Connection conn) throws SQLException {
        List<ServiceRequest> requests = new ArrayList<>();
        int rejected = 0;

        String sql = "SELECT request_id, location_id, description, priority, status, timestamp FROM service_requests";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("request_id");
                int locationId = rs.getInt("location_id");
                String description = rs.getString("description");
                int priority = rs.getInt("priority");
                String status = rs.getString("status");
                String timestamp = rs.getString("timestamp");

                if (!isValidServiceRequest(id, locationId, priority, status)) {
                    rejected++;
                    logAudit(conn, "SERVICE_REQUEST_REJECTED",
                            "Rejected service_request row with id=" + id + " (missing/invalid required field)");
                    continue;
                }

                requests.add(new ServiceRequest(id, locationId, description, priority, status, timestamp));
            }
        }

        logAudit(conn, "SERVICE_REQUESTS_LOADED",
                "Loaded " + requests.size() + " service requests (" + rejected + " rejected)");
        return requests;
    }

    private static boolean isValidServiceRequest(int id, int locationId, int priority, String status) {
        return id > 0 && locationId > 0 && priority > 0
                && status != null && !status.trim().isEmpty();
    }

    // ---------- AUDIT LOGGING ----------

    /**
     * Inserts a row into audit_events. Adjust column names/types to match schema.sql
     * (expected columns here: event_type, description, event_time).
     */
    private static void logAudit(Connection conn, String eventType, String description) throws SQLException {
        String sql = "INSERT INTO audit_events (event_type, description, event_time) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, eventType);
            stmt.setString(2, description);
            stmt.setString(3, LocalDateTime.now().toString());
            stmt.executeUpdate();
        }
    }

    // ---------- TEST / DEMO ----------

    public static void main(String[] args) {
        try (Connection conn = DBConnection.connect()) {
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
