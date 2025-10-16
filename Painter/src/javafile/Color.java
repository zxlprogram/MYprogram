package javafile;
/**
 * we allowed the input is bigger than 1 or smaller than 0, and the value will be change to the legal value 
 * 
 * we remove that allow now, it's possible to make the bug
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
	protected Object clone() {
		return new Color(R,G,B);
	}
}