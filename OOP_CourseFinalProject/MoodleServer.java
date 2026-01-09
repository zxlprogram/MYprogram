import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MoodleServer {
	private List<Task>homeworkList=new ArrayList<>();
	private PasswordManager password=new PasswordManager();
	private BaseServer baseServer=new BaseServer();
	public MoodleServer() {
		log("welcome to moodle");
	}
	public void addTask(Task t,MoodleUser mdu) {
		if(getBaseServer().ask(mdu.getBaseUser())) {
			if(mdu.getPermissions().contains(PERMISSION.EDIT_TASK)) {
				homeworkList.add(t);
				log(mdu+" add the task:" +t);
			}
			else
				log(mdu+" have no permission to add task");
		}
	}
	public void removeTask(Task t) {
		homeworkList.remove(t);
	}
	public void log(String message) {
		System.out.println("[moodle]"+message);
	}
	public List<Task>getHomeWork(){
		return Collections.unmodifiableList(homeworkList);
	}
	public void submitAnswer(MoodleUser mdu,Task task,Object answer) {
		if(getBaseServer().ask(mdu.getBaseUser())) {
			if(getHomeWork().contains(task))
				if(mdu.getPermissions().contains(PERMISSION.SUBMIT_ANS)) {
					task.getUserAnswer().put(mdu,answer);
					log("student "+mdu+" done the homework "+task+" , the result is "+task.result(mdu));
				}
				else
					log(mdu+" have no permission to submit answer");
			else
				log(task+" is not in homework");
		}
			
	}
	public BaseServer getBaseServer() {
		return this.baseServer;
	}
	public void register(BaseUser b,Password i) {
		this.baseServer.register(b);
		this.password.addPassword(b, i);
	}
	public void signIn(BaseUser b, Password i) {
		if(this.password.check(b,i))
			this.baseServer.signIn(b);
	}
	public void signOut(BaseUser b) {
		if(this.baseServer.ask(b))
			this.baseServer.signOut(b);
	}
	public void showInfo() {
		this.baseServer.showInfo();
		this.password.showInfo();
		System.out.println("homeworkList: "+this.homeworkList);
	}
}