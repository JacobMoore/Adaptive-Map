/*   FILE: VQdCurve.java
 *   DATE OF CREATION:   Oct 02 2001
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
 * $Id: VQdCurve.java 3443 2010-07-28 08:57:10Z epietrig $
 */

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import fr.inria.zvtm.glyphs.projection.ProjQdCurve;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;

/**
 * Cubic Curve: a curved segment that has two endpoints and one control point.
 * The control point determines the shape of the curve by controlling both of the endpoint tangent vectors <br> for this particular glyph, vx and vy correspond to the center of the imaginary segment linking the curve's start and end points <br> the coordinates of the control point are expressed w.r.t this point in polar coordinates (orient=0 on segment linking start and end points, meaning that if orient=0, start control and end points are aligned)  See <a href="ftp://ftp.inria.fr/INRIA/publication/Theses/TU-0769.pdf">ftp://ftp.inria.fr/INRIA/publication/Theses/TU-0769.pdf</a>, page 147 for a diagramatic explaination.
 *@see fr.inria.zvtm.glyphs.VCbCurve
 *@see fr.inria.zvtm.glyphs.DPath
 * @author Emmanuel Pietriga
 **/

public class VQdCurve extends Glyph {

    long vs;

    /*control point, polar coordinates - origin is vx,vy - orient=0 on segment linking start and end points*/
    long vrad;
    float ang;

    ProjQdCurve[] pc;

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param s size (width=height) in virtual space
     *@param c fill color
     *@param or orientation
     *@param ctrlDist1 distance of control point (polar coords origin=(x,y) provided in this constructor)
     *@param or1 orientation of control point (polar coords origin=(x,y) provided in this constructor)
     */
    public VQdCurve(long x,long y, int z,long s,Color c,float or,long ctrlDist1,float or1){
	vx=x;
	vy=y;
	vz=z;
	vs=s;
	sensit=false;
	orient=or;
	vrad=ctrlDist1;
	ang=or1;
	computeSize();
	setColor(c);
    }

    /** Set position of control point (polar coords w.r.t center of segment linking start and end points). */
    public void setCtrlPoint(long d,float o){
	vrad=d;
	ang=o;
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    /** Get distance from center of segment linking start and end points to control point (polar coords). */
    public long getCtrlPointRadius(){return vrad;}

    /** Get orientation of control point (polar coords). */
    public float getCtrlPointAngle(){return ang;}

    public void initCams(int nbCam){
	pc=new ProjQdCurve[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new ProjQdCurve();
	}
    }

    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		ProjQdCurve[] ta=pc;
		pc=new ProjQdCurve[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new ProjQdCurve();
	    }
	    else {System.err.println("VQdCurve:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new ProjQdCurve[1];
		pc[0]=new ProjQdCurve();
	    }
	    else {System.err.println("VQdCurve:Error while adding camera "+verifIndex);}
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
	vrad=Math.round(vrad*radius/size);
	size=radius;
	vs=Math.round(size);
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void reSize(float factor){
	size*=factor;
	vs=(long)Math.round(size);
	vrad=Math.round(vrad*factor);
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
	
    /** The cursor is never considered as being inside a cubic curve. Glyph entry/exit are never fired by a VQdCurve. */
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
	    pc[i].ctrl.setLocation(pc[i].cx+(int)Math.round(coef*vrad*Math.cos(orient-ang)),pc[i].cy+(int)Math.round(coef*vrad*Math.sin(orient-ang)));
	    pc[i].curve.setCurve(pc[i].start,pc[i].ctrl,pc[i].end);
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
	    pc[i].lctrl.setLocation(pc[i].lcx+(int)Math.round(coef*vrad*Math.cos(orient-ang)),pc[i].lcy+(int)Math.round(coef*vrad*Math.sin(orient-ang)));
	    pc[i].lcurve.setCurve(pc[i].lstart,pc[i].lctrl,pc[i].lend);
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
                    g.translate(dx, dy);
                    g.draw(pc[i].curve);
                    g.translate(-dx, -dy);
                    g.setStroke(stdS);
                }
                else {
                    g.translate(dx, dy);
                    g.draw(pc[i].curve);
                    g.translate(-dx, -dy);
                }
                g.setComposite(acO);
            }
            else {
                if (stroke!=null) {
                    g.setStroke(stroke);
                    g.translate(dx, dy);
                    g.draw(pc[i].curve);
                    g.translate(-dx, -dy);
                    g.setStroke(stdS);
                }
                else {
                    g.translate(dx, dy);
                    g.draw(pc[i].curve);
                    g.translate(-dx, -dy);
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
                    g.translate(dx, dy);
                    g.draw(pc[i].lcurve);
                    g.translate(-dx, -dy);
                    g.setStroke(stdS);
                }
                else {
                    g.translate(dx, dy);
                    g.draw(pc[i].lcurve);
                    g.translate(-dx, -dy);
                }
                g.setComposite(acO);
            }
            else {
                if (stroke!=null) {
                    g.setStroke(stroke);
                    g.translate(dx, dy);
                    g.draw(pc[i].lcurve);
                    g.translate(-dx, -dy);
                    g.setStroke(stdS);
                }
                else {
                    g.translate(dx, dy);
                    g.draw(pc[i].lcurve);
                    g.translate(-dx, -dy);
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
        VQdCurve res=new VQdCurve(vx,vy,0,vs,color,orient,vrad,ang);
        res.cursorInsideColor=this.cursorInsideColor;
        res.setTranslucencyValue(getTranslucencyValue());
        return res;
    }

    public void highlight(boolean b, Color selectedColor){}

}

