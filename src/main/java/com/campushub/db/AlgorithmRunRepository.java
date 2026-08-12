package com.campushub.db;

import com.campushub.model.AlgorithmRun;
import java.sql.*;
import com.campushub.structures.linear.DynamicArray;

public class AlgorithmRunRepository {

    public void saveRun(AlgorithmRun run) throws SQLException {
        String sql = "INSERT INTO algorithm_runs (algorithmName, inputSize, timeNs, memoryKb, dateRun) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, run.getAlgorithmName());
            ps.setInt(2, run.getInputSize());
            ps.setInt(3, run.getTimeNs());
            ps.setInt(4, run.getMemoryKb());
            ps.setString(5, run.getDateRun());
            ps.executeUpdate();
        }
    }

    public DynamicArray<AlgorithmRun> getAllRuns() throws SQLException {
        DynamicArray<AlgorithmRun> list = new DynamicArray<>();
        String sql = "SELECT runId, algorithmName, inputSize, timeNs, memoryKb, dateRun FROM algorithm_runs";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new AlgorithmRun(
                        rs.getInt("runId"),
                        rs.getString("algorithmName"),
                        rs.getInt("inputSize"),
                        rs.getInt("timeNs"),
                        rs.getInt("memoryKb"),
                        rs.getString("dateRun")
                ));
            }
        }
        return list;
    }
}
