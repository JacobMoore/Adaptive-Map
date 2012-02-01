package view;

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
 * @author John Nein
 * @version Oct 4, 2011
 */
public class AppCanvas extends JPanel {

	private static final long serialVersionUID = 7885546829753042319L;

	private final VirtualSpaceManager vSpaceManager;
	private List<Node> nodeList;
	private VirtualSpace detailedSpace;
	private Camera detailedCamera;
	public static AppletContext appletContext;

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
		appFrame.add(AppCanvas.this);

		detailedSpace = vSpaceManager
				.addVirtualSpace(Configuration.APPLICATION_TITLE);

		detailedCamera = detailedSpace.addCamera();
		Vector<Camera> cameras = new Vector<Camera>();
		cameras.add(detailedCamera);

		View activeView = vSpaceManager.addFrameView(cameras,
				Configuration.APPLICATION_TITLE, View.STD_VIEW, 800, 600,
				false, false);
		activeView.setEventHandler(new CameraMovementListener(nodeList));
		activeView.setBackgroundColor(Configuration.APPLICATION_BG_COLOR);
		activeView.getPanel().setSize(new Dimension(800, 600));
		// Set the camera location and altitude
		activeView.getActiveCamera().setLocation(new Location(500, -300, 1));
		// Add view to the frame given
		appFrame.add(activeView.getPanel());
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
		nodeList.addAll(XmlParser.parseNodeInformation());
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
}
