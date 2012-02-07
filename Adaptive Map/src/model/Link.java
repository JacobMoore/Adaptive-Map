package model;

import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import java.awt.Color;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.VSegment;

/**
 *
 * @author John Nein
 * @version Oct 24, 2011
 */
public class Link extends VSegment {

	public enum LinkLineType {
		BOLD, DASHED, STANDARD;
	}

	private static Map<String, LinkProperties> linkTypes;
	public static void addLinkType(String linkName,
			LinkProperties linkProperties) {
		if (linkTypes == null) {
			linkTypes = new HashMap<String, LinkProperties>();
		}
		linkTypes.put(linkName, linkProperties);
	}

	private static final int BOLD_WIDTH = 5;
	private static final int LINK_Z_INDEX = -1;

	private Node fromNode, toNode;
	private final String linkType;
	private VirtualSpace virtualSpace;
	private VText linkText;

	/**
	 * A new link object that links two nodes together.
	 *
	 * @param fromNode
	 *            the node that is being linked from
	 * @param toNode
	 *            the node that is being linked to
	 * @param linkType
	 *            the type of link connecting the two given nodes
	 */
	public Link(Node fromNode, Node toNode, String linkType) {
		// Create link glyph
	    //constructor for custom color
		/*super(fromNode.getCenterPoint().x, fromNode.getCenterPoint().y,
				LINK_Z_INDEX, linkTypes.get(linkType).getLinkColor(), toNode
						.getCenterPoint().x, toNode.getCenterPoint().y);*/
	    super(fromNode.getCenterPoint().x, fromNode.getCenterPoint().y,
            LINK_Z_INDEX, Color.lightGray, toNode
                    .getCenterPoint().x, toNode.getCenterPoint().y);
		switch (linkTypes.get(linkType).getLinkLineType()) {
			case BOLD :
				setStrokeWidth(BOLD_WIDTH);
				break;
			case DASHED :
				setDashed(true);
				break;
			case STANDARD :
			default :
				break;
		}

		this.fromNode = fromNode;
		this.toNode = toNode;
		this.linkType = linkType;
		if (fromNode.virtualSpace.equals(toNode.virtualSpace)) {
			this.virtualSpace = fromNode.virtualSpace;
		} else {
			throw new UnsupportedOperationException(
					"Cannot link nodes on different VirtualSpaces");
		}

		Point linkCenter = getLinkCenter();
		linkText = new VText(linkCenter.x, linkCenter.y, 0,
		    Color.black, linkType);
		virtualSpace.addGlyph(this);
		virtualSpace.addGlyph(linkText);
		linkText.setVisible(false);
		}

	/**
	 * Returns true if the node given is attached to this link.
	 *
	 * @param nodeToCheck
	 *            the node to be tested
	 * @return true if the node given is attached to this link.
	 */
	public boolean contains(Node nodeToCheck) {
		return nodeToCheck.equals(fromNode) || nodeToCheck.equals(toNode);
	}

	/**
	 * Refreshes this link's location by moving it's endpoints to the center of
	 * the nodes it is attached to.
	 */
	public void refresh() {
		Point fromCenterPoint = fromNode.getCenterPoint();
		Point toCenterPoint = toNode.getCenterPoint();
		setEndPoints(fromCenterPoint.x, fromCenterPoint.y, toCenterPoint.x,
				toCenterPoint.y);
		Point linkCenter = getLinkCenter();
		linkText.moveTo(linkCenter.x, linkCenter.y);
	}

	public Node getFromNode() {
		return fromNode;
	}

	public Node getToNode() {
		return toNode;
	}

	/**
	 * @return the nodeType
	 */
	public String getLinkType() {
		return linkType;
	}

	public void highlight() {
	    setColor(Color.black);
	    if (isVisible())
	        linkText.setVisible(true);
	    VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void unhighlight() {
        setColor(Color.lightGray);
        linkText.setVisible(false);
        VirtualSpaceManager.INSTANCE.repaintNow();
    }

    private Point getLinkCenter() {
        int centerX = (fromNode.getCenterPoint().x + toNode.getCenterPoint().x) / 2;
        int centerY = (fromNode.getCenterPoint().y + toNode.getCenterPoint().y) / 2;
        return new Point(centerX, centerY);
    }

	public static class LinkProperties {
		private LinkLineType linkLineType;
		private Color linkColor;
		private String description;
		private boolean highlighted;

		public LinkProperties(LinkLineType linkLineType, Color linkColor,
				String description) {
			this(linkLineType, linkColor);
			this.description = description;
		}

		public LinkProperties(LinkLineType linkLineType, Color linkColor) {
			this.linkLineType = linkLineType;
			this.linkColor = linkColor;
		}

		public LinkLineType getLinkLineType() {
			return linkLineType;
		}

		public Color getLinkColor() {
			return linkColor;
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
