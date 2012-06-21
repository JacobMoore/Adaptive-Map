package controller;

import java.io.File;
import java.awt.Color;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 *
 * @author John Nein
 * @version Oct 5, 2011
 */
public class Configuration {
	public static final Color NODE_TITLE_COLOR = Color.BLACK;
	public static final Color NODE_DESCRIPTION_COLOR = Color.BLACK;
	public static final Color APPLICATION_BG_COLOR = Color.WHITE;
	public static final String APPLICATION_TITLE = "Virtual Textbook";
	
	public static final int MAX_CHARS_PER_LINE = 30;
	public static final int MIN_NODE_WIDTH = 200;
	public static final int MAX_LINK_WIDTH = 50;
	public static final int LINK_FONT_SIZE = 20;
	public static int NODE_FONT_SIZE = 14;
	public static int CHAPTER_TITLE_FONT_SIZE = 100;
	public static int CHAPTER_DESCRIPTION_FONT_SIZE = 80;

	public static final int GRID_BUFFER_SPACE = 300;
	public static final int GRID_COLUMN_WIDTH = (int) (MIN_NODE_WIDTH * 1.5);
	public static final int GRID_ROW_HEIGHT = 150;

    // Variables for the minimum Camera height at different view levels.
    public static final int ZOOM_CHAPTER_HEIGHT = 100;
    public static final int ZOOM_OVERVIEW_MIN = 300;
    public static int ZOOM_OVERVIEW_HEIGHT = 400;
    public static final int ZOOM_OVERVIEW_MAX = 1000;
    
    public static final boolean USE_FIXED_NODE_POSITIONS = true;
    public static boolean SHOW_ALL_CHAPTER_CONNECTIONS = false;

	public static final String getXMLFilePath() {
		return AccessController.doPrivileged(new PrivilegedAction<String>() {
			@Override
			public String run() {
				 String currentDirPath = (new File(".")).getAbsolutePath();
				 currentDirPath = currentDirPath.substring(0, currentDirPath
				 .length() - 1);
				 return String.format("%scontent/nodes.xml", currentDirPath);
				 //return "http://adaptivemap.me.vt.edu/AdaptiveMap/content/nodes.xml";
			}
		});
	}

}
