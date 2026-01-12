import java.util.HashSet;
import java.util.Set;

public class BaseServer implements AccountManager,Logger {
    private Set<BaseUser>accountPool=new HashSet<>();
    private Set<BaseUser>onlinePool=new HashSet<>();
    @Override
    public void register(BaseUser user) {
        this.accountPool.add(user);
        log(user+" registered!");
    }
    @Override
    public void signOut(BaseUser user) {
        this.onlinePool.remove(user);
        log(user+" logout!");
    }
    @Override
    public void signIn(BaseUser user) {
        if(accountPool.contains(user)) {
            this.onlinePool.add(user);
            log(user+ "login!");
        }
        else {
            log(user+ "not registered!");
        }
    }
    @Override
    public void showInfo() {
        System.out.println("AccountPool: "+accountPool);
        System.out.println("onlinePool: "+onlinePool);
    }
    @Override
    public void log(String message) {
        System.out.println(message);
    }
    public boolean ask(BaseUser usr) {
        if(!onlinePool.contains(usr))
            log(usr+ " , you didn't sign in or no permission");
        else
            log(usr+" is in AccountPool");
        return onlinePool.contains(usr);
    }
}