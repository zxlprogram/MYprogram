package javafile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
/**
 * 
 * it's used to initialize logo loading and load file, also can ctrl+S to save the file on desktop
 * 
 **/
class Exporter {
	private ExportLoadSystem sys;
	public Exporter(ExportLoadSystem sys) {
		this.sys=sys;
	}
	public void ExportFlie(String path) {
		final List<PainterObj>data=sys.getScene().getAllPainterObj();
		try {
			File file=new File(path);
			FileWriter writer=new FileWriter(file);
			writer.write(sys.getScene().getScale()+" "+sys.getScene().getOffsetX()+" "+sys.getScene().getOffsetY()+"\n");
			for(PainterObj s:data) {
				writer.write(s.toString()+"\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
class Loader {
	private ExportLoadSystem sys;
	public Loader(ExportLoadSystem sys) {
		this.sys=sys;
	}
	private void loading(BufferedReader reader) {
		try {
			sys.getScene().removeAll();
			List<PainterObj>returnList=new ArrayList<>();
			
			String line=reader.readLine();
			String[]view=line.split(" ");
			sys.getScene().setScale(Double.parseDouble(view[0]));
			sys.getScene().setOffsetX(Double.parseDouble(view[1]));
			sys.getScene().setOffsetY(Double.parseDouble(view[2]));
			
			while((line=reader.readLine())!=null) {
				String []array=line.split(" ");
				PainterObj painterObj = new PainterObj(this.sys.getScene());
				try {
					painterObj = sys.getScene().getObjTranslator().get(array[0]).getDeclaredConstructor(Scene.class).newInstance(sys.getScene());
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException| InvocationTargetException | NoSuchMethodException e) {
					e.printStackTrace();
				}
				painterObj.loadfile(array);
				returnList.add(painterObj);
			}
			
			sys.getScene().setAllPainterObj(returnList);
			sys.getScene().getNote().saveInfo();
			reader.close();
		}catch(IOException e) {e.printStackTrace();}
	}
	public void loadFile(String path) {
		try(BufferedReader reader=new BufferedReader(new FileReader(path))) {
			loading(reader);
		} catch (IOException e) {}
		
	}
	public void loadFile(URL url) {// initialize loading
		try(BufferedReader reader=new BufferedReader(new InputStreamReader(url.openStream()))) {
			loading(reader);
		}
		catch(IOException e) {}
	}
}
public class ExportLoadSystem {
	private Scene scene;
	private Exporter exporter;
	private Loader loader;
	public Scene getScene() {
		return this.scene;
	}
	public ExportLoadSystem(Scene scene) {
		this.scene=scene;
		this.exporter=new Exporter(this);
		this.loader=new Loader(this);
	}
	public Exporter getExporter() {
		return this.exporter;
	}
	public Loader getLoader() {
		return this.loader;
	}
	/**
	 * @see Surface#toString()
	*/
}