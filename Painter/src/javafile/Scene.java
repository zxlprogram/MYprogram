
package javafile;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.*;

/**
 * @since 2025-10-13
 * @author z.x.l
 * @version 1.1
 */
public class Scene extends JPanel implements MouseListener,MouseMotionListener,KeyListener,MouseWheelListener,DropTargetListener {//AI接手mouseEvent, base-on-swing
	
    protected class Note extends Stack<List<Surface>>{
		private Stack<List<Surface>>undoStack=new Stack<>();
		private static final long serialVersionUID = 1L;
		private List<Surface> copySurfaceList(List<Surface> allList) {
			List<Surface> copy=new ArrayList<>();
			for(Surface s:allList) {
			Surface newSurface=new Surface();
			for(Point p:s.getEdge())
				newSurface.addPoint(new Point(p.getX(),p.getY(),newSurface));
			newSurface.setColor(new Color(s.getColor().getR(),s.getColor().getG(),s.getColor().getB()));
			copy.add(newSurface);
			}
		return copy;
    }
    public Note() {
    		this.undoStack.push(new ArrayList<>());
    }
	public void redo(Scene scene) {
			if(this.size()>1) {
				this.undoStack.push(copySurfaceList(this.pop()));
				scene.setAllSurface(copySurfaceList(this.peek()));
			}
		}
		public void saveInfo(List<Surface> recordAllSurface) {
			this.push(copySurfaceList(recordAllSurface));
			this.undoStack.clear();
		}
		public void undo(Scene scene) {
			if(this.undoStack.size()>0) {
				List<Surface> undoList=copySurfaceList(this.undoStack.pop());
				scene.setAllSurface(undoList);
    			this.push(copySurfaceList(undoList));
			}
		}
    }
    
    
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
    private ExportLoadSystem saveLoader=new ExportLoadSystem(this);
    private Note note=new Note();
    private JPanel mainPanel;
	/**
     * 
     * just like javaFx Application.start()
	 * @throws InterruptedException 
     */
    public void execute() throws InterruptedException {
    		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException|InstantiationException|IllegalAccessException|UnsupportedLookAndFeelException e) {}

    		
	    int width = getWidth();
	    int height = getHeight();
	    scale = Math.min(width, height) / 10.0;
	    offsetX = width / 2.0;
	    offsetY = height / 2.0;
	        
        note.push(new ArrayList<>());
    		if(new File("file.txt").exists()) {
        		saveLoader.loadFile("file.txt");
    		}
    		
    		JFrame frame = new JFrame(appName);
        ToolList toolList=new ToolList(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setVisible(true);
        mainPanel=new JPanel(new BorderLayout());
        mainPanel.add(toolList,BorderLayout.NORTH);
        mainPanel.add(this,BorderLayout.CENTER);
        frame.setContentPane(mainPanel);
        addMouseListener(this);
        addKeyListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        new DropTarget(this,this);
        setFocusable(true);
        requestFocusInWindow();
        Thread.sleep(100);
        	Timer timer = new Timer(10,e->{repaint();});
        timer.start();
    }
    
    	public void browseMode() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException|InstantiationException|IllegalAccessException|UnsupportedLookAndFeelException e) {}
		JFrame frame = new JFrame("Browser");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 400);
		frame.setContentPane(this);
		frame.setVisible(true);
		new DropTarget(this,this);
		setFocusable(true);
		requestFocusInWindow();
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
        for (Surface s : allSurfaces) {
            drawSurface(g, s);
        }
    }
    /**
     * this part I let chatgpt did
     */
    private void drawSurface(Graphics g, Surface s) {
        Point[] points = s.getEdge();
        if (points.length < 2) return;

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
        		offsetX+=dx*50;
        		offsetY+=dy*50;
        }
        prevMouseX = mx;
        prevMouseY = my;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
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
			note.saveInfo(this.getAllSurface());
			break;
		case KeyEvent.VK_Z:
			if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)
				note.redo(this);
			break;
		case KeyEvent.VK_Y:
			if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)
				note.undo(this);
			break;
		case KeyEvent.VK_S:
			if((e.getModifiersEx()&KeyEvent.CTRL_DOWN_MASK)!=0) {
				saveLoader.ExportFlie("file.txt");
				JOptionPane.showMessageDialog(this,"Save file successfull!","Save successfull", JOptionPane.INFORMATION_MESSAGE);
			}
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
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int rol=e.getWheelRotation();
		if(allSurfaces.size()>0) {
			if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
	            double cx = (getWidth() / 2.0 - offsetX) / scale;
	            double cy = (getHeight() / 2.0 - offsetY) / scale;
	            double scaleFactor = rol == -1 ? 1.05 : 1 / 1.05;
				for(Surface s:allSurfaces) {
					for(Point p:s.getEdge()) {
	                    double x = p.getX();
	                    double y = p.getY();
	                    double newX = cx + (x - cx) * scaleFactor;
	                    double newY = cy + (y - cy) * scaleFactor;
	                    p.setX(newX);
	                    p.setY(newY);

					}
				}
			}
		}
		note.saveInfo(this.getAllSurface());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void drop(DropTargetDropEvent dtde) {
		try {
			dtde.acceptDrop(DnDConstants.ACTION_COPY);
			Transferable trans=dtde.getTransferable();
			DataFlavor[]flavor=trans.getTransferDataFlavors();
			String path ="";
			for(DataFlavor f:flavor) {
				if(f.isFlavorJavaFileListType()) {
					List<File>files=(List<File>)trans.getTransferData(f);
					for(File file:files) {
						path=file.getAbsolutePath();
						saveLoader.loadFile(path);
					}
				}
			}
			dtde.dropComplete(true);
		    repaint();// it is for browse mode
			JOptionPane.showMessageDialog(this,"the file "+path+" is opened!","Open the file Successful", JOptionPane.INFORMATION_MESSAGE);
		}
		catch(Exception e) {
			dtde.dropComplete(false);
			JOptionPane.showMessageDialog(this,"the file were broken or it have a wrong format","File Error", JOptionPane.ERROR_MESSAGE);
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
	@Override public void dragEnter(DropTargetDragEvent dtde) {}
	@Override public void dragOver(DropTargetDragEvent dtde) {}
	@Override public void dropActionChanged(DropTargetDragEvent dtde) {}
	@Override public void dragExit(DropTargetEvent dte) {}
}