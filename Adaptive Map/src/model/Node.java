package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
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
	 * Represents an (x,y) coordinate location in the grid surrounding a
	 * selected node.
	 *
	 * @author John Nein
	 * @version Nov 9, 2011
	 */
	public static class GridLocation {
		private int x;
		private int y;

		/**
		 * Create a new GridLocation object.
		 * @param x The x for this GridLocation.
		 * @param y The y for this GridLocation.
		 */
		public GridLocation(int x, int y) {
			this.setX(x);
			this.setY(y);
		}

		/**
		 * Returns the x coordinate location.
		 *
		 * @return the x
		 */
		public int getX() {
			return x;
		}

		/**
		 * Returns the y coordinate location.
		 *
		 * @return the y
		 */
		public int getY() {
			return y;
		}

		/**
		 * Sets the x coordinate location.
		 *
		 * @param x
		 *            the x to set
		 */
		public void setX(int x) {
			if (x < 0) {
				throw new UnsupportedOperationException(
						"Cannot declare a negative x grid index.");
			}
			this.x = x;
		}

		/**
		 * Sets the y coordinate location.
		 *
		 * @param y
		 *            the y to set
		 */
		public void setY(int y) {
			this.y = y;
		}
	}
	/**
	 * Represents the text of a node
	 */
	private class NodeText extends VText {

		public NodeText(long x, long y, int z, Color textColor, String text) {
			super(x, y, z, textColor, text);
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
	public enum ViewType {
		FULL_DESCRIPTION, HIDDEN, TITLE_ONLY;
	}

	private static Map<String, ChapterProperties> chapterTypes;
	
	private List<Node> subNodes;

	/**
	 * Adds a chapter with its associated properties to the map.
	 * @param chapterName The name of the chapter.
	 * @param chapterProperties The properties of the chapter.
	 */
	public static void addChapterType(String chapterName,
			ChapterProperties chapterProperties) {
		if (chapterTypes == null) {
			chapterTypes = new HashMap<String, ChapterProperties>();
		}
		chapterTypes.put(chapterName, chapterProperties);
	}

	private static final int NODE_NOT_CONNECTED = -999;

	private static final int NODE_PADDING = 2; // in px
	private static List<Link> nodeLinks = new LinkedList<Link>();
	
	public final static List<Link> getAllLinks()
	{
		return nodeLinks;
	}
	
	private int fixedXPos = 0, fixedYPos = 0;
	
	public void setFixedNodePosition(int x, int y)
	{
		fixedXPos = x;
		fixedYPos = y;
	}

	/**
	 * Finds the depth of one node from another based on the links between them.
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
                selectedNode.getNodeChapter()) && ( selectedNode.subNodes == null ||
                !selectedNode.subNodes.contains(firstLevelNode)))
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
	        nodeLinks.add( new Link(node1, node2, linkType, arrowSize) );
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
	 * Constructor
	 * @param nodeTitle The title for the node.
	 * @param nodeDescription The description for the node.
	 * @param nodeChapter The chapter that the node is in.
	 */
	public Node(String nodeTitle, String nodeDescription, String nodeChapter) {
		// Important to create this before assigning title/description
		nodeRectangle = new VRoundRect(0,
				0, 1, 200, 11, chapterTypes.get(nodeChapter).getChapterColor(),
				Color.BLACK, 1f, 15, 15);
		nodeRectangle.setStroke( new BasicStroke( 2.0f ) );

		setNodeTitle(nodeTitle);
		this.nodeTitle.setSpecialFont(new Font("Arial", Font.BOLD, 12));
		setNodeDescription(nodeDescription);
		this.nodeDescription.setSpecialFont(new Font("Arial", Font.PLAIN, 12));
		setNodeChapter(nodeChapter);
		subNodes = null;

		showView(ViewType.TITLE_ONLY, false);
		bindTextToRectangle();
	}

	/**
	 * @return The color of this node's chapter.
	 */
	public Color getNodeChapterColor() {
	    return chapterTypes.get(nodeChapter).getChapterColor();
	}

	/**
	 * @return The x-position of this node.
	 */
	public  long getX()
	{
		return nodeRectangle.vx;
	}
	/**
	 * @return The y-position of this node.
	 */
	public long getY()
	{
		return nodeRectangle.vy;
	}
	/**
	 * Constructor
	 * @param nodeTitle The title of the node.
	 * @param nodeDescription The description of the node.
	 * @param nodeChapter The chapter the node is in.
	 * @param titleFontSize The font size for this node's title.
	 * @param descriptionFontSize The font size for this node's description.
	 */
	public Node(String nodeTitle, String nodeDescription, String nodeChapter,
	    int titleFontSize, int descriptionFontSize, float gradientAdjust)
	{
	    this(nodeTitle, nodeDescription, nodeChapter);
	    nodeRectangle = new VRoundRect(0,
				0, 1, 700, 200, chapterTypes.get(nodeChapter).getChapterColor(),
				Color.BLACK, 1f, 15, 15, gradientAdjust);
	    this.nodeTitle.setSpecialFont(  VText.getMainFont().
	        deriveFont(titleFontSize * 1.0f) );
	    this.nodeDescription.setSpecialFont(  VText.getMainFont().
	        deriveFont(descriptionFontSize * 1.0f) );
	    showView(ViewType.FULL_DESCRIPTION, false);
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
	 * @return Point at the center of the node
	 */
	public Point getCenterPoint() {
		return new Point((int)nodeRectangle.vx, (int)nodeRectangle.vy);
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
	 * @return nodeRectangle.height
	 */
	public long getHeight() {
		return nodeRectangle.getHeight();
	}

	/**
	 * @return List of linked nodes
	 */
	public List<Node> getLinkedNodes() {
		List<Node> linkedNodes = new ArrayList<Node>();
		for (Link link : nodeLinks) {
			if (link.contains(this)) {
				Node fromNode = link.getFromNode();
				Node linkedNode = fromNode.equals(this)
						? link.getToNode()
						: fromNode;
				linkedNodes.add(linkedNode);
			}
		}
		return linkedNodes;
	}

	/**
	 * @return the nodeChapter
	 */
	public String getNodeChapter() {
		return nodeChapter;
	}

	/**
	 * @return nodeDescription in string format
	 */
	public String getNodeDescription()
	{
		return nodeDescription.toString();
	}
	/**
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
	 * @return nodeTitle in string format
	 */
	public String getNodeTitle() {
		return nodeTitle.getText();
	}

	/**
	 * @return the node's translucency value
	 */
	private float getNodeTranslucency() {
		return (nodeRectangle.getTranslucencyValue()
				+ nodeTitle.getTranslucencyValue() + nodeDescription
				.getTranslucencyValue()) / 3;
	}

	/**
	 * @return nodeView
	 */
	public ViewType getViewType() {
		return nodeView;
	}

	/**
	 * @return nodeRectangle.width
	 */
	public long getWidth() {
		return nodeRectangle.getWidth();
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
	 *
	 */
	public void realignNodeText() {
		long titleWidth = nodeTitle.textContainerWidth;
		long titleHeight = nodeTitle.textContainerHeight;

		// Center the node description text
		switch (nodeView) {
			case FULL_DESCRIPTION :
				long descriptionWidth = nodeDescription.textContainerWidth;
				long descriptionHeight = nodeDescription.textContainerHeight;
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
	 * @param visibility
	 * 		True to show links, false otherwise.
	 */
	private void setLinksVisibility(boolean visibility) {
		for (Link link : getNodeLinks()) {
			Node fromNode = link.getFromNode();
			Node linkedNode = fromNode.equals(this)
					? link.getToNode()
					: fromNode;
			if (visibility == false
					|| linkedNode.getViewType() != ViewType.HIDDEN) {
				link.setVisible(visibility);
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
	 */
    public void unhighlightLinks() {
        for (Link link: getNodeLinks()) {
            link.unhighlight();
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
	 * @param animate
	 *            if the transition should be animated
	 */
	private void setNodeTranslucency(final long alpha, boolean animate) {
		if (animate)
		{
			setLinksVisibility(alpha > 0);
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
			setLinksVisibility(alpha > 0);
			nodeRectangle.setTranslucencyValue(alpha);
			nodeTitle.setTranslucencyValue(alpha);
			nodeDescription.setTranslucencyValue(alpha);
		}
	}

	/**
	 * Sets the node to show specific information.
	 *
	 * @param viewType
	 *            the type of information view for this node to display
	 * @param animate
	 *            whether the transition should be animated if possible
	 */
	public final void showView(ViewType viewType, boolean animate) {
		nodeView = viewType;
		boolean nodeDescriptionVisible = false;
		switch (viewType) {
			case FULL_DESCRIPTION :
				nodeDescriptionVisible = true;
			case TITLE_ONLY :
				if (getNodeTranslucency() == 0) {
					setNodeTranslucency(1, animate);
				}
				nodeRectangle.setVisible(true);
				nodeTitle.setVisible(true);
				nodeDescription.setVisible(nodeDescriptionVisible);
				break;
			case HIDDEN :
				setNodeTranslucency(0, animate);
				break;
			default :
		}
	}
	
	public void resizeFont( int newSize )
	{
		this.nodeTitle.setSpecialFont(nodeTitle.getFont()
				.deriveFont(newSize * 1.0f));
		this.nodeDescription.setSpecialFont(nodeDescription.getFont()
				.deriveFont(newSize * 1.0f));
	}
	
	public void resizeFont( int titleSize, int descriptionSize )
	{
		this.nodeTitle.setSpecialFont(nodeTitle.getFont()
				.deriveFont(titleSize * 1.0f));
		this.nodeDescription.setSpecialFont(nodeDescription.getFont()
				.deriveFont(descriptionSize * 1.0f));
	}
	
	public void printDebugInfo()
	{
		for(Link l: this.getNodeLinks())
		{
			System.out.println(l.getLinkType());
		}
	}
	
	public List<Node> getSubNodeList()
	{
		return subNodes;
	}
	
	/**
	 * Goes through all links and creates a list of subnodes
	 * that represent each type of link with 3 or more occurrences.
	 */
	public void initializeSubNodeList()
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
            subNodes = new ArrayList<Node>();
        
        // Create the special nodes
        for ( Entry<String, Integer> entry : linkOccurrences.entrySet() )
        {
        	String names = ". TOPICS: ";
            for ( Link l : getNodeLinks() )
            {
            	if ( l.getLinkType().equals(entry.getKey()) && 
            			!l.getToNode().getNodeChapter().equals(l.getFromNode().getNodeChapter()) ) {
            		names += " - ";
            		if ( l.getFromNode().getGlyph().equals(this.getGlyph()) )
                		names += l.getToNode().getNodeTitle().toUpperCase();
            		else
                		names += l.getFromNode().getNodeTitle().toUpperCase();
            	}
            }
        	Node newNode = new Node( entry.getValue() + " " + entry.getKey() 
        			+ " Nodes in Other Chapters", Link.getLinkProperty(entry.getKey())
        			.getDescription() + names, nodeChapter);
        	newNode.addToVirtualSpace(virtualSpace);
        	newNode.nodeRectangle.setColor(Color.white);
        	Node.link(this, newNode, entry.getKey(), 20, false);
        	newNode.showView(ViewType.HIDDEN, false);
        	subNodes.add(newNode);
        }  
	}
	
	public boolean hasSubNodes()
	{
		return subNodes != null;
	}

	/**
	 *  Defines a chapter's properties (color and description).
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
		
		public int getChapterXPos() {
			return fixedXPosition;
		}
		
		public int getChapterYPos() {
			return fixedYPosition;
		}
		
		public String getDefaultNode() {
			return defaultNode;
		}
		
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

}
