/*   FILE: JPanelView.java
 *   DATE OF CREATION:  Mon Jun 19 18:37:59 2006
 *   AUTHOR :           Ruben Kleiman (rk@post.harvard.edu)
 *   MODIF:             Ruben Kleiman (rk@post.harvard.edu)
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: JPanelView.java 2142 2009-06-29 08:11:31Z epietrig $
 */ 

package fr.inria.zvtm.engine;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import fr.inria.zvtm.engine.ViewEventHandler;

/**
 * @author Ruben Kleiman (rk@post.harvard.edu)
 * 
 * A ZVTM view implemented as a JPanel rather than as a JFrame. This view supports standard and OpenGL rendering.
 */

public class JPanelView extends View  implements KeyListener {

    /**
     * The JFrame in which this view exists.
     */
    private JFrame frame;
	
    /**
     * The JPanel containing the view.
     */
    private JPanel viewContainerPanel;

    /**
     * The parent panel for the viewPanel.
     */
    private JPanel parentPanel;

    /**
     * Creates a view implemented as a JPanel.
     * @param cameraList	Initial list of cameras.
     * @param name	The view's name.
     * @param panelWidth	The width of the view's panel.
     * @param panelHeight	The height of the view's panel.
     * @param visible	Whether the view should initially be visible.
     * @param decorated	Whether the view should initially be decorated.
     * @param viewType	One of <code>View.STD_VIEW</code>, <code>View.OPENGL_VIEW</code>,
     * or <code>View.VOLATILE_VIEW</code>.
     * @param parentPanel	This is the parent panel for this view. A JPanel
     * presenting this view will be created as a child of this panel.
     * If the parent is <code>null</code>, the frame's content panel
     * will be used as the parent.
     * @param frame	The frame in which this panel will be created.
     * (This is to be compatible with the <code>View</code> API.)
     */
    public JPanelView(Vector cameraList, String name, int panelWidth, int panelHeight,
		      boolean visible, boolean decorated,
		      short viewType, JPanel parentPanel,
		      JFrame frame) {

	checkArgs(cameraList, name, viewType, parentPanel, frame);
		
	this.frame = frame;
	this.parentPanel = parentPanel;
	if (parentPanel == null) {
	    parentPanel = (JPanel) frame.getContentPane();
	}

	this.mouse = new VCursor(this);
	this.name = name;
	this.detectMultipleFullFills = VirtualSpaceManager.INSTANCE.defaultMultiFill;

	initCameras(cameraList);

	this.viewContainerPanel = new JPanel();
	viewContainerPanel.setLayout(new BoxLayout(viewContainerPanel, BoxLayout.Y_AXIS));
	viewContainerPanel.setAlignmentY(Component.TOP_ALIGNMENT);
	viewContainerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

	panel = makePanel(viewType, cameraList);
	panel.setSize(panelWidth, panelHeight);

	viewContainerPanel.add(panel);
	parentPanel.add(viewContainerPanel);

	viewContainerPanel.addKeyListener(this);
	viewContainerPanel.setVisible(visible);
	viewContainerPanel.setFocusable(true);
    }

    private ViewPanel makePanel(short viewType, Vector cameraList) {
	switch (viewType) {
	case View.STD_VIEW: return new StdViewPanel(cameras, this, true);
	case View.OPENGL_VIEW: return new GLViewPanel(cameras, this, true);
	default: throw new IllegalArgumentException("Invalid view type");
	}
    }

    private void checkArgs(Vector cameraList, String name, short viewType, JPanel parentPanel, JFrame frame) {
	if ((frame == null) && (parentPanel == null)) {
	    throw new IllegalArgumentException("Failed to provide parentPanel");
	}
	if ((viewType < View.STD_VIEW) || (viewType > View.OPENGL_VIEW)) {
	    throw new IllegalArgumentException("Invalid viewType");
	}
	if ((name == null) || (name.length() == 0)) {
	    throw new IllegalArgumentException("Failed to provide name");
	}
	if ((cameraList == null) || (cameraList.size() == 0)) {
	    throw new IllegalArgumentException("Failed to provide at least one camera in list");
	}
    }

    //    @Override
    public void destroyView() {
	panel.stop();
	VirtualSpaceManager.INSTANCE.destroyView(this.name);
	parentPanel.remove(viewContainerPanel);
    }

    //    @Override
    public Container getFrame() {
	return frame;
    }

    //    @Override
    /**
     * Requests the this view receive the focus.
     * Focus is granted if the view's window
     * is active.
     */
    public void requestFocus() {
	if (this.frame.isActive()) {
	    this.viewContainerPanel.requestFocusInWindow();
	}
    }

    //    @Override
    public boolean isSelected() {
	return (this.frame == VirtualSpaceManager.INSTANCE.activeJFrame);
    }

    //    @Override
    /**
     * Sets the title of this view's window.
     */
    public void setTitle(String title) {
	frame.setTitle(title);
    }

    //    @Override
    /**
     * Sets the location of this view's panel.
     */
    public void setLocation(int x, int y) {
	this.viewContainerPanel.setLocation(x, y);
    }

    //    @Override
    /**
     * Sets the size of this view's panel.
     */
    public void setSize(int x,int y) {
	this.viewContainerPanel.setSize(x,y);
    }

    //    @Override
    /**
     * Determines whether this view's window may be
     * resized or not.
     */
    public void setResizable(boolean b) {
	frame.setResizable(b);
    }

//     @Override
    /**
     * This will make the view's presentation panel
     * invisible--not the window in which the
     * panel exists.
     * @param	visible	If <code>true</code>, the
     * view's presentation becomes invisible and
     * the view is deactivated.
     */
    public void setVisible(boolean visible) {
	viewContainerPanel.setVisible(visible);
	if (visible) {
	    this.activate();
	} else {
	    this.deactivate();
	}
    }

//     @Override
    /**
     * Brings this view's window to the front.
     */
    public void toFront() {
	frame.toFront();
    }

//     @Override
    /**
     * Sends this view's window to the back.
     */
    public void toBack() {
	frame.toBack();
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
