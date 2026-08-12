package com.campushub.db;

import com.campushub.model.ServiceRequest;
import java.sql.*;
import com.campushub.structures.linear.DynamicArray;

public class RequestRepository {

    public DynamicArray<ServiceRequest> getAllRequests() throws SQLException {
        DynamicArray<ServiceRequest> list = new DynamicArray<>();
        String sql = "SELECT requestId, sourceLocationId, destinationLocationId, category, urgency, timeSubmitted, deadline, status, fineAmountGHS FROM service_requests";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ServiceRequest(
                        rs.getInt("requestId"),
                        rs.getInt("sourceLocationId"),
                        rs.getInt("destinationLocationId"),
                        rs.getString("category"),
                        rs.getString("urgency"),
                        rs.getString("timeSubmitted"),
                        rs.getString("deadline"),
                        rs.getString("status"),
                        rs.getDouble("fineAmountGHS")
                ));
            }
        }
        return list;
    }

    public void updateStatus(int requestId, String newStatus) throws SQLException {
        String sql = "UPDATE service_requests SET status = ? WHERE requestId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, requestId);
            ps.executeUpdate();
        }
    }
}
