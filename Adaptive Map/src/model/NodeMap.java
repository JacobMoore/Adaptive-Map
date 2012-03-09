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

    private void generateChapterCoords()
    {
        //find which chapters are linked, and treat them as nodes
    }
    /**
     * Given a list of nodes, creates a graphviz graph based on their links.
     * @param nodes the list of nodes to create the graph for
     */
    private void CreateGraphViz(ArrayList<Node> nodes)
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
                    gv.addln(fromNode.getNodeTitle() + " -> " +
                        toNode.getNodeTitle() + ";");
                }
            }
        }

        gv.addln(gv.end_graph());
        System.out.println(gv.getDotSource());

        File out = new File(FILENAME);
        gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), "plain" ), out );
    }
    /**
     * Read the graphviz output and set the nodes to the correct coordinates
     * @param nodes the list of nodes to set
     */
    private void readGraphViz(ArrayList<Node> nodes)
    {
        try
        {
            Map<String, Point> map = GraphViz.parseText(FILENAME, 100, 200);
            System.out.println(map.size());
            for(Map.Entry<String, Point> entry: map.entrySet())
            {
                for(Node node: nodes)
                {
                    if(node.getNodeTitle().equals(entry.getKey()))
                    {
                        node.moveTo(entry.getValue().x, entry.getValue().y);
                    }
                }
            }
        }
        catch (FileNotFoundException e)
        {
          e.printStackTrace();
        }
    }

}
