
package javafile;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.*;

/**
 * @since 2025-10-13
 * @author z.x.l
 * @version 1.1
 */
public class Scene extends JPanel implements MouseListener, MouseMotionListener,KeyListener {//AI接手mouseEvent, base-on-swing

	private static final long serialVersionUID = 1L;
    static final String appName = "Painter";
    /**
     * @param
     * allSurfaces is the container of Surface, it is a List
     */
    private java.util.List<Surface> allSurfaces = new ArrayList<>();
    /**
     * @param
     * draggingSurface used to save the surface which we are choosing, when mouse pressed, it will set to null if mouse is not in any surface, and it will not forget after mouse released, so does draggingPoint
     */
    private Surface draggingSurface = null;
    private Point draggingPoint = null;
    /**
     * 
     * this part I let chatgpt did
     */
    private int prevMouseX, prevMouseY;
    private double scale;
    private double offsetX;
    private double offsetY;
    private final int POINT_RADIUS = 10;

    protected class Note extends Stack<List<Surface>>{
		private static final long serialVersionUID = 1L;
			public void redo(Scene scene) {
    			if(!this.isEmpty()) {
    				List<Surface>repaired=this.pop();
    				scene.setAllSurface(repaired);
    				if(this.size()==0)
    					this.add(new ArrayList<Surface>());
    			}
    		}
    		public void saveInfo(List<Surface>recordAllSurface) {
    			List<Surface>newRecord=new ArrayList<>();
    			for(Surface s:recordAllSurface) {
    				Surface newSurface=new Surface();
    				for(Point p:s.getEdge())
    					newSurface.addPoint(new Point(p.getX(),p.getY(),newSurface));
    				newSurface.setColor(new Color(s.getColor().getR(),s.getColor().getG(),s.getColor().getB()));
    				newRecord.add(newSurface);
    			}
    			this.push(newRecord);
    		}
    }
    private Note note=new Note();

	/**
     * 
     * just like javaFx Application.start()
     */
    public void execute() {
    		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException|InstantiationException|IllegalAccessException|UnsupportedLookAndFeelException e) {}
        JFrame frame = new JFrame(appName);
        ToolList toolList=new ToolList(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setSize(400, 400);
        frame.setContentPane(this);
        frame.setVisible(true);
        this.add(toolList,BorderLayout.NORTH);
        addMouseListener(this);
        addKeyListener(this);
        addMouseMotionListener(this);
        setFocusable(true);
        note.push(new ArrayList<Surface>());
        requestFocusInWindow();  
        Timer timer = new Timer(10,e->{repaint();});
        timer.start();
    }

    public void addSurface(Surface s) {
        this.allSurfaces.add(s);
    }
    public void setAllSurface(List<Surface>allsurface) {
    		this.allSurfaces=allsurface;
    }
    public void removeSurface(Surface s) {
        this.allSurfaces.remove(s);
    }
    public List<Surface>getAllSurface() {
    		return this.allSurfaces;
    }
  
    
	public Point getDraggingPoint() {
		return this.draggingPoint;
	}
	public void setDraggingPoint(Point p) {
		this.draggingPoint=p;
	}
	public Surface getDraggingSurface() {
		return this.draggingSurface;
	}
	public void setDraggingSurface(Surface s) {
		this.draggingSurface=s;
	}
    public void setScale(double d) {
    		this.scale=d;
    }
    public double getScale() {
    		return this.scale;
    }
    public void setOffsetX(double d) {
		this.offsetX=d;
    }	
    public double getOffsetX() {
		return this.offsetX;
    }
    public void setOffsetY(double d) {
		this.offsetY=d;
    }	
    public double getOffsetY() {
		return this.offsetY;
    }
    public Note getNote() {
    	return this.note;
    }
    
    @Override
    /**
     * this part I let chatgpt did
     */
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();
        scale = Math.min(width, height) / 10.0;
        offsetX = width / 2.0;
        offsetY = height / 2.0;
        for (Surface s : allSurfaces) {
            drawSurface(g, s);
        }
    }
    /**
     * this part I let chatgpt did
     */
    private void drawSurface(Graphics g, Surface s) {//AI method
        Point[] points = s.getEdge();
        if (points.length < 2) return;

        int width = getWidth();
        int height = getHeight();

        double scale = Math.min(width, height) / 10.0; // 調整比例
        double offsetX = width / 2.0;
        double offsetY = height / 2.0;

        int[] xPoints = new int[points.length];
        int[] yPoints = new int[points.length];

        for (int i = 0; i < points.length; i++) {
            xPoints[i] = (int)(points[i].getX() * scale + offsetX);
            yPoints[i] = (int)(points[i].getY() * scale + offsetY);
        }

        Color color = s.getColor();
        if (color != null) {
            g.setColor(new java.awt.Color(
                (float)color.getR(),
                (float)color.getG(),
                (float)color.getB()
            ));
        } else {
            g.setColor(java.awt.Color.BLACK);
        }
        g.fillPolygon(xPoints, yPoints, points.length);
    }
    @Override
    public void mousePressed(MouseEvent e) {
    		draggingSurface=null;
    		draggingPoint=null;
    		prevMouseX=e.getX();
    		prevMouseY=e.getY();
    		this.requestFocusInWindow();
        int mx = e.getX();
        int my = e.getY();
        for (Surface s : allSurfaces) {
            for (Point p : s.getEdge()) {
            	int px = (int)(p.getX() * scale + offsetX);
            int py = (int)(p.getY() * scale + offsetY);
                double dist = Math.hypot(mx - px, my - py);
                if (dist <= POINT_RADIUS) {
                    draggingPoint = p;
                    prevMouseX = mx;
                    prevMouseY = my;
                    return;
                }
            }
        }
        for (int i=allSurfaces.size()-1;i>=0;i--) {
            if (isPointInSurface(mx, my, allSurfaces.get(i))) {
                draggingSurface = this.allSurfaces.get(i);
                int index=i;
                this.allSurfaces.add(this.allSurfaces.get(i));
                this.allSurfaces.remove(index);
                prevMouseX = mx;
                prevMouseY = my;
                break;
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {//AI
        int mx = e.getX();
        int my = e.getY();
        double dx = (mx - prevMouseX) / scale;
        double dy = (my - prevMouseY) / scale;

        if (draggingPoint != null) {
            draggingPoint.setX(draggingPoint.getX() + dx);
            draggingPoint.setY(draggingPoint.getY() + dy);
        } else if (draggingSurface != null) {
            draggingSurface.moveX(dx);
            draggingSurface.moveY(dy);
        }
        else {
        		for(Surface s:allSurfaces) {
        			s.moveX(dx);
        			s.moveY(dy);
        		}
        }
        prevMouseX = mx;
        prevMouseY = my;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    		if(draggingSurface!=null||draggingPoint!=null)
    			note.saveInfo(this.allSurfaces);
    }

    private boolean isPointInSurface(int mx, int my, Surface s) {//AI
        Point[] points = s.getEdge();
        int[] xPoints = new int[points.length];
        int[] yPoints = new int[points.length];
        for (int i = 0; i < points.length; i++) {
        	xPoints[i] = (int)(points[i].getX() * scale + offsetX);
        	yPoints[i] = (int)(points[i].getY() * scale + offsetY);
        }
        java.awt.Polygon polygon = new java.awt.Polygon(xPoints, yPoints, points.length);
        return polygon.contains(mx, my);
    }

   	@Override
   	public void keyPressed(KeyEvent e) {
   		switch(e.getKeyCode()) {
   		case KeyEvent.VK_C:
   		    if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)
   			if(draggingSurface!=null) {
   				Surface s=new Surface();
   				for(Point p:draggingSurface.getEdge()) {
   					s.addPoint(p.getX(),p.getY());
   				}
   				s.setColor(draggingSurface.getColor().getR(),draggingSurface.getColor().getG(),draggingSurface.getColor().getB());
   				s.moveX(0.25);
   				s.moveY(0.25);
   				this.addSurface(s);
   				draggingSurface=s;
   				note.saveInfo(this.getAllSurface());
   			}
   			break;
		case KeyEvent.VK_DELETE:
			if(draggingSurface!=null||draggingPoint!=null) {
				if(draggingPoint==null) {
					this.removeSurface(draggingSurface);
					this.draggingSurface=null;
				}
				else {
					draggingPoint.getSurface().removePoint(draggingPoint,this);
					this.draggingPoint=null;
				}
			}
			else if(allSurfaces.size()>0)
				allSurfaces.removeLast();
			note.saveInfo(this.getAllSurface());
			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_UP:
			if(draggingSurface!=null) {
				Point cert=draggingSurface.getCertain();
				for(Point p:draggingSurface.getEdge()) {
					p.setX(p.getX()+(p.getX()-cert.getX())*0.1);
					p.setY(p.getY()+(p.getY()-cert.getY())*0.1);
				}
			}
			else if(allSurfaces.size()>0) {
				if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)
				for(Surface s:allSurfaces) {
					for(Point p:s.getEdge()) {
						p.setX(p.getX()*1.1);
						p.setY(p.getY()*1.1);
					}
				}
			}
			note.saveInfo(this.getAllSurface());
			break;
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_DOWN:
			if(draggingSurface!=null) {
				Point cert=draggingSurface.getCertain();
				for(Point p:draggingSurface.getEdge()) {
					p.setX(p.getX()-(p.getX()-cert.getX())*0.1);
					p.setY(p.getY()-(p.getY()-cert.getY())*0.1);
				}
			}
			else if(allSurfaces.size()>0) {
				if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)
				for(Surface s:allSurfaces) {
					for(Point p:s.getEdge()) {
						p.setX(p.getX()/1.1);
						p.setY(p.getY()/1.1);
					}
				}
			}
			note.saveInfo(this.getAllSurface());
			break;
		case KeyEvent.VK_Z:
			if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)
				note.redo(this);
			break;
		}
   	}
   	@Override
   	public void mouseClicked(MouseEvent e) {// change color
   	    	switch (e.getButton()) {
   	        case MouseEvent.BUTTON3:
   	            for (int i=this.allSurfaces.size()-1;i>=0;i--) {
   	                if (isPointInSurface(e.getX(), e.getY(),this.allSurfaces.get(i))) {
   	                    prevMouseX = e.getX();
   	                    prevMouseY = e.getY();
   	                    draggingSurface = this.allSurfaces.get(i);
   	                    break;
   	                }
   	            }
   	            if (draggingSurface != null && draggingSurface.getColor() != null) {
   	            		SwingUtilities.invokeLater(() -> new ChoiceColor(draggingSurface,this));
   	            }
   	            break;
   	        default:
   	            break;
   	    }
   	}
   	/**
   	 * when you click mouse_keyRight on surface,it will show you three text field,you can enter the number(0~1) to change the surface's color
   	 * if your entered is invalid, it will show a dialog told you you have a wrong operate
   	 * 
  	 */
   	protected class ChoiceColor extends JFrame{
   		private static final long serialVersionUID = 1L;
   		private JTextField R=new JTextField(5),G=new JTextField(5),B=new JTextField(5);
   		private JButton enter=new JButton("Enter");
   		private Scene scene;
   		public ChoiceColor(Surface s,Scene scene) {
   			this.scene=scene;
   			this.setSize(300,100);
   			this.setTitle("choice color");
   			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
   			this.setLayout(new FlowLayout());
   			this.add(new JLabel("R:"));
   			this.add(R);
   			this.add(new JLabel("G:"));
   			this.add(G);
   			this.add(new JLabel("B:"));
   			this.add(B);
   			enter.addActionListener(e-> {
   				try {
   					double Rtext=Double.parseDouble(R.getText());
   					double Gtext=Double.parseDouble(G.getText());
   					double Btext=Double.parseDouble(B.getText());
   					if(Rtext<0||Rtext>1||Gtext<0||Gtext>1||Btext<0||Btext>1)
   						throw new IllegalArgumentException();
   					s.setColor(Rtext,Gtext,Btext);
   	   				note.saveInfo(this.scene.getAllSurface());
   					this.dispose();
   				}
   				catch(IllegalArgumentException e2) {
   					JOptionPane.showMessageDialog(this,"Please enter current number(0~1)","Enter error",JOptionPane.ERROR_MESSAGE);
   				}

   			});
   			this.add(enter);
   			this.setVisible(true);
   		}
   	}
   	@Override public void mouseExited(MouseEvent e) {}
   	@Override public void mouseMoved(MouseEvent e) {}
   	@Override public void keyTyped(KeyEvent e) {}
   	@Override public void keyReleased(KeyEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
}
