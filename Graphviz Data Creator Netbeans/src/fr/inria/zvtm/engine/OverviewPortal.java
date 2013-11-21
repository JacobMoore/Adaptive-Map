/*   FILE: OverviewPortal.java
 *   DATE OF CREATION:  Sat Jun 17 07:19:59 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: OverviewPortal.java 3364 2010-06-16 13:10:13Z OlivierChapuis $
 */ 

package fr.inria.zvtm.engine;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Timer;
import java.util.TimerTask;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.glyphs.Translucent;

/**A portal showing what is seen through a camera that serves as an overview. Shape: rectangular.
   The Camera should not be used in any other View or Portal.*/

public class OverviewPortal extends CameraPortal {

    Camera observedRegionCamera;
    View observedRegionView;
    long[] observedRegion;
    float orcoef;

    Color observedRegionColor = Color.GREEN;

    /** for translucency of the rectangle representing the region observed through the main viewport (default is 0.5)*/
    AlphaComposite acST;
    /** alpha channel*/
    float alpha = 0.5f;

    Timer borderTimer;

    /** Builds a new portal displaying what is seen through a camera and the region seen through another camera
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param pc camera associated with the portal (provinding the overview)
     *@param orc camera observing a region; this region is displayed as an overlay in the portal
     */
    public OverviewPortal(int x, int y, int w, int h, Camera pc, Camera orc){
	super(x, y, w, h, pc);
	this.observedRegionCamera = orc;
	this.observedRegionView = orc.getOwningView();
	observedRegion = new long[4];
	borderTimer = new Timer();
	borderTimer.scheduleAtFixedRate(new BorderTimer(this), 40, 40);
    }

    private class BorderTimer extends TimerTask {

	OverviewPortal portal;
	long[] portalRegion = new long[4];
	long[] intersection = new long[4];
	
	BorderTimer(OverviewPortal p){
	    super();
	    this.portal = p;
	}
	
	public void run(){
	    portal.getVisibleRegion(portalRegion);
	    intersection[0] = portal.observedRegion[0] - portalRegion[0]; // west
	    intersection[1] = portal.observedRegion[1] - portalRegion[1]; // north
	    intersection[2] = portal.observedRegion[2] - portalRegion[2]; // east
	    intersection[3] = portal.observedRegion[3] - portalRegion[3]; // south
	    portal.observedRegionIntersects(intersection);
	}
	
    }

    /**detects whether the given point is inside the observed region rectangle depicting what is seen through the main camera 
     *@param cx horizontal cursor coordinate (JPanel)
     *@param cy vertical cursor coordinate (JPanel)
     */
    public boolean coordInsideObservedRegion(int cx, int cy){
	return (cx >= x+w/2 + Math.round((observedRegion[0]-camera.posx)*orcoef) &&
		cy >= y+h/2 + Math.round((camera.posy-observedRegion[1])*orcoef) &&
		cx <= x+w/2 + Math.round((observedRegion[2]-camera.posx)*orcoef) &&
		cy <= y+h/2 + Math.round((camera.posy-observedRegion[3])*orcoef));
    }
    
    public void setObservedRegionColor(Color c){
	observedRegionColor = c;
    }

    public Color getObservedRegionColor(){
	return observedRegionColor;
    }

    public void setObservedRegionTranslucency(float a){
	if (a == 1.0f){
	    acST = null;
	}
	else {
	    acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a);
	}
    }

    public double getObservedRegionX() {
	orcoef = (float)(camera.focal/(camera.focal+camera.altitude));
	return (double)x+ (double)w/2.0 + (double)(observedRegion[0]-camera.posx)*orcoef;
    }
    public double getObservedRegionY() {
	orcoef = (float)(camera.focal/(camera.focal+camera.altitude));
	return (double)y+ h/2.0 - (double)(observedRegion[1]-camera.posy)*orcoef;
    }
    public double getObservedRegionW() {
	orcoef = (float)(camera.focal/(camera.focal+camera.altitude));
	return (double)(observedRegion[2]-observedRegion[0])*orcoef;
    }
    public double getObservedRegionH() {
	orcoef = (float)(camera.focal/(camera.focal+camera.altitude));
	return (double)(observedRegion[1]-observedRegion[3])*orcoef;
    }
    public double getObservedRegionCX() {
	return getObservedRegionX() + getObservedRegionW()/2.0;
    }
    public double getObservedRegionCY() {
	return getObservedRegionY() + getObservedRegionH()/2.0;
    }
    
    /**returns null if translucency is 1.0f*/
    public AlphaComposite getObservedRegionTranslucency(){
	return acST;
    }

    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
		if (!visible){return;}
	g2d.setClip(x, y, w, h);
	if (bkgColor != null){
	    g2d.setColor(bkgColor);
	    g2d.fillRect(x, y, w, h);
	}
	standardStroke = g2d.getStroke();
	// be sure to call the translate instruction before getting the standard transform
	// as the latter's matrix is preconcatenated to the translation matrix of glyphs
	// that use AffineTransforms for translation
	standardTransform = g2d.getTransform();
	drawnGlyphs = cameraSpace.getDrawnGlyphs(camIndex);
	synchronized(drawnGlyphs){
	    drawnGlyphs.removeAllElements();
	    uncoef = (float)((camera.focal+camera.altitude) / camera.focal);
	    //compute region seen from this view through camera
	    viewWC = (long)(camera.posx - (w/2)*uncoef);
	    viewNC = (long)(camera.posy + (h/2)*uncoef);
	    viewEC = (long)(camera.posx + (w/2)*uncoef);
	    viewSC = (long)(camera.posy - (h/2)*uncoef);
	    gll = cameraSpace.getDrawingList();
	    for (int i=0;i<gll.length;i++){
		if (gll[i] != null){
		    synchronized(gll[i]){
			if (gll[i].visibleInViewport(viewWC, viewNC, viewEC, viewSC, camera)){
			    //if glyph is at least partially visible in the reg. seen from this view, display
			    gll[i].project(camera, size); // an invisible glyph should still be projected
			    if (gll[i].isVisible()){      // as it can be sensitive
				gll[i].draw(g2d, w, h, camIndex, standardStroke, standardTransform, x, y);
			    }
			}
		    }
		}
	    }
	}
	// paint region observed through observedRegionCamera
	observedRegion = observedRegionView.getVisibleRegion(observedRegionCamera, observedRegion);
	g2d.setColor(observedRegionColor);
	orcoef = (float)(camera.focal/(camera.focal+camera.altitude));
	if (acST != null){
	    g2d.setComposite(acST);
	    g2d.fillRect(x+w/2 + Math.round((observedRegion[0]-camera.posx)*orcoef),
			 y+h/2 - Math.round((observedRegion[1]-camera.posy)*orcoef),
			 Math.round((observedRegion[2]-observedRegion[0])*orcoef),
			 Math.round((observedRegion[1]-observedRegion[3])*orcoef));
	    g2d.setComposite(Translucent.acO);
	}
	g2d.drawRect(x+w/2 + Math.round((observedRegion[0]-camera.posx)*orcoef),
		     y+h/2 - Math.round((observedRegion[1]-camera.posy)*orcoef),
		     Math.round((observedRegion[2]-observedRegion[0])*orcoef),
		     Math.round((observedRegion[1]-observedRegion[3])*orcoef));
	// reset Graphics2D
	g2d.setClip(0, 0, viewWidth, viewHeight);
	if (borderColor != null){
	    g2d.setColor(borderColor);
	    g2d.drawRect(x, y, w, h);
	}
    }

    public void dispose(){
	borderTimer.cancel();
    }

    ObservedRegionListener observedRegionListener;

    public void setObservedRegionListener(ObservedRegionListener orl){
	this.observedRegionListener = orl;
    }

    void observedRegionIntersects(long[] wnes){
	if (observedRegionListener != null){
	    observedRegionListener.intersectsParentRegion(wnes);
	}
    }

}
