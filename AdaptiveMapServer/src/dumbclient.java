
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Shawn
 */
public class dumbclient {
    
    public static void main(String[] args) throws Exception {
        
        Socket socket = new Socket("127.0.0.1", 10001);
        
        BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter os = new PrintWriter(socket.getOutputStream());
        
        System.out.println("Made streams");
        
        System.out.println(is.readLine());
        
        os.println("bad");
        os.flush();
        String line;
        while((line = is.readLine())!=null) {
            String readLine = line.replace("filepath=", "");
            System.out.println(readLine);
            os.println("OKAY");
            os.flush();
            File file = new File(getFilePath() + readLine);
            File dir = new File(file.getAbsolutePath().replace(file.getName(), ""));
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
            
            FileWriter fw = new FileWriter(file);
            fw.write(content);
            fw.close();
            
        }
        System.out.println("Finished");
        socket.close();
    }
    
    
    private static void p(String string) {
        System.out.println(string);
    }
    
    private static String getFilePath() {
        return System.getProperty("user.home") + "/VirtualClient/";
    }
}
