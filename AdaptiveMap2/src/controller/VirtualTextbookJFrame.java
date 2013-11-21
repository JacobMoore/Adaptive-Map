/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import network.DataRetriever;
import view.AppCanvas;

/**
 *
 * @author Shawn
 */
public class VirtualTextbookJFrame extends JFrame {
    
    private static final String HOST = "http://www.adaptivemap.ma.psu.edu";
    private static final int PORT = 10001;    

    public static void main(String[] args) {
        DataRetriever retriever = new DataRetriever(HOST, PORT);
        try {
            retriever.connect();
        } catch (IOException ex) {
            Logger.getLogger(VirtualTextbookJFrame.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        try {
            retriever.downloadData();
        } catch (IOException ex) {
            Logger.getLogger(VirtualTextbookJFrame.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(2);
        }
        
        System.out.println("downloaded files succesfully");
        
        VirtualTextbookJFrame frame = new VirtualTextbookJFrame();
        
        frame.setVisible(true);
    }
    
    private AppCanvas canvas;
    private BufferedImage startImage;
    private boolean showStart;
    
    public VirtualTextbookJFrame() {
        super();
        Configuration.runLocally = true;
        init();
       
    }
    
    
    private void init() {/*
        this.setLayout(new BorderLayout());
        VirtualSpaceManager vspace =  VirtualSpaceManager.INSTANCE;
        vspace.getAnimationManager().start();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        canvas = new AppCanvas(vspace, panel, null);
        SizeChangedListener listener = new SizeChangedListener();
        addComponentListener(listener);
					
        System.out.println("HI");
        try {
                URL url = new URL(Configuration.getServerFolder() + "startImage.jpg");
                System.out.println("Made url");
                //startImage = ImageIO.read(url);	
                System.out.println("HI");
        } catch (Exception e) {
                System.out.println("UH OH");
                e.printStackTrace();
                System.out.println("UH OH");
        }
        canvas.validate();	
        this.add(panel, BorderLayout.CENTER);
        System.out.println("HI");*/
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        VirtualTextbookApplet applet = new VirtualTextbookApplet();
        this.add(applet, BorderLayout.CENTER);
        applet.start();
        applet.init();
        this.setSize(700, 700);
    }
    /*
    public void hideStartImage()
	{
		showStart = false;
	}
    
    public void paint(Graphics g)
	{
		super.paint(g);
		if (showStart)
			g.drawImage(startImage, getWidth()/2-startImage.getWidth()/2, 25, null);
	}
*/
private class SizeChangedListener implements ComponentListener {
        @Override public void componentHidden( ComponentEvent e ) {}
        @Override public void componentMoved( ComponentEvent e ) {}
        @Override
        public void componentResized( ComponentEvent e ) {
            canvas.setToolSizes();
           VirtualTextbookJFrame.this.revalidate();
           VirtualTextbookJFrame.this.repaint();
           canvas.repaint();
        }
        @Override
        public void componentShown( ComponentEvent e ) {
            canvas.setToolSizes();
            VirtualTextbookJFrame.this.revalidate();
           VirtualTextbookJFrame.this.repaint();
           canvas.repaint();

        } 
    }
}
