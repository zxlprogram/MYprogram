package testing;
import javafile.Scene;
public class painterBrowser {
public static void main(String[]args) {
	Scene scene=new Scene();
	scene.browserMode(args.length==0?null:args[0]);
}
}