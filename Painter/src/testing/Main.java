package testing;
import javax.swing.SwingUtilities;

import javafile.Scene;
/**
 * we did it! we can just add the code instead fixed the old code to add the new feature
 * 
 * we have at least 1500 lines in this program
 */
public class Main {
	public static void main(String[]args) {
		SwingUtilities.invokeLater(()-> {
			Scene scene=new Scene();
			scene.execute();
		});
	}
}