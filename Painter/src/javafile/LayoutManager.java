package javafile;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class LayoutManager extends JPanel {
	private static final long serialVersionUID = 7672158295712151948L;
	private Scene scene;
	private final List<DraggableItem>items=new ArrayList<>();
	private DraggableItem draggingItem=null;
	public LayoutManager(Scene scene) {
		this.scene=scene;
		this.setBackground(java.awt.Color.BLACK);
		MouseAdapter mouse=new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				for(DraggableItem item:items) {
					if(item.getBounds().contains(e.getPoint())) {
						draggingItem=item;
						break;
					}
				}
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				if(draggingItem!=null) {
					draggingItem.setLocation(0,e.getY()-25);
					draggingItem.setBackground(java.awt.Color.LIGHT_GRAY);
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
	
	public void addItem(Surface s) {
		DraggableItem item=new DraggableItem();
		item.setSurface(s);
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
	

	private void drawSurface(Graphics g, Surface s) {
	    Point[] points = s.getEdge();
	    if (points.length < 2) return;
	    double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
	    double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
	    for (Point p : points) {
	        if (p.getX() < minX) minX = p.getX();
	        if (p.getY() < minY) minY = p.getY();
	        if (p.getX() > maxX) maxX = p.getX();
	        if (p.getY() > maxY) maxY = p.getY();
	    }
	    double width = maxX - minX;
	    double height = maxY - minY;
	    if (width == 0 || height == 0) return;
	    int[] xPoints = new int[points.length];
	    int[] yPoints = new int[points.length];

	    int panelWidth = 50;
	    int panelHeight = 50;
	    for (int i = 0; i < points.length; i++) {
	        xPoints[i] = (int) ((points[i].getX() - minX) / width * panelWidth);
	        yPoints[i] = (int) ((points[i].getY() - minY) / height * panelHeight);
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

	
	class DraggableItem extends JPanel {
		
		@Override
	    protected void paintComponent(Graphics g) {
	        super.paintComponent(g);
	        drawSurface(g, this.surface);
	    }
		
		private static final long serialVersionUID = 191952922949924862L;
		private Surface surface;
		public void setSurface(Surface s) {
			this.surface=s;
			repaint();
		}
		public Surface getSurface() {
			return this.surface;
		}
	    public DraggableItem() {
			this.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK));
	        this.setBackground(java.awt.Color.WHITE);
	    }
	}
}	

