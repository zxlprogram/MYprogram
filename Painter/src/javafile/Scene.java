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
public class Scene extends JPanel
		implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener, DropTargetListener {// base-on-swing
	private static final long serialVersionUID = 1L;
	// _________________________________________DESCRIPTION______________________
	static final String appName = "Painter";
	static final String version = "1.10";

	// _________________________________________OPERATION________________________
	private java.util.List<PainterObj> allPainterObj = new ArrayList<>();
	private List<PainterObj> draggingPainterObj = new ArrayList<>();
	private List<Point> draggingPoint = new ArrayList<>();
	private ExportLoadSystem saveLoader = new ExportLoadSystem(this);
	private LayerManager layerManager = new LayerManager(this);
	private ToolList toolList = new ToolList(this);
	private Note note = new Note();
	private JPanel mainPanel = new JPanel();
	private JFrame frame = new JFrame();
	private JScrollPane scroll = new JScrollPane();
	private int prevMouseX, prevMouseY, pressedLocationX, pressedLocationY; // events listener
	private double scale, offsetX, offsetY; // camera
	private Map<String, Class<? extends PainterObj>> trans = new HashMap<>();
	private final int POINT_RADIUS = 10;
	private List<PainterObj> trash = new ArrayList<>();

	// _________________________________________PAINTEROBJ_SIGN__________________
	private void buildFileFormat() {
		trans.put("SS", Surface.class);
		trans.put("SL", Line.class);
		trans.put("BS", BezierSurface.class);
		trans.put("BL", BezierLine.class);
		trans.put("Cr", Circle.class);
		trans.put("G:", Group.class);
		trans.put("Tx", Text.class);
	}

	public Map<String, Class<? extends PainterObj>> getObjTranslator() {
		return this.trans;
	}

	// _________________________________________HIDDEN_CONTROLLER________________
	protected class Note extends Stack<Note.Event> {
		private Stack<Event> redoStack = new Stack<>();
		private static final long serialVersionUID = 1L;
		private int MAX_SAVING_EVENT = 100;

		private class Event {
			private List<PainterObj> graphic;
			private double scale;
			private double offsetX;
			private double offsetY;

			public Event(List<PainterObj> graphic, double scale, double offsetX, double offsetY) {
				this.graphic = graphic;
				this.scale = scale;
				this.offsetX = offsetX;
				this.offsetY = offsetY;
			}

			public List<PainterObj> getAllPainterObj() {
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
		}

		private Event copyPainterObjList(Event allList) {
			List<PainterObj> copy = new ArrayList<>();
			for (PainterObj s : allList.getAllPainterObj()) {
				copy.add(s.clone());
			}
			return new Event(copy, allList.getScale(), allList.getOffsetX(), allList.getOffsetY());
		}

		private void prepareNote(double scale, double offsetX, double offsetY) {
			this.redoStack.push(new Event(new ArrayList<>(), scale, offsetX, offsetY));
		}

		public void redo(Scene scene) {
			if (this.redoStack.size() > 0) {
				Event undoList = copyPainterObjList(this.redoStack.pop());
				scene.setAllPainterObj(undoList.getAllPainterObj());
				scene.setScale(undoList.scale);
				scene.setOffsetX(undoList.offsetX);
				scene.setOffsetY(undoList.offsetY);
				this.push(copyPainterObjList(undoList));
				Scene.this.getDraggingPainterObj().clear();
			}
		}

		public void undo(Scene scene) {
			if (this.size() > 1) {
				this.redoStack.push(copyPainterObjList(this.pop()));
				scene.setAllPainterObj(copyPainterObjList(this.peek()).getAllPainterObj());
				scene.setScale(this.peek().getScale());
				scene.setOffsetX(this.peek().getOffsetX());
				scene.setOffsetY(this.peek().getOffsetY());
				Scene.this.getDraggingPainterObj().clear();
			}
		}

		public void saveInfo() {
			List<PainterObj> copyList = new ArrayList<>();
			for (PainterObj p : Scene.this.allPainterObj)
				copyList.add(p.clone());
			this.push(
					copyPainterObjList(new Event(copyList, Scene.this.scale, Scene.this.offsetX, Scene.this.offsetY)));
			if (this.size() > MAX_SAVING_EVENT)
				this.removeFirst();
			this.redoStack.clear();
		}
	}

	protected class ChoiceColor extends JPanel {
		private static final long serialVersionUID = 1L;

		public ChoiceColor(List<PainterObj> painterObj, Scene scene) {
			java.awt.Color color = JColorChooser.showDialog(this, "color choosing board", getBackground());
			try {
				for (PainterObj p : painterObj)
					p.setColor(color.getRed() / 255.0, color.getGreen() / 255.0, color.getBlue() / 255.0);
				note.saveInfo();
			} catch (NullPointerException e) {
			}
		}
	}

	// _________________________________________LAUNCH___________________________
	private void launch(String name) {
		this.buildFileFormat();
		frame.setIconImage(new ImageIcon(Scene.class.getResource("/painter_logo.png")).getImage());
		toolList.setBackground(new java.awt.Color(0, 0, 120));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1000, 500);
		frame.setTitle(name + "(ver: " + version + ")");
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(this, BorderLayout.CENTER);
		frame.setVisible(true);
		frame.setContentPane(mainPanel);
		new DropTarget(this, this);

	}
	public void execute() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
		}
		launch(appName);
		scroll.setViewportView(layerManager);
		scroll.setPreferredSize(new Dimension(layerManager.getIconSize() + 18, 0));
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		mainPanel.add(scroll, BorderLayout.EAST);
		toolList.setPreferredSize(new Dimension(0, toolList.getPanelHeight()));
		mainPanel.add(toolList, BorderLayout.NORTH);
		addMouseListener(this);
		addKeyListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		setFocusable(true);
		frame.addWindowListener(new WindowAdapter() {// to avoid GUI system crash on execution thread
			@SuppressWarnings("unused")
			@Override
			public void windowOpened(WindowEvent e) {
				int width = getWidth();
				int height = getHeight();
				scale = Math.min(width, height) / 10.0;
				offsetX = width / 2.0;
				offsetY = height / 2.0;
				note.prepareNote(scale, offsetX, offsetY);
				URL logo = Scene.class.getResource("/file.vecf");
				saveLoader.getLoader().loadFile(logo);
				requestFocusInWindow();
				new javax.swing.Timer(5, e2 -> {
					repaint();
					counter++;
					Runtime rt=Runtime.getRuntime();
					long used=rt.totalMemory()-rt.freeMemory();
					System.out.println(used/1024/1024+" "+counter);
				}).start();
			}
		});
	}

	static int counter=0;
	
	public void browserMode(String path) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
		}
		launch("Browser");
		if (path != null)
			this.saveLoader.getLoader().loadFile(path);
		repaint();
	}

	// _________________________________________REMOVER__________________________
	public void removePainterObj(PainterObj s) {
		this.allPainterObj.remove(s);
	}

	@Override
	public void removeAll() {
		this.allPainterObj.clear();
	}

	// _________________________________________ADDER____________________________
	public void addPainterObj(PainterObj s) {
		this.allPainterObj.add(s);
	}

	public void addDraggingPoint(Point p) {
		if (!this.draggingPoint.contains(p)) {
			this.draggingPoint.add(p);
		}
	}

	public void addDraggingPainterObj(PainterObj p) {
		if (!this.draggingPainterObj.contains(p))
			this.draggingPainterObj.add(p);
	}

	// _________________________________________GETTER___________________________
	public List<PainterObj> getDraggingPainterObj() {
		return this.draggingPainterObj;
	}

	public List<Point> getDraggingPoint() {
		return this.draggingPoint;
	}

	public List<PainterObj> getAllPainterObj() {
		return this.allPainterObj;
	}

	public Note getNote() {
		return this.note;
	}

	public LayerManager getLayerManager() {
		return this.layerManager;
	}

	public double getOffsetX() {
		return this.offsetX;
	}

	public double getOffsetY() {
		return this.offsetY;
	}

	public double getScale() {
		return this.scale;
	}

	// _________________________________________SETTER____________________________
	public void setAllPainterObj(List<PainterObj> list) {
		this.allPainterObj = list;
	}

	public void setDraggingPainterObj(List<PainterObj> s) {
		this.draggingPainterObj = s;
	}

	public void setAllPainterObjDraggable(boolean n) {
		for (PainterObj p : this.allPainterObj)
			p.setDraggable(n);
	}

	public void setDraggingPoint(List<Point> p) {
		this.draggingPoint = p;
	}

	public void setOffsetX(double d) {
		this.offsetX = d;
	}

	public void setOffsetY(double d) {
		this.offsetY = d;
	}

	public void setScale(double d) {
		this.scale = d;
	}

	// _________________________________________GRAPHIC_CONTROL___________________
	private void selectItems(MouseEvent e) {
		if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == 0) {
			this.getDraggingPainterObj().clear();
			this.getDraggingPoint().clear();
			for (PainterObj s : this.allPainterObj) {
				s.setDraggable(false);
			}
		}
		prevMouseX = e.getX();
		prevMouseY = e.getY();
		pressedLocationX = e.getX();
		pressedLocationY = e.getY();
		this.requestFocusInWindow();
		int mx = e.getX();
		int my = e.getY();
		Point point = null;
		if (this.getDraggingPainterObj().isEmpty()) {
			find: for (int i = allPainterObj.size() - 1; i >= 0; i--) {
				if (allPainterObj.get(i).getEdge() != null) {
					for (Point p : allPainterObj.get(i).getEdge()) {
						int px = (int) (p.getX() * scale + offsetX);
						int py = (int) (p.getY() * scale + offsetY);
						double dist = Math.hypot(mx - px, my - py);
						if (dist <= POINT_RADIUS) {
							p.setDraggable(true);
							addDraggingPoint(p);
							point = p;
							prevMouseX = mx;
							prevMouseY = my;
							break find;
						}
					}
				}
			}
		}
		for (int i = allPainterObj.size() - 1; i >= 0; i--) {
			if (allPainterObj.get(i).isPointInPainterObj(mx, my, scale, offsetX, offsetY)
					&& (point == null || this.allPainterObj.indexOf(point.getPainterObj()) < i)) {
				allPainterObj.get(i).setDraggable(true);
				this.addDraggingPainterObj(this.allPainterObj.get(i));
				prevMouseX = mx;
				prevMouseY = my;
				break;
			}
		}
		if (!draggingPainterObj.isEmpty()) {
			draggingPoint.clear();
			for (PainterObj s : this.allPainterObj) {
				for (Point p : s.getEdge())
					p.setDraggable(false);
			}
		}
	}

	private void drawPoint(Graphics g, Point p) {
		g.setColor(java.awt.Color.BLACK);
		g.fillOval((int) (p.getX() * scale + offsetX) - POINT_RADIUS / 2,
				(int) (p.getY() * scale + offsetY) - POINT_RADIUS / 2, POINT_RADIUS, POINT_RADIUS);
	}

	@Override // be called when repaint()
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		toolList.setPreferredSize(new Dimension(0, toolList.getPanelHeight()));
		scroll.setPreferredSize(new Dimension(layerManager.getIconSize() + 18, 0));
		if (this.getLayerManager() != null && !this.getLayerManager().isOperating()) {
			this.getLayerManager().clearAllItems();
			for (PainterObj s : this.allPainterObj) {
				this.getLayerManager().addItem(s);
			}
		}
		for (PainterObj s : allPainterObj) {
			if (s.islegalObj()) {
				s.draw(g, scale, offsetX, offsetY);
				for (Point p : s.getEdge()) {
					if (p.draggable() || p.getPainterObj().Draggable()) {
						drawPoint(g, p);
					}
				}
			} else {
				trash.add(s);
			}
		}
		for (PainterObj p : trash) {
			allPainterObj.remove(p);
			draggingPainterObj.remove(p);
		}
	}

	// _________________________________________EVENTS____________________________
	@Override
	public void mousePressed(MouseEvent e) {
		selectItems(e);
		switch (e.getButton()) {
		case MouseEvent.BUTTON3:
			if (!draggingPainterObj.isEmpty())
				SwingUtilities.invokeLater(() -> new ChoiceColor(draggingPainterObj, this));
			break;
		}

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		revalidate();
		int mx = e.getX();
		int my = e.getY();
		double dx = (mx - prevMouseX) / scale;
		double dy = (my - prevMouseY) / scale;

		if (!draggingPoint.isEmpty()) {
			for (Point p : draggingPoint) {
				if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0) {
					if (Math.abs(e.getX() - pressedLocationX) > Math.abs(e.getY() - pressedLocationY))
						p.setX(p.getX() + dx);
					else
						p.setY(p.getY() + dy);
				} else {
					p.setX(p.getX() + dx);
					p.setY(p.getY() + dy);
				}
			}
		} else if (!draggingPainterObj.isEmpty()) {
			for (PainterObj painterObj : draggingPainterObj) {
				if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0) {
					if (Math.abs(e.getX() - pressedLocationX) > Math.abs(e.getY() - pressedLocationY))
						painterObj.moveX(dx);
					else
						painterObj.moveY(dy);
				} else {
					painterObj.moveX(dx);
					painterObj.moveY(dy);
				}
			}
		} else {
			offsetX += dx * 50;
			offsetY += dy * 50;
		}
		prevMouseX = mx;
		prevMouseY = my;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getX() != pressedLocationX && e.getY() != pressedLocationY)
			note.saveInfo();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
			double rol = e.getWheelRotation();
			double cx = (getWidth() / 2.0 - getOffsetX()) / getScale();
			double cy = (getHeight() / 2.0 - getOffsetY()) / getScale();
			for (PainterObj s : allPainterObj)
				s.changeSize(rol == -1 ? 1.05 : 1 / 1.05, cx, cy);
		}
		note.saveInfo();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void drop(DropTargetDropEvent dtde) {
		try {
			dtde.acceptDrop(DnDConstants.ACTION_COPY);
			Transferable trans = dtde.getTransferable();
			DataFlavor[] flavor = trans.getTransferDataFlavors();
			String path = "";
			for (DataFlavor f : flavor) {
				if (f.isFlavorJavaFileListType()) {
					List<File> files = (List<File>) trans.getTransferData(f);
					for (File file : files) {
						path = file.getAbsolutePath();
						saveLoader.getLoader().loadFile(path);
					}
				}
			}
			dtde.dropComplete(true);
			repaint();// it is for browse mode
			JOptionPane.showMessageDialog(this, "the file " + path + " is opened!", "Open the file Successful",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			dtde.dropComplete(false);
			JOptionPane.showMessageDialog(this, "the file were broken or it have a wrong format", "File Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_C:
			if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)
				if (!draggingPainterObj.isEmpty()) {
					List<PainterObj> copied = new ArrayList<>();
					for (PainterObj painterObj : draggingPainterObj) {
						PainterObj s = painterObj.clone();
						s.moveX(0.25);
						s.moveY(0.25);
						this.addPainterObj(s);
						copied.add(s);
					}
					for (PainterObj p : this.draggingPainterObj) {
						p.setDraggable(false);
					}
					for (PainterObj p : copied) {
						p.setDraggable(true);
					}
					setDraggingPainterObj(copied);
					note.saveInfo();
				}
			break;
		case KeyEvent.VK_DELETE:
			if (!draggingPainterObj.isEmpty() || !draggingPoint.isEmpty()) {
				if (draggingPainterObj.isEmpty()) {
					for (Point p : draggingPoint) {
						p.getPainterObj().removePoint(p);
					}
				} else {
					for (PainterObj s : draggingPainterObj) {
						this.removePainterObj(s);
					}
					this.getDraggingPainterObj().clear();
				}
				this.getDraggingPoint().clear();
			} else if (allPainterObj.size() > 0)
				allPainterObj.removeLast();
			note.saveInfo();
			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_UP:
			if (!this.draggingPainterObj.isEmpty()) {
				for (PainterObj painterObj : draggingPainterObj) {
					painterObj.changeSize(1.05, painterObj.getCertain().getX(), painterObj.getCertain().getY());
				}
			}
			note.saveInfo();
			break;
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_DOWN:
			if (!this.draggingPainterObj.isEmpty()) {
				for (PainterObj painterObj : draggingPainterObj) {
					painterObj.changeSize(1 / 1.05, painterObj.getCertain().getX(), painterObj.getCertain().getY());
				}
			}
			note.saveInfo();
			break;
		case KeyEvent.VK_Z:
			if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)
				note.undo(this);
			break;
		case KeyEvent.VK_Y:
			if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)
				note.redo(this);
			break;
		case KeyEvent.VK_S:
			if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setDialogTitle("choose the folder");
				int result = chooser.showOpenDialog(this);
				if (result == JFileChooser.APPROVE_OPTION) {
					String path = chooser.getSelectedFile().getAbsolutePath();
					if (!path.toLowerCase().endsWith(".vecf"))
						path += ".vecf";
					this.saveLoader.getExporter().ExportFlie(path);
					JOptionPane.showMessageDialog(this, "Save file successfull!", "Save successfull",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
			break;
		case KeyEvent.VK_A:
			if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
				for (PainterObj p : this.allPainterObj) {
					this.addDraggingPainterObj(p);
					p.setDraggable(true);
				}
			}
			break;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
	}
}