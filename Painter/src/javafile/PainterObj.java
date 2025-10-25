package javafile;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//getEdge in group should return the corner
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
	public void changeSize(double rol,double certainX,double certainY) {
		for(Point p:this.getEdge()) {
			double x = p.getX();
            double y = p.getY();
            double newX = certainX+(x-certainX)*rol;
            double newY = certainY+(y-certainY)*rol;
            p.setX(newX);
            p.setY(newY);
		}
	}
	@Override
	public PainterObj clone() {
		PainterObj newObj=new PainterObj(getScene());
		try {
			newObj=this.getClass().getDeclaredConstructor(Scene.class).newInstance(getScene());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException| InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		if(this.getEdge()!=null)
		for(Point p:this.getEdge()) {
			newObj.addPoint(new Point(p.getX(),p.getY(),newObj));
		}
		if(this.getColor()!=null)
		newObj.setColor(new Color(this.getColor().getR(),this.getColor().getG(),this.getColor().getB()));
		return newObj;
	}
	protected void setDrawingColor(Graphics g,PainterObj p) {
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
	public PainterObj(Scene scene) {
		this.scene=scene;
	}
	public Scene getScene() {
		return this.scene;
	}
	public void draw(Graphics g,double scale,double offsetX,double offsetY) {}
	public void setDraggable(boolean b) {
		this.isDragging=b;
		for(Point p:this.getEdge())
			p.setDraggable(b);
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
	public void setColor(double r,double g,double b) {
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
	protected String DescriptPoint() {
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
    //[type name] [pointX pointY]...[pointX pointY] [C] [R] [G] [B]
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
		setDrawingColor(g,this);
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
		setDrawingColor(g,this);
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
			getScene().getDraggingSurface().remove(this);
		}
	}
	@Override 
	public void draw(Graphics g,double scale,double offsetX,double offsetY) {
		setDrawingColor(g,this);
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
		setDrawingColor(g,this);
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
		setDrawingColor(g,this);
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
class Group extends PainterObj {
	private List<PainterObj>group=new ArrayList<>();
	private Point 
	 mr=new Point(0,0,this)
	,ml=new Point(0,0,this)
	,nr=new Point(0,0,this)
	,nl=new Point(0,0,this);

	public Group(Scene scene) {
		super(scene);
		this.addPoint(mr,ml,nr,nl);
	}
	public List<PainterObj>getGroup() {
		return this.group;
	}
	public void addGroup(PainterObj p) {
		if(p.getClass().equals(Group.class)) {
			Group group=(Group)p;
			List<PainterObj>groupList=new ArrayList<>();
			for(PainterObj obj:group.group) {
				groupList.add(obj.clone());
			}
			for(PainterObj obj:groupList) {
				this.group.add(obj);
			}
		}else {
			this.group.add(p.clone());
		}
		this.getScene().removeSurface(p);
	}
	public void disGroup() {
		for(PainterObj obj:this.group) {
			this.getScene().addSurface(obj);
		}
		this.getScene().removeSurface(this);
		this.getScene().getDraggingSurface().remove(this);
	}
	@Override
	public void changeSize(double rol,double cx,double cy) {
		for(PainterObj obj:this.getGroup()) {
			for(Point p:obj.getEdge()) {
				double x = p.getX();
				double y = p.getY();
				double newX = cx+(x-cx)*rol;
				double newY = cy+(y-cy)*rol;
				p.setX(newX);
				p.setY(newY);
			}
		}
	}
	@Override
	public Group clone() {
		Group newgroup=new Group(getScene());
		if(this.getEdge()!=null)
		for(PainterObj obj:this.getGroup()) {
			PainterObj newObj=new PainterObj(getScene());
			for(Point p:obj.getEdge()) {
				newObj.addPoint(new Point(p.getX(),p.getY(),newObj));
			}
			if(obj.getColor()!=null)
				newObj.setColor(new Color(obj.getColor().getR(),obj.getColor().getG(),obj.getColor().getB()));
			newgroup.addGroup(obj);
		}
		return newgroup;
	}
	@Override
	public String toString() {
		String ret="G: ";
		for(PainterObj obj:getGroup()) {
			ret+=obj.toString()+" , ";
		}
		return ret;
	}
	@Override
	public void draw(Graphics g,double scale,double offsetX,double offsetY) {
		double mrx=Double.MIN_VALUE,mry=Double.MIN_VALUE;
		double mlx=Double.MAX_VALUE,mly=Double.MIN_VALUE;
		double nrx=Double.MIN_VALUE,nry=Double.MAX_VALUE;
		double nlx=Double.MAX_VALUE,nly=Double.MAX_VALUE;
		for(PainterObj obj:this.getGroup())
			for(Point point:obj.getEdge())  {
				mrx=Math.max(mrx,point.getX());mry=Math.max(mry,point.getY());
				mlx=Math.min(mlx,point.getX());mly=Math.max(mly,point.getY());
				nrx=Math.max(nrx,point.getX());nry=Math.min(nry,point.getY());
				nlx=Math.min(nlx,point.getX());nly=Math.min(nly,point.getY());
			}
		mr.setX(mrx);
		mr.setY(mry);
		
		ml.setX(mlx);
		ml.setY(mly);

		nr.setX(nrx);
		nr.setY(nry);

		nl.setX(nlx);
		nl.setY(nly);
		for(PainterObj obj:this.getGroup()) {
			if(this.Draggable()) {
				obj.setDraggable(true);
			}
			else {
				obj.setDraggable(false);
			}
			obj.draw(g, scale, offsetX, offsetY);
		}
	}
	@Override
	public Point getCertain() {
		double x=0,y=0,amount=0;
		for(PainterObj obj:this.getGroup()) {
			for(Point p:obj.getEdge()) {
				x+=p.getX();
				y+=p.getY();
				amount++;
			}
		}
		x/=amount;
		y/=amount;
		return new Point(x,y,null);
	}
	@Override
	public Color getColor() {
		System.out.println("group don't give color");
		return null;
	}
	@Override
	protected List<Point>getRawEdge() {
		System.out.println("group don't give point list");
		return null;
	}
	@Override
	public boolean isPointInSurface(int mx,int my,double scale,double offsetX,double offsetY) {
		for(PainterObj obj:this.getGroup())
			if(obj.isPointInSurface(mx,my,scale,offsetX,offsetY))
				return true;
		return false;
	}
	@Override
	public void loadfile(String[]array) {
		List<String>list=new ArrayList<>();
		for(int read=1;read<array.length;read++) {
			if(array[read].equals(",")) {
				PainterObj copied = null;
				String[] subarr=new String[list.size()];
				for(int i=0;i<subarr.length;i++)
					subarr[i]=list.get(i);
				try {
					copied=getScene().getObjTranslator().get(subarr[0]).getDeclaredConstructor(Scene.class).newInstance(getScene());
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException| InvocationTargetException | NoSuchMethodException e) {
					e.printStackTrace();
				}
				copied.loadfile(subarr);
				this.addGroup(copied);
				list.clear();
			}
			else
				list.add(array[read]);
		}
	}
	@Override
	public void moveX(double x) {
		for(PainterObj p:this.getGroup())
			p.moveX(x);
	}
	@Override
	public void moveY(double y) {
		for(PainterObj p:this.getGroup())
			p.moveY(y);
	}
	@Override public void removePoint(Point p) {}//group don't remove point
	@Override public void removePoint(int x) {}
	@Override 
	public void setColor(Color c) {
		for(PainterObj p:this.group)
			p.setColor(c);
	}
	@Override public void setColor(double r,double g,double b) {
		for(PainterObj p:this.group)
			p.setColor(r,g,b);
	}
	@Override protected void setDrawingColor(Graphics g,PainterObj p) {
		System.out.println("group don't set drawing color : Graphics g,PainterObj p");
	}
	@Override
	public void setEdge(Point[]p) {
		System.out.println("group don't set point list : setEdge(Point[]p)");
	}
}