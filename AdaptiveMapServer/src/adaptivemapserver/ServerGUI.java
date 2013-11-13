/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package adaptivemapserver;

import file.ContentDirectory;
import util.ServerEventListener;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import util.DirectoryChangeListener;

/**
 *
 * @author Shawn
 */
public class ServerGUI extends JFrame implements ServerEventListener, DirectoryChangeListener {
    
    private AdaptiveMapServer server;
    private JTextArea logArea;
    private JButton closeServerButton;
    private JLabel serverKeyLabel;
    
    public ServerGUI(AdaptiveMapServer server) {
        super("Server Log");
        this.server = server;
        init();
    }
    
    private void init() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        serverKeyLabel = new JLabel();
        updateServerKeyLabel();
        
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        
        closeServerButton = new JButton("Close Serner");
        closeServerButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    server.stop();
                } catch (IOException ex) {
                    // some problem happened when closing the server
                    // we are going t ignore it
                }
                System.exit(1);
            }
        });
        
        JScrollPane scroller = new JScrollPane(logArea);
        
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(scroller, BorderLayout.CENTER);
        this.getContentPane().add(closeServerButton, BorderLayout.SOUTH);
        this.getContentPane().add(serverKeyLabel, BorderLayout.LINE_END);
        
        this.setSize(300, 300);
    }

    @Override
    public void onServerAccapt(Socket accepted) {
        logArea.append("Connected: " + accepted.getInetAddress().getHostAddress() + "\n");
    }

    @Override
    public void onServerAction(String action) {
        logArea.append(action + "\n");
    }

    public void setContentDirectory(ContentDirectory directory) {
        directory.addDirectoryChangeListener(this);
    }

    @Override
    public void directoryChanged() {
        updateServerKeyLabel();
    }
    
    private void updateServerKeyLabel() {
        serverKeyLabel.setText("ServerKey=" + server.getServerKey());
    }
}
