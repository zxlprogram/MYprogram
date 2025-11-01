package javafile;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
/**
 * this is a bar used to save the tool which used to add surface
 * it have a sub-class: Tool
 * 
 */
class ToolList extends JPanel {
	private static final long serialVersionUID = 1L;
	private int panelHeight=85;
	private List<Component>toolList=new ArrayList<>();
	private Scene scene;
	private boolean sizeChanging=false;
	public int getPanelHeight() {
		return this.panelHeight;
	}
	public void setPanelHeight(int i) {
		this.panelHeight=i;
	}
	public ToolList(Scene scene) {
		this.scene=scene;
		super.setLayout(new FlowLayout());
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
		        if (e.getY()<=3||e.getY()>=getHeight()-3) {
		            setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
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
					prevMouseX=e.getY();
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				ToolList.this.scene.revalidate();
				mx=e.getY();
				dx=(mx-prevMouseX);
				if(sizeChanging) {
					setPanelHeight(Math.max(50,getPanelHeight()+dx));
				}
				prevMouseX=e.getY();
			}
		};
		addMouseListener(mouse);
		addMouseMotionListener(mouse);
		addAllTool();
	}
	private int getIntegerValue(JTextField f) {
		int i=4;
		try {
			i=Integer.parseInt(f.getText());
		}
		catch(IllegalArgumentException e) {
		}
		return i;
	}
	public List<Component>getToolList() {
		return this.toolList;
	}
	public void addAllTool() {
		toolList.clear();
		removeAll();
		toolList.add(new Tool(ToolList.class.getResource("/triangle.png"),()->{
			Surface t=Surface.TRIANGLE(this.scene);
			t.setColor(1,0,0);
			scene.addPainterObj(t);
		}));
		toolList.add(new Tool(ToolList.class.getResource("/quad.png"),()->{
			Surface t=Surface.QUAD(this.scene);
			t.setColor(1,0,0);
			scene.addPainterObj(t);
		}));
		toolList.add(new Tool(ToolList.class.getResource("/circle.png"),()->{
			Circle t=Circle.CIRCLE(this.scene);
			t.setColor(1,0,0);
			scene.addPainterObj(t);
		}));
		JTextField enterMoreEdge=new JTextField(5);
		enterMoreEdge.setPreferredSize(new Dimension(0,31));
		toolList.add(new Tool(ToolList.class.getResource("/Nedge_SS.png"),()-> {
			this.setPreferredSize(new Dimension(83,41));
			Surface t=new Surface(this.scene);
			int edge=getIntegerValue(enterMoreEdge);
			for(int i=0;i<edge;i++)
				t.addPoint(Math.random(),Math.random());
			t.setColor(1,0,0);
			scene.addPainterObj(t);
		}));
		toolList.add(enterMoreEdge);
		JTextField enterMoreBezierEdge=new JTextField(5);
		enterMoreBezierEdge.setPreferredSize(new Dimension(0,31));
		toolList.add(new Tool(ToolList.class.getResource("/Nedge_BS.png"),()-> {
			this.setPreferredSize(new Dimension(83,41));
			BezierSurface t=new BezierSurface(this.scene);
			int edge=getIntegerValue(enterMoreBezierEdge);
			for(int i=0;i<edge;i++)
				t.addPoint(Math.random(),Math.random());
			t.setColor(1,0,0);
			scene.addPainterObj(t);
		}));
		toolList.add(enterMoreBezierEdge);
		JTextField enterMoreLine=new JTextField(5);
		enterMoreLine.setPreferredSize(new Dimension(0,31));
		toolList.add(new Tool(ToolList.class.getResource("/Nedge_SL.png"),()-> {
			this.setPreferredSize(new Dimension(83,41));
			Line t=new Line(this.scene);
			int edge=getIntegerValue(enterMoreLine);
			for(int i=0;i<edge;i++)
				t.addPoint(Math.random(),Math.random());
			t.setColor(1,0,0);
			scene.addPainterObj(t);
		}));
		toolList.add(enterMoreLine);
		JTextField enterMoreBezLine=new JTextField(5);
		enterMoreBezLine.setPreferredSize(new Dimension(0,31));
		toolList.add(new Tool(ToolList.class.getResource("/Nedge_BL.png"),()-> {
			this.setPreferredSize(new Dimension(83,41));
			BezierLine t=new BezierLine(this.scene);
			int edge=getIntegerValue(enterMoreBezLine);
			for(int i=0;i<edge;i++)
				t.addPoint(Math.random(),Math.random());
			t.setColor(1,0,0);
			scene.addPainterObj(t);
		}));
		toolList.add(enterMoreBezLine);
		toolList.add(new Tool("Group",()-> {
			Group group=new Group(this.scene);
			for(PainterObj p:this.scene.getDraggingPainterObj()) {
				group.addGroup(p);
			}
			this.scene.getDraggingPainterObj().clear();
			if(!group.getGroup().isEmpty())
			scene.addPainterObj(group);
		}));
		toolList.add(new Tool("disGroup",()-> {
			List<Group>removeList=new ArrayList<>();
			for(PainterObj obj:this.scene.getDraggingPainterObj()) {
				if(obj.getClass().equals(Group.class)) {
					Group g=(Group)obj;
					removeList.add(g);
				}
			}
			for(Group obj:removeList)
				obj.disGroup();
		}));
		JTextField text=new JTextField(5);
		text.setPreferredSize(new Dimension(0,31));
		text.getDocument().addDocumentListener(new DocumentListener() {
		    private void update() {
		        int len = text.getText().length();
		        text.setColumns(Math.max(5, len));
		        if(len>20)
		        	text.setColumns(20);
		        ToolList.this.revalidate();
		    }
		    public void insertUpdate(DocumentEvent e) { update(); }
		    public void removeUpdate(DocumentEvent e) { update(); }
		    public void changedUpdate(DocumentEvent e) { update(); }
		});
		toolList.add(new Tool("Text",()-> {
			Text t=new Text(this.scene,text.getText(),1,1,20);
			t.setColor(1,0,0);
			scene.addPainterObj(t);
		}));
		toolList.add(text);
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
				scene.getNote().saveInfo();
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
    				scene.getNote().saveInfo();
	        });
		}
	}
	
}