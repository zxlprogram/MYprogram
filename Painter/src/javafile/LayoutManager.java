package javafile;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class LayoutManager extends JPanel {
	private static final long serialVersionUID = 7672158295712151948L;
	private final List<DraggableItem>items=new ArrayList<>();
	private DraggableItem draggingItem=null;
	private JScrollPane scrollPane;
	public JScrollPane getScrollPane() {
		return this.scrollPane;
	}
	public LayoutManager(Scene scene) {
		this.setBackground(java.awt.Color.GRAY);
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

        this.setPreferredSize(new Dimension(200,0));
        scrollPane=new JScrollPane(this);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	}
	
	public void addAllItem(List<Surface>list) {
		this.removeAll();
		for(Surface s:list) {
			DraggableItem item=new DraggableItem(s.toString());
			items.add(item);
			this.add(item);
		}
		layoutItems();
	}
	private void layoutItems() {
		int y=0;
		for(DraggableItem item:items) {
			item.setBounds(0,y,this.getWidth(),50);
			y+=50;
		}
		revalidate();
	}
	
	private void snapItems() {
		items.sort((a,b)->Integer.compare(a.getY(),b.getY()));
		layoutItems();
	}
	
	private void reorderItems() {
		items.sort((a,b)->Integer.compare(a.getY(), b.getY()));
		int y=0;
		for(DraggableItem item:items) {
			if(item!=draggingItem) {
				item.setLocation(0,y);
			}
			y+=50;
		}
	}
	
	public Dimension getPreferredSize( ) {
		int height=items.size()*50;
		return new Dimension(200,height);
	}
	public List<String> getOrder() {
		List<String>order=new ArrayList<>();
		for(DraggableItem item:items) {
			order.add(item.getName());
		}
		return order;
	}
	
	@Override
	public void doLayout() {
		layoutItems();
	}
	class DraggableItem extends JPanel {
		private static final long serialVersionUID = 191952922949924862L;
		private final JLabel label;

	    public DraggableItem(String name) {
	        setLayout(new BorderLayout());
	        label = new JLabel(name, JLabel.CENTER);
	        this.add(label, BorderLayout.CENTER);
	        this.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK));
	        this.setBackground(java.awt.Color.WHITE);
	        this.setName(name); // 方便追蹤順序
	    }
	}
}	

