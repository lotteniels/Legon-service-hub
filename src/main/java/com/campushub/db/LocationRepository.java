package com.campushub.db;

import com.campushub.model.Location;
import java.sql.*;
import com.campushub.structures.linear.DynamicArray;

public class LocationRepository {

    public DynamicArray<Location> getAllLocations() throws SQLException {
        DynamicArray<Location> list = new DynamicArray<>();
        String sql = "SELECT locationId, name, area, type, coordinates FROM locations";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Location(
                        rs.getInt("locationId"),
                        rs.getString("name"),
                        rs.getString("area"),
                        rs.getString("type"),
                        rs.getString("coordinates")
                ));
            }
        }
        return list;
    }

    public Location getLocationById(int id) throws SQLException {
        String sql = "SELECT locationId, name, area, type, coordinates FROM locations WHERE locationId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Location(
                            rs.getInt("locationId"),
                            rs.getString("name"),
                            rs.getString("area"),
                            rs.getString("type"),
                            rs.getString("coordinates")
                    );
                }
            }
        }
        return null;
    }
}
