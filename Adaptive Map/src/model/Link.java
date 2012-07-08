package model;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import view.AppCanvas;

import controller.Configuration;
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
    @SuppressWarnings("javadoc")
    public enum LinkLineType
    {
		BOLD,
        DASHED,
        STANDARD;
    }

    private static Map<String, LinkProperties> linkTypes;
    
    
    /**
     * @param type Type of link
     * @return the link properties object for that type
     */
    public static LinkProperties getLinkProperty(String type)
    {
    	return linkTypes.get(type);
    }
    
    /**
     * @return All link types
     */
    public static ArrayList<String> getLinkTypes()
    {
    	ArrayList<String> list = new ArrayList<String>();
    	list.addAll(linkTypes.keySet());
    	return list;
    }

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

    private static final int LINK_Z_INDEX = -1;

    private Node             fromNode, toNode;
    private final String     linkType;
    private VirtualSpace     virtualSpace;
    private VText            linkText;
    private int              linkWeight;
    private int				 arrowSize;
    private EndTriangle      endTriangle;
    private boolean			 highlighted;
    
    // Number of next link color, used to give all new links different colors
    private static int 		 colorNum = 0;
    private Color			 linkColor;

    /**
     * A new link object that links two nodes together.
     * @param fromNode
     *            the node that is being linked from
     * @param toNode
     *            the node that is being linked to
     * @param linkType
     *            the type of link connecting the two given nodes
     * @param aSize 
     * 			  the size of the arrowhead
     */
    public Link(Node fromNode, Node toNode, String linkType, int aSize)
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
        
        arrowSize = aSize;

        this.fromNode = fromNode;
        this.toNode = toNode;
        this.linkType = linkType;
        if (AppCanvas.appletContext == null)
        {
        	return;
        }
        else if (fromNode.virtualSpace.equals(toNode.virtualSpace))
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
        linkText.setSpecialFont(new Font("Arial", Font.BOLD, Configuration.LINK_FONT_SIZE));
        virtualSpace.addGlyph(this);
        virtualSpace.addGlyph(linkText);
        linkText.setVisible(false);
        virtualSpace.onTop(linkText);
        linkWeight = 1;
        
        switch( colorNum )
        {
        case 0:
        	linkColor = new Color(218, 213, 213);
        	this.setColor(new Color(218, 213, 213));
        	colorNum++;
        	break;
        case 1:
        	linkColor = new Color(195, 191, 191);
        	this.setColor(new Color(195, 191, 191));
        	colorNum++;
        	break;
        case 2:
        	linkColor = new Color(169, 167, 167);
        	this.setColor(new Color(169, 167, 167));
        	colorNum++;
        	break;
        case 3:
        	linkColor = new Color(139, 137, 137);
        	this.setColor(new Color(139, 137, 137));
        	colorNum = 0;
        	break;
        }

        // Adding end-of-link triangle
        endTriangle =
            new EndTriangle(
                createTrianglePoints(fromNode, toNode),
                0,
                linkColor);
        virtualSpace.addGlyph(endTriangle);
        endTriangle.setVisible(this.isVisible());
        highlighted = false;
    }
    
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
    	super.draw(g, vW, vH, i, stdS, stdT, dx, dy);

    	// Adjust link widths if this is an overview link
    	if ( linkType.equals("STANDARD") )
    	{
        	float altitude = VirtualSpaceManager.INSTANCE.getActiveCamera().getAltitude();
	    	float minHeight = Configuration.ZOOM_CHAPTER_HEIGHT;
	    	float maxHeight = Configuration.ZOOM_OVERVIEW_MAX;
	    	float adjustedWeight = linkWeight / Math.abs((altitude - minHeight) / (maxHeight-minHeight));
	    	if ( adjustedWeight > Configuration.MAX_LINK_WIDTH)
	    		adjustedWeight = Configuration.MAX_LINK_WIDTH;
	    	setStrokeWidth(adjustedWeight);
			
			endTriangle.sizeTo(arrowSize+10*linkWeight);
    	}
    }

    @Override
    public void setVisible(boolean b)
    {
        super.setVisible(b);
        endTriangle.setVisible(b);
    }
    
    /**
     * setVisible with option to set link transparency
     * @param b Visible or not
     * @param alpha Transparency value
     */
    public void setVisible(boolean b, float alpha)
    {
        super.setVisible(b);
        setTranslucencyValue(alpha);
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
        // Center the text on the link
        linkText.moveTo(linkCenter.x - Configuration.LINK_FONT_SIZE, linkCenter.y);
        virtualSpace.onTop(linkText);

        // Remove, then re-add triangle, as it will have moved
        virtualSpace.removeGlyph(endTriangle);
        endTriangle =
            new EndTriangle(
                createTrianglePoints(fromNode, toNode),
                0,
                linkColor);
        virtualSpace.addGlyph(endTriangle);
        endTriangle.setVisible(this.isVisible());

        if (highlighted)
        {
	        setColor(Color.black);
	        endTriangle.setColor(Color.black);
	        endTriangle.setBorderColor(Color.yellow.brighter());
        }
        else
        {	        
        	setColor(linkColor);
        	endTriangle.setColor(linkColor);
        	endTriangle.setBorderColor(Color.black);
        }
    }


    /**
     * Creates an array of vertices for a link-ending triangle based on the node
     * to which the end of the link is pointing.
     * @param startNode The node the link originates from
     * @param endNode The node the link goes to
     * @return Array of points representing each vertex of the triangle
     */
    private LongPoint[] createTrianglePoints(Node startNode, Node endNode)
    {
        int arrowHeight = arrowSize;
        int arrowWidth = arrowSize/2;
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
        	//color = Color.orange;
            if (diffY > 0)
            {
            	endY -= endNode.getHeight()/2;
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
            	endY += endNode.getHeight()/2;
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
        	//color = Color.orange;
            if (diffX > 0)
            {
            	endX -= endNode.getWidth()/2;
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
            	endX += endNode.getWidth()/2;
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

            // Left
            if ((intersectionPoint =
                intersection(endX, endY, startX, startY,
                		endX - nodeWidth, endY - nodeHeight, 
                		endX - nodeWidth, endY + nodeHeight)) != null)
            {
                	//color = Color.green;
                    endX = intersectionPoint.x;
                    endY = intersectionPoint.y;

            }
            // Right
            else if ((intersectionPoint =
                    intersection(
                        endX, endY, startX, startY,
                        endX + nodeWidth, endY - nodeHeight,
                        endX + nodeWidth, endY + nodeHeight)) != null)
            {

            	//color = Color.yellow;
                endX = intersectionPoint.x;
                endY = intersectionPoint.y;
            }
            // Bottom
            else if ((intersectionPoint =
                intersection(endX, endY, startX, startY, 
                		endX - nodeWidth, endY + nodeHeight, 
                		endX + nodeWidth, endY + nodeHeight)) != null)
            {
                	//color = Color.red;
                    endX = intersectionPoint.x;
                    endY = intersectionPoint.y;
            }
            // Top
            else if(
                (intersectionPoint =
                    intersection(
                        endX, endY, startX, startY,
                        endX - nodeWidth, endY - nodeHeight,
                        endX + nodeWidth, endY - nodeHeight)) != null)
            {
            	//color = Color.blue;
                endX = intersectionPoint.x;
                endY = intersectionPoint.y;
            }
            /*else
            {
            	System.out.println("No valid link intersection found.");
            	System.out.println(startNode.getNodeTitle() + " to " + 
            			endNode.getNodeTitle() + ".");
            }*/

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

            vertices[0] = new LongPoint(endX, endY);
            vertices[1] = new LongPoint(newX1, newY1);
            vertices[2] = new LongPoint(newX2, newY2);
            return vertices;
        }
    }


    /**
     * Computes the intersection between two segments.
     * This is based off an explanation and expanded math presented by Paul Bourke,
     * and corresponding C# code by Olaf Rabbachin.
     * See http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline2d/
     * 
     * @param x1 Starting point of Segment 1
     * @param y1 Starting point of Segment 1
     * @param x2 Ending point of Segment 1
     * @param y2 Ending point of Segment 1
     * @param x3 Starting point of Segment 2
     * @param y3 Starting point of Segment 2
     * @param x4 Ending point of Segment 2
     * @param y4 Ending point of Segment 2
     * @return Point where the segments intersect, or null if they don't
     */
    public Point intersection(
    	int x1,int y1,int x2,int y2, 
        int x3, int y3, int x4,int y4
     ) {
    	
           // Denominator for ua and ub are the same, so store this calculation
           double d =
              (y4 - y3) * (x2 - x1)
              -
              (x4 - x3) * (y2 - y1);

           //n_a and n_b are calculated as seperate values for readability
           double n_a =
              (x4 - x3) * (y1 - y3)
              -
              (y4 - y3) * (x1 - x3);

           double n_b =
              (x2 - x1) * (y1 - y3)
              -
              (y2 - y1) * (x1 - x3);

           // Make sure there is not a division by zero - this also indicates that
           // the lines are parallel.  
           // If n_a and n_b were both equal to zero the lines would be on top of each 
           // other (coincidental).  This check is not done because it is not 
           // necessary for this implementation (the parallel check accounts for this).
           if (d == 0)
              return null;

           // Calculate the intermediate fractional point that the lines potentially intersect.
           double ua = n_a / d;
           double ub = n_b / d;

           // The fractional point will be between 0 and 1 inclusive if the lines
           // intersect.  If the fractional calculation is larger than 1 or smaller
           // than 0 the lines would need to be longer to intersect.
           if (ua >= 0d && ua <= 1d && ub >= 0d && ub <= 1d) {
        	  Point p = new Point((int)(x1 + (ua * (x2 - x1))), (int)(y1 + (ua * (y2 - y1))));
              return p;
           }
           return null;
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
     * @param showText Whether the text should be shown.
     */
    public void highlight(boolean showText)
    {
    	this.setTranslucencyValue(1.0f);
        highlighted = true;
        if (isVisible() && showText)
            linkText.setVisible(true);
        VirtualSpaceManager.INSTANCE.repaintNow();
    }


    /**
     * Unhighlights this link and its arrow, and hides its text.
     */
    public void unhighlight()
    {
    	highlighted = false;
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
        //this.setStrokeWidth(linkWeight);
    }


    /**
     *  Represents all the properties of a link.
	 *  !NOTE!: This code is a duplicate of AdaptiveMapGUI's LinkProperties!
	 *  Duplicate any changes across projects to maintain consistency!
     */
    public static class LinkProperties
    {
        private Color        linkColor;
        private LinkLineType linkLineType;
        private String       description;

        /**
         * Create a new LinkProperties object.
         * @param linkLineType 
         * @param linkColor The color of the link.
         * @param description The description for this link.
         */
        public LinkProperties(
            LinkLineType linkLineType, Color linkColor,
            String description)
        {
            this(linkColor);
            this.linkLineType = linkLineType;
            this.description = description;
        }


        /**
         * Create a new LinkProperties object.
         * @param linkColor The color of the the link.
         */
        public LinkProperties( Color linkColor)
        {
            this.linkColor = linkColor;
        }


        /**
         * @return The color of this link.
         */
        public Color getLinkColor()
        {
            return linkColor;
        }
        
        /**
         * @param type The line type of this link
         */
        public void setLinkLineType(LinkLineType type)
        {
        	linkLineType = type;
        }
        
        /**
         * @return The line type of this link
         */
        public LinkLineType getLinkLineType()
        {
        	return linkLineType;
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
