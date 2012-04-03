package controller;

import java.awt.Dimension;
import javax.swing.JFrame;
import view.AppCanvas;
import fr.inria.zvtm.engine.VirtualSpaceManager;

/**
 *
 * @author John Nein
 * @version Sep 28, 2011
 */
public class Main {

	private static final VirtualSpaceManager vSpaceManager = VirtualSpaceManager.INSTANCE;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new AppCanvas(vSpaceManager, getDevelopmentFrame(), null);
	}

	public static JFrame getDevelopmentFrame() {
		JFrame developmentWindow = new JFrame();
		developmentWindow.setVisible(true);
		developmentWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		developmentWindow.setTitle(Configuration.APPLICATION_TITLE);
		developmentWindow.setSize(new Dimension(800, 600));
		developmentWindow.setCursor(new java.awt.Cursor(
				java.awt.Cursor.CROSSHAIR_CURSOR));

		return developmentWindow;
	}

}
