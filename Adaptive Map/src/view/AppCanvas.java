package view;

import java.awt.Point;
import model.Node.ViewType;
import java.awt.Font;
import fr.inria.zvtm.glyphs.VText;
import model.NodeMap;
import java.awt.event.ActionEvent;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import fr.inria.zvtm.widgets.TranslucentButton;
import fr.inria.zvtm.widgets.TranslucentJList;
import javax.swing.JButton;
import java.util.Map;
import javax.swing.JTextArea;
import fr.inria.zvtm.widgets.TranslucentTextArea;
import java.awt.event.ComponentEvent;
import javax.swing.JApplet;
import java.awt.event.ItemEvent;
import javax.swing.JRadioButton;
import fr.inria.zvtm.widgets.TranslucentRadioButton;
import java.awt.event.*;
import fr.inria.zvtm.widgets.TranslucentTextField;
import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import java.applet.AppletContext;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.JPanel;

import model.Link;
import model.Node;
import model.Link.LinkProperties;
import model.Node.ChapterProperties;
import controller.Configuration;
import controller.xml.XmlParser;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 *
 * @author John Nein, Michel Pascale, Lauren Gibboney
 * @version Apr 03, 2011
 */
public class AppCanvas extends JPanel {

	private static final long serialVersionUID = 7885546829753042319L;

	private final VirtualSpaceManager vSpaceManager;
	private List<Node> nodeList;
	private List<Node> chapterList;
	private NodeMap nodeMap;
	private Map<String, Point> chapterMap;
	private VirtualSpace detailedSpace;
	private Camera detailedCamera;
	public AppletContext appletContext;
	private View activeView;
	private CameraMovementListener cameraListener;

	// Tools panel variables.
	private JRadioButton lowViewRadioButton, medViewRadioButton, highViewRadioButton;
	private JButton legendButton;
	private JTextField searchBar;
	private JTextArea legendTextArea;

	// Search Variables
	private DefaultListModel listModel;
	private JList list;
	private Node selected;
	private ArrayList<Node> nodes;
	private boolean initial = true;
	public AppCanvas(VirtualSpaceManager vSpaceManager, Container appFrame,
	    AppletContext context) {
        nodeList = new ArrayList<Node>();
        nodeMap = new NodeMap();
		chapterList = new ArrayList<Node>();
        VText.setMainFont( VText.getMainFont().deriveFont( Configuration.NODE_FONT_SIZE ) );
		this.vSpaceManager = vSpaceManager;
		createView(appFrame, nodeList);
        addTools(appFrame);
        appletContext = context;
		populateCanvas();
		hideSectionNodes();
	}

	/**
	 * Creates the ZVTM virtual space and active view, and sets the camera's
	 * position.
	 *
	 * @param appFrame
	 *            the frame that this instance of AppCanvas will be added to
	 * @param nodeList
	 *            the List that contains all of the nodes for this application;
	 *            this method will populate this list
	 */
	private void createView(Container appFrame, List<Node> nodeList) {
		//appFrame.add(AppCanvas.this);
		detailedSpace = vSpaceManager
				.addVirtualSpace(Configuration.APPLICATION_TITLE);

		detailedCamera = detailedSpace.addCamera();
		Vector<Camera> cameras = new Vector<Camera>();
		cameras.add(detailedCamera);

		/*View*/ activeView = vSpaceManager.addFrameView(cameras,
				Configuration.APPLICATION_TITLE, View.STD_VIEW, 1200, 1200,
				false, false);
	    //the nodeMap must be populated before creating the Camera Listener
        populateNodeMap();
        cameraListener = new CameraMovementListener(this, nodeList, nodeMap);
		activeView.setEventHandler(cameraListener);
		activeView.setBackgroundColor(Configuration.APPLICATION_BG_COLOR);
		activeView.getPanel().setSize(new Dimension(1200, 1200));
		// Set the camera location and altitude
		activeView.getActiveCamera().setLocation(new Location(500, -300, 300));

		// Add view to the frame given
		appFrame.add(activeView.getPanel());
	}
	/**
	 * Populates the canvas by calling parsing functions in the XML parser. The
	 * parser parses all of the nodes in the xml file, and adds each one to the
	 * virtual space; the parsing is then continued to parse the properties for
	 * each link type, then parses the actual links between the nodes and adds
	 * them to the virtual space.
	 * @precondition populateNodeMap has been called.
	 */
	private void populateCanvas() {
        nodeList.addAll(nodeMap.getNodes());
        for (Node nodeToAdd : nodeList) {
            nodeToAdd.showView( ViewType.ONLY_RECTANGLE );
            nodeToAdd.addToVirtualSpace(detailedSpace);
        }
        // IMPORTANT: parse node properties before linking the nodes
        for (Entry<String, LinkProperties> linkProperty : XmlParser
                .parseLinkProperties().entrySet()) {
            Link.addLinkType(linkProperty.getKey(), linkProperty.getValue());
        }
        XmlParser.parseNodeLinks(nodeList);

        // Set the chapter node positions.
        chapterMap = nodeMap.getChapterCoords();
        for ( Node n : chapterList ) {
            Point coord = chapterMap.get( n.getNodeTitle() );
            n.moveTo( coord.x, coord.y );
        }
        parseChapterLinks();
	}

	/**
	 * Goes through all nodes and sets up the links between chapters.
	 */
	private void parseChapterLinks() {
	    for ( String chapter : nodeMap.getChapters() ) {
	        Node currentChapter = getChapterNode( chapter );
	        if ( currentChapter == null )
	            continue;
	        // For all nodes in current chapter
	        for ( Node node : nodeMap.getChapterNodes( chapter ) ) {
	            // For all linked nodes
	            for ( Node i : node.getLinkedNodes() ) {
	                // Check not linked to current chapter
	                if ( !i.getNodeChapter().equals( chapter ) ) {
	                    Node linkedChapter = getChapterNode(i.getNodeChapter());
	                    if ( linkedChapter == null )
	                        continue;
	                    Node.link( currentChapter, linkedChapter, Link.LinkLineType.STANDARD.name() );
	                }
	            }
	        }
	    }
	}

	/**
	 * Returns the node of the specified chapter in the chapterList, or null
	 * if it was not found.
	 */
	private Node getChapterNode( String chapterName) {
	    for ( Node node : chapterList ) {
	        if ( node.getNodeTitle().equals( chapterName ) )
	            return node;
	    }
        System.err.println( "Chapter not found in chapterList." );
	    return null;
	}

	public ArrayList<Node> searchForNode(String searchString)
	{
		ArrayList<Node> nodes = new ArrayList<Node>();
		String str = searchString.toLowerCase();
		for (Node n: nodeList)
		{
			if (n.getNodeTitle().toLowerCase().contains(str) || n.getNodeDescription().toLowerCase().contains(str) ||
					n.getNodeChapter().toLowerCase().contains(str))
			{
				nodes.add(n);
			}
		}
		return nodes;
	}
    private void populateNodeMap()
    {
        // IMPORTANT: parse chapter properties before parsing node information
        for (Entry<String, ChapterProperties> chapterProperty : XmlParser
                .parseChapterProperties().entrySet()) {
            Node.addChapterType(chapterProperty.getKey(), chapterProperty
                    .getValue());

            // Add the chapter to the chapter list, and the vs.
            Node newChapter = new Node(chapterProperty.getKey(), chapterProperty
                    .getValue().getDescription(), chapterProperty.getKey(),
                    Configuration.CHAPTER_TITLE_FONT_SIZE,
                    Configuration.CHAPTER_DESCRIPTION_FONT_SIZE);
            newChapter.showView( ViewType.TITLE_ONLY );
            newChapter.addToVirtualSpace( detailedSpace );
            chapterList.add( newChapter );
        }
        nodeMap = XmlParser.parseNodeInformation();
    }
	/**
	 * Navigates a browser window to the given url.
	 *
	 * @param url
	 *            the url to navigate to
	 */
	public void navigateTo(String url) {

		URL relativeURL = null;
		try {
			relativeURL = new URL (appletContext.getApplet("Adaptive Map").getCodeBase(),
					url);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		try {
			if (appletContext != null) {
				// Application started in an applet
				appletContext.showDocument(relativeURL, "_blank");
			} else {
				// Standalone application
				java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
			}
		} catch (Exception e) {
			System.out.println("Error navigating to url " + url + ".");
			e.printStackTrace();
		}
	}

   /**
    * Adds search, view-control, and settings tools to the view.
    * @param appFrame
     *       the frame that this instance of AppCanvas will be added to
    */
   private void addTools( Container appFrame )
   {
        JLayeredPane toolsPane;
        if ( appFrame.getClass() == JFrame.class )
            toolsPane = ((JFrame)appFrame).getRootPane().getLayeredPane();
        else
            toolsPane = ((JApplet)appFrame).getRootPane().getLayeredPane();

        appFrame.addComponentListener( new ComponentListener() {
            @Override
            public void componentHidden( ComponentEvent e ) {
                // Empty for this listener.
            }
            @Override
            public void componentMoved( ComponentEvent e ) {
                // Empty for this listener.
            }
            @Override
            public void componentResized( ComponentEvent e ) {
                setToolSizes();
            }
            @Override
            public void componentShown( ComponentEvent e ) {
                setToolSizes();
            } } );
        searchBar = new TranslucentTextField("Search...");
        searchBar.setForeground(Color.WHITE);
        searchBar.setBackground(Color.DARK_GRAY);
        searchBar.setToolTipText( "Enter your search terms, and press ENTER to search." );
        searchBar.addKeyListener( new SearchBarListener() );
        toolsPane.add(searchBar, (Integer)(JLayeredPane.DRAG_LAYER + 50));

        lowViewRadioButton = new TranslucentRadioButton("Section", false);
        lowViewRadioButton.addItemListener( new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                viewRadioItemStateChanged(e, 0);
            }
        });
        toolsPane.add(lowViewRadioButton, (Integer)(JLayeredPane.DEFAULT_LAYER+50));

        medViewRadioButton = new TranslucentRadioButton("Chapter", false);
        medViewRadioButton.addItemListener( new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                viewRadioItemStateChanged(e, 1);
            }
        });
        toolsPane.add(medViewRadioButton, (Integer)(JLayeredPane.DEFAULT_LAYER+50));

        highViewRadioButton = new TranslucentRadioButton("Overview", true);
        highViewRadioButton.setEnabled( false );
        highViewRadioButton.addItemListener( new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                viewRadioItemStateChanged(e, 2);
            }
        });
        toolsPane.add(highViewRadioButton, (Integer)(JLayeredPane.DEFAULT_LAYER+50));

        legendTextArea = new TranslucentTextArea();
        legendTextArea.setEditable( false );
        legendTextArea.setLineWrap( true );
        legendTextArea.setForeground(Color.WHITE);
        legendTextArea.setBackground(Color.DARK_GRAY);
        legendTextArea.setTabSize( 8 );

        String legendString = new String("\tLegend\n1");
        Map<String, LinkProperties> links = XmlParser.parseLinkProperties();
        Object[] linkTypes = links.keySet().toArray();
        for( int i=0; i < linkTypes.length; ++i ) {
            legendString += linkTypes[i].toString() + ": ";
            String description = links.get(linkTypes[i]).getDescription();
            legendString += description + "\n";
        }
        legendTextArea.setText( legendString );
        toolsPane.add(legendTextArea, (Integer)(JLayeredPane.DEFAULT_LAYER+50));

        legendButton = new TranslucentButton("Legend");
        legendButton.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed( ActionEvent e )
                {
                    legendTextArea.setVisible( !legendTextArea.isVisible() );
                }
            });
        toolsPane.add(legendButton, (Integer)(JLayeredPane.DEFAULT_LAYER+50));
    }
   /**
    * Sets the bounds for all of the tools on the toolbar. Should be called
    * when tools are initialized, and the view is resized.
    */
   private void setToolSizes()
   {
       int parentWidth = searchBar.getParent().getWidth();
       int parentHeight = searchBar.getParent().getHeight();
       searchBar.setBounds( parentWidth - 155, 25, 150, 25);
       lowViewRadioButton.setBounds( parentWidth - 105, 50, 100, 25);
       medViewRadioButton.setBounds( parentWidth - 105, 75, 100, 25);
       highViewRadioButton.setBounds( parentWidth - 105, 100, 100, 25);
       legendTextArea.setBounds( parentWidth - 255, parentHeight - 230, 250, 225 );
       legendButton.setBounds( parentWidth - 105, 130, 100, 25 );
   }
   /**
    *  Key Listener for the search toolbar that handles calling the appropriate
    *  search functions.
    *  @author Michel Pascale
    *  @version Feb 14, 2012
    */
    private class SearchBarListener implements KeyListener
    {
        @Override
        public void keyPressed( KeyEvent arg0 )
        {
            // clear the search bar when typing starts
            if ( initial )
            {
                searchBar.setText( "" );
                initial = false;
            }

            // Perform search when enter is pressed
            if ( arg0.getKeyCode() == KeyEvent.VK_ENTER
                && searchBar.getText().length() > 0 )
            {
                nodes = searchForNode( searchBar.getText() );
                if ( nodes.size() == 1 )
                {
                    // shift camera focus to node
                    detailedCamera.move(
                        nodes.get( 0 ).getX() - 500,
                        nodes.get( 0 ).getY() + 300 );
                }
                else if ( nodes.size() > 1 )
                {
                    // display list of possibilities
                    listModel = new DefaultListModel();

                    for ( int i = 0; i < nodes.size(); i++ )
                    {
                        listModel.addElement( nodes.get( i ).getNodeTitle() );

                    }
                    list = new JList( listModel );
                    list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
                    list.setSelectedIndex( 0 );
                    list.addListSelectionListener( new Listen() );
                    list.setVisibleRowCount( 5 );

                    JScrollPane scrollPane = new JScrollPane( list );
                    JOptionPane.showMessageDialog(
                        null,
                        scrollPane,
                        "Search",
                        JOptionPane.INFORMATION_MESSAGE );
                }
            }
        }


        @Override
        public void keyReleased( KeyEvent arg0 )
        {
            // Empty for this listener.
        }


        @Override
        public void keyTyped( KeyEvent arg0 )
        {
            // Empty for this listener.
        }
    }
   private class Listen implements ListSelectionListener
   {

	@Override
	  public void valueChanged(ListSelectionEvent e)
	  {
        if (e.getValueIsAdjusting() == false)
        {
        	//get selected Node
            if (list.getSelectedIndex() != -1)
            {
            	selected = nodes.get(list.getSelectedIndex());
            	//java.awt.Desktop.getDesktop().browse(selected.getNodeContentUrl());
            }


        }
	  }

   }
    /**
     *  Handles when the view radio buttons are selected, disabling the selected
     *  button, clearing the others, and moving to the appropriate view.
     */
    private void viewRadioItemStateChanged( ItemEvent e, int buttonIndex )
    {
        if ( buttonIndex == 0 && e.getStateChange() == 1 ) {
            selectLowButton();
            //Change to low view.
            Camera activeCamera = VirtualSpaceManager.INSTANCE.getActiveCamera();
            activeCamera.setAltitude( Configuration.ZOOM_NODE_HEIGHT, true );
        }
        else if ( buttonIndex == 1 && e.getStateChange() == 1 ) {
            switchToMidLevelView();
        }
        else if ( buttonIndex == 2 && e.getStateChange() == 1 ) {
            switchToHighLevelView();
        }
    }

    public void switchToHighLevelView()
    {
        cameraListener.deselectNodes();
        cameraListener.moveNodesToOriginalPositions();
        selectHighButton();
        //Change to high view.
        Camera activeCamera = VirtualSpaceManager.INSTANCE.getActiveCamera();
        activeCamera.setAltitude( Configuration.ZOOM_OVERVIEW_HEIGHT, true );
    }

    public void switchToMidLevelView()
    {
        selectMedButton();
        //Change to med view.
        Camera activeCamera = VirtualSpaceManager.INSTANCE.getActiveCamera();
        activeCamera.setAltitude( Configuration.ZOOM_CHAPTER_HEIGHT, true );
    }

    private void selectLowButton()
    {
        lowViewRadioButton.setSelected( true );
        lowViewRadioButton.setEnabled( false );
        medViewRadioButton.setSelected( false );
        medViewRadioButton.setEnabled( true );
        highViewRadioButton.setSelected( false );
        highViewRadioButton.setEnabled( true );
        //setNodeVisibilities(false);
    }
    private void selectMedButton()
    {
        lowViewRadioButton.setSelected( false );
        lowViewRadioButton.setEnabled( true );
        medViewRadioButton.setSelected( true );
        medViewRadioButton.setEnabled( false );
        highViewRadioButton.setSelected( false );
        highViewRadioButton.setEnabled( true );
        setNodeVisibilities(false);
    }
    private void selectHighButton()
    {
        lowViewRadioButton.setSelected( false );
        lowViewRadioButton.setEnabled( true );
        medViewRadioButton.setSelected( false );
        medViewRadioButton.setEnabled( true );
        highViewRadioButton.setSelected( true );
        highViewRadioButton.setEnabled( false );
        setNodeVisibilities(true);
    }
    /**
     * Sets the view level buttons to correspond with the current zoom level.
     * @param altitude
     *      The altitude of the activeCamera.
     */
    public void updateZoomLevel( float altitude )
    {
        if ( altitude >= Configuration.ZOOM_NODE_HEIGHT && altitude < Configuration.ZOOM_NODE_HEIGHT
            + Configuration.ZOOM_HEIGHT_PADDING && !lowViewRadioButton.isSelected() ) {
            selectLowButton();
        }
        else if ( altitude >= Configuration.ZOOM_CHAPTER_HEIGHT && altitude < Configuration.ZOOM_CHAPTER_HEIGHT
            + Configuration.ZOOM_HEIGHT_PADDING && !medViewRadioButton.isSelected() ) {
            selectMedButton();
        }
        else if ( altitude >= Configuration.ZOOM_OVERVIEW_HEIGHT &&
            !highViewRadioButton.isSelected() ) {
            selectHighButton();
        }
    }

    public void setNodeVisibilities( boolean isMovingToChapterOverview )
    {
        ViewType newView = isMovingToChapterOverview ? ViewType.HIDDEN : ViewType.TITLE_ONLY;
        for (Node n : nodeList) {
            n.showView( newView );
        }
        newView = isMovingToChapterOverview ? ViewType.FULL_DESCRIPTION : ViewType.HIDDEN;
        for (Node c : chapterList) {
            c.showView( newView );
        }
    }

    public void hideSectionNodes()
    {
        for (Node n : nodeList) {
            n.showView( ViewType.HIDDEN );
        }
        for (Node n : nodeList) {
            n.showView( ViewType.FULL_DESCRIPTION );
        }
    }
}
