package com.campushub.db;

import com.campushub.model.Resource;
import java.sql.*;
import com.campushub.structures.linear.DynamicArray;

public class ResourceRepository {

    public DynamicArray<Resource> getAllResources() throws SQLException {
        DynamicArray<Resource> list = new DynamicArray<>();
        String sql = "SELECT resourceId, type, name, homeLocationId, capacity, availabilityStatus FROM resources";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Resource(
                        rs.getInt("resourceId"),
                        rs.getString("type"),
                        rs.getString("name"),
                        rs.getInt("homeLocationId"),
                        rs.getInt("capacity"),
                        rs.getString("availabilityStatus")
                ));
            }
        }
        return list;
    }

    public void setAvailabilityStatus(int resourceId, String status) throws SQLException {
        String sql = "UPDATE resources SET availabilityStatus = ? WHERE resourceId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, resourceId);
            ps.executeUpdate();
        }
    }
}
