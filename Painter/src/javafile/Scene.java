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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.*;

/**
 * @since 2025-10-13
 * @author z.x.l
 * @version 1.10-alpha
 */
public class Scene extends JPanel implements MouseListener,MouseMotionListener,KeyListener,MouseWheelListener,DropTargetListener {//AI接手mouseEvent, base-on-swing

    protected class Note extends Stack<Note.Event>{
    		private class Event {
    			private List<PainterObj>graphic;
    			private double scale;
    			private double offsetX;
    			private double offsetY;
    			public Event(List<PainterObj>graphic,double scale,double offsetX,double offsetY) {
    				this.graphic=graphic;
    				this.scale=scale;
    				this.offsetX=offsetX;
    				this.offsetY=offsetY;
    			}
    			public List<PainterObj> getAllSurface() {
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
    			@SuppressWarnings("unused")
				public void setScale(double scale) {
    				this.scale=scale;
    			}	
    			@SuppressWarnings("unused")
				public void setOffsetX(double offsetX) {
    				this.offsetX=offsetX;
    			}	
    			@SuppressWarnings("unused")
				public void setOffsetY(double offsetY) {
    				this.offsetY=offsetY;
    			}	
    		}
		private Stack<Event>redoStack=new Stack<>();
		private static final long serialVersionUID = 1L;
		private Event copySurfaceList(Event allList) {
			List<PainterObj> copy=new ArrayList<>();
			for(PainterObj s:allList.getAllSurface()) {
				copy.add(s.clone());
			}
			return new Event(copy,allList.getScale(),allList.getOffsetX(),allList.getOffsetY());
		}
		private void prepareNote(double scale,double offsetX,double offsetY) {
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
		public void undo(Scene scene) {
			if(this.size()>1) {
				this.redoStack.push(copySurfaceList(this.pop()));
				scene.setAllSurface(copySurfaceList(this.peek()).getAllSurface());
				scene.setScale(this.peek().getScale());
				scene.setOffsetX(this.peek().getOffsetX());
				scene.setOffsetY(this.peek().getOffsetY());
			}
		}
		public void saveInfo(List<PainterObj> list,double scale,double offsetX,double offsetY) {
			this.push(copySurfaceList(new Event(list,scale,offsetX,offsetY)));
			if(this.size()>50)
				this.removeFirst();
			this.redoStack.clear();
		}
    }
   	/**
   	 * click mouse_keyRight on surface to call it
   	 * 
  	 */
   	protected class ChoiceColor extends JPanel {
   		private static final long serialVersionUID = 1L;
   		private Scene scene;
   		public ChoiceColor(List<PainterObj>painterObj,Scene scene) {
   			this.scene=scene;
   			java.awt.Color color=JColorChooser.showDialog(this,"color choosing board", getBackground());
   			try {
   				for(PainterObj p:painterObj)
   				p.setColor(color.getRed()/255.0,color.getGreen()/255.0,color.getBlue()/255.0);
   	    			note.saveInfo(this.scene.allSurfaces,this.scene.scale,this.scene.offsetX,this.scene.offsetY);
   			}
   			catch(NullPointerException e) {}
   		}
   	}
	private static final long serialVersionUID = 1L;
    static final String appName = "Painter";
    static final String version = "1.10-alpha";
    
    //operating
    /**
     * @param
     * allSurfaces is the container of Surface, it is a List
     */
    private java.util.List<PainterObj> allSurfaces = new ArrayList<>();
    /**
     * @param
     * draggingSurface used to save the surface which we are choosing, when mouse pressed, it will set to null if mouse is not in any surface, and it will not forget after mouse released, so does draggingPoint
     */
    private List<PainterObj>draggingSurface = new ArrayList<>();
    private List<Point> draggingPoint = new ArrayList<>();
    private final int POINT_RADIUS = 10;
    private ExportLoadSystem saveLoader=new ExportLoadSystem(this);
    private LayerManager layerManager;
    private Note note=new Note();
    private JPanel mainPanel=new JPanel();
    
    //events listener
    private int prevMouseX, prevMouseY, pressedLocationX,pressedLocationY;
    
    //camera
    private double scale;
    private double offsetX;
    private double offsetY;
	private Map<String,Class<? extends PainterObj>>trans=new HashMap<>();
	
	private void buildFileFormat() {
		trans.put("SS",Surface.class);
		trans.put("SL", Line.class);
		trans.put("BS",BezierSurface.class);
		trans.put("BL", BezierLine.class);
		trans.put("Cr",Circle.class);
		trans.put("G:", Group.class);
	}
	public Map<String, Class<? extends PainterObj>> getObjTranslator() {
		return this.trans;
	}
	
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
    		this.buildFileFormat();
    		JFrame frame = new JFrame(appName+"(ver: "+version+")");
        frame.setIconImage(new ImageIcon(Scene.class.getResource("/painter_logo.png")).getImage());
    	ToolList toolList=new ToolList(this);
    	toolList.setBackground(new java.awt.Color(0,0,120));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 500);
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
        		@SuppressWarnings("unused")
				@Override
        		public void windowOpened(WindowEvent e) {
        		    int width = getWidth();
        		    int height = getHeight();
        		    scale = Math.min(width, height) / 10.0;
        		    offsetX = width / 2.0;
        		    offsetY = height / 2.0;
        		    note.prepareNote(scale,offsetX,offsetY);
        		    URL logo=Scene.class.getResource("/file.txt");
        		    saveLoader.getLoader().loadFile(logo);
        	        new javax.swing.Timer(10,e2->{repaint();}).start();
        		}
        });
    }
    
    public void browserMode(String path) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException|InstantiationException|IllegalAccessException|UnsupportedLookAndFeelException e) {}
		this.buildFileFormat();
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
    			this.saveLoader.getLoader().loadFile(path);
    		repaint();
    }
    public void addSurface(PainterObj s) {
        this.allSurfaces.add(s);
    }
    public void setAllSurface(List<PainterObj> list) {
    		this.allSurfaces=list;
    }
    public void removeSurface(PainterObj s) {
        this.allSurfaces.remove(s);
    }
    public List<PainterObj>getAllSurface() {
    		return this.allSurfaces;
    }
	public List<Point> getDraggingPoint() {
		return this.draggingPoint;
	}
	public void setDraggingPoint(List<Point> p) {
		this.draggingPoint=p;
	}
	public void addDraggingPoint(Point p) {
		if(!this.draggingPoint.contains(p)) {
			this.draggingPoint.add(p);
		}
	}
	public List<PainterObj> getDraggingSurface() {
		return this.draggingSurface;
	}
	public void setDraggingSurface(List<PainterObj> s) {
		this.draggingSurface=s;
	}
	public void addDraggingSurface(PainterObj p) {
		if(!this.draggingSurface.contains(p))
			this.draggingSurface.add(p);
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
    
    //graphic control

    @Override//be called when repaint()
    protected void paintComponent(Graphics g) {
    	if(this.getLayerManager()!=null&&!this.getLayerManager().isOperating()) {
    		this.getLayerManager().clearAllItems();
    		for(PainterObj s:this.allSurfaces) {
    			this.getLayerManager().addItem(s);
    		}
    	}
        super.paintComponent(g);
        for (PainterObj s : allSurfaces) {
            s.draw(g, scale, offsetX, offsetY);
            for(Point p:s.getEdge()) {
            	if(p.draggable()||p.getSurface().Draggable())
            		drawPoint(g,p);
            }
        }
    }
    private void drawPoint(Graphics g,Point p) {
    	g.setColor(java.awt.Color.BLACK);
    	g.fillOval((int)(p.getX()*scale+offsetX)-POINT_RADIUS/2,(int)(p.getY()*scale+offsetY)-POINT_RADIUS/2,POINT_RADIUS,POINT_RADIUS);
    }
    private void selectItems(MouseEvent e) {
		if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == 0) {
			this.getDraggingSurface().clear();
			this.getDraggingPoint().clear(); 
			for(PainterObj s:this.allSurfaces) {
				s.setDraggable(false);
				if(s.getEdge()!=null)
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
		if(this.getDraggingSurface().isEmpty()) {
			find:for (int i=allSurfaces.size()-1;i>=0;i--) {
				if(allSurfaces.get(i).getEdge()!=null) {
					for (Point p : allSurfaces.get(i).getEdge()) {
						int px = (int)(p.getX() * scale + offsetX);
						int py = (int)(p.getY() * scale + offsetY);
						double dist = Math.hypot(mx - px, my - py);
						if (dist <= POINT_RADIUS) {
							p.setDraggable(true);
							addDraggingPoint(p);
							point=p;
							prevMouseX = mx;
							prevMouseY = my;
							break find;
						}
					}
				}
			}
		}
		for (int i=allSurfaces.size()-1;i>=0;i--) {
			if (allSurfaces.get(i).isPointInSurface(mx, my,scale,offsetX,offsetY)&&(point==null||this.allSurfaces.indexOf(point.getSurface())<i)) {
				this.getAllSurface().get(i).setDraggable(true);
				this.addDraggingSurface(this.allSurfaces.get(i));
				prevMouseX = mx;
				prevMouseY = my;
				break;
			}	
		}
		if(!draggingSurface.isEmpty()) {
			draggingPoint.clear();
			for(PainterObj s:this.allSurfaces) {
				if(s.getEdge()!=null)
					for(Point p:s.getEdge())
						p.setDraggable(false);
			}
		}
    }
    @Override
    public void mousePressed(MouseEvent e) {
		selectItems(e);
    	switch(e.getButton()) {
    		case MouseEvent.BUTTON3:
    			if (!draggingSurface.isEmpty())	
    			SwingUtilities.invokeLater(() -> new ChoiceColor(draggingSurface,this));
    			break;
       		}
       		
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
        	
        	for(PainterObj surface:draggingSurface) {
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
    }
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
			double rol=e.getWheelRotation();
			double cx = (getWidth() / 2.0 - getOffsetX()) / getScale();
			double cy = (getHeight() / 2.0 -getOffsetY()) / getScale();
			for(PainterObj s:allSurfaces)
				s.changeSize(rol==-1?1.05:1/1.05,cx,cy);
		}
		note.saveInfo(this.allSurfaces,this.scale,this.offsetX,this.offsetY);
	}
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
						saveLoader.getLoader().loadFile(path);
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
			e.printStackTrace();
		}
	}
   	public void keyPressed(KeyEvent e) {
   		switch(e.getKeyCode()) {
   		case KeyEvent.VK_C:
   		    if((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)
   			if(!draggingSurface.isEmpty()) {
   				List<PainterObj>copied=new ArrayList<>();
   			for(PainterObj surface:draggingSurface) {
   				PainterObj s=surface.clone();
   				s.moveX(0.25);
   				s.moveY(0.25);
   				this.addSurface(s);
   				copied.add(s);
   			}
   			for(PainterObj p:this.draggingSurface) {
   				p.setDraggable(false);
   			}
   			for(PainterObj p:copied) {
   				p.setDraggable(true);
   			}
   			setDraggingSurface(copied);
   			note.saveInfo(this.allSurfaces,this.scale,this.offsetX,this.offsetY);
   			}
   			break;
		case KeyEvent.VK_DELETE:
			if(!draggingSurface.isEmpty()||!draggingPoint.isEmpty()) {
				if(draggingSurface.isEmpty()) {
					for(Point p:draggingPoint) {
						p.getSurface().removePoint(p);
					}
				}
				else {
					for(PainterObj s:draggingSurface) {
							this.removeSurface(s);
					}
					this.getDraggingSurface().clear();
				}
				this.getDraggingPoint().clear();
			}
			else if(allSurfaces.size()>0)
				getAllSurface().removeLast();
			note.saveInfo(this.allSurfaces,this.scale,this.offsetX,this.offsetY);
			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_UP:
			if(!this.draggingSurface.isEmpty()) {
				double certX=0,certY=0;
				int amount=0;
				for(PainterObj surface:draggingSurface) {
					for(Point p:surface.getEdge()) {
						certX+=p.getX();
						certY+=p.getY();
						amount++;
					}
				}
				certX/=amount;
				certY/=amount;
				for(PainterObj surface:draggingSurface) {
					surface.changeSize(1.05,certX,certY);
				}
			}
			note.saveInfo(this.allSurfaces,this.scale,this.offsetX,this.offsetY);
			break;
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_DOWN:
			if(!this.draggingSurface.isEmpty()) {
				double certX=0,certY=0;
				int amount=0;
				for(PainterObj surface:draggingSurface) {
					for(Point p:surface.getEdge()) {
						certX+=p.getX();
						certY+=p.getY();
						amount++;
					}
				}
				certX/=amount;
				certY/=amount;
				for(PainterObj surface:draggingSurface) {
					surface.changeSize(1/1.05,certX,certY);
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
					this.saveLoader.getExporter().ExportFlie(path);
					JOptionPane.showMessageDialog(this,"Save file successfull!","Save successfull", JOptionPane.INFORMATION_MESSAGE);
				}
			}
			break;
		case KeyEvent.VK_A:
			if((e.getModifiersEx()&KeyEvent.CTRL_DOWN_MASK)!=0) {
				for(PainterObj p:this.getAllSurface()) {
					this.addDraggingSurface(p);
					p.setDraggable(true);
				}
			}
			break;
		}
   	}

   	@Override public void mouseClicked(MouseEvent e) {}
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