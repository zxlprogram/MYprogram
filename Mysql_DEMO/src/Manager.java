import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Manager{
	private static TableManager table=new TableManager();
	public Connection connect(String URL, String USER, String PASSWORD) throws SQLException {
		return table.connect(URL, USER, PASSWORD);
	}
	public void writeLog(LogEntry log) {
		table.writeLog(log);	
	}
	public static void deleteLog(int id) {
		table.deleteLog(id);
	}
	public static List<LogEntry> showInfo() {
		return table.showInfo();
	}
	public static void main(String[]args) throws SQLException {
		Manager s=new Manager();
		s.connect(
                "jdbc:mysql://localhost:3306/system_log_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                "root",
                "daniel0912282575"
        );
		for(Object o:showInfo()) {
			System.out.println(o);
		}
	}
}
