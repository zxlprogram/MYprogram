package javafile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class ExportLoadSystem {
	private Scene scene;
	public ExportLoadSystem(Scene scene) {
		this.scene=scene;
	}
	public void ExportFlie(String path) {
		List<Surface>data=this.scene.getAllSurface();
		try {
			File file=new File(path);
			FileWriter writer=new FileWriter(file);
			writer.write(this.scene.getScale()+" "+this.scene.getOffsetX()+" "+this.scene.getOffsetY()+"\n");
			for(Surface s:data) {
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
	public void loadFile(String path) {
		try {
			this.scene.getAllSurface().clear();
			List<Surface>returnList=new ArrayList<>();
			FileReader file=new FileReader(path);
			BufferedReader reader=new BufferedReader(file);
			String line=reader.readLine();
			String[]view=line.split(" ");
			this.scene.setScale(Double.parseDouble(view[0]));
			this.scene.setOffsetX(Double.parseDouble(view[1]));
			this.scene.setOffsetY(Double.parseDouble(view[2]));
			while((line=reader.readLine())!=null) {
				Surface surface=new Surface();
				String []array=line.split(" ");
				
				for(int i=0;i<array.length-5;i+=2) {
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
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
