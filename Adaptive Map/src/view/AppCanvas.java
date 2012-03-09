package view;

import model.NodeMap;
import java.awt.event.ActionEvent;
import javax.swing.border.BevelBorder;
import fr.inria.zvtm.widgets.TranslucentButton;
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
import java.awt.Container;
import java.awt.Dimension;
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

/**
 *
 * @author John Nein, Michel Pascale
 * @version Feb 14, 2011
 */
public class AppCanvas extends JPanel {

	private static final long serialVersionUID = 7885546829753042319L;

	// Variables for the minimum Camera height at different view levels.
	private static final int HEIGHT_PADDING = 50;
	private static final int NODE_HEIGHT = 1;
	private static final int CHAPTER_HEIGHT = 100;
	private static final int OVERVIEW_HEIGHT = 300;

	private final VirtualSpaceManager vSpaceManager;
	private List<Node> nodeList;
	private VirtualSpace detailedSpace;
	private Camera detailedCamera;
	public static AppletContext appletContext;
	private JRadioButton lowViewRadioButton, medViewRadioButton, highViewRadioButton;
	private JButton legendButton;
	private JTextField searchBar;
	private JTextArea legendTextArea;

	public AppCanvas(VirtualSpaceManager vSpaceManager, Container appFrame) {
		nodeList = new ArrayList<Node>();
		this.vSpaceManager = vSpaceManager;
		createView(appFrame, nodeList);
		populateCanvas();
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
		detailedSpace = vSpaceManager
				.addVirtualSpace(Configuration.APPLICATION_TITLE);

		detailedCamera = detailedSpace.addCamera();
		Vector<Camera> cameras = new Vector<Camera>();
		cameras.add(detailedCamera);

		View activeView = vSpaceManager.addFrameView(cameras,
				Configuration.APPLICATION_TITLE, View.STD_VIEW, 800, 600,
				false, false);
		activeView.setEventHandler(new CameraMovementListener(this, nodeList));
		activeView.setBackgroundColor(Configuration.APPLICATION_BG_COLOR);
		activeView.getPanel().setSize(new Dimension(800, 600));
		// Set the camera location and altitude
		activeView.getActiveCamera().setLocation(new Location(500, -300, 100));

		// Add view to the frame given
		appFrame.add(activeView.getPanel());

		addTools(appFrame);
	}
	/**
	 * Populates the canvas by calling parsing functions in the XML parser. The
	 * parser parses all of the nodes in the xml file, and adds each one to the
	 * virtual space; the parsing is then continued to parse the properties for
	 * each link type, then parses the actual links between the nodes and adds
	 * them to the virtual space.
	 */
	private void populateCanvas() {
		// IMPORTANT: parse chapter properties before parsing node information
		for (Entry<String, ChapterProperties> chapterProperty : XmlParser
				.parseChapterProperties().entrySet()) {
			Node.addChapterType(chapterProperty.getKey(), chapterProperty
					.getValue());
		}
		NodeMap nodeMap = XmlParser.parseNodeInformation();
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

	}
	/**
	 * Navigates a browser window to the given url.
	 *
	 * @param url
	 *            the url to navigate to
	 */
	public static void navigateTo(String url) {
		try {
			if (appletContext != null) {
				// Application started in an applet
				appletContext.showDocument(new URL(url), "_blank");
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
        toolsPane.add(searchBar, (Integer)(JLayeredPane.DEFAULT_LAYER+50));

        lowViewRadioButton = new TranslucentRadioButton("Section", false);
        lowViewRadioButton.addItemListener( new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                viewRadioItemStateChanged(e, 0);
            }
        });
        toolsPane.add(lowViewRadioButton, (Integer)(JLayeredPane.DEFAULT_LAYER+50));

        medViewRadioButton = new TranslucentRadioButton("Chapter", true);
        medViewRadioButton.setEnabled( false );
        medViewRadioButton.addItemListener( new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                viewRadioItemStateChanged(e, 1);
            }
        });
        toolsPane.add(medViewRadioButton, (Integer)(JLayeredPane.DEFAULT_LAYER+50));

        highViewRadioButton = new TranslucentRadioButton("Overview", false);
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
        public void keyPressed( KeyEvent e ) {
            if ( e.getKeyCode() == KeyEvent.VK_ENTER ) {
                //TODO: Handle searching.
                System.out.println( "Pressed ENTER in search bar." );
            }
        }
        @Override
        public void keyReleased( KeyEvent e ) {
            // Empty for this listener.
        }
        @Override
        public void keyTyped( KeyEvent e ) {
            // Empty for this listener.
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
            activeCamera.setAltitude( NODE_HEIGHT, true );
        }
        else if ( buttonIndex == 1 && e.getStateChange() == 1 ) {
            selectMedButton();
            //Change to med view.
            Camera activeCamera = VirtualSpaceManager.INSTANCE.getActiveCamera();
            activeCamera.setAltitude( CHAPTER_HEIGHT, true );
        }
        else if ( buttonIndex == 2 && e.getStateChange() == 1 ) {
            selectHighButton();
            //Change to high view.
            Camera activeCamera = VirtualSpaceManager.INSTANCE.getActiveCamera();
            activeCamera.setAltitude( OVERVIEW_HEIGHT, true );
        }
    }
    private void selectLowButton()
    {
        lowViewRadioButton.setSelected( true );
        lowViewRadioButton.setEnabled( false );
        medViewRadioButton.setSelected( false );
        medViewRadioButton.setEnabled( true );
        highViewRadioButton.setSelected( false );
        highViewRadioButton.setEnabled( true );
    }
    private void selectMedButton()
    {
        lowViewRadioButton.setSelected( false );
        lowViewRadioButton.setEnabled( true );
        medViewRadioButton.setSelected( true );
        medViewRadioButton.setEnabled( false );
        highViewRadioButton.setSelected( false );
        highViewRadioButton.setEnabled( true );
    }
    private void selectHighButton()
    {
        lowViewRadioButton.setSelected( false );
        lowViewRadioButton.setEnabled( true );
        medViewRadioButton.setSelected( false );
        medViewRadioButton.setEnabled( true );
        highViewRadioButton.setSelected( true );
        highViewRadioButton.setEnabled( false );
    }
    /**
     * Sets the view level buttons to correspond with the current zoom level.
     * @param altitude
     *      The altitude of the activeCamera.
     */
    public void updateZoomLevel( float altitude )
    {
        if ( altitude >= NODE_HEIGHT && altitude < NODE_HEIGHT + HEIGHT_PADDING &&
            !lowViewRadioButton.isSelected() ) {
            selectLowButton();
        }
        else if ( altitude >= CHAPTER_HEIGHT && altitude < CHAPTER_HEIGHT +
            HEIGHT_PADDING && !medViewRadioButton.isSelected() ) {
            selectMedButton();
        }
        else if ( altitude >= OVERVIEW_HEIGHT && !highViewRadioButton.isSelected() ) {
            selectHighButton();
        }
    }
}
