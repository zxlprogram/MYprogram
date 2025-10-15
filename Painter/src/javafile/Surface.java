package javafile;
import java.util.ArrayList;
import java.util.List;

/**
 * we have two initial shape: quad and triangle
 * if use getColor but color is null, it will give a color red to the surface
 * the structure is: point & color -> surface
 * 
 * add the remove-point method
 */
public class Surface {
	public static Surface QUAD() {
		Surface quad = new Surface();
		quad.addPoint(new Point(0,0,quad));
		quad.addPoint(new Point(0,1,quad));
		quad.addPoint(new Point(1,1,quad));
		quad.addPoint(new Point(1,0,quad));
		return quad;
	}
	public static Surface TRIANGLE() {
		Surface tria=new Surface();
		tria.addPoint(new Point(0,0,tria));
		tria.addPoint(new Point(0,1,tria));
		tria.addPoint(new Point(1,1,tria));
		return tria;
	}
	private Color color=new Color(1,0,0);
	private List<Point>Edge=new ArrayList<>();
	public void setColor(Color color) {
		this.color=color;
	}
	public void setColor(double r,double g,double b){
		this.color=new Color(r,g,b);
	}
	public void moveX(double x) {
		for(Point p:Edge)
			p.setX(p.getX()+x);
	}
	public void moveY(double y) {
		for(Point p:Edge)
			p.setY(p.getY()+y);
	}
	public Color getColor() {
		if(this.color==null)
			this.color=new Color(1,0,0);
		return this.color;
	}
	public Surface(Point...point) {
		for(Point p:point)
			this.Edge.add(p);
	}
	public void addPoint(Point...p) {
		if(p==null)
			return;
		for(Point pp:p)
			this.Edge.add(pp);
	}
	public void addPoint(double a,double b) {
		this.Edge.add(new Point(a,b,this));
	}
	public Point[] getEdge() {
		return Edge.toArray(new Point[0]);
	}
	public void removePoint(Point point,Scene scene) {
		this.Edge.remove(point);
		if(this.getEdge().length<3) {
			scene.removeSurface(this);
		}
	}
	public void removePoint(int index) {
		this.Edge.remove(index);
	}
}