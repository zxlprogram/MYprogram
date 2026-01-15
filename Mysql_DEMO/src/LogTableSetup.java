import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LogTableSetup {

    public static Connection connect(String URL, String USER, String PASSWORD) throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void writeLog(Connection conn, LogEntry log) {
    		String sql="INSERT INTO system_log ("
    		+String.join(",",LogEntry.dataFormatNote)
    		+") VALUES ("
    		+String.join(",", Collections.nCopies(LogEntry.dataFormatNote.length, "?"))
    		+")";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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

    public static List<LogEntry> showInfo(Connection conn) {
        List<LogEntry> logs = new ArrayList<>();
        String query = "SELECT * FROM system_log ORDER BY log_time DESC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                logs.add(LogEntry.format(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    public static void deleteLog(Connection conn, int userId) {
        if (userId > 0) {
            String sql = "DELETE FROM system_log WHERE user_id=?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                int rows = pstmt.executeUpdate();
                System.out.println("deleted " + rows + " data, user_id=" + userId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "DELETE FROM system_log";
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                System.out.println("deleted all data");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        Connection conn = connect(
                "jdbc:mysql://localhost:3306/system_log_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                "root",
                "daniel0912282575"
        );
     
        deleteLog(conn, 0);

        LogEntry log1 = new LogEntry("INFO", "SimpleLogDemo", "this is a testing message.", 1, "{\"ip\":\"127.0.0.1\"}");
        LogEntry log2 = new LogEntry("ERROR", "SimpleLogDemo", "this is an error message", 2, "{\"ip\":\"192.168.1.10\"}");

        writeLog(conn, log1);
        writeLog(conn, log2);

        for (LogEntry log : showInfo(conn)) {
        		System.out.println(log);
        }

        deleteLog(conn, 1);

        System.out.println("deleted data which user_id=1:");
        for (LogEntry log : showInfo(conn)) {
            System.out.println(log);
        }
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
    public static String[] dataFormatNote= {"log_level","source","message","user_id","extra"};
    public String getLevel() { return level; }
    public String getSource() { return source; }
    public String getMessage() { return message; }
    public Integer getUserId() { return userId; }
    public String getExtra() { return extra; }
    

    public Object[] getParams() {
        return new Object[]{ level, source, message, userId, extra };
    }
    
    public String toString() {
        return "user_id="+getUserId()+", level="+getLevel()+", source="+getSource()+", message="+getMessage()+", extra="+getExtra();
    }
}
