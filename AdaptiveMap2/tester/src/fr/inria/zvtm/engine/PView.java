/*   FILE: AppletView.java/PView.java
 *   DATE OF CREATION:   Dec 27 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Thu Feb 20 16:31:33 2003 by Emmanuel Pietriga
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2009. All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For full terms see the file COPYING.
 *
 * $Id: PView.java 2142 2009-06-29 08:11:31Z epietrig $
 */

package fr.inria.zvtm.engine;

import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import fr.inria.zvtm.engine.ViewEventHandler;

  /**
   * A PView (formerly AppletView) is based on a JPanel. It can be inserted anywhere in a Swing component hierarchy.
   * It can be composed of one or several cameras superimposed.
   * @author Emmanuel Pietriga
   **/

public class PView extends View implements KeyListener {

	/**
		*@param v list of cameras
		*@param t view name
		*@param panelWidth width of window in pixels
		*@param panelHeight height of window in pixels
		*/
	protected PView(Vector v,String t,int panelWidth,int panelHeight){
		mouse=new VCursor(this);
		name=t;
		detectMultipleFullFills = VirtualSpaceManager.INSTANCE.defaultMultiFill;
		initCameras(v);   //vector -> cast elements as "Camera"
		panel = new StdViewPanel(v, this, true);
		panel.setSize(panelWidth,panelHeight);
		panel.addKeyListener(this);
	}

    /**get the java.awt.Container for this view*/
    public Container getFrame(){return panel;}

    /**tells whether this frame is selected or not - not used*/
    public boolean isSelected(){
	return false;
    } 

    /**set the window location*/
    public void setLocation(int x,int y){}

    /**set the window title*/
    public void setTitle(String t){}

    /**set the window size*/
    public void setSize(int x,int y){}

    /**can the window be resized or not (no effect)*/
    public void setResizable(boolean b){}

    /**Shows or hides this view*/
    public void setVisible(boolean b){
    }

    /**Brings this window to the front. Places this window at the top of the stacking order and shows it in front of any other windows*/
    public void toFront(){}

    /**Sends this window to the back. Places this window at the bottom of the stacking order and makes the corresponding adjustment to other visible windows*/
    public void toBack(){}

    /**destroy this view*/
    public void destroyView(){
	panel.stop();
	//parent.destroyView(this.name);
	//frame.dispose();
    }

    /**detect key typed and send to application event handler*/
    public void keyTyped(KeyEvent e){
	if (e.isShiftDown()) {
	    if (e.isControlDown()) {panel.evHs[panel.activeLayer].Ktype(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_SHIFT_MOD, e);}
	    else {panel.evHs[panel.activeLayer].Ktype(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.SHIFT_MOD, e);}
	}
	else {
	    if (e.isControlDown()) {panel.evHs[panel.activeLayer].Ktype(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_MOD, e);}
	    else {panel.evHs[panel.activeLayer].Ktype(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.NO_MODIFIER, e);}
	}
    }

    /**detect key pressed and send to application event handler*/
    public void keyPressed(KeyEvent e){
	if (e.isShiftDown()) {
	    if (e.isControlDown()) {panel.evHs[panel.activeLayer].Kpress(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_SHIFT_MOD, e);}
	    else {panel.evHs[panel.activeLayer].Kpress(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.SHIFT_MOD, e);}
	}
	else {
	    if (e.isControlDown()) {panel.evHs[panel.activeLayer].Kpress(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_MOD, e);}
	    else {panel.evHs[panel.activeLayer].Kpress(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.NO_MODIFIER, e);}
	}
    }

    /**detect key released and send to application event handler*/
    public void keyReleased(KeyEvent e) {
	if (e.isShiftDown()) {
	    if (e.isControlDown()) {panel.evHs[panel.activeLayer].Krelease(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_SHIFT_MOD, e);}
	    else {panel.evHs[panel.activeLayer].Krelease(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.SHIFT_MOD, e);}
	}
	else {
	    if (e.isControlDown()) {panel.evHs[panel.activeLayer].Krelease(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_MOD, e);}
	    else {panel.evHs[panel.activeLayer].Krelease(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.NO_MODIFIER, e);}
	}
    }

}
