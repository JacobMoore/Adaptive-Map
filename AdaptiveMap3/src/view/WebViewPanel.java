/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package view;

import java.awt.BorderLayout;
import java.net.URL;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javax.swing.JFrame;

/**
 *
 * @author Shawn
 */
public class WebViewPanel extends JFXPanel {
    
    public static void main(String[] args) throws InterruptedException {
        final WebViewPanel webview = new WebViewPanel();
        System.out.println("Made the panel");
        
        Thread.sleep(1000);
        System.out.println("init fx");
        JFrame frame = new JFrame();
        System.out.println("made frame");
        frame.setLayout(new BorderLayout());
        System.out.println("set layout");
        frame.add(webview, BorderLayout.CENTER);
        System.out.println("added webview");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        System.out.println("set close");
        frame.setVisible(true);
        System.out.println("frame visible");
    }
    
    private Browser browser;
    
    public WebViewPanel() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("Started platform run");
                WebViewPanel.this.initFX();
                 System.out.println("fx finished");
            }
        });
    }
    
    public void initFX() {
        browser = new Browser();
        Scene scene = new Scene(browser);
        this.setScene(scene);
    }
    
    public void navigateTo(final URL url) {
        System.out.println("Navigating to " + url);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
               browser.gotoPage(url);
            }
       });
        System.out.println("Navigated to " + url.getHost() + url.getPath());
       // while(doingFX){/*System.out.println("Doing fx");*/}
    }
}

