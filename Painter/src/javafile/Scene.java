/** 
 * 
* the painter is base on java swing
*
* @author: z.x.l
* @since: 2025-10-13
* 
*/
package javafile;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
/**
 * @since 2025-10-14
 * 
 * this part I give AI write it, here is the method we charging our interface to swing
 * 
 * allSurfaces is a list
 * draggingPoint and draggingSurface is the object which we are choosing
 * prevMouseX,Y is used to record the mouse coordinate
 * offsetX,Y,scale is used to let all surface to fit the windows size
 * 
 * when delete pressed, if nothing were be choosing, it will remove the last element of all_surface (list)
 * we make all event and listener at here, if you want to add new thing and you need to control the outside object, just let your function have an input value
 * 
 * dragging the point takes precedence over dragging the surface
 * 
 * use the function execute() to start the application
 * 
 * we have the auto repaint, don't worry about this
 * 		
 */
public class Scene extends JPanel implements MouseListener, MouseMotionListener,KeyListener {//AI接手mouseEvent, base-on-swing
    private static final long serialVersionUID = 1L;
    static final String appName = "Painter";
    private java.util.List<Surface> allSurfaces = new ArrayList<>();
    private Surface draggingSurface = null;
    private Point draggingPoint = null;
    private int prevMouseX, prevMouseY;
    private double scale;
    private double offsetX;
    private double offsetY;

    private final int POINT_RADIUS = 10;
    
    public void execute() {
    		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException|InstantiationException|IllegalAccessException|UnsupportedLookAndFeelException e) {}
        JFrame frame = new JFrame(appName);
        ToolList toolList=new ToolList(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setSize(400, 400);
        frame.setContentPane(this);
        frame.setVisible(true);
        this.setBackground(java.awt.Color.WHITE);
        this.add(toolList,BorderLayout.NORTH);
        addMouseListener(this);
        addKeyListener(this);
        addMouseMotionListener(this);

        setFocusable(true);
        requestFocusInWindow();
        
        Timer timer = new Timer(16, e -> repaint());
        timer.start();
    }

    public void addSurface(Surface s) {
        this.allSurfaces.add(s);
    }

    public void removeSurface(Surface s) {
        this.allSurfaces.remove(s);
    }
    public List<Surface>getAllSurface() {
    		return this.allSurfaces;
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();
        scale = Math.min(width, height) / 10.0;
        offsetX = width / 2.0;
        offsetY = height / 2.0;
        for (Surface s : allSurfaces) {
            drawSurface(g, s);
        }
    }


    private void drawSurface(Graphics g, Surface s) {
        Point[] points = s.getEdge();
        if (points.length < 2) return;

        int width = getWidth();
        int height = getHeight();

        double scale = Math.min(width, height) / 10.0; // 調整比例
        double offsetX = width / 2.0;
        double offsetY = height / 2.0;

        int[] xPoints = new int[points.length];
        int[] yPoints = new int[points.length];

        for (int i = 0; i < points.length; i++) {
            xPoints[i] = (int)(points[i].getX() * scale + offsetX);
            yPoints[i] = (int)(points[i].getY() * scale + offsetY);
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


    @Override
    public void mousePressed(MouseEvent e) {
    		draggingSurface=null;
    		this.requestFocusInWindow();
        int mx = e.getX();
        int my = e.getY();
        // 先找拖拽點
        for (Surface s : allSurfaces) {
            for (Point p : s.getEdge()) {
            	int px = (int)(p.getX() * scale + offsetX);
            int py = (int)(p.getY() * scale + offsetY);
                double dist = Math.hypot(mx - px, my - py);
                if (dist <= POINT_RADIUS) {
                    draggingPoint = p;
                    prevMouseX = mx;
                    prevMouseY = my;
                    return; // 找到點就拖點，不再拖多邊形
                }
            }
        }

        // 找不到拖點，嘗試拖整個多邊形
        for (Surface s : allSurfaces) {
            if (isPointInSurface(mx, my, s)) {
                draggingSurface = s;
                prevMouseX = mx;
                prevMouseY = my;
                break;
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        double dx = (mx - prevMouseX) / scale;
        double dy = (my - prevMouseY) / scale;

        if (draggingPoint != null) {
            draggingPoint.setX(draggingPoint.getX() + dx);
            draggingPoint.setY(draggingPoint.getY() + dy);
        } else if (draggingSurface != null) {
            draggingSurface.moveX(dx);
            draggingSurface.moveY(dy);
        }

        prevMouseX = mx;
        prevMouseY = my;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        draggingPoint = null;
    }

    private boolean isPointInSurface(int mx, int my, Surface s) {
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
		case KeyEvent.VK_DELETE:
			if(draggingSurface!=null)
				this.removeSurface(draggingSurface);
			break;
		}
   	}
   	@Override
   	public void mouseClicked(MouseEvent e) {// change color
   		Surface choisedSurface = null;
   	    	switch (e.getButton()) {
   	        case MouseEvent.BUTTON3:
   	            for (Surface s : allSurfaces) {
   	                if (isPointInSurface(e.getX(), e.getY(), s)) {
   	                    choisedSurface = s;
   	                    prevMouseX = e.getX();
   	                    prevMouseY = e.getY();
   	                    draggingSurface = s;
   	                    break;
   	                }
   	            }
   	            if (choisedSurface != null && choisedSurface.getColor() != null) {
   	            		SwingUtilities.invokeLater(() -> new ChoiceColor(draggingSurface));
   	            }
   	            break;
   	        default:
   	            break;
   	    }
   	}
   	private class ChoiceColor extends JFrame{
   		private static final long serialVersionUID = 1L;
   		private JTextField R=new JTextField(5),G=new JTextField(5),B=new JTextField(5);
   		private JButton enter=new JButton("Enter");
   		public ChoiceColor(Surface s) {
   			this.setSize(300,100);
   			this.setTitle("choice color");
   			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
   			this.setLayout(new FlowLayout());
   			this.add(new JLabel("R:"));
   			this.add(R);
   			this.add(new JLabel("G:"));
   			this.add(G);
   			this.add(new JLabel("B:"));
   			this.add(B);
   			enter.addActionListener(e-> {
   				try {
   					double Rtext=Double.parseDouble(R.getText());
   					double Gtext=Double.parseDouble(G.getText());
   					double Btext=Double.parseDouble(B.getText());
   					if(Rtext<0||Rtext>1||Gtext<0||Gtext>1||Btext<0||Btext>1)
   						throw new IllegalArgumentException();
   					s.setColor(Rtext,Gtext,Btext);
   					this.dispose();
   				}
   				catch(IllegalArgumentException e2) {
   					JOptionPane.showMessageDialog(this,"Please enter current number(0~1)","Enter error",JOptionPane.ERROR_MESSAGE);
   				}
   				
   			});
   			this.add(enter);
   			this.setVisible(true);
   		}
   	}
   	@Override public void mouseEntered(MouseEvent e) {}
   	@Override public void mouseExited(MouseEvent e) {}
   	@Override public void mouseMoved(MouseEvent e) {}
   	@Override public void keyTyped(KeyEvent e) {}
   	@Override public void keyReleased(KeyEvent e) {}
}
/**
 * when you click mouse_keyRight on surface,it will show you three text field,you can enter the number(0~1) to change the surface's color
 * if your entered is invalid, it will show a dialog told you you have a wrong operate
 * 
 */
