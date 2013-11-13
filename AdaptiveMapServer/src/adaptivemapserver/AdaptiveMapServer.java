/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package adaptivemapserver;

import util.ServerEventListener;
import file.ContentDirectory;
import file.FileSender;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import util.DirectoryChangeListener;
import util.Utilities;

/**
 *
 * @author Shawn
 */
public class AdaptiveMapServer implements DirectoryChangeListener {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        AdaptiveMapServer server;
        try {
            server = new AdaptiveMapServer();
        } catch(IOException ex) {
            // port blocked
            JOptionPane.showMessageDialog(null, "Couldn't open server on port: " + Configuration.getServerPort());
            return;
        }
        
        ContentDirectory directory;
        try {
            directory = new ContentDirectory(Configuration.getContentFilePath());
            directory.start();
        } catch(IOException ex) {
            // could not access files
            JOptionPane.showMessageDialog(null, ex.getMessage());
            return;
        }
        
        ServerGUI gui = new ServerGUI(server);
        gui.setContentDirectory(directory);
        
        server.addServerListener(gui);
        server.setContentDirectory(directory);
        
        gui.setVisible(true);
        server.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook()));
    }
    
    private ServerSocket ss;
    private ContentDirectory directory;
    
    private ArrayList<ServerEventListener> listeners;
    
    private static final File keyFile = new File(Configuration.getApplicationDirectoryPath() + "serverKey");
    private static int serverKey;
    
    public AdaptiveMapServer() throws IOException {
        ss = new ServerSocket(Configuration.getServerPort());
        listeners = new ArrayList<>();
        readServerKey();
    }
    
    public void start() {
        Runnable r = new ServerRunnable();
        new Thread(r).start();
    }
    
    public void stop() throws IOException {
        ss.close();
    }
    
    public void setContentDirectory(ContentDirectory directory) {
        this.directory = directory;
        directory.addDirectoryChangeListener(this);
    }
    
    private class ServerRunnable implements Runnable {
        @Override
        public void run() {
            notifyOfAction("Server ready to accept connections");
            
            while(!ss.isClosed()) {
                Socket socket = null;
                try {
                    socket = ss.accept();
                    notifyOfNewConnection(socket);
                    
                    File[] files = directory.getFiles();
                    send(files, socket);
                    
                    notifyOfAction("Finished sending content. Closing session");
                    socket.close();
                    notifyOfAction("Session succesfully closed");
                } catch (IOException ex) {
                    Logger.getLogger(AdaptiveMapServer.class.getName()).log(Level.SEVERE, null, ex);
                    if(socket != null)
                        try {
                        socket.close();
                    } catch (IOException ex1) {}
                    notifyOfAction("Session closed due to error. See event log.");
                }
                
            }
        }
        
    }
    
    public  void addServerListener(ServerEventListener listener) {
        listeners.add(listener);
    }
    
    public void removeServerListener(ServerEventListener listener) {
        listeners.remove(listener);
    }
    
    public void notifyOfNewConnection(Socket socket) {
        for(ServerEventListener listener: listeners) {
            listener.onServerAccapt(socket);
        }
    }
    
    public void notifyOfAction(String action) {
        for(ServerEventListener listener: listeners) {
            listener.onServerAction(action);
        }
    }

    @Override
    public void directoryChanged() {
        serverKey++;
    }
    
    private void readServerKey() {
        try {
            String key = Utilities.readFromFile(keyFile);
            serverKey = Integer.parseInt(key);
            serverKey++;
        } catch (Exception ex) {
            try {
                ex.printStackTrace();
                serverKey = 1000;
                
                File dir = new File(keyFile.getAbsolutePath().replace(keyFile.getName(), ""));
                dir.mkdirs();
                keyFile.createNewFile();
                
                Utilities.writeToFile("" + serverKey, keyFile);
            } catch (IOException ex1) {
                JOptionPane.showMessageDialog(null, "Cannot create server key file. Check application permissions and try again.");
                System.exit(1);
            }
        }
    }
    
    public int getServerKey() {
        return serverKey;
    }
    
    private void send(File[] files, Socket socket) throws IOException {
        FileSender sender = new FileSender(files, socket);
        sender.sendFiles(serverKey);
    }
    
    private static class ShutdownHook implements Runnable {
        @Override
        public void run() {
            try {
                Utilities.writeToFile("" + serverKey, keyFile);
            } catch (IOException ex) {
                Logger.getLogger(AdaptiveMapServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
