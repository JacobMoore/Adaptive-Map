package model;

import controller.Configuration;
import controller.GraphViz;
import HierarchialLayout.Graph;
import HierarchialLayout.Edge;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import model.Node.ChapterProperties;

import fr.inria.zvtm.glyphs.VText;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

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
    private HashMap<String, byte[]> nodeData;

	 // Map of chapter names to chapter properties
	 private Map<String, ChapterProperties> chapterTypes;
	
	 /**
	  * Adds a chapter with its associated properties to the map.
	  * @param chapterName The name of the chapter.
	  * @param chapterProperties The properties of the chapter.
	  */
	 public void addChapterType(String chapterName,
	 		ChapterProperties chapterProperties) {
	 	if (chapterTypes == null) {
	 		chapterTypes = new HashMap<String, ChapterProperties>();
	 	}
	 	chapterTypes.put(chapterName, chapterProperties);
	 }
	 
	 /**
	  * Gets the chapterProperties object for a given chapter.
	  * @param chapter The name of the chapter.
	  * @return The chapter properties of the given chapter, or null.
	  */
	 public ChapterProperties getChapterType(String chapter)
	 {
		 return chapterTypes.get(chapter);
	 }
    
    /**
     * Create a new NodeMap
     */
    public NodeMap()
    {
        nodeMap = new HashMap<String, ArrayList<Node>>();
        nodeData = new  HashMap<String, byte[]>();
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
     * Get a map of chapters to coordinates.
     * @param nodeMap The node map of chapters to nodes.
     * @param fontSize The size of the overview font, used for spacing.
     * @return The coordinates of each chapter.
     */
	public static Map<String, Point> getChapterCoords(NodeMap nodeMap, int fontSize)
    {
        Map<String, Point> coordMap = new HashMap<String, Point>();
        ArrayList<String> chapters = new ArrayList<String>(nodeMap.getChapters());
        Edge[] edges = generateEdgeArrayFromChapters(chapters, nodeMap);
        Graph graph = new Graph(chapters.size(), edges);
        Point[] coords = getCoords(graph, CHAPTER_SCALE, fontSize);
        
        for (int i = 0; i < chapters.size(); i++)
        {
            String chapter = chapters.get(i);
            coordMap.put(chapter, coords[i]);
        }
        return coordMap;
    }
	
    
	/*----------------------- GraphViz methods ------------------------*/
    
    /**
     * Given a list of nodes, creates a GraphViz graph based on their links.
     * @param nodes the list of nodes to create the graph for
     * @return GraphViz object representing the linked nodes.
     */
    public GraphViz generateGraphFromNodes(ArrayList<Node> nodes)
    {
    	if ( Configuration.RUN_AS_APPLET ) //Only call in java application
    		return null;
    	
        GraphViz gv = new GraphViz();
        gv.addln(gv.start_graph());

        //add a link in the graphviz graph for each link within this chapter
        for(Node node: nodes)
        {
            int labelFrom = nodes.indexOf(node) + 10;
            gv.addln(labelFrom + ";");
            List<Link> links = node.getNodeLinks();
            for(Link link: links)
            {
                Node fromNode = link.getFromNode();
                Node toNode = link.getToNode();
                if(fromNode.equals(node) &&
                    nodes.contains(toNode))
                {
                    //use labels that are index + 10, so they will be the same length
                    int labelTo = nodes.indexOf(toNode) + 10;
                    gv.addln(labelFrom + " -> " +
                        labelTo + ";");
                }
            }
        }
        gv.addln(gv.end_graph());
        return gv;
    }
    
	/**
	 * Gets saved graphViz data from the server. The data is then saved in nodeData,
	 * so that it only needs to be retrieved from the server once.
	 * @param centerNode The selected node.
	 * @param overview If the graph layout for the overview nodes is desired
	 * @return The graphviz data for the desired chapter, or the overview if specified.
	 */
	private byte[] getGraphData(final Node centerNode, final boolean overview) {
		if ( !overview && nodeData.containsKey(centerNode.getNodeChapter()))
			return nodeData.get(centerNode.getNodeChapter());
		else if ( overview && nodeData.containsKey("OVERVIEW"))
			return nodeData.get("OVERVIEW");
		
                boolean useLocal = Configuration.runLocally;
                
		byte[] graph = null;
		try {
                    String location = "";

                    if (!overview )
                    {
                            location = String.format("%s-%s", 
                                    Configuration.getDataFilePath(useLocal), 
                                    centerNode.getNodeChapter().replace(" ", "_"));
                            
                    }
                    else
                    {
                            location = String.format("%s-%s", 
                                            Configuration.getDataFilePath(useLocal), 
                                            "OVERVIEW");
                    }
                    BufferedInputStream in;
                            
                    if(!useLocal) {
                        URL target = null;

                        try {
                                target = new URL(location);
                        }	catch (MalformedURLException e)	{
                                System.err.println("ERROR: Malformed URL");
                                System.err.println(e.getLocalizedMessage());
                        }
                        InputStream stream = target.openStream();
                        in = new BufferedInputStream(stream);
                    } else {
                        FileInputStream fr = new FileInputStream(new File(location));
                        in = new BufferedInputStream(fr);
                    }
                            // Code required to make eclipse load data
                    graph = new byte[Configuration.GRAPHVIZ_BUFFER_LIMIT];
                    in.mark(2);
                    int next = in.read();
                    if ( next != -1 ) {
                            in.reset();
                            next = in.read(graph);
                    }
                    /*
                     * OLD code that works fine everywhere except eclipse
                     * 	graph = new byte[in.available()];
                             *  in.read(graph);
                     */

                            if (in != null)
                                    in.close();

                            if (!overview)
                                    nodeData.put(centerNode.getNodeChapter(), graph);
                            else
                                    nodeData.put("OVERVIEW", graph);
                            return graph;
		} catch (FileNotFoundException e) {
                        System.err.println("ERROR: File not found");
			System.err.println(e.getLocalizedMessage());
			return null;
		} catch (IOException e) {
                        System.err.println("ERROR: IO Exception");
			System.err.println(e.getLocalizedMessage());
			return null;
		}
	}
    
    /**
     * Read the GraphViz output and set the nodes to the correct coordinates
     * @param nodes the list of nodes to set
     * @param centerNode the node to center the list on
     * @return the new node coordinates
     */
    public HashMap<Integer, Point> setNodeCoordsFromFile(ArrayList<Node> nodes,
    		Node centerNode)
    {
        HashMap<Integer, Point> coords = new HashMap<Integer, Point>();
        byte[] graph = getGraphData(centerNode, false);
        if ( graph == null ) {
        	System.err.println("ERROR!: Unable to load Graphviz data.");
        	System.err.println("ERROR!: getGraphData returned null.");
        	return null;
        }
        
        // Determine initial node positions
        int minWidth = 0, minHeight = Configuration.GRID_BUFFER_SPACE;
        for ( Node n : nodes )
        {
        	minWidth += Math.max(Configuration.MIN_NODE_WIDTH, n.getNodeTitleWidth());
        	minHeight += VText.getMainFont().getSize() + Configuration.NODE_PADDING;
        }
        minHeight *= 2;
		
        Map<Integer, Point> coordMap = GraphViz.parseText(graph, minWidth, minHeight);
        if ( coordMap.isEmpty() ) {
        	System.err.println("ERROR!: Unable to parse Graphviz data.");
        	System.err.println("ERROR!: getGraphData returned empty byte array.");
        	return null;
        }
        
        // Reposition nodes, depending on how many rows and columns there are
        Map<Integer, Integer> rows = new HashMap<Integer, Integer>();
        Map<Integer, Integer> columns = new HashMap<Integer, Integer>();

        for (Entry<Integer, Point> entry: coordMap.entrySet())
        {
        	if ( !rows.containsKey(entry.getValue().x) )
        		rows.put(entry.getValue().x, 1);
        	
        	if ( !columns.containsKey(entry.getValue().y) )
        		columns.put(entry.getValue().y, 1);
        }
        
        minWidth = rows.size() * Configuration.MIN_NODE_WIDTH;
        minHeight = columns.size() * VText.getMainFont().getSize() * 20;
        coordMap = GraphViz.parseText(graph, minWidth, minHeight);
        if ( coordMap.isEmpty() ) {
        	System.err.println("ERROR!: Unable to parse Graphviz data.");
        	return null;
        }
        
        int centerIndex = nodes.indexOf(centerNode) + 10;
        int xDifference = (int) (centerNode.getGlyph().vx - coordMap.get(centerIndex).x);
        int yDifference = (int) (centerNode.getGlyph().vy - coordMap.get(centerIndex).y);
        for (Entry<Integer, Point> entry: coordMap.entrySet())
        {
            Point point = new Point((int)(entry.getValue().x + xDifference),
                (int)(entry.getValue().y + yDifference));
            coords.put(entry.getKey() - 10, point);
        }

        return coords;
    }
	
    /**
     * Read the GraphViz output and set the chapter nodes to the correct coordinates
     * @param chapters the list of chapters
     * @param centerNode the default chapter node
     * @return the new chapter coordinates
     */
    public HashMap<String, Point> setChapterCoordsFromFile(
    		ArrayList<String> chapters, Node centerNode)
	{
        HashMap<String, Point> coords = new HashMap<String, Point>();

        byte[] graph = getGraphData(centerNode, true);
        if ( graph == null )
        	return null;
        
        int minWidth = Configuration.GRID_BUFFER_SPACE + chapters.size() * 900;
        int minHeight = Configuration.GRID_BUFFER_SPACE + chapters.size() * 300;
		
        Map<Integer, Point> coordMap = GraphViz.parseText(graph, minWidth, minHeight); //10000, 5000
        
        int i = 0;
        for (Entry<Integer, Point> entry: coordMap.entrySet())
        {
            String chapter = chapters.get(i);
            coords.put(chapter, entry.getValue());
            i++;
        }
        return coords;
	}
    
    /**
     * Sets the linked coordinates for a graph generated by GraphViz
     * @param firstLevelNodes The list of first level nodes
     * @param verticalPos Array of y positions
     * @param horizontalPos Array of x positions
     * @param centerIndex Index of center node in the arrays
     * @param centerNode The center node
     * @return List of points for firstLevelNodes
     */
    public static ArrayList<Point> setLinkedCoordsForGraphViz (List<Node> firstLevelNodes,
            int[] verticalPos, int[] horizontalPos, int centerIndex, Node centerNode)
    {
        ArrayList<Point> linkedCoords = new ArrayList<Point>();
        Map<Integer, Integer> nodesPerLevel = getNodesPerLevel(verticalPos);

        int depthPlus1 = findMinPos(verticalPos);
        int depthMinus1 = findMaxPos(verticalPos);
        
        // Find the correct vertical positions for the first level nodes
        for (int i=0; i < verticalPos.length; i++)
        {
        	if ( verticalPos[i] > verticalPos[centerIndex] && verticalPos[i] < depthMinus1 )
        		depthMinus1 = verticalPos[i];

        	if ( verticalPos[i] < verticalPos[centerIndex] && verticalPos[i] > depthPlus1 )
        		depthPlus1 = verticalPos[i];
        }
        if ( depthMinus1 == verticalPos[centerIndex] )
        	depthMinus1 += centerNode.getHeight() + Configuration.GRID_ROW_HEIGHT;
        if ( depthPlus1 == verticalPos[centerIndex] )
        	depthPlus1 -= centerNode.getHeight() + Configuration.GRID_ROW_HEIGHT;
        
        for(Node node: firstLevelNodes)
        {
            int vertical, horizontal;
            int depth = Node.findDepthBetween(centerNode, node);
            if (depth > 0)
                vertical = depthPlus1;
            else
                vertical = depthMinus1;

            if (!nodesPerLevel.containsKey(vertical))
            	horizontal = horizontalPos[centerIndex];
            else
            {
            	int max = Integer.MIN_VALUE;
            	int min = Integer.MAX_VALUE;
            	for (int i = 0; i < horizontalPos.length; i++)
            	{
            		if(horizontalPos[i] > max && verticalPos[i] == vertical)
            			max = horizontalPos[i];
            		if(horizontalPos[i] < min && verticalPos[i] == vertical)
            			min = horizontalPos[i];
            	}
                for (Point point: linkedCoords)
                {
                    if (point.y == vertical && point.x > max)
                        max = point.x;
                    if (point.y == vertical && point.x < min)
                        min = point.x;
                }
                if ( Point.distance(horizontalPos[centerIndex], 0, max, 0) 
                		< Point.distance(horizontalPos[centerIndex], 0, min, 0) )
                	horizontal = (int) (max + Configuration.GRID_COLUMN_WIDTH + node.getWidth());
                else
                	horizontal = (int) (min -  Configuration.GRID_COLUMN_WIDTH - node.getWidth());
            }
            if (nodesPerLevel.containsKey(vertical))
                nodesPerLevel.put(vertical, nodesPerLevel.remove(vertical) + 1);
            else
                nodesPerLevel.put(vertical, 1);
            linkedCoords.add(new Point(horizontal, vertical));
        }
    	
    	return linkedCoords;
    }
    
    /*------------------------ Helper methods -----------------------*/
    
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
	 * Finds the largest value in an integer array.
	 * 
	 * @param pos
	 *            The array to search.
	 * @return The largest value in the array,
	 */
    private static int findMaxPos(int[] pos)
    {
        int max = Integer.MIN_VALUE;
        for(int i = 0; i < pos.length; i++)
        {
            if(pos[i] > max)
                max = pos[i];
        }
        return max;
    }
    
	/**
	 * Finds the smallest value in an integer array.
	 * 
	 * @param pos
	 *            The array to search.
	 * @return The largest value in the array,
	 */
    private static int findMinPos(int[] pos)
    {
        int min = Integer.MAX_VALUE;
        for(int i = 0; i < pos.length; i++)
        {
            if(pos[i] < min)
                min = pos[i];
        }
        return min;
    }
    
    /*---------------- Backup HierachicalLayout Code ----------------*/
    
    /**
     * Create an edge array to determine the coordinates of the chapters.
     * @param chapters List of all chapter names
     * @param nodeMap The NodeMap object containing each chapter's nodes
     * @return Edge array representing all chapters
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
     * @return An array of edge objects representing the relationship between all nodes.
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
	 * @param centerNode 
	 * 			  The node that is selected
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
                + Configuration.GRID_BUFFER_SPACE) * centerNode.getTitleFontSize() / 20);
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
	 * @param fontSize The size of the overview font, used for spacing.
	 * 
	 * @return The point values for the nodes.
	 */
    private static Point[] getCoords(Graph graphParam, double scale, int fontSize)
    {
        Graph graph = graphParam.getReducedGraph();
        int[] verticalPos = graph.getVertexLayers();
        int[] horizontalPos = graph.getHorizontalPosition(verticalPos);
        horizontalPos = centerTopLevelCoords(verticalPos, horizontalPos);
        return centerCoords(verticalPos, horizontalPos, scale, fontSize);
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
	 * @param fontSize The size of the overview font, used for spacing.
	 * 
	 * @return The centered coordinates.
	 */
    private static Point[] centerCoords(int[] verticalPos, int[] horizontalPos,
        double scale, int fontSize)
    {
        horizontalPos = centerHorizontalCoords(verticalPos, horizontalPos);
        int interval_x = (int)((Configuration.GRID_COLUMN_WIDTH
            + Configuration.GRID_BUFFER_SPACE) * fontSize / 50);
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
     * Get linked coordinates for positions generated by HierachcicalLayout.
     * Does not layout nodes depending on which side has less nodes like GraphViz version.
     * @param firstLevelNodes
     * @param verticalPos
     * @param horizontalPos
     * @param centerIndex
     * @param centerNode
     * @return
     */
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
