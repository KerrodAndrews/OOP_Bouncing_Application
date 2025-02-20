/*
 *  ============================================================================================
 *  A3.java : Extends JFrame and contains a panel where shapes move around on the screen.
 *  YOUR UPI: kand684
 *  ============================================================================================
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.ArrayList;
import javax.swing.tree.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.ListDataListener;
import java.lang.reflect.Field;


public class A3  extends JFrame {
	private AnimationViewer bouncingPanel;  // panel for bouncing area
	JButton addNodeButton, removeNodeButton;
	JComboBox<ShapeType> shapesComboBox;
	JComboBox<PathType> pathComboBox;
	JTree tree;

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new A3();
			}
		});
	}
	public A3() {
		super("Bouncing Application");
		JPanel mainPanel = setUpMainPanel();
		add(mainPanel, BorderLayout.CENTER);
		add(setUpToolsPanel(), BorderLayout.NORTH);
		addComponentListener(
			new ComponentAdapter() { // resize the frame and reset all margins for all shapes
				public void componentResized(ComponentEvent componentEvent) {
					bouncingPanel.resetMarginSize();
			}
		});
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}
	public JPanel setUpMainPanel() {
		JPanel mainPanel = new JPanel();
		bouncingPanel = new AnimationViewer();
		bouncingPanel.setPreferredSize(new Dimension(Shape.DEFAULT_PANEL_WIDTH, Shape.DEFAULT_PANEL_HEIGHT));
		JPanel modelPanel = setUpModelPanel();
		modelPanel.setPreferredSize(new Dimension(Shape.DEFAULT_PANEL_WIDTH, Shape.DEFAULT_PANEL_HEIGHT));
		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, modelPanel, bouncingPanel);
		mainSplitPane.setResizeWeight(0.5);
		mainSplitPane.setOneTouchExpandable(true);
		mainSplitPane.setContinuousLayout(true);
		mainPanel.add(mainSplitPane);
		return mainPanel;
	}
	public JPanel setUpModelPanel() {
		JPanel treePanel = new JPanel(new BorderLayout());
		treePanel.setPreferredSize(new Dimension(Shape.DEFAULT_PANEL_WIDTH, Shape.DEFAULT_PANEL_HEIGHT/2));
		tree = new JTree(bouncingPanel);
		//tree = new JTree(); //replace this line
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		JScrollPane treeScrollpane = new JScrollPane(tree);
		JPanel treeButtonsPanel = new JPanel();
		addNodeButton = new JButton("Add Node");
		addNodeButton.addActionListener( new AddListener());
		removeNodeButton = new JButton("Remove Node");
		removeNodeButton.addActionListener( new RemoveListener());
		treeButtonsPanel.add(addNodeButton);
		treeButtonsPanel.add(removeNodeButton);
		treePanel.add(treeButtonsPanel,BorderLayout.NORTH);
		treePanel.add(treeScrollpane,BorderLayout.CENTER);
		return treePanel;
	}
	class AddListener implements ActionListener {
		public void actionPerformed( ActionEvent e) {
			Object node = tree.getLastSelectedPathComponent();
			if (node != null){
				if (node instanceof NestedShape){
					bouncingPanel.addShapeNode((NestedShape) node);

				}
				else{
					JOptionPane.showMessageDialog(null, "ERROR: Must select a NestedShape node.");
					//System.out.println("ERROR: Must select a NestedShape node.");
				}
			} else{
				JOptionPane.showMessageDialog(null, "ERROR: No node selected.");
				//System.out.println("ERROR: No node selected.");
			}
		}
	}
	class RemoveListener implements ActionListener {
		public void actionPerformed( ActionEvent e) {
			Object node = tree.getLastSelectedPathComponent();
			if (node != null){
			    try{
				if (node != tree.getRootPane()){
					bouncingPanel.removeNodeFromParent((Shape) node);
				}
				else{
					//System.out.println("ERROR: Must not remove the root.");
					JOptionPane.showMessageDialog(null, "ERROR: Must not remove the root."); 
				}
			    } catch(NullPointerException g){
			        //System.out.println("ERROR: Must not remove the root.");
					JOptionPane.showMessageDialog(null, "ERROR: Must not remove the root.");
			    }
			}
			else{
				//System.out.println("ERROR: No node selected.");
				JOptionPane.showMessageDialog(null, "ERROR: No node selected.");
			}

		}
	}
	public JPanel setUpToolsPanel() {
		shapesComboBox = new JComboBox<ShapeType>(new DefaultComboBoxModel<ShapeType>(ShapeType.values()));
		shapesComboBox.addActionListener( new ShapeActionListener()) ;
		pathComboBox = new JComboBox<PathType>(new DefaultComboBoxModel<PathType>(PathType.values()));
		pathComboBox.addActionListener( new PathActionListener());
		JPanel toolsPanel = new JPanel();
		toolsPanel.setLayout(new BoxLayout(toolsPanel, BoxLayout.X_AXIS));
		toolsPanel.add(new JLabel(" Shape: ", JLabel.RIGHT));
		toolsPanel.add(shapesComboBox);
		toolsPanel.add(new JLabel(" Path: ", JLabel.RIGHT));
		toolsPanel.add(pathComboBox);
		return toolsPanel;
	}
	class ShapeActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			bouncingPanel.setCurrentShapeType((ShapeType)shapesComboBox.getSelectedItem());
		}
	}
	class PathActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			bouncingPanel.setCurrentPathType((PathType)pathComboBox.getSelectedItem());
		}
	}
}

abstract class Shape {
    public static final PathType DEFAULT_PATHTYPE = PathType.BOUNCING;
    public static final ShapeType DEFAULT_SHAPETYPE = ShapeType.RECTANGLE;
    public static final int DEFAULT_X = 0, DEFAULT_Y = 0, DEFAULT_WIDTH=200, DEFAULT_HEIGHT=100, DEFAULT_PANEL_WIDTH=600, DEFAULT_PANEL_HEIGHT=400;
    public static final Color DEFAULT_COLOR=Color.orange, DEFAULT_BORDER_COLOR=Color.black;
    public int x, y, width=DEFAULT_WIDTH, height=DEFAULT_HEIGHT, panelWidth=DEFAULT_PANEL_WIDTH, panelHeight=DEFAULT_PANEL_HEIGHT; // the bouncing area
    protected Color color = DEFAULT_COLOR, borderColor =DEFAULT_BORDER_COLOR ;
    protected MovingPath path = new BouncingPath(1, 2);
	public static final String DEFAULT_LABEL = "0";
    private static int numberOfShapes = 0;
    protected String label = DEFAULT_LABEL; 
    public Shape(int x, int y, int w, int h, int panelWidth, int panelHeight, Color fillColor, Color borderColor, PathType pathType) {
		this.x = x;
        this.y = y;
        label = "" + numberOfShapes++;
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        width = w;
        height = h;
        color = fillColor;
        this.borderColor = borderColor;
		if (pathType == PathType.BOUNCING)
			path = new BouncingPath(1, 2);
		else
			path = new DownRightPath(5, 5);
    }
	public String getLabel() { return this.label; }
	public void setLabel(String label) { this.label = label; }
	public void drawString(Graphics g) {
		g.setColor(Color.black);
		g.drawString("" + label, x, y + 10);
	}
    public String toString() {
		return String.format("%s,%s,%dx%d,%s,%dx%d", this.getClass().getName(),path.getClass().getSimpleName(), width, height, label,panelWidth, panelHeight);
	}
    public void move() {
        path.move();
    }
    public abstract void draw(Graphics g);
    public int getX() { return this.x; }
	public void setX(int x) { this.x = x; }
    public int getY() { return this.y;}
	public void setY(int y) { this.y = y; }
	public int getWidth() { return width; }
	public void setWidth(int w) { if (w < DEFAULT_PANEL_WIDTH && w > 0) width = w; }
	public int getHeight() {return height; }
	public void setHeight(int h) { if (h < DEFAULT_PANEL_HEIGHT && h > 0) height = h; }
	public Color getColor() { return color; }
    public void setColor(Color fillColor) { color = fillColor; }
	public Color getBorderColor() { return borderColor; }
    public void setBorderColor(Color borderColor) { this.borderColor = borderColor; }
    public void resetPanelSize(int w, int h) {
		panelWidth = w;
		panelHeight = h;
	}
    /* Inner class ===================================================================== Inner class
     *    MovingPath : The superclass of all paths. It is an inner class.
     *    A path can change the current position of the shape.
     *    =============================================================================== */
    abstract class MovingPath {
        protected int deltaX, deltaY; // moving distance
        public MovingPath(int dx, int dy) { deltaX=dx; deltaY=dy; }
        public MovingPath() { }
        public abstract void move();
        public String toString() { return getClass().getSimpleName(); };
    }
    class BouncingPath extends MovingPath {
        public BouncingPath(int dx, int dy) {
            super(dx, dy);
         }
        public void move() {
             x = x + deltaX;
             y = y + deltaY;
             if ((x < 0) && (deltaX < 0)) {
                 deltaX = -deltaX;
                 x = 0;
             }
             else if ((x + width > panelWidth) && (deltaX > 0)) {
                 deltaX = -deltaX;
                 x = panelWidth - width;
             }
             if ((y< 0) && (deltaY < 0)) {
                 deltaY = -deltaY;
                 y = 0;
             }
             else if((y + height > panelHeight) && (deltaY > 0)) {
                 deltaY = -deltaY;
                 y = panelHeight - height;
             }
        }
    }
    class DownRightPath extends MovingPath {
		public static final int INTERVAL = 5;
		private int count =0;
		public DownRightPath(int dx, int dy) {
			super(dx, dy);
		}
		public void move() {
			if (count > INTERVAL) {
				x += deltaX;
				if ((x + width > panelWidth) && (deltaX > 0))
					x = 0;
				count = 0;
			} else {
				y += deltaY;
				if ((y + height > panelHeight) && (deltaY > 0))
					y = 0;
				count += 1;
			}
		}
	}

    public Shape[] getPathToRoot(Shape aShape, int depth) {
        Shape[] returnShapes;
        if (aShape == null) {
          if(depth == 0) return null;
          else returnShapes = new Shape[depth];
        }
        else {
          depth++;
          returnShapes = getPathToRoot(aShape.getParent(), depth);
          returnShapes[returnShapes.length - depth] = aShape;
        }
        return returnShapes;
      }
        
        protected NestedShape parent;
        
        public NestedShape getParent(){
          return parent;
            
        }
        
        public void setParent(NestedShape s){
            this.parent = s;
        }
        
        public Shape[] getPath(){
            return getPathToRoot(this, 0);
        }
} 

class NestedShape extends RectangleShape{

    private ArrayList<Shape> innerShapes = new ArrayList<>();

    public Shape createInnerShape(PathType pt, ShapeType st){
        int innerWidth = width/4;
        int innerHeight = height/4;
        
        Shape innerShape;
        if (st == ShapeType.SQUARE) {
            int squareWidth =  Math.min(innerWidth, innerHeight);
            innerShape = new SquareShape(0, 0, squareWidth, width, height, color, borderColor, pt);
        }

        else if (st == ShapeType.RECTANGLE){
            innerShape = new RectangleShape(0, 0, innerWidth, innerHeight, width, height, color, borderColor, pt);
        }

        else{ // if (st == ShapeType.NESTED){
            innerShape = new NestedShape(0, 0, innerWidth, innerHeight, width, height, color, borderColor, pt);
        }

        innerShape.setParent(this);
        innerShapes.add(innerShape);
        return innerShape;
    }

    public NestedShape(int x, int y, int innerWidth, int innerHeight, int panelWidth, int panelHeight, Color fillColor, Color borderColor, PathType pathType){
        super(x ,y ,innerWidth, innerHeight ,panelWidth ,panelHeight, fillColor, borderColor, pathType);
        createInnerShape(PathType.BOUNCING, ShapeType.RECTANGLE);
    }

    public NestedShape(int w, int h){
        super(0, 0, w, h, Shape.DEFAULT_PANEL_WIDTH, Shape.DEFAULT_PANEL_HEIGHT, Shape.DEFAULT_COLOR, Shape.DEFAULT_BORDER_COLOR, PathType.BOUNCING);
    }

    public Shape getInnerShapeAt(int index){
        return innerShapes.get(index);
    }

    public int getSize(){
        return innerShapes.size();
    }
    
    public void draw(Graphics g){
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
        g.translate(x, y);
        for (Shape shapes : innerShapes){
            shapes.draw(g);
            shapes.drawString(g);
        }

        g.translate(-x, -y);

    }

    public void move(){
        super.move();
        for (Shape shapes : innerShapes){
            shapes.move();
        }
    }

    public int indexOf(Shape s){
        return innerShapes.indexOf(s);
    }

    public void addInnerShape(Shape s){
        innerShapes.add(s);
        s.setParent(this);
    }

    public void removeInnerShape(Shape s){
        innerShapes.remove(s);
        s.setParent(null);
    }

    public void removeInnerShapeAt(int index){
        innerShapes.remove(index);
        this.setParent(null);
    }

    public ArrayList<Shape> getAllInnerShapes(){
        return innerShapes;
    }
    
}

class AnimationViewer extends JComponent implements Runnable, TreeModel {
	private Thread animationThread = null; // the thread for animation
	private static int DELAY = 120; // the current animation speed
	private ShapeType currentShapeType = Shape.DEFAULT_SHAPETYPE; // the current shape type,
	private PathType currentPathType = Shape.DEFAULT_PATHTYPE; // the current path type
	private Color currentColor = Shape.DEFAULT_COLOR; // the current fill colour of a shape
	private Color currentBorderColor = Shape.DEFAULT_BORDER_COLOR;
	private int currentPanelWidth = Shape.DEFAULT_PANEL_WIDTH, currentPanelHeight = Shape.DEFAULT_PANEL_HEIGHT,currentWidth = Shape.DEFAULT_WIDTH, currentHeight = Shape.DEFAULT_HEIGHT;
	private String currentLabel = Shape.DEFAULT_LABEL;
	//ArrayList<Shape> shapes = new ArrayList<Shape>(); //create the ArrayList to store shapes
	protected NestedShape root;


	public AnimationViewer() {
		start();
		root = new NestedShape(Shape.DEFAULT_PANEL_WIDTH, Shape.DEFAULT_PANEL_HEIGHT);
	}

	//protected void createNewShape(int x, int y) {
		//int min_size = Math.min(currentWidth, currentHeight);
		//switch (currentShapeType) {
			//case RECTANGLE: {
			//shapes.add( new RectangleShape(x, y,currentWidth,currentHeight,currentPanelWidth,currentPanelHeight,currentColor,currentBorderColor,currentPathType));
			//break;
			//}  case SQUARE: {
			//shapes.add( new SquareShape(x, y,min_size,currentPanelWidth,currentPanelHeight,currentColor,currentBorderColor,currentPathType));
			//break;
			//}
		//}
	//}
	public final void paintComponent(Graphics g) {
		super.paintComponent(g);
		for (Shape currentShape : root.getAllInnerShapes()) {
		   currentShape.move();
		   currentShape.draw(g);
		   currentShape.drawString(g);
		
		}
	}
	public void resetMarginSize() {
		currentPanelWidth = getWidth();
		currentPanelHeight = getHeight();
		for (Shape currentShape : root.getAllInnerShapes()){
	    	currentShape.resetPanelSize(currentPanelWidth, currentPanelHeight);
		}
}


	// you don't need to make any changes after this line ______________
	public String getCurrentLabel() {return currentLabel;}
	public int getCurrentHeight() { return currentHeight; }
	public int getCurrentWidth() { return currentWidth; }
	public Color getCurrentColor() { return currentColor; }
	public Color getCurrentBorderColor() { return currentBorderColor; }
	public void setCurrentShapeType(ShapeType value) {currentShapeType = value;}
	public void setCurrentPathType(PathType value) {currentPathType = value;}
	public ShapeType getCurrentShapeType() {return currentShapeType;}
	public PathType getCurrentPathType() {return currentPathType;}
	public void update(Graphics g) {
		paint(g);
	}
	public void start() {
		animationThread = new Thread(this);
		animationThread.start();
	}
	public void stop() {
		if (animationThread != null) {
			animationThread = null;
		}
	}
	public void run() {
		Thread myThread = Thread.currentThread();
		while (animationThread == myThread) {
			repaint();
			pause(DELAY);
		}
	}
	private void pause(int milliseconds) {
		try {
			Thread.sleep((long) milliseconds);
		} catch (InterruptedException ie) {}
	}

	private ArrayList<TreeModelListener> treeModelListeners = new ArrayList<>();

	public NestedShape getRoot(){
		return root;
	}

	public boolean isLeaf(Object node){
		if (node instanceof NestedShape){
			return false;
		}
		else{
			return true;
		}
	}

	public boolean isRoot(Shape selectedNode){
		return selectedNode == root;
	}

	public Object getChild(Object parent, int index){
		if (parent instanceof NestedShape){
			NestedShape nested = (NestedShape) parent;
            if (index >= 0 && index < nested.getSize()) {
                return nested.getInnerShapeAt(index);
            }
            else{
                return null;
            }
		}
		else{
			return null;
		}
	}

	public int getChildCount(Object parent){
		if (parent instanceof NestedShape){
			return ((NestedShape)parent).getSize();
		}
		else{
			return 0;
		}
	}

	public int getIndexOfChild(Object parent, Object child){
		if (parent instanceof NestedShape){
			return ((NestedShape)parent).indexOf((Shape) child);
		}
		else{
			return -1;
		}
	}


	public void addTreeModelListener(final TreeModelListener tml){
		treeModelListeners.add(tml);
	}

	public void removeTreeModelListener(final TreeModelListener tml){
		treeModelListeners.remove(tml);
	}

	public void valueForPathChanged(TreePath path, Object newValue){}

	public void fireTreeNodesInserted(Object source, Object[] path,int[] childIndices,Object[] children){

		TreeModelEvent tree = new TreeModelEvent(source, path, childIndices, children);
		for (TreeModelListener listener : treeModelListeners){
			listener.treeNodesInserted(tree);
		}
		System.out.printf("Called fireTreeNodesInserted: path=%s, childIndices=%s, children=%s\n", Arrays.toString(path), Arrays.toString(childIndices), Arrays.toString(children));

	}

	public void addShapeNode(NestedShape selectedNode){
        
        Shape innerShape;
        if (currentShapeType == ShapeType.SQUARE) {
            int squareWidth =  Math.min(selectedNode.getWidth() / 4, selectedNode.getHeight() / 4);
            innerShape = new SquareShape(0, 0, squareWidth, selectedNode.getWidth(), selectedNode.getHeight(), selectedNode.getColor(), selectedNode.getBorderColor(), currentPathType);
        }
        else if (currentShapeType == ShapeType.RECTANGLE){
            innerShape = new RectangleShape(0, 0, selectedNode.getWidth() / 4, selectedNode.getHeight() / 4, selectedNode.getWidth(), selectedNode.getHeight(), selectedNode.getColor(), selectedNode.getBorderColor(), currentPathType);
        }

        else{ 
            innerShape = new NestedShape(0, 0, selectedNode.getWidth() / 4, selectedNode.getHeight() / 4, selectedNode.getWidth(), selectedNode.getHeight(), selectedNode.getColor(), selectedNode.getBorderColor(), currentPathType);
        }

        selectedNode.addInnerShape(innerShape);
        
		
        Object[] path = selectedNode.getPath();
        int[] childIndices = {selectedNode.indexOf(innerShape)};
        Object[] children = {innerShape};

        fireTreeNodesInserted(this, path, childIndices, children);
	}


	public void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices,Object[] children){
		TreeModelEvent tree = new TreeModelEvent(source, path, childIndices, children);
		for (TreeModelListener listener : treeModelListeners){
			listener.treeNodesRemoved(tree);
		}
		System.out.printf("Called fireTreeNodesRemoved: path=%s, childIndices=%s, children=%s\n", Arrays.toString(path), Arrays.toString(childIndices), Arrays.toString(children));
	}

	public void removeNodeFromParent(Shape selectedNode){
		NestedShape parent = selectedNode.getParent();
		int index = parent.indexOf(selectedNode);
		parent.removeInnerShape(selectedNode);

		Object[] path = parent.getPath();
        int[] childIndices = {index};
        Object[] children = {selectedNode};

		fireTreeNodesRemoved(this, path, childIndices, children);

	}

	
}


class RectangleShape extends Shape {
	public RectangleShape(int x, int y, int w, int h, int panelWidth, int panelHeight, Color fillColor, Color borderColor, PathType pathType) {
		super(x ,y ,w, h ,panelWidth ,panelHeight, fillColor, borderColor, pathType);
	}
	public void draw(Graphics g) {
		g.setColor(color);
		g.fillRect(x, y, width, height);
		g.setColor(borderColor);
		g.drawRect(x, y, width, height);
	}
} 

enum PathType { BOUNCING, DOWN_RIGHT;}

enum ShapeType { RECTANGLE(4), SQUARE(4), NESTED(4);
    private int numberOfSides;
    private ShapeType(int numberOfSides) { this.numberOfSides = numberOfSides; }
    public int getNumberOfSides() { return numberOfSides; }
  }

  class SquareShape extends RectangleShape {
	public SquareShape(int x, int y, int sideLength, int panelWidth, int panelHeight, Color fillColor, Color borderColor, PathType pathType) {
		super(x ,y ,sideLength, sideLength ,panelWidth ,panelHeight, fillColor, borderColor, pathType);
	}
}
