import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class LogTableSetup {

    // 連線資料庫資訊
    private static final String URL = "jdbc:mysql://localhost:3306/system_log_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "daniel0912282575";
    
    public static void showInfo() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT * FROM system_log ORDER BY log_time DESC";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                System.out.println("\n目前 Log 資料：");
                while (rs.next()) {
                    System.out.printf("id=%d, time=%s, level=%s, source=%s, message=%s, user_id=%s, extra=%s%n",
                            rs.getInt("id"),
                            rs.getTimestamp("log_time"),
                            rs.getString("log_level"),
                            rs.getString("source"),
                            rs.getString("message"),
                            rs.getObject("user_id"),
                            rs.getString("extra"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
       /* try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            writeLog(conn, "INFO", "SimpleLogDemo", "這是一條測試訊息", 1, "{\"ip\":\"127.0.0.1\"}");
            writeLog(conn, "ERROR", "SimpleLogDemo", "這是一條錯誤訊息", 2, "{\"ip\":\"192.168.1.10\"}");
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        showInfo();
    }
    //https://www.youtube.com/watch?v=LN0iWapPUBI
    private static void writeLog(Connection conn, String level, String source, String message, Integer userId, String extra) {
        String sql = "INSERT INTO system_log (log_level, source, message, user_id, extra) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, level);
            pstmt.setString(2, source);
            pstmt.setString(3, message);
            if (userId != null) {
                pstmt.setInt(4, userId);
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }
            pstmt.setString(5, extra);
            pstmt.executeUpdate();
            System.out.println("Log 已寫入: " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
