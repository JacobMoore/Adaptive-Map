package view;

import controller.Configuration;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;

import model.Link;
import model.Node;
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

	private AppCanvas canvas;
	private VirtualSpaceManager vSpaceManager = VirtualSpaceManager.INSTANCE;
	private Node highlightedNode;
	private Link highlightedLink;
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
	 */
	public CameraMovementListener(AppCanvas canvas) {
	    this.canvas = canvas;
	}

	/**
	 * Listener for when the user enters a node.
	 * @param glyph
	 * 			The glyph the user entered.
	 */
	public void enterGlyph(Glyph glyph) {
		// Link text
		if ( highlightedLink == null && highlightedNode == null && glyph.getOwner().equals(model.Link.class))
		{
			for (Link l : Node.getAllLinks())
			{
				if ( glyph.equals(l.getGlyph()) && l.isVisible())
				{
					highlightedLink = l;
					l.showLinkInfo();
					l.highlight(true);
				}
			}
			return;
		}
		if (!ignoreGlyph(glyph)) {
		    // Determine if the chapter or node list should be searched.
		    List<Node> targetList;
		    if ( canvas.isSelectedAChapterNode() )
		        targetList = canvas.getChapterList();
		    else
		        targetList = canvas.getNodeList();

            // If the glyph entered is a node in nodeList, set it as highlighted
            // and show its full description
		    boolean isSubNode = true;
			for (Node node : targetList) {
				if (glyph.equals(node.getGlyph())) {
					if (highlightedNode == null ) {
						node.showView(ViewType.FULL_DESCRIPTION, node, true);
						node.highlight(node.getNodeChapterColor(), 4);
						node.highlightLinks(!canvas.isSelectedAChapterNode());
					}
					isSubNode = false;
					highlightedNode = node;
					break;
				}
			}
			// Node is a sub-node
			if (isSubNode)
			{
				for (Node node : canvas.getSelectedNode().getSubNodeList()) {
					if (glyph.equals(node.getGlyph())) {
						if (highlightedNode == null)
							node.showView(ViewType.FULL_DESCRIPTION, canvas.getSelectedNode(), true);
						highlightedNode = node;
						node.highlight(Color.black, 2);
						break;
					}
				}
			}
		}
	}

	/**
	 * Listener for when the user exits a node.
	 * @param glyph
	 * 			The glyph the user exited.
	 */
	public void exitGlyph(Glyph glyph) {
		// Link text
		if ( highlightedLink != null &&  glyph.equals(highlightedLink.getGlyph()))
		{
			highlightedLink.hideLinkInfo();
			highlightedLink.unhighlight();
			highlightedLink = null;
			return;
		}
		if (!ignoreGlyph(glyph)) {
		    // Determine if the chapter or node list should be searched.
		    List<Node> targetList;
            if ( canvas.isSelectedAChapterNode() )
                targetList = canvas.getChapterList();
            else
                targetList = canvas.getNodeList();

            boolean isSubNode = true;
			for (Node node : targetList) {
				if (glyph.equals(node.getGlyph())) {
					// Show title only if the highlighted node is not the
					// selected node
					node.unhighlight();
					if (node != canvas.getSelectedNode() && !canvas.isSelectedAChapterNode()) {
						node.showView(ViewType.TITLE_ONLY, canvas.getSelectedNode(), true);
					}
					else if ( node == canvas.getSelectedNode() )
						node.highlight(Color.yellow, 2);
					highlightedNode = null;
					node.unhighlightLinks(node);
					//canvas.getSelectedNode().highlightLinks(false);
					isSubNode = false;
					break;
				}
			}
			if (isSubNode) {
				for (Node node : canvas.getSelectedNode().getSubNodeList()) {
					if (glyph.equals(node.getGlyph())) {
							node.showView(ViewType.FULL_DESCRIPTION, canvas.getSelectedNode(), true);
						highlightedNode = null;
						node.unhighlight();
						break;
					}
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
		    	System.out.println("<xvalue>" + highlightedNode.getGlyph().vx + "</xvalue>\t<yvalue>"
		    			+ highlightedNode.getGlyph().vy + "</yvalue>" );
		    	highlightedNode.printDebugInfo();
		    	return;
		    }
		    
		    Node selectedNode = canvas.getSelectedNode();

		    // If the node clicked on is the selected node...
			if (highlightedNode.equals(selectedNode)) {
			    // and is a chapter node, move to the correct chapter view
			    if ( canvas.isSelectedAChapterNode() ) {
		            canvas.addNodeToBackList(selectedNode);
			        centerOnNode = !canvas.updateZoomLevel(Configuration.ZOOM_CHAPTER_HEIGHT);
			        highlightedNode = null;
			    }
			    // and is a page node, navigate to its content url and return
			    else {
    				canvas.navigateTo(selectedNode.getNodeContentUrl());
                    return;
			    }
			}
			// If the node is a sub-node
			else if (selectedNode.hasSubNodes() && 
					selectedNode.getSubNodeList().contains(highlightedNode))
			{
			    vSpaceManager.getActiveView().centerOnGlyph(
				        highlightedNode.getGlyph(),
				        vSpaceManager.getActiveCamera(), 1000, true);
				canvas.showSubNodeList();
				highlightedNode = null;
				return;
			}
			// If the node clicked on isn't the selected node, isn't a sub-node,
			//  and not in overview, then show just the title of the currently selected node
			else if (selectedNode != null && !canvas.isSelectedAChapterNode()) {
				selectedNode.showView(ViewType.TITLE_ONLY, selectedNode, true);
			}

			if (highlightedNode != null) {
				selectedNode.unhighlight();
				selectedNode.unhighlightLinks(highlightedNode);
	            if ( selectedNode.hasSubNodes() ) {
	            	for ( Node n : selectedNode.getSubNodeList())
	            		n.showView(ViewType.HIDDEN, selectedNode, true);
	            }
	            
	            canvas.addNodeToBackList(selectedNode);
    			canvas.setSelectedNode( highlightedNode );

    			// Show the full description of the newly selected node
    			if(!canvas.isSelectedAChapterNode())
    			{
    				highlightedNode.showView(ViewType.FULL_DESCRIPTION, selectedNode, true);
    			    canvas.showSelectedChapter();
    			}
    			
    			highlightedNode.highlight(Color.yellow, 3);
    			highlightedNode.highlightLinks(false);
    			//highlightedNode = null;
			}

	         // Center the camera on the newly selected node
			if (centerOnNode) {
			    vSpaceManager.getActiveView().centerOnGlyph(
			        canvas.getSelectedNode().getGlyph(),
			        vSpaceManager.getActiveCamera(), 1000, false );
			    if ( !canvas.isSelectedAChapterNode() )
			    {
			    	vSpaceManager.getActiveCamera().setAltitude(
			    		Configuration.ZOOM_OVERVIEW_HEIGHT-300 > Configuration.ZOOM_OVERVIEW_MIN ? 
			    		Configuration.ZOOM_OVERVIEW_MIN - 10 : Configuration.ZOOM_OVERVIEW_HEIGHT-300, 
			    		true);
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
			if ( activeCamera.altitude > Configuration.ZOOM_OVERVIEW_MAX )
				activeCamera.altitude = Configuration.ZOOM_OVERVIEW_MAX;
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

	/**
	 * Unhighlights links of the highlighted node, and sets it to null.
	 */
	public void deselectNodes()
    {
        if (highlightedNode != null) {
            highlightedNode.unhighlightLinks(canvas.getSelectedNode());
            highlightedNode = null;
        }
    }
}
