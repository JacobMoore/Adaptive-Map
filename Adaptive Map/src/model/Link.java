package model;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import java.awt.Color;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.VSegment;

/**
 * @author John Nein
 * @version Oct 24, 2011
 */
public class Link
    extends VSegment
{

    /**
     * Represents the types of links.
     */
    public enum LinkLineType
    {
        BOLD,
        DASHED,
        STANDARD;
    }

    private static Map<String, LinkProperties> linkTypes;


    /**
     * Adds a link to the map.
     * @param linkName
     *            The name of the link to add.
     * @param linkProperties
     *            The properties of the link to add.
     */
    public static void addLinkType(
        String linkName,
        LinkProperties linkProperties)
    {
        if (linkTypes == null)
        {
            linkTypes = new HashMap<String, LinkProperties>();
        }
        linkTypes.put(linkName, linkProperties);
    }

    private static final int BOLD_WIDTH   = 5;
    private static final int LINK_Z_INDEX = -1;
    private static final int LETTER_SIZE  = 10;

    private Node             fromNode, toNode;
    private final String     linkType;
    private VirtualSpace     virtualSpace;
    private VText            linkText;
    private int              linkWeight;
    private EndTriangle      endTriangle;


    /**
     * A new link object that links two nodes together.
     * @param fromNode
     *            the node that is being linked from
     * @param toNode
     *            the node that is being linked to
     * @param linkType
     *            the type of link connecting the two given nodes
     */
    public Link(Node fromNode, Node toNode, String linkType)
    {
        // Create link glyph
        // constructor for custom color
        /*
         * super(fromNode.getCenterPoint().x, fromNode.getCenterPoint().y,
         * LINK_Z_INDEX, linkTypes.get(linkType).getLinkColor(), toNode
         * .getCenterPoint().x, toNode.getCenterPoint().y);
         */
        super(
            fromNode.getCenterPoint().x,
            fromNode.getCenterPoint().y,
            LINK_Z_INDEX,
            Color.lightGray,
            toNode.getCenterPoint().x,
            toNode.getCenterPoint().y);
        // switch for custom line types
        /**
         * switch (linkTypes.get(linkType).getLinkLineType()) { case BOLD :
         * setStrokeWidth(BOLD_WIDTH); break; case DASHED : setDashed(true);
         * break; case STANDARD : default : break; }
         */

        this.fromNode = fromNode;
        this.toNode = toNode;
        this.linkType = linkType;
        if (fromNode.virtualSpace.equals(toNode.virtualSpace))
        {
            this.virtualSpace = fromNode.virtualSpace;
        }
        else
        {
            throw new UnsupportedOperationException(
                "Cannot link nodes on different VirtualSpaces");
        }

        Point linkCenter = getLinkCenter();
        linkText =
            new VText(linkCenter.x, linkCenter.y, 0, Color.red, linkType);
        virtualSpace.addGlyph(this);
        virtualSpace.addGlyph(linkText);
        linkText.setVisible(false);
        linkWeight = 1;

        // Adding end-of-link triangle
        endTriangle =
            new EndTriangle(
                createTrianglePoints(fromNode, toNode),
                0,
                this.color);
        virtualSpace.addGlyph(endTriangle);
        endTriangle.setVisible(this.isVisible());
    }


    @Override
    public void setVisible(boolean b)
    {
        super.setVisible(b);
        endTriangle.setVisible(b);
    }


    /**
     * Returns true if the node given is attached to this link.
     * @param nodeToCheck
     *            the node to be tested
     * @return true if the node given is attached to this link.
     */
    public boolean contains(Node nodeToCheck)
    {
        return nodeToCheck.equals(fromNode) || nodeToCheck.equals(toNode);
    }


    /**
     * Refreshes this link's location by moving it's endpoints to the center of
     * the nodes it is attached to.
     */
    public void refresh()
    {
        Point fromCenterPoint = fromNode.getCenterPoint();
        Point toCenterPoint = toNode.getCenterPoint();
        setEndPoints(
            fromCenterPoint.x,
            fromCenterPoint.y,
            toCenterPoint.x,
            toCenterPoint.y);
        Point linkCenter = getLinkCenter();
        linkText.moveTo(linkCenter.x, linkCenter.y);

        // Remove, then re-add triangle, as it will have moved
        virtualSpace.removeGlyph(endTriangle);
        endTriangle =
            new EndTriangle(
                createTrianglePoints(fromNode, toNode),
                0,
                this.color);
        virtualSpace.addGlyph(endTriangle);
        endTriangle.setVisible(this.isVisible());
    }


    /**
     * Creates an array of vertices for a link-ending triangle based on the node
     * to which the end of the link is pointing.
     */
    private LongPoint[] createTrianglePoints(Node startNode, Node endNode)
    {
        int arrowHeight = 20;
        int arrowWidth = 10;
        LongPoint[] vertices = new LongPoint[3];
        Point centerOfEndNode = endNode.getCenterPoint();
        Point centerOfStartNode = startNode.getCenterPoint();
        Point linkCenter = getLinkCenter();
        int nodeWidth = (int)endNode.getWidth();
        int nodeHeight = (int)endNode.getHeight();

        int startX = centerOfStartNode.x;
        int startY = centerOfStartNode.y;
        int endX = centerOfEndNode.x;
        int endY = centerOfEndNode.y;

        int diffX = endX - startX;
        int diffY = endY - startY;

        if (diffX == 0)
        {
            if (diffY > 0)
            {
                vertices[0] = new LongPoint(endX, endY - nodeHeight / 2);
                vertices[1] =
                    new LongPoint(endX - arrowWidth, endY - nodeHeight / 2
                        - arrowHeight);
                vertices[2] =
                    new LongPoint(endX + arrowWidth, endY - nodeHeight / 2
                        - arrowHeight);
                return vertices;
            }
            else
            {
                vertices[0] = new LongPoint(endX, endY + nodeHeight / 2);
                vertices[1] =
                    new LongPoint(endX - arrowWidth, endY + nodeHeight / 2
                        + arrowHeight);
                vertices[2] =
                    new LongPoint(endX + arrowWidth, endY + nodeHeight / 2
                        + arrowHeight);
                return vertices;
            }
        }
        else if (diffY == 0)
        {
            if (diffX > 0)
            {
                vertices[0] = new LongPoint(endX - nodeWidth / 2, endY);
                vertices[1] =
                    new LongPoint(endX - nodeWidth / 2 - arrowHeight, endY
                        + arrowWidth);
                vertices[2] =
                    new LongPoint(endX - nodeWidth / 2 - arrowHeight, endY
                        - arrowWidth);
                return vertices;
            }
            else
            {
                vertices[0] = new LongPoint(endX + nodeWidth / 2, endY);
                vertices[1] =
                    new LongPoint(endX + nodeWidth / 2 + arrowHeight, endY
                        + arrowWidth);
                vertices[2] =
                    new LongPoint(endX + nodeWidth / 2 + arrowHeight, endY
                        - arrowWidth);
                return vertices;
            }
        }
        else
        {

            Point intersectionPoint;

            // Top or bottom
            if ((intersectionPoint =
                intersection(endX, endY, linkCenter.x, linkCenter.y, endX
                    - nodeWidth, endY + nodeHeight, endX + nodeWidth, endY
                    + nodeHeight)) != null)
            {
                if (linkCenter.y > endY)
                {
                    endX = intersectionPoint.x;
                    endY = intersectionPoint.y;
                }
                else
                {
                    intersectionPoint =
                        intersection(
                            endX,
                            endY,
                            linkCenter.x,
                            linkCenter.y,
                            endX - nodeWidth,
                            endY - nodeHeight,
                            endX + nodeWidth,
                            endY - nodeHeight);
                    endX = intersectionPoint.x;
                    endY = intersectionPoint.y;
                }
            }
            // Left or right
            else if ((intersectionPoint =
                intersection(endX, endY, linkCenter.x, linkCenter.y, endX
                    - nodeWidth, endY - nodeHeight, endX - nodeWidth, endY
                    + nodeHeight)) != null)
            {
                if (linkCenter.x < endX)
                {
                    endX = intersectionPoint.x;
                    endY = intersectionPoint.y;
                }
                else
                {
                    intersectionPoint =
                        intersection(
                            endX,
                            endY,
                            linkCenter.x,
                            linkCenter.y,
                            endX + nodeWidth,
                            endY - nodeHeight,
                            endX + nodeWidth,
                            endY + nodeHeight);
                    endX = intersectionPoint.x;
                    endY = intersectionPoint.y;
                }
            }

            double distance = distance(endX, endY, linkCenter.x, linkCenter.y);
            int xDist = endX - linkCenter.x;
            int yDist = endY - linkCenter.y;
            double newX1 =
                endX - arrowHeight * (xDist / distance) - arrowWidth
                    * (yDist / distance);
            double newY1 =
                endY - arrowHeight * (yDist / distance) + arrowWidth
                    * (xDist / distance);
            double newX2 =
                endX - arrowHeight * (xDist / distance) + arrowWidth
                    * (yDist / distance);
            double newY2 =
                endY - arrowHeight * (yDist / distance) - arrowWidth
                    * (xDist / distance);

            LongPoint nodePoint = new LongPoint(endX, endY);
            vertices[0] = nodePoint;
            vertices[1] = new LongPoint(newX1, newY1);
            vertices[2] = new LongPoint(newX2, newY2);
            return vertices;
        }
    }


    /**
     * Finds the intersection point of two line segments (X1,Y1) to (X2,Y2) and
     * (X3,Y3) to (X4,Y4).
     * @param x1
     *            First x.
     * @param y1
     *            First y.
     * @param x2
     *            Second x.
     * @param y2
     *            Second y.
     * @param x3
     *            Third x.
     * @param y3
     *            Third y.
     * @param x4
     *            Fourth x.
     * @param y4
     *            Fourth y.
     * @return The intersection point.
     */
    private Point intersection(
        int x1,
        int y1,
        int x2,
        int y2,
        int x3,
        int y3,
        int x4,
        int y4)
    {
        int d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (d == 0)
            return null;

        int xi =
            ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4))
                / d;
        int yi =
            ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4))
                / d;

        return new Point(xi, yi);
    }


    /**
     * Finds the distance between two points (X1,Y1) and (X2,Y2).
     * @param x1
     *            First x.
     * @param y1
     *            First y.
     * @param x2
     *            Second x.
     * @param y2
     *            Second y.
     * @return The distance between the points.
     */
    private static double distance(double x1, double y1, double x2, double y2)
    {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }


    /**
     * @return The node this link is pointing from.
     */
    public Node getFromNode()
    {
        return fromNode;
    }


    /**
     * @return The node this link is pointing to.
     */
    public Node getToNode()
    {
        return toNode;
    }


    /**
     * @return the nodeType
     */
    public String getLinkType()
    {
        return linkType;
    }


    /**
     * Highlights this link and its arrow, and shows its text.
     */
    public void highlight()
    {
        setColor(Color.black);
        endTriangle.setColor(Color.lightGray);
        if (isVisible())
            linkText.setVisible(true);
        VirtualSpaceManager.INSTANCE.repaintNow();
    }


    /**
     * Unhighlights this link and its arrow, and hides its text.
     */
    public void unhighlight()
    {
        setColor(Color.lightGray);
        endTriangle.setColor(Color.lightGray);
        linkText.setVisible(false);
        VirtualSpaceManager.INSTANCE.repaintNow();
    }


    /**
     * @return The center of this link.
     */
    private Point getLinkCenter()
    {
        int centerX =
            (fromNode.getCenterPoint().x + toNode.getCenterPoint().x) / 2;
        int centerY =
            (fromNode.getCenterPoint().y + toNode.getCenterPoint().y) / 2;
        // move the text so that its center is on the center of the link
        centerX -= LETTER_SIZE;
        centerY -= (LETTER_SIZE / 2);
        return new Point(centerX, centerY);
    }


    /**
     * @return The weight of this link.
     */
    public int getWeight()
    {
        return linkWeight;
    }


    /**
     * @param newWeight The new weight for this link.
     */
    public void setWeight(int newWeight)
    {
        linkWeight = newWeight;
        this.stroke = new java.awt.BasicStroke(linkWeight);
    }


    /**
     *  Represents all the properties of a link.
     */
    public static class LinkProperties
    {
        private LinkLineType linkLineType;
        private Color        linkColor;
        private String       description;
        private boolean      highlighted;


        /**
         * Create a new LinkProperties object.
         * @param linkLineType The type of link.
         * @param linkColor The color of the link.
         * @param description The description for this link.
         */
        public LinkProperties(
            LinkLineType linkLineType,
            Color linkColor,
            String description)
        {
            this(linkLineType, linkColor);
            this.description = description;
        }


        /**
         * Create a new LinkProperties object.
         * @param linkLineType The type of link.
         * @param linkColor The color of the the link.
         */
        public LinkProperties(LinkLineType linkLineType, Color linkColor)
        {
            this.linkLineType = linkLineType;
            this.linkColor = linkColor;
        }


        /**
         * @return The type of link.
         */
        public LinkLineType getLinkLineType()
        {
            return linkLineType;
        }


        /**
         * @return The color of this link.
         */
        public Color getLinkColor()
        {
            return linkColor;
        }


        /**
         * @param description
         *            the description to set
         */
        public void setDescription(String description)
        {
            this.description = description;
        }


        /**
         * @return the description
         */
        public String getDescription()
        {
            return description;
        }
    }


    /**
     * Represents a triangle at the end of a link.
     * @author Joe Luke
     * @version 2012.02.20
     */
    private static class EndTriangle
        extends VPolygon
    {
        /**
         * Create a new EndTriangle object with the given parameters.
         * @param v
         *            The points for the triangle.
         * @param z
         *            The depth value for the triangle.
         * @param c
         *            The color for the triangle.
         */
        public EndTriangle(LongPoint[] v, int z, Color c)
        {
            super(v, z, c);
        }


        @Override
        public void setColor(Color color)
        {
            this.color = color;
            this.setBorderColor(color);
        }
    }
}
