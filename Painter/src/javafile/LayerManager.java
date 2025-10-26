package javafile;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class LayerManager extends JPanel implements MouseListener{
	private static final long serialVersionUID = 7672158295712151948L;
	private Scene scene;
	private int iconSize=50;
	private boolean sizeChanging=false;
	private final List<DraggableItem>items=new ArrayList<>();
	private boolean isOperating=false;
	private DraggableItem draggingItem=null;
	public int getIconSize() {
		return this.iconSize;
	}
	public void setIconSize(int i) {
		this.iconSize=i;
	}
	public List<DraggableItem>getAllDraggableItems() {
		return items;
	}
	public LayerManager(Scene scene) {
		this.scene=scene;
		this.setBackground(java.awt.Color.BLACK);
		addMouseListener(this);
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
		        if (e.getX()<=3||e.getX()>=getWidth()-3) {
		            setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
		            sizeChanging=true;
		        }
		        else {
		        	setCursor(Cursor.getDefaultCursor());
		        	sizeChanging=false;
		        }
			}
		});
		MouseAdapter mouse=new MouseAdapter() {
	        int mx,prevMouseX;
	        int dx;
			@Override
			public void mousePressed(MouseEvent e) {
					prevMouseX=e.getX();
	    			scene.getDraggingPainterObj().clear();
	    			scene.getDraggingPoint().clear(); 
	    			scene.setAllPainterObjDraggable(false);
				for(DraggableItem item:items) {
					if(item.getBounds().contains(e.getPoint())) {
						draggingItem=item;
						scene.getDraggingPainterObj().clear();
						scene.getDraggingPoint().clear();
						scene.addDraggingPainterObj(draggingItem.getPainterObj());
						draggingItem.getPainterObj().setDraggable(true);
						break;
					}
				}
				if(draggingItem!=null)
					draggingItem.setBackground(java.awt.Color.LIGHT_GRAY);
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				mx=e.getX();
				dx=(prevMouseX-mx);
				if(sizeChanging) {
					setIconSize(Math.max(30,getIconSize()+dx));
				}
				else if(draggingItem!=null) {
					draggingItem.setBounds(0,e.getY()-iconSize/2,iconSize,iconSize);
					reorderItems();
				}
				prevMouseX=e.getX();
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				LayerManager.this.scene.revalidate();
				if(draggingItem!=null) {
					snapItems();
					draggingItem.setBackground(java.awt.Color.WHITE);
					draggingItem=null;
				}
			}
		};
		addMouseListener(mouse);
		addMouseMotionListener(mouse);
	}
	
	public void addItem(PainterObj s) {
		DraggableItem item=new DraggableItem(s);
		items.add(item);
		this.add(item);
		layoutItems();
	}
	private void layoutItems() {
		List<DraggableItem> removeList=new ArrayList<>();
		int y=0;
		for(DraggableItem item:items) {
			if(item.painterObj!=null) {
				item.setBounds(0,y,iconSize,iconSize);
				y+=iconSize;
			}
			else {
				removeList.add(item);
			}
		}
		for(DraggableItem item:removeList)
			removeItem(item);
		revalidate();
	}
	
	public void removeItem(DraggableItem item) {
	    items.remove(item);
	    this.remove(item);
	    revalidate();
	}
	
	private void snapItems() {
		items.sort((a,b)->Integer.compare(a.getY(),b.getY()));
		this.scene.getNote().saveInfo();
		layoutItems();
	}
	public void clearAllItems() {
	    items.clear();
	    removeAll();
	    revalidate();
	    repaint();
	}
	private void reorderItems() {
		items.sort((a,b)->Integer.compare(a.getY(), b.getY()));
		this.scene.setAllPainterObj(new ArrayList<>());
		for(DraggableItem item:items) {
			this.scene.addPainterObj(item.getPainterObj());
		}
		int y=0;
		draggingItem.getParent().setComponentZOrder(draggingItem, 0);
		for(DraggableItem item:items) {
			if(!item.equals(draggingItem)) {
				item.setBounds(0,y,iconSize,iconSize);
			}
			y+=iconSize;
		}
	}
	
	public Dimension getPreferredSize( ) {
		int height=items.size()*iconSize;
		return new Dimension(200,height);
	}
	@Override
	public void doLayout() {
		layoutItems();
	}
	
	private void drawPainterObj(Graphics g, PainterObj painterObj) {//the logic is promise that all point is in the panel(if your painting is out of your point, it possibly shows weird
	    double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
	    double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
	    for (Point p : painterObj.getEdge()) {
	        if (p.getX() < minX) minX = p.getX();
	        if (p.getY() < minY) minY = p.getY();
	        if (p.getX() > maxX) maxX = p.getX();
	        if (p.getY() > maxY) maxY = p.getY();
	    }
	    double width = maxX - minX;
	    double height = maxY - minY;
	    if (width == 0 || height == 0) return;
	    int panelWidth = iconSize;
	    int panelHeight = iconSize;
	    double tranB=(Math.max(width,height));
	    painterObj.draw(g,Math.min(panelWidth, panelHeight)/tranB,-minX*panelWidth/tranB,-minY*panelHeight/tranB);
	    
	    //(x-a)*b==>(x*r)+d  --> (x*b)-(a*b), a=minX & minY,b=panelWidth/Width & panelHeight/height
	    //(x-minX)*50/width=x*50/width - minX*50/width
	    //(x-minY)*50/Height=x*50/height - minY*50/height
	}

	
	class DraggableItem extends JPanel {
		
		@Override
	    protected void paintComponent(Graphics g) {
	        super.paintComponent(g);
	        drawPainterObj(g, this.painterObj);
	    }
		
		private static final long serialVersionUID = 191952922949924862L;
		private PainterObj painterObj;
		public void setPainterObj(PainterObj s) {
			this.painterObj=s;
			repaint();
		}
		public PainterObj getPainterObj() {
			return this.painterObj;
		}
	    public DraggableItem(PainterObj s) {
	    		this.painterObj=s;
			this.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK));
	        this.setBackground(java.awt.Color.WHITE);
	    }
	}
	public boolean isOperating() {
		return this.isOperating;
	}
	@Override public void mousePressed(MouseEvent e) {
		LayerManager.this.isOperating=true;
	}
	@Override public void mouseReleased(MouseEvent e) {
		LayerManager.this.isOperating=false;
	}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
	@Override public void mouseClicked(MouseEvent e) {}
}	