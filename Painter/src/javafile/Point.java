package javafile;
public class Point {
	private double X,Y;
	public Point(double x,double y) {
		this.X=x;
		this.Y=y;
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
}