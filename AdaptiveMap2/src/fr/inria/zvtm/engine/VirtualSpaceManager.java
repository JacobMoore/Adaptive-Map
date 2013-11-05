/*   FILE: VirtualSpaceManager.java
 *   DATE OF CREATION:   Jul 11 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2008. All Rights Reserved
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
 * $Id: VirtualSpaceManager.java 3119 2010-03-31 13:24:56Z epietrig $
 */

package fr.inria.zvtm.engine;

import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JFrame;

import fr.inria.zvtm.animation.AnimationManager;
import fr.inria.zvtm.engine.Portal;
import fr.inria.zvtm.engine.RepaintListener;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;

/**
 * Virtual space manager. This is the main entry point to the toolkit. Virtual spaces, cameras, glyphs and views are instanciated from here.
 * @author Emmanuel Pietriga
 **/

public class VirtualSpaceManager implements AWTEventListener {

	/** called by VText */
	public void onMainFontUpdated(){
		for (int i=0;i<allViews.length;i++){
			allViews[i].updateFont();
		}
		Object g;
		for (Enumeration e=allVirtualSpaces.elements();e.hasMoreElements();){
    		for (Enumeration e2=((VirtualSpace)e.nextElement()).getAllGlyphs().elements();e2.hasMoreElements();){
    			g = e2.nextElement();
    			if (g instanceof VText){((VText)g).invalidate();}
    		}		    
		}
		repaintNow();
	}

	/**select only mouse sensitive and visible glyphs*/
	public static short VIS_AND_SENS_GLYPHS=0;
	/**select only visible glyphs*/ 
	public static short VISIBLE_GLYPHS=1;
	/**select only mouse sensitive glyphs*/
    public static short SENSITIVE_GLYPHS=2;
    /**select all glyphs in the region*/
    public static short ALL_GLYPHS=3;     

    /**print exceptions and warning*/
    static boolean debug = false;

    /**key is space name (String)*/
    protected Hashtable allVirtualSpaces;
    /**All views managed by this VSM*/
    protected View[] allViews;
    /**used to quickly retrieve a view by its name (gives its index position in the list of views)*/
    protected Hashtable name2viewIndex;

    /**View which has the focus (or which was the last to have it among all views)*/
    public View activeView;
    protected int activeViewIndex = -1;

    /**default policy for view repainting - true means all views are repainted even if ((not active) or (mouse not inside the view)) - false means only the active view and the view in which the mouse is currently located (if different) are repainted - default is true*/
    boolean generalRepaintPolicy=true;

    /**enables detection of multiple full fills in one view repaint - default value assigned to new views  - STILL VERY BUGGY - ONLY SUPPORTS VRectangle and VCircle for now - setting it to true will prevent some glyphs from being painted if they are not visible in the final rendering (because of occlusion). This can enhance performance (in configurations where occlusion does happen).*/
    boolean defaultMultiFill=false;

    /**value under which a VText is drawn as a segment instead of a text (considered too small to be read). Default is 0.5 - if you raise this value, text that was still displayed as a string will be displayed as a segment and inversely - of course, displaying a line instead of applying affine transformations to strings is faster*/
    float textAsLineCoef=0.5f;

    /**Animation Manager*/
    private final AnimationManager animationManager;
    
    public static final VirtualSpaceManager INSTANCE = new VirtualSpaceManager();
 
    /**
     * Automatic instantiation as a singleton. THere is always a single VSM per application.
     */
    private VirtualSpaceManager(){
		if (debug){System.out.println("Debug mode ON");}
		animationManager = new AnimationManager(this);
		allVirtualSpaces=new Hashtable();
		allViews = new View[0];
		name2viewIndex = new Hashtable();
	}

    /**set debug mode ON or OFF*/
    public static void setDebug(boolean b){
	debug=b;
    }

    /**get debug mode state (ON or OFF)*/
    public static boolean debugModeON(){return debug;}

    /**
     * Returns a reference to the AnimationManager associated
     * with this VirtualSpaceManager.
     */
    public AnimationManager getAnimationManager(){
	return animationManager;
    }

    /**set policy for view repainting - true means all views are repainted even if ((not active) or (mouse not inside the view)) - policy is forwarded to all existing views (no matter its current policy) and will be applied to future ones (but it can be changed for each single view)*/
    public void setRepaintPolicy(boolean b){
	if (b!=generalRepaintPolicy){
	    generalRepaintPolicy=b;
	    for (int i=0;i<allViews.length;i++){
		allViews[i].setRepaintPolicy(generalRepaintPolicy);
	    }
	}
    }

    /**get general policy for view repainting (this is the current default policy, but this does not guarantee that all views comply with it since the policy may be changed for each single view)*/
    public boolean getRepaintPolicy(){return generalRepaintPolicy;}

    /**enable/disable detection of multiple full fills in one view repaint - default value assigned to new views - default is false */
    public void setDefaultMultiFills(boolean b){
	defaultMultiFill=b;
    }

    /**get state of detection of multiple full fills in one view repaint - default value assigned to new views */
    public boolean getDefaultMultiFills(){
	return defaultMultiFill;
    }

    /**get active camera (in focused view) - null if no view is active*/
    public Camera getActiveCamera(){
	return (activeView != null) ? activeView.getActiveCamera() : null;
    }

    /* -------------- PORTALS ------------------ */

    /**add a portal to view v
     *@param p portal
     *@param v owning view
     */
    public Portal addPortal(Portal p, View v){
		return v.addPortal(p);
	}

    /**destroy a portal*/
    public void destroyPortal(Portal p){
		View v = p.getOwningView();
		v.removePortal(p);
	}

    /* ----------------- VIEWS ---------------- */

    /**create a new external view
     *@param c vector of cameras making this view (if more than one camera, cameras will be superimposed on different layers)
     *@param name view name
     *@param viewType one of View.STD_VIEW, View.VOLATILE_VIEW, View.OPENGL_VIEW - determines the type of view and acceleration method
     *@param w width of window in pixels
     *@param h height of window in pixels
     *@param bar true -&gt; add a status bar to this view (below main panel)
     *@param visible should the view be made visible automatically or not
     */
    public View addFrameView(Vector c, String name, short viewType, int w, int h, boolean bar, boolean visible){
	return addFrameView(c, name, viewType, w, h, bar, visible, null);
    }

    /**Create a new external view.<br>
     * The use of OPENGL_VIEW requires the following Java property: -Dsun.java2d.opengl=true
     *@param c vector of cameras making this view (if more than one camera, cameras will be superimposed on different layers)
     *@param name view name
     *@param viewType one of View.STD_VIEW, View.VOLATILE_VIEW, View.OPENGL_VIEW - determines the type of view and acceleration method
     *@param w width of window in pixels
     *@param h height of window in pixels
     *@param bar true -&gt; add a status bar to this view (below main panel)
     *@param visible should the view be made visible automatically or not
     *@param mnb a menu bar (null if none), already configured with ActionListeners already attached to items (it is just added to the view)
     *@see #addFrameView(Vector c, String name, short viewType, int w, int h, boolean bar, boolean visible, boolean decorated, JMenuBar mnb)
     */
    public View addFrameView(Vector c, String name, short viewType, int w, int h,
				boolean bar, boolean visible, JMenuBar mnb){
	return addFrameView(c, name, viewType, w, h, bar, visible, true, mnb);
    }
    
    /**Create a new external view.<br>
     * The use of OPENGL_VIEW requires the following Java property: -Dsun.java2d.opengl=true
     *@param c vector of cameras making this view (if more than one camera, cameras will be superimposed on different layers)
     *@param name view name
     *@param viewType one of View.STD_VIEW, View.VOLATILE_VIEW, View.OPENGL_VIEW - determines the type of view and acceleration method
     *@param w width of window in pixels
     *@param h height of window in pixels
     *@param bar true -&gt; add a status bar to this view (below main panel)
     *@param visible should the view be made visible automatically or not
     *@param decorated should the view be decorated with the underlying window manager's window frame or not
     *@param mnb a menu bar (null if none), already configured with ActionListeners already attached to items (it is just added to the view)
     *@see #addFrameView(Vector c, String name, short viewType, int w, int h, boolean bar, boolean visible, JMenuBar mnb)
     */
    public View addFrameView(Vector c, String name, short viewType, int w, int h,
				boolean bar, boolean visible, boolean decorated, JMenuBar mnb){
	View v = null;
	switch(viewType){
	case View.STD_VIEW:{
	    v = (mnb != null) ? new EView(c, name, w, h, bar, visible, decorated, mnb) : new EView(c, name, w, h, bar, visible, decorated);
	    addView(v);
	    v.setRepaintPolicy(generalRepaintPolicy);
	    break;
	}
	case View.OPENGL_VIEW:{
	    v = (mnb != null) ? new GLEView(c, name, w, h, bar, visible, decorated, mnb) : new GLEView(c, name, w, h, bar, visible, decorated);
	    addView(v);
	    v.setRepaintPolicy(generalRepaintPolicy);
	    break;
	}
	}
	return v;
    }

    /**create a new view embedded in a JPanel, suitable for inclusion in other Components including JApplet
     *@param c vector of cameras superimposed in this view
     *@param name view name
     *@param w width of window in pixels
     *@param h height of window in pixels
     */
    public JPanel addPanelView(Vector c,String name,int w,int h){
        PView tvi = new PView(c, name, w, h);
        addView(tvi);
        tvi.setRepaintPolicy(generalRepaintPolicy);
        return tvi.panel;
    }

    /**
     * Adds a newly created view to the list of existing views
     * Side-effect: attempts to start the animation manager
     */
    protected void addView(View v){
	View[] tmpA = new View[allViews.length+1];
	System.arraycopy(allViews, 0, tmpA, 0, allViews.length);
	tmpA[allViews.length] = v;
	allViews = tmpA;
	name2viewIndex.put(v.name, new Integer(allViews.length-1));
	animationManager.start(); //starts animationManager if not already running
    }

     /**
       * Creates an external view which presents itself
       * in a JPanel in a window (JFrame) provided by the client application (and which can contain other components).
	 * @param cameraList vector of cameras superimposed in this view
	 * @param name	View name. Since this view is
	 * not itself a window, this does not affect the
	 * window's title: use setTitle() for that.
	 * @param panelWidth	width of panel in pixels
	 * @param panelHeight	width of panel in pixels
	 * @param visible	should the view be made visible automatically or not
	 * @param decorated	should the view be decorated with the underlying window manager's window frame or not
	 * @param viewType	One of <code>View.STD_VIEW</code>,
	 * <code>View.OPENGL_VIEW</code>,
	 * or <code>View.VOLATILE_VIEW</code>.
	 * @param parentPanel	This is the parent panel for this view. A JPanel
	 * presenting this view will be created as a child of this panel.
	 * If the parent is <code>null</code>, the frame's content panel
	 * will be used as the parent.
	 * @param frame	The frame in which this panel will be created.
	 * (This is to be compatible with the <code>View</code> API.)
	 * @return	View	The created view.
	 */
    public View addFrameView(Vector cameraList, String name, int panelWidth, int panelHeight,
				boolean visible, boolean decorated, short viewType,
				JPanel parentPanel, JFrame frame) {
    	View v = new JPanelView(cameraList, name, panelWidth, panelHeight,
				visible, decorated, viewType,
				parentPanel, frame);
	addView(v);
	v.setRepaintPolicy(generalRepaintPolicy);
	return v;
     }

    /**Get view whose name is n (-1 if view does not exist).*/
    protected int getViewIndex(String n){
	try {
	    return ((Integer)name2viewIndex.get(n)).intValue();
	}
	catch (NullPointerException ex){return -1;}
    }

    /**Get view whose name is n (null if no match).*/
    public View getView(String n){
	int index = getViewIndex(n);
	if (index != -1){
	    return allViews[index];
	}
	else {
	    return null;
	}
    }

    /**Destroy a view identified by its index in the list of views.*/
    protected void destroyView(int i){
	View[] tmpA = new View[allViews.length-1];
	if (tmpA.length > 0){
	    System.arraycopy(allViews, 0, tmpA, 0, i);
	    System.arraycopy(allViews, i+1, tmpA, i, allViews.length-i-1);
	}
	allViews = tmpA;
	updateViewIndex();
    }

    /**update mapping between view name and view index in the list of views when
     * complex changes are made to the list of views (like removing a view)*/
    protected void updateViewIndex(){
	name2viewIndex.clear();
	for (int i=0;i<allViews.length;i++){
	    name2viewIndex.put(allViews[i].name, new Integer(i));
	}
    }

    /**Destroy a view.*/
    protected void destroyView(View v){
	for (int i=0;i<allViews.length;i++){
	    if (allViews[i] == v){
		destroyView(i);
		break;
	    }
	}
    }

    /**Destroy a view. 
     * Used internally - not available outside from package, you should call the method directly on the view itself*/
    protected void destroyView(String viewName){
	destroyView(getView(viewName));
    }

    /**Call this if you want to repaint all views at once.
     * In some cases it is not possible to detect graphical changes so repaint
     * calls have to be issued manually (unless you are willing to wait for
     * another event to trigger repaint).
     *@see #repaintNow(View v)
     *@see #repaintNow(View v, RepaintListener rl)
     */
    public void repaintNow(){
	for (int i=0;i<allViews.length;i++){
	    allViews[i].repaintNow();
	}
    }

    /**Call this if you want to repaint a given view at once.
     * In some cases it is not possible to detect graphical changes so repaint
     * calls have to be issued manually (unless you are willing to wait for
     * another event to trigger repaint).
     *@see #repaintNow()
     *@see #repaintNow(View v, RepaintListener rl)
     */
    public void repaintNow(View v){
	v.repaintNow();
    }

    /**Call this if you want to repaint a given view at once.
     * In some cases it is not possible to detect graphical changes so repaint
     * calls have to be issued manually (unless you are willing to wait for
     * another event to trigger repaint).
     *@param v the view to repaint
     *@param rl a repaint listener to be notified when this repaint cycle is completed (it must be removed manually if you are not interested in being notified about following repaint cycles)
     *@see #repaintNow(View v)
     *@see View#removeRepaintListener()
     */
    public void repaintNow(View v, RepaintListener rl){
	v.repaintNow(rl);
    }

    /**Call this if you want to repaint a given view at once. Internal use.
     * In some cases it is not possible to detect graphical changes so repaint
     * calls have to be issued manually (unless you are willing to wait for
     * another event to trigger repaint).
     *@param i view index in list of views
     */
    protected void repaintNow(int i){
	repaintNow(allViews[i]);
    }
    
    /**manually set active view*/
    public void setActiveView(View v){
	activeView=v;
	activeViewIndex = getViewIndex(v.getName());
    }

    /**get active view*/
    public View getActiveView(){
	return activeView;
    }

    /* ----------- VIRTUAL SPACE --------------- */

    /**create a new virtual space with name n*/
    public VirtualSpace addVirtualSpace(String n){
	VirtualSpace tvs=new VirtualSpace(n);
	allVirtualSpaces.put(n,tvs);
	return tvs;
    }

    /**destroy a virtual space*/
    public void destroyVirtualSpace(String n){
	if (allVirtualSpaces.containsKey(n)){
	    VirtualSpace vs=(VirtualSpace)(allVirtualSpaces.get(n));
	    vs.destroy();
	    allVirtualSpaces.remove(n);
	}
    }

    /**returns the virtual space owning glyph g*/
    public VirtualSpace getOwningSpace(Glyph g){
	VirtualSpace vs;
	for (Enumeration e=allVirtualSpaces.elements();e.hasMoreElements();){
	    vs = (VirtualSpace)e.nextElement();
	    if (vs.getAllGlyphs().contains(g)){return vs;}
	}
	return null;
    }

    /**get virtual space whose name is n*/
    public VirtualSpace getVirtualSpace(String n){
	return (VirtualSpace)(allVirtualSpaces.get(n));
    }

    /**get active virtual space (i.e., the space owning the camera currently active), null if no view is active*/
    public VirtualSpace getActiveSpace(){
	return (activeView != null) ? activeView.getActiveCamera().getOwningSpace() : null;
    }

    /* ----------- GLYPHS, CAMERAS, CURSOR --------------- */
    
    /**set the value under which a VText is drawn as a point instead of a text (considered too small to be read). Default is 0.5 (it is compared to the product of the font size by the projection value) - if you raise this value, more text that was still displayed as a string will be displayed as a segment and inversely - of course, displaying a line instead of applying affine transformations to strings is faster*/
    public void setTextDisplayedAsSegCoef(float f){
	textAsLineCoef=f;
    }

    /**set the value under which a VText is drawn as a point instead of a text (considered too small to be read). Default is 0.5 (it is compared to the product of the font size by the projection value)*/
    public float getTextDisplayedAsSegCoef(){
	return textAsLineCoef;
    }

    /**should not be used by applications - public because accessed by Glyphs themselves when made unsensitive*/
    public void removeGlyphFromUnderMouseLists(Glyph g){
	VirtualSpace vs=null;
	try {
	    for (Enumeration e=allVirtualSpaces.elements();e.hasMoreElements();){
		vs=(VirtualSpace)e.nextElement();
		if (vs.getAllGlyphs().contains(g)){break;}
	    }
	    Camera[] cl = vs.getCameraListAsArray();
	    for (int i=0;i<cl.length;i++){
		((View)(cl[i].getOwningView())).mouse.removeGlyphFromList(g);
	    }
	}
	catch (NullPointerException ex){}
    }
    
    Object activeJFrame=null;
    
    public void eventDispatched(AWTEvent e){
	if (e.getID() == WindowEvent.WINDOW_ACTIVATED){activeJFrame=e.getSource();}
    }

}



