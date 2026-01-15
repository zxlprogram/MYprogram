import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TableManager {
	Connection connected;
    public Connection connect(String URL, String USER, String PASSWORD) throws SQLException {
    		this.connected=DriverManager.getConnection(URL,USER,PASSWORD);
        return connected;
    }
    
    public void registered(String username,String password) {
		try (Connection rootConn = java.sql.DriverManager.getConnection(
		        "jdbc:mysql://localhost:3306/system_log_db?useSSL=false&serverTimezone=UTC",
		        "root", "daniel0912282575");
		     Statement stmt = rootConn.createStatement()) {
		    stmt.executeUpdate("CREATE USER '"+username+"'@'localhost' IDENTIFIED BY '"+password+"'");
		    stmt.executeUpdate("GRANT INSERT ON system_log_db.system_log TO '"+username+"'@'localhost'");
		    stmt.executeUpdate("FLUSH PRIVILEGES");
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
    }

    public void writeLog(LogEntry log) {
    		String sql="INSERT INTO system_log ("
    		+String.join(",",LogEntry.dataFormatNote)
    		+") VALUES ("
    		+String.join(",", Collections.nCopies(LogEntry.dataFormatNote.length, "?"))
    		+")";
        try (PreparedStatement pstmt = connected.prepareStatement(sql)) {
            Object[] params = log.getParams();
            for (int i = 0; i < params.length; i++) {
                if (params[i] != null) {
                    pstmt.setObject(i + 1, params[i]);
                } else {
                    pstmt.setNull(i + 1, java.sql.Types.INTEGER);
                }
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<LogEntry> showInfo() {
        List<LogEntry> logs = new ArrayList<>();
        String query = "SELECT * FROM system_log ORDER BY log_time DESC";

        try (Statement stmt = connected.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                logs.add(LogEntry.format(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    public void deleteLog(int userId) {
        if (userId > 0) {
            String sql = "DELETE FROM system_log WHERE user_id=?";
            try (PreparedStatement pstmt = connected.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "DELETE FROM system_log";
            try (Statement stmt = connected.createStatement()) {
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
	public Connection getConnection() {
		return connected;
	}
}
class LogEntry {
    private String level;
    private String source;
    private String message;
    private Integer userId;
    private String extra;

    public LogEntry(String level, String source, String message, Integer userId, String extra) {
        this.level = level;
        this.source = source;
        this.message = message;
        this.userId = userId;
        this.extra = extra;
    }

    public static final LogEntry format(ResultSet rs) throws SQLException {
        return new LogEntry(
                rs.getString("log_level"),
                rs.getString("source"),
                rs.getString("message"),
                (Integer) rs.getObject("user_id"),
                rs.getString("extra")
        );
    }
    public Object[] getParams() {
        return new Object[]{ level, source, message, userId, extra };
    }
    
    public String toString() {
        return "user_id="+getUserId()+", level="+getLevel()+", source="+getSource()+", message="+getMessage()+", extra="+getExtra();
    }
    
    public final static String[] dataFormatNote= {"log_level","source","message","user_id","extra"};
    public String getLevel() { return level; }
    public String getSource() { return source; }
    public String getMessage() { return message; }
    public Integer getUserId() { return userId; }
    public String getExtra() { return extra; }
}
/*
 * SQL:
 * CREATE TABLE system_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    log_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    log_level VARCHAR(10) NOT NULL,
    source VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    user_id INT NULL,
    extra JSON NULL
);

register method only give the insert permission

 */
