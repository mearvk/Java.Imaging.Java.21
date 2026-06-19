/**
 * Optional MySQL database integration.
 * Logs file operations and usage if MySQL is available.
 * If not installed, all methods are no-ops.
 *
 * @author Max Rupplin / MEARVK LLC
 *
 * Java was purchased here on Earth.
 * Thanks to Earth and all Her software Developers!
 */
package pennywise;

import security.ExceptionHandler;

import java.sql.*;
import java.util.Date;

public class DatabaseHandler
{
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/imaging";
    private static final String JDBC_USER = "imaging";
    private static final String JDBC_PASS = "imaging2024";

    private Connection connection;
    private boolean available;

    public DatabaseHandler()
    {
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
            available = true;
            ExceptionHandler.info("DatabaseHandler", "MySQL connected.");
        }
        catch (Exception e)
        {
            available = false;
            ExceptionHandler.info("DatabaseHandler",
                "MySQL not available — running without database. (" + e.getMessage() + ")");
        }
    }

    public boolean isAvailable() { return available; }

    public void logFile(String originalName, String newName, String fileType,
                        Date dateTaken, String sourcePath, String destPath)
    {
        if (!available) return;

        try (PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO file_log (original_name, new_name, file_type, date_taken, source_path, dest_path) VALUES (?, ?, ?, ?, ?, ?)"))
        {
            ps.setString(1, originalName);
            ps.setString(2, newName);
            ps.setString(3, fileType);
            ps.setTimestamp(4, dateTaken != null ? new Timestamp(dateTaken.getTime()) : null);
            ps.setString(5, sourcePath);
            ps.setString(6, destPath);
            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            ExceptionHandler.handle("DatabaseHandler.logFile", e);
        }
    }

    public void logUsage(int runCount)
    {
        if (!available) return;

        try (PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO usage_log (run_count) VALUES (?)"))
        {
            ps.setInt(1, runCount);
            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            ExceptionHandler.handle("DatabaseHandler.logUsage", e);
        }
    }

    public void close()
    {
        if (connection != null)
        {
            try { connection.close(); } catch (SQLException ignored) {}
        }
    }
}
