package controller;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

enum Tag {
	// Node XML tags
	TEXTBOOK("textbook"), NODE("node"), TITLE("title"), DESCRIPTION(
			"description"), CHAPTER("chapter"), PAGE("page"), LINK("link"), LINK_TYPE(
			"linktype"), LINE_TYPE("linetype"), COLOR("color"), CHAPTER_TYPE(
			"chaptertype"), XPOS("xvalue"), YPOS("yvalue"), DEFAULT("default"), DEFAULTCHAPTER(
			"defaultChapter"), KEYWORDS("keywords"),
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
	 **/
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
	 **/
	public String getTagLabel() {
		return tagLabel;
	}
}

/**
 * 
 * @author Michel Pascale
 * 
 */
public class EditorXMLParser {
	public static LinkedList<ChapterData> chapterData;
	public static LinkedList<LinkData> linkData;
	public static LinkedList<NodeData> nodeData;

	public static void initializeLists() {
		nodeData = new LinkedList<NodeData>();
		chapterData = new LinkedList<ChapterData>();
		linkData = new LinkedList<LinkData>();
	}

	public static LinkedList<String> getChapterColors() {
		LinkedList<String> names = new LinkedList<String>();
		names.add("Pink");
		names.add("Tan");
		names.add("Yellow");
		names.add("Orange");
		names.add("Red");
		names.add("LightBlue");
		names.add("Teal");
		names.add("DarkBlue");
		names.add("Purple");
		names.add("Seafoam");
		names.add("Green");
		names.add("Gold");
		return names;
	}

	public static void parseChapterProperties(final File file) {
		NodeList nodeList = getListOfAll(Tag.CHAPTER_TYPE, file);
		chapterData = new LinkedList<ChapterData>();
		if (nodeList == null) {
			System.err.println("Could not import node list");
			return;
		}
		for (int i = 0; i < nodeList.getLength(); i++) {
			// Declare temp variables to store parsed information
			String chapterTitle, chapterDescription, defaultNode, chapterKeywords = "EMPTY";
			int x = -99999, y = -99999;
			String chapterColor = null;
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
						chapterColor = (String) nodeChild.getTextContent();
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
					defaultChapter = Boolean.parseBoolean(nodeChild
							.getTextContent());
					break;
				default:
					break;
				}
			}
			chapterData.add(new ChapterData(chapterTitle, chapterColor,
					chapterDescription, chapterKeywords, x, y, defaultNode, defaultChapter));
		}
	}

	public static void parseLinkProperties(final File file) {
		NodeList nodeList = getListOfAll(Tag.LINK_TYPE, file);
		linkData = new LinkedList<LinkData>();
		if (nodeList == null) {
			System.err.println("Could not import node list");
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
				case TITLE:
					linkTitle = nodeChild.getTextContent();
					break;
				case DESCRIPTION:
					linkDescription = nodeChild.getTextContent();
					break;
				case LINE_TYPE:
					linkLineType = nodeChild.getTextContent();
					break;
				case COLOR:
					try {
						Field field = Color.class.getField(nodeChild
								.getTextContent());
						linkColor = (Color) field.get(null);
					} catch (Exception e) {
						throw new IllegalArgumentException(String.format(
								"Invalid color specified (%s)",
								nodeChild.getTextContent()));
					}
					break;
				default:
					break;
				}
			}
			linkData.add(new LinkData(linkTitle, linkColor, linkDescription,
					linkLineType));
		}
	}

	public static void parseNodeInformation(final File file) {
		NodeList nodeList = getListOfAll(Tag.NODE, file);
		if (nodeList == null) {
			System.err.println("Could not import node list");
			return;
		}
		// List<Node> parsedNodeList = new
		// ArrayList<Node>(nodeList.getLength());
		nodeData = new LinkedList<NodeData>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			// Declare temp variables to store parsed information
			String nodeTitle, nodeDescription, nodeContentUrl, nodeChapter, nodeKeywords = "EMPTY";
			LinkedList<Link> linkList = new LinkedList<Link>();
			nodeTitle = nodeDescription = nodeChapter = nodeContentUrl = null;

			// Get all tagged information within each NODE element
			NodeList nodeChildList = nodeList.item(i).getChildNodes();
			for (int p = 0; p < nodeChildList.getLength(); p++) {
				org.w3c.dom.Node nodeChild = nodeChildList.item(p);
				if (ignoreNodeType(nodeChild.getNodeType())) {
					continue;
				}
				switch (Tag.fromTagLabel(nodeChild.getNodeName())) {
				case CHAPTER:
					nodeChapter = nodeChild.getTextContent();
					break;
				case PAGE:
					nodeContentUrl = nodeChild.getTextContent();
					break;
				case TITLE:
					nodeTitle = nodeChild.getTextContent();
					break;
				case DESCRIPTION:
					nodeDescription = nodeChild.getTextContent();
					break;
				case KEYWORDS:
					nodeKeywords = nodeChild.getTextContent();
					break;
				case LINK:
					// If the title has been found, link the nodes
					if (nodeTitle != null) {
						String linkType = nodeChild
								.getAttributes()
								.getNamedItem(
										TagAttribute.LINK_TYPE.getTagLabel())
								.getNodeValue();
						linkList.add(new Link(nodeChild.getTextContent(),
								linkType));
						break;
					}
					break;
				default:
				}
			}
			nodeData.add(new NodeData(nodeTitle, nodeChapter, nodeDescription,
					nodeContentUrl, nodeKeywords));
			nodeData.getLast().linkList.addAll(linkList);
		}
	}

	public static void save(File location) {
		try {
			if (location.exists())
				location.delete();
			location.createNewFile();
			FileWriter output = new FileWriter(location);
			output.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
			output.write("<textbook>\n\n");
			output.write("\t<!-- Links -->\n\n");
			for (LinkData l : linkData) {
				output.write("\t<linktype>\n");
				output.write("\t\t<title>" + l.linkTitle + "</title>\n");
				output.write("\t\t<description>" + l.description
						+ "</description>\n");
				output.write("\t\t<linetype>" + l.linkLineType
						+ "</linetype>\n");
				output.write("\t\t<color>" + "black" + "</color>\n");
				output.write("\t</linktype>\n\n");
				output.flush();
			}

			output.write("\t<!-- Chapters -->\n\n");
			for (ChapterData c : chapterData) {
				output.write("\t<chaptertype>\n");
				output.write("\t\t<title>" + c.chapterTitle + "</title>\n");
				output.write("\t\t<description>" + c.description
						+ "</description>\n");
				output.write("\t\t<keywords>" + c.keywords + "</keywords>\n");
				output.write("\t\t<color>" + c.chapterColor + "</color>\n");
				output.write("\t\t<default>" + c.defaultNode + "</default>\n");
				output.write("\t\t<defaultChapter>"
						+ String.valueOf(c.isDefaultChapter)
						+ "</defaultChapter>\n");
				output.write("\t\t<xvalue>" + c.fixedXPosition + "</xvalue>\n");
				output.write("\t\t<yvalue>" + c.fixedYPosition + "</yvalue>\n");
				output.write("\t</chaptertype>\n\n");
				output.flush();
			}

			output.write("\t<!-- Nodes -->\n\n");
			for (NodeData n : nodeData) {
				output.write("\t<node>\n");
				output.write("\t\t<title>" + n.nodeTitle + "</title>\n");
				output.write("\t\t<description>" + n.nodeDescription
						+ "</description>\n");
				output.write("\t\t<chapter>" + n.nodeChapter + "</chapter>\n");
				output.write("\t\t<page>" + n.nodeWebpage.replace("&", "&amp;")
						+ "</page>\n");
				output.write("\t\t<keywords>" + n.nodeKeywords + "</keywords>\n");
				for (Link l : n.linkList)
					output.write("\t\t<link type=\"" + l.linkType + "\">"
							+ l.linkedNode + "</link>\n");
				output.write("\t</node>\n\n");
				output.flush();
			}
			output.write("</textbook>");
			output.close();
		} catch (IOException e) {
			System.err.println("Failure saving file.");
			e.printStackTrace();
		}
	}

	/**
	 * @param nodeType
	 * @return
	 */
	private static boolean ignoreNodeType(short nodeType) {
		return nodeType == org.w3c.dom.Node.COMMENT_NODE
				|| nodeType == org.w3c.dom.Node.TEXT_NODE;
	}

	private static NodeList getListOfAll(Tag tag, final File file) {
		Element documentElement = getNodeDocument(file);
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

	private static Element getNodeDocument(final File file) {
		return AccessController.doPrivileged(new PrivilegedAction<Element>() {
			@Override
			public Element run() {
				// Get document builder factory
				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				try {
					DocumentBuilder db;
					db = dbf.newDocumentBuilder();
					Document xmlFile = db.parse(file.getAbsolutePath());
					return xmlFile.getDocumentElement();
				} catch (Exception e) {
					e.printStackTrace();
					Main.showXMLError();
				}
				return null;
			}
		});
	}

	/**
	 * Defines a chapter's properties (color and description).
	 */
	public static class ChapterData {
		public String chapterTitle;
		public String chapterColor;
		public String description;
		public String keywords;
		public int fixedXPosition;
		public int fixedYPosition;
		public String defaultNode;
		public boolean isDefaultChapter;

		/**
		 * Create a new ChapterProperties object.
		 * 
		 * @param chapterColor
		 *            The color of the chapter.
		 * @param description
		 *            The description of the chapter.
		 */
		public ChapterData(String chapterTitle, String chapterColor,
				String description, String keywords, int x, int y, String node, boolean isDefault) {
			this.chapterTitle = chapterTitle;
			this.chapterColor = chapterColor;
			this.description = description;
			this.keywords = keywords;
			fixedXPosition = x;
			fixedYPosition = y;
			defaultNode = node;
			isDefaultChapter = isDefault;
		}
	}

	/**
	 * Represents all the properties of a link.
	 */
	public static class LinkData {
		public String linkTitle;
		public String linkLineType;
		public Color linkColor;
		public String description;

		/**
		 * Create a new LinkProperties object.
		 * 
		 * @param linkColor
		 *            The color of the link.
		 * @param description
		 *            The description for this link.
		 */
		public LinkData(String linkTitle, Color linkColor, String description,
				String linkLineType) {
			this.linkTitle = linkTitle;
			this.linkColor = linkColor;
			this.description = description;
			this.linkLineType = linkLineType;
		}
	}

	public static class NodeData {
		public String nodeTitle;
		public String nodeChapter;
		public String nodeDescription;
		public String nodeWebpage;
		public String nodeKeywords;
		public LinkedList<Link> linkList;

		public NodeData(String nodeTitle, String nodeChapter,
				String nodeDescription, String nodeWebpage, String nodeKeywords) {
			this.nodeTitle = nodeTitle;
			this.nodeChapter = nodeChapter;
			this.nodeDescription = nodeDescription;
			this.nodeWebpage = nodeWebpage;
			this.nodeKeywords = nodeKeywords;
			this.linkList = new LinkedList<Link>();
		}
	}

	public static class Link {
		public String linkedNode;
		public String linkType;

		public Link(String linkedNode, String linkType) {
			this.linkedNode = linkedNode;
			this.linkType = linkType;
		}
	}
}
