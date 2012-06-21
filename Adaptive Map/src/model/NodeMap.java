package model;

import controller.Configuration;
import HierarchialLayout.Graph;
import HierarchialLayout.Edge;
import java.util.List;
import java.awt.Point;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * // -------------------------------------------------------------------------
/**
 *  A class to store nodes keyed by their chapters.
 *
 *  @author Joshua Rush
 *  @version Mar 9, 2012
 */
public class NodeMap
{
    private final static double CHAPTER_SCALE = 2.0;
    private final static double NODE_SCALE = 0.5;
    private HashMap<String, ArrayList<Node>> nodeMap;
    /**
     * Create a new NodeMap
     */
    public NodeMap()
    {
        nodeMap = new HashMap<String, ArrayList<Node>>();
    }
    /**
     * Add a node to the map with the chapter as its key
     * @param chapter The chapter for this node.
     * @param node The node to store.
     */
    public void addNode(String chapter, Node node)
    {
        if (nodeMap.containsKey(chapter))
        {
            nodeMap.get(chapter).add(node);
        }
        else
        {
            ArrayList<Node> list = new ArrayList<Node>();
            list.add(node);
            nodeMap.put(chapter, list);
        }
    }
    /**
     * Returns a list of all the nodes within this map
     * @return a list of all the nodes
     */
    public ArrayList<Node> getNodes()
    {
        ArrayList<Node> ret = new ArrayList<Node>();
        for(ArrayList<Node> nodes: nodeMap.values())
        {
            for(Node node: nodes)
            {
                ret.add(node);
            }
        }
        return ret;
    }

    /**
     * @return The coordinates for all nodes.
     */
    public ArrayList<Point> getNodeCoords()
    {
        ArrayList<Point> points = new ArrayList<Point>();
        for (Node node: getNodes())
        {
            points.add(node.getCenterPoint());
        }
        return points;
    }

    /**
     * @return The list of all chapter names.
     */
    public ArrayList<String> getChapters()
    {
        return new ArrayList<String>(nodeMap.keySet());
    }

    /**
     * Get a list of nodes that share the same chapter.
     * @param chapter the chapter to get the list of nodes from
     * @return a list of nodes from the specified chapter
     */
    public ArrayList<Node> getChapterNodes(String chapter)
    {
        if (nodeMap.containsKey(chapter))
            return nodeMap.get(chapter);
        else
            return new ArrayList<Node>();
    }

    /**
     * Create an edge array to determine the coordinates of the chapters.
     */
    private static Edge[] generateEdgeArrayFromChapters(ArrayList<String> chapters,
        NodeMap nodeMap)
    {
        ArrayList<String> usedChapters = new ArrayList<String>();
        ArrayList<Edge> edges = new ArrayList<Edge>();
        //for each link in each node in each chapter, check if it links to
        //another chapter
        for (String chapter: chapters)
        {
            ArrayList<Node> nodes = nodeMap.getChapterNodes(chapter);
            for (Node node: nodes)
            {
                for (Link link: node.getNodeLinks())
                {
                    String linkChapter = link.getToNode().getNodeChapter();
                    if (!linkChapter.equals(chapter) &&
                        !usedChapters.contains(chapter))
                    {
                        int indexFrom = chapters.indexOf(chapter);
                        int indexTo = chapters.indexOf(linkChapter);
                        //reverse to and from for a better layout
                        edges.add(new Edge(indexTo, indexFrom));
                        usedChapters.add(chapter);
                    }
                }
            }
        }
        return edges.toArray(new Edge[0]);
    }

    /**
     * Given a list of nodes, creates an array of edges graph based on their links.
     * @param nodes the list of nodes to create the edge array for
     */
    private static Edge[] generateEdgeArrayFromNodes(ArrayList<Node> nodes)
    {
        ArrayList<Edge> edges = new ArrayList<Edge>();
        //add an edge for each link within this chapter
        for(Node node: nodes)
        {
            int indexFrom = nodes.indexOf(node);
            List<Link> links = node.getNodeLinks();
            for(Link link: links)
            {
                Node fromNode = link.getFromNode();
                Node toNode = link.getToNode();
                if(fromNode.equals(node) &&
                    nodes.contains(toNode))
                {
                    int indexTo = nodes.indexOf(toNode);
                    //reverse to and from for a better layout
                    edges.add(new Edge(indexTo, indexFrom));
                }
            }
        }
        return edges.toArray(new Edge[0]);
    }

    /**
     * Read an edge array and set the nodes to the correct coordinates
     * @param chapterNodes the list of nodes to set
     * @param centerNode the node to center the list on
     * @param firstLevelNodes The nodes one level out from the center node.
     * @return the new node coordinates
     */
    public static Map<Integer, Point> setNodeCoords(ArrayList<Node> chapterNodes,
        Node centerNode, List<Node> firstLevelNodes)
    {	
        Map<Integer, Point> coordMap = new HashMap<Integer, Point>();
        Edge[] edges = generateEdgeArrayFromNodes(chapterNodes);

        Graph graph = new Graph(chapterNodes.size(), edges);
        int centerIndex = chapterNodes.indexOf(centerNode);
        Point[] points = getCoords(graph, NODE_SCALE, firstLevelNodes, 
        		centerIndex, centerNode);

        //center the points on the center node
        int xDifference = centerNode.getCenterPoint().x - points[centerIndex].x;
        int yDifference = centerNode.getCenterPoint().y - points[centerIndex].y;
        for (int i = 0; i < points.length; i++)
        {
            Point point = new Point(points[i].x + xDifference,
                points[i].y + yDifference);
            coordMap.put(i, point);
        }

        return coordMap;
    }

    /**
     * Get a map of chapters to coordinates.
     * @param nodeMap The node map of chapters to nodes.
     * @return The coordinates of each chapter.
     */
    public static Map<String, Point> getChapterCoords(NodeMap nodeMap)
    {
        Map<String, Point> coordMap = new HashMap<String, Point>();
        ArrayList<String> chapters = new ArrayList<String>(nodeMap.getChapters());
        Edge[] edges = generateEdgeArrayFromChapters(chapters, nodeMap);
        Graph graph = new Graph(chapters.size(), edges);
        Point[] coords = getCoords(graph, CHAPTER_SCALE);
        
        for (int i = 0; i < chapters.size(); i++)
        {
            String chapter = chapters.get(i);
            coordMap.put(chapter, coords[i]);
        }
        return coordMap;
    }

	/**
	 * Gets the centered point coordinates for the given graph, and the provided
	 * first level nodes.
	 * 
	 * @param graphParam
	 *            The graph to use.
	 * @param firstLevelNodes
	 *            The nodes one level out from the selected node.
	 * @param centerIndex
	 *            The index of the selected node.
	 * @param scale
	 *            Scale that determines how far apart nodes should be placed.
	 * @return The point values for the nodes.
	 */
    private static Point[] getCoords(Graph graphParam, double scale, List<Node>
    firstLevelNodes, int centerIndex, Node centerNode)
    {
        Graph graph = graphParam.getReducedGraph();
        int[] verticalPos = graph.getVertexLayers();
        int[] horizontalPos = graph.getHorizontalPosition(verticalPos);

        horizontalPos = centerTopLevelCoords(verticalPos, horizontalPos);

        ArrayList<Point> linkedCoords = getLinkedCoords(firstLevelNodes,
            verticalPos, horizontalPos, centerIndex, centerNode);
        verticalPos = combineLinkedCoords(linkedCoords, verticalPos, false);

        verticalPos = reverseVerticalCoords(verticalPos);
        horizontalPos = combineLinkedCoords(linkedCoords, horizontalPos, true);

        //return centerCoords(verticalPos, horizontalPos, scale);
        Point[] coords = new Point[verticalPos.length];
        int interval_x = (int)((Configuration.GRID_COLUMN_WIDTH
                + Configuration.GRID_BUFFER_SPACE) * Configuration.NODE_FONT_SIZE / 20);
        int interval_y = (int)((Configuration.GRID_ROW_HEIGHT
                + Configuration.GRID_BUFFER_SPACE) * scale);

        // Shifts every other row over
    	float shift = 0.0f;
        for (int i = 0; i < coords.length; i++)
        {
        	if ( verticalPos[i] % 2 == 1)
        		shift = 0.5f;
        	else
        		shift = 0;
        	
            coords[i] = new Point((int) ((horizontalPos[i]-shift) * interval_x),
                (verticalPos[i]) * interval_y);
        }
        return coords;
    }

	/**
	 * Gets the centered point coordinates for the given graph.
	 * 
	 * @param graphParam
	 *            The graph to use.
	 * @param scale
	 *            Scale that determines how far apart nodes should be placed.
	 * @return The point values for the nodes.
	 */
    private static Point[] getCoords(Graph graphParam, double scale)
    {
        Graph graph = graphParam.getReducedGraph();
        int[] verticalPos = graph.getVertexLayers();
        int[] horizontalPos = graph.getHorizontalPosition(verticalPos);
        horizontalPos = centerTopLevelCoords(verticalPos, horizontalPos);
        return centerCoords(verticalPos, horizontalPos, scale);
    }

	/**
	 * Given arrays of vertical and horizontal coordinates, centers the
	 * coordinates and returns them as a point array
	 * 
	 * @param verticalPos
	 *            The x values.
	 * @param horizontalPos
	 *            The y values.
	 * @param scale
	 *            Scale that determines how far apart nodes should be placed.
	 * @return The centered coordinates.
	 */
    private static Point[] centerCoords(int[] verticalPos, int[] horizontalPos,
        double scale)
    {
        horizontalPos = centerHorizontalCoords(verticalPos, horizontalPos);
        int interval_x = (int)((Configuration.GRID_COLUMN_WIDTH
            + Configuration.GRID_BUFFER_SPACE) * Configuration.CHAPTER_TITLE_FONT_SIZE / 50);
        int interval_y = (int)((Configuration.GRID_ROW_HEIGHT
            + Configuration.GRID_BUFFER_SPACE) * scale);
        Point[] coords = new Point[verticalPos.length];
        for (int i = 0; i < coords.length; i++)
        {
            coords[i] = new Point((horizontalPos[i]) * interval_x,
                (verticalPos[i]) * interval_y);
        }
        return coords;
    }

    private static int[] centerTopLevelCoords(int[] verticalPos, int[] horizontalPos)
    {
        int[] newHorizontalPos = horizontalPos;
        int vertMax = findMaxPos(verticalPos);
        int horizontalShift = 1;
        for(int i = 0; i < horizontalPos.length; i++)
        {
            if(verticalPos[i] == vertMax)
            {
                newHorizontalPos[i] += horizontalShift++;
            }
        }
        return newHorizontalPos;
    }

	/**
	 * Centers an array of positions at 0.
	 * 
	 * @param verticalPos
	 *            Array of vertical position of each node.
	 * @param horizontalPos
	 *            Array of horizontal position of each node.
	 * @return The centered horizontal coordinates.
	 */
    private static int[] centerHorizontalCoords(int[] verticalPos, int[] horizontalPos)
    {
        int[] centeredhorizontal = horizontalPos;
        Map<Integer, Integer> nodesPerLevel = getNodesPerLevel(verticalPos);
        int baseNodeCount = getMaxNodesPerLevel(nodesPerLevel);
        int vertMax = findMaxPos(verticalPos);
        int horizontalShift = 0;
        for(int i = 0; i < horizontalPos.length; i++)
        {
            horizontalShift = ((baseNodeCount * 2 -
                (nodesPerLevel.get(verticalPos[i]) * 2))) / 2;

            centeredhorizontal[i] = centeredhorizontal[i] * 2 + horizontalShift;
        }

        // Center the nodes at horizontal position 0.
        for(int i = 0; i < horizontalPos.length; i++)
        {
            if(verticalPos[i] == vertMax)
            {
                horizontalShift = centeredhorizontal[i];
                break;
            }
        }
        for(int i = 0; i < horizontalPos.length; i++)
        {
            centeredhorizontal[i] -= horizontalShift;
        }
        return centeredhorizontal;
    }

	/**
	 * Given an array of vertical positions, creates a map of how many nodes are
	 * on each level.
	 * 
	 * @param verticalPos
	 *            The array of each node's vertical position.
	 * @return The map of nodes per level.
	 */
    private static Map<Integer, Integer> getNodesPerLevel(int[] verticalPos)
    {
        Map<Integer, Integer> nodesPerLevel = new HashMap<Integer, Integer>();
        for(int i = 0; i < verticalPos.length; i++)
        {
            int currCount = 0;
            if (nodesPerLevel.containsKey(verticalPos[i]))
                currCount = nodesPerLevel.remove(verticalPos[i]);
            nodesPerLevel.put(verticalPos[i], ++currCount);
        }
        return nodesPerLevel;
    }

	/**
	 * Given a map of nodesPerLevel, finds the largest number of nodes on any
	 * level.
	 * 
	 * @param nodesPerLevel
	 *            The map to search.
	 * @return The largest amount of nodes on any level.
	 */
    private static int getMaxNodesPerLevel(Map<Integer, Integer> nodesPerLevel)
    {
        int max = 0;
        for(int i: nodesPerLevel.values())
        {
            if (i > max)
                max = i;
        }
        return max;
    }

	/**
	 * Reverses the order of a set of vertical coordinates.
	 * 
	 * @param verticalPos
	 *            The coordinates to reverse.
	 * @return The reveres coordinates.
	 */
    private static int[] reverseVerticalCoords(int[] verticalPos)
    {
        int vertMax = findMaxPos(verticalPos);
        int[] reversedPos = new int[verticalPos.length];
        for (int i = 0 ; i < reversedPos.length; i++)
        {
            reversedPos[i] = vertMax - verticalPos[i];
        }
        return reversedPos;
    }

	/**
	 * Finds the largest value in an integer array.
	 * 
	 * @param pos
	 *            The array to search.
	 * @return The largest value in the array,
	 */
    private static int findMaxPos(int[] pos)
    {
        int max = 0;
        for(int i = 0; i < pos.length; i++)
        {
            if(pos[i] > max)
                max = pos[i];
        }
        return max;
    }

    private static ArrayList<Point> getLinkedCoords(List<Node> firstLevelNodes,
        int[] verticalPos, int[] horizontalPos, int centerIndex, Node centerNode)
    {
        ArrayList<Point> linkedCoords = new ArrayList<Point>();
        Map<Integer, Integer> nodesPerLevel = getNodesPerLevel(verticalPos);
        boolean selectedOnRight = false;
        int horizontalMax = findMaxPos(horizontalPos);
        if (horizontalPos[centerIndex] > horizontalMax / 2)
            selectedOnRight = true;
        for(Node node: firstLevelNodes)
        {
            int vertical, horizontal;
            int depth = Node.findDepthBetween(centerNode, node);
            if (depth > 0)
                vertical = verticalPos[centerIndex] + depth;
            else
                vertical = verticalPos[centerIndex] - depth;

            if (!nodesPerLevel.containsKey(vertical))
            	horizontal = horizontalPos[centerIndex];
            else if (selectedOnRight)
            {
            	int max = horizontalPos[centerIndex];
            	for (int i = 0; i < horizontalPos.length; i++)
            	{
            		if(horizontalPos[i] > max && verticalPos[i] == vertical)
            			max = horizontalPos[i];
            	}
                for (Point point: linkedCoords)
                {
                    if (point.y == vertical && point.x > max)
                        max = point.x;
                }
                horizontal = max + 1;
            }
            else
            {
            	int min = horizontalPos[centerIndex];
            	for (int i = 0; i < horizontalPos.length; i++)
            	{
            		if(horizontalPos[i] < min && verticalPos[i] == vertical)
            			min = horizontalPos[i];
            	}
                for (Point point: linkedCoords)
                {
                    if (point.y == vertical && point.x < min)
                        min = point.x;
                }
                horizontal = min - 1;
            }
            if (nodesPerLevel.containsKey(vertical))
                nodesPerLevel.put(vertical, nodesPerLevel.remove(vertical) + 1);
            else
                nodesPerLevel.put(vertical, 1);
            linkedCoords.add(new Point(horizontal, vertical));
        }
        return linkedCoords;
    }

	/**
	 * Adds the given linkedCoords to the given int array.
	 * 
	 * @param linkedCoords
	 *            The linked coordinates to add.
	 * @param Coords
	 *            The integer array to add the coordinates to.
	 * @param horizontal
	 *            Flag to determine if the x or y coordinates should be added.
	 * @return The combined coordinate array.
	 */
    private static int[] combineLinkedCoords(ArrayList<Point> linkedCoords,
        int[] Coords, boolean horizontal)
    {
        int[] newCoords = new int[Coords.length + linkedCoords.size()];
        for (int i = 0; i < Coords.length; i++)
        {
            newCoords[i] = Coords[i];
        }

        for (int i = 0; i < linkedCoords.size(); i++)
        {
            if (horizontal)
                newCoords[i + Coords.length] = linkedCoords.get(i).x;
            else
                newCoords[i + Coords.length] = linkedCoords.get(i).y;
        }
        return newCoords;
    }

}
