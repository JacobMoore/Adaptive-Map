/*   FILE: View.java
 *   DATE OF CREATION:   Jul 11 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2007. All Rights Reserved
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
 * $Id: View.java 2741 2010-01-11 14:53:08Z epietrig $
 */

package fr.inria.zvtm.engine;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.JLabel;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.AnimationManager;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.engine.Portal;
import fr.inria.zvtm.engine.RepaintListener;
import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.lens.Lens;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.RectangularShape;

  /**
   * A view is a window and can be composed of one or several cameras superimposed - use EView or IView <BR>
   * A view is repainted on a regular basis when active - for inactive views, the default is to repaint only if the mouse is inside the view (but the frame is not selected) - this can be changed to repaint the view automatically even if it is not selected and if the mouse is not inside, using setRepaintPolicy()
   * @author Emmanuel Pietriga
   **/

public abstract class View {

    /**Standard ZVTM view, with no particular acceleration method*/
    public static final short STD_VIEW = 0;
    /**ZVTM view based on Java 5's OpenGL rendering pipeline; does accelerate rendering but requires a JVM 1.5 or later*/
    public static final short OPENGL_VIEW = 1;

    /**list of Camera objects used in this view*/
    Vector cameras;

    void initCameras(Vector c){
	cameras=c;
	for (int i=0;i<cameras.size();i++){
	    ((Camera)c.elementAt(i)).setOwningView(this);
	}
    }

    /**portals embedded in this view*/
    Portal[] portals = new Portal[0];
    
    /**add a portal to this view*/
    Portal addPortal(Portal p){
	Portal[] tmpP = new Portal[portals.length+1];
	System.arraycopy(portals, 0, tmpP, 0, portals.length);
	tmpP[portals.length] = p;
	portals = tmpP;
	p.setOwningView(this);
	return p;
    }

    /**remove a portal from this view*/
    void removePortal(Portal p){
	for (int i=0;i<portals.length;i++){
	    if (portals[i] == p){
		removePortalAtIndex(i);
		break;
	    }
	}
    }

    /**remove portal at index portalIndex in the list of portals*/
    void removePortalAtIndex(int portalIndex){
	Portal[] tmpP = new Portal[portals.length-1];
	System.arraycopy(portals, 0, tmpP, 0, portalIndex);
	System.arraycopy(portals, portalIndex+1, tmpP, portalIndex, portals.length-portalIndex-1);
	portals = tmpP;
	panel.resetCursorInsidePortals();
    }

    /**mouse glyph*/
    public VCursor mouse;

	/** Returns this view's cursor object. */
	public VCursor getCursor(){
		return mouse;
	}

    /**the actual panel*/
    ViewPanel panel;

    /**enables detection of multiple full fills in one view repaint - for this specific view - STILL VERY BUGGY - ONLY SUPPORTS VRectangle and VCircle for now*/
    boolean detectMultipleFullFills;

    JLabel statusBar;

    /**View name*/
    protected String name;

    /**triggers the mouseMoved method in ViewEventHandler when the mouse is moved - set to false by default because few applications will need this; it is therefore not necessary to overload other applications with these events*/
    boolean notifyMouseMoved = true;

    /**hooks for Java2D painting in ZVTM views (BACKGROUND, FOREGROUND, AFTER_DISTORTION, AFTER_PORTALS)*/
    Java2DPainter[] painters = new Java2DPainter[4];

    /**
     * get the ViewPanel associated with this view
     */
    public ViewPanel getPanel(){
	return panel;
    }

    /**destroy this view*/
    public abstract void destroyView();

    /**get the java.awt.Container for this view*/
    public abstract Container getFrame();

    /**Set the cursor for this view.
     * Either the ZVTM cursor or one of the default AWT cursors.
     *@param cursorType any of the cursor type values declared in java.awt.Cursor, such as DEFAULT_CURSOR, CROSSHAIR_CURSOR HAND_CURSOR, etc. To get the ZVTM cursor, use Cursor.CUSTOM_CURSOR.
     *@see #setCursorIcon(Cursor c)
     */
    public void setCursorIcon(int cursorType){
	panel.setAWTCursor(cursorType);
    }

    /**Set the cursor for this view.
     * Replaces the ZVTM cursor by a bitmap cursor similar to the default AWT cursors.
     *@param c an AWT cursor instantiated e.g. by calling java.awt.Toolkit.createCustomCursor(Image cursor, Point hotSpot, String name)
     *@see #setCursorIcon(int cursorType)
     */
    public void setCursorIcon(Cursor c){
	panel.setAWTCursor(c);
    }
   
    /** Set application class to which events are sent.
     * Assumes layer 0 (deepest) is active.
     */
    public void setEventHandler(ViewEventHandler eh){
	setEventHandler(eh, 0);
    }

    /** Set application class to which events are sent.
     *@param layer depth of layer to which the event handler should be associated.
     */
    public void setEventHandler(ViewEventHandler eh, int layer){
	panel.setEventHandler(eh, layer);
    }

    /** Sets whether the mouseMoved callback in ViewEventHandler is triggered when the mouse is moved.
     * Set to true by default. Applications that do not care about this callback can disable notification
     * about these events to avoid unnecessary callbacks (an event each sent each time the cursor moves).
     */
    public void setNotifyMouseMoved(boolean b){
	    notifyMouseMoved=b;
    }

    /** Tells whether the mouseMoved callback in ViewEventHandler is triggered when the mouse is moved.
     * Set to true by default.*/
    public boolean getNotifyMouseMoved(){return notifyMouseMoved;}

    /**set status bar text*/
    public void setStatusBarText(String s){
	if (statusBar!=null){if (s.equals("")){statusBar.setText(" ");}else{{statusBar.setText(s);}}}
    }

    /**set font used in status bar text*/
    public void setStatusBarFont(Font f){
	if (statusBar!=null){statusBar.setFont(f);}
    }

    /**set color used for status bar text*/
    public void setStatusBarForeground(Color c){
	if (statusBar!=null){statusBar.setForeground(c);}
    }

    /**enable/disable detection of multiple full fills in one view repaint - for this specific view */
    public void setDetectMultiFills(boolean b){
	detectMultipleFullFills=b;
    }

    /**get state of detection of multiple full fills in one view repaint - for this specific view*/
    public boolean getDetectMultiFills(){
	return detectMultipleFullFills;
    }

    /**returns bounds of rectangle representing virtual space's region seen through camera c [west,north,east,south]*/
    public long[] getVisibleRegion(Camera c){
	return getVisibleRegion(c, new long[4]);
    }

    /**returns bounds of rectangle representing virtual space's region seen through camera c [west,north,east,south]
     *@param c camera
     *@param res array which will contain the result */
    public long[] getVisibleRegion(Camera c, long[] res){
	if (cameras.contains(c)){
	    float uncoef=(float)((c.focal+c.altitude)/c.focal);  //compute region seen from this view through camera
		Dimension panelSize = panel.getSize();
	    res[0] = (long)(c.posx-(panelSize.width/2-panel.visibilityPadding[0])*uncoef);
	    res[1] = (long)(c.posy+(panelSize.height/2-panel.visibilityPadding[1])*uncoef);
	    res[2] = (long)(c.posx+(panelSize.width/2-panel.visibilityPadding[2])*uncoef);
	    res[3] = (long)(c.posy-(panelSize.height/2-panel.visibilityPadding[3])*uncoef);
	    return res;
	}
	return null;
    }

    public long getVisibleRegionWidth(Camera c){
	return (long)(panel.getSize().width * ((c.focal+c.altitude) / c.focal));
    }

    public long getVisibleRegionHeight(Camera c){
	return (long)(panel.getSize().height * ((c.focal+c.altitude) / c.focal));
    }

    /**returns a BufferedImage representation of this view (this is actually a COPY of the original) that can be used for instance with ImageIO.ImageWriter*/
    public BufferedImage getImage(){
	BufferedImage res=null;
	    BufferedImage i=panel.getImage();
	    if (i!=null){
		//this is the old method for doing this, which eventually stopped working on POSIX systems  (hangs at i.copyData())
// 		java.awt.image.WritableRaster wr=Raster.createWritableRaster(i.getSampleModel(),new java.awt.Point(0,0));
// 		res=new BufferedImage(i.getColorModel(),i.copyData(wr),false,null);
		//new way of doing things
		res=new BufferedImage(i.getWidth(),i.getHeight(),i.getType());
		Graphics2D resg2d=res.createGraphics();
		resg2d.drawImage(i,null,0,0);
	    }
	return res;
    }

    /**set the layer (camera) active in this view
     * @param i i-th layer 0 is the deepest layer
     */
    public void setActiveLayer(int i){
	Camera c = (Camera)cameras.elementAt(i);
	mouse.unProject(c, panel);
	mouse.resetGlyphsUnderMouseList(c.parentSpace,
					c.getIndex());
	panel.activeLayer=i;
    }

    /**get the active layer in this view (0 is deepest)*/
    public int getActiveLayer(){
	return panel.activeLayer;
    }
    
    /**Get the number of layers in this view.*/
    public int getLayerCount(){
	return cameras.size();
    }
    
    /**update font used in this view (for all cameras) (should be automatically called when changing the VSM's main font)*/
    public void updateFont(){panel.updateFont=true;}

    /**set antialias rendering hint for this view*/
    public void setAntialiasing(boolean b){
	if (b!=panel.antialias){
	    panel.antialias=b;
	    panel.updateAntialias=true;
	    repaintNow();
	}
    }

    /**get the value of the antialias rendering hint for this view*/
    public boolean getAntialiasing(){
	return panel.antialias;
    }

    /**get camera number i (corresponds to layer)*/
    public Camera getCameraNumber(int i){
	if (cameras.size()>i){return (Camera)cameras.elementAt(i);}
	else return null;
    }

    /**get active camera (associated with active layer)*/
    public Camera getActiveCamera(){
	return panel.cams[panel.activeLayer];
    }

    void destroyCamera(Camera c){
	for (int i=0;i<panel.cams.length;i++){
	    if (panel.cams[i]==c){
		panel.cams[i]=null;
		if (i==panel.activeLayer){//if the camera we remove was associated to the active layer, make active another non-null layer
		    for (int j=0;j<panel.cams.length;j++){
			if (panel.cams[j]!=null){
			    panel.activeLayer=j;
			    break;
			}
		    }
		}
		break;
	    }
	}
	cameras.remove(c);
    }

    /**set background color for this view*/
    public void setBackgroundColor(Color c){
	panel.backColor=c;
    }

    /**get background color of this view*/
    public Color getBackgroundColor(){
	return panel.backColor;
    }

    /**tells whether this frame is selected or not*/
    public abstract boolean isSelected();

    /**set the window title*/
    public abstract void setTitle(String t);

    /**set the window location*/
    public abstract void setLocation(int x,int y);

    /**set the window size*/
    public abstract void setSize(int x,int y);

    /**get the dimensions of the ZVTM panel embedded in this view*/
    public Dimension getPanelSize(){
	return panel.size;
    }

    /**can the window be resized or not*/
    public abstract void setResizable(boolean b);

    /**Shows or hides this view*/
    public abstract void setVisible(boolean b);

    /**Brings this window to the front. Places this window at the top of the stacking order and shows it in front of any other windows*/
    public abstract void toFront();

    /**Sends this window to the back. Places this window at the bottom of the stacking order and makes the corresponding adjustment to other visible windows*/
    public abstract void toBack();

    /**Set this view's refresh rate - default is 20
     *@param r positive integer (refresh rate in milliseconds)
     */
    public void setRefreshRate(int r){
	panel.setRefreshRate(r);
    }

    /**Set this view's refresh rate - default is 20*/
    public int getRefreshRate(){
	return panel.getRefreshRate();
    }

    /**should repaint this view on a regular basis or not (even if not activated, but does not apply to iconified views)*/
    public void setRepaintPolicy(boolean b){
	panel.alwaysRepaintMe=b;
	if (b){panel.active=true;}
	else {if ((!isSelected()) && (!panel.inside)){panel.active=false;}}
    }

    /**
     * make a view blank (the view is erased and filled with a uniform color)
     *@param c blank color (will fill the entire view) - put null to exit blank mode
     */
    public void setBlank(Color c){
	if (c==null){
	    panel.blankColor=null;
	    panel.notBlank=true;
	    repaintNow();
	}
	else {
	    panel.blankColor=c;
	    panel.notBlank=false;
	    repaintNow();
	}
    }

    /**
     *tells if a view is in blank mode (returns the fill color) or not (returns null)
     */
    public Color isBlank(){
	if (!panel.notBlank){
	    return panel.blankColor;
	}
	else return null;
    }

    /**if true, compute the list of glyphs under mouse each time the view is repainted (default is false) - note that this list is computed each time the mouse is moved inside the view, no matter the policy*/
    public void setComputeMouseOverListPolicy(boolean b){
	panel.computeListAtEachRepaint=b;
    }

    /**activate the view means that it will be repainted*/
    public void activate(){
	VirtualSpaceManager.INSTANCE.setActiveView(this);
	panel.active=true;
	if (panel.evHs[panel.activeLayer]!=null){panel.evHs[panel.activeLayer].viewActivated(this);}
    }
    
    /**deactivate the view (will not be repainted unless setRepaintPolicy(true) or mouse inside the view)*/
    public void deactivate(){
	if ((!panel.alwaysRepaintMe) && (!panel.inside)){panel.active=false;}
	if (panel.evHs[panel.activeLayer]!=null){panel.evHs[panel.activeLayer].viewDeactivated(this);}
    }

    /**called from the window listener when the window is iconified - repaint is automatically disabled*/
    void iconify(){
	panel.active=false;
	if (panel.evHs[panel.activeLayer]!=null){panel.evHs[panel.activeLayer].viewIconified(this);}
    }

    /**called from the window listener when the window is deiconified - repaint is automatically re-enabled*/
    void deiconify(){
	panel.active=true;
	if (panel.evHs[panel.activeLayer]!=null){panel.evHs[panel.activeLayer].viewDeiconified(this);}
    }

    /**called from the window listener when the window is closed*/
    protected void close(){
	if (panel.evHs[panel.activeLayer]!=null){panel.evHs[panel.activeLayer].viewClosing(this);}
    }

    /**Call this if you want to repaint this view at once.
        *@see #repaintNow(RepaintListener rl)
        */
    public void repaintNow(){
        panel.repaintNow = true;
    }

    /**Call this if you want to repaint this view at once.
        *@param rl a repaint listener to be notified when this repaint cycle is completed (it must be removed manually if you are not interested in being notified about following repaint cycles)
        *@see #repaintNow()
        *@see #removeRepaintListener()     */
    public void repaintNow(RepaintListener rl){
        panel.repaintListener = rl;
        repaintNow();
    }

    /**Remove the repaint listener associated with this view.
     *@see #repaintNow(RepaintListener rl)
     */
    public void removeRepaintListener(){
	panel.repaintListener = null;
    }

    /**gives access to the panel's Graphics object - can be useful in some cases, for instance to compute the bounds of a text string that has not yet been added to any virtual space. SHOULD NOT BE TAMPERED WITH. USE AT YOUR OWN RISKS!*/
    public Graphics getGraphicsContext(){
	return panel.stableRefToBackBufferGraphics;
    }

    /**ask for a bitmap rendering of this view and encode it in a PNG file
     *@param w width of rendered image
     *@param h height of rendered image
     *@param vsm the current VirtualSpaceManager
     *@param f the location of the resulting PNG file
     *@see #rasterize(int w, int h, VirtualSpaceManager vsm, java.io.File f, Vector layers)
     *@see #rasterize(int w, int h, VirtualSpaceManager vsm)
     *@see #rasterize(int w, int h, VirtualSpaceManager vsm, Vector layers)
     */
    public void rasterize(int w, int h, VirtualSpaceManager vsm, java.io.File f){
	rasterize(w, h, vsm, f, null);
    }

    /**ask for a bitmap rendering of this view and encode it in a PNG file
     *@param w width of rendered image
     *@param h height of rendered image
     *@param vsm the current VirtualSpaceManager
     *@param f the location of the resulting PNG file
     *@param layers Vector of cameras : what layers (represented by cameras) of this view should be rendered (you can pass null for all layers)
     *@see #rasterize(int w, int h, VirtualSpaceManager vsm, java.io.File f)
     *@see #rasterize(int w, int h, VirtualSpaceManager vsm)
     *@see #rasterize(int w, int h, VirtualSpaceManager vsm, Vector layers)
     */
    public void rasterize(int w, int h, VirtualSpaceManager vsm, java.io.File f, Vector layers){
	javax.imageio.ImageWriter writer = (javax.imageio.ImageWriter)javax.imageio.ImageIO.getImageWritersByFormatName("png").next();
	try {
	    writer.setOutput(javax.imageio.ImageIO.createImageOutputStream(f));
	    BufferedImage bi = this.rasterize(w, h, vsm, layers);
	    if (bi != null){
		writer.write(bi);
		writer.dispose();
	    }
	}
	catch (java.io.IOException ex){ex.printStackTrace();}
    }

    /**ask for a bitmap rendering of this view and return the resulting BufferedImage
     *@param w width of rendered image
     *@param h height of rendered image
     *@param vsm the current VirtualSpaceManager
     *@return the resulting buffered image which can then be manipulated and serialized
     *@see #rasterize(int w, int h, VirtualSpaceManager vsm, java.io.File f)
     *@see #rasterize(int w, int h, VirtualSpaceManager vsm, java.io.File f, Vector layers)
     *@see #rasterize(int w, int h, VirtualSpaceManager vsm, Vector layers)
     */
    public BufferedImage rasterize(int w, int h, VirtualSpaceManager vsm){
	return rasterize(w, h, vsm, (Vector)null);
    }

    /**ask for a bitmap rendering of this view and return the resulting BufferedImage
     *@param w width of rendered image
     *@param h height of rendered image
     *@param vsm the current VirtualSpaceManager
     *@param layers Vector of cameras : what layers (represented by cameras) of this view should be rendered (you can pass null for all layers)
     *@return the resulting buffered image which can then be manipulated and serialized
     *@see #rasterize(int w, int h, VirtualSpaceManager vsm, java.io.File f)
     *@see #rasterize(int w, int h, VirtualSpaceManager vsm, java.io.File f, Vector layers)
     *@see #rasterize(int w, int h, VirtualSpaceManager vsm)
     */
    public BufferedImage rasterize(int w, int h, VirtualSpaceManager vsm, Vector layers){
	Dimension panelSize = panel.getSize();
	float mFactor = 1/Math.min(w / ((float)panelSize.getWidth()),
				   h / ((float)panelSize.getHeight()));
	Camera c, nc;
	Vector clones= new Vector();
	Vector cams = (layers != null) ? layers : cameras;
	for (int i=0;i<cams.size();i++){
	    c = (Camera)cams.elementAt(i);
	    nc = c.parentSpace.addCamera();
	    nc.posx = c.posx;
	    nc.posy = c.posy;
	    /*change this altitude to compensate for the w/h change what we
	      want is to get the same view at a higher (or lower) resolution*/
	    nc.focal = c.focal;
	    nc.altitude = (c.altitude + c.focal) * mFactor - c.focal;
	    clones.add(nc);
	}
	BufferedImage img = (new OffscreenViewPanel(clones)).rasterize(w, h, panel.backColor);
	for (int i=0;i<clones.size();i++){
	    nc = (Camera)clones.elementAt(i);
	    vsm.getVirtualSpace(nc.parentSpace.spaceName).removeCamera(nc.index);
	}
	return img;
    }

    //we have to specify the layer too (I think)     write this later
//     /**show/hide in this view the region seen through a camera (as a rectangle)
//      *@param c the camera to be displayed (should not be one of the cameras composing this view)
//      */
//     public void showCamera(Camera c,boolean b,Color col){
    
//     }

    /** set a paint method (containing Java2D paint instructions) that will be called each time the view is repainted
     *@param p the paint method encapsulated in an object implementing the Java2DPainter interface (pass null to unset an existing one)
     *@param g one of Java2DPainter.BACKGROUND, Java2DPainter.FOREGROUND, Java2DPainter.AFTER_DISTORTION, Java2DPainter.AFTER_PORTALS depending on whether the method should be called before or after ZVTM glyphs have been painted, after distortion by a lens (FOREGROUND and AFTER_DISTORTION are equivalent in the absence of lens), or after portals have been painted
     */
    public void setJava2DPainter(Java2DPainter p, short g){
	painters[g] = p;
	repaintNow();
    }

    /** get the paint method (containing Java2D paint instructions) that will be called each time the view is repainted
     *@param g one of Java2DPainter.BACKGROUND, Java2DPainter.FOREGROUND, Java2DPainter.AFTER_DISTORTION, Java2DPainter.AFTER_PORTALS depending on whether the method should be called before or after ZVTM glyphs have been painted, after distortion by a lens (FOREGROUND and AFTER_DISTORTION are equivalent in the absence of lens), or after portals have been painted
     *@return p the paint method encapsulated in an object implementing the Java2DPainter interface (null if not set)
     */
    public Java2DPainter getJava2DPainter(short g){
	return painters[g];
    }
    
    /**get the name of this view*/
    public String getName(){
	return name;
    }

    /** set a padding for customizing the region inside the view for which objects are actually visibles
     *@param wnesPadding padding values in pixels for the west, north, east and south borders
    */
    public void setVisibilityPadding(int[] wnesPadding){
	panel.setVisibilityPadding(wnesPadding);
    }

    /** get the padding values customizing the region inside the view for which objects are actually visibles
     *@return padding values in pixels for the west, north, east and south borders
    */
    public int[] getVisibilityPadding(){
	return panel.getVisibilityPadding();
    }
    
    void buildConstraints(GridBagConstraints gbc, int gx,int gy,int gw,int gh,int wx,int wy){
	gbc.gridx=gx;
	gbc.gridy=gy;
	gbc.gridwidth=gw;
	gbc.gridheight=gh;
	gbc.weightx=wx;
	gbc.weighty=wy;
    }
    
    /* --------------------- LENSES -------------------------- */

    /**set a lens for this view ; set to null to remove an existing lens<br/>Only works with standard view (has no effect when set on accelereated views)<br>
        * Important: Distortion lenses cannot be associated with VolatileImage-based or OpenGL-based views*/
    public Lens setLens(Lens l){
        Lens res = panel.setLens(l);
        return res;
    }

    /**return Lens currently used by this view (null if none)*/
    public Lens getLens(){
        return panel.getLens();
    }
    
    /* ----------------- Navigation ------------------ */
    
    /** Get the location from which a camera will see all glyphs visible in the associated virtual space.
     * The camera must be used in this view. Otherwise, the method returns null and does nothing.
     *@param c camera considered (will not be moved)
     *@param mFactor magnification factor - 1.0 (default) means that the glyphs will occupy the whole screen. mFactor &gt; 1 will zoom out from this default location. mFactor &lt; 1 will do the opposite
     *@return the location to which the camera should go, null if the camera is not associated with this view.
     *@see #getGlobalView(Camera c, int d)
     *@see #getGlobalView(Camera c)
     *@see #getGlobalView(Camera c, int d, float mFactor)
     */
    public Location getGlobalView(Camera c, float mFactor){
        if (c.getOwningView() != this){return null;}
        //wnes=west north east south
        long[] wnes = c.parentSpace.findFarmostGlyphCoords();
        //new coords where camera should go
        long dx = (wnes[2]+wnes[0])/2;
        long dy = (wnes[1]+wnes[3])/2;
        long[] regBounds = this.getVisibleRegion(c);
        /*region that will be visible after translation, but before zoom/unzoom (need to
        compute zoom) ; we only take left and down because we only need horizontal and
        vertical ratios, which are equals for left and right, up and down*/
        long[] trRegBounds = {regBounds[0]+dx-c.posx, regBounds[3]+dy-c.posy};
        float currentAlt = c.getAltitude()+c.getFocal();
        float ratio = 0;
        //compute the mult factor for altitude to see all stuff on X
        if (trRegBounds[0]!=0){ratio = (dx-wnes[0])/((float)(dx-trRegBounds[0]));}
        //same for Y ; take the max of both
        if (trRegBounds[1]!=0){
            float tmpRatio = (dy-wnes[3])/((float)(dy-trRegBounds[1]));
            if (tmpRatio>ratio){ratio = tmpRatio;}
        }
        ratio *= mFactor;
        return new Location(dx, dy, currentAlt*Math.abs(ratio)-c.getFocal());
    }
    
    /** Translates and (un)zooms a camera in order to see everything visible in the associated virtual space.
     * The camera must be used in this view. Otherwise, the method returns null and does nothing.
     *@param c Camera to be moved (will actually be moved)
     *@param d duration of the animation in ms
     *@return the final camera location, null if the camera is not associated with this view.
     *@see #getGlobalView(Camera c)
     *@see #getGlobalView(Camera c, int d, float mFactor)
     *@see #getGlobalView(Camera c, float mFactor)
     */
    public Location getGlobalView(Camera c, int d){
        return getGlobalView(c, d, 1.0f);
    }
    
    /** Translates and (un)zooms a camera in order to see everything visible in the associated virtual space.
     * The camera must be used in this view. Otherwise, the method returns null and does nothing.
     *@param c Camera to be moved (will actually be moved)
     *@param d duration of the animation in ms
     *@param mFactor magnification factor - 1.0 (default) means that the glyphs will occupy the whole screen. mFactor &gt; 1 will zoom out from this default location. mFactor &lt; 1 will do the opposite
     *@return the final camera location, null if the camera is not associated with this view.
     *@see #getGlobalView(Camera c)
     *@see #getGlobalView(Camera c, int d)
     *@see #getGlobalView(Camera c, float mFactor)
     */
	public Location getGlobalView(Camera c, int d, float mFactor){
		Location l = this.getGlobalView(c, mFactor);
		if (l != null){
		    Animation trans = 
			VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraTranslation(d,c,
										       new LongPoint(l.vx,l.vy),
										       false,
										       SlowInSlowOutInterpolator.getInstance(),
										       null);
		    
		    Animation alt = 
			VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraAltAnim(d,c,
										   l.alt,
										   false,
										   SlowInSlowOutInterpolator.getInstance(),
										   null);
		    
		    //VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(trans, false);
		    //VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(alt, false);
		}
		return l;
	}
	
	/** Get the location from which a camera will see everything visible in the associated virtual space.
	 * The camera must be used in this view. Otherwise, the method returns null and does nothing.
	 *@param c camera considered (will not be moved)
	 *@return the location to which the camera should go, null if the camera is not associated with this view.
	 *@see #getGlobalView(Camera c, int d)
	 *@see #getGlobalView(Camera c, int d, float mFactor)
	 *@see #getGlobalView(Camera c, float mFactor)
	 */
	public Location getGlobalView(Camera c){
		return getGlobalView(c, 1.0f);
	}

    /** Translates and (un)zooms a camera in order to focus on glyph g
     * The camera must be used in this view. Otherwise, the method returns null and does nothing.
        *@param g Glyph of interest
        *@param c Camera to be moved
        *@param d duration of the animation in ms
        *@param z if false, do not (un)zoom, just translate (default is true)
        *@param mFactor magnification factor: 1.0 (default) means that the glyph will occupy the whole screen. mFactor < 1 will make the glyph smaller (zoom out). mFactor > 1 will make the glyph appear bigger (zoom in)
        *@param endAction end action to execute after camera reaches its final position
        *@return the final camera location, null if the camera is not associated with this view.
        */
    public Location centerOnGlyph(Glyph g, Camera c, int d, boolean z, float mFactor, EndAction endAction){
        if (c.getOwningView() != this){return null;}
        long dx;
        long dy;
        if (g instanceof VText){
            VText t=(VText)g;
            LongPoint p=t.getBounds(c.getIndex());
            if (t.getTextAnchor()==VText.TEXT_ANCHOR_START){
                dx=g.vx+p.x/2-c.posx;
                dy=g.vy+p.y/2-c.posy;
            }
            else if (t.getTextAnchor()==VText.TEXT_ANCHOR_MIDDLE){
                dx=g.vx-c.posx;
                dy=g.vy-c.posy;
            }
            else {
                dx=g.vx-p.x/2-c.posx;
                dy=g.vy-p.y/2-c.posy;
            }
        }
        else {
            dx=g.vx-c.posx;
            dy=g.vy-c.posy;
        }

        //relative translation
        Animation trans = 
            VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
            createCameraTranslation(d, c,
            new LongPoint(dx,dy),
            true,
            SlowInSlowOutInterpolator.getInstance(),
            endAction);
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(trans, false);

        float currentAlt=c.getAltitude()+c.getFocal();
        if (z){
            long[] regBounds = this.getVisibleRegion(c);
            // region that will be visible after translation, but before zoom/unzoom  (need to compute zoom) ;
            // we only take left and down because ratios are equals for left and right, up and down
            long[] trRegBounds={regBounds[0]+dx,regBounds[3]+dy};
            float ratio=0;
            //compute the mult factor for altitude to see glyph g entirely
            if (trRegBounds[0]!=0){
                if (g instanceof VText){
                    ratio = ((float)(((VText)g).getBounds(c.getIndex()).x)) / ((float)(g.vx-trRegBounds[0]));
                }
                else if (g instanceof RectangularShape){
                    ratio = ((float)(((RectangularShape)g).getWidth())) / ((float)(g.vx-trRegBounds[0]));
                }
                else {
                    ratio = g.getSize() / ((float)(g.vx-trRegBounds[0]));
                }
            }
            //same for Y ; take the max of both
            if (trRegBounds[1]!=0){
                float tmpRatio;
                if (g instanceof VText){
                    tmpRatio = ((float)(((VText)g).getBounds(c.getIndex()).y)) / ((float)(g.vy-trRegBounds[1]));
                }
                else if (g instanceof RectangularShape){
                    tmpRatio = ((float)(((RectangularShape)g).getHeight())) / ((float)(g.vy-trRegBounds[1]));
                }
                else {
                    tmpRatio = (g.getSize())/((float)(g.vy-trRegBounds[1]));
                }
                if (tmpRatio>ratio){ratio=tmpRatio;}
            }
            ratio *= mFactor;
            float newAlt=currentAlt*Math.abs(ratio);

            Animation altAnim = 
                VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
                createCameraAltAnim(d, c, 
                newAlt, false,
                SlowInSlowOutInterpolator.getInstance(),
                null);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(altAnim, false);

            return new Location(g.vx,g.vy,newAlt);
        }
        else {
            return new Location(g.vx,g.vy,currentAlt);
        }
    }
 
    /** Translates and (un)zooms a camera in order to focus on glyph g
     * The camera must be used in this view. Otherwise, the method returns null and does nothing.
     *@param g Glyph of interest
     *@param c Camera to be moved
     *@param d duration of the animation in ms
     *@return the final camera location, null if the camera is not associated with this view.
     */
    public Location centerOnGlyph(Glyph g,Camera c,int d){
	return this.centerOnGlyph(g,c,d,true);
    }

    /** Translates and (un)zooms a camera in order to focus on glyph g
     * The camera must be used in this view. Otherwise, the method returns null and does nothing.
     *@param g Glyph of interest
     *@param c Camera to be moved
     *@param d duration of the animation in ms
     *@param z if false, do not (un)zoom, just translate (default is true)
     *@return the final camera location, null if the camera is not associated with this view.
     */
    public Location centerOnGlyph(Glyph g, Camera c, int d, boolean z){
	return this.centerOnGlyph(g, c, d, z, 1.0f);
    }

    /** Translates and (un)zooms a camera in order to focus on glyph g
     * The camera must be used in this view. Otherwise, the method returns null and does nothing.
     *@param g Glyph of interest
     *@param c Camera to be moved
     *@param d duration of the animation in ms
     *@param z if false, do not (un)zoom, just translate (default is true)
     *@param mFactor magnification factor - 1.0 (default) means that the glyph will occupy the whole screen. mFactor < 1 will make the glyph smaller (zoom out). mFactor > 1 will make the glyph appear bigger (zoom in)
     *@return the final camera location, null if the camera is not associated with this view.
     */
    public Location centerOnGlyph(Glyph g, Camera c, int d, boolean z, float mFactor){
	return this.centerOnGlyph(g, c, d, z, mFactor, null);
    }
    
    

    /** Translates and (un)zooms a camera in order to focus on a specific rectangular region
     * The camera must be used in this view. Otherwise, the method returns null and does nothing.
		*@param c Camera to be moved
		*@param d duration of the animation in ms (pass 0 to go there instantanesouly)
		*@param x1 x coord of first point
		*@param y1 y coord of first point
		*@param x2 x coord of opposite point
		*@param y2 y coord of opposite point
		*@return the final camera location, null if the camera is not associated with this view.
		*/
	public Location centerOnRegion(Camera c, int d, long x1, long y1, long x2, long y2){
	    return centerOnRegion(c, d, x1, y1, x2, y2, null);
    }
    
	/** Translates and (un)zooms a camera in order to focus on a specific rectangular region
     * The camera must be used in this view. Otherwise, the method returns null and does nothing.
		*@param c Camera to be moved
		*@param d duration of the animation in ms (pass 0 to go there instantanesouly)
		*@param x1 x coord of first point
		*@param y1 y coord of first point
		*@param x2 x coord of opposite point
		*@param y2 y coord of opposite point
		*@param ea action to be performed at end of animation
		*@return the final camera location, null if the camera is not associated with this view.
		*/
	public Location centerOnRegion(Camera c, int d, long x1, long y1, long x2, long y2, EndAction ea){
        if (c.getOwningView() != this){return null;}
        long minX = Math.min(x1,x2);
        long minY = Math.min(y1,y2);
        long maxX = Math.max(x1,x2);
        long maxY = Math.max(y1,y2);
        //wnes=west north east south
        long[] wnes = {minX, maxY, maxX, minY};
        //new coords where camera should go
        long dx = (wnes[2]+wnes[0]) / 2; 
        long dy = (wnes[1]+wnes[3]) / 2;
        // new alt to fit horizontally
		Dimension panelSize = this.getPanel().getSize();
        float nah = (wnes[2]-dx) * 2 * c.getFocal() / panelSize.width - c.getFocal();
        // new alt to fit vertically
        float nav = (wnes[1]-dy) * 2 * c.getFocal() / panelSize.height - c.getFocal();
        // take max of both
        float na = Math.max(nah, nav);
        if (d > 0){
            Animation trans =
                VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
                createCameraTranslation(d, c, new LongPoint(dx, dy), false,
                SlowInSlowOutInterpolator.getInstance(),
                ea);
            Animation altAnim = 
                VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
                createCameraAltAnim(d, c, na, false,
                SlowInSlowOutInterpolator.getInstance(),
                null);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(trans, false);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(altAnim, false);			        
        }
        else {
            c.setAltitude(na);
            c.moveTo(dx, dy);
        }
        return new Location(dx, dy, na);
    }

    /** returns a vector of glyphs whose hotspot is in region delimited by rectangle (x1,y1,x2,y2) in virtual space vs (returns null if empty). Coordinates of the mouse cursor in virtual space are available in instance variables vx and vy of class VCursor. The selection rectangle can be drawn on screen by using ViewPanel.setDrawRect(true) (e.g. call when mouse button is pressed)/ViewPanel.setDrawRect(false) (e.g. call when mouse button is released)
     *@param x1 x coord of first point
     *@param y1 y coord of first point
     *@param x2 x coord of opposite point
     *@param y2 y coord of opposite point
     *@param vsn name of virtual space
     *@param wg which glyphs in the region should be returned (among VIS_AND_SENS_GLYPHS (default), VISIBLE_GLYPHS, SENSIBLE_GLYPHS, ALL_GLYPHS)
     */
    public Vector getGlyphsInRegion(long x1,long y1,long x2,long y2,String vsn,int wg){
        Vector res=new Vector();
        VirtualSpace vs = VirtualSpaceManager.INSTANCE.getVirtualSpace(vsn);
        long minX=Math.min(x1,x2);
        long minY=Math.min(y1,y2);
        long maxX=Math.max(x1,x2);
        long maxY=Math.max(y1,y2);
        if (vs!=null){
            Vector allG=vs.getAllGlyphs();
            Glyph g;
            for (int i=0;i<allG.size();i++){
                g=(Glyph)allG.elementAt(i);
                if ((g.vx>=minX) && (g.vy>=minY) && (g.vx<=maxX) && (g.vy<=maxY)){
                    if ((wg==VirtualSpaceManager.VIS_AND_SENS_GLYPHS) && g.isSensitive() && g.isVisible()){res.add(g);}
                    else if ((wg==VirtualSpaceManager.VISIBLE_GLYPHS) && g.isVisible()){res.add(g);}
                    else if ((wg==VirtualSpaceManager.SENSITIVE_GLYPHS) && g.isSensitive()){res.add(g);}
                    else if (wg==VirtualSpaceManager.ALL_GLYPHS){res.add(g);}
                }
            }
        }
        if (res.isEmpty()){res=null;}
        return res;
    }
   
}

