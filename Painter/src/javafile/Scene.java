package javafile;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.awt.BorderLayout;
import java.awt.Dimension;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.swing.*;

/**
 * @since 2025-10-13
 * @author z.x.l
 * @version 1.1
 */
public class Scene extends JPanel implements MouseListener,MouseMotionListener,KeyListener,MouseWheelListener,DropTargetListener {//AI接手mouseEvent, base-on-swing
	protected class Event {
		private List<Surface>graphic;
		private double scale;
		private double offsetX;
		private double offsetY;
		public Event(List<Surface>graphic,double scale,double offsetX,double offsetY) {
			this.graphic=graphic;
			this.scale=scale;
			this.offsetX=offsetX;
			this.offsetY=offsetY;
		}
		public List<Surface> getAllSurface() {
			return this.graphic;
		}
		public double getScale() {
			return this.scale;
		}
		public double getOffsetX() {
			return this.offsetX;
		}
		public double getOffsetY() {
			return this.offsetY;
		}
		public void setScale(double scale) {
			this.scale=scale;
		}
		public void setOffsetX(double offsetX) {
			this.offsetX=offsetX;
		}
		public void setOffsetY(double offsetY) {
			this.offsetY=offsetY;
		}
	}
    protected class Note extends Stack<Event>{
		private Stack<Event>redoStack=new Stack<>();
		private static final long serialVersionUID = 1L;
		private Event copySurfaceList(Event allList) {
			List<Surface> copy=new ArrayList<>();
			for(Surface s:allList.getAllSurface()) {
			Surface newSurface=new Surface();
			for(Point p:s.getEdge())
				newSurface.addPoint(new Point(p.getX(),p.getY(),newSurface));
			newSurface.setColor(new Color(s.getColor().getR(),s.getColor().getG(),s.getColor().getB()));
			copy.add(newSurface);
			}
			return new Event(copy,allList.getScale(),allList.getOffsetX(),allList.getOffsetY());
		}
		public Note() {};
		public void prepareNote(double scale,double offsetX,double offsetY) {
			this.redoStack.push(new Event(new ArrayList<>(),scale,offsetX,offsetY));
		}
		public void redo(Scene scene) {
			if(this.redoStack.size()>0) {
				Event undoList=copySurfaceList(this.redoStack.pop());
				scene.setAllSurface(undoList.getAllSurface());
				scene.setScale(undoList.scale);
				scene.setOffsetX(undoList.offsetX);
				scene.setOffsetY(undoList.offsetY);
    			this.push(copySurfaceList(undoList));
			}

		}
		
		public void saveInfo(List<Surface> recordAllSurface,double scale,double offsetX,double offsetY) {
			this.push(copySurfaceList(new Event(recordAllSurface,scale,offsetX,offsetY)));
			if(this.size()>50)
				this.removeFirst();
			this.redoStack.clear();
		}
		public void undo(Scene scene) {
			if(this.size()>1) {
				this.redoStack.push(copySurfaceList(this.pop()));
				scene.setAllSurface(copySurfaceList(this.peek()).getAllSurface());
				scene.setScale(this.peek().getScale());
				scene.setOffsetX(this.peek().getOffsetX());
				scene.setOffsetY(this.peek().getOffsetY());
			}
		}
    }
    
    
	private static final long serialVersionUID = 1L;
    static final String appName = "Painter";
    static final String version = "1.7.1";
    /**
     * @param
     * allSurfaces is the container of Surface, it is a List
     */
    private java.util.List<Surface> allSurfaces = new ArrayList<>();
    /**
     * @param
     * draggingSurface used to save the surface which we are choosing, when mouse pressed, it will set to null if mouse is not in any surface, and it will not forget after mouse released, so does draggingPoint
     */
    private List<Surface>draggingSurface = new ArrayList<>();
    private List<Point> draggingPoint = new ArrayList<>();
    /**
     * 
     * this part I let chatgpt did
     */
    private int prevMouseX, prevMouseY, pressedLocationX,pressedLocationY;
    private double scale;
    private double offsetX;
    private double offsetY;
    
    
    private final int POINT_RADIUS = 10;
    private ExportLoadSystem saveLoader=new ExportLoadSystem(this);
    private LayerManager layerManager;
    private Note note=new Note();
    private JPanel mainPanel=new JPanel();
	/**
     * 
     * just like javaFx Application.start()
	 * @throws InterruptedException 
     */
    public void execute() {
    		//圖片載體
    		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException|InstantiationException|IllegalAccessException|UnsupportedLookAndFeelException e) {}	
    		JFrame frame = new JFrame(appName+"(ver: "+version+")");
        frame.setIconImage(new ImageIcon(Scene.class.getResource("/painter_logo.png")).getImage());
    		ToolList toolList=new ToolList(this);
    		toolList.setBackground(new java.awt.Color(0,0,120));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(toolList,BorderLayout.NORTH);
        mainPanel.add(this,BorderLayout.CENTER);
        layerManager=new LayerManager(Scene.this);
        JScrollPane scroll=new JScrollPane(layerManager);
        scroll.setPreferredSize(new Dimension(70,0));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mainPanel.add(scroll,BorderLayout.EAST);
        frame.add(mainPanel);
        addMouseListener(this);
        addKeyListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        new DropTarget(this,this);
        setFocusable(true);
        requestFocusInWindow();
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {//to avoid GUI system crash on execution thread
        		@Override
        		public void windowOpened(WindowEvent e) {
        		    int width = getWidth();
        		    int height = getHeight();
        		    scale = Math.min(width, height) / 10.0;
        		    offsetX = width / 2.0;
        		    offsetY = height / 2.0;
        		    note.prepareNote(scale,offsetX,offsetY);
        		    URL logo=Scene.class.getResource("/file.txt");
        		    saveLoader.loadFile(logo);
        			if(Scene.this.getLayerManager()!=null) refrashLayerManager();
        	        new javax.swing.Timer(10,e2->{repaint();}).start();
        		}
        });
    }
    
    public void browserMode(String path) {
	
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException|InstantiationException|IllegalAccessException|UnsupportedLookAndFeelException e) {}
		JFrame frame = new JFrame("Browser"+"(ver: "+version+")");
        frame.setIconImage(new ImageIcon(Scene.class.getResource("/painter_logo.png")).getImage());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 400);
		frame.setVisible(true);
		mainPanel=new JPanel(new BorderLayout());
		mainPanel.add(this,BorderLayout.CENTER);
		frame.setContentPane(mainPanel);
		new DropTarget(this,this);
    		note=new Note();
    		if(path!=null)
    			this.saveLoader.loadFile(path);
    		repaint();
    }
    
    
    private void refrashLayerManager() {
    		this.getLayerManager().clearAllItems();
    		for(Surface s:this.allSurfaces) {
    			this.getLayerManager().addItem(s);
    		}
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
	public List<Point> getDraggingPoint() {
		return this.draggingPoint;
	}
	public void setDraggingPoint(List<Point> p) {
		this.draggingPoint=p;
	}
	public List<Surface> getDraggingSurface() {
		return this.draggingSurface;
	}
	public void setDraggingSurface(List<Surface> s) {
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
    public LayerManager getLayerManager() {
    		return this.layerManager;
    }
    @Override
    /**
     * this part I let chatgpt did
     */
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Surface s : allSurfaces) {
            drawSurface(g, s);
            for(Point p:s.getEdge()) {
            		drawPoint(g,p);
            }
        }
    }
    private void drawPoint(Graphics g,Point p) {
    		if(p.draggable()) {
    			g.setColor(java.awt.Color.BLACK);
    			g.fillOval((int)(p.getX()*scale+offsetX)-5,(int)(p.getY()*scale+offsetY)-5,10,10);
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
        		if(!s.Draggable()) {
        			g.setColor(new java.awt.Color(
        					(float)color.getR(),
        					(float)color.getG(),
        					(float)color.getB()
        					));
        		}
        		else {
        			float alpha=0.5F;
        			g.setColor(new java.awt.Color(
        					(float)color.getR()*(1-alpha),
        					(float)color.getG()*(1-alpha),
        					(float)color.getB()*(1-alpha)+alpha
        					));
        		}
        } else {
            g.setColor(java.awt.Color.BLACK);
        }
        g.fillPolygon(xPoints, yPoints, points.length);
    }
    @Override
    public void mousePressed(MouseEvent e) {

		if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == 0) {
    			draggingSurface.clear();
    			draggingPoint.clear(); 
    			for(Surface s:this.allSurfaces) {
        			s.setDraggable(false);
        			for(Point p:s.getEdge())
        				p.setDraggable(false);
    			}
		}
		
    		prevMouseX=e.getX();
    		prevMouseY=e.getY();
    		pressedLocationX=e.getX();
    		pressedLocationY=e.getY();
    		this.requestFocusInWindow();
        int mx = e.getX();
        int my = e.getY();
        Point point = null;
        if(draggingSurface.isEmpty()) {
        find:for (int i=allSurfaces.size()-1;i>=0;i--) {
            for (Point p : allSurfaces.get(i).getEdge()) {
            	int px = (int)(p.getX() * scale + offsetX);
            int py = (int)(p.getY() * scale + offsetY);
                double dist = Math.hypot(mx - px, my - py);
                if (dist <= POINT_RADIUS) {
                		p.setDraggable(true);
                    draggingPoint.add(p);
                    point=p;
                    prevMouseX = mx;
                    prevMouseY = my;
                    break find;
                }
            }
        }
        }
        for (int i=allSurfaces.size()-1;i>=0;i--) {
            if (isPointInSurface(mx, my, allSurfaces.get(i))&&(point==null||this.allSurfaces.indexOf(point.getSurface())<i)) {
            		this.allSurfaces.get(i).setDraggable(true);
                draggingSurface.add(this.allSurfaces.get(i));
                prevMouseX = mx;
                prevMouseY = my;
                break;
            }
        }
        if(!draggingSurface.isEmpty()) {
        draggingPoint.clear();
		for(Surface s:this.allSurfaces) {
			for(Point p:s.getEdge())
				p.setDraggable(false);
		}
        }
		if(this.getLayerManager()!=null) refrashLayerManager();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        double dx = (mx - prevMouseX) /scale;
        double dy = (my - prevMouseY) /scale;

        if (!draggingPoint.isEmpty()) {
        	
        	for(Point p:draggingPoint) {
        		if((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0) {
        			if(Math.abs(e.getX()-prevMouseX)>Math.abs(e.getY()-prevMouseY)*3)
        				p.setX(p.getX() + dx);
        			else if(Math.abs(e.getX()-prevMouseX)*3<Math.abs(e.getY()-prevMouseY))
        				p.setY(p.getY() + dy);
        		}
        		else {
        			p.setX(p.getX() + dx);
        			p.setY(p.getY() + dy);
        		}
        	}
        } else if (!draggingSurface.isEmpty()) {
        	
        	for(Surface surface:draggingSurface) {
			if((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0) {
    				if(Math.abs(e.getX()-prevMouseX)>Math.abs(e.getY()-prevMouseY)*3)
    					surface.moveX(dx);
    				else if(Math.abs(e.getX()-prevMouseX)*3<Math.abs(e.getY()-prevMouseY))
    					surface.moveY(dy);
			}
			else {
				surface.moveX(dx);
        			surface.moveY(dy);
			}
        	}
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
    			if(e.getX()!=pressedLocationX&&e.getY()!=pressedLocationY) 
    				note.saveInfo(this.allSurfaces,this.scale,this.offsetX,this.offsetY);
    			if(this.getLayerManager()!=null)
    				refrashLayerManager();
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
   			if(!draggingSurface.isEmpty()) {
   			for(Surface surface:draggingSurface) {
   				Surface s=new Surface();
   				for(Point p:surface.getEdge()) {
   					s.addPoint(p.getX(),p.getY());
   				}
   				s.setColor(surface.getColor().getR(),surface.getColor().getG(),surface.getColor().getB());
   				s.moveX(0.25);
   				s.moveY(0.25);
   				this.addSurface(s);
   				note.saveInfo(this.allSurfaces,this.scale,this.offsetX,this.offsetY);
   			}
   			}
   			break;
		case KeyEvent.VK_DELETE:
			if(!draggingSurface.isEmpty()||!draggingPoint.isEmpty()) {
				if(draggingSurface.isEmpty()) {
					for(Point p:draggingPoint) {
						p.getSurface().removePoint(p,this);
					}
				}
				else {
					for(Surface s:draggingSurface) {
						this.removeSurface(s);
					}
					this.draggingSurface.clear();
				}
				this.draggingPoint.clear();
			}
			else if(allSurfaces.size()>0)
				allSurfaces.removeLast();
			note.saveInfo(this.allSurfaces,this.scale,this.offsetX,this.offsetY);
			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_UP:
			if(!this.draggingSurface.isEmpty()) {
				for(Surface surface:draggingSurface) {
					Point cert=surface.getCertain();
					for(Point p:surface.getEdge()) {
						p.setX(p.getX()+(p.getX()-cert.getX())*0.1);
						p.setY(p.getY()+(p.getY()-cert.getY())*0.1);
					}
				}
			}
			note.saveInfo(this.allSurfaces,this.scale,this.offsetX,this.offsetY);
			break;
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_DOWN:
			if(!this.draggingSurface.isEmpty()) {
			for(Surface surface:draggingSurface) {
				Point cert=surface.getCertain();
				for(Point p:surface.getEdge()) {
					p.setX(p.getX()-(p.getX()-cert.getX())*0.1);
					p.setY(p.getY()-(p.getY()-cert.getY())*0.1);
				}
			}
			}
			note.saveInfo(this.allSurfaces,this.scale,this.offsetX,this.offsetY);
			break;
		case KeyEvent.VK_Z:
			if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)
				note.undo(this);
			break;
		case KeyEvent.VK_Y:
			if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)
				note.redo(this);
			break;
		case KeyEvent.VK_S:
			if((e.getModifiersEx()&KeyEvent.CTRL_DOWN_MASK)!=0) {
				JFileChooser chooser=new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setDialogTitle("choose the folder");
				int result=chooser.showOpenDialog(this);
				if(result==JFileChooser.APPROVE_OPTION) {
					String path=chooser.getSelectedFile().getAbsolutePath();
					if(!path.toLowerCase().endsWith(".txt"))
						path+=".txt";
					this.saveLoader.ExportFlie(path);
					JOptionPane.showMessageDialog(this,"Save file successfull!","Save successfull", JOptionPane.INFORMATION_MESSAGE);
				}
			}
			break;
		}
		if(this.getLayerManager()!=null) refrashLayerManager();
   	}
   	@Override
   	public void mouseClicked(MouseEvent e) {// change color
   	    	switch (e.getButton()) {
   	        case MouseEvent.BUTTON3:
   	        		draggingSurface.clear();
   	            for (int i=this.allSurfaces.size()-1;i>=0;i--) {
   	                if (isPointInSurface(e.getX(), e.getY(),this.allSurfaces.get(i))) {
   	                    prevMouseX = e.getX();
   	                    prevMouseY = e.getY();
   	                    draggingSurface.add(this.allSurfaces.get(i));
   	                    break;
   	                }
   	            }
   	            if (draggingSurface.getFirst().getColor() != null) {
   	            		SwingUtilities.invokeLater(() -> new ChoiceColor(draggingSurface.getFirst(),this));
   	            }
   	            break;
   	        default:
   	            break;
   	    }
		if(this.getLayerManager()!=null) refrashLayerManager();
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
		note.saveInfo(this.allSurfaces,this.scale,this.offsetX,this.offsetY);
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
			if(this.getLayerManager()!=null) refrashLayerManager();
		    repaint();// it is for browse mode
			JOptionPane.showMessageDialog(this,"the file "+path+" is opened!","Open the file Successful", JOptionPane.INFORMATION_MESSAGE);
		}
		catch(Exception e) {
			dtde.dropComplete(false);
			JOptionPane.showMessageDialog(this,"the file were broken or it have a wrong format","File Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
   	/**
   	 * click mouse_keyRight on surface to call it
   	 * 
  	 */
   	protected class ChoiceColor extends JPanel {
   		private static final long serialVersionUID = 1L;
   		private Scene scene;
   		public ChoiceColor(Surface s,Scene scene) {
   			this.scene=scene;
   			java.awt.Color color=JColorChooser.showDialog(this,"color choosing board", getBackground());
   			try {
   				s.setColor(color.getRed()/255.0,color.getGreen()/255.0,color.getBlue()/255.0);
   	    			note.saveInfo(this.scene.allSurfaces,this.scene.scale,this.scene.offsetX,this.scene.offsetY);
   	    			if(Scene.this.getLayerManager()!=null) refrashLayerManager();
   			}
   			catch(NullPointerException e) {}

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