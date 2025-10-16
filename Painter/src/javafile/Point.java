/**
 * 
 * add the getSurface method, and all point should declare which surface did it belong
 */
package javafile;
public class Point {
	private double X,Y;
	private Surface surface;
	public Point(double x,double y,Surface surface) {
		this.X=x;
		this.Y=y;
		this.surface=surface;
	}
	public void setX(double x) {
		this.X=x;
	}
	public void setY(double y) {
		this.Y=y;
	}
	public double getX() {
		return this.X;
	}
	public double getY() {
		return this.Y;
	}
	public Surface getSurface() {
		return this.surface;
	}
	@Override
	public String toString() {
		return "("+this.X+","+this.Y+")";
	}
}