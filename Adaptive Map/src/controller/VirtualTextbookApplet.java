package controller;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;
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
	private static VirtualSpaceManager vSpaceManager;
	private static AppCanvas canvas;

    BufferedImage startImage = null;

	@Override
	public void init() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					// Line below is for ZVTM support for applets
					getRootPane().putClientProperty(
							"defeatSystemEventQueueCheck", Boolean.TRUE);
					vSpaceManager = VirtualSpaceManager.INSTANCE;
					canvas = new AppCanvas(vSpaceManager, VirtualTextbookApplet.this,
						    getAppletContext());
			        try {
						startImage = 
								ImageIO.read(new URL(Configuration.getServerFolder() + "startImage.jpg"));
					} catch (Exception e) {
						e.printStackTrace();
					}
					canvas.validate();
				}
			});
		} catch (Exception e) {
			System.err.println("createGUI didn't complete successfully");
			e.printStackTrace();
		}
	}
	
	@Override
	public void destroy() {
		vSpaceManager = null;
	}
	
	public void paint(Graphics g)
	{
		super.paint(g);
        g.drawImage(startImage, getWidth()/2-startImage.getWidth()/2, 25, null);
	}
}
