package model;

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

    private final String FILENAME = "C:out.plain";
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

    /**
     * Create a graphviz graph to determine the coordinates of the chapters.
     */
    private void generateGraphFromChapters(ArrayList<String> chapters)
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

        File out = new File(FILENAME);
        gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), "plain" ), out );
    }
    /**
     * Given a list of nodes, creates a graphviz graph based on their links.
     * @param nodes the list of nodes to create the graph for
     */
    private void createGraphFromNodes(ArrayList<Node> nodes)
    {
        GraphViz gv = new GraphViz();
        gv.addln(gv.start_graph());
        //add a link in the graphviz graph for each link within this chapter
        for(Node node: nodes)
        {
            List<Link> links = node.getNodeLinks();
            for(Link link: links)
            {
                Node fromNode = link.getFromNode();
                Node toNode = link.getToNode();
                if(fromNode.equals(node) &&
                    nodes.contains(toNode))
                {
                    //use labels that are index + 10, so they will be the same length
                    int labelFrom = nodes.indexOf(fromNode) + 10;
                    int labelTo = nodes.indexOf(toNode) + 10;
                    gv.addln(labelFrom + " -> " +
                        labelTo + ";");
                }
            }
        }

        gv.addln(gv.end_graph());

        File out = new File(FILENAME);
        gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), "plain" ), out );
    }
    /**
     * Read the graphviz output and set the nodes to the correct coordinates
     * @param nodes the list of nodes to set
     */
    public void setNodeCoords(ArrayList<Node> nodes)
    {
        createGraphFromNodes(nodes);
        try
        {
            Map<Integer, Point> map = GraphViz.parseText(FILENAME, 100, 200);
            for(Map.Entry<Integer, Point> entry: map.entrySet())
            {
                nodes.get(entry.getKey() - 10).moveTo(entry.getValue().x,
                    entry.getValue().y);
            }
        }
        catch (FileNotFoundException e)
        {
          e.printStackTrace();
        }
    }
    /**
     * Get a map of chapters to coordinates.
     */
    public Map<String, Point> getChapterCoords()
    {
        Map<String, Point> ret = new HashMap<String, Point>();
        ArrayList<String> chapters = new ArrayList<String>(nodeMap.keySet());
        generateGraphFromChapters(chapters);
        try
        {
            //currently passing an arbitrary scale into parseText
            Map<Integer, Point> coords = GraphViz.parseText(FILENAME, 200, 200);
            for (Integer key: coords.keySet())
            {
                int index = key - 10;
                String chapter = chapters.get(index);
                ret.put(chapter, coords.get(key));
            }
        }
        catch (FileNotFoundException e)
        {
            System.out.println("There's no Graph to read");
            e.printStackTrace();
        }
        return ret;
    }
}
