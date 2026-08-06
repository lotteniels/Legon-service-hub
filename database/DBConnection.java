import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Handles the JDBC connection to the service_hub SQLite database.
 * Adjust URL if your .db file lives somewhere other than the project root.
 */
public class DBConnection {

    private static final String URL = "jdbc:sqlite:service_hub.db";

    /**
     * Opens and returns a new connection to the database.
     * Caller is responsible for closing it (use try-with-resources).
     */
    public static Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found on classpath.", e);
        }
        return DriverManager.getConnection(URL);
    }
}
