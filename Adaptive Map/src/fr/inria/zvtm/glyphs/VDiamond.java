/*   FILE: VDiamond.java
 *   DATE OF CREATION:   Jul 27 2000
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
 *
 * $Id: VDiamond.java 3443 2010-07-28 08:57:10Z epietrig $
 */

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import fr.inria.zvtm.glyphs.projection.BProjectedCoordsP;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;

/**
 * Diamond (losange with height equal to width). This version is the most efficient, but it can neither be reoriented (see VDiamondOr*) nor made translucent (see VDiamond*ST).
 * @author Emmanuel Pietriga
 *@see fr.inria.zvtm.glyphs.VDiamondOr
 */

public class VDiamond extends ClosedShape {

    /*vertex x coords*/
    int[] xcoords = new int[4];
    /*vertex y coords*/
    int[] ycoords = new int[4];

    /*height=width in virtual space*/
    long vs;

    /*array of projected coordinates - index of camera in virtual space is equal to index of projected coords in this array*/
    BProjectedCoordsP[] pc;

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param s size (width=height) in virtual space
     *@param c fill color
     */
    public VDiamond(long x,long y, int z,long s,Color c){
	    this(x, y, z, s, c, Color.BLACK, 1f);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param s size (width=height) in virtual space
     *@param c fill color
     *@param bc border color
     */
    public VDiamond(long x, long y, int z, long s, Color c, Color bc){
        this(x, y, z, s, c, bc, 1f);
    }
    
    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param s size (width=height) in virtual space
     *@param c fill color
     *@param bc border color
      *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
     */
    public VDiamond(long x, long y, int z, long s, Color c, Color bc, float alpha){
        vx=x;
        vy=y;
        vz=z;
        vs=s;
        computeSize();
        orient=0;
        setColor(c);
        setBorderColor(bc);
        setTranslucencyValue(alpha);
    }

    public void initCams(int nbCam){
	pc=new BProjectedCoordsP[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new BProjectedCoordsP();
	}
    }

    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		BProjectedCoordsP[] ta=pc;
		pc=new BProjectedCoordsP[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new BProjectedCoordsP();
	    }
	    else {System.err.println("VDiamond:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new BProjectedCoordsP[1];
		pc[0]=new BProjectedCoordsP();
	    }
	    else {System.err.println("VDiamond:Error while adding camera "+verifIndex);}
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

    void computeSize(){
	size=(float)vs;
    }

    public void sizeTo(float radius){
	size=radius;
	vs=Math.round(size);
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void reSize(float factor){
	size*=factor;
	vs=(long)Math.round(size);
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public boolean fillsView(long w,long h,int camIndex){
        return ((alphaC == null) &&
            (pc[camIndex].p.contains(0,0)) && (pc[camIndex].p.contains(w,0)) &&
            (pc[camIndex].p.contains(0,h)) && (pc[camIndex].p.contains(w,h)));
    }

    public boolean coordInside(int jpx, int jpy, int camIndex, long cvx, long cvy){
        if (pc[camIndex].p.contains(jpx, jpy)){return true;}
        else {return false;}
    }

    /** The disc is actually approximated to its bounding box here. Precise intersection computation would be too costly. */
	public boolean visibleInDisc(long dvx, long dvy, long dvr, Shape dvs, int camIndex, int jpx, int jpy, int dpr){
		return pc[camIndex].p.intersects(jpx-dpr, jpy-dpr, 2*dpr, 2*dpr);
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
	//project height and construct polygon
	pc[i].cr=Math.round(vs*coef);
	xcoords[0] = pc[i].cx+pc[i].cr;
	ycoords[0] = pc[i].cy;
	xcoords[1] = pc[i].cx;
	ycoords[1] = pc[i].cy+pc[i].cr;
	xcoords[2] = pc[i].cx-pc[i].cr;
	ycoords[2] = pc[i].cy;
	xcoords[3] = pc[i].cx;
	ycoords[3] = pc[i].cy-pc[i].cr;
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
	//project height and construct polygon
	pc[i].lcr=Math.round(vs*coef);
	xcoords[0] = pc[i].lcx+pc[i].lcr;
	ycoords[0] = pc[i].lcy;
	xcoords[1] = pc[i].lcx;
	ycoords[1] = pc[i].lcy+pc[i].lcr;
	xcoords[2] = pc[i].lcx-pc[i].lcr;
	ycoords[2] = pc[i].lcy;
	xcoords[3] = pc[i].lcx;
	ycoords[3] = pc[i].lcy-pc[i].lcr;
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
        if (pc[i].cr>1){
            //repaint only if object is visible
            if (alphaC != null){
                g.setComposite(alphaC);
                if (filled){
                    g.setColor(this.color);
                    g.translate(dx, dy);
                    g.fillPolygon(pc[i].p);
                    g.translate(-dx, -dy);
                }
                if (paintBorder){
                    g.setColor(borderColor);
                    if (stroke!=null) {
                        g.setStroke(stroke);
                        g.translate(dx, dy);
                        g.drawPolygon(pc[i].p);
                        g.translate(-dx, -dy);
                        g.setStroke(stdS);
                    }
                    else {
                        g.translate(dx, dy);
                        g.drawPolygon(pc[i].p);
                        g.translate(-dx, -dy);
                    }
                }
                g.setComposite(acO);
            }
            else {
                if (filled){
                    g.setColor(this.color);
                    g.translate(dx, dy);
                    g.fillPolygon(pc[i].p);
                    g.translate(-dx, -dy);
                }
                if (paintBorder){
                    g.setColor(borderColor);
                    if (stroke!=null) {
                        g.setStroke(stroke);
                        g.translate(dx, dy);
                        g.drawPolygon(pc[i].p);
                        g.translate(-dx, -dy);
                        g.setStroke(stdS);
                    }
                    else {
                        g.translate(dx, dy);
                        g.drawPolygon(pc[i].p);
                        g.translate(-dx, -dy);
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
        if (pc[i].lcr>1){
            //repaint only if object is visible
            if (alphaC != null){
                g.setComposite(alphaC);
                if (filled){
                    g.setColor(this.color);
                    g.translate(dx, dy);
                    g.fillPolygon(pc[i].lp);
                    g.translate(-dx, -dy);
                }
                if (paintBorder){
                    g.setColor(borderColor);
                    if (stroke!=null) {
                        g.setStroke(stroke);
                        g.translate(dx, dy);
                        g.drawPolygon(pc[i].lp);
                        g.translate(-dx, -dy);
                        g.setStroke(stdS);
                    }
                    else {
                        g.translate(dx, dy);
                        g.drawPolygon(pc[i].lp);
                        g.translate(-dx, -dy);
                    }
                }
                g.setComposite(acO);
            }
            else {
                if (filled){
                    g.setColor(this.color);
                    g.translate(dx, dy);
                    g.fillPolygon(pc[i].lp);
                    g.translate(-dx, -dy);
                }
                if (paintBorder){
                    g.setColor(borderColor);
                    if (stroke!=null) {
                        g.setStroke(stroke);
                        g.translate(dx, dy);
                        g.drawPolygon(pc[i].lp);
                        g.translate(-dx, -dy);
                        g.setStroke(stdS);
                    }
                    else {
                        g.translate(dx, dy);
                        g.drawPolygon(pc[i].lp);
                        g.translate(-dx, -dy);
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
        VDiamond res=new VDiamond(vx,vy,0,vs,color, getBorderColor(), (alphaC != null) ? alphaC.getAlpha(): 1f);
        res.cursorInsideColor=this.cursorInsideColor;
        res.bColor=this.bColor;
        return res;
    }

}

