package testing;
import javafile.Scene;
public class Browser {
public static void main(String[]args) {
	String path=args[0];
	Scene scene=new Scene();
	scene.browserMode(path);
}
}