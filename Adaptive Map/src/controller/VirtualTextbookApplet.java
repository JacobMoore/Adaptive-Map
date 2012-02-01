package controller;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;

import view.AppCanvas;
import fr.inria.zvtm.engine.VirtualSpaceManager;

/**
 * 
 * @author John Nein
 * @version Nov 15, 2011
 */
public class VirtualTextbookApplet extends JApplet {

	private static final long serialVersionUID = 4637779516193337728L;
	private static final VirtualSpaceManager vSpaceManager = VirtualSpaceManager.INSTANCE;

	@Override
	public void init() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					// Line below is for ZVTM support for applets
					getRootPane().putClientProperty(
							"defeatSystemEventQueueCheck", Boolean.TRUE);
					AppCanvas.appletContext = getAppletContext();
					new AppCanvas(vSpaceManager, VirtualTextbookApplet.this);
				}
			});
		} catch (Exception e) {
			System.err.println("createGUI didn't complete successfully");
			e.printStackTrace();
		}
	}
}
