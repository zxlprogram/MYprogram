import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

public class Main {
	public static void main(String[]args) {
		MoodleServer s=new MoodleServer();
		Task t = new TFTask(s, "question 1", "1+1=2", true);
		MoodleUser stu;
		try {
			stu = new MoodleUser(s.getBaseServer(),"student",123456,new HashSet<PERMISSION>(Arrays.asList(
				PERMISSION.READ_TASK,
				PERMISSION.SUBMIT_ANS
				)));
		MoodleUser prof=new MoodleUser(s.getBaseServer(),"professor",12346,new HashSet<PERMISSION>(Arrays.asList(
				PERMISSION.READ_TASK,
				PERMISSION.EDIT_TASK,
				PERMISSION.SUBMIT_ANS
		)));
		System.out.println(prof.getBaseUser().getID());
		s.register(prof.getBaseUser(),new StringPassword("abc"));
		s.signIn(prof.getBaseUser(),new StringPassword("abc"));
		s.register(stu.getBaseUser(),new StringPassword("0000"));
		s.signIn(stu.getBaseUser(),new StringPassword("0000"));
		prof.submitTask(t);
		prof.doTask(t, false);
		stu.doTask(t, true);
		} catch (IdRepeatitiveException e) {
			e.printStackTrace();
		}
	}
}