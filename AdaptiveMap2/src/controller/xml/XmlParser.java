package controller.xml;

import java.awt.Color;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
	KEYWORDS("keywords"),
	PAGE("page"),
	LINK("link"),
	LINK_TYPE("linktype"),
	LINE_TYPE("linetype"),
	COLOR("color"),
	CHAPTER_TYPE("chaptertype"),
	XPOS("xvalue"),
	YPOS("yvalue"),
	DEFAULT("default"),
	DEFAULTCHAPTER("defaultChapter"),
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

	private static Color c1 = new Color(255, 192, 203);
	private static Color c2 = new Color(210, 180, 140);
	private static Color c3 = new Color(255, 255, 0);
	private static Color c4 = new Color(255, 165, 0);
	private static Color c5 = new Color(255, 0, 0);
	private static Color c6 = new Color(173, 216, 230);
	private static Color c7 = new Color(0, 128, 128);
	private static Color c8 = new Color( 0, 0, 139);
	private static Color c9 = new Color(128, 0, 128);
	private static Color c10 = new Color(161, 218, 199);
	private static Color c11 = new Color( 0, 128, 0);
	private static Color c12 = new Color(255, 215, 0);

	/**
	 * Goes through the xml file and creates a map of chapter titles to chapter properties.
	 * @return map of chapter titles to chapter properties.
	 */
	public static Map<String, ChapterProperties> parseChapterProperties() {
		NodeList nodeList = getListOfAll(Tag.CHAPTER_TYPE);
		Map<String, ChapterProperties> chapterProperties = new HashMap<String, ChapterProperties>();
		if (nodeList == null) {
			System.err.println("Could not import node list");
			return chapterProperties;
		}
		for (int i = 0; i < nodeList.getLength(); i++) {
			// Declare temp variables to store parsed information
			String chapterTitle, chapterDescription, defaultNode, chapterKeywords = "EMPTY";
			int x = -99999, y = -99999;
			Color chapterColor = null;
			boolean defaultChapter = false;
			chapterTitle = chapterDescription = defaultNode = null;

			NodeList nodeChildList = nodeList.item(i).getChildNodes();
			for (int p = 0; p < nodeChildList.getLength(); p++) {

				org.w3c.dom.Node nodeChild = nodeChildList.item(p);
				if (ignoreNodeType(nodeChild.getNodeType())) {
					continue;
				}
				switch (Tag.fromTagLabel(nodeChild.getNodeName())) {
				case TITLE:
					chapterTitle = nodeChild.getTextContent();
					break;
				case DESCRIPTION:
					chapterDescription = nodeChild.getTextContent();
					break;
				case KEYWORDS:
					chapterKeywords = nodeChild.getTextContent();
					break;
				case COLOR:
					try {
						String color = (String) nodeChild.getTextContent();

						if (color.equalsIgnoreCase("pink"))
							chapterColor = c1;
						else if (color.equalsIgnoreCase("tan"))
							chapterColor = c2;
						else if (color.equalsIgnoreCase("yellow"))
							chapterColor = c3;
						else if (color.equalsIgnoreCase("orange"))
							chapterColor = c4;
						else if (color.equalsIgnoreCase("red"))
							chapterColor = c5;
						else if (color.equalsIgnoreCase("lightBlue"))
							chapterColor = c6;
						else if (color.equalsIgnoreCase("teal"))
							chapterColor = c7;
						else if (color.equalsIgnoreCase("darkBlue"))
							chapterColor = c8;
						else if (color.equalsIgnoreCase("purple"))
							chapterColor = c9;
						else if (color.equalsIgnoreCase("seafoam"))
							chapterColor = c10;
						else if (color.equalsIgnoreCase("green"))
							chapterColor = c11;
						else if (color.equalsIgnoreCase("gold"))
							chapterColor = c12;
						else{
							Scanner in = new Scanner(color).useDelimiter(", ");
							int red = Integer.parseInt(in.next());
							int green = Integer.parseInt(in.next());
							int blue = Integer.parseInt(in.next());
							chapterColor = new Color(red, green, blue);
							in.close();
						}
					} catch (Exception e) {
						throw new IllegalArgumentException(String.format(
								"Invalid color specified (%s)",
								nodeChild.getTextContent()));
					}
					break;
				case XPOS:
					x = Integer.parseInt(nodeChild.getTextContent());
					break;
				case YPOS:
					y = Integer.parseInt(nodeChild.getTextContent());
					break;
				case DEFAULT:
					defaultNode = nodeChild.getTextContent();
					break;
				case DEFAULTCHAPTER:
					defaultChapter = Boolean.parseBoolean
						(nodeChild.getTextContent());
					break;
				default:
					break;
				}
			}
			if(chapterTitle != null && chapterColor != null && chapterDescription != null
					&& chapterKeywords != null && defaultNode != null) {
				if (Configuration.USE_FIXED_NODE_POSITIONS) {
					chapterProperties.put(chapterTitle, new ChapterProperties(
							chapterColor, chapterDescription, chapterKeywords, x, y, defaultNode, defaultChapter));
				} else {
					chapterProperties.put(chapterTitle, new ChapterProperties(
							chapterColor, chapterDescription, chapterKeywords, 0, 0, defaultNode, defaultChapter));
				}
			}
		}

		return chapterProperties;
	}

	/**
	 * Goes through the xml file and creates a map of link type names to link properties.
	 * @return map of link type names to link properties.
	 */
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
			if(linkTitle != null && linkLineType != null && linkColor != null && linkDescription != null)
			linkProperties.put(linkTitle, new LinkProperties(LinkLineType
					.valueOf(linkLineType), linkColor, linkDescription));
		}

		return linkProperties;
	}

	/**
	 * Goes through the xml file and creates all nodes.
	 * @param map 
	 * 		The nodeMap for these nodes.
	 * @param fontSize 
	 * 		The size of the node's font.
	 */
	public static void parseNodeInformation(NodeMap map, int fontSize) {
		NodeList nodeList = getListOfAll(Tag.NODE);
		if (nodeList == null) {
			System.err.println("Could not import node list");
			return;
		}
		//List<Node> parsedNodeList = new ArrayList<Node>(nodeList.getLength());
		for (int i = 0; i < nodeList.getLength(); i++) {
			// Declare temp variables to store parsed information
			String nodeTitle, nodeDescription, nodeContentUrl, nodeChapter, nodeKeywords = "EMPTY";
			nodeTitle = nodeDescription = nodeChapter = nodeContentUrl = null;
			int x = -99999, y = -99999;

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
					case KEYWORDS:
						nodeKeywords = nodeChild.getTextContent();
						break;
					case XPOS:
						x = Integer.parseInt(nodeChild.getTextContent());
						break;
					case YPOS:
						y = Integer.parseInt(nodeChild.getTextContent());
						break;
					default :
				}
			}
			if(nodeTitle != null && nodeDescription != null && nodeChapter != null && nodeKeywords != null
					&& nodeContentUrl != null ) {
				Node newNode = new Node(nodeTitle, nodeDescription, nodeChapter, 
						nodeKeywords, map.getChapterType(nodeChapter).getChapterColor(), fontSize);
				if ( nodeContentUrl.startsWith("http") )
					nodeContentUrl.replaceAll("&amp;", "&");
				
				newNode.setNodeContentUrl(nodeContentUrl);
				if (x != -99999 && y != -99999)
					newNode.setFixedNodePosition(x, y);
				map.addNode(nodeChapter, newNode);
			}
		}
	}

	/**
	 * Goes through the xml file and sets up links for all given nodes.
	 * @param nodesToLink
	 * 				List of all nodes to link
	 * @return list of all created links
	 */
	public static List<Link> parseNodeLinks(List<Node> nodesToLink) {
		if (nodesToLink == null || nodesToLink.size() <= 1) {
			return null;
		}
		NodeList nodeList = getListOfAll(Tag.NODE);
		List<Link> linkList = new LinkedList<Link>();
		String nodeTitle;
		for (int i = 0; i < nodeList.getLength(); i++) {
			// Declare temp variables
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
											Node.link(node1, node2, linkType, 20, false);
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
					Document xmlFile = db.parse(Configuration.getXMLFilePath());
					return xmlFile.getDocumentElement();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		});
	}
}
