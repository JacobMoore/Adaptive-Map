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
	
	public static final int MIN_NODE_WIDTH = 200;
	public static final int LINK_FONT_SIZE = 20;
	public static int NODE_FONT_SIZE = 14;
	public static int CHAPTER_TITLE_FONT_SIZE = 100;
	public static int CHAPTER_DESCRIPTION_FONT_SIZE = 80;

	public static final int GRID_BUFFER_SPACE = 300;
	// TODO can I get the actual max width of the nodes? for column width
	public static final int GRID_COLUMN_WIDTH = NODE_FONT_SIZE * 12;
	public static final int GRID_ROW_HEIGHT = 150;

    // Variables for the minimum Camera height at different view levels.
	public static final int ZOOM_HEIGHT_PADDING = 5;
    public static final int ZOOM_NODE_HEIGHT = 1;
    public static final int ZOOM_CHAPTER_HEIGHT = 100;
    
    public static final int ZOOM_OVERVIEW_MIN = 100;
    public static int ZOOM_OVERVIEW_HEIGHT = 250;
    public static final int ZOOM_OVERVIEW_MAX = 500;
    
    public static final boolean USE_FIXED_NODE_POSITIONS = false;

	public static final String getNodesFilePath() {
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
