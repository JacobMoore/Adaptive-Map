/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import controller.Configuration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Shawn
 */
public class DataRetriever {
    
    private String host;
    private int port;
    
    private Socket socket;
    private BufferedReader is;
    private PrintWriter os;
    
    public DataRetriever(String host, int port) {
        super();
        this.host = host;
        this.port = port;
    }
    
    public void connect() throws IOException {
        socket = new Socket(host, port);
        is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        os = new PrintWriter(socket.getOutputStream());
    }
    
    public void downloadData() throws IOException {
        boolean keyGood = checkServerKey();
        
        if(keyGood) {
            System.out.println("Key good");
            return;
        }
        
        System.out.println("Key bad, retrieving files");
        
        File dir = new File(Configuration.getDataFilePath());
        delete(dir);
        downloadFiles();
    }

    private boolean checkServerKey() throws IOException {
        String read = is.readLine();
        int serverKey = Integer.parseInt(read.replace("serverKey=", "").trim());
        System.out.println(serverKey);
        
        String status;
        try {
            int myServerKey = NetworkUtilities.readServerKeyFile();
            if(myServerKey != serverKey) {
                NetworkUtilities.writeToServerKeyFile(serverKey);
                status = "BAD";
            } else {            
                status = "GOOD";
            }
        } catch(Exception ex) {
            NetworkUtilities.writeToServerKeyFile(serverKey);
            status = "BAD";
        }
        
        System.out.println(status);
        os.println(status);
        os.flush();
        System.out.println("Wrote status");
        
        return status.equals("GOOD");
    }
    
    private void downloadFiles() throws IOException {
        String line;
        while((line = is.readLine())!=null) {
            String readLine = line.replace("filepath=", "");
            System.out.println(readLine);
            os.println("OKAY");
            os.flush();
            File file = new File(Configuration.getDataFilePath() + readLine);
            File dir = file.getParentFile();
            dir.mkdirs();
            file.createNewFile();
            
            String content = "";
            while((line = is.readLine())!=null) {
                if(!content.equals(""))
                    content += "\n";
                
                if(!line.equals("ENDOFTRANSFER")) {
                    content += line;
                    
                }else
                    break;
            }
            System.out.println(content);
            FileWriter fw = new FileWriter(file);
            fw.write(content);
            fw.close();
        }
    }
    
    private void delete(File toDelete) {
        System.out.println("Deleting " + toDelete.getPath());
        if(toDelete.isDirectory()) {
            for(File file: toDelete.listFiles()) {
                delete(file);
            }
            toDelete.delete();
        } else {
            toDelete.delete();
        }
    }
}
