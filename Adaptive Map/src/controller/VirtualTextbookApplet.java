package controller;

import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JApplet;
import javax.swing.SwingUtilities;

import model.Node;
import view.AppCanvas;
import fr.inria.zvtm.engine.VirtualSpaceManager;

/**
 *
 * @author John Nein
 * @version Nov 15, 2011
 */
public class VirtualTextbookApplet extends JApplet {

	private static final long serialVersionUID = 4637779516193337728L;
	private VirtualSpaceManager vSpaceManager;
	private AppCanvas canvas;
	
	private boolean showStart;
	SizeChangedListener listener;
    BufferedImage startImage;

	@Override
	public void init() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					// Line below is for ZVTM support for applets
					getRootPane().putClientProperty(
							"defeatSystemEventQueueCheck", Boolean.TRUE);
					showStart = true;
					startImage = null;
					vSpaceManager = VirtualSpaceManager.INSTANCE;
					vSpaceManager.getAnimationManager().start();
					canvas = new AppCanvas(vSpaceManager, VirtualTextbookApplet.this,
						    getAppletContext());
					listener = new SizeChangedListener();
			        addComponentListener(listener);
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
	public void stop() {
		Node.destroyAllLinks();
		vSpaceManager.getActiveView().destroyView();
		vSpaceManager.destroyVirtualSpace(Configuration.APPLICATION_TITLE);
		removeAll();
		removeComponentListener(listener);
	}
	
	@Override
	public void destroy() {
	}
	
	public void paint(Graphics g)
	{
		super.paint(g);
		if (showStart)
			g.drawImage(startImage, getWidth()/2-startImage.getWidth()/2, 25, null);
	}
	
	/**
	 * Hides the starting image.
	 */
	public void hideStartImage()
	{
		showStart = false;
	}
	
	private class SizeChangedListener implements ComponentListener {
        @Override public void componentHidden( ComponentEvent e ) {}
        @Override public void componentMoved( ComponentEvent e ) {}
        @Override
        public void componentResized( ComponentEvent e ) {
            canvas.setToolSizes();
        }
        @Override
        public void componentShown( ComponentEvent e ) {
            canvas.setToolSizes();
        } 
    }
}
