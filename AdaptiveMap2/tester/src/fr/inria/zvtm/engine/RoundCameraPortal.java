/*   FILE: RoundCameraPortal.java
 *   DATE OF CREATION:  Sun Jun 18 16:44:59 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: RoundCameraPortal.java 2957 2010-02-24 10:19:25Z epietrig $
 */ 

package fr.inria.zvtm.engine;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import fr.inria.zvtm.engine.Camera;

/**A portal showing what is seen through a camera. Shape: circular.
   The Camera should not be used in any other View or Portal.*/

public class RoundCameraPortal extends CameraPortal {

    Ellipse2D clippingShape;

    /** Builds a new portal displaying what is seen through a camera
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param c camera associated with the portal
     */
    public RoundCameraPortal(int x, int y, int w, int h, Camera c){
	super(x, y, w, h, c);
 	clippingShape = new Ellipse2D.Float(x, y, w, h);
    }

    /**returns bounds of rectangle representing virtual space's region seen through camera c [west,north,east,south].
       Although the region seen is actually an oval, we approximate it to the bounding rectangle.*/
    public long[] getVisibleRegion(){
	return super.getVisibleRegion();
    }

    /**detects whether the given point is inside this portal or not 
     *@param cx horizontal cursor coordinate (JPanel)
     *@param cy vertical cursor coordinate (JPanel)
     */
    public boolean coordInside(int cx, int cy){
	return clippingShape.contains(cx, cy);
    }

    /**move the portal by dx and dy inside the view (JPanel coordinates)*/
    public void move(int dx, int dy){
	super.move(dx, dy);
	clippingShape.setFrame(x, y, w, h);
    }

    /**move the portal by dx and dy inside the view (JPanel coordinates)*/
    public void moveTo(int x, int y){
	super.moveTo(x, y);
	clippingShape.setFrame(x, y, w, h);
    }

    public void updateDimensions(){
	size.setSize(w, h);
	if (clippingShape != null){clippingShape.setFrame(x, y, w, h);}
	else {clippingShape = new Ellipse2D.Float(x, y, w, h);}
    }
    
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
		if (!visible){return;}
 	g2d.setClip(clippingShape);
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
    }

}
