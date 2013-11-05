/*   FILE: VRoundRect.java
 *   DATE OF CREATION:   Wed May 28 14:27:51 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: VRoundRect.java 3446 2010-07-29 14:42:29Z epietrig $
 */ 

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import fr.inria.zvtm.glyphs.projection.ProjRoundRect;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;

/**
 * Round Rectangle. This version is the most efficient, but it cannot be made translucent (see VRoundRectST).<br>Corners are approximated to right angles for some operations such as cursor entry/exit events.
 * @author Emmanuel Pietriga
 *@see fr.inria.zvtm.glyphs.VRectangle
 */

public class VRoundRect extends ClosedShape implements RectangularShape  {
	
	/* Number that affects the position of the gradient */
	private float fillNum;

    /* Half width and height in virtual space. MADE PUBLIC FOR OUTSIDE PACKAGE SUBCLASSING. */
    public long vw,vh;
    /*aspect ratio (width divided by height)*/
    float ar;

    /**MADE PUBLIC FOR OUTSIDE PACKAGE SUBCLASSING.*/
    public ProjRoundRect[] pc;

    /**
     * Horizontal diameter of the arc at the four corners. MADE PUBLIC FOR OUTSIDE PACKAGE SUBCLASSING.
     */
    public int arcWidth;
    /**
     * Vertical diameter of the arc at the four corners. MADE PUBLIC FOR OUTSIDE PACKAGE SUBCLASSING.
     */
    public int arcHeight;

    public VRoundRect(){
        this(0, 0, 0, 10, 10, Color.WHITE, Color.BLACK, 1.0f, 10, 10);
    }

    /**
        *@param x coordinate in virtual space
        *@param y coordinate in virtual space
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param w half width in virtual space
        *@param h half height in virtual space
        *@param c fill color
        *@param aw arc width in virtual space
        *@param ah arc height in virtual space
        */
    public VRoundRect(long x,long y, int z,long w,long h,Color c,int aw,int ah){
        this(x, y, z, w, h, c, Color.BLACK, 1.0f, aw, ah);
    }

    /**
        *@param x coordinate in virtual space
        *@param y coordinate in virtual space
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param w half width in virtual space
        *@param h half height in virtual space
        *@param c fill color
        *@param bc border color
        *@param aw arc width in virtual space
        *@param ah arc height in virtual space
        */
    public VRoundRect(long x, long y, int z, long w, long h, Color c, Color bc, int aw, int ah){
        this(x, y, z, w, h, c, bc, 1.0f, aw, ah);
    }

    /**
        *@param x coordinate in virtual space
        *@param y coordinate in virtual space
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param w half width in virtual space
        *@param h half height in virtual space
        *@param c fill color
        *@param bc border color
        *@param aw arc width in virtual space
        *@param ah arc height in virtual space
        */
    public VRoundRect(long x, long y, int z, long w, long h, Color c, Color bc, float alpha, int aw, int ah){
        vx = x;
        vy = y;
        vz = z;
        vw = w;
        vh = h;
        computeSize();
        if (vw==0 && vh==0){ar = 1.0f;}
        else {ar = (float)vw / (float)vh;}
        orient = 0;
        setColor(c);
        setBorderColor(bc);
        arcWidth = aw;
        arcHeight = ah;
        fillNum = 1.0f;
    }
    public VRoundRect(long x, long y, int z, long w, long h, Color c, Color bc, float alpha, int aw, int ah, float n){
    	this(x, y, z, w, h, c, bc, alpha, aw, ah);
        fillNum = n;
    }

    public void initCams(int nbCam){
	pc=new ProjRoundRect[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new ProjRoundRect();
	}
    }

    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		ProjRoundRect[] ta=pc;
		pc=new ProjRoundRect[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new ProjRoundRect();
	    }
	    else {System.err.println("VRoundRect:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new ProjRoundRect[1];
		pc[0]=new ProjRoundRect();
	    }
	    else {System.err.println("VRoundRect:Error while adding camera "+verifIndex);}
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

    public float getOrient(){return 0;}

    /** Cannot be reoriented. */
    public void orientTo(float angle){}

    public float getSize(){return size;}

    public long getWidth(){return vw;}

    public long getHeight(){return vh;}

    void computeSize(){
	size=(float)Math.sqrt(Math.pow(vw,2)+Math.pow(vh,2));
    }

    public void sizeTo(float radius){  //new bounding circle radius
	size=radius;
	vw=(long)Math.round((size*ar)/(Math.sqrt(Math.pow(ar,2)+1)));
	vh=(long)Math.round((size)/(Math.sqrt(Math.pow(ar,2)+1)));
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void setWidth(long w){ 
	vw=w;
	ar=(float)vw/(float)vh;
	computeSize();
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void setHeight(long h){
	vh=h;
	ar=(float)vw/(float)vh;
	computeSize();
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void reSize(float factor){ //resizing factor
	size*=factor;
	vw=(long)Math.round((size*ar)/(Math.sqrt(Math.pow(ar,2)+1)));
	vh=(long)Math.round((size)/(Math.sqrt(Math.pow(ar,2)+1)));
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

	/** Get the bounding box of this Glyph in virtual space coordinates.
	 *@return west, north, east and south bounds in virtual space.
	 */
	public long[] getBounds(){
		long[] res = {vx-vw,vy+vh,vx+vw,vy-vh};
		return res;
	}

    /**
     * set horizontal diameter of the arc at the four corners
     */
    public void setArcWidth(int w){
	arcWidth=(w>=0) ? w : 0;
    }

    /**
     * set vertical diameter of the arc at the four corners
     */
    public void setArcHeight(int h){
	arcHeight=(h>=0) ? h : 0;
    }

    /**
     * get horizontal diameter of the arc at the four corners
     */
    public int getArcWidth(){
	return arcWidth;
    }

    /**
     * get vertical diameter of the arc at the four corners
     */
    public int getArcHeight(){
	return arcHeight;
    }

    public boolean fillsView(long w,long h,int camIndex){
        if ((alphaC == null) &&
            (w<=pc[camIndex].cx+pc[camIndex].cw) && (0>=pc[camIndex].cx-pc[camIndex].cw) &&
            (h<=pc[camIndex].cy+pc[camIndex].ch) && (0>=pc[camIndex].cy-pc[camIndex].ch)){return true;}
        else {return false;}
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
        pc[i].cw = (int)Math.round(Math.ceil(vw*coef));
        pc[i].ch = (int)Math.round(Math.ceil(vh*coef));
        pc[i].aw=Math.round(arcWidth*coef);
        pc[i].ah=Math.round(arcHeight*coef);
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
        int i=c.getIndex();
        coef=(float)(c.focal/(c.focal+c.altitude)) * lensMag;
        //find coordinates of object's geom center wrt to camera center and project
        //translate in JPanel coords
        pc[i].lcx = (lensWidth/2) + Math.round((vx-(lensx))*coef);
        pc[i].lcy = (lensHeight/2) - Math.round((vy-(lensy))*coef);
        //project width and height
        pc[i].lcw = (int)Math.round(Math.ceil(vw*coef));
        pc[i].lch = (int)Math.round(Math.ceil(vh*coef));
        pc[i].law=Math.round(arcWidth*coef);
        pc[i].lah=Math.round(arcHeight*coef);
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alphaC != null && alphaC.getAlpha()==0){return;}
        if ((pc[i].cw>=1) || (pc[i].ch>=1)) {
            //repaint only if object is visible
            if (alphaC != null){
                g.setComposite(alphaC);
                if (filled) {
                    g.setColor(this.color);
                    g.fillRoundRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch,pc[i].aw,pc[i].ah);
                }
                if (paintBorder){
                    g.setColor(borderColor);
                    if (stroke!=null) {
                        g.setStroke(stroke);
                        g.drawRoundRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1,pc[i].aw,pc[i].ah);
                        g.setStroke(stdS);
                    }
                    else {
                        g.drawRoundRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1,pc[i].aw,pc[i].ah);
                    }
                }
                g.setComposite(acO);
            }
            else {
                if (filled) {
                    g.setColor(this.color);
                    // Changed code
                    GradientPaint gp = new GradientPaint(
                    		dx+pc[i].cx-pc[i].cw + (pc[i].cw * fillNum), 0, 
                    		this.color, 
                    		dx+pc[i].cx-pc[i].cw + (2*pc[i].cw * fillNum), 0, 
                    		Color.WHITE);
                	g.setPaint(gp);
                	g.fillRoundRect(dx+pc[i].cx-pc[i].cw, dy+pc[i].cy-pc[i].ch, 2*pc[i].cw, 2*pc[i].ch, pc[i].aw, pc[i].ah);

                }
                if (paintBorder){
                    g.setColor(borderColor);
                    if (stroke!=null) {
                        g.setStroke(stroke);
                        g.drawRoundRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1,pc[i].aw,pc[i].ah);
                        g.setStroke(stdS);
                    }
                    else {
                        g.drawRoundRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1,pc[i].aw,pc[i].ah);
                    }
                }
            }
        }
        else {
            g.setColor(this.color);
            if (alphaC != null){
                g.setComposite(alphaC);
                g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
                g.setComposite(acO);
            }
            else {
                g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
            }
        }
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alphaC != null && alphaC.getAlpha()==0){return;}
        if ((pc[i].lcw>=1) || (pc[i].lch>=1)) {
            //repaint only if object is visible
            if (alphaC != null){
                g.setComposite(alphaC);
                if (filled) {
                    g.setColor(this.color);
                    g.fillRoundRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch,pc[i].law,pc[i].lah);
                }
                if (paintBorder){
                    g.setColor(borderColor);
                    if (stroke!=null) {
                        g.setStroke(stroke);
                        g.drawRoundRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw-1,2*pc[i].lch-1,pc[i].law,pc[i].lah);
                        g.setStroke(stdS);
                    }
                    else {
                        g.drawRoundRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw-1,2*pc[i].lch-1,pc[i].law,pc[i].lah);
                    }
                }
                g.setComposite(acO);
            }
            else {
                if (filled) {
                    g.setColor(this.color);
                    g.fillRoundRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch,pc[i].law,pc[i].lah);
                }
                if (paintBorder){
                    g.setColor(borderColor);
                    if (stroke!=null) {
                        g.setStroke(stroke);
                        g.drawRoundRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw-1,2*pc[i].lch-1,pc[i].law,pc[i].lah);
                        g.setStroke(stdS);
                    }
                    else {
                        g.drawRoundRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw-1,2*pc[i].lch-1,pc[i].law,pc[i].lah);
                    }
                }
            }
        }
        else {
            g.setColor(this.color);
            if (alphaC != null){
                g.setComposite(alphaC);
                g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
                g.setComposite(acO);
            }
            else {
                g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
            }
        }
    }

    public Object clone(){
        VRoundRect res = new VRoundRect(vx, vy, 0, vw, vh, color, borderColor, (alphaC != null) ? alphaC.getAlpha() : 1.0f, arcWidth, arcHeight);
        res.cursorInsideColor=this.cursorInsideColor;
        return res;
    }
    
    //Begin changed code
    public void setFillNum(float num)
    {
    	fillNum = num;
    }
    
    public float getFillNum()
    {
    	return fillNum;
    }
    //End changed code

}
