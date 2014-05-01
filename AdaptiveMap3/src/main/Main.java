/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package main;

import controller.Configuration;
import controller.VirtualTextbookJFrame;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import network.DataRetriever;
import textbook.Textbook;
import textbook.TextbookXMLParser;

/**
 *
 * @author Shawn
 */
public class Main {
    
    private static final String HOST = "128.118.245.131";
    private static final int PORT = 10001;    

    private static Textbook textbook;
    
    public static void main(String[] args) {
        downloadData();
        createTextbook();
        
        System.out.println("downloaded files succesfully");
                VirtualTextbookJFrame frame = new VirtualTextbookJFrame();
                System.out.println("Created frame");
                frame.setVisible(true);
     
    }
    
    private static void downloadData() {
        DataRetriever retriever = new DataRetriever(HOST, PORT);
        try {
            retriever.connect();
            retriever.downloadData();
        } catch (Exception ex) {
            Logger.getLogger(VirtualTextbookJFrame.class.getName()).log(Level.SEVERE, null, ex);
            handleConnectException();
        }
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
    
    private static void createTextbook() {
        File textbookDataFile = new File(Configuration.getDataFilePath());
        textbook = TextbookXMLParser.parse(textbookDataFile);
    }
}
