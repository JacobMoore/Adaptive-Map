/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import controller.VirtualTextbookApplet;
import controller.VirtualTextbookJFrame;
import java.awt.BorderLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.swing.JFrame;
import view.Browser;
/**
 *
 * @author Shawn
 */
public class AdaptiveMap2FX extends Application {
    
    public void start (Stage stage) {
        System.setProperty("java.awt.headless", "false");
        VirtualTextbookJFrame.main(null);
    }


    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //launch(args);
        VirtualTextbookJFrame.main(null);
    }
    
}
