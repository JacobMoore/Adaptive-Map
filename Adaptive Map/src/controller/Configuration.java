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

	public static final Color NODE_BG_COLOR = Color.WHITE;
	public static final Color NODE_TITLE_COLOR = Color.BLUE;
	public static final Color NODE_DESCRIPTION_COLOR = Color.BLACK;
	public static final Color APPLICATION_BG_COLOR = Color.WHITE;
	public static final String APPLICATION_TITLE = "Virtual Textbook";
	public static final int FONT_SIZE = 14;
	public static final int GRID_BUFFER_SPACE = 100;
	// TODO can I get the actual max width of the nodes? for column width
	public static final int GRID_COLUMN_WIDTH = FONT_SIZE * 20;
	public static final int GRID_ROW_HEIGHT = 150;

	public static final String getNodesFilePath() {
		return AccessController.doPrivileged(new PrivilegedAction<String>() {
			@Override
			public String run() {
				 String currentDirPath = (new File(".")).getAbsolutePath();
				 currentDirPath = currentDirPath.substring(0, currentDirPath
				 .length() - 1);
				 return String.format("%scontent/nodes.xml", currentDirPath);
				//return "http://localhost:8080/content/nodes.xml";
			}
		});
	}

}
