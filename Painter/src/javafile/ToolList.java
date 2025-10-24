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
 * 
 */
class ToolList extends JPanel {
	private static final long serialVersionUID = 1L;
	private List<Component>toolList=new ArrayList<>();
	private Scene scene;
	public ToolList(Scene scene) {
		this.scene=scene;
		super.setLayout(new FlowLayout());
		addAllTool();
	}
	private void addAllTool() {
		toolList.add(new Tool(ToolList.class.getResource("/triangle.png"),()->{
			Surface t=Surface.TRIANGLE(this.scene);
			t.setColor(1,0,0);
			scene.addSurface(t);
		}));
		toolList.add(new Tool(ToolList.class.getResource("/quad.png"),()->{
			Surface t=Surface.QUAD(this.scene);
			t.setColor(1,0,0);
			scene.addSurface(t);
		}));
		toolList.add(new Tool(ToolList.class.getResource("/circle.png"),()->{
			Circle t=Circle.CIRCLE(this.scene);
			t.setColor(1,0,0);
			scene.addSurface(t);
		}));
		JTextField enterMoreEdge=new JTextField(5);
		enterMoreEdge.setPreferredSize(new Dimension(0,31));
		toolList.add(new Tool(ToolList.class.getResource("/Nedge_SS.png"),()-> {
			this.setPreferredSize(new Dimension(83,41));
			Surface t=new Surface(this.scene);
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
		}));
		toolList.add(enterMoreEdge);
		JTextField enterMoreBezierEdge=new JTextField(5);
		enterMoreBezierEdge.setPreferredSize(new Dimension(0,31));
		toolList.add(new Tool(ToolList.class.getResource("/Nedge_BS.png"),()-> {
			this.setPreferredSize(new Dimension(83,41));
			BezierSurface t=new BezierSurface(this.scene);
			int edge;
			try {
				edge=Integer.parseInt(enterMoreBezierEdge.getText());
			}
			catch(IllegalArgumentException e) {
				edge=4;
			}
			for(int i=0;i<edge;i++)
				t.addPoint(Math.random(),Math.random());
			t.setColor(1,0,0);
			scene.addSurface(t);
		}));
		toolList.add(enterMoreBezierEdge);
		JTextField enterMoreLine=new JTextField(5);
		enterMoreLine.setPreferredSize(new Dimension(0,31));
		toolList.add(new Tool(ToolList.class.getResource("/Nedge_SL.png"),()-> {
			this.setPreferredSize(new Dimension(83,41));
			Line t=new Line(this.scene);
			int edge;
			try {
				edge=Integer.parseInt(enterMoreLine.getText());
			}
			catch(IllegalArgumentException e) {
				edge=4;
			}
			for(int i=0;i<edge;i++)
				t.addPoint(Math.random(),Math.random());
			t.setColor(1,0,0);
			scene.addSurface(t);
		}));
		toolList.add(enterMoreLine);
		JTextField enterMoreBezLine=new JTextField(5);
		enterMoreBezLine.setPreferredSize(new Dimension(0,31));
		toolList.add(new Tool(ToolList.class.getResource("/Nedge_BL.png"),()-> {
			this.setPreferredSize(new Dimension(83,41));
			BezierLine t=new BezierLine(this.scene);
			int edge;
			try {
				edge=Integer.parseInt(enterMoreBezLine.getText());
			}
			catch(IllegalArgumentException e) {
				edge=4;
			}
			for(int i=0;i<edge;i++)
				t.addPoint(Math.random(),Math.random());
			t.setColor(1,0,0);
			scene.addSurface(t);
		}));
		toolList.add(enterMoreBezLine);
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
		@SuppressWarnings("unused")
		public Tool(URL url,Runnable r) {
			ImageIcon ico=new ImageIcon(url);
			Image scaledImage=ico.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
			ImageIcon scaledIcon=new ImageIcon(scaledImage);
			super(scaledIcon);
			this.action=r;
			addActionListener(e -> {
	            if (this.action != null) {
	                this.action.run(); 
	            }
                scene.requestFocusInWindow();
				scene.getNote().saveInfo(scene.getAllSurface(),scene.getScale(),scene.getOffsetX(),scene.getOffsetY());
			});
		}
		@SuppressWarnings("unused")
		public Tool(String name,Runnable r) {
			super(name);
			this.name=name;
			this.action=r;
			addActionListener(e-> {
	            if (this.action != null) {
	                this.action.run(); 
	            }
                scene.requestFocusInWindow();
    				scene.getNote().saveInfo(scene.getAllSurface(),scene.getScale(),scene.getOffsetX(),scene.getOffsetY());
	        });
		}
	}
}
