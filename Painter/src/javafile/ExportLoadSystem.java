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
public class ExportLoadSystem {
	private Scene scene;
	public ExportLoadSystem(Scene scene) {
		this.scene=scene;
	}
	public void ExportFlie(String path) {
		List<PainterObj>data=this.scene.getAllSurface();
		try {
			File file=new File(path);
			FileWriter writer=new FileWriter(file);
			writer.write(this.scene.getScale()+" "+this.scene.getOffsetX()+" "+this.scene.getOffsetY()+"\n");
			for(PainterObj s:data) {
				writer.write(s.toString()+"\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * @see Surface#toString()
	*/
	private void loading(BufferedReader reader) {
		try {
			this.scene.getAllSurface().clear();
			List<PainterObj>returnList=new ArrayList<>();
			String line=reader.readLine();
			String[]view=line.split(" ");
			this.scene.setScale(Double.parseDouble(view[0]));
			this.scene.setOffsetX(Double.parseDouble(view[1]));
			this.scene.setOffsetY(Double.parseDouble(view[2]));
			while((line=reader.readLine())!=null) {
				String []array=line.split(" ");
				PainterObj surface = new PainterObj();
				try {
					surface = this.scene.getObjTranslator().get(array[0]).getDeclaredConstructor().newInstance();
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException| InvocationTargetException | NoSuchMethodException e) {
					e.printStackTrace();
				}
				for(int i=1;i<array.length-5;i+=2) {
					Double X=Double.parseDouble(array[i]);
					Double Y=Double.parseDouble(array[i+1]);
					surface.addPoint(new Point(X,Y,surface));
				}
				Double R=Double.parseDouble(array[array.length-3]);
				Double G=Double.parseDouble(array[array.length-2]);
				Double B=Double.parseDouble(array[array.length-1]);
				surface.setColor(R,G,B);
				returnList.add(surface);
			}
			this.scene.setAllSurface(returnList);
			this.scene.getNote().saveInfo(this.scene.getAllSurface(),this.scene.getScale(),this.scene.getOffsetX(),this.scene.getOffsetY());
			reader.close();
		}catch(IOException e) {}
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
