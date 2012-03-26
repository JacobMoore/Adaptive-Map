package model;

import java.util.Collection;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
	public enum ViewType {
		FULL_DESCRIPTION, HIDDEN, TITLE_ONLY;
	}

	private static Map<String, ChapterProperties> chapterTypes;
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
	public static int xLocation = 0;

	public static int yLocation = 0;

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

	public static int generateNextXPosition() {
		return (xLocation += Configuration.GRID_COLUMN_WIDTH
				+ Configuration.GRID_BUFFER_SPACE)
				% ((Configuration.GRID_COLUMN_WIDTH + Configuration.GRID_BUFFER_SPACE) * 10);
	}

	public static int generateNextYPosition() {
		return -(Configuration.GRID_ROW_HEIGHT + Configuration.GRID_BUFFER_SPACE)
				* (xLocation / ((Configuration.GRID_COLUMN_WIDTH + Configuration.GRID_BUFFER_SPACE) * 10));
	}

	public static List<Node> getFirstLevelNodes(Node selectedNode) {
        // Find all of the nodes that will go in the grid
        List<Node> nodesToShow = new ArrayList<Node>();
        // Get the nodes one level out
        List<Link> selectedNodeLinks = selectedNode.getNodeLinks();
        for (Link firstNodeLink : selectedNodeLinks) {
            Node firstLevelNode = firstNodeLink.getFromNode() == selectedNode
                    ? firstNodeLink.getToNode()
                    : firstNodeLink.getFromNode();
            if (!firstLevelNode.getNodeChapter().equals(
                selectedNode.getNodeChapter()))
                nodesToShow.add(firstLevelNode);
        }
        return nodesToShow;
	}

	/**
	 * Returns the nodes that are 2 levels out from the given node, and where
	 * they go in the "grid" that surrounds the given node.
	 *
	 * @param selectedNode
	 *            the selected node that the grid is based around
	 * @return a map of nodes and their grid locations based on the given node
	 */
	public static void setGridLocations(Node selectedNode,
	    Collection<Point> nodeCoords) {
		List<Node> firstLevelNodes = new ArrayList<Node>();
		// Get the nodes one level out
		firstLevelNodes = getFirstLevelNodes(selectedNode);
		Point selectedCenter = selectedNode.getCenterPoint();
		Point chapterMax = new Point();
		Point chapterMin = new Point(selectedCenter);
		//find the bounds of the arranged chapter
		for (Point point: nodeCoords)
		{
		    if (point.x > chapterMax.x)
		        chapterMax.x = point.x;
		    if (point.y > chapterMax.y)
		        chapterMax.y = point.y;
		    if (point.x < chapterMin.x)
		        chapterMin.x = point.x;
		    if (point.y < chapterMin.y)
		        chapterMin.y = point.y;
		}
		int topX, bottomX, currX;
		boolean selectedNodeOnRight = selectedCenter.x >
		chapterMin.x + (chapterMax.x - chapterMin.x) / 2;
		int interval_x = (Configuration.GRID_COLUMN_WIDTH
        + Configuration.GRID_BUFFER_SPACE) / 2;
        int interval_y = (Configuration.GRID_ROW_HEIGHT
        + Configuration.GRID_BUFFER_SPACE) / 2;
        int depth = 0;
        //place Nodes in a gridlike manner around the chapter bounds.
        if (selectedNodeOnRight)
        {
            topX = bottomX = chapterMax.x;
            for (Node node: firstLevelNodes)
            {
                depth = findDepthBetween(selectedNode, node);
                if (depth > 0)
                {
                    topX += interval_x;
                    currX = topX;
                }
                else
                {
                    bottomX += interval_x;
                    currX = bottomX;
                }
                node.moveTo(currX, selectedCenter.y +
                    interval_y * depth);
            }
        }
        else
        {
            topX = bottomX = chapterMin.x;
            for (Node node: firstLevelNodes)
            {
                depth = findDepthBetween(selectedNode, node);
                if (depth > 0)
                {
                    topX -= interval_x;
                    currX = topX;
                }
                else
                {
                    bottomX -= interval_x;
                    currX = bottomX;
                }
                node.moveTo(currX, selectedCenter.y +
                    interval_y * depth);
            }
        }
	}

	public static void link(Node node1, Node node2, String linkType) {
		nodeLinks.add(new Link(node1, node2, linkType));
	}
	private String nodeChapter;
	private String nodeContentUrl;
	private NodeText nodeDescription;

	// Node graphics variables
	private VRoundRect nodeRectangle;
	// Node information variables
	private NodeText nodeTitle;

	private ViewType nodeView;

	protected VirtualSpace virtualSpace;

	public Node(String nodeTitle, String nodeDescription, String nodeChapter) {
		// Important to create this before assigning title/description
		nodeRectangle = new VRoundRect(generateNextXPosition(),
				generateNextYPosition(), 0, 0, 0, Configuration.NODE_BG_COLOR,
				chapterTypes.get(nodeChapter).getChapterColor(), 5, 5);
		nodeRectangle.setStroke( new BasicStroke( 2.0f ) );

		setNodeTitle(nodeTitle);
		setNodeDescription(nodeDescription);
		setNodeChapter(nodeChapter);

		showView(ViewType.TITLE_ONLY);
		bindTextToRectangle();
	}

	public Node(String nodeTitle, String nodeDescription, String nodeChapter,
	    int titleFontSize, int descriptionFontSize)
	{
	    this(nodeTitle, nodeDescription, nodeChapter);
	    this.nodeTitle.setSpecialFont(  VText.getMainFont().
	        deriveFont(titleFontSize * 1.0f) );
	    this.nodeDescription.setSpecialFont(  VText.getMainFont().
	        deriveFont(descriptionFontSize * 1.0f) );
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

	public Point getCenterPoint() {
		return new Point((int) (nodeRectangle.vx - getWidth() / 2),
				(int) (nodeRectangle.vy - getHeight() / 2));
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
	 * @return
	 */
	public long getHeight() {
		return nodeRectangle.getHeight();
	}

	/**
	 * @return
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
	 * @return the nodeContentLocation
	 */
	public String getNodeContentUrl() {
		return nodeContentUrl;
	}

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
	 * @return
	 */
	public String getNodeTitle() {
		return nodeTitle.getText();
	}

	/**
	 * @return
	 */
	private float getNodeTranslucency() {
		return (nodeRectangle.getTranslucencyValue()
				+ nodeTitle.getTranslucencyValue() + nodeDescription
				.getTranslucencyValue()) / 3;
	}

	/**
	 * @return
	 */
	public ViewType getViewType() {
		return nodeView;
	}

	/**
	 * @return
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
		refreshLinks();
	}

	/**
	 * Moves this Node to the given location.
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
		refreshLinks();
	}

	/**
	 *
	 * @param gridLocation
	 * @param centerPoint
	 */
	public void moveToGridLocation(GridLocation gridLocation, Point centerPoint) {
		// Get the position where 0,0 should be drawn
		Point startingPoint = new Point(centerPoint.x
				+ Configuration.GRID_COLUMN_WIDTH / 2, centerPoint.y);
		// Get the intervals to move for each grid location in x, y directions
		int interval_x = Configuration.GRID_COLUMN_WIDTH
				+ Configuration.GRID_BUFFER_SPACE;
		int interval_y = Configuration.GRID_ROW_HEIGHT
				+ Configuration.GRID_BUFFER_SPACE;
		this.moveTo(startingPoint.x
				+ interval_x
				* (gridLocation.getX() % 2 == 0
						? (gridLocation.getX() / -2)
						: ((gridLocation.getX() + 1) / 2)), startingPoint.y
				+ interval_y * gridLocation.getY());
		refreshLinks();
	}

	/**
	 * Aligns the full and short descriptions with the rectangle so both
	 * descriptions are within the bounds of the rectangle. Needed because the
	 * text and rectangle are not drawn in the same way
	 *
	 * @param width
	 *            the width to adjust by
	 * @param height
	 *            the height to adjust by
	 */
	public void realignNodeText() {
		long titleWidth = nodeTitle.textContainerWidth;
		long titleHeight = nodeTitle.textContainerHeight;

		// Center the node description text
		switch (nodeView) {
			case FULL_DESCRIPTION :
				long descriptionWidth = nodeDescription.textContainerWidth;
				long descriptionHeight = nodeDescription.textContainerHeight;
				long nodeAdjustedWidth = Math.max(titleWidth, descriptionWidth)
						+ NODE_PADDING;
				long nodeAdjustedHeight = titleHeight + descriptionHeight
						+ NODE_PADDING;
				// Set adjusted width/height
				nodeRectangle
						.setWidth(nodeAdjustedWidth / 2 + NODE_PADDING * 2);
				nodeRectangle.setHeight(nodeAdjustedHeight / 2 + NODE_PADDING
						* 2);
				nodeTitle.moveTo(nodeRectangle.vx - titleWidth / 2
						+ NODE_PADDING, nodeRectangle.vy
						+ nodeRectangle.getHeight() - titleHeight
						- NODE_PADDING);
				nodeDescription.moveTo(nodeRectangle.vx - descriptionWidth / 2
						+ NODE_PADDING, nodeTitle.vy - titleHeight
						- NODE_PADDING);
				break;
			case TITLE_ONLY :
				nodeAdjustedWidth = titleWidth + NODE_PADDING;
				nodeAdjustedHeight = titleHeight + NODE_PADDING;
				// Set adjusted width/height
				nodeRectangle
						.setWidth(nodeAdjustedWidth / 2 + NODE_PADDING * 2);
				nodeRectangle.setHeight(nodeAdjustedHeight / 2 + NODE_PADDING
						* 2);
				nodeTitle.moveTo(nodeRectangle.vx - titleWidth / 2
						+ NODE_PADDING, nodeRectangle.vy
						+ nodeRectangle.getHeight() - titleHeight
						- NODE_PADDING);
				refreshLinks();
				break;
			default :
				break;
		}
		// TODO hack for getting two line titles to work
		if (nodeTitle.getText().split(" ").length > VText.WORDSPERROW) {
			nodeTitle.move(0, 20);
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
	 * @param b
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
	 * Highlight all links connected to this node
	 */
	public void highlightLinks() {
        for (Link link : getNodeLinks()) {
            link.highlight();
        }
    }

	/**
	 * Unhlighlight all links connected to this node
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

	public void setNodeDescription(String nodeDescription) {
		if (this.nodeDescription != null) {
			virtualSpace.removeGlyph(this.nodeDescription);
		}
		this.nodeDescription = new NodeText(nodeRectangle.vx, nodeRectangle.vy,
				0, Configuration.NODE_DESCRIPTION_COLOR, nodeDescription);
	}

	public void setNodeTitle(String nodeTitle) {
		if (this.nodeTitle != null && virtualSpace != null) {
			virtualSpace.removeGlyph(this.nodeTitle);
		}
		this.nodeTitle = new NodeText(nodeRectangle.vx, nodeRectangle.vy, 0,
				Configuration.NODE_TITLE_COLOR, nodeTitle);
	}

	/**
	 * Sets the translucency of this node to the given alpha. This will animate
	 * the transition between the current translucency and the new one.
	 *
	 * @param alpha
	 *            the alpha of the translucency for the node
	 */
	private void setNodeTranslucency(final long alpha) {
		Animation nodeTransparancy = VirtualSpaceManager.INSTANCE
				.getAnimationManager().getAnimationFactory()
				.createTranslucencyAnim(1000, new Translucent() {
					@Override
					public float getTranslucencyValue() {
						return Node.this.getNodeTranslucency();
					}
					@Override
					public void setTranslucencyValue(float alpha) {
						nodeRectangle.setTranslucencyValue(alpha);
						nodeTitle.setTranslucencyValue(alpha);
						nodeDescription.setTranslucencyValue(alpha);
					}
				}, alpha, false, IdentityInterpolator.getInstance(),
						new EndAction() {
							@Override
							public void execute(Object subject,
									Dimension dimension) {
								setLinksVisibility(alpha > 0);
							}
						});
		VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(
				nodeTransparancy, true);
	}

	/**
	 * Sets the node to show specific information.
	 *
	 * @param viewType
	 *            the type of information view for this node to display
	 */
	public final void showView(ViewType viewType) {
		nodeView = viewType;
		boolean nodeDescriptionVisible = false;
		switch (viewType) {
			case FULL_DESCRIPTION :
				nodeDescriptionVisible = true;
			case TITLE_ONLY :
				if (getNodeTranslucency() == 0) {
					setNodeTranslucency(1);
				}
				nodeRectangle.setVisible(true);
				nodeTitle.setVisible(true);
				nodeDescription.setVisible(nodeDescriptionVisible);
				break;
			case HIDDEN :
				setNodeTranslucency(0);
				break;
			default :
		}
	}

	public static class ChapterProperties {
		private Color chapterColor;
		private String description;

		public ChapterProperties(Color chapterColor, String description) {
			this.chapterColor = chapterColor;
			this.description = description;
		}

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
