package testing;
import javax.swing.SwingUtilities;
import javafile.Scene;
public class painterBrowser {
	public static void main(String[]args) {
		SwingUtilities.invokeLater(()-> {
			Scene scene=new Scene();
			scene.browserMode(args.length==0?null:args[0]);
		});
	}
}