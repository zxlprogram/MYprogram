package javafile;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
/**
 * this is a bar used to save the tool which used to add surface
 * it have a sub-class: Tool
 * when you want to add new Tool, goto function addAllTool(Scene),and write like this:
 * 
 * 		toolList.add(new Tool("{@value}"
			Surface t=new Surface();
			
			
			[describe the surface here]
			
			scene.addSurface(t);
		},scene));
		
		or add another component
 * 
 */
class ToolList extends JPanel {
	private static final long serialVersionUID = 1L;
	private List<Component>toolList=new ArrayList<>();
	public ToolList(Scene scene) {
		super.setLayout(new FlowLayout());
		addAllTool(scene);
	}
	private void addAllTool(Scene scene) {
		toolList.add(new Tool(ToolList.class.getResource("/triangle.png"),()->{
			Surface t=Surface.TRIANGLE();
			t.setColor(1,0,0);
			scene.addSurface(t);
			scene.getLayerManager().addItem(t);
		},scene));
		toolList.add(new Tool(ToolList.class.getResource("/quad.png"),()->{
			Surface t=Surface.QUAD();
			t.setColor(1,0,0);
			scene.addSurface(t);
			scene.getLayerManager().addItem(t);
		},scene));
		JTextField enterMoreEdge=new JTextField(5);
		toolList.add(new Tool(ToolList.class.getResource("/circle.png"),()->{
			Surface t=new Surface();
			for(double a=-1;a<=1;a+=0.01)		
				t.addPoint(a,Math.sqrt(1-a*a));	
			for(double a=1;a>=-1;a-=0.01)
				t.addPoint(a,-Math.sqrt(1-a*a));	
			t.setColor(1,0,0);
			scene.addSurface(t);
			scene.getLayerManager().addItem(t);
		},scene));
		Tool moreEdge=new Tool("more edge:",()-> {
			Surface t=new Surface();
			int edge;
			try {
				edge=Integer.parseInt(enterMoreEdge.getText());
			}
			catch(IllegalArgumentException e) {
				edge=4;
			}
			for(int i=0;i<edge;i++)
				t.addPoint(Math.random(),Math.random());
			t.setColor(1,0,0);
			scene.addSurface(t);
			scene.getLayerManager().addItem(t);
		},scene);
		moreEdge.setPreferredSize(new Dimension(83,41));
		enterMoreEdge.setPreferredSize(new Dimension(0,31));
		toolList.add(moreEdge);
		toolList.add(enterMoreEdge);

		for(Component t:toolList)
			super.add(t);
		
	}
	public class Tool extends JButton {
		private static final long serialVersionUID = 1L;
		private String name;
		private Runnable action;
		public String getName() {
			return this.name;
		}
		public void setName(String name) {
			this.name=name;
			this.setText(name);
		}
		public Tool(URL url,Runnable r,Scene scene) {
			ImageIcon ico=new ImageIcon(url);
			Image scaledImage=ico.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
			ImageIcon scaledIcon=new ImageIcon(scaledImage);
			super(scaledIcon);
			this.action=r;
			addActionListener(_ -> {
	            if (this.action != null) {
	                this.action.run(); 
	            }
                scene.requestFocusInWindow();
				scene.getNote().saveInfo(scene.getAllSurface(),scene.getScale(),scene.getOffsetX(),scene.getOffsetY());
			});
		}
		public Tool(String name,Runnable r,Scene scene) {
			super(name);
			this.name=name;
			this.action=r;
			addActionListener(_-> {
	            if (this.action != null) {
	                this.action.run(); 
	            }
                scene.requestFocusInWindow();
    				scene.getNote().saveInfo(scene.getAllSurface(),scene.getScale(),scene.getOffsetX(),scene.getOffsetY());
	        });
		}
	}
}
