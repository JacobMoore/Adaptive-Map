package controller.xml;

import java.awt.Color;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import model.Link;
import model.Node;
import model.Link.LinkLineType;
import model.Link.LinkProperties;
import model.Node.ChapterProperties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import controller.Configuration;

import model.NodeMap;

enum Tag {
	// Node XML tags
	TEXTBOOK("textbook"),
	NODE("node"),
	TITLE("title"),
	DESCRIPTION("description"),
	CHAPTER("chapter"),
	PAGE("page"),
	LINK("link"),
	LINK_TYPE("linktype"),
	LINE_TYPE("linetype"),
	COLOR("color"),
	CHAPTER_TYPE("chaptertype"),
	// Grid Tags
	GRIDS("grids");

	public static Tag fromTagLabel(String label) {
		for (Tag tag : Tag.values()) {
			if (tag.tagLabel.equals(label)) {
				return tag;
			}
		}
		throw new IllegalArgumentException("Unknown tag in XML.");
	}
	private final String tagLabel;

	Tag(String tagLabel) {
		this.tagLabel = tagLabel;
	}

	/**
	 * @return the tagLabel
	 */
	public String getTagLabel() {
		return tagLabel;
	}
}

enum TagAttribute {
	LINK_TYPE("type");

	public static TagAttribute fromTagLabel(String label) {
		for (TagAttribute tagAttribute : TagAttribute.values()) {
			if (tagAttribute.tagLabel.equals(label)) {
				return tagAttribute;
			}
		}
		throw new IllegalArgumentException("Unknown tag in XML.");
	}
	private final String tagLabel;

	TagAttribute(String tagLabel) {
		this.tagLabel = tagLabel;
	}

	/**
	 * @return the tagLabel
	 */
	public String getTagLabel() {
		return tagLabel;
	}
}

/**
 *
 * @author John Nein
 * @version Oct 18, 2011
 */
public class XmlParser {

	private static Color c1 = new Color(45, 180, 255);
	private static Color c2 = new Color(232, 164, 74);
	private static Color c3 = new Color(255, 215, 65);
	private static Color c4 = new Color(255, 120, 55);
	private static Color c5 = new Color(232, 67, 59);
	private static Color c6 = new Color(84, 153, 181);
	private static Color c7 = new Color(60, 181, 181);
	private static Color c8 = new Color(93, 52, 181);
	private static Color c9 = new Color(188, 92, 181);
	private static Color c10 = new Color(90, 232, 152);
	private static Color c11 = new Color(76, 181, 36);
	private static Color c12 = new Color(255, 181, 62);
	public static Map<String, ChapterProperties> parseChapterProperties() {
		NodeList nodeList = getListOfAll(Tag.CHAPTER_TYPE);
		Map<String, ChapterProperties> chapterProperties = new HashMap<String, ChapterProperties>();
		if (nodeList == null) {
			System.err.println("Could not import node list");
			return chapterProperties;
		}
		for (int i = 0; i < nodeList.getLength(); i++) {
			// Declare temp variables to store parsed information
			String chapterTitle, chapterDescription;
			Color chapterColor = null;
			chapterTitle = chapterDescription = null;

			NodeList nodeChildList = nodeList.item(i).getChildNodes();
			for (int p = 0; p < nodeChildList.getLength(); p++) {

				org.w3c.dom.Node nodeChild = nodeChildList.item(p);
				if (ignoreNodeType(nodeChild.getNodeType())) {
					continue;
				}
				switch (Tag.fromTagLabel(nodeChild.getNodeName())) {
					case TITLE :
						chapterTitle = nodeChild.getTextContent();
						break;
					case DESCRIPTION :
						chapterDescription = nodeChild.getTextContent();
						break;
					case COLOR :
						try {
							//Field field = Color.class.getField(nodeChild
//							.getTextContent());
					String color = (String) nodeChild
							.getTextContent();
					//chapterColor = (Color) field.get(null);
					
//					System.out.println(color);
					if (color.equalsIgnoreCase("pink"))
					{
						chapterColor = c1;
					}
					else if (color.equalsIgnoreCase("tan"))
					{
						chapterColor = c2;
					}
					else if (color.equalsIgnoreCase("yellow"))
					{
						chapterColor = c3;
					}
					else if (color.equalsIgnoreCase("orange"))
					{
						chapterColor = c4;
					}
					else if (color.equalsIgnoreCase("red"))
					{
						chapterColor = c5;
					}
					else if (color.equalsIgnoreCase("lightBlue"))
					{
						chapterColor = c6;
					}
					
					else if (color.equalsIgnoreCase("teal"))
					{
						chapterColor = c7;
					}
					else if (color.equalsIgnoreCase("darkBlue"))
					{
						chapterColor = c8;
					}
					else if (color.equalsIgnoreCase("purple"))
					{
						chapterColor = c9;
					}
					
					else if (color.equalsIgnoreCase("seafoam"))
					{
						chapterColor = c10;
					}
					else if (color.equalsIgnoreCase("green"))
					{
						chapterColor = c11;
					}
					
					else if (color.equalsIgnoreCase("gold"))
					{
						chapterColor = c12;
					}
						} catch (Exception e) {
							throw new IllegalArgumentException(String.format(
									"Invalid color specified (%s)", nodeChild
											.getTextContent()));
						}
						break;
					default :
						break;
				}
			}
			chapterProperties.put(chapterTitle, new ChapterProperties(
					chapterColor, chapterDescription));
		}

		return chapterProperties;
	}

	public static Map<String, LinkProperties> parseLinkProperties() {
		NodeList nodeList = getListOfAll(Tag.LINK_TYPE);
		Map<String, LinkProperties> linkProperties = new HashMap<String, LinkProperties>();
		if (nodeList == null) {
			System.err.println("Could not import node list");
			return linkProperties;
		}
		for (int i = 0; i < nodeList.getLength(); i++) {
			// Declare temp variables to store parsed information
			String linkTitle, linkDescription, linkLineType;
			Color linkColor = null;
			linkTitle = linkDescription = linkLineType = null;

			NodeList nodeChildList = nodeList.item(i).getChildNodes();
			for (int p = 0; p < nodeChildList.getLength(); p++) {

				org.w3c.dom.Node nodeChild = nodeChildList.item(p);
				if (ignoreNodeType(nodeChild.getNodeType())) {
					continue;
				}
				switch (Tag.fromTagLabel(nodeChild.getNodeName())) {
					case TITLE :
						linkTitle = nodeChild.getTextContent();
						break;
					case DESCRIPTION :
						linkDescription = nodeChild.getTextContent();
						break;
					case LINE_TYPE :
						linkLineType = nodeChild.getTextContent();
						break;
					case COLOR :
						try {
							Field field = Color.class.getField(nodeChild
									.getTextContent());
							linkColor = (Color) field.get(null);
						} catch (Exception e) {
							throw new IllegalArgumentException(String.format(
									"Invalid color specified (%s)", nodeChild
											.getTextContent()));
						}
						break;
					default :
						break;
				}
			}
			linkProperties.put(linkTitle, new LinkProperties(LinkLineType
					.valueOf(linkLineType), linkColor, linkDescription));
		}

		return linkProperties;
	}

	public static NodeMap parseNodeInformation() {
		NodeList nodeList = getListOfAll(Tag.NODE);
		if (nodeList == null) {
			System.err.println("Could not import node list");
			return new NodeMap();
		}
		//List<Node> parsedNodeList = new ArrayList<Node>(nodeList.getLength());
		NodeMap parsedNodeMap = new NodeMap();
		for (int i = 0; i < nodeList.getLength(); i++) {
			// Declare temp variables to store parsed information
			String nodeTitle, nodeDescription, nodeContentUrl, nodeChapter;
			nodeTitle = nodeDescription = nodeChapter = nodeContentUrl = null;

			// Get all tagged information within each NODE element
			NodeList nodeChildList = nodeList.item(i).getChildNodes();
			for (int p = 0; p < nodeChildList.getLength(); p++) {
				org.w3c.dom.Node nodeChild = nodeChildList.item(p);
				if (ignoreNodeType(nodeChild.getNodeType())) {
					continue;
				}
				switch (Tag.fromTagLabel(nodeChild.getNodeName())) {
					case CHAPTER :
						nodeChapter = nodeChild.getTextContent();
						break;
					case PAGE :
						nodeContentUrl = nodeChild.getTextContent();
						break;
					case TITLE :
						nodeTitle = nodeChild.getTextContent();
						break;
					case DESCRIPTION :
						nodeDescription = nodeChild.getTextContent();
						break;
					default :
				}
			}
			Node newNode = new Node(nodeTitle, nodeDescription, nodeChapter);
			newNode.setNodeContentUrl(nodeContentUrl);
			parsedNodeMap.addNode(nodeChapter, newNode);
		}
		return parsedNodeMap;
	}

	public static List<Link> parseNodeLinks(List<Node> nodesToLink) {
		if (nodesToLink == null || nodesToLink.size() <= 1) {
			return null;
		}
		NodeList nodeList = getListOfAll(Tag.NODE);
		List<Link> linkList = new LinkedList<Link>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			// Declare temp variables
			String nodeTitle;
			nodeTitle = null;

			// Get all tagged information within each NODE element
			NodeList nodeChildList = nodeList.item(i).getChildNodes();
			for (int p = 0; p < nodeChildList.getLength(); p++) {
				org.w3c.dom.Node nodeChild = nodeChildList.item(p);
				if (ignoreNodeType(nodeChild.getNodeType())) {
					continue;
				}
				switch (Tag.fromTagLabel(nodeChild.getNodeName())) {
					case TITLE :
						nodeTitle = nodeChild.getTextContent();
						break;
					case LINK :
						// If the title has been found, link the nodes
						if (nodeTitle != null) {
							for (Node node1 : nodesToLink) {
								if (node1.getNodeTitle().equals(nodeTitle)) {
									for (Node node2 : nodesToLink) {
										if (node2.getNodeTitle().equals(
												nodeChild.getTextContent())) {
											String linkType = nodeChild
													.getAttributes()
													.getNamedItem(
															TagAttribute.LINK_TYPE
																	.getTagLabel())
													.getNodeValue();
											Node.link(node1, node2, linkType);
											break;
										}
									}
									break;
								}
							}
						} else {
							throw new IllegalArgumentException(
									"Node links declared before title in XML.");
						}
						break;
					default :
						break;
				}
			}
		}
		return linkList;
	}
	/**
	 * @param nodeType
	 * @return
	 */
	private static boolean ignoreNodeType(short nodeType) {
		return nodeType == org.w3c.dom.Node.COMMENT_NODE
				|| nodeType == org.w3c.dom.Node.TEXT_NODE;
	}

	private static NodeList getListOfAll(Tag tag) {
		Element documentElement = getNodeDocument();
		if (documentElement == null) {
			return null;
		}
		// Get all NODE elements
		NodeList nodeList = documentElement.getElementsByTagName(tag
				.getTagLabel());
		if (nodeList == null || nodeList.getLength() <= 0) {
			throw new IllegalArgumentException(
					"Invalid number of nodes found in content XML document.");
		}
		return nodeList;
	}

	private static Element getNodeDocument() {
		return AccessController.doPrivileged(new PrivilegedAction<Element>() {
			@Override
			public Element run() {
				// Get document builder factory
				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				try {
					DocumentBuilder db;
					db = dbf.newDocumentBuilder();
					Document xmlFile = db.parse(Configuration
							.getNodesFilePath());
					return xmlFile.getDocumentElement();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		});
	}
}
