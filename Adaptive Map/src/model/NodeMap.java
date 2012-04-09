package model;

import java.util.Map.Entry;
import java.util.List;
import controller.GraphViz;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
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

    private final String FILENAME = "out.plain";
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
     * Create a graphviz graph to determine the coordinates of the chapters.
     */
    private byte[] generateGraphFromChapters(ArrayList<String> chapters)
    {
        ArrayList<String> usedChapters = new ArrayList<String>();
        GraphViz gv = new GraphViz();
        gv.addln(gv.start_graph());
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
                        int labelFrom = chapters.indexOf(chapter) + 10;
                        int labelTo = chapters.indexOf(linkChapter) + 10;
                        gv.addln(labelFrom + " -> " + labelTo);
                        usedChapters.add(chapter);
                    }
                }
            }
        }
        gv.addln(gv.end_graph());
        return gv.getGraph( gv.getDotSource(), "plain" );
        //File out = new File(FILENAME);
        //gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), "plain" ), out );
    }
    /**
     * Given a list of nodes, creates a graphviz graph based on their links.
     * @param nodes the list of nodes to create the graph for
     */
    private byte[] generateGraphFromNodes(ArrayList<Node> nodes)
    {

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
        return gv.getGraph( gv.getDotSource(), "plain" );
        //File out = new File(FILENAME);
        //gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), "plain" ), out );
    }
    /**
     * Read the graphviz output and set the nodes to the correct coordinates
     * @param nodes the list of nodes to set
     * @param centerNode the node to center the list on
     * @return the new node coordinates
     */
    public Map<Integer, Point> setNodeCoords(ArrayList<Node> nodes, Node centerNode)
    {
        Map<Integer, Point> coords = new HashMap<Integer, Point>();
        byte[] graph = generateGraphFromNodes(nodes);

        Map<Integer, Point> map = GraphViz.parseText(graph, 1000, 1000);
        //center the points on the center node
        int centerIndex = nodes.indexOf(centerNode) + 10;
        int xDifference = centerNode.getCenterPoint().x - map.get(centerIndex).x;
        int yDifference = centerNode.getCenterPoint().y - map.get(centerIndex).y;
        for (Entry<Integer, Point> entry: map.entrySet())
        {
            Point point = new Point(entry.getValue().x + xDifference,
                entry.getValue().y + yDifference);
            coords.put(entry.getKey() - 10, point);
        }
        for(Map.Entry<Integer, Point> entry: coords.entrySet())
        {
            nodes.get(entry.getKey()).moveTo(entry.getValue().x,
                entry.getValue().y);
        }
        return coords;
    }


    /**
     * Get a map of chapters to coordinates.
     */
    public Map<String, Point> getChapterCoords()
    {
        Map<String, Point> ret = new HashMap<String, Point>();
        ArrayList<String> chapters = new ArrayList<String>(nodeMap.keySet());
        byte[] graph = generateGraphFromChapters(chapters);
        Map<Integer, Point> coords = GraphViz.parseText(graph, 2800, 2800);
        for (Integer key: coords.keySet())
        {
            int index = key - 10;
            String chapter = chapters.get(index);
            ret.put(chapter, coords.get(key));
        }
        return ret;
    }
}
