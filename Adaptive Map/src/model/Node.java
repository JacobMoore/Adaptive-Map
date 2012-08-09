package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import controller.Configuration;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation.Dimension;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.Translucent;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VRoundRect;
import fr.inria.zvtm.glyphs.VText;

/**
 * Represents a block of information with both a full and short description
 * drawn within a rectangle
 *
 * @author james
 */
public class Node {
	/**
	 * Represents the text of a node
	 */
	private class NodeText extends VText {

		public NodeText(long x, long y, int z, Color textColor, String text) {
			super(x, y, z, textColor, text);
			this.setType("NodeText");
		}

		@Override
		public void draw(Graphics2D g, int vW, int vH, int i, Stroke stdS,
				AffineTransform stdT, int dx, int dy) {
			super.draw(g, vW, vH, i, stdS, stdT, dx, dy);
			realignNodeText();
		}

	}
	/**
	 *  Types of view available for a node.
	 */
	@SuppressWarnings("javadoc")
	public enum ViewType {
		FULL_DESCRIPTION, HIDDEN, TITLE_ONLY;
	}
	
	// List of special 'multinodes' that represent multiple firstlevel nodes
	// with the same link type
	private List<Node> multiNodes;
	
	// List used in multi-nodes, for the selection rectangles
	private List<VRectangle> subNodeRectangles;
	// List used in multi-nodes, references to the "connected" nodes
	private List<Node> subNodes;

	private static final int NODE_NOT_CONNECTED = -999;

	private static List<Link> nodeLinks = new LinkedList<Link>();
	
	/**
	 * @return The list of links between all nodes.
	 */
	public static final List<Link> getAllLinks()
	{
		return nodeLinks;
	}
	
	/**
	 * Clears the list of all links.
	 */
	public static void destroyAllLinks()
	{
		nodeLinks.clear();
	}
	
	// Fixed positions used in overview
	private int fixedXPos = 0, fixedYPos = 0;
	
	/**
	 * Sets the fixed position for this node.
	 * @param x The node's fixed x position.
	 * @param y The node's fixed y position.
	 */
	public void setFixedNodePosition(int x, int y)
	{
		fixedXPos = x;
		fixedYPos = y;
	}
	
	/**
	 * Finds the distance between two nodes based on the links between them.
	 * 
	 * Depth is different from distance in that, if two nodes are on the same level
	 * but are not directly linked, they will have the same depth, but will have
	 * some positive distance from each other.
	 *
	 * @param fromNode
	 *            first node that you are trying to find the depth from
	 * @param toNode
	 *            second node that you are trying to find the depth to
	 * @return the shortest number of links between the two given nodes
	 */
	public static int findDistanceFrom(Node fromNode, Node toNode) {
		if (fromNode.getNodeTitle().equals("Dry Friction") && toNode.getNodeTitle().equals("Rolling Resistance"))
			System.out.print("");
		return findDistanceFrom(fromNode, toNode, new ArrayList<Node>());
	}
	
	/**
	 * Helper method for findDistanceFrom().
	 * @param fromNode The starting node
	 * @param toNode The target node
	 * @param traversedNodes The nodes that have already been traversed
	 * @return The shortest distance between the nodes
	 */
	private static int findDistanceFrom(Node fromNode, Node toNode, List<Node> traversedNodes)
	{
		int shortestDepth = NODE_NOT_CONNECTED;
		traversedNodes.add(fromNode);
		for (Link link : nodeLinks) {
			if (link.contains(fromNode)) {
				Node tempFromNode = link.getFromNode();
				Node tempToNode = link.getToNode();
				if (traversedNodes.contains(tempFromNode)
						&& traversedNodes.contains(tempToNode)) {
					continue;
				}
				if (tempFromNode.equals(fromNode)) {
					if (tempToNode.equals(toNode)) {
						return 1;
					} else {
						int tempDepth = 1 + findDistanceFrom(tempToNode,
								toNode, traversedNodes);
						if (Math.abs(shortestDepth) > Math.abs(tempDepth)) {
							shortestDepth = tempDepth;
						}
					}
				} else {
					if (tempFromNode.equals(toNode)) {
						return 1;
					} else {
						int tempDepth = 1
								+ findDistanceFrom(tempFromNode, toNode,
										traversedNodes);
						if (Math.abs(shortestDepth) > Math.abs(tempDepth)) {
							shortestDepth = tempDepth;
						}
					}
				}
			}
		}
		return shortestDepth;
	}

	/**
	 * Finds the depth of one node from another based on the links between them.
	 * 
	 * Depth is different from distance in that, if two nodes are on the same level
	 * but are not directly linked, they will have the same depth, but will have
	 * some positive distance from each other.
	 *
	 * @param fromNode
	 *            first node that you are trying to find the depth from
	 * @param toNode
	 *            second node that you are trying to find the depth to
	 * @return the shortest number of links between the two given nodes
	 */
	public static int findDepthBetween(Node fromNode, Node toNode) {
		return findDepthBetween(fromNode, toNode, new ArrayList<Node>());
	}

	/**
	 * Helper method for findDepthBetween().
	 * @param fromNode The starting node
	 * @param toNode The target node
	 * @param traversedNodes The nodes that have already been traversed
	 * @return The shortest depth between the nodes
	 */
	private static int findDepthBetween(Node fromNode, Node toNode,
			List<Node> traversedNodes) {
		int shortestDepth = NODE_NOT_CONNECTED;
		traversedNodes.add(fromNode);
		for (Link link : nodeLinks) {
			if (link.contains(fromNode)) {
				Node tempFromNode = link.getFromNode();
				Node tempToNode = link.getToNode();
				if (traversedNodes.contains(tempFromNode)
						&& traversedNodes.contains(tempToNode)) {
					continue;
				}
				if (tempFromNode.equals(fromNode)) { // Going up in depth
					if (tempToNode.equals(toNode)) {
						return 1;
					} else {
						int tempDepth = 1 + findDepthBetween(tempToNode,
								toNode, traversedNodes);
						if (Math.abs(shortestDepth) > Math.abs(tempDepth)) {
							shortestDepth = tempDepth;
						}
					}
				} else { // Going down in depth
					if (tempFromNode.equals(toNode)) {
						return -1;
					} else {
						int tempDepth = -1
								+ findDepthBetween(tempFromNode, toNode,
										traversedNodes);
						if (Math.abs(shortestDepth) > Math.abs(tempDepth)) {
							shortestDepth = tempDepth;
						}
					}
				}
			}
		}
		return shortestDepth;
	}

	/**
	 * Gets a list of all nodes one level out from the given node.
	 * @param selectedNode The node to check.
	 * @return The list of all nodes one level out.
	 */
	public static List<Node> getFirstLevelNodes(Node selectedNode) {
        // Find all of the nodes that will go in the grid
        List<Node> nodesToShow = new ArrayList<Node>();
        // Get the nodes one level out, that are not subNodes
        for (Link firstNodeLink : selectedNode.getNodeLinks()) {
            Node firstLevelNode = firstNodeLink.getFromNode() == selectedNode
                    ? firstNodeLink.getToNode()
                    : firstNodeLink.getFromNode();
            if (!firstLevelNode.getNodeChapter().equals(
                selectedNode.getNodeChapter()) && ( selectedNode.multiNodes == null ||
                !selectedNode.multiNodes.contains(firstLevelNode)))
                nodesToShow.add(firstLevelNode);
        }
        return nodesToShow;
	}

	/**
	 * Links two nodes together with a specific link type.
	 * @param node1 The first node.
	 * @param node2 The second node.
	 * @param linkType The type of link to use.
	 * @param arrowSize The size of the links arrows.
	 * @param isChapter If this link is between overview nodes.
	 */
	public static void link(Node node1, Node node2, String linkType, int arrowSize,
			boolean isChapter) {
	    Link link = null;
	    for ( Link link1 : node1.getNodeLinks() ) {
	        if ( link1.getToNode().equals(node2) )
	            link = link1;
	    }
	    if ( link != null && isChapter && 
	    		link.getWeight() < Configuration.MAX_LINK_WIDTH )
	    {
	        link.setWeight( link.getWeight() + 1 );
	        link.setZindex(link.getZindex()-1);
	    }
	    else
	        nodeLinks.add( new Link(node1, node2, linkType, arrowSize, !isChapter) );
	}
	
	private String nodeChapter;
	private String nodeContentUrl;
	private NodeText nodeDescription;

	// Node graphics variables
	private VRoundRect nodeRectangle;
	// Node information variables
	private NodeText nodeTitle;
	private ViewType nodeView;

	/**
	 * The virtual space that all drawing is done in.
	 */
	protected VirtualSpace virtualSpace;

	/**
	 * Constructor, used for regular ndoe
	 * @param nodeTitle The title for the node.
	 * @param nodeDescription The description for the node.
	 * @param nodeChapter The chapter that the node is in.
	 * @param color The color of the node, generally set by the node's chapter.
	 * @param fontSize The initial size of the node's font.
	 */
	public Node(String nodeTitle, String nodeDescription, String nodeChapter, Color color, int fontSize) {
		// Important to create this before assigning title/description
		nodeRectangle = new VRoundRect(0,
				0, 1, 200, 11, color,
				Color.BLACK, 1f, 15, 15);
		nodeRectangle.setStroke( new BasicStroke( 1.0f ) );
		nodeRectangle.setOwner(Node.class);
		nodeRectangle.setType("Node");

		setNodeTitle(nodeTitle);
		this.nodeTitle.setSpecialFont(new Font("Arial", Font.BOLD, fontSize));
		setNodeDescription(nodeDescription);
		this.nodeDescription.setSpecialFont(new Font("Arial", Font.PLAIN, fontSize));
		setNodeChapter(nodeChapter);
		multiNodes = null;

		showView(ViewType.TITLE_ONLY, this, false);
		bindTextToRectangle();
		realignNodeText();
	}
	
	/**
	 * Constructor, used for overview nodes
	 * @param nodeTitle The title of the node.
	 * @param nodeDescription The description of the node.
	 * @param nodeChapter The chapter the node is in.
	 * @param color The color of the node, generally set by the node's chapter.
	 * @param titleFontSize The font size for this node's title.
	 * @param descriptionFontSize The font size for this node's description.
	 * @param gradientAdjust The positioning of the gradient color of the node.
	 */
	public Node(String nodeTitle, String nodeDescription, String nodeChapter, Color color,
	    int titleFontSize, int descriptionFontSize, float gradientAdjust)
	{
	    this(nodeTitle, nodeDescription, nodeChapter, color, titleFontSize);
	    nodeRectangle = new VRoundRect(0, 0, 1, 700, 200, color,
				Color.BLACK, 1f, 15, 15, gradientAdjust);
		nodeRectangle.setOwner(Node.class);
		nodeRectangle.setType("Node");
	    this.nodeTitle.setSpecialFont(  VText.getMainFont().
	        deriveFont(titleFontSize * 1.0f) );
	    this.nodeDescription.setSpecialFont(  VText.getMainFont().
	        deriveFont(descriptionFontSize * 1.0f) );
	    showView(ViewType.FULL_DESCRIPTION, this, false);
	}
	
	/**
	 * Add this node to the given virtual space
	 *
	 * @param vs
	 *            the VirtualSpace to add the node to
	 */
	public void addToVirtualSpace(VirtualSpace vs) {
		virtualSpace = vs;
		virtualSpace.addGlyph(nodeRectangle);
		virtualSpace.addGlyph(nodeTitle);
		virtualSpace.addGlyph(nodeDescription);
		if ( subNodeRectangles != null )
		{
			for ( VRectangle r : subNodeRectangles )
			{
				virtualSpace.addGlyph(r);
				this.nodeRectangle.stick(r);
			}
		}
	}

	/**
	 * Binds the full and short descriptions to the node's rectangle so they all
	 * move when the node moves
	 */
	private void bindTextToRectangle() {
		Glyph.stickToGlyph(nodeTitle, nodeRectangle);
		Glyph.stickToGlyph(nodeDescription, nodeTitle);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (other instanceof Glyph) {
			return this.nodeRectangle.equals(other);
		}
		if (other instanceof Node) {
			Node theOther = (Node) other;
			return nodeTitle.equals(theOther.nodeTitle)
					&& nodeDescription.equals(theOther.nodeDescription);
		} else {
			return false;
		}
	}

	/**
	 * Return's the center point of the node
	 * @return Point at the center of the node
	 */
	public Point getCenterPoint() {
		return new Point((int)nodeRectangle.vx, (int)nodeRectangle.vy);
	}

	/**
	 * Returns the color of the node's chapter
	 * @return The color of this node's chapter.
	 */
	public Color getNodeChapterColor() {
	    return nodeRectangle.getColor();
	}

	/**
	 * Gets the rectangle for the node. Used for centering on a glyph
	 *
	 * @return the glyph
	 */
	public Glyph getGlyph() {
		return nodeRectangle;
	}

	/**
	 * Returns the node's chapter
	 * @return the nodeChapter
	 */
	public String getNodeChapter() {
		return nodeChapter;
	}

	/**
	 * Returns the node's description
	 * @return nodeDescription in string format
	 */
	public String getNodeDescription()
	{
		return nodeDescription.toString();
	}
	/**
	 * Returns the node's url
	 * @return the nodeContentLocation
	 */
	public String getNodeContentUrl() {
		return nodeContentUrl;
	}

	/**
	 * Returns a list of the node's links.
	 * @return A list of the node's links.
	 */
	public List<Link> getNodeLinks() {
		List<Link> linkList = new LinkedList<Link>();
		for (Link link : nodeLinks) {
			if (link.contains(this)) {
				linkList.add(link);
			}
		}
		return linkList;
	}

	/**
	 * Returns the nodes title
	 * @return nodeTitle in string format
	 */
	public String getNodeTitle() {
		return nodeTitle.getText();
	}

	/**
	 * Return's the average translucency of the node's rectangle,
	 * title and description.
	 * @return the node's translucency value
	 */
	private float getNodeTranslucency() {
		return (nodeRectangle.getTranslucencyValue()
				+ nodeTitle.getTranslucencyValue() + nodeDescription
				.getTranslucencyValue()) / 3;
	}

	/**
	 * Return's the node's view type
	 * @return nodeView THhe current view type
	 */
	public ViewType getViewType() {
		return nodeView;
	}

	/**
	 * Returns the absolute half-width of the nodeRectangle.
	 * @return nodeRectangle.width
	 */
	public long getWidth() {
		return nodeRectangle.getWidth();
	}
	
	/**
	 * Returns the unadjusted width of the node's title
	 * @return nodeTitle.textContainerWidth
	 */
	public long getNodeTitleWidth() {
		return nodeTitle.getTextContainerWidth();
	}
	
	/**
	 * Returns the unadjusted width of the node's description
	 * @return nodeDescription.textContainerWidth
	 */
	public long getNodeDescriptionWidth() {
		return nodeDescription.getTextContainerWidth();
	}
	
	/**
	 * Returns the absolute half-height of the node rectangle.
	 * @return nodeRectangle.height
	 */
	public long getHeight() {
		return nodeRectangle.getHeight();
	}
	
	/**
	 * Returns the unadjusted height of the node's title
	 * @return nodeTitle.textContainerHeight
	 */
	public long getNodeTitleHeight() {
		return nodeTitle.getTextContainerHeight();
	}
	
	/**
	 * Returns the unadjusted height of the node's description
	 * @return nodeDescription.textContainerHeight
	 */
	public long getNodeDescriptionHeight() {
		return nodeDescription.getTextContainerHeight();
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 67 * hash + (nodeTitle != null ? nodeTitle.hashCode() : 0);
		return hash;
	}

	/**
	 * Move the node by x,y relative to current location
	 *
	 * @param x
	 *            horizontal offset
	 * @param y
	 *            vertical offset
	 */
	public void move(long x, long y) {
		nodeRectangle.move(x, y);
		realignNodeText();
		refreshLinks();
	}
	
	/** Move this Node to the given location with no animation.
	 * 
	 * @param x
	 *            the x coordinates to move this node to
	 * @param y
	 *            the y coordinates to move this node to
	 */
	public void moveAbsolute(long x, long y) {
		nodeRectangle.vx = x;
		nodeRectangle.vy = y;
		realignNodeText();
		refreshLinks();
	}

	/**
	 * Moves this Node to the given location, with an animation.
	 *
	 * @param x
	 *            the x coordinates to move this node to
	 * @param y
	 *            the y coordinates to move this node to
	 */
	public void moveTo(int x, int y) {
		Animation nodeTranslation = VirtualSpaceManager.INSTANCE
				.getAnimationManager().getAnimationFactory()
				.createGlyphTranslation(1000, nodeRectangle,
						new LongPoint(x, y), false,
						IdentityInterpolator.getInstance(), null);
		VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(
				nodeTranslation, true);
		realignNodeText();
		refreshLinks();
	}
	
	/**
	 * Moves this Node to its fixed position.
	 */
	public void moveToFixedPos() {
		Animation nodeTranslation = VirtualSpaceManager.INSTANCE
				.getAnimationManager().getAnimationFactory()
				.createGlyphTranslation(1000, nodeRectangle,
						new LongPoint(fixedXPos, fixedYPos), false,
						IdentityInterpolator.getInstance(), null);
		VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(
				nodeTranslation, true);
		realignNodeText();
		refreshLinks();
	}

	/**
	 * Aligns the full and short descriptions with the rectangle so both
	 * descriptions are within the bounds of the rectangle. Needed because the
	 * text and rectangle are not drawn in the same way
	 * Also set's the positions of the sub-rectangles, if there are any.
	 *
	 */
	public void realignNodeText() {
		long titleWidth = nodeTitle.getTextContainerWidth();
		long titleHeight = nodeTitle.getTextContainerHeight();
		int NODE_PADDING = Configuration.NODE_PADDING;

		// Center the node description text
		switch (nodeView) {
			case FULL_DESCRIPTION :
				long descriptionWidth = nodeDescription.getTextContainerWidth();
				long descriptionHeight = nodeDescription.getTextContainerHeight();
				long nodeAdjustedWidth = Math.max(Math.max(titleWidth, 
						descriptionWidth), Configuration.MIN_NODE_WIDTH) 
						+ NODE_PADDING;
				long nodeAdjustedHeight = titleHeight + descriptionHeight
						+ NODE_PADDING;
				// Set adjusted width/height
				nodeRectangle.setWidth(nodeAdjustedWidth / 2 + NODE_PADDING * 2);
				nodeRectangle.setHeight(nodeAdjustedHeight / 2 + NODE_PADDING * 2);
				nodeTitle.moveTo(nodeRectangle.vx - titleWidth / 2
						+ NODE_PADDING, nodeRectangle.vy
						+ nodeRectangle.vh - (long)nodeTitle.getFont().getSize2D()
						- NODE_PADDING);
				nodeDescription.moveTo(nodeRectangle.vx - descriptionWidth / 2
						+ NODE_PADDING, nodeTitle.vy - titleHeight 
						+ (long)nodeTitle.getFont().getSize2D()
						- (long)nodeDescription.getFont().getSize2D()
						+ NODE_PADDING);
				refreshLinks();
				break;
			case TITLE_ONLY :
				nodeAdjustedWidth = Math.max(titleWidth + NODE_PADDING, 
						Configuration.MIN_NODE_WIDTH);
				nodeAdjustedHeight = titleHeight + NODE_PADDING;
				// Set adjusted width/height
				nodeRectangle.setWidth(nodeAdjustedWidth / 2 + NODE_PADDING * 2);
				nodeRectangle.setHeight(nodeAdjustedHeight / 2 + NODE_PADDING * 2);
				nodeTitle.moveTo(nodeRectangle.vx - titleWidth / 2
						+ NODE_PADDING, nodeRectangle.vy
						+ nodeRectangle.getHeight() - (long)nodeTitle.getFont().getSize2D()
						- NODE_PADDING);
				refreshLinks();
				break;
			default :
				break;
		}
		
		// If this is a multi-node, align the sub-rectangles
		if ( subNodeRectangles != null )
		{
			int num = 1;
			for ( VRectangle r : subNodeRectangles )
			{
				r.setWidth(nodeRectangle.vw-NODE_PADDING*2);
				r.setHeight(nodeDescription.getFont().getSize()/2);
				r.moveTo(nodeRectangle.vx, 
						 nodeRectangle.vy - nodeRectangle.vh
						 + num*(nodeDescription.getFont().getSize()+2) + (num-1)*2);
				num++;
			}
		}
	}

	/**
	 * Refreshes the locations of all of the links connected to this node.
	 */
	private void refreshLinks() {
		for (Link link : getNodeLinks()) {
			link.refresh();
		}
	}

	/**
	 * Sets the visibility of links connected to this node.
	 * @param selectedNode The node currently selected, used to set transparency.
	 * @param visibility
	 * 		True to show links, false otherwise.
	 */
	private void setLinksVisibility(final Node selectedNode, boolean visibility) {
		for (Link link : getNodeLinks()) {
			Node fromNode = link.getFromNode();
			Node linkedNode = fromNode.equals(this)
					? link.getToNode()
					: fromNode;
			if (visibility == false || linkedNode.getViewType() == ViewType.HIDDEN)
				link.setVisible(false);
			else {
				int depth = findDepthBetween(selectedNode, linkedNode);
				if ( Math.abs(depth) >= 3)
					link.setVisible(visibility, 0.2f);
				else if ( Math.abs(depth) == 2)
					link.setVisible(visibility, 0.4f);
				else
					link.setVisible(visibility, 1.0f);
			}
		}
	}
	
	/**
	 * Highlights this node.
	 * @param borderColor
	 * 		The new color of the node's border.
	 * @param borderWidth
	 * 		The new border width.
	 */
	public void highlight(Color borderColor, int borderWidth)
	{
		nodeRectangle.setBorderColor(borderColor);
		nodeRectangle.setStrokeWidth(borderWidth);
	}
	
	
	/**
	 * Unhighlights this node.
	 */
	public void unhighlight()
	{
		nodeRectangle.setBorderColor(Color.black);
		nodeRectangle.setStrokeWidth(1);
		if ( subNodeRectangles != null )
		{
			for ( VRectangle r : subNodeRectangles )
				r.setBorderColor(r.getColor());
		}
	}
	
	/**
	 * Highlight all links connected to this node
	 * @param showText
	 * 		If the link text should be displayed.
	 */
	public void highlightLinks(boolean showText) {
        for (Link link : getNodeLinks()) {
            link.highlight(showText);
        }
    }

	/**
	 * Unhighlight all links connected to this node
	 * @param selectedNode The node currently selected
	 */
    public void unhighlightLinks(Node selectedNode) {
        for (Link link: getNodeLinks()) {
            link.unhighlight();
			int depth = 0;
            if ( link.getToNode().equals(selectedNode))
            	findDepthBetween(selectedNode, link.getFromNode());
            else
            	findDepthBetween(selectedNode, link.getToNode());
            
			if ( link.getToNode().getNodeContentUrl() == null)
				continue;
			if ( Math.abs(depth) >= 3)
				link.setTranslucencyValue(0.2f);
			else if ( Math.abs(depth) == 2)
				link.setTranslucencyValue(0.4f);
			else
				link.setTranslucencyValue(1.0f);
        }
    }

	/**
	 * @param nodeChapter
	 *            the nodeChapter to set
	 */
	public void setNodeChapter(String nodeChapter) {
		this.nodeChapter = nodeChapter;
	}

	/**
	 * @param nodeContentUrl
	 *            the nodeContentLocation to set
	 */
	public void setNodeContentUrl(String nodeContentUrl) {
		this.nodeContentUrl = nodeContentUrl;
	}

	/**
	 * @param nodeDescription
	 *         the nodeDescription to set
	 */
	public void setNodeDescription(String nodeDescription) {
		if (this.nodeDescription != null) {
			virtualSpace.removeGlyph(this.nodeDescription);
		}
		this.nodeDescription = new NodeText(nodeRectangle.vx, nodeRectangle.vy,
				1, Configuration.NODE_DESCRIPTION_COLOR, nodeDescription);
	}

	/**
	 * @param nodeTitle
	 *         the nodeTitle to set
	 */
	public void setNodeTitle(String nodeTitle) {
		if (this.nodeTitle != null && virtualSpace != null) {
			virtualSpace.removeGlyph(this.nodeTitle);
		}
		this.nodeTitle = new NodeText(nodeRectangle.vx, nodeRectangle.vy, 1,
				Configuration.NODE_TITLE_COLOR, nodeTitle);
	}

	/**
	 * Sets the translucency of this node to the given alpha. This will animate
	 * the transition between the current translucency and the new one.
	 *
	 * @param alpha
	 *            the alpha of the translucency for the node
	 * @param selectedNode 
	 * 			 The node currently selected, used to determine gradient fill and translucency.
	 * @param animate
	 *            if the transition should be animated
	 */
	private void setNodeTranslucency(final long alpha, final Node selectedNode, boolean animate) {
		if (animate)
		{
			setLinksVisibility(selectedNode, alpha > 0);
			Animation nodeTransparancy = VirtualSpaceManager.INSTANCE
					.getAnimationManager().getAnimationFactory()
					.createTranslucencyAnim(1000, new Translucent() {
						@Override
						public float getTranslucencyValue() {
							return Node.this.getNodeTranslucency();
						}
						@Override
						public void setTranslucencyValue(float alpha1) {
							nodeRectangle.setTranslucencyValue(alpha1);
							nodeTitle.setTranslucencyValue(alpha1);
							nodeDescription.setTranslucencyValue(alpha1);
							if ( subNodeRectangles != null )
							{
								for ( VRectangle r : subNodeRectangles )
									r.setVisible(alpha>0);
							}
						}
					}, alpha, false, IdentityInterpolator.getInstance(),
							new EndAction() {
								@Override
								public void execute(Object subject,
										Dimension dimension) {
									refreshLinks();
								}
							});
			VirtualSpaceManager.INSTANCE.getAnimationManager()
			.startAnimation(nodeTransparancy, true);
		}
		else
		{
			setLinksVisibility(selectedNode, alpha > 0);
			nodeRectangle.setTranslucencyValue(alpha);
			nodeTitle.setTranslucencyValue(alpha);
			nodeDescription.setTranslucencyValue(alpha);
			if ( subNodeRectangles != null )
			{
				for ( VRectangle r : subNodeRectangles )
					r.setVisible(alpha>0);
			}
		}
	}

	/**
	 * Sets the node to show specific information.
	 *
	 * @param viewType
	 *            the type of information view for this node to display
	 * @param selectedNode 
	 * 			  The currently selected node, used to set link transparency. 
	 * @param animate
	 *            whether the transition should be animated if possible
	 */
	public final void showView(ViewType viewType, Node selectedNode, boolean animate) {
		nodeView = viewType;
		boolean nodeDescriptionVisible = false;
		switch (viewType) {
			case FULL_DESCRIPTION :
				nodeDescriptionVisible = true;
			case TITLE_ONLY :
				if (getNodeTranslucency() == 0) {
					setNodeTranslucency(1, selectedNode, animate);
				}
				nodeRectangle.setVisible(true);
				nodeTitle.setVisible(true);
				nodeDescription.setVisible(nodeDescriptionVisible);
				break;
			case HIDDEN :
				setNodeTranslucency(0, selectedNode, animate);
				break;
			default :
		}
	}
	
	/**
	 * Sets the node to show specific information.
	 *
	 * @param viewType
	 *            the type of information view for this node to display
	 * @param selectedNode 
	 * 			  The currently selected node, used to set link transparency. 
	 * @param animate
	 *            whether the transition should be animated if possible
	 * @param depth 
	 * 			  How far away this node is from the selected node.
	 */
	public final void showView(ViewType viewType, Node selectedNode, boolean animate, int depth)
	{
		if ( Math.abs(depth) == 0)
			nodeRectangle.setFillNum(1.0f);
		else if ( Math.abs(depth) == 1 )
			nodeRectangle.setFillNum(0.7f);
		else if ( Math.abs(depth) == 2 )
			nodeRectangle.setFillNum(0.3f);
		else
			nodeRectangle.setFillNum(0.1f);
			
		showView(viewType, selectedNode, animate);
	}
	
	/**
	 * Test method for printing out debug information.
	 */
	public void printDebugInfo()
	{
		System.out.println(nodeRectangle.getFillNum());
	}
	
	/**
	 * @param newSize The new size of this node's font.
	 */
	public void resizeFont( int newSize )
	{
		this.nodeTitle.setSpecialFont(nodeTitle.getFont()
				.deriveFont(newSize * 1.0f));
		this.nodeDescription.setSpecialFont(nodeDescription.getFont()
				.deriveFont(newSize * 1.0f));
	}
	
	/**
	 * Resizes the node's title and description separately.
	 * @param titleSize The new size of this node's title font.
	 * @param descriptionSize The new size of this node's description font.
	 */
	public void resizeFont( int titleSize, int descriptionSize )
	{
		this.nodeTitle.setSpecialFont(nodeTitle.getFont()
				.deriveFont(titleSize * 1.0f));
		this.nodeDescription.setSpecialFont(nodeDescription.getFont()
				.deriveFont(descriptionSize * 1.0f));
	}
	
	/**
	 * @return The list of this node's subnodes.
	 */
	public List<Node> getMultiNodeList()
	{
		return multiNodes;
	}
	
	/**
	 * Goes through all links and creates a list of subnodes
	 * that represent each type of link with 3 or more occurrences.
	 */
	public void initializeMultiNodeList()
	{
        // Initialize map of all link types.
    	Map<String, Integer> linkOccurrences = new HashMap<String, Integer>();
    	ArrayList<String> typeList = Link.getLinkTypes();
    	for ( String s : typeList )
    		linkOccurrences.put(s, 0);
    		
    	// Determine the number of each type of link
        for ( Link l : getNodeLinks() )
        {
        	if ( linkOccurrences.containsKey(l.getLinkType()) && 
        			!l.getToNode().getNodeChapter().equals(l.getFromNode().getNodeChapter()) )
        		linkOccurrences.put(l.getLinkType(), linkOccurrences.get(l.getLinkType())+1);
        }
        
        // Remove entries with less than 3 occurrences
        for ( String s : typeList )
        {
        	if ( linkOccurrences.get(s) < 3 )
        		linkOccurrences.remove(s);
        }
        
        if ( !linkOccurrences.isEmpty() )
            multiNodes = new ArrayList<Node>();
        
        // Create the special nodes
        for ( Entry<String, Integer> entry : linkOccurrences.entrySet() )
        {
        	Node newNode = new Node( entry.getValue() + " " + entry.getKey() 
        			+ " Nodes in Other Chapters", "temp", nodeChapter, Color.white, nodeTitle.getFont().getSize());
        	newNode. subNodeRectangles = new ArrayList<VRectangle>();
    		newNode.subNodes = new ArrayList<Node>();
        	
        	String names = ". \n |TOPICS|:\n ";
            for ( Link l : getNodeLinks() )
            {
            	if ( l.getLinkType().equals(entry.getKey()) && 
            			!l.getToNode().getNodeChapter().equals(l.getFromNode().getNodeChapter()) ) {
            		if ( l.getFromNode().getGlyph().equals(this.getGlyph()) )
            		{
                		names += l.getToNode().getNodeTitle().toUpperCase();
                		Color chapterColor = l.getToNode().getNodeChapterColor();
                		newNode.subNodes.add(l.getToNode());
                		VRectangle rect =  new VRectangle(nodeRectangle.vx - nodeRectangle.vw, 
        						nodeRectangle.vy-nodeRectangle.vh, 2,  
        						nodeRectangle.vw, nodeRectangle.vh, 
        						chapterColor, chapterColor, 0.5f);
		        		rect.setType("MultiNodeRectangle");
		        		newNode.subNodeRectangles.add(rect);
            		}
            		else
            		{
                		names += l.getFromNode().getNodeTitle().toUpperCase();
                		Color chapterColor = l.getFromNode().getNodeChapterColor();
                		newNode.subNodes.add(l.getFromNode());
                		VRectangle rect =  new VRectangle(nodeRectangle.vx - nodeRectangle.vw, 
                						nodeRectangle.vy-nodeRectangle.vh, 2,  
                						nodeRectangle.vw, nodeRectangle.vh, 
                						chapterColor, chapterColor, 0.5f);
                		rect.setType("MultiNodeRectangle");
                		newNode.subNodeRectangles.add(rect);
            		}
            		names += "\n ";
            	}
            }
            
            // Reverse the order of the lists
            Collections.reverse(newNode.subNodeRectangles);
            Collections.reverse(newNode.subNodes);

    		newNode.nodeDescription.setText(Link.getLinkProperty(entry.getKey())
        			.getDescription() + names);
    		
    		newNode.nodeRectangle.setStroke( new BasicStroke( 1.0f ) );
        	newNode.nodeRectangle.setBorderColor(Color.black);
        	newNode.nodeRectangle.setColor(Color.white);
        	
        	newNode.addToVirtualSpace(virtualSpace);
        		
        	Node.link(this, newNode, entry.getKey(), 20, false);
        	newNode.showView(ViewType.HIDDEN, this, false);
        	multiNodes.add(newNode);
        }
	}
	
	/**
	 * @return True if this node has any multinodes, false otherwise
	 */
	public boolean hasMultiNodes()
	{
		return multiNodes != null;
	}

	/**
	 *  Defines a chapter's properties (color and description).
	 *  !NOTE!: This code is a duplicate of AdaptiveMapGUI's ChapterProperties!
	 *  Duplicate any changes across projects to maintain consistency!
	 */
	public static class ChapterProperties {
		private Color chapterColor;
		private String description;
		private int fixedXPosition;
		private int fixedYPosition;
		private String defaultNode;
		private boolean isDefaultChapter;

		/**
		 * Create a new ChapterProperties object.
		 * @param chapterColor The color of the chapter.
		 * @param description The description of the chapter.
		 * @param x The fixed x position of the chapter.
		 * @param y The fixed y position of the chapter.
		 * @param node The chapter's default node.
		 * @param isDefault Is this the default chapter.
		 */
		public ChapterProperties(Color chapterColor, String description, 
				int x, int y, String node, boolean isDefault) {
			this.chapterColor = chapterColor;
			this.description = description;
			fixedXPosition = x;
			fixedYPosition = y;
			defaultNode = node;
			isDefaultChapter = isDefault;
		}
		
		/**
		 * @return fixedXPosition
		 */
		public int getChapterXPos() {
			return fixedXPosition;
		}
		
		/**
		 * @return fixedYPosition
		 */
		public int getChapterYPos() {
			return fixedYPosition;
		}
		
		/**
		 * @return defaultNode
		 */
		public String getDefaultNode() {
			return defaultNode;
		}
		
		/**
		 * @return isDefaultChapter
		 */
		public boolean isDefaultChapter()
		{
			return isDefaultChapter;
		}

		/**
		 * @return The color associated with the chapter.
		 */
		public Color getChapterColor() {
			return chapterColor;
		}

		/**
		 * @param description
		 *            the description to set
		 */
		public void setDescription(String description) {
			this.description = description;
		}

		/**
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}
	}

	/**
	 * Returns the size of nodTitle's font.
	 * @return  nodeTitle.getFont().getSize()
	 */
	public int getTitleFontSize() {
		return nodeTitle.getFont().getSize();
	}

	/**
	 * Given a sub-node selection rectangle, returns the related node.
	 * @param glyph The VRectangle that was entered. 
	 * @return The related node, or null if it is not in the subNodes list.
	 */
	public Node getSubNode(Glyph glyph) {
		for ( VRectangle rect : subNodeRectangles )
		{
			if (rect.equals(glyph))
				return subNodes.get(subNodeRectangles.indexOf(rect));
		}
		System.err.println("No subnodes");
		return null;
	}

	/**
	 * Returns if this node has a list of sub-nodes,
	 * used to determine if this is a multi-node.
	 * @return
	 */
	public boolean hasSubNodes() {
		return subNodes !=  null;
	}
	
	/**
	 * Resets the borders of all sub-node rectangles.
	 */
	public void resetSubNodeRectangleBorders()
	{
		if ( subNodeRectangles != null )
		{
			for ( VRectangle r : subNodeRectangles )
				r.setBorderColor(r.getColor());
		}
		else
			System.err.println("No subnodes");
	}

}
