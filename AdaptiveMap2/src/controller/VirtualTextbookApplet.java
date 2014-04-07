package controller;

import java.awt.event.ComponentEvent;
import javax.swing.SwingUtilities;

import model.Node;
import view.AppCanvas;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author John Nein
 * @version Nov 15, 2011
 */
public class VirtualTextbookApplet extends JPanel {

	private static final long serialVersionUID = 4637779516193337728L;
	private VirtualSpaceManager vSpaceManager;
	private AppCanvas canvas;
        private SizeChangedListener listener;
        
      //  public VirtualTextbookApplet() {
        //    init();
        //}
        
	//@Override
	public void init() {
		try {/*
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
                                    try {*/
					// Line below is for ZVTM support for applets
					//getRootPane().putClientProperty(
					//		"defeatSystemEventQueueCheck", Boolean.TRUE);
					vSpaceManager = VirtualSpaceManager.INSTANCE;
					vSpaceManager.getAnimationManager().start();
					canvas = new AppCanvas(vSpaceManager, (JFrame) SwingUtilities.getWindowAncestor(VirtualTextbookApplet.this));
					listener = new SizeChangedListener();
                                        addComponentListener(listener);
						//startImage = 
						//		ImageIO.read(new URL(Configuration.getServerFolder() + "startImage.jpg"));
					} catch (Exception e) {
						e.printStackTrace();
					}
					canvas.validate();
                                        /*
				}
			});
		} catch (Exception e) {
			System.err.println("createGUI didn't complete successfully");
			e.printStackTrace();
		}
                */
                System.out.println("Stuff" + canvas.getChapterList().toString());
	}
	
	//@Override
	public void stop() {
		Node.destroyAllLinks();
		vSpaceManager.getActiveView().destroyView();
		vSpaceManager.destroyVirtualSpace(Configuration.APPLICATION_TITLE);
		removeAll();
		removeComponentListener(listener);
	}
	
	private class SizeChangedListener extends ComponentAdapter {
            @Override
            public void componentResized( ComponentEvent e ) {
                canvas.setComponentPositions();
            }
            @Override
            public void componentShown( ComponentEvent e ) {
                canvas.setComponentPositions();
            } 
        }
       
}
