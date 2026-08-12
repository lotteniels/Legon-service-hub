package com.campushub.db;

import com.campushub.model.AuditEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditEventRepository {

    public void saveEvent(AuditEvent event) throws SQLException {
        String sql = "INSERT INTO audit_events (eventType, description, timestamp) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event.getEventType());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getTimestamp());
            ps.executeUpdate();
        }
    }

    public List<AuditEvent> getAllEvents() throws SQLException {
        List<AuditEvent> list = new ArrayList<>();
        String sql = "SELECT eventId, eventType, description, timestamp FROM audit_events ORDER BY eventId DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new AuditEvent(
                        rs.getInt("eventId"),
                        rs.getString("eventType"),
                        rs.getString("description"),
                        rs.getString("timestamp")
                ));
            }
        }
        return list;
    }
}
