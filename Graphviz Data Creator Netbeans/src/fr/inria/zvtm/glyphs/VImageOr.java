/*   FILE: VImageOr.java
 *   DATE OF CREATION:   Jan 09 2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
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
 */

package fr.inria.zvtm.glyphs;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.Shape;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;

/**
 * Re-orientable Bitmap Image. This version is less efficient than VImage, but it can be reoriented. It cannot be made translucent (see VImage*ST).
 * @author Emmanuel Pietriga
 *@see fr.inria.zvtm.glyphs.VImage
 */

public class VImageOr extends VImage {

    /*vertex x coords*/
    int[] xcoords = new int[4];
    /*vertex y coords*/
    int[] ycoords = new int[4];

    /**
        *@param img image to be displayed
        *@param or orientation
        */
    public VImageOr(Image img, float or){
        this(0, 0, 0, img, or, 1.0f);
    }

    /**
        *@param img image to be displayed
        *@param or orientation
        *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
        */
    public VImageOr(Image img, float or, float alpha){
        this(0, 0, 0, img, or, alpha);
    }

    /**
        *@param x coordinate in virtual space
        *@param y coordinate in virtual space
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param img image to be displayed
        *@param or orientation
        */
    public VImageOr(long x,long y, int z,Image img,float or){
        this(x, y, z, img, or, 1.0f);
    }

    /**
        *@param x coordinate in virtual space
        *@param y coordinate in virtual space
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param img image to be displayed
        *@param or orientation
        *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
        */
    public VImageOr(long x, long y, int z, Image img, float or, float alpha){
        super(x,y,z,img);
        orient = or;
    }

    /** Set the glyph's absolute orientation.
     *@param angle in [0:2Pi[ 
     */
    public void orientTo(float angle){
	orient=angle;
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public boolean coordInside(int jpx, int jpy, int camIndex, long cvx, long cvy){
        if (pc[camIndex].p.contains(jpx, jpy)){return true;}
        else {return false;}
    }
    
    public boolean visibleInRegion(long wb, long nb, long eb, long sb, int i){
        if ((vx>=wb) && (vx<=eb) && (vy>=sb) && (vy<=nb)){
            /* Glyph hotspot is in the region. The glyph is obviously visible */
            return true;
        }
        else if (((vx-size)<=eb) && ((vx+size)>=wb) && ((vy-size)<=nb) && ((vy+size)>=sb)){
            /* Glyph is at least partially in region.
            We approximate using the glyph bounding box, meaning that some glyphs not
            actually visible can be projected and drawn (but they won't be displayed)) */
            return true;
        }
        return false;
    }
    
    /** The disc is actually approximated to its bounding box here. Precise intersection computation would be too costly. */
	public boolean visibleInDisc(long dvx, long dvy, long dvr, Shape dvs, int camIndex, int jpx, int jpy, int dpr){
		return pc[camIndex].p.intersects(jpx-dpr, jpy-dpr, 2*dpr, 2*dpr);
	}

    public void project(Camera c, Dimension d){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude));
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].cx=(d.width/2)+Math.round((vx-c.posx)*coef);
	pc[i].cy=(d.height/2)-Math.round((vy-c.posy)*coef);
	//project width and height
	if (zoomSensitive){pc[i].cw=Math.round(vw*coef);pc[i].ch=Math.round(vh*coef);}else{pc[i].cw=(int)vw;pc[i].ch=(int)vh;}
	float x1=-pc[i].cw;
	float y1=-pc[i].ch;
	float x2=pc[i].cw;
	float y2=pc[i].ch;
	xcoords[0] = (int)Math.round((x2*Math.cos(orient)+y1*Math.sin(orient))+pc[i].cx);
	ycoords[0] = (int)Math.round((y1*Math.cos(orient)-x2*Math.sin(orient))+pc[i].cy);
	xcoords[1] = (int)Math.round((x1*Math.cos(orient)+y1*Math.sin(orient))+pc[i].cx);
	ycoords[1] = (int)Math.round((y1*Math.cos(orient)-x1*Math.sin(orient))+pc[i].cy);
	xcoords[2] = (int)Math.round((x1*Math.cos(orient)+y2*Math.sin(orient))+pc[i].cx);
	ycoords[2] = (int)Math.round((y2*Math.cos(orient)-x1*Math.sin(orient))+pc[i].cy);
	xcoords[3] = (int)Math.round((x2*Math.cos(orient)+y2*Math.sin(orient))+pc[i].cx);
	ycoords[3] = (int)Math.round((y2*Math.cos(orient)-x2*Math.sin(orient))+pc[i].cy);
	if (pc[i].p == null){
	    pc[i].p = new Polygon(xcoords, ycoords, 4);
	}
	else {
	    for (int j=0;j<xcoords.length;j++){
		pc[i].p.xpoints[j] = xcoords[j];
		pc[i].p.ypoints[j] = ycoords[j];
	    }
	    pc[i].p.invalidate();
	}
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude)) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].lcx = (lensWidth/2) + Math.round((vx-(lensx))*coef);
	pc[i].lcy = (lensHeight/2) - Math.round((vy-(lensy))*coef);
	//project width and height
	if (zoomSensitive){
	    pc[i].lcw=Math.round(vw*coef);
	    pc[i].lch=Math.round(vh*coef);
	}
	else {
	    pc[i].lcw=(int)vw;
	    pc[i].lch=(int)vh;
	}
	float x1=-pc[i].lcw;
	float y1=-pc[i].lch;
	float x2=pc[i].lcw;
	float y2=pc[i].lch;
	xcoords[0] = (int)Math.round((x2*Math.cos(orient)+y1*Math.sin(orient))+pc[i].lcx);
	ycoords[0] = (int)Math.round((y1*Math.cos(orient)-x2*Math.sin(orient))+pc[i].lcy);
	xcoords[1] = (int)Math.round((x1*Math.cos(orient)+y1*Math.sin(orient))+pc[i].lcx);
	ycoords[1] = (int)Math.round((y1*Math.cos(orient)-x1*Math.sin(orient))+pc[i].lcy);
	xcoords[2] = (int)Math.round((x1*Math.cos(orient)+y2*Math.sin(orient))+pc[i].lcx);
	ycoords[2] = (int)Math.round((y2*Math.cos(orient)-x1*Math.sin(orient))+pc[i].lcy);
	xcoords[3] = (int)Math.round((x2*Math.cos(orient)+y2*Math.sin(orient))+pc[i].lcx);
	ycoords[3] = (int)Math.round((y2*Math.cos(orient)-x2*Math.sin(orient))+pc[i].lcy);
	if (pc[i].lp == null){
	    pc[i].lp = new Polygon(xcoords, ycoords, 4);
	}
	else {
	    for (int j=0;j<xcoords.length;j++){
		pc[i].lp.xpoints[j] = xcoords[j];
		pc[i].lp.ypoints[j] = ycoords[j];
	    }
	    pc[i].lp.invalidate();
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
            // a threshold greater than 0.01 causes jolts when zooming-unzooming around the 1.0 scale region
            if (Math.abs(trueCoef-1.0f)<0.01f){trueCoef=1.0f;}
            if (trueCoef!=1.0f){
                // translate
                at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch);
                if (orient != 0){
                    // rotate
                    at.concatenate(AffineTransform.getRotateInstance(-orient,(float)pc[i].cw,(float)pc[i].ch));
                }
                // rescale
                at.concatenate(AffineTransform.getScaleInstance(trueCoef,trueCoef));
                // draw
                if (alphaC != null){
                    // translucent
                    g.setComposite(alphaC);
                    if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
                        g.drawImage(image,at,null);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                    else {
                        g.drawImage(image,at,null);
                    }
                    if ((drawBorder==1 && pc[i].prevMouseIn) || drawBorder==2){
                        g.setColor(borderColor);
                        g.drawPolygon(pc[i].p);
                    }
                    g.setComposite(acO);
                }
                else {
                    // opaque
                    if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
                        g.drawImage(image,at,null);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                    else {
                        g.drawImage(image,at,null);
                    }
                    if ((drawBorder==1 && pc[i].prevMouseIn) || drawBorder==2){
                        g.setColor(borderColor);
                        g.drawPolygon(pc[i].p);
                    }
                }
            }
            else {
                if (alphaC != null){
                    // translucent
                    g.setComposite(alphaC);
                    if (orient==0){
                        // no rotating, no rescaling, just draw after implicit translation
                        g.drawImage(image,dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,null);
                    }
                    else {
                        // translate
                        at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch);
                        // rotate
                        at.concatenate(AffineTransform.getRotateInstance(-orient,(float)pc[i].cw,(float)pc[i].ch));
                        // draw
                        g.drawImage(image,at,null);
                    }
                    if ((drawBorder==1 && pc[i].prevMouseIn) || drawBorder==2){
                        g.setColor(borderColor);
                        g.drawPolygon(pc[i].p);
                    }
                    g.setComposite(acO);
                }
                else {
                    // opaque
                    if (orient==0){
                        // no rotating, no rescaling, just draw after implicit translation
                        g.drawImage(image,dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,null);
                    }
                    else {
                        // translate
                        at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch);
                        // rotate
                        at.concatenate(AffineTransform.getRotateInstance(-orient,(float)pc[i].cw,(float)pc[i].ch));
                        // draw
                        g.drawImage(image,at,null);
                    }
                    if ((drawBorder==1 && pc[i].prevMouseIn) || drawBorder==2){
                        g.setColor(borderColor);
                        g.drawPolygon(pc[i].p);
                    }
                }
            }
        }
        else {
            g.setColor(this.borderColor);
            g.fillRect(pc[i].lcx,pc[i].lcy,1,1);
        }
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alphaC != null && alphaC.getAlpha()==0){return;}
        if ((pc[i].lcw>=1) || (pc[i].lch>=1)){
            if (zoomSensitive){
                trueCoef=scaleFactor*coef;
            }
            else{
                trueCoef=scaleFactor;
            }
            //a threshold greater than 0.01 causes jolts when zooming-unzooming around the 1.0 scale region
            if (Math.abs(trueCoef-1.0f)<0.01f){trueCoef=1.0f;} 
            if (trueCoef!=1.0f){
                // translate
                at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch);
                if (orient!=0){
                    // rotate
                    at.concatenate(AffineTransform.getRotateInstance(-orient,(float)pc[i].lcw,(float)pc[i].lch));
                }
                // rescale
                at.concatenate(AffineTransform.getScaleInstance(trueCoef,trueCoef));
                // draw
                if (alphaC != null){
                    // translucent
                    g.setComposite(alphaC);
                    if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
                        g.drawImage(image,at,null);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                    else {
                        g.drawImage(image,at,null);
                    }
                    if ((drawBorder==1 && pc[i].prevMouseIn) || drawBorder==2){
                        g.setColor(borderColor);
                        g.drawPolygon(pc[i].lp);
                    }
                    g.setComposite(acO);
                }
                else {
                    // opaque
                    if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
                        g.drawImage(image,at,null);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                    else {
                        g.drawImage(image,at,null);
                    }
                    if ((drawBorder==1 && pc[i].prevMouseIn) || drawBorder==2){
                        g.setColor(borderColor);
                        g.drawPolygon(pc[i].lp);
                    }
                }
            }
            else {
                if (alphaC != null){
                    // translucent
                    g.setComposite(alphaC);
                    if (orient==0){
                        // no rotating, no rescaling, just draw after implicit translation
                        g.drawImage(image,dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,null);
                    }
                    else {
                        // translate
                        at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch);
                        // rotate
                        at.concatenate(AffineTransform.getRotateInstance(-orient,(float)pc[i].lcw,(float)pc[i].lch));
                        // draw
                        g.drawImage(image,at,null);
                    }
                    if ((drawBorder==1 && pc[i].prevMouseIn) || drawBorder==2){
                        g.setColor(borderColor);
                        g.drawPolygon(pc[i].lp);
                    }
                    g.setComposite(acO);
                }
                else {
                    // opaque
                    if (orient==0){
                        // no rotating, no rescaling, just draw after implicit translation
                        g.drawImage(image,dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,null);
                    }
                    else {
                        // translate
                        at=AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch);
                        // rotate
                        at.concatenate(AffineTransform.getRotateInstance(-orient,(float)pc[i].lcw,(float)pc[i].lch));
                        // draw
                        g.drawImage(image,at,null);
                    }
                    if ((drawBorder==1 && pc[i].prevMouseIn) || drawBorder==2){
                        g.setColor(borderColor);
                        g.drawPolygon(pc[i].lp);
                    }
                }
            }
        }
        else {
            g.setColor(this.borderColor);
            g.fillRect(dx+dx+pc[i].lcx,dy+pc[i].lcy,1,1);
        }
    }

    public Object clone(){
        VImageOr res = new VImageOr(vx,vy,0,image,orient, (alphaC != null) ? alphaC.getAlpha(): 1.0f);
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
