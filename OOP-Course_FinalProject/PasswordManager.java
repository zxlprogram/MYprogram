import java.util.HashMap;
import java.util.Map;

class PasswordManager implements Logger{
    private Map<BaseUser,Password>passwordMap=new HashMap<>();
    public void addPassword(BaseUser user,Password password) {
    		this.passwordMap.put(user, password);
    }
    public boolean check(BaseUser user,Password password) {
    		if(password.equals(passwordMap.get(user))) {
    			log(user+": correct password");
    			return true;
    		}
    		log(user+": wrong password");
    		return false;
    }
   @Override
    public void showInfo() {
    		System.out.println("passwordMap: "+this.passwordMap);
    }
	@Override
	public void log(String message) {
		System.out.println("[password] "+message);
	}
}