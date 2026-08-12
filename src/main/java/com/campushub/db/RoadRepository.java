package com.campushub.db;

import com.campushub.model.Road;
import java.sql.*;
import com.campushub.structures.linear.DynamicArray;

public class RoadRepository {

    public DynamicArray<Road> getAllRoads() throws SQLException {
        DynamicArray<Road> list = new DynamicArray<>();
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
