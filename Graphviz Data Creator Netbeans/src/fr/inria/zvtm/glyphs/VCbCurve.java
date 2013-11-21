/*   FILE: VCbCurve.java
 *   DATE OF CREATION:   Oct 03 2001
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
 * $Id: VCbCurve.java 3443 2010-07-28 08:57:10Z epietrig $
 */

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import fr.inria.zvtm.glyphs.projection.ProjCbCurve;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;


/**
 * Cubic Curve: a curved segment that has two endpoints and two control points.
 * Each control point determines the shape of the curve by controlling one of the endpoint tangent vectors. <br> For this particular glyph, vx and vy correspond to the center of the imaginary segment linking the curve's start and end points. <br> The coordinates of the control points are expressed respectively w.r.t start and end points in polar coordinates (orient=0 on segment linking start and end points, meaning that if orient=0 for both control points, start control1 control2 and end points are aligned). See <a href="ftp://ftp.inria.fr/INRIA/publication/Theses/TU-0769.pdf">ftp://ftp.inria.fr/INRIA/publication/Theses/TU-0769.pdf</a>, page 147 for a diagramatic explaination.
 *@see fr.inria.zvtm.glyphs.VQdCurve
 *@see fr.inria.zvtm.glyphs.DPath
 * @author Emmanuel Pietriga
 **/

public class VCbCurve extends Glyph {

    long vs;

    /*control points, polar coordinates - origin is vx,vy - orient=0 on segment linking start and end points*/
    long vrad1;
    float ang1;
    long vrad2;
    float ang2;

    ProjCbCurve[] pc;

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param s size (distance between start and end points) in virtual space
     *@param c color
     *@param or orientation
     *@param ctrlDist1 distance of control point (polar coords origin=start point)
     *@param or1 orientation of control point (polar coords origin=start point)
     *@param ctrlDist2 distance of control point (polar coords origin=end point)
     *@param or2 orientation of control point (polar coords origin=end point)
     */
    public VCbCurve(long x,long y, int z,long s,Color c,float or,long ctrlDist1,float or1,long ctrlDist2,float or2){
	vx=x;
	vy=y;
	vz=z;
	vs=s;
	sensit=false;
	orient=or;
	vrad1=ctrlDist1;
	ang1=or1;
	vrad2=ctrlDist2;
	ang2=or2;
	computeSize();
	setColor(c);
    }

    /** Set position of control point 1 (polar coords w.r.t start point). */
    public void setCtrlPoint1(long d,float o){
	vrad1=d;
	ang1=o;
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    /** Set position of control point 2 (polar coords w.r.t end point). */
    public void setCtrlPoint2(long d,float o){
	vrad2=d;
	ang2=o;
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    /** Get distance from start point to control point 1 (polar coords). */
    public long getCtrlPointRadius1(){return vrad1;}

    /** Get orientation of control point 1 (polar coords). */
    public float getCtrlPointAngle1(){return ang1;}

    /** Get distance from start point to control point 2 (polar coords). */
    public long getCtrlPointRadius2(){return vrad2;}

    /** Get orientation of control point 2 (polar coords). */
    public float getCtrlPointAngle2(){return ang2;}

    public void initCams(int nbCam){
	pc=new ProjCbCurve[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new ProjCbCurve();
	}
    }

    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		ProjCbCurve[] ta=pc;
		pc=new ProjCbCurve[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new ProjCbCurve();
	    }
	    else {System.err.println("VCbCurve:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new ProjCbCurve[1];
		pc[0]=new ProjCbCurve();
	    }
	    else {System.err.println("VCbCurve:Error while adding camera "+verifIndex);}
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
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public float getSize(){return size;}

    void computeSize(){
	size=(float)vs;
    }

    public void sizeTo(float radius){
	vrad1=Math.round(vrad1*radius/size);
	vrad2=Math.round(vrad2*radius/size);
	size=radius;
	vs=Math.round(size);
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void reSize(float factor){
	size*=factor;
	vs=(long)Math.round(size);
	vrad1=Math.round(vrad1*factor);
	vrad2=Math.round(vrad2*factor);
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public boolean fillsView(long w,long h,int camIndex){
	return false;
    }

    public boolean coordInside(int jpx, int jpy, int camIndex, long cvx, long cvy){
	    return false;
    }

    /** The disc is actually approximated to its bounding box here. Precise intersection computation would be too costly. */
	public boolean visibleInDisc(long dvx, long dvy, long dvr, Shape dvs, int camIndex, int jpx, int jpy, int dpr){
		return pc[camIndex].curve.intersects(jpx-dpr, jpy-dpr, 2*dpr, 2*dpr);
	}

    /** The cursor is never considered as being inside a cubic curve. Glyph entry/exit are never fired by a VCbCurve. */
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
	//project height and construct curve
	pc[i].cr=Math.round(vs*coef);
	if (pc[i].cr>1){
	    pc[i].start.setLocation(pc[i].cx+pc[i].cr*Math.cos(orient),pc[i].cy+pc[i].cr*Math.sin(orient));
	    pc[i].end.setLocation(pc[i].cx-pc[i].cr*Math.cos(orient),pc[i].cy-pc[i].cr*Math.sin(orient));
	    
	    pc[i].ctrlStart.setLocation(pc[i].start.x+(int)Math.round(coef*vrad1*Math.cos(orient-ang1)),pc[i].start.y+(int)Math.round(coef*vrad1*Math.sin(orient-ang1)));
	    pc[i].ctrlEnd.setLocation(pc[i].end.x+(int)Math.round(coef*vrad2*Math.cos(orient-ang2)),pc[i].end.y+(int)Math.round(coef*vrad2*Math.sin(orient-ang2)));
	    pc[i].curve.setCurve(pc[i].start,pc[i].ctrlStart,pc[i].ctrlEnd,pc[i].end);
	}
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude)) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].lcx = (lensWidth/2) + Math.round((vx-(lensx))*coef);
	pc[i].lcy = (lensHeight/2) - Math.round((vy-(lensy))*coef);
	//project height and construct curve
	pc[i].lcr=Math.round(vs*coef);
	if (pc[i].lcr>1){
	    pc[i].lstart.setLocation(pc[i].lcx+pc[i].lcr*Math.cos(orient),pc[i].lcy+pc[i].lcr*Math.sin(orient));
	    pc[i].lend.setLocation(pc[i].lcx-pc[i].lcr*Math.cos(orient),pc[i].lcy-pc[i].lcr*Math.sin(orient));
	    
	    pc[i].lctrlStart.setLocation(pc[i].lstart.x+(int)Math.round(coef*vrad1*Math.cos(orient-ang1)),pc[i].lstart.y+(int)Math.round(coef*vrad1*Math.sin(orient-ang1)));
	    pc[i].lctrlEnd.setLocation(pc[i].lend.x+(int)Math.round(coef*vrad2*Math.cos(orient-ang2)),pc[i].lend.y+(int)Math.round(coef*vrad2*Math.sin(orient-ang2)));
	    pc[i].lcurve.setCurve(pc[i].lstart,pc[i].lctrlStart,pc[i].lctrlEnd,pc[i].lend);
	}
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alphaC != null && alphaC.getAlpha()==0){return;}
        g.setColor(this.color);
        if (pc[i].cr >1){
            //repaint only if object is visible
            if (alphaC != null){
                g.setComposite(alphaC);
                if (stroke!=null) {
                    g.setStroke(stroke);
                    g.translate(dx,dy);
                    g.draw(pc[i].curve);
                    g.translate(-dx,-dy);
                    g.setStroke(stdS);
                }
                else {
                    g.translate(dx,dy);
                    g.draw(pc[i].curve);
                    g.translate(-dx,-dy);
                }                
                g.setComposite(acO);
            }
            else {
                if (stroke!=null) {
                    g.setStroke(stroke);
                    g.translate(dx,dy);
                    g.draw(pc[i].curve);
                    g.translate(-dx,-dy);
                    g.setStroke(stdS);
                }
                else {
                    g.translate(dx,dy);
                    g.draw(pc[i].curve);
                    g.translate(-dx,-dy);
                }
            }
        }
        else {
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
        g.setColor(this.color);
        if (pc[i].lcr >1){
            //repaint only if object is visible
            if (alphaC != null){
                g.setComposite(alphaC);
                if (stroke!=null) {
                    g.setStroke(stroke);
                    g.translate(dx,dy);
                    g.draw(pc[i].lcurve);
                    g.translate(-dx,-dy);
                    g.setStroke(stdS);
                }
                else {
                    g.translate(dx,dy);
                    g.draw(pc[i].lcurve);
                    g.translate(-dx,-dy);
                }                
                g.setComposite(acO);
            }
            else {
                if (stroke!=null) {
                    g.setStroke(stroke);
                    g.translate(dx,dy);
                    g.draw(pc[i].lcurve);
                    g.translate(-dx,-dy);
                    g.setStroke(stdS);
                }
                else {
                    g.translate(dx,dy);
                    g.draw(pc[i].lcurve);
                    g.translate(-dx,-dy);
                }
            }
        }
        else {
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
        VCbCurve res = new VCbCurve(vx,vy,0,vs,color,orient,vrad1,ang1,vrad2,ang2);
        res.cursorInsideColor = this.cursorInsideColor;
        res.setTranslucencyValue(getTranslucencyValue());
        return res;
    }

    public void highlight(boolean b, Color selectedColor){}

}

