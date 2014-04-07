/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import network.DataRetriever;

/**
 *
 * @author Shawn
 */
public class VirtualTextbookJFrame extends JFrame {
    
    private static final String HOST = "128.118.245.131";
    private static final int PORT = 10001;    

    public static void main(String[] args) {
        DataRetriever retriever = new DataRetriever(HOST, PORT);
        try {
            retriever.connect();
            retriever.downloadData();
        } catch (Exception ex) {
            Logger.getLogger(VirtualTextbookJFrame.class.getName()).log(Level.SEVERE, null, ex);
            handleConnectException();
        }
        
        System.out.println("downloaded files succesfully");
                VirtualTextbookJFrame frame = new VirtualTextbookJFrame();
                System.out.println("Created frame");
                frame.setVisible(true);
     
    }
    
    public VirtualTextbookJFrame() {
        super();
        init();
    }
    
    
    private void init() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        Rectangle bounds = getPrefferedBounds();
        this.setBounds(bounds);
        
        VirtualTextbookApplet applet = new VirtualTextbookApplet();
        this.add(applet, BorderLayout.CENTER);
        applet.init();
    }
    
    private static void handleConnectException() {
        int n = JOptionPane.showConfirmDialog(null, 
                "Problem connecting to server\n"
                + "Run in offline mode?", 
                "Connect Exception",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        
        if(n == JOptionPane.NO_OPTION || n == JOptionPane.CLOSED_OPTION)
            System.exit(2);
    }
    
    private Rectangle getPrefferedBounds() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        
        int xBuffer = screenWidth / 8;
        int yBuffer = screenHeight / 8;
        
        int x = xBuffer;
        int y = yBuffer;
        int width = screenWidth - (2 * xBuffer);
        int height = screenHeight - (2 * yBuffer);
        
        Rectangle bounds = new Rectangle(x, y, width, height);
        return bounds;
    }

    
}