import java.util.HashMap;
import java.util.Map;

public abstract class Task {
	private String title;
	private String context;
	private MoodleServer server;
	private Object correctAnswer;
	private Map<MoodleUser,Object> userAnswer=new HashMap<>();
	public Task(MoodleServer server,String t,String context,Object correctAnswer) {
		this.server=server;
		this.title=t;
		this.context=context;
		this.correctAnswer=correctAnswer;
	}
	public MoodleServer getServer() {
		return server;
	}
	public String getTitle() {
		return title;
	}
	public String getContext() {
		return context;
	}
	public abstract void requestDoTask(MoodleUser mdu,Object o);
	public abstract void requestAddTask(MoodleUser mdu);
	public abstract String result(MoodleUser mdu);
	public abstract boolean checkAnswer(MoodleUser mdu);
	public Object getCorrectAnswer() {
		return correctAnswer;
	}
	@Override
	public String toString() {
		return title;
	}
	public Map<MoodleUser,Object>getUserAnswer() {
		return userAnswer;
	}
}