/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package adaptivemapserver;

/**
 *
 * @author Shawn
 */
public final class Configuration {
    
    public static String getApplicationDirectoryPath() {
        return System.getProperty("user.home") + "\\VirtualTextbookServer\\";
    }
    
    public static String getContentFilePath() {
        return getApplicationDirectoryPath() + "content\\";
    }
    
    public static int getServerPort() {
        return 10001;
    }
}
