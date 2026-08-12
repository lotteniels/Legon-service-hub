package com.campushub.db;

import com.campushub.model.Road;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoadRepository {

    public List<Road> getAllRoads() throws SQLException {
        List<Road> list = new ArrayList<>();
        String sql = "SELECT fromLocationId, toLocationId, distance_m, travelTime_min, roadConditionWeight FROM roads";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Road(
                        rs.getInt("fromLocationId"),
                        rs.getInt("toLocationId"),
                        rs.getDouble("distance_m"),
                        rs.getDouble("travelTime_min"),
                        rs.getDouble("roadConditionWeight")
                ));
            }
        }
        return list;
    }
}
