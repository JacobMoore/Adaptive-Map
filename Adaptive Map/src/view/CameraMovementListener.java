package view;

import controller.Configuration;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;

import model.Node;
import model.Node.ViewType;
import model.NodeMap;
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

	private AppCanvas canvas;
	private VirtualSpaceManager vSpaceManager = VirtualSpaceManager.INSTANCE;
	private Node highlightedNode;
	private List<Node> nodeList;
	private List<Node> chapterList;
	private List<Point> startingNodeCoords;
	private boolean dragging = false;
	private int xLocation;
	private int yLocation;

	/**
	 * Creates a new CameraMovementListener.
	 * @param canvas
	 *        A reference to the AppCanvas attached to this listener.
	 * @param nodeList
	 *        A reference to the node list of the attached AppCanvas.
	 * @param chapterList
	 *        A reference to the chapter list of the attached AppCanvas.
	 * @param nodeMap
	 *        A reference to the node map of the attached AppCanvas.
	 */
	public CameraMovementListener(AppCanvas canvas, List<Node> nodeList,
	    List<Node> chapterList, NodeMap nodeMap) {
	    this.canvas = canvas;
		this.nodeList = nodeList;
		this.chapterList = chapterList;
		startingNodeCoords = nodeMap.getNodeCoords();
	}

	public void enterGlyph(Glyph glyph) {
		if (!ignoreGlyph(glyph)) {
		    // Determine if the chapter or node list should be searched.
		    List<Node> targetList;
		    if ( canvas.isSelectedAChapterNode() )
		        targetList = chapterList;
		    else
		        targetList = nodeList;

            // If the glyph entered is a node in nodeList, set it as highlighted
            // and show its full description
			for (Node node : targetList) {
				if (glyph.equals(node.getGlyph())) {
					if (highlightedNode == null
							|| highlightedNode != canvas.getSelectedNode()
							&& !canvas.isSelectedAChapterNode()) {
						node.showView(ViewType.FULL_DESCRIPTION, true);
					}
					highlightedNode = node;
					node.highlightLinks(!canvas.isSelectedAChapterNode());
					break;
				}
			}
		}
	}

	public void exitGlyph(Glyph glyph) {
		if (!ignoreGlyph(glyph)) {
		    // Determine if the chapter or node list should be searched.
		    List<Node> targetList;
            if ( canvas.isSelectedAChapterNode() )
                targetList = chapterList;
            else
                targetList = nodeList;

			for (Node node : targetList) {
				if (glyph.equals(node.getGlyph())) {
					// Show title only if the highlighted node is not the
					// selected node
					if (node != canvas.getSelectedNode() && !canvas.isSelectedAChapterNode()) {
						node.showView(ViewType.TITLE_ONLY, true);
					}
					highlightedNode = null;
//                    if ( !canvas.isSelectedAChapterNode() )
                        node.unhighlightLinks();
					break;
				}
			}
		}
	}

	public void click1(ViewPanel pnl, int mod, int jpx, int jpy,
			int clickNumber, MouseEvent me) {
		// If a node was clicked on...
		if (highlightedNode != null) {
		    boolean centerOnNode = true;
		    if ( mod == ViewEventHandler.SHIFT_MOD ) {
		    	System.out.println("<xvalue>" + highlightedNode.getX() + "</xvalue>\t<yvalue>"
		    			+ highlightedNode.getY() + "</yvalue>" );
		    	highlightedNode.printDebugInfo();
		    	return;
		    }

		    // If the node clicked on is the selected node...
			if (highlightedNode.equals(canvas.getSelectedNode())) {
			    // and is a chapter node, move to the correct chapter view
			    if ( canvas.isSelectedAChapterNode() ) {
			        centerOnNode = !canvas.updateZoomLevel(Configuration.ZOOM_CHAPTER_HEIGHT);
			        highlightedNode = null;
			    }
			    // and is a page node, navigate to its content url and return
			    else {
    				canvas.navigateTo(canvas.getSelectedNode().getNodeContentUrl());
                    return;
			    }
			} else if (canvas.getSelectedNode() != null && !canvas.isSelectedAChapterNode()) {
				// If the node clicked on isn't the selected node, and not in overview,
				//  then show just the title of the currently selected node
			    canvas.getSelectedNode().showView(ViewType.TITLE_ONLY, true);
			}

			if (highlightedNode != null) {
    			canvas.setSelectedNode( highlightedNode );
    			highlightedNode = null;
			}

			// Show the full description of the newly selected node
			if(!canvas.isSelectedAChapterNode())
				canvas.getSelectedNode().showView(ViewType.FULL_DESCRIPTION, true);

			if (!canvas.isSelectedAChapterNode())
			    canvas.showSelectedChapter();

	         // Center the camera on the newly selected node
			if (centerOnNode) {
			    vSpaceManager.getActiveView().centerOnGlyph(
			        canvas.getSelectedNode().getGlyph(),
			        vSpaceManager.getActiveCamera(), 1000);
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

					if (highlightedNode != null && canvas.canDrag()) {
						highlightedNode.move(
								(long) (altitude * (jpx - xLocation)),
								(long) (altitude * (yLocation - jpy)));
						xLocation = jpx;
						yLocation = jpy;
					}
			        // Move the camera
					activeCamera.move(altitude * (xLocation - jpx),
							altitude * (jpy - yLocation));
					xLocation = jpx;
					yLocation = jpy;
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

	public void click3(ViewPanel pnl, int mod, int jpx, int jpy,
			int clickNumber, MouseEvent me) {
	    /**
		int count = 0;
	    // Show all nodes and move them back to their starting positions
		for (Node node : nodeList) {
			node.showView(ViewType.TITLE_ONLY);
			int nodeX = startingNodeCoords.get(count).x;
			int nodeY = startingNodeCoords.get(count).y;
			node.moveTo(nodeX, nodeY);
			count++;
		}
		*/
	    VirtualSpaceManager.INSTANCE.getActiveCamera().altitude = Configuration.ZOOM_OVERVIEW_HEIGHT;
	    canvas.updateZoomLevel(Configuration.ZOOM_OVERVIEW_HEIGHT);
	}

	public void click2(ViewPanel pnl, int mod, int jpx, int jpy,
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
		canvas.updateZoomLevel( activeCamera.altitude );
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

	public void moveNodesToOriginalPositions()
	{
	    int count = 0;
        //move nodes back to their starting positions
        for (Node node : nodeList) {
            int nodeX = startingNodeCoords.get(count).x;
            int nodeY = startingNodeCoords.get(count).y;
            node.moveTo(nodeX, nodeY);
            count++;
        }
	}

	public void deselectNodes()
    {
        if (highlightedNode != null) {
            highlightedNode.unhighlightLinks();
            highlightedNode = null;
        }
    }
}
