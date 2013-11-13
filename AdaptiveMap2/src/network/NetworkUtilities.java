/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import controller.Configuration;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.JOptionPane;

/**
 *
 * @author Shawn
 */
public class NetworkUtilities {
    
    private NetworkUtilities(){}
    
    private static final File keyFile = new File(Configuration.getApplicationDirectory() + "serverKey");
    
    public static int readServerKeyFile() throws FileNotFoundException {
        Scanner scanner = new Scanner(keyFile);
        return scanner.nextInt();
    }
    
    public static void writeToServerKeyFile(int key) throws IOException {
        try {
            String keyString = "" + key;
            FileWriter write = new FileWriter(keyFile);
            write.write(keyString);
            write.close();
        } catch(FileNotFoundException ex) {
            try { 
                File dir = new File(keyFile.getAbsolutePath().replace(keyFile.getName(), ""));
                dir.mkdirs();
                keyFile.createNewFile();
                writeToServerKeyFile(key);
            } catch(IOException ex1) {
                JOptionPane.showMessageDialog(null, "Couldn't create server key file. Check application permissions and try again");
            }
        }
    }
}
