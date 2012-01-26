package view;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import model.Node;
import model.Node.GridLocation;
import model.Node.ViewType;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;

/**
 * 
 * @author John Nein
 * @version Sep 28, 2011
 */
public class CameraMovementListener implements ViewEventHandler {

	// Wheel speed factor
	static final float WSF = 5f;

	private VirtualSpaceManager vSpaceManager = VirtualSpaceManager.INSTANCE;
	private Node highlightedNode;
	private Node selectedNode;
	private List<Node> nodeList;
	private boolean dragging = false;
	private int xLocation;
	private int yLocation;

	/**
	 * @param nodeList
	 */
	public CameraMovementListener(List<Node> nodeList) {
		this.nodeList = nodeList;
	}

	public void enterGlyph(Glyph glyph) {
		if (!ignoreGlyph(glyph)) {
			// If the glyph entered is a node in nodeList, set it as highlighted
			// and show its full description
			for (Node node : nodeList) {
				if (glyph.equals(node.getGlyph())) {
					if (highlightedNode == null
							|| highlightedNode != selectedNode) {
						node.showView(ViewType.FULL_DESCRIPTION);
					}
					highlightedNode = node;
					break;
				}
			}
		}
	}

	public void exitGlyph(Glyph glyph) {
		if (!ignoreGlyph(glyph)) {
			for (Node node : nodeList) {
				if (glyph.equals(node.getGlyph())) {
					// Show title only if the highlighted node is not the
					// selected node
					if (node != selectedNode) {
						node.showView(ViewType.TITLE_ONLY);
					}
					highlightedNode = null;
					break;
				}
			}
		}
	}

	public void click1(ViewPanel pnl, int mod, int jpx, int jpy,
			int clickNumber, MouseEvent me) {
		// If a node was clicked on...
		if (highlightedNode != null) {
			if (highlightedNode.equals(selectedNode)) {
				// If the node clicked on is the selected node, navigate to its
				// content url and return
				AppCanvas.navigateTo(selectedNode.getNodeContentUrl());
				return;
			} else if (selectedNode != null) {
				// If the node clicked on isn't the selected node, then show
				// just the title of the currently selected node
				selectedNode.showView(ViewType.TITLE_ONLY);
			}
			selectedNode = highlightedNode;
			highlightedNode = null;
			// Center the camera on the newly selected node
			vSpaceManager.getActiveView().centerOnGlyph(
					selectedNode.getGlyph(), vSpaceManager.getActiveCamera(),
					1000);
			// Show the full description of the newly selected node
			selectedNode.showView(ViewType.FULL_DESCRIPTION);

			// Move nodes to their location in the grid
			Map<Node, GridLocation> nodeGrid = Node
					.getGridLocations(selectedNode);
			for (Entry<Node, GridLocation> nodeLocation : nodeGrid.entrySet()) {
				nodeLocation.getKey().moveToGridLocation(
						nodeLocation.getValue(), selectedNode.getCenterPoint());
			}

			// Hide nodes that are not linked to the selected node and show
			// nodes that are
			for (final Node node : nodeList) {
				if (!nodeGrid.containsKey(node) && node != selectedNode) {
					node.showView(ViewType.HIDDEN);
				} else if (!node.equals(selectedNode)) {
					node.showView(ViewType.TITLE_ONLY);
				}
			}
		}
	}

	public void press1(ViewPanel pnl, int mod, int jpx, int jpy, MouseEvent me) {
		xLocation = jpx;
		yLocation = jpy;
	}

	public void press2(ViewPanel pnl, int mod, int jpx, int jpy, MouseEvent me) {
		xLocation = jpx;
		yLocation = jpy;
	}

	public void press3(ViewPanel pnl, int mod, int jpx, int jpy, MouseEvent me) {
		xLocation = jpx;
		yLocation = jpy;
	}

	public void release2(ViewPanel pnl, int mod, int jpx, int jpy, MouseEvent me) {
		xLocation = 0;
		yLocation = 0;
	}

	public void release3(ViewPanel pnl, int mod, int jpx, int jpy, MouseEvent me) {
		xLocation = 0;
		yLocation = 0;
	}

	public void mouseDragged(ViewPanel pnl, int i, int buttonNumber, int jpx,
			int jpy, MouseEvent me) {
		switch (buttonNumber) {
			case MouseEvent.BUTTON1 :
				Camera activeCamera = vSpaceManager.getActiveCamera();
				double altitude = (activeCamera.focal + Math
						.abs(activeCamera.altitude))
						/ activeCamera.focal;
				dragging = true;
				synchronized (activeCamera) {
					// If a node is being hovered over, drag that node
					if (highlightedNode != null) {
						highlightedNode.move(
								(long) (altitude * (jpx - xLocation)),
								(long) (altitude * (yLocation - jpy)));
						xLocation = jpx;
						yLocation = jpy;
					} else { // Otherwise just move the camera
						activeCamera.move(altitude * (xLocation - jpx),
								altitude * (jpy - yLocation));
						xLocation = jpx;
						yLocation = jpy;
					}
				}
				break;

		}
	}

	public void release1(ViewPanel pnl, int mod, int jpx, int jpy, MouseEvent me) {
		dragging = false;
	}

	/**
	 * Returns true if actions towards the glyph given should be ignored, false
	 * otherwise.
	 * 
	 * @param glyph
	 *            the glyph to be tested
	 * @return true if actions towards the glyph given should be ignored, false
	 *         otherwise.
	 */
	private boolean ignoreGlyph(Glyph glyph) {
		return dragging || !glyph.isVisible()
				|| glyph.getTranslucencyValue() == (float) 0;
	}

	public void click2(ViewPanel pnl, int mod, int jpx, int jpy,
			int clickNumber, MouseEvent me) {
		// Show all nodes
		for (Node node : nodeList) {
			node.showView(ViewType.TITLE_ONLY);
		}
	}

	public void click3(ViewPanel pnl, int mod, int jpx, int jpy,
			int clickNumber, MouseEvent me) {
	}

	public void mouseMoved(ViewPanel pnl, int i, int i1, MouseEvent me) {
	}

	public void mouseWheelMoved(ViewPanel pnl, short wheelDirection, int i,
			int i1, MouseWheelEvent mwe) {
		Camera activeCamera = VirtualSpaceManager.INSTANCE.getActiveCamera();
		double altitudeFactor = (activeCamera.focal + Math.abs(activeCamera.altitude)) / activeCamera.focal;
		if (wheelDirection == WHEEL_DOWN) {
			activeCamera.altitudeOffset((float) (-altitudeFactor * WSF));
			VirtualSpaceManager.INSTANCE.repaintNow();
		} else {
			activeCamera.altitudeOffset((float) (altitudeFactor * WSF));
			VirtualSpaceManager.INSTANCE.repaintNow();
		}
	}

	public void Ktype(ViewPanel pnl, char c, int i, int i1, KeyEvent ke) {
	}

	public void Kpress(ViewPanel pnl, char c, int i, int i1, KeyEvent ke) {
	}

	public void Krelease(ViewPanel pnl, char c, int i, int i1, KeyEvent ke) {
	}

	public void viewActivated(View view) {
	}

	public void viewDeactivated(View view) {
	}

	public void viewIconified(View view) {
	}

	public void viewDeiconified(View view) {
	}

	public void viewClosing(View view) {
	}

}
