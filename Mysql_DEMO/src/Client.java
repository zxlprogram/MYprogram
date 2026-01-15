import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
class IdRepeatitiveException extends Exception {
	private static final long serialVersionUID = 1L;
	public IdRepeatitiveException(String msg) {
		super(msg);
	}
}
public class Client extends ABSuser {
	private static Set<Integer>idSet=new HashSet<>();
	private int user_id;
	private static TableManager table=new TableManager();
	@Override
	public void registered(String username,String password) {
		super.registered(username, password);
	}
	
	public Connection connect(String URL, String USER, String PASSWORD) throws SQLException {
		return table.connect(URL, USER, PASSWORD);
	}
	public static void writeLog(LogEntry log) {
		table.writeLog(log);	
	}
	public Client(TableManager table,int user_id) throws IdRepeatitiveException {
		super(table);
		if(!idSet.contains(user_id)) {
			this.user_id=user_id;
			idSet.add(user_id);
		}
		else
			throw new IdRepeatitiveException("id: "+user_id+" repeated");
	}
	public static void main(String[]args) throws SQLException, IdRepeatitiveException {
		Client s=new Client(table,1);
		//Client has-a account
		//registered("loguser","userpassword");
		s.connect(
                "jdbc:mysql://localhost:3306/system_log_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                "loguser",
                "userpassword"
        );
		writeLog(new LogEntry("MESSAGE","user","this is a testing message.",s.user_id,"{\"ip\":\"192.168.1.10\"}"));
	}
}
