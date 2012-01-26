/*   FILE: RoundCameraPortalST.java
 *   DATE OF CREATION:  Sun Jun 18 16:44:59 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: RoundCameraPortalST.java 2957 2010-02-24 10:19:25Z epietrig $
 */ 

package fr.inria.zvtm.engine;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Translucent;

/**A portal showing what is seen through a camera. Shape: circular.
   The Camera should not be used in any other View or Portal.*/

public class RoundCameraPortalST extends RoundCameraPortal implements Translucent {

    /** for translucency (default is 0.5)*/
    AlphaComposite acST;

    /** alpha channel*/
    float alpha = 0.5f;

    /** Builds a new portal displaying what is seen through a camera
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param c camera associated with the portal
     */
    public RoundCameraPortalST(int x, int y, int w, int h, Camera c, float a){
	super(x, y, w, h, c);
	this.alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.alpha);
    }

    /**
     * Set alpha channel value (translucency).
     *@param a in [0;1.0]. 0 is fully transparent, 1 is opaque
     */
    public void setTranslucencyValue(float a){
	try {
	    acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a);  //transparency set to alpha
	    alpha = a;
	}
	catch (IllegalArgumentException ex){
	    if (VirtualSpaceManager.debugModeON()){System.err.println("Error animating translucency of "+this.toString());}
	}
    }

    /** Get alpha channel value (translucency).
     *@return a value in [0;1.0]. 0 is fully transparent, 1 is opaque
     */
    public float getTranslucencyValue(){
	return alpha;
    }

    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
		if (!visible){return;}
 	g2d.setClip(clippingShape);
	g2d.setComposite(acST);
	if (bkgColor != null){
	    g2d.setColor(bkgColor);
	    g2d.fill(clippingShape);
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
	g2d.setClip(0, 0, viewWidth, viewHeight);
	if (borderColor != null){
	    g2d.setColor(borderColor);
	    g2d.draw(clippingShape);
	}
	g2d.setComposite(acO);
    }

}
