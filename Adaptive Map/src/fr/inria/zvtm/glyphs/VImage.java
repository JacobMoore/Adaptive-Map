/*   FILE: VImage.java
 *   DATE OF CREATION:   Jan 09 2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
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
 * $Id: VImage.java 3447 2010-07-29 14:44:08Z epietrig $
 */

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.Shape;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import fr.inria.zvtm.glyphs.projection.RProjectedCoordsP;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;

/**
 * Bitmap Image. This version is the most efficient, but it can neither be reoriented (see VImageOr*) nor made translucent (see VImage*ST).
 * @author Emmanuel Pietriga
 *@see fr.inria.zvtm.glyphs.VImageOr
 */

public class VImage extends ClosedShape implements RectangularShape {

    public static final short DRAW_BORDER_NEVER = 0;
    public static final short DRAW_BORDER_MOUSE_INSIDE = 1;
    public static final short DRAW_BORDER_ALWAYS = 2;

    /** Half width in virtual space (read-only). */
    public long vw;
    /** Half height in virtual space (read-only). */
    public long vh;
    /** Aspect ratio: width divided by height (read-only). */
    public float ar;

    /** For internal use. Made public for easier outside package subclassing. */
    public AffineTransform at;

    /** For internal use. Made public for easier outside package subclassing. */
    public RProjectedCoordsP[] pc;

    /** For internal use. Made public for easier outside package subclassing. */
    public Image image;

    /** Indicates when a border is drawn around the image (read-only).
     * One of DRAW_BORDER_*
     */
    public short drawBorder = DRAW_BORDER_NEVER;

    /** For internal use. Made public for easier outside package subclassing. */
    public boolean zoomSensitive = true;

    /** For internal use. Made public for easier outside package subclassing. */
    public float scaleFactor = 1.0f;
    
    /** For internal use. Made public for easier outside package subclassing. */
    public float trueCoef = 1.0f;

    /** Construct an image at (0, 0) with original scale.
     *@param img image to be displayed
     */
    public VImage(Image img){
	    this(0, 0, 0, img, 1.0, 1.0f);
    }

    /** Construct an image at (x, y) with original scale.
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param img image to be displayed
     */
    public VImage(long x,long y, int z,Image img){
        this(x, y, z, img, 1.0, 1.0f);
    }

    /** Construct an image at (x, y) with a custom scale.
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param img image to be displayed
     *@param scale scaleFactor w.r.t original image size
     */
    public VImage(long x, long y, int z, Image img, double scale){
        this(x, y, z, img, scale, 1.0f);
    }
    
    /** Construct an image at (x, y) with a custom scale.
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param img image to be displayed
     *@param scale scaleFactor w.r.t original image size
      *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
     */
    public VImage(long x, long y, int z, Image img, double scale, float alpha){
        vx = x;
        vy = y;
        vz = z;
        image = img;
        vw = Math.round(image.getWidth(null) * scale / 2.0);
        vh = Math.round(image.getHeight(null) * scale / 2.0);
        if (vw==0 && vh==0){ar = 1.0f;}
        else {ar = (float)vw/(float)vh;}
        computeSize();
        orient = 0;
        setBorderColor(Color.black);
        scaleFactor = (float)scale;
        setTranslucencyValue(alpha);
    }

    public void initCams(int nbCam){
	pc=new RProjectedCoordsP[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new RProjectedCoordsP();
	}
    }

    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		RProjectedCoordsP[] ta=pc;
		pc=new RProjectedCoordsP[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new RProjectedCoordsP();
	    }
	    else {System.err.println("VImage:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new RProjectedCoordsP[1];
		pc[0]=new RProjectedCoordsP();
	    }
	    else {System.err.println("VImage:Error while adding camera "+verifIndex);}
	}
    }

    public void removeCamera(int index){
	pc[index]=null;
    }

    public void resetMouseIn(){
	for (int i=0;i<pc.length;i++){
	    resetMouseIn(i);
	}
    }

    public void resetMouseIn(int i){
	if (pc[i]!=null){pc[i].prevMouseIn=false;}
	borderColor = bColor;
    }

    public float getOrient(){return orient;}

    /** Cannot be reoriented. */
    public void orientTo(float angle){}

    public float getSize(){return size;}

    /** For internal use. */
    public void computeSize(){
        size=(float)Math.sqrt(Math.pow(vw,2)+Math.pow(vh,2));
    }

    public void setWidth(long w){
	vw=w;
	vh=Math.round((float)vw/ar);
	computeSize();
	scaleFactor=(float)(size/Math.sqrt(Math.pow(image.getWidth(null)/2,2)+Math.pow(image.getHeight(null)/2,2)));
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void setHeight(long h){
	vh=h;
	vw=Math.round(vh*ar);
	computeSize();
	scaleFactor=(float)(size/Math.sqrt(Math.pow(image.getWidth(null)/2,2)+Math.pow(image.getHeight(null)/2,2)));
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public long getWidth(){return vw;}

    public long getHeight(){return vh;}

    public void sizeTo(float radius){
	size=radius;
	vw=(long)Math.round((size*ar)/(Math.sqrt(Math.pow(ar,2)+1)));
	vh=(long)Math.round((size)/(Math.sqrt(Math.pow(ar,2)+1)));
	scaleFactor=(float)(size/Math.sqrt(Math.pow(image.getWidth(null)/2,2)+Math.pow(image.getHeight(null)/2,2)));
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void reSize(float factor){
	size*=factor;
	vw=(long)Math.round((size*ar)/(Math.sqrt(Math.pow(ar,2)+1)));
	vh=(long)Math.round((size)/(Math.sqrt(Math.pow(ar,2)+1)));
	scaleFactor=(float)(size/Math.sqrt(Math.pow(image.getWidth(null)/2,2)+Math.pow(image.getHeight(null)/2,2)));
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

	/** Get the bounding box of this Glyph in virtual space coordinates.
	 *@return west, north, east and south bounds in virtual space.
	 */
	public long[] getBounds(){
		long[] res = {vx-vw,vy+vh,vx+vw,vy-vh};
		return res;
	}

    /** Set bitmap image to be displayed. */
    public void setImage(Image i){
	image=i;
	vw = Math.round(image.getWidth(null) * scaleFactor / 2.0);
	vh = Math.round(image.getHeight(null) * scaleFactor / 2.0);
	ar=(float)vw/(float)vh;
	computeSize();
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    /** Get the bitmap image displayed. */
    public Image getImage(){
	return image;
    }

    /** Set to false if the image should not be scaled according to camera's altitude. Its size can still be changed, but its apparent size will always be the same, no matter the camera's altitude.
     *@see #isZoomSensitive()
     */
    public void setZoomSensitive(boolean b){
	if (zoomSensitive!=b){
	    zoomSensitive=b;
	    VirtualSpaceManager.INSTANCE.repaintNow();
	}
    }

    /** Indicates whether the image is scaled according to camera's altitude.
     *@see #setZoomSensitive(boolean b)
     */
    public boolean isZoomSensitive(){
	return zoomSensitive;
    }

    /** Should a border be drawn around the bitmap image.
     *@param p one of DRAW_BORDER_*
     */
    public void setDrawBorderPolicy(short p){
	if (drawBorder!=p){
	    drawBorder=p;
	    VirtualSpaceManager.INSTANCE.repaintNow();
	}
    }

    public boolean fillsView(long w,long h,int camIndex){
	return false; //can contain transparent pixel (we have no way of knowing without analysing the image data -could be done when constructing the object or setting the image)
    }

    public boolean coordInside(int jpx, int jpy, int camIndex, long cvx, long cvy){
        if ((jpx>=(pc[camIndex].cx-pc[camIndex].cw)) && (jpx<=(pc[camIndex].cx+pc[camIndex].cw)) &&
            (jpy>=(pc[camIndex].cy-pc[camIndex].ch)) && (jpy<=(pc[camIndex].cy+pc[camIndex].ch))){return true;}
        else {return false;}
    }

    public boolean visibleInRegion(long wb, long nb, long eb, long sb, int i){
        if ((vx>=wb) && (vx<=eb) && (vy>=sb) && (vy<=nb)){
            /* Glyph hotspot is in the region. The glyph is obviously visible */
            return true;
        }
        else if (((vx-vw)<=eb) && ((vx+vw)>=wb) && ((vy-vh)<=nb) && ((vy+vh)>=sb)){
            /* Glyph is at least partially in region.
            We approximate using the glyph bounding box, meaning that some glyphs not
            actually visible can be projected and drawn (but they won't be displayed)) */
            return true;
        }
        return false;
    }
    
	public boolean visibleInDisc(long dvx, long dvy, long dvr, Shape dvs, int camIndex, int jpx, int jpy, int dpr){
		return dvs.intersects(vx-vw, vy-vh, 2*vw, 2*vh);
	}

    public short mouseInOut(int jpx, int jpy, int camIndex, long cvx, long cvy){
        if (coordInside(jpx, jpy, camIndex, cvx, cvy)){
            //if the mouse is inside the glyph
            if (!pc[camIndex].prevMouseIn){
                //if it was not inside it last time, mouse has entered the glyph
                pc[camIndex].prevMouseIn=true;
                return Glyph.ENTERED_GLYPH;
            }
            //if it was inside last time, nothing has changed
            else {return Glyph.NO_CURSOR_EVENT;}  
        }
        else{
            //if the mouse is not inside the glyph
            if (pc[camIndex].prevMouseIn){
                //if it was inside it last time, mouse has exited the glyph
                pc[camIndex].prevMouseIn=false;
                return Glyph.EXITED_GLYPH;
            }//if it was not inside last time, nothing has changed
            else {return Glyph.NO_CURSOR_EVENT;}
        }
    }

    public void project(Camera c, Dimension d){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude));
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].cx=(d.width/2)+Math.round((vx-c.posx)*coef);
	pc[i].cy=(d.height/2)-Math.round((vy-c.posy)*coef);
	//project width and height
	if (zoomSensitive){
	    pc[i].cw = Math.round(vw*coef);
	    pc[i].ch = Math.round(vh*coef);
	}
	else{
	    pc[i].cw = (int)vw;
	    pc[i].ch = (int)vh;
	}
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i = c.getIndex();
	coef = ((float)(c.focal/(c.focal+c.altitude))) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].lcx = lensWidth/2 + Math.round((vx-lensx)*coef);
	pc[i].lcy = lensHeight/2 - Math.round((vy-lensy)*coef);
	//project width and height
	if (zoomSensitive){
	    pc[i].lcw = Math.round(vw*coef);
	    pc[i].lch = Math.round(vh*coef);
	}
	else {
	    pc[i].lcw = (int)vw;
	    pc[i].lch = (int)vh;
	}
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alphaC != null && alphaC.getAlpha()==0){return;}
        if ((pc[i].cw>=1) || (pc[i].ch>=1)){
            if (zoomSensitive){
                trueCoef = scaleFactor*coef;
            }
            else{
                trueCoef = scaleFactor;
            }
            //a threshold greater than 0.01 causes jolts when zooming-unzooming around the 1.0 scale region
            if (Math.abs(trueCoef-1.0f)<0.01f){trueCoef=1.0f;}
            if (trueCoef!=1.0f){
                // translate
                at = AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch);
                g.setTransform(at);
                // rescale and draw
                if (alphaC != null){
                    // translucent
                    g.setComposite(alphaC);
                    if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
                        g.drawImage(image,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                    else {
                        g.drawImage(image,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
                    }
                    g.setTransform(stdT);
                    if ((drawBorder==1 && pc[i].prevMouseIn) || drawBorder==2){
                        g.setColor(borderColor);
                        g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
                    }
                    g.setComposite(acO);
                }
                else {
                    // opaque
                    if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
                        g.drawImage(image,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                    else {
                        g.drawImage(image,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
                    }
                    g.setTransform(stdT);
                    if ((drawBorder==1 && pc[i].prevMouseIn) || drawBorder==2){
                        g.setColor(borderColor);
                        g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
                    }
                }
            }
            else {
                if (alphaC != null){
                    // translucent
                    g.setComposite(alphaC);
                    g.drawImage(image,dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,null);
                    if ((drawBorder==1 && pc[i].prevMouseIn) || drawBorder==2){
                        g.setColor(borderColor);
                        g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
                    }
                    g.setComposite(acO);
                }
                else {
                    // opaque
                    g.drawImage(image,dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,null);
                    if ((drawBorder==1 && pc[i].prevMouseIn) || drawBorder==2){
                        g.setColor(borderColor);
                        g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
                    }
                }
            }
        }
        else {
            g.setColor(this.borderColor);
            g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
        }
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alphaC != null && alphaC.getAlpha()==0){return;}
        if ((pc[i].lcw >= 1) || (pc[i].lch >= 1)){
            if (zoomSensitive){trueCoef=scaleFactor*coef;}
            else {trueCoef=scaleFactor;}
            //a threshold greater than 0.01 causes jolts when zooming-unzooming around the 1.0 scale region
            if (Math.abs(trueCoef-1.0f)<0.01f){trueCoef=1.0f;}
            if (trueCoef!=1.0f){
                g.setTransform(AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch));
                if (alphaC != null){
                    // translucent
                    g.setComposite(alphaC);
                    if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
                        g.drawImage(image,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                    else {
                        g.drawImage(image,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
                    }
                    g.setTransform(stdT);
                    if ((drawBorder==1 && pc[i].prevMouseIn) || drawBorder==2){
                        g.setColor(borderColor);
                        g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
                    }
                    g.setComposite(acO);
                }
                else {
                    // opaque
                    if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
                        g.drawImage(image,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                    else {
                        g.drawImage(image,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
                    }
                    g.setTransform(stdT);
                    if ((drawBorder==1 && pc[i].prevMouseIn) || drawBorder==2){
                        g.setColor(borderColor);
                        g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
                    }
                }
            }
            else {
                if (alphaC != null){
                    // translucent
                    g.setComposite(alphaC);
                    g.drawImage(image, dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, null);
                    if ((drawBorder==1 && pc[i].prevMouseIn) || drawBorder==2){
                        g.setColor(borderColor);
                        g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
                    }
                    g.setComposite(acO);
                }
                else {
                    // opaque
                    g.drawImage(image, dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, null);
                    if ((drawBorder==1 && pc[i].prevMouseIn) || drawBorder==2){
                        g.setColor(borderColor);
                        g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
                    }
                }
            }
        }
        else {
            g.setColor(this.borderColor);
            g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
        }
    }
        
    /** For internal use. Made public for easier outside package subclassing. */
    public Object interpolationMethod = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
    
    /** Specify how image should be interpolated when drawn at a scale different from its original scale.
        *@param im one of java.awt.RenderingHints.{VALUE_INTERPOLATION_NEAREST_NEIGHBOR,VALUE_INTERPOLATION_BILINEAR,VALUE_INTERPOLATION_BICUBIC} ; default is VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        */
    public void setInterpolationMethod(Object im){
        interpolationMethod = im;
    }
    
    /** Get information about how image should be interpolated when drawn at a scale different from its original scale.
        *@return one of java.awt.RenderingHints.{VALUE_INTERPOLATION_NEAREST_NEIGHBOR,VALUE_INTERPOLATION_BILINEAR,VALUE_INTERPOLATION_BICUBIC} ; default is VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        */
    public Object getInterpolationMethod(){
        return interpolationMethod;
    }

    public Object clone(){
	VImage res=new VImage(vx,vy,0,image);
	res.setWidth(vw);
	res.setHeight(vh);
	res.borderColor=this.borderColor;
	res.cursorInsideColor=this.cursorInsideColor;
	res.bColor=this.bColor;
	res.setDrawBorderPolicy(drawBorder);
	res.setZoomSensitive(zoomSensitive);
	return res;
    }

}
