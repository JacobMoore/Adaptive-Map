/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Shawn
 */
public class Utilities {
    
    public static String readFromFile(File file) throws FileNotFoundException {
        String content = "";
        
        Scanner scanner = new Scanner(file);
        while(scanner.hasNextLine()) {
            content += scanner.nextLine();
            if(scanner.hasNextLine())
                content += "\n";
        }
        
        scanner.close();
        
        return content;
    }
    
    public static void writeToFile(String content, File file) throws IOException {
        FileWriter fw = new FileWriter(file);
        fw.write(content);
        fw.close();
    }
}
