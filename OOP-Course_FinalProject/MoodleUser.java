import java.util.HashSet;
import java.util.Set;

public class MoodleUser {
	private BaseUser baseUser;
    private Set<PERMISSION>permissions=new HashSet<>();
    public MoodleUser(BaseServer server, String name, int ID,Set<PERMISSION>permission) throws IdRepeatitiveException {
        baseUser=new BaseUser(server, name, ID);
        this.permissions=permission;
    }
    public Set<PERMISSION>getPermissions(){
        return this.permissions;
    }
    public BaseUser getBaseUser() {
    		return baseUser;
    }
	public void submitTask(Task t) {
		if(getBaseUser().getServer().ask(this.getBaseUser())) {
			t.requestAddTask(this);
		}
	}
	public void doTask(Task t,Object o) {
		if(getBaseUser().getServer().ask(this.getBaseUser())) {
			t.requestDoTask(this, o);
		}
	}
	@Override
	public String toString() {
		return this.getBaseUser().toString();
	}
    @Override
    public boolean equals(Object o) {
    		if(this==o)return true;
    		if(!(o instanceof MoodleUser))return false;
    		MoodleUser m=(MoodleUser)o;
    		return this.getBaseUser().equals(m.getBaseUser());
    }
    @Override
    public int hashCode() {
        return this.getBaseUser().hashCode();
    }
}