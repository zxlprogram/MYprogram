package javafile;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
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
			scene.getNote().saveInfo(scene.getAllSurface());
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
		toolList.add(new Tool("Triangle",()->{
			Surface t=Surface.TRIANGLE();
			t.setColor(1,0,0);
			scene.addSurface(t);
		},scene));
		toolList.add(new Tool("Quad",()->{
			Surface t=Surface.QUAD();
			t.setColor(1,0,0);
			scene.addSurface(t);
		},scene));
		JTextField enterMoreEdge=new JTextField(2);
				toolList.add(new Tool("Circle",()->{
			Surface t=new Surface();
			for(double a=-1;a<=1;a+=0.01)
				t.addPoint(a,Math.sqrt(1-a*a));	
			for(double a=1;a>=-1;a-=0.01)
				t.addPoint(a,-Math.sqrt(1-a*a));	
			t.setColor(1,0,0);
			scene.addSurface(t);
		},scene));
		toolList.add(new Tool("more edge:",()-> {
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
		},scene));
		toolList.add(enterMoreEdge);

		for(Component t:toolList)
			super.add(t);
		
	}
	public class Tool extends JButton {
		private static final long serialVersionUID = 1L;
		private String name;
		private Runnable action;
		private Icon icon;
		public String getName() {
			return this.name;
		}
		public void setName(String name) {
			this.name=name;
			this.setText(name);
		}
		public Tool(Icon icon,Runnable r,Scene scene) {
			super(icon);
			this.icon=icon;
			this.action=r;
			addActionListener(e -> {
	            if (this.action != null) {
	                this.action.run(); 
	            }
                scene.requestFocusInWindow();
    				scene.getNote().saveInfo(scene.getAllSurface());
			});
		}
		public Tool(String name,Runnable r,Scene scene) {
			super(name);
			this.name=name;
			this.action=r;
			addActionListener(e -> {
	            if (this.action != null) {
	                this.action.run(); 
	            }
                scene.requestFocusInWindow();
    				scene.getNote().saveInfo(scene.getAllSurface());
	        });
		}
	}
}
