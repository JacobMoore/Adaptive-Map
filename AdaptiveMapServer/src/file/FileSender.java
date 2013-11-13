/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package file;

import adaptivemapserver.Configuration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author Shawn
 */
public class FileSender {
    
    private File[] files;
    private Socket socket;
    private PrintWriter os;
    private BufferedReader is;
    
    public FileSender(File[] files, Socket socket) throws IOException {
        super();
        this.files = files;
        this.socket = socket;
        is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        os = new PrintWriter(socket.getOutputStream());
    }
    
    public void sendFiles(int serverKey) throws IOException {
        boolean checkKey = checkServerKey(serverKey);
        if(checkKey){
            System.out.println("Key good");
            return;
        }
        
        System.out.println("Continuing sending");
        
        sendFiles();
        
        System.out.println("Sneding files");
    }
    
    private boolean checkServerKey(int serverKey) throws IOException {
        //byte[] bytes = ("" + serverKey).getBytes();
        //System.out.println(new String(bytes));
        //os.write(bytes);
        os.println("serverKey=" + serverKey);
        os.flush();
        System.out.println("Wrote it");
        /*
        bytes = new byte[1024];
        is.read(bytes);
        String keyGood = new String(bytes);
        System.out.println(keyGood);*/
        
        String keyGood = is.readLine();
        System.out.println("Read it");
        System.out.println(keyGood);
        return keyGood.equals("GOOD");
    }
    
    private void sendFiles() throws IOException {
        for(File file: files) {
            sendFile(file);
        }
    }
    
    private void sendFile(File file) throws IOException {
        sendFilePath(file);
        sendFileContent(file);
    }
    
    private void sendFilePath(File file) throws IOException {
        String relativePath = file.getAbsolutePath().replace(Configuration.getContentFilePath(), "");
        os.println("filepath=" + relativePath);
        os.flush();
        System.out.println(relativePath);
        
        String response = is.readLine();
        System.out.println(response);
        if(!response.equals("OKAY"))
            sendFilePath(file);
    }
    
    private void sendFileContent(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);;
        
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            os.println(line);
            os.flush();
        }
        os.println("ENDOFTRANSFER");
        os.flush();
        System.out.println("Finished sending " + file.getName());
    }
}
