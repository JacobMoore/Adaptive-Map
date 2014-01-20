package view;

import java.applet.AppletContext;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
//import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import model.Link;
import model.Link.LinkProperties;
import model.Node;
import model.Node.ChapterProperties;
import model.Node.ViewType;
import model.NodeMap;
import controller.Configuration;
import controller.xml.XmlParser;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.widgets.TranslucentButton;
import fr.inria.zvtm.widgets.TranslucentRadioButton;
import fr.inria.zvtm.widgets.TranslucentTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Graphics;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author John Nein, Michel Pascale, Lauren Gibboney
 * @version Apr 03, 2011
 */
public class AppCanvas extends JPanel {

	private static final long serialVersionUID = 7885546829753042319L;

	/**
	 * The applet's context.
	 */
	private JFrame appFrame;

	// ZVTM objects
	private final VirtualSpaceManager vSpaceManager;
	private VirtualSpace detailedSpace;
	private View activeView;

	// Data objects to handle nodes and chapters
	private List<Node> nodeList;
	private Map<String, String> defaultNodes;
	private ArrayList<Node> chapterList;
	private NodeMap nodeMap;
	private Map<String, Point> chapterMap;

	// List of all chapter names
	private ArrayList<String> chapters;
	// Numbers for gradient fill for chapters
	private ArrayList<Integer> numPerChapter;
	// Number of total nodes, used for coloring chapter nodes.
	private float numberOfNodesInLargestChapter;

	// Currently selected node.
	private Node selectedNode;
	// Last selected non-chapter node.
	private Node prevSelectedNode;
	// Default chapter node
	private Node defaultChapter;
	private CameraMovementListener cameraListener;

	/**
	 * Max height in the chapter view.
	 */
	public static final int ZOOM_CHAPTER_HEIGHT = 100;
	/**
	 * Min height in the overview view.
	 */
	public static final int ZOOM_OVERVIEW_MIN = 300;
	/**
	 * Max height in the overview view.
	 */
	public static final int ZOOM_OVERVIEW_MAX = 2000;
	// Current height in the overview.
	private int zoomOverviewHeight;

	// Font size variables.
	private int nodeFontSize;
	private int chapterTitleFontSize;
	private int chapterDescriptionFontSize;

	// Tools panel variables.
	private JRadioButton medViewRadioButton, highViewRadioButton;
	private TranslucentButton pageButton, zoomInButton, zoomOutButton,
			startButton;
        private JPanel toolsPane;
	private JButton optionsButton, backButton;
	private LinkedList<History> backButtonList;
	private JTextField searchBar;
	private JDialog optionsDialog;
	private boolean canDrag;

	// Search Variables
	private boolean initial;

	/**
	 * Default constructor.
	 * 
	 * @param vSpaceManager
	 *            The virtual space to use.
	 * @param appFrame
	 *            The frame to use.
	 * @param context
	 *            The applet's context.
	 */
	public AppCanvas(VirtualSpaceManager vSpaceManager, JFrame appFrame,
			AppletContext context) {
		this.appFrame = appFrame;
		this.vSpaceManager = vSpaceManager;
		nodeList = new ArrayList<Node>();
		nodeMap = new NodeMap();
		chapterList = new ArrayList<Node>();
		chapters = new ArrayList<String>();
		numPerChapter = new ArrayList<Integer>();

		zoomOverviewHeight = 400;
		nodeFontSize = 18;
		chapterTitleFontSize = 100;
		chapterDescriptionFontSize = 80;
		canDrag = false;
		initial = true;

		VText.setMainFont(VText.getMainFont().deriveFont(nodeFontSize));
		addTools();
		initializeOptionsDialog();
		createStartScreen();
	}

	/**
	 * Returns the list of nodes.
	 * 
	 * @return nodeList
	 */
	public List<Node> getNodeList() {
		return nodeList;
	}

	/**
	 * Returns the list of overview nodes.
	 * 
	 * @return chapterList
	 */
	public List<Node> getChapterList() {
		return chapterList;
	}

	/**
	 * Gets the number of nodes in a chapter; used to set the chapter nodes'
	 * color fill.
	 * 
	 * @param chapterTitle
	 *            The title of the target chapter.
	 * @return Float value for node color fill.
	 */
	public float getNumNodesPerChapter(String chapterTitle) {
		int chapter = chapters.indexOf(chapterTitle);
		if (chapter == -1)
			return -1;
		return numPerChapter.get(chapter) / numberOfNodesInLargestChapter;
	}

	/**
	 * Creates the start button.
	 */
	public void createStartScreen() {
            //JPanel toolsPane = new JPanel();
		startButton = new TranslucentButton("START");
                startButton.setPreferredSize(new Dimension(300,150));
		startButton.setForeground(Color.WHITE);
		startButton.setBackground(Color.DARK_GRAY);
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
                                appFrame.getLayeredPane().remove(startButton);
				showToolButtons();
				createView();
                                appFrame.getLayeredPane().add(toolsPane, JLayeredPane.PALETTE_LAYER);
                                //appFrame.getLayeredPane().add(activeView.getPanel(), JLayeredPane.FRAME_CONTENT_LAYER);
                                appFrame.getLayeredPane().setLayout(new BorderLayout());
                                appFrame.getLayeredPane().add(activeView.getPanel(), BorderLayout.CENTER, JLayeredPane.FRAME_CONTENT_LAYER);
                                toolsPane.setOpaque(false);
                                appFrame.revalidate();
			}
		});
                //appFrame.getLayeredPane().setLayout(new BorderLayout());
                appFrame.getLayeredPane().add(startButton, JLayeredPane.PALETTE_LAYER);
                
                //toolsPane.add(startButton);
		startButton.setBounds(appFrame.getWidth() / 2 - 100, appFrame.getHeight() / 2, 200, 50);
                //appFrame.add(toolsPane, BorderLayout.CENTER);
	}

	/**
	 * Creates the ZVTM virtual space and active view, and sets the camera's
	 * position.
	 * 
	 * @param appFrame
	 *            the frame that this instance of AppCanvas will be added to
	 */
	private void createView() {
		// appFrame.add(AppCanvas.this);
		detailedSpace = vSpaceManager
				.addVirtualSpace(Configuration.APPLICATION_TITLE);

		Camera detailedCamera = detailedSpace.addCamera();
		Vector<Camera> cameras = new Vector<Camera>();
		cameras.add(detailedCamera);

		activeView = vSpaceManager.addFrameView(cameras,
				Configuration.APPLICATION_TITLE, View.STD_VIEW, 1200, 1200,
				false, false);
		cameraListener = new CameraMovementListener(this);
		activeView.setEventHandler(cameraListener);
		activeView.setBackgroundColor(Configuration.APPLICATION_BG_COLOR);
		//activeView.getPanel().setSize(new Dimension(1200, 1200));
		// Set the camera location and altitude
		activeView.getActiveCamera().setLocation(new Location(0, 0, 400));
		activeView.setCursorIcon(Cursor.DEFAULT_CURSOR);

                //appFrame.add(activeView.getPanel(), BorderLayout.CENTER);
		populateCanvas();
	}

	/**
	 * Populates the canvas by calling parsing functions in the XML parser. The
	 * parser parses all of the nodes in the xml file, and adds each one to the
	 * virtual space; the parsing is then continued to parse the properties for
	 * each link type, then parses the actual links between the nodes and adds
	 * them to the virtual space.
	 * 
	 * @precondition populateNodeMap has been called.
	 */
	private void populateCanvas() {
		// IMPORTANT: parse chapters before nodes
		for (Entry<String, ChapterProperties> chapterProperty : XmlParser
				.parseChapterProperties().entrySet()) {
			nodeMap.addChapterType(chapterProperty.getKey(),
					chapterProperty.getValue());
			chapters.add(chapterProperty.getKey());
		}

		XmlParser.parseNodeInformation(nodeMap, nodeFontSize);
		nodeList.addAll(nodeMap.getNodes());
		for (Node nodeToAdd : nodeList) {
			nodeToAdd.addToVirtualSpace(detailedSpace);
		}
		// IMPORTANT: parse node properties before linking the nodes
		for (Entry<String, LinkProperties> linkProperty : XmlParser
				.parseLinkProperties().entrySet()) {
			Link.addLinkType(linkProperty.getKey(), linkProperty.getValue());
		}
		XmlParser.parseNodeLinks(nodeList);

		// Hide all nodes, must be done after links are added to prevent ghost
		// links.
		for (Node node : nodeList) {
			node.showView(ViewType.HIDDEN, node, false);
			node.initializeMultiNodeList();
		}

		// Initialize numPerChapter.
		for (int j = 0; j < chapters.size(); j++)
			numPerChapter.add(0);

		// Determine the numPerChapter for each chapter.
		for (Node n : nodeList) {
			for (int i = 0; i < chapters.size(); i++) {
				if (chapters.get(i).equals(n.getNodeChapter())) {
					numPerChapter.set(i, numPerChapter.get(i) + 1);
					break;
				}
			}
		}

		// Determine the total number of nodes
		numberOfNodesInLargestChapter = 0;
		for (Integer i : numPerChapter) {
			if (i > numberOfNodesInLargestChapter)
				numberOfNodesInLargestChapter = i;
		}

		defaultNodes = new HashMap<String, String>();
		for (Entry<String, ChapterProperties> chapterProperty : XmlParser
				.parseChapterProperties().entrySet()) {
			// Add the chapter to the chapter list, and the vs.
			// Titles and chapters of chapter nodes are the same so that
			// chapter nodes are easily identified.
			Node newChapter = new Node(chapterProperty.getKey(),
					chapterProperty.getValue().getDescription(),
					chapterProperty.getKey(), chapterProperty.getValue()
							.getKeywords(), nodeMap.getChapterType(
							chapterProperty.getKey()).getChapterColor(),
					chapterTitleFontSize, chapterDescriptionFontSize,
					getNumNodesPerChapter(chapterProperty.getKey()));
			if (Configuration.USE_FIXED_NODE_POSITIONS) {
				newChapter.setFixedNodePosition(chapterProperty.getValue()
						.getChapterXPos(), chapterProperty.getValue()
						.getChapterYPos());
			}
			newChapter.addToVirtualSpace(detailedSpace);
			chapterList.add(newChapter);
			if (chapterProperty.getValue().isDefaultChapter())
				defaultChapter = newChapter;
			defaultNodes.put(chapterProperty.getKey(), chapterProperty
					.getValue().getDefaultNode());
		}
		if (defaultChapter == null)
			defaultChapter = chapterList.get(0);

		// Set the chapter node positions.
		setChapterCoords();
		parseChapterLinks();

		// Set the default selected node
		selectedNode = defaultChapter;
		// selectedNode.highlightLinks(false);
		selectedNode.highlight(Color.yellow, 3);

		activeView.getActiveCamera().setLocation(
				new Location(selectedNode.getGlyph().getLocation().x,
						selectedNode.getGlyph().getLocation().y,
						zoomOverviewHeight));
	}

	/**
	 * Goes through all nodes and sets up the links between chapters.
	 */
	private void parseChapterLinks() {
		for (String chapter : nodeMap.getChapters()) {
			Node currentChapter = getChapterNode(chapter);
			if (currentChapter == null)
				continue;
			List<Link> links = new ArrayList<Link>();
			links.addAll(Node.getAllLinks());
			for (final Link l : links) {
				if (getChapterNode(l.getFromNode().getNodeChapter()).equals(
						currentChapter)
						&& !l.getFromNode().equals(currentChapter)
						&& !getChapterNode(l.getToNode().getNodeChapter())
								.equals(currentChapter)) {
					Node targetChapter = getChapterNode(l.getToNode()
							.getNodeChapter());
					Node.link(currentChapter, targetChapter,
							Link.LinkLineType.STANDARD.name(), 100, true);
				}
			}
		}
	}

	/**
	 * Moves chapter nodes to their original positions.
	 */
	private void setChapterCoords() {
		if (Configuration.USE_FIXED_NODE_POSITIONS) {
			for (Node n : chapterList)
				n.moveToFixedPos();
		} else {
			chapterMap = nodeMap.setChapterCoordsFromFile(
					new ArrayList<String>(nodeMap.getChapters()),
					defaultChapter);
			if (chapterMap == null) {
				System.out.println("GraphViz failed to load chapters!");
				chapterMap = NodeMap.getChapterCoords(nodeMap,
						chapterTitleFontSize);
			}

			for (Node n : chapterList) {
				Point coord = chapterMap.get(n.getNodeTitle());
				n.moveAbsolute(coord.x, coord.y);
			}
		}
	}

	/**
	 * Returns the node of the specified chapter in the chapterList, or null if
	 * it was not found.
	 * 
	 * @param chapterName
	 *            The name of the chapter
	 * @return The node representing the given chapter
	 */
	private Node getChapterNode(String chapterName) {
		for (Node node : chapterList) {
			if (node.getNodeTitle().equals(chapterName))
				return node;
		}
		System.err.println("Chapter not found in chapterList.");
		return null;
	}

	/**
	 * Navigates a browser window to the given url.
	 * 
	 * @param url
	 *            the url to navigate to
	 */
        /*
	public void navigateTo(String url) {
		try {
			// Absolute URL
			if (url.startsWith("http")) {
				URL absoluteURL = new URL(url);
				appletContext.showDocument(absoluteURL, "_blank");
			}
			// Relative URL
			else if (appletContext != null) {
				// Application started in an applet
				URL relativeURL = null;
				try {
					relativeURL = new URL(appletContext.getApplet(
							"Adaptive Map").getCodeBase(), url);
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
				appletContext.showDocument(relativeURL, "_blank");
			} else {
				// Stand-alone application
				java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
			}
		} catch (Exception e) {
			System.out.println("Error navigating to url " + url + ".");
			e.printStackTrace();
		}
	}*/
        
        public void navigateTo(String urlPath) {
            try {
                if(!urlPath.startsWith("http://"))
                    urlPath = Configuration.getServerFolder() + urlPath;
                Desktop.getDesktop().browse(new URL(urlPath).toURI());
            } catch (IOException ex) {
                Logger.getLogger(AppCanvas.class.getName()).log(Level.SEVERE, null, ex);
            } catch (URISyntaxException ex) {
                Logger.getLogger(AppCanvas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

	/**
	 * Adds search, view-control, and settings tools to the view.
	 * 
	 * @param appFrame
	 *            the frame that this instance of AppCanvas will be added to
	 */
	private void addTools() {
                toolsPane = new JPanel();
                toolsPane.setLayout(null);
            
		searchBar = new TranslucentTextField("Search...");
		searchBar.setForeground(Color.WHITE);
		searchBar.setBackground(Color.DARK_GRAY);
		searchBar.setToolTipText("Enter your search terms, and press ENTER to search.");
		searchBar.addKeyListener(new SearchBarListener());
		searchBar.setVisible(false);
		toolsPane.add(searchBar);

		pageButton = new TranslucentButton("Topic Page");
		pageButton.setEnabled(false);
		pageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				navigateTo(selectedNode.getNodeContentUrl());
			}
		});
		pageButton.setVisible(false);
		toolsPane.add(pageButton);

		medViewRadioButton = new TranslucentRadioButton("Cluster View", false);
		medViewRadioButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				viewRadioItemStateChanged(e, 1);
			}
		});
		medViewRadioButton.setVisible(false);
		toolsPane.add(medViewRadioButton);

		highViewRadioButton = new TranslucentRadioButton("Overview", true);
		highViewRadioButton.setEnabled(false);
		highViewRadioButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				viewRadioItemStateChanged(e, 2);
			}
		});
		highViewRadioButton.setVisible(false);
		toolsPane.add(highViewRadioButton);

		zoomInButton = new TranslucentButton("+");
		zoomInButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				activeView.getActiveCamera().altitudeOffset(-25, true);
				updateZoomLevel(activeView.getActiveCamera().altitude);
			}
		});
		zoomInButton.setVisible(false);
		toolsPane.add(zoomInButton);

		zoomOutButton = new TranslucentButton("-");
		zoomOutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				activeView.getActiveCamera().altitudeOffset(25, true);
				if (activeView.getActiveCamera().altitude > ZOOM_OVERVIEW_MAX)
					activeView.getActiveCamera().altitude = ZOOM_OVERVIEW_MAX;
				updateZoomLevel(activeView.getActiveCamera().altitude);
			}
		});
		zoomOutButton.setVisible(false);
		toolsPane.add(zoomOutButton);

		optionsButton = new TranslucentButton("Options");
		optionsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				optionsDialog.setVisible(true);
			}
		});
		optionsButton.setVisible(false);
		toolsPane.add(optionsButton);

		backButtonList = new LinkedList<History>();
		backButton = new TranslucentButton("Back");
		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (backButtonList.isEmpty())
					return;

				if (selectedNode.hasMultiNodes()) {
					for (Node n : selectedNode.getMultiNodeList()) {
						n.unhighlight();
						n.showView(ViewType.HIDDEN, selectedNode, true);
					}
				}

				History undo = backButtonList.removeLast();
				boolean inOverview = isSelectedAChapterNode();
				boolean toOverview = chapterList.contains(undo.node);
				selectedNode = undo.node;
				if (!inOverview && !toOverview)
					showSelectedChapter();
				else if (inOverview && !toOverview) {
					medViewRadioButton.setSelected(true);
					return;
				} else if (!inOverview && toOverview) {
					highViewRadioButton.setSelected(true);
					return;
				}

				Camera activeCamera = vSpaceManager.getActiveCamera();
				vSpaceManager.getActiveCamera().setAltitude(undo.cameraHeight,
						false);
				activeCamera.moveTo(undo.node.getGlyph().vx,
						undo.node.getGlyph().vy);
			}
		});
		backButton.setVisible(false);
		toolsPane.add(backButton);
	}

	/**
	 * Initializes the options dialog and its components.
	 */
	private void initializeOptionsDialog() {
		// Options button components
		optionsDialog = new JDialog((JFrame) null, "Options", true);
		optionsDialog.setResizable(false);
		JRadioButton dragEnable = new JRadioButton("Enable Node Dragging");
		dragEnable.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		JButton ok = new JButton("OK");
		JButton increaseFontButton = new JButton("Increase Font Size");
		JButton decreaseFontButton = new JButton("Decrease Font Size");
		JLabel zoomLabel = new JLabel("Zoom Level: Mode Switch");
		JSlider zoomSlider = new JSlider(ZOOM_OVERVIEW_MIN, ZOOM_OVERVIEW_MAX,
				zoomOverviewHeight);
		JButton resetPosButton = new JButton("Reset Node Positions");

		// Options buttons component listeners
		dragEnable.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				canDrag = !canDrag;
			}
		});
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				optionsDialog.setVisible(false);
			}
		});
		increaseFontButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (nodeFontSize >= 32)
					return;
				nodeFontSize += 2;
				chapterTitleFontSize += 4;
				chapterDescriptionFontSize += 4;
				for (Node n : nodeList) {
					n.resizeFont(nodeFontSize);
					if (n.hasMultiNodes()) {
						for (Node m : n.getMultiNodeList()) {
							m.resizeFont(nodeFontSize);
						}
					}
				}
				for (Node n : chapterList) {
					n.resizeFont(chapterTitleFontSize,
							chapterDescriptionFontSize);

				}
				if (!isSelectedAChapterNode())
					showSelectedChapter();
				else {
					chapterMap = NodeMap.getChapterCoords(nodeMap,
							chapterTitleFontSize);
					for (Node n : chapterList) {
						Point coord = chapterMap.get(n.getNodeTitle());
						if (Configuration.USE_FIXED_NODE_POSITIONS)
							n.moveToFixedPos();
						else
							n.moveAbsolute(coord.x, coord.y);
					}
				}
			}
		});
		decreaseFontButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (nodeFontSize <= 10)
					return;
				nodeFontSize -= 2;
				chapterTitleFontSize -= 4;
				chapterDescriptionFontSize -= 4;
				for (Node n : nodeList) {
					n.resizeFont(nodeFontSize);
					if (n.hasMultiNodes()) {
						for (Node m : n.getMultiNodeList())
							m.resizeFont(nodeFontSize);
					}
				}
				for (Node n : chapterList) {
					n.resizeFont(chapterTitleFontSize,
							chapterDescriptionFontSize);
				}
				if (!isSelectedAChapterNode())
					showSelectedChapter();
				else {
					setChapterCoords();
				}
			}
		});
		zoomSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					zoomOverviewHeight = (int) source.getValue();
				}
			}
		});
		resetPosButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (isSelectedAChapterNode()) {
					setChapterCoords();
				} else {
					showSelectedChapter();
					vSpaceManager.getActiveView().centerOnGlyph(
							selectedNode.getGlyph(),
							vSpaceManager.getActiveCamera(), 1000, false);
				}
			}
		});

		// Add options button components to the dialog
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new javax.swing.BoxLayout(dialogPanel,
				javax.swing.BoxLayout.Y_AXIS));
		dragEnable.setAlignmentX(CENTER_ALIGNMENT);
		dialogPanel.add(dragEnable);
		increaseFontButton.setAlignmentX(CENTER_ALIGNMENT);
		dialogPanel.add(increaseFontButton);
		dialogPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		decreaseFontButton.setAlignmentX(CENTER_ALIGNMENT);
		dialogPanel.add(decreaseFontButton);
		dialogPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		zoomLabel.setAlignmentX(CENTER_ALIGNMENT);
		dialogPanel.add(zoomLabel);
		zoomSlider.setAlignmentX(CENTER_ALIGNMENT);
		dialogPanel.add(zoomSlider);
		dialogPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		resetPosButton.setAlignmentX(CENTER_ALIGNMENT);
		dialogPanel.add(resetPosButton);
		dialogPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		ok.setAlignmentX(CENTER_ALIGNMENT);
		dialogPanel.add(ok);
		dialogPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		optionsDialog.add(dialogPanel);
		optionsDialog.pack();
	}

	/**
	 * Makes visible all tool buttons
	 */
	private void showToolButtons() {
		medViewRadioButton.setVisible(true);
		highViewRadioButton.setVisible(true);
		pageButton.setVisible(true);
		zoomInButton.setVisible(true);
		zoomOutButton.setVisible(true);
		optionsButton.setVisible(true);
		backButton.setVisible(true);
		searchBar.setVisible(true);
	}

	/**
	 * Adds a node to the undo list, removing the oldest node if 10 nodes are
	 * already stored.
	 * 
	 * @param n
	 *            The node to add.
	 */
	public void addNodeToBackList(Node n) {
		if (backButtonList.size() >= 10)
			backButtonList.removeFirst();
		History undo = new History(n, vSpaceManager.getActiveCamera().altitude);
		backButtonList.addLast(undo);
	}

	/**
	 * Sets the bounds for all of the tools on the toolbar. Should be called
	 * when tools are initialized, and the view is resized.
	 */
	public void setToolSizes() {
		int parentWidth = appFrame.getWidth();
                int parentHeight = appFrame.getHeight();
                int inset = 10;
                
                int buttonWidth = 150;
                int buttonHeight = 25;
                int x = parentWidth - buttonWidth - inset;
                int y = inset;
                /*
		searchBar.setBounds(parentWidth - 155, 10, 150, 25);
		pageButton.setBounds(parentWidth - 105, 100, 100, 25);
		medViewRadioButton.setBounds(parentWidth - 105, 75, 100, 25);
		highViewRadioButton.setBounds(parentWidth - 105, 50, 100, 25);
		// legendButton.setBounds( parentWidth - 105, 160, 100, 25 );
		optionsButton.setBounds(parentWidth - 105, 130, 100, 25);
		zoomInButton.setBounds(parentWidth - 105, 160, 45, 25);
		zoomOutButton.setBounds(parentWidth - 55, 160, 45, 25);
		// legendTextArea.setBounds( parentWidth - 255, parentHeight - 280, 250,
		// 275 );
		backButton.setBounds(10, 10, 100, 25);*/
                
                searchBar.setBounds(x, 10, buttonWidth, buttonHeight);
		pageButton.setBounds(x, 100, buttonWidth, buttonHeight);
		medViewRadioButton.setBounds(x, 75, buttonWidth, buttonHeight);
		highViewRadioButton.setBounds(x, 50, buttonWidth, buttonHeight);
		// legendButton.setBounds( parentWidth - 105, 160, 100, 25 );
		optionsButton.setBounds(x, 130, buttonWidth, buttonHeight);
		zoomInButton.setBounds(x, 160, buttonWidth, buttonHeight);
		zoomOutButton.setBounds(x, 160, buttonWidth, buttonHeight);
		// legendTextArea.setBounds( parentWidth - 255, parentHeight - 280, 250,
		// 275 );
		backButton.setBounds(0, inset, buttonWidth, buttonHeight);
                toolsPane.setBounds(0,0,appFrame.getWidth(), appFrame.getHeight());
	}

	/**
	 * Searches for a node with the given string in its title or description.
	 * 
	 * @param searchString
	 *            The string to search for.
	 * @return A list of nodes that match the given string.
	 */
	public ArrayList<Node> searchForNode(String searchString) {
		ArrayList<Node> nodes = new ArrayList<Node>();
		String str = searchString.toLowerCase();
		String[] toSearchFor = str.split(" ");
		for (Node n : nodeList) {
			for (String s : toSearchFor) {
				if (n.getNodeTitle().toLowerCase().contains(s)
						|| n.getNodeDescription().toLowerCase().contains(s)
						|| n.getNodeChapter().toLowerCase().contains(s)
						|| n.getNodeKeywords().toLowerCase().contains(s)) {
					nodes.add(n);
				}
			}
		}
		return nodes;
	}

    public void setActiveViewSize() {
        if(activeView != null) {
            activeView.getPanel().setBounds(0,0,appFrame.getWidth(), appFrame.getHeight());
        }
        
    }

    public void setStartButtonSize() {
        startButton.setBounds(appFrame.getWidth() / 2 - 100, appFrame.getHeight() / 2 - 40, 200, 80);
    }

	/**
	 * Key Listener for the search toolbar that handles calling the appropriate
	 * search functions.
	 * 
	 * @author Michel Pascale
	 * @version Feb 14, 2012
	 */
	private class SearchBarListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent arg0) {
			// clear the search bar when typing starts
			if (initial) {
				searchBar.setText("");
				initial = false;
			}

			// Perform search when enter is pressed
			if (arg0.getKeyCode() == KeyEvent.VK_ENTER
					&& searchBar.getText().length() > 0) {
				ArrayList<Node> searchResultNodes = searchForNode(searchBar
						.getText());
				if (searchResultNodes.size() == 1) {
					// shift camera focus to node
					selectedNode.unhighlight();
					selectedNode.unhighlightLinks(selectedNode);
					addNodeToBackList(selectedNode);
					selectedNode = searchResultNodes.get(0);
					selectedNode.highlight(Color.yellow, 3);
					// selectedNode.highlightLinks(false);

					if (!isSelectedAChapterNode()) {
						if (!medViewRadioButton.isSelected()) {
							prevSelectedNode = selectedNode;
							medViewRadioButton.setSelected(true);
						} else {
							setNodeVisibilities(false);
							showSelectedChapter();
							vSpaceManager.getActiveView().centerOnGlyph(
									selectedNode.getGlyph(),
									vSpaceManager.getActiveCamera(), 1000);
						}
					}
				} else if (searchResultNodes.size() > 1) {
					// display list of possibilities
					DefaultTableModel dataModel = new DefaultTableModel();
					String[] columnNames = { "Node Name", "Node Chapter" };
					dataModel.setColumnIdentifiers(columnNames);

					for (int i = 0; i < searchResultNodes.size(); i++) {
						String[] row = {
								searchResultNodes.get(i).getNodeTitle(),
								searchResultNodes.get(i).getNodeChapter() };
						dataModel.addRow(row);
					}
					JTable searchTable = new JTable(dataModel);
					searchTable.setDefaultRenderer(
							searchTable.getColumnClass(0),
							new ChapterColorRenderer());
					searchTable.setDefaultEditor(searchTable.getColumnClass(0),
							null);
					searchTable
							.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					searchTable.setRowSelectionAllowed(true);
					searchTable.setColumnSelectionAllowed(false);
					searchTable.setRowSelectionInterval(0, 0);
					searchTable.doLayout();

					JScrollPane scrollPane = new JScrollPane(searchTable);
					scrollPane.setPreferredSize(new Dimension(500, 200));
					Object[] options = { "OK", "CANCEL" };
					int selection = JOptionPane.showOptionDialog(null,
							scrollPane, "Search", JOptionPane.DEFAULT_OPTION,
							JOptionPane.INFORMATION_MESSAGE, null, options,
							options[0]);

					if (selection == 1
							|| selection == JOptionPane.CLOSED_OPTION) {
						initial = true;
						return;
					}

					selectedNode.unhighlight();
					selectedNode.unhighlightLinks(selectedNode);
					addNodeToBackList(selectedNode);
					selectedNode = searchResultNodes.get(searchTable
							.getSelectedRow());
					if (!medViewRadioButton.isSelected()) {
						prevSelectedNode = selectedNode;
						medViewRadioButton.setSelected(true);
					} else {
						setNodeVisibilities(false);
						showSelectedChapter();
						vSpaceManager.getActiveView().centerOnGlyph(
								selectedNode.getGlyph(),
								vSpaceManager.getActiveCamera(), 1000);
					}
					selectedNode.highlight(Color.yellow, 3);
					// selectedNode.highlightLinks(false);
				} else {
					// Color background = JColorChooser.showDialog(null,
					// "JColorChooser Sample", null);
					JOptionPane.showMessageDialog(null,
							"There are no relevant nodes found with the phrase \'"
									+ searchBar.getText() + "\'",
							"No entries found!", JOptionPane.ERROR_MESSAGE);
				}
				initial = true;
			}
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// Empty for this listener.
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			// Empty for this listener.
		}
	}

	private class ChapterColorRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5765844429249765111L;

		public ChapterColorRenderer() {
			super();
		}

		public void setValue(Object value) {
			setText((value == null || !(value instanceof String)) ? ""
					: (String) value);
			Color c = null;
			for (Node n : nodeList) {
				if (n.getNodeTitle().equals(value)) {
					c = n.getNodeChapterColor();
					break;
				}
			}
			if (c != null) {
				this.setBackground(c);
			}
		}
	}

	/**
	 * Shows a dialog with all of the linked nodes connected to the selected
	 * node, dependent on the given multi-node.
	 */
	public void showMultiNodeList() {
		DefaultTableModel dataModel = new DefaultTableModel();
		String[] columnNames = { "Node Name", "Node Chapter" };
		dataModel.setColumnIdentifiers(columnNames);
		ArrayList<Node> multiNodes = new ArrayList<Node>();

		for (Node n : selectedNode.getMultiNodeList()) {
			String type = n.getNodeTitle().split(" ")[1];
			for (Link l : selectedNode.getNodeLinks()) {
				if (l.getLinkType().equals(type)) {
					Node targetNode = l.getFromNode();
					if (targetNode.equals(selectedNode)) {
						targetNode = l.getToNode();
					}
					if (!targetNode.getNodeChapter().equals(
							selectedNode.getNodeChapter())
							&& !targetNode.equals(selectedNode)) {
						String[] row = { targetNode.getNodeTitle(),
								targetNode.getNodeChapter() };
						dataModel.addRow(row);
						multiNodes.add(targetNode);
					}
				}
			}
		}

		JTable selectionTable = new JTable(dataModel);
		selectionTable.setDefaultRenderer(selectionTable.getColumnClass(0),
				new ChapterColorRenderer());
		selectionTable.setDefaultEditor(selectionTable.getColumnClass(0), null);
		selectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectionTable.setRowSelectionAllowed(true);
		selectionTable.setColumnSelectionAllowed(false);
		selectionTable.setRowSelectionInterval(0, 0);
		selectionTable.doLayout();

		JScrollPane scrollPane = new JScrollPane(selectionTable);
		scrollPane.setPreferredSize(new Dimension(500, 200));
		Object[] options = { "OK", "CANCEL" };
		int selection = JOptionPane.showOptionDialog(null, scrollPane,
				"Linked Nodes", JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

		if (selection == 1 || selection == JOptionPane.CLOSED_OPTION)
			return;

		selectedNode.unhighlight();
		selectedNode.unhighlightLinks(selectedNode);
		addNodeToBackList(selectedNode);
		selectedNode = multiNodes.get(selectionTable.getSelectedRow());
		if (!medViewRadioButton.isSelected()) {
			prevSelectedNode = selectedNode;
			medViewRadioButton.setSelected(true);
		} else {
			setNodeVisibilities(false);
			showSelectedChapter();
			vSpaceManager.getActiveView().centerOnGlyph(
					selectedNode.getGlyph(), vSpaceManager.getActiveCamera(),
					1000);
		}
		selectedNode.highlight(Color.yellow, 3);
	}

	/**
	 * Handles when the view radio buttons are selected, disabling the selected
	 * button, clearing the others, and moving to the appropriate view.
	 * 
	 * @param e
	 *            The event that triggered this state change.
	 * @param buttonIndex
	 *            The button that was pressed ( 1 for the Chapter button, 2 for
	 *            the overview button).
	 */
	private void viewRadioItemStateChanged(ItemEvent e, int buttonIndex) {
		if (buttonIndex == 1 && e.getStateChange() == 1) {
			selectMedButton();
		} else if (buttonIndex == 2 && e.getStateChange() == 1) {
			selectHighButton();
		}
	}

	/**
	 * Moves nodes to their starting positions, under their overview node.
	 */
	public void moveNodesToOriginalPositions() {
		// move nodes back to their starting positions
		for (Node node : nodeList) {
			Node chapter = getChapterNode(node.getNodeChapter());
			node.moveTo(chapter.getCenterPoint().x, chapter.getCenterPoint().y);
			if (node.getMultiNodeList() != null) {
				for (Node multiNode : node.getMultiNodeList())
					multiNode.moveTo(chapter.getCenterPoint().x,
							chapter.getCenterPoint().y);
			}
		}
	}

	private void switchToHighLevelView() {
		selectedNode.unhighlight();
		selectedNode.unhighlightLinks(selectedNode);
		cameraListener.deselectNodes();
		moveNodesToOriginalPositions();

		prevSelectedNode = selectedNode;
		selectedNode = getChapterNode(selectedNode.getNodeChapter());

		// Change to high view.
		Camera activeCamera = VirtualSpaceManager.INSTANCE.getActiveCamera();
		activeCamera.setAltitude(zoomOverviewHeight, true);
		activeCamera.moveTo(selectedNode.getGlyph().vx,
				selectedNode.getGlyph().vy);

		selectedNode.highlight(Color.yellow, 3);
		for (Link l : Node.getAllLinks()) {
			l.highlight(true);
			l.unhighlight();
		}
		// selectedNode.highlightLinks(false);

		vSpaceManager.getActiveView().centerOnGlyph(selectedNode.getGlyph(),
				vSpaceManager.getActiveCamera(), 1500, false);
		activeView.setBackgroundColor(Configuration.APPLICATION_BG_COLOR);
	}

	private void switchToMidLevelView() {
		selectedNode.unhighlight();
		selectedNode.unhighlightLinks(selectedNode);
		cameraListener.deselectNodes();
		// Set the selected node to the default node.
		if (prevSelectedNode == null
				|| !prevSelectedNode.getNodeChapter().equals(
						selectedNode.getNodeChapter())) {
			selectedNode = nodeMap.getChapterNodes(
					selectedNode.getNodeChapter()).get(0);
			for (Node n : nodeMap
					.getChapterNodes(selectedNode.getNodeChapter())) {
				if (n.getNodeTitle().equals(
						defaultNodes.get(selectedNode.getNodeChapter()))) {
					selectedNode = n;
					break;
				}
			}
		} else
			selectedNode = prevSelectedNode;

		selectedNode
				.moveAbsolute(getChapterNode(selectedNode.getNodeChapter())
						.getCenterPoint().x,
						getChapterNode(selectedNode.getNodeChapter())
								.getCenterPoint().y);
		showSelectedChapter();

		CameraMovementListener.enabled = false;
		Camera activeCamera = VirtualSpaceManager.INSTANCE.getActiveCamera();
		activeCamera
				.setAltitude(
						zoomOverviewHeight - 300 > ZOOM_OVERVIEW_MIN ? ZOOM_OVERVIEW_MIN - 10
								: zoomOverviewHeight - 300, true);

		selectedNode.highlight(Color.yellow, 3);

		// vSpaceManager.getActiveView().centerOnGlyph(selectedNode.getGlyph(),
		// vSpaceManager.getActiveCamera(), 1000, false);
		CameraMovementListener.enabled = true;
	}

	private void selectMedButton() {
		pageButton.setEnabled(true);
		medViewRadioButton.setSelected(true);
		medViewRadioButton.setEnabled(false);
		highViewRadioButton.setSelected(false);
		highViewRadioButton.setEnabled(true);
		setNodeVisibilities(false);

		switchToMidLevelView();
	}

	private void selectHighButton() {
		pageButton.setEnabled(false);
		medViewRadioButton.setSelected(false);
		medViewRadioButton.setEnabled(true);
		highViewRadioButton.setSelected(true);
		highViewRadioButton.setEnabled(false);
		setNodeVisibilities(true);

		switchToHighLevelView();
	}

	/**
	 * Sets the view level buttons to correspond with the current zoom level.
	 * 
	 * @param altitude
	 *            The altitude of the activeCamera.
	 * @return If a zoom level switch occurred.
	 */
	public boolean updateZoomLevel(float altitude) {
		if (altitude <= ZOOM_CHAPTER_HEIGHT && !medViewRadioButton.isSelected()) {
			medViewRadioButton.setSelected(true);
			return true;
		} else if (altitude >= ZOOM_OVERVIEW_MIN
				&& !highViewRadioButton.isSelected()) {
			highViewRadioButton.setSelected(true);
			return true;
		}
		return false;
	}

	/**
	 * Returns the current zoom level for the overview.
	 * 
	 * @return zoomOverviewHeight
	 */
	public int getZoomOverviewHeight() {
		return zoomOverviewHeight;
	}

	/**
	 * Sets either chapter or overview nodes to be visible.
	 * 
	 * @param isMovingToChapterOverview
	 *            True to show overview nodes, false to show chapter nodes.
	 */
	private void setNodeVisibilities(boolean isMovingToChapterOverview) {
		ViewType newView = isMovingToChapterOverview ? ViewType.HIDDEN
				: ViewType.TITLE_ONLY;
		for (Node n : nodeList) {
			n.showView(newView, selectedNode, true);
			if (n.hasMultiNodes()) {
				for (Node m : n.getMultiNodeList())
					m.showView(ViewType.HIDDEN, selectedNode, true);
			}
		}
		newView = isMovingToChapterOverview ? ViewType.FULL_DESCRIPTION
				: ViewType.HIDDEN;
		for (Node c : chapterList) {
			c.showView(newView, selectedNode, true);
		}
	}

	/**
	 * @return The currently selected node.
	 */
	public Node getSelectedNode() {
		return selectedNode;
	}

	/**
	 * @param n
	 *            The new selected node.
	 */
	public void setSelectedNode(Node n) {
		selectedNode = n;
	}

	/**
	 * @return True if selected node is a chapter, false otherwise.
	 */
	public boolean isSelectedAChapterNode() {
		if (chapterList.contains(selectedNode))
			return true;
		return false;
	}

	/**
	 * Shows only the nodes of the selected chapter, and nodes that are linked
	 * to the currently selected node.
	 */
	public void showSelectedChapter() {
		ArrayList<Node> chapterNodes = nodeMap.getChapterNodes(selectedNode
				.getNodeChapter());

		ArrayList<Node> firstLevelNodes = new ArrayList<Node>();
		firstLevelNodes.addAll(Node.getFirstLevelNodes(selectedNode));

		// Group first level nodes
		if (selectedNode.hasMultiNodes()) {
			for (Node n : selectedNode.getMultiNodeList()) {
				String type = n.getNodeTitle().split(" ")[1];
				for (Link l : selectedNode.getNodeLinks()) {
					if (l.getLinkType().equals(type)) {
						Node targetNode = l.getFromNode();
						if (!firstLevelNodes.remove(targetNode)) {
							targetNode = l.getToNode();
							firstLevelNodes.remove(targetNode);
						}
						targetNode.showView(ViewType.HIDDEN, selectedNode,
								false);
					}
				}
				firstLevelNodes.add(n);
			}
		}

		Map<Integer, Point> coordMap = nodeMap.setNodeCoordsFromFile(
				nodeMap.getChapterNodes(selectedNode.getNodeChapter()),
				selectedNode);
		;
		if (coordMap != null) {
			// Use GraphViz data
			for (Map.Entry<Integer, Point> entry : coordMap.entrySet()) {
				final Node n = chapterNodes.get(entry.getKey());
				n.moveTo(entry.getValue().x, entry.getValue().y);
			}

			// Set firstLevelNodes
			int[] xValues = new int[coordMap.size()];
			int[] yValues = new int[coordMap.size()];
			int i = 0, centerIndex = 0;
			for (Map.Entry<Integer, Point> entry : coordMap.entrySet()) {
				xValues[i] = entry.getValue().x;
				yValues[i] = entry.getValue().y;
				if (chapterNodes.get(entry.getKey()).equals(selectedNode))
					centerIndex = i;
				i++;
			}

			ArrayList<Point> firstLevelPoints = NodeMap
					.setLinkedCoordsForGraphViz(firstLevelNodes, yValues,
							xValues, centerIndex, selectedNode);

			i = 0;
			for (Point p : firstLevelPoints) {
				firstLevelNodes.get(i).moveTo(p.x, p.y);
				i++;
			}
		} else // Graphviz data not available
		{
			coordMap = NodeMap.setNodeCoords(chapterNodes, selectedNode,
					firstLevelNodes);

			ArrayList<Node> nodesInView = new ArrayList<Node>(chapterNodes);
			nodesInView.addAll(firstLevelNodes);

			for (Map.Entry<Integer, Point> entry : coordMap.entrySet()) {
				final Node n = nodesInView.get(entry.getKey());
				n.moveTo(entry.getValue().x, entry.getValue().y);
			}
		}

		// Hide nodes that are not linked to the selected node and show
		// nodes that are
		for (final Node node : nodeList) {
			if (!chapterNodes.contains(node) && !firstLevelNodes.contains(node)) {
				node.showView(ViewType.HIDDEN, selectedNode, true);
			} else if (!node.equals(selectedNode)) {
				int depth = Node.findDistanceFrom(selectedNode, node);
				node.showView(ViewType.TITLE_ONLY, selectedNode, true, depth);
			}
		}

		// Show all multi-nodes
		if (selectedNode.hasMultiNodes()) {
			for (Node n : selectedNode.getMultiNodeList()) {
				n.showView(ViewType.FULL_DESCRIPTION, selectedNode, true);
				n.highlight(Color.WHITE, 3);
			}
		}

		selectedNode.showView(ViewType.FULL_DESCRIPTION, selectedNode, true, 0);

		Color background = selectedNode.getNodeChapterColor();
		float[] hsbColor = new float[3];
		Color.RGBtoHSB(background.getRed(), background.getGreen(),
				background.getBlue(), hsbColor);
		hsbColor[1] = hsbColor[1] * 0.4f;
		hsbColor[2] = Math.min(1.0f, hsbColor[2] * 1.1f);
		background = new Color(Color.HSBtoRGB(hsbColor[0], hsbColor[1],
				hsbColor[2]));
		activeView.setBackgroundColor(background);
	}

	/**
	 * @return If user dragging is enabled.
	 */
	public boolean canDrag() {
		return canDrag;
	}

	/**
	 * Represents an item in the undo list, with selected node and zoom level
	 * information.
	 * 
	 * @author Michel
	 */
	private class History {
		public Node node;
		public float cameraHeight;

		public History(Node n, float h) {
			node = n;
			cameraHeight = h;
		}
	}
}
