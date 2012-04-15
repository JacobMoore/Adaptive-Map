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
    private final double CHAPTER_SCALE = 2;
    private final double NODE_SCALE = 0.5;
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

    public ArrayList<Point> getNodeCoords()
    {
        ArrayList<Point> points = new ArrayList<Point>();
        for (Node node: getNodes())
        {
            points.add(node.getCenterPoint());
        }
        return points;
    }

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
    private Edge[] generateEdgeArrayFromChapters(ArrayList<String> chapters)
    {
        ArrayList<String> usedChapters = new ArrayList<String>();
        ArrayList<Edge> edges = new ArrayList<Edge>();
        //for each link in each node in each chapter, check if it links to
        //another chapter
        for (String chapter: chapters)
        {
            ArrayList<Node> nodes = nodeMap.get(chapter);
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
                        edges.add(new Edge(indexFrom, indexTo));
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
    private Edge[] generateEdgeArrayFromNodes(ArrayList<Node> nodes)
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
                    edges.add(new Edge(indexFrom, indexTo));
                }
            }
        }
        return edges.toArray(new Edge[0]);
    }

    /**
     * Read an edge array and set the nodes to the correct coordinates
     * @param nodes the list of nodes to set
     * @param centerNode the node to center the list on
     * @return the new node coordinates
     */
    public Map<Integer, Point> setNodeCoords(ArrayList<Node> nodes, Node centerNode)
    {
        Map<Integer, Point> coordMap = new HashMap<Integer, Point>();
        Edge[] edges = generateEdgeArrayFromNodes(nodes);

        Graph graph = new Graph(nodes.size(), edges);
        Point[] points = getCoords(graph, NODE_SCALE);

        //center the points on the center node
        int centerIndex = nodes.indexOf(centerNode);
        int xDifference = centerNode.getCenterPoint().x - points[centerIndex].x;
        int yDifference = centerNode.getCenterPoint().y - points[centerIndex].y;
        for (int i = 0; i < points.length; i++)
        {
            Point point = new Point(points[i].x + xDifference,
                points[i].y + yDifference);
            coordMap.put(i, point);
        }
        for(Map.Entry<Integer, Point> entry: coordMap.entrySet())
        {
            nodes.get(entry.getKey()).moveTo(entry.getValue().x,
                entry.getValue().y);
        }
        return coordMap;
    }

    /**
     * Get a map of chapters to coordinates.
     */
    public Map<String, Point> getChapterCoords()
    {
        Map<String, Point> ret = new HashMap<String, Point>();
        ArrayList<String> chapters = new ArrayList<String>(nodeMap.keySet());
        Edge[] edges = generateEdgeArrayFromChapters(chapters);
        Graph graph = new Graph(chapters.size(), edges);
        Point[] coords = getCoords(graph, CHAPTER_SCALE);

        for (int i = 0; i < chapters.size(); i++)
        {
            String chapter = chapters.get(i);
            ret.put(chapter, coords[i]);
        }
        return ret;
    }

    private  Point[] getCoords(Graph graphParam, double scale)
    {
        Graph graph = graphParam.getReducedGraph();
        int[] verticalPos = graph.getVertexLayers();
        int[] horizontalPos = graph.getHorizontalPosition(verticalPos);
        //Graph only gives a vertical and horizontal ordering, the nodes
        //must then be centered.
        horizontalPos = centerHorizontalCoords(verticalPos, horizontalPos);
        int interval_x = (int)((Configuration.GRID_COLUMN_WIDTH
            + Configuration.GRID_BUFFER_SPACE) * scale);
        int interval_y = (int)((Configuration.GRID_ROW_HEIGHT
            + Configuration.GRID_BUFFER_SPACE) * scale);
        Point[] coords = new Point[verticalPos.length];
        for (int i = 0; i < coords.length; i++)
        {
            //verticalPos is not 0 based, so 1 must be subtracted
            coords[i] = new Point((horizontalPos[i]) * interval_x,
                (verticalPos[i] - 1) * interval_y);
        }
        return coords;
    }

    private int[] centerHorizontalCoords(int[] verticalPos, int[] horizontalPos)
    {
        int[] centeredhorizontal = CenterTopLevelCoords(verticalPos, horizontalPos);
        Map<Integer, Integer> nodesPerLevel = new HashMap<Integer, Integer>();
        int maxCount = 0;
        for(int i = 0; i < verticalPos.length; i++)
        {
            int currCount = 0;
            if (nodesPerLevel.containsKey(verticalPos[i]))
                currCount = nodesPerLevel.remove(verticalPos[i]);
            nodesPerLevel.put(verticalPos[i], ++currCount);
            if (currCount > maxCount)
            {
                maxCount = currCount;
            }
        }
        int baseNodeCount = maxCount;
        int horizontalShift;
        for(int i = 0; i < horizontalPos.length; i++)
        {
            horizontalShift = ((baseNodeCount * 2 -
                (nodesPerLevel.get(verticalPos[i]) * 2))) / 2;

            centeredhorizontal[i] = centeredhorizontal[i] * 2 + horizontalShift;

        }
        return centeredhorizontal;
    }

    private int[] CenterTopLevelCoords(int[] verticalPos, int[] horizontalPos)
    {
        int[] fixedhorizontalPos = horizontalPos;
        int vertMax = 0;
        for(int i = 0; i < verticalPos.length; i++)
        {
            if(verticalPos[i] > vertMax)
                vertMax = verticalPos[i];
        }
        int horizontalShift = 1;
        for(int i = 0; i < horizontalPos.length; i++)
        {
            if(verticalPos[i] == vertMax)
            {
                fixedhorizontalPos[i] += horizontalShift++;
            }
        }
        return fixedhorizontalPos;
    }

}
