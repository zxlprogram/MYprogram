/**
 * 
 * add the getSurface method, and all point should declare which surface did it belong
 */
package javafile;
public class Point {
	private boolean draggable=false;
	private double X,Y;
	private PainterObj painterObj;
	public Point(double x,double y,PainterObj quad) {
		this.X=x;
		this.Y=y;
		this.painterObj=quad;
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
	public PainterObj getSurface() {
		return this.painterObj;
	}
	@Override
	public String toString() {
		return "("+this.X+","+this.Y+")";
	}
	public void setDraggable(boolean b) {
		this.draggable=b;
	}
	public boolean draggable() {
		return this.draggable;
	}
}