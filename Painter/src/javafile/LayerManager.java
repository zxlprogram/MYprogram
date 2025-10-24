package javafile;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class LayerManager extends JPanel implements MouseListener{
	private static final long serialVersionUID = 7672158295712151948L;
	private Scene scene;
	private final List<DraggableItem>items=new ArrayList<>();
	private boolean isOperating=false;
	private DraggableItem draggingItem=null;
	public LayerManager(Scene scene) {
		this.scene=scene;
		this.setBackground(java.awt.Color.BLACK);
		addMouseListener(this);
		MouseAdapter mouse=new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
	    			scene.getDraggingSurface().clear();
	    			scene.getDraggingPoint().clear(); 
	    			for(PainterObj s:scene.getAllSurface()) {
	    				s.setDraggable(false);
	    				if(s.getEdge()!=null)
	    				for(Point p:s.getEdge())
	    					p.setDraggable(false);
	    			}
				for(DraggableItem item:items) {
					if(item.getBounds().contains(e.getPoint())) {
						draggingItem=item;
						scene.getDraggingSurface().clear();
						scene.getDraggingPoint().clear();
						scene.addDraggingSurface(draggingItem.getSurface());
						draggingItem.getSurface().setDraggable(true);
						break;
					}
				}
				if(draggingItem!=null)
					draggingItem.setBackground(java.awt.Color.LIGHT_GRAY);
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				if(draggingItem!=null) {
					draggingItem.setLocation(0,e.getY()-draggingItem.getHeight()/2);
					reorderItems();
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
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
			if(item.surface!=null) {
				item.setBounds(0,y,50,50);
				y+=50;
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
	    this.remove(item);      // 從面板上移除元件
	    revalidate();           // 重新驗證佈局
	    repaint();              // 重繪畫面
	}
	
	private void snapItems() {
		items.sort((a,b)->Integer.compare(a.getY(),b.getY()));
		this.scene.getNote().saveInfo(this.scene.getAllSurface(),this.scene.getScale(),this.scene.getOffsetX(),this.scene.getOffsetY());
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
		this.scene.setAllSurface(new ArrayList<>());
		for(DraggableItem item:items) {
			this.scene.addSurface(item.getSurface());
		}
		int y=0;
		for(DraggableItem item:items) {
			if(item!=draggingItem) {
				item.setBounds(0,y,50,50);
			}
			y+=50;
		}
	}
	
	public Dimension getPreferredSize( ) {
		int height=items.size()*50;
		return new Dimension(200,height);
	}
	@Override
	public void doLayout() {
		layoutItems();
	}
	

	private void drawSurface(Graphics g, PainterObj surface) {//the logic is promise that all point is in the panel(if your painting is out of your point, it possibly shows weird
	    double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
	    double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
	    for (Point p : surface.getEdge()) {
	        if (p.getX() < minX) minX = p.getX();
	        if (p.getY() < minY) minY = p.getY();
	        if (p.getX() > maxX) maxX = p.getX();
	        if (p.getY() > maxY) maxY = p.getY();
	    }
	    double width = maxX - minX;
	    double height = maxY - minY;
	    if (width == 0 || height == 0) return;
	    int panelWidth = 50;
	    int panelHeight = 50;
	    double tranB=(Math.max(width,height));
	    surface.setDrawingColor(g,surface);
	    surface.draw(g,Math.min(panelWidth, panelHeight)/tranB,-minX*panelWidth/tranB,-minY*panelHeight/tranB);
	    
	    //(x-a)*b==>(x*r)+d  --> (x*b)-(a*b), a=minX & minY,b=panelWidth/Width & panelHeight/height
	    //(x-minX)*50/width=x*50/width - minX*50/width
	    //(x-minY)*50/Height=x*50/height - minY*50/height
	}

	
	class DraggableItem extends JPanel {
		
		@Override
	    protected void paintComponent(Graphics g) {
	        super.paintComponent(g);
	        drawSurface(g, this.surface);
	    }
		
		private static final long serialVersionUID = 191952922949924862L;
		private PainterObj surface;
		public void setSurface(PainterObj s) {
			this.surface=s;
			repaint();
		}
		public PainterObj getSurface() {
			return this.surface;
		}
	    public DraggableItem(PainterObj s) {
	    		this.surface=s;
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

