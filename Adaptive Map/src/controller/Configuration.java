package controller;

import java.io.File;
import java.awt.Color;

/**
 *
 * @author John Nein
 * @version Oct 5, 2011
 */
@SuppressWarnings("javadoc")
public class Configuration {
	public static final Color NODE_TITLE_COLOR = Color.BLACK;
	public static final Color NODE_DESCRIPTION_COLOR = Color.BLACK;
	public static final Color APPLICATION_BG_COLOR = Color.WHITE;
	public static final String APPLICATION_TITLE = "Virtual Textbook";
	
	public static final int MAX_CHARS_PER_LINE = 30;
	public static final int NODE_PADDING = 2;
	public static final int MIN_NODE_WIDTH = 350;
	public static final int MAX_LINK_WIDTH = 50;
	public static final int LINK_FONT_SIZE = 20;

	public static final int GRID_BUFFER_SPACE = 300;
	public static final int GRID_COLUMN_WIDTH = (int) (MIN_NODE_WIDTH * 1.5);
	public static final int GRID_ROW_HEIGHT = 150;
    
    public static final boolean USE_FIXED_NODE_POSITIONS = true;
    public static final boolean USE_LOCAL_PATH = true;
	public static final int GRAPHVIZ_BUFFER_LIMIT = 4096;
	
	public static boolean RUN_AS_APPLET = true;

    /**
     * Returns the path to the nodes.xml file.
     * @param useLocal
     * 			If the location is local, or on the server.
     * @return
     * 			The path to the file.
     */
	public static final String getXMLFilePath(final boolean useLocal) {
		 if ( useLocal )
		 {
			 String currentDirPath = (new File(".")).getAbsolutePath();
			 currentDirPath = currentDirPath.substring(0, currentDirPath
			 .length() - 1);
			 return String.format("%scontent/nodes.xml", currentDirPath);
		 }
		 else {
			 return "http://adaptivemap.me.vt.edu/AdaptiveMap/content/nodes.xml";
		 }
	}
	
	public static final String getServerFolder()
	{
		return "http://adaptivemap.me.vt.edu/AdaptiveMap/";
	}
	
	/**
	 * Returns the path to the folder where saved GraphViz data is stored.
     * @param useLocal
     * 			If the location is local, or on the server.
     * @return
     * 			The path to the file.
	 */
	public static final String getDataFilePath(final boolean useLocal) {
		 if ( useLocal )
		 {
			 String currentDirPath = (new File(".")).getAbsolutePath();
			 currentDirPath = currentDirPath.substring(0, currentDirPath
			 .length() - 1);
			 return String.format("%sgraphvizData", currentDirPath);
		 }
		 else
			 return "http://adaptivemap.me.vt.edu/AdaptiveMap/content/graphvizData";
	}

}
