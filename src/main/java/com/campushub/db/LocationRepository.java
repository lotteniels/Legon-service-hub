package com.campushub.db;

import com.campushub.model.Location;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LocationRepository {

    public List<Location> getAllLocations() throws SQLException {
        List<Location> list = new ArrayList<>();
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
