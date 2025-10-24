package javafile;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 * while add new shape, you should go to @see Scene#buildFileFormat() and sign up a new Format
 * because I can't find all subclass of PainterObj
 * what should you do when you add a new shape:
 * 	Override the draw method
 *  Override the removePoint method(optional) if your shape have a limited range for point, the default range is points>=3
 *  Override
 *  
 */
public class PainterObj {
	private boolean isDragging;
	private Color color=new Color(1,0,0);
	private Scene scene;
	private List<Point>Edge=new ArrayList<>();
	
	public void setDrawingColor(Graphics g,PainterObj p) {
		Color color=null;
		if(p.getColor()!=null)
			color =p.getColor();
		if (color != null) {
			if(!p.Draggable()) {
				g.setColor(new java.awt.Color(
						(float)color.getR(),
						(float)color.getG(),
						(float)color.getB()
						));
			}
			else {//choosing
				float alpha=0.3F;
				g.setColor(new java.awt.Color(
						(float)color.getR()*(1-alpha),
						(float)color.getG()*(1-alpha),
						(float)color.getB()*(1-alpha)+alpha
						));
			}	
		} else {
			g.setColor(java.awt.Color.BLACK);
		}
	}
    public void drawSurface(Graphics g) {
    		setDrawingColor(g,this);
		this.draw(g,getScene().getScale(),getScene().getOffsetX(),getScene().getOffsetY());
    }
	public PainterObj(Scene scene) {
		this.scene=scene;
	}
	public Scene getScene() {
		return this.scene;
	}
	public void draw(Graphics g,double scale,double offsetX,double offsetY) {}
	public void setDraggable(boolean b) {
		this.isDragging=b;
	}
	public boolean Draggable() {
		return this.isDragging;
	}
	public Point getCertain() {
		double centx=0,centy=0;
		for(Point p:this.getEdge()) {
			centx+=p.getX();
			centy+=p.getY();
		}
		centx/=this.getEdge().length;
		centy/=this.getEdge().length;
		return new Point(centx,centy,null);
	}
	public Point[] getEdge() {
		return Edge.toArray(new Point[0]);
	}
	public void setEdge(Point[] edge) {
		this.Edge.clear();
		for(int i=0;i<edge.length;i++)
			this.Edge.add(edge[i]);
	}

	public void setColor(double r,double g,double b){
		this.color=new Color(r,g,b);
	}
	protected List<Point>getRawEdge() {
		return this.Edge;
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
		return this.color;
	}
	public void setColor(Color c) {
		this.color=c;
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
	public void removePoint(Point point) {
		this.Edge.remove(point);
		if(this.getEdge().length<3) {
			scene.removeSurface(this);
		}
	}
	public void removePoint(int index) {
		this.Edge.remove(index);
	}
	public String DescriptPoint() {
		String s="";
		for(Point p:this.getEdge()) {
			s+=p.getX()+" "+p.getY()+" ";
		}
		s+=" C "+this.getColor().getR()+" "+this.getColor().getG()+" "+this.getColor().getB();
		return s;
	}
    public boolean isPointInSurface(int mx, int my,double scale,double offsetX,double offsetY) {//AI
        Point[] points = this.getEdge();
        int[] xPoints = new int[points.length];
        int[] yPoints = new int[points.length];
        for (int i = 0; i < points.length; i++) {
        	xPoints[i] = (int)(points[i].getX() * scale + offsetX);
        	yPoints[i] = (int)(points[i].getY() * scale + offsetY);
        }
        java.awt.Polygon polygon = new java.awt.Polygon(xPoints, yPoints, points.length);
        return polygon.contains(mx, my);
    }
    public void loadfile(String[]array) {
    		for(int i=1;i<array.length-5;i+=2) {
    			Double X=Double.parseDouble(array[i]);
    			Double Y=Double.parseDouble(array[i+1]);
    			this.addPoint(new Point(X,Y,this));
    		}
    		Double R=Double.parseDouble(array[array.length-3]);
    		Double G=Double.parseDouble(array[array.length-2]);
    		Double B=Double.parseDouble(array[array.length-1]);
    		this.setColor(R,G,B);
    }
}

class BezierLine extends PainterObj {
	public BezierLine(Scene scene) {
		super(scene);
	}
	public static BezierLine INSTANCE(Scene scene) {
		BezierLine l=new BezierLine(scene);
		l.addPoint(0,0);
		l.addPoint(1,1);
		l.addPoint(0,2);
		return l;
	}
	@Override
	public void draw(Graphics g,double scale,double offsetX,double offsetY) {
		Graphics2D g2=(Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(g.getColor());
	Path2D path=new Path2D.Double();
	path.moveTo(this.getEdge()[0].getX()*scale+offsetX,this.getEdge()[0].getY()*scale+offsetY);
		if(this.getEdge().length>3)
	for(int i=1;i<this.getEdge().length-2;i++)
			path.curveTo(this.getEdge()[i].getX()*scale+offsetX,this.getEdge()[i].getY()*scale+offsetY,this.getEdge()[i+1].getX()*scale+offsetX,this.getEdge()[i+1].getY()*scale+offsetY,this.getEdge()[i+2].getX()*scale+offsetX,this.getEdge()[i+2].getY()*scale+offsetY);
		else
			path.quadTo(this.getEdge()[1].getX()*scale+offsetX,this.getEdge()[1].getY()*scale+offsetY,this.getEdge()[2].getX()*scale+offsetX,this.getEdge()[2].getY()*scale+offsetY);
		g2.draw(path);
	}
	@Override
	public String toString() {
		return "BL "+this.DescriptPoint();
	}
}
class BezierSurface extends PainterObj {
	public BezierSurface(Scene scene) {
		super(scene);
	}
	public static BezierSurface INSTANCE(Scene scene) {
		BezierSurface s=new BezierSurface(scene);
		s.addPoint(0,0);
		s.addPoint(0,1);
		s.addPoint(1,1);
		s.addPoint(1,0);
		return s;
	}
	@Override
	public void draw(Graphics g,double scale,double offsetX,double offsetY) {
		Graphics2D g2=(Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(g.getColor());
	Path2D path=new Path2D.Double();
	path.moveTo(this.getEdge()[0].getX()*scale+offsetX,this.getEdge()[0].getY()*scale+offsetY);
		if(this.getEdge().length>3) {
			for(int i=1;i<this.getEdge().length-2;i++) 
			path.curveTo(this.getEdge()[i].getX()*scale+offsetX,this.getEdge()[i].getY()*scale+offsetY,this.getEdge()[i+1].getX()*scale+offsetX,this.getEdge()[i+1].getY()*scale+offsetY,this.getEdge()[i+2].getX()*scale+offsetX,this.getEdge()[i+2].getY()*scale+offsetY);
		path.curveTo(this.getEdge()[this.getEdge().length-2].getX()*scale+offsetX,this.getEdge()[this.getEdge().length-2].getY()*scale+offsetY,this.getEdge()[this.getEdge().length-1].getX()*scale+offsetX,this.getEdge()[this.getEdge().length-1].getY()*scale+offsetY,this.getEdge()[0].getX()*scale+offsetX,this.getEdge()[0].getY()*scale+offsetY);
		}
		else {
			path.quadTo(this.getEdge()[1].getX()*scale+offsetX,this.getEdge()[1].getY()*scale+offsetY,this.getEdge()[2].getX()*scale+offsetX,this.getEdge()[2].getY()*scale+offsetY);
			path.quadTo(this.getEdge()[2].getX()*scale+offsetX,this.getEdge()[2].getY()*scale+offsetY,this.getEdge()[0].getX()*scale+offsetX,this.getEdge()[0].getY()*scale+offsetY);
			path.quadTo(this.getEdge()[0].getX()*scale+offsetX,this.getEdge()[0].getY()*scale+offsetY,this.getEdge()[1].getX()*scale+offsetX,this.getEdge()[1].getY()*scale+offsetY);
		}
		path.closePath();
		g2.fill(path);
	}
	@Override
	public String toString() {
		return "BS "+this.DescriptPoint();
	}
}
class Line extends PainterObj{
	public Line(Scene scene) {
		super(scene);
	}
	public static Line STRIGHT(Scene scene) {
		Line l=new Line(scene);
		l.addPoint(0,0);
		l.addPoint(0,1);
		return l;
	}
	@Override
	public void removePoint(Point point) {
		this.getRawEdge().remove(point);
		if(this.getEdge().length<2) {
			getScene().removeSurface(this);
		}
	}
	@Override 
	public void draw(Graphics g,double scale,double offsetX,double offsetY) {
		for(int i=0;i<this.getEdge().length-1;i++) {
			g.drawLine((int)(this.getEdge()[i].getX()*scale+offsetX), (int)(this.getEdge()[i].getY()*scale+offsetY),(int)(this.getEdge()[i+1].getX()*scale+offsetX),(int)(this.getEdge()[i+1].getY()*scale+offsetY));
		}
	}
	@Override
	public String toString() {
		return "SL "+this.DescriptPoint();
	}
}
class Surface extends PainterObj{
	public Surface(Scene scene) {
		super(scene);
	}
	public static Surface QUAD(Scene scene) {
		Surface quad = new Surface(scene);
		quad.addPoint(new Point(0,0,quad));
		quad.addPoint(new Point(0,1,quad));
		quad.addPoint(new Point(1,1,quad));
		quad.addPoint(new Point(1,0,quad));
		return quad;
	}
	public static Surface TRIANGLE(Scene scene) {
		Surface tria=new Surface(scene);
		tria.addPoint(new Point(0,0,tria));
		tria.addPoint(new Point(0,1,tria));
		tria.addPoint(new Point(1,1,tria));
		return tria;
	}
	@Override
	public void draw(Graphics g,double scale,double offsetX,double offsetY) {
		Point[] points = this.getEdge();
		if (points.length < 2) return;
		int[] xPoints = new int[points.length];
		int[] yPoints = new int[points.length];
		for (int i = 0; i < points.length; i++) {
			xPoints[i] = (int)(points[i].getX() * scale + offsetX);
			yPoints[i] = (int)(points[i].getY() * scale + offsetY);
		}
		g.fillPolygon(xPoints, yPoints, points.length);
	}
	@Override
	public String toString() {
		return "SS "+this.DescriptPoint();
	}
}
class Circle extends PainterObj {
	public Circle(Scene scene) {
		super(scene);
	}
	public static Circle CIRCLE(Scene scene) {
		Circle c=new Circle(scene);
		c.addPoint(0,0);
		c.addPoint(1,0);
		c.addPoint(0,1);
		return c;
	}
	@Override
	public void removePoint(Point point) {
		this.getRawEdge().remove(point);
		if(this.getEdge().length!=3) {
			getScene().removeSurface(this);
		}
	}
	@Override
	public void draw(Graphics g,double scale,double offsetX,double offsetY) {
		int x=(int)(this.getEdge()[0].getX()*scale+offsetX);
		int y=(int)(this.getEdge()[0].getY()*scale+offsetY);
		int width=Math.abs((int)(this.getEdge()[1].getX()* scale+offsetX)-x)*2;
		int height=Math.abs((int)(this.getEdge()[2].getY()*scale+offsetY)-y)*2;
		g.fillOval(x-width/2,y-height/2,width,height);
	}
	@Override
	public boolean isPointInSurface(int mx, int my,double scale,double offsetX,double offsetY) {
		double x=(this.getEdge()[0].getX()*scale+offsetX);
		double y=(this.getEdge()[0].getY()*scale+offsetY);
		double width=Math.abs((this.getEdge()[1].getX()* scale+offsetX)-x);
		double height=Math.abs((this.getEdge()[2].getY()*scale+offsetY)-y);
		if((((mx-x)*(mx-x))/(width*width)+((my-y)*(my-y))/(height*height))<=1) {
			return true;
		}
		return false;
	}
	@Override
	public String toString() {
		return "Cr "+this.DescriptPoint();
	}
}