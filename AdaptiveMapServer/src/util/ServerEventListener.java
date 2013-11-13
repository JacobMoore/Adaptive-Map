/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.net.Socket;

/**
 *
 * @author Shawn
 */
public interface ServerEventListener {
    
    public void onServerAccapt(Socket accaepted);
    public void onServerAction(String action);
    
}
