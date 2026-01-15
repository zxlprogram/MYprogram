
public abstract class ABSuser {
	private TableManager table;
	public ABSuser(TableManager t) {
		table=t;
	}
	public void registered(String username,String password) {
		table.registered(username, password);
	}
}