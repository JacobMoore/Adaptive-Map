/*   FILE: VSegment.java
 *   DATE OF CREATION:   Jul 20 2000
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
 * $Id: VSegment.java 3443 2010-07-28 08:57:10Z epietrig $
 */

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.AffineTransform;

import fr.inria.zvtm.glyphs.projection.RProjectedCoords;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpaceManager;


/**
 * Segment (straight line). This version is the most efficient, but it cannot be made translucent (see VSegmentST).
 * @author Emmanuel Pietriga
 */

public class VSegment extends Glyph implements RectangularShape {

    /*half width and height in virtual space*/
    long vw,vh;

    RProjectedCoords[] pc;

    /**
     *give the centre of segment and half its projected length on X & Y axis
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param w half width in virtual space (can be negative)
     *@param h half height in virtual space (can be negative)
     *@param c fill color
     */
    public VSegment(long x, long y, int z, long w, long h, Color c){
        this(x, y, z, w, h, c, 1f);
    }
    
    /**
     *give the centre of segment and half its projected length on X & Y axis
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param w half width in virtual space (can be negative)
     *@param h half height in virtual space (can be negative)
     *@param c fill color
      *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
     */
    public VSegment(long x, long y, int z, long w, long h, Color c, float alpha){
        vx = x;
        vy = y;
        vz = z;
        vw = w;
        vh = h;
        computeSize();
        setColor(c);
        setTranslucencyValue(alpha);
    }

    /**
     *give the end points of segment
     *@param x1 coordinate of endpoint 1 in virtual space
     *@param y1 coordinate of endpoint 1 in virtual space
     *@param x2 coordinate of endpoint 2 in virtual space
     *@param y2 coordinate of endpoint 2 in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param c fill color
     */
    public VSegment(long x1, long y1, int z, Color c, long x2, long y2){
        this(x1, y1, z, c, x2, y2, 1f);
    }
    
    /**
     *give the end points of segment
     *@param x1 coordinate of endpoint 1 in virtual space
     *@param y1 coordinate of endpoint 1 in virtual space
     *@param x2 coordinate of endpoint 2 in virtual space
     *@param y2 coordinate of endpoint 2 in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param c fill color
      *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
     */
    public VSegment(long x1, long y1, int z, Color c, long x2, long y2, float alpha){
        vx = (x1 + x2) / 2;
        vy = (y1 + y2) / 2;
        vz = z;
        vw = (x2 - x1) / 2;
        vh = (y2 - y1) / 2;
        computeSize();
        setColor(c);
        setTranslucencyValue(alpha);
    }

    /**
     *give the centre of segment and half its length & orient
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param lgth half length in virtual space
     *@param angle orientation
     *@param c fill color
     */
    public VSegment(long x, long y, int z, float lgth, float angle, Color c){
        this(x, y, z, lgth, angle, c, 1f);
    }
    
    /**
     *give the centre of segment and half its length & orient
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param lgth half length in virtual space
     *@param angle orientation
     *@param c fill color
      *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
     */
    public VSegment(long x, long y, int z, float lgth, float angle, Color c, float alpha){
        vx = x;
        vy = y;
        vz = z;
        orient = angle;
        size = lgth;
        computeEdges();
        setColor(c);
        setTranslucencyValue(alpha);
    }

    public void initCams(int nbCam){
	pc=new RProjectedCoords[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new RProjectedCoords();
	}
    }

    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		RProjectedCoords[] ta=pc;
		pc=new RProjectedCoords[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new RProjectedCoords();
	    }
	    else {System.err.println("VSegment:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new RProjectedCoords[1];
		pc[0]=new RProjectedCoords();
	    }
	    else {System.err.println("VSegment:Error while adding camera "+verifIndex);}
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
    }

    public float getOrient(){return orient;}

    public void orientTo(float angle){
	orient=angle;
	computeEdges();
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public float getSize(){return size;}

	/** Get the bounding box of this Glyph in virtual space coordinates.
	 *@return west, north, east and south bounds in virtual space.
	 */
	public long[] getBounds(){
		long[] res = {vx-vw,vy+vh,vx+vw,vy-vh};
		return res;
	}

    /** Change the segment's location, size and orientation by giving its two endpoints (absolute coordinates). */
    public void setEndPoints(long x1, long y1, long x2, long y2){
	vx = (x1 + x2) / 2;
	vy = (y1 + y2) / 2;
	vw = (x2 - x1) / 2;
	vh = (y2 - y1) / 2;
	computeSize();
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    /** Get the segment's two endpoints
     *@return absolute coordinates.
     */
    public LongPoint[] getEndPoints(){
	LongPoint[] res = new LongPoint[2];
	res[0] = new LongPoint(vx+vw, vy+vh);
	res[1] = new LongPoint(vx-vw, vy-vh);
	return res;
    }
    
    void computeSize(){  
	size=(float)Math.sqrt(Math.pow(vw,2)+Math.pow(vh,2));
	if (vw!=0){orient=(float)Math.atan((vh/(float)vw));}
	else {
	    orient=(vh>0) ? (float)Math.PI/2.0f : (float)-Math.PI/2.0f ;
	}
	if (orient<0){
	    if (vh>0){orient=(float)Math.PI-orient;} 
	    else {orient=-orient;}
	}
	else if(orient>0){
	    if (vh>0){orient=2*(float)Math.PI-orient;}
	    else {orient=(float)Math.PI-orient;}
	}
	else if(orient==0 && vw<0){orient=(float)Math.PI;}	    
    }

    public long getWidth(){return vw;}

    public long getHeight(){return vh;}

    public void sizeTo(float radius){
	size=radius;
	computeEdges();
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void reSize(float factor){
	size*=factor;
	computeEdges();
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void setWidth(long w){ 
	vw=w;
	computeSize();
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void setHeight(long h){
	vh=h;
	computeSize();
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void setWidthHeight(long w,long h){
	this.vw=w;
	this.vh=h;
	computeSize();
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public boolean fillsView(long w,long h,int camIndex){
	return false;
    }

    void computeEdges(){
	vw=(long)Math.round(size*Math.cos(orient));
	vh=(long)Math.round(size*Math.sin(orient));
    }

    public boolean coordInside(int jpx, int jpy, int camIndex, long cvx, long cvy){
	    return false;
    }

    /** Detects whether the point (x,y) lies on the segment or not. Default tolerance of 2 pixels.
     *@param x EXPECTS PROJECTED JPanel COORDINATE (obtained e.g. in ViewEventHandler's mouse methods as jpx)
     *@param y EXPECTS PROJECTED JPanel COORDINATE (obtained e.g. in ViewEventHandler's mouse methods as jpy)
     *@param camIndex camera index (obtained through Camera.getIndex())
     */
    public boolean intersects(int x, int y, int camIndex){
	return intersects(x, y, 2, camIndex);
    }

    /** Detects whether the point (x,y) lies on the segment or not.
     *@param x EXPECTS PROJECTED JPanel COORDINATE (obtained e.g. in ViewEventHandler's mouse methods as jpx)
     *@param y EXPECTS PROJECTED JPanel COORDINATE (obtained e.g. in ViewEventHandler's mouse methods as jpy)
     *@param tolerance the segment's clickable thickness in pixels, not virtual space units
     *@param camIndex camera index (obtained through Camera.getIndex())
     */
    public boolean intersects(int x, int y, int tolerance, int camIndex){
	    return Line2D.ptSegDist(pc[camIndex].cx-pc[camIndex].cw, pc[camIndex].cy+pc[camIndex].ch,
	                            pc[camIndex].cx+pc[camIndex].cw, pc[camIndex].cy-pc[camIndex].ch,
	                            x, y) <= tolerance;
    }
    
    public boolean visibleInDisc(long dvx, long dvy, long dvr, Shape dvs, int camIndex, int jpx, int jpy, int dpr){
		return Line2D.ptSegDist(vx-vw, vy-vh, vx+vw, vy+vh, dvx, dvy) <= dvr;
	}

    public short mouseInOut(int jpx, int jpy, int camIndex, long cvx, long cvy){
	    return Glyph.NO_CURSOR_EVENT;
    }

    public void project(Camera c, Dimension d){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude));
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].cx=(d.width/2)+Math.round((vx-c.posx)*coef);
	pc[i].cy=(d.height/2)-Math.round((vy-c.posy)*coef);
	//project width and height
	pc[i].cw=Math.round(vw*coef);
	pc[i].ch=Math.round(vh*coef);
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i = c.getIndex();
	coef = ((float)(c.focal/(c.focal+c.altitude))) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].lcx = (lensWidth/2) + Math.round((vx-(lensx))*coef);
	pc[i].lcy = (lensHeight/2) - Math.round((vy-(lensy))*coef);
	//project width and height
	pc[i].lcw = Math.round(vw*coef);
	pc[i].lch = Math.round(vh*coef);
    }
    
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alphaC != null && alphaC.getAlpha()==0){return;}
        g.setColor(this.color);
        if (stroke!=null) {
            if (alphaC != null){
                g.setComposite(alphaC);
                g.setStroke(stroke);
                g.drawLine(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy+pc[i].ch,dx+pc[i].cx+pc[i].cw,dy+pc[i].cy-pc[i].ch);
                g.setStroke(stdS);
                g.setComposite(acO);
            }
            else {
                g.setStroke(stroke);
                g.drawLine(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy+pc[i].ch,dx+pc[i].cx+pc[i].cw,dy+pc[i].cy-pc[i].ch);
                g.setStroke(stdS);
            }
        }
        else if (alphaC != null){
            g.setComposite(alphaC);
            g.drawLine(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy+pc[i].ch,dx+pc[i].cx+pc[i].cw,dy+pc[i].cy-pc[i].ch);
            g.setComposite(acO);
        }
        else {
            g.drawLine(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy+pc[i].ch,dx+pc[i].cx+pc[i].cw,dy+pc[i].cy-pc[i].ch);	    
        }
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alphaC != null && alphaC.getAlpha()==0){return;}
        g.setColor(this.color);
        if (stroke!=null) {
            if (alphaC != null){
                g.setComposite(alphaC);
                g.setStroke(stroke);
                g.drawLine(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy+pc[i].lch,dx+pc[i].lcx+pc[i].lcw,dy+pc[i].lcy-pc[i].lch);
                g.setStroke(stdS);
                g.setComposite(acO);
            }
            else {
                g.setStroke(stroke);
                g.drawLine(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy+pc[i].lch,dx+pc[i].lcx+pc[i].lcw,dy+pc[i].lcy-pc[i].lch);
                g.setStroke(stdS);
            }
        }
        else if (alphaC != null){
            g.setComposite(alphaC);
            g.drawLine(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy+pc[i].lch,dx+pc[i].lcx+pc[i].lcw,dy+pc[i].lcy-pc[i].lch);
            g.setComposite(acO);
        }
        else {
            g.drawLine(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy+pc[i].lch,dx+pc[i].lcx+pc[i].lcw,dy+pc[i].lcy-pc[i].lch);
        }
    }

    public Object clone(){
        VSegment res = new VSegment(vx,vy,0,vw,vh,color, (alphaC != null) ? alphaC.getAlpha() : 1f);
        res.cursorInsideColor = this.cursorInsideColor;
        return res;
    }

    /** Highlight this glyph to give visual feedback when the cursor is inside it. */
    public void highlight(boolean b, Color selectedColor){
        boolean update = false;
        if (b){
            if (cursorInsideColor != null){color = cursorInsideColor;update = true;}
        }
        else {
            if (isSelected() && selectedColor != null){
                color = selectedColor;
                update = true;
            }
            else {
                if (cursorInsideColor != null){color = fColor;update = true;}
            }
        }
        if (update){
		VirtualSpaceManager.INSTANCE.repaintNow();
        }
    }

}
