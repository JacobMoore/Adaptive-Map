/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package file;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.DirectoryChangeListener;
/**
 *
 * @author Shawn
 */
public class ContentDirectory {
    
    private ArrayList<DirectoryChangeListener> listeners;
    
    private WatchService service;
    private boolean scanning = false;
    private File dir;
    
    public ContentDirectory(String directoryPath) throws IOException {
        super();
        listeners = new ArrayList();
        
        try {
            service = FileSystems.getDefault().newWatchService();
        } catch (IOException ex) {
            throw new IOException("Cannot access file system. Check application permissions");
        }
        dir = new File(directoryPath);
        Path path = dir.toPath();
        try {
            path.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        } catch (IOException ex) {
            dir.mkdirs();
            try {
                path.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            } catch(IOException ex1) {
                service.close();
                throw ex1;
            }
        }
    }
    
    public void start() {
        scanning = true;
        new Thread(new DirectoryScan()).start();
    }
    
    private class DirectoryScan implements Runnable {

        @Override
        public void run() {
            while(scanning) {
                try {
                    WatchKey take = service.take();
                    onDirectoryChange();
                    for(WatchEvent event: take.pollEvents()) {
                        String name = event.kind().name();
                        System.out.println(name);
                    }
                    take.reset();
                } catch(InterruptedException ex) {}
            }
            System.out.println("hiy");
            try {
                service.close();
            } catch (IOException ex) {
                Logger.getLogger(ContentDirectory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    public void addDirectoryChangeListener(DirectoryChangeListener listener) {
        listeners.add(listener);
    }
    
    public void removeDirectoryChangeListener(DirectoryChangeListener listener) {
        listeners.remove(listener);
    }
    
    public void onDirectoryChange() {
        for(DirectoryChangeListener listener: listeners) {
            listener.directoryChanged();
        }
    }
    
    public File[] getFiles() {
        return getFiles(dir);
    }
    
    private File[] getFiles(File dir) {
        ArrayList<File> list = new ArrayList();
        
        File[] listFiles = dir.listFiles();
        for(File file: listFiles) {
            if(file.isDirectory()) {
                File[] moreFiles = getFiles(file);
                for(File f: moreFiles) {
                    list.add(f);
                }
            } else {
                list.add(file);
            }
        }
        
        return list.toArray(new File[list.size()]);
    }
}
