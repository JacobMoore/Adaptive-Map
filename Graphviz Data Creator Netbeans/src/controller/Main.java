package controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import model.Link;
import model.Node;
import model.NodeMap;
import model.Link.LinkProperties;
import model.Node.ChapterProperties;

import controller.xml.XmlParser;

/**
 * Main file, used for generating graphviz data for the applet.
 * 
 * @author Michel Pascale
 * @version Jul 25, 2012
 */
public class Main {
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Configuration.RUN_AS_APPLET = false;
		ArrayList<Node> nodeList = new ArrayList<Node>();
		NodeMap nodeMap = new NodeMap();
		System.out.println("Loading data from: "
				+ Configuration.getXMLFilePath(true));

		for (Entry<String, ChapterProperties> chapterProperty : XmlParser
				.parseChapterProperties().entrySet()) {
			nodeMap.addChapterType(chapterProperty.getKey(),
					chapterProperty.getValue());
		}
		XmlParser.parseNodeInformation(nodeMap, 10);
		nodeList.addAll(nodeMap.getNodes());

		// IMPORTANT: parse node properties before linking the nodes
		for (Entry<String, LinkProperties> linkProperty : XmlParser
				.parseLinkProperties().entrySet()) {
			Link.addLinkType(linkProperty.getKey(), linkProperty.getValue());
		}
		XmlParser.parseNodeLinks(nodeList);

		for (String chapter : nodeMap.getChapters()) {
			String location = String.format("%s-%s",
					Configuration.getGraphViz(),
					chapter.replace(" ", "_"));
			System.out.println("Creating file: " + location);
			File out = new File(location);
                        out.getParentFile().mkdirs();
			out.createNewFile();
			GraphViz gv = nodeMap.generateGraphFromNodes(nodeMap
					.getChapterNodes(chapter));
			if (gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), "plain"),
					out, false) != 1)
				System.out
						.println("Error writing graphViz data. Check that the folder C:\temp exists.");
		}

		// Parse chapter nodes
		ArrayList<Node> chapterList = new ArrayList<Node>();
		for (Entry<String, ChapterProperties> chapterProperty : XmlParser
				.parseChapterProperties().entrySet()) {
			Node newChapter = new Node(chapterProperty.getKey(),
					chapterProperty.getValue().getDescription(),
					chapterProperty.getKey(), chapterProperty.getValue()
							.getKeywords(), java.awt.Color.white, 10, 10, 1.0f);
			if (Configuration.USE_FIXED_NODE_POSITIONS) {
				newChapter.setFixedNodePosition(chapterProperty.getValue()
						.getChapterXPos(), chapterProperty.getValue()
						.getChapterYPos());
			}
			chapterList.add(newChapter);
		}
		for (Node currentChapter : chapterList) {
			if (currentChapter == null)
				continue;
			List<Link> links = new ArrayList<Link>();
			links.addAll(Node.getAllLinks());
			for (final Link l : links) {
				if (l.getFromNode().getNodeChapter()
						.equals(currentChapter.getNodeTitle())
						&& !l.getFromNode().equals(currentChapter)
						&& !l.getToNode().getNodeChapter()
								.equals(currentChapter.getNodeTitle())) {
					for (Node chapter : chapterList) {
						if (l.getToNode().getNodeChapter()
								.equals(chapter.getNodeTitle())) {
							Node targetChapter = chapter;
							Node.link(currentChapter, targetChapter,
									Link.LinkLineType.STANDARD.name(), 100,
									true);
							break;
						}
					}
				}
			}
		}
		String location = String.format("%s-%s",
                        Configuration.getGraphViz(), "OVERVIEW");
		File out = new File(location);
                out.getParentFile().mkdirs();
		out.createNewFile();
		GraphViz gv = nodeMap.generateGraphFromNodes(chapterList);
		if (gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), "plain"), out,
				false) != 1)
			System.out
					.println("Error writing graphViz data. Check that the folder C:\temp exists.");

		System.out.println("Complete.");
	}
}
