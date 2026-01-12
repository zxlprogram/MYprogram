import java.util.HashSet;
import java.util.Set;

public class BaseUser implements Account{
    private static Set<Integer>IDpool=new HashSet<>();
    private BaseServer server;
    private String name;
    private int ID;
    public BaseUser(BaseServer server,String name,int ID) throws IdRepeatitiveException {
        if(!IDpool.contains(ID)) {
            this.ID=ID;
            this.name=name;
            this.server=server;
            IDpool.add(ID);
        }
        else
            throw new IdRepeatitiveException("the ID is repeatitive (ID:"+ID+")");
    }
    public int getID() {
    		return ID;
    }
    public BaseServer getServer() {
        return server;
    }
    @Override
    public void signIn() {
        server.signIn(this);
    }
    @Override
    public void signOut() {
        if(server.ask(this))
            server.signOut(this);
    }
    @Override
    public String toString() {
        return this.name+"/"+this.ID;
    }
    @Override
    public boolean equals(Object o) {
        if (this==o) return true;
        if (!(o instanceof BaseUser))return false;
        BaseUser u=(BaseUser)o;
        return ID==u.ID;
    }
    @Override
    public int hashCode() {
        return Integer.hashCode(ID);
    }
}