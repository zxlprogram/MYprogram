package javafile;
/**
 *we should take the value and call java.awt.Color when coloring, it just a pack
 */
public class Color {
	private double R,G,B;
	public Color(double r,double g,double b) {
		this.R=r;
		this.G=g;
		this.B=b;
	}
	public double getR() {
		return this.R;
	}
	public double getG() {
		return this.G;
	}
	public double getB() {
		return this.B;
	}
	public void setColor(double r,double g,double b) {
		this.R=r;
		this.G=g;
		this.B=b;
	}
	@Override
	public String toString() {
		return R+" "+G+" "+B;
	}
}