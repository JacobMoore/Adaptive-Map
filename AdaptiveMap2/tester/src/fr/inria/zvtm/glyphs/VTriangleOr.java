/*   FILE: VTriangleOr.java
 *   DATE OF CREATION:   Jul 25 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2007. All Rights Reserved
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
 * $Id: VTriangleOr.java 3443 2010-07-28 08:57:10Z epietrig $
 */

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;

/**
 * Re-orientable Equilateral Triangle. This version is less efficient than VTriangle, but it can be reoriented.
 * @author Emmanuel Pietriga
 *@see fr.inria.zvtm.glyphs.VTriangle
 */

public class VTriangleOr extends VTriangle {

    /**
        *@param x coordinate in virtual space
        *@param y coordinate in virtual space
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param h height in virtual space
        *@param c fill color
        *@param or orientation
        */
    public VTriangleOr(long x,long y, int z,long h,Color c,float or){
        this(x, y, z, h, c, Color.BLACK, or, 1f);
    }

    /**
        *@param x coordinate in virtual space
        *@param y coordinate in virtual space
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param h height in virtual space
        *@param c fill color
        *@param bc border color
        *@param or orientation
        *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
        */
    public VTriangleOr(long x, long y, int z, long h, Color c, Color bc, float or, float alpha){
        super(x, y, z, h, c, bc);
        orient = or;
    }

    public float getOrient(){return orient;}

    /** Set the glyph's absolute orientation.
     *@param angle in [0:2Pi[ 
     */
    public void orientTo(float angle){
	orient=angle;
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void project(Camera c, Dimension d){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude));
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].cx=(d.width/2)+Math.round((vx-c.posx)*coef);
	pc[i].cy=(d.height/2)-Math.round((vy-c.posy)*coef);
	//project height and construct polygon
	pc[i].cr=Math.round(vh*coef);
	pc[i].halfEdge=Math.round(halfEdgeFactor*pc[i].cr);
	pc[i].thirdHeight=Math.round(thirdHeightFactor*pc[i].cr);
	xcoords[0]=(int)Math.round(pc[i].cx-pc[i].cr*Math.sin(orient));
	xcoords[1]=(int)Math.round(pc[i].cx-pc[i].halfEdge*Math.cos(orient)+pc[i].thirdHeight*Math.sin(orient));
	xcoords[2]=(int)Math.round(pc[i].cx+pc[i].halfEdge*Math.cos(orient)+pc[i].thirdHeight*Math.sin(orient));
	ycoords[0]=(int)Math.round(pc[i].cy-pc[i].cr*Math.cos(orient));
	ycoords[1]=(int)Math.round(pc[i].cy+pc[i].thirdHeight*Math.cos(orient)+pc[i].halfEdge*Math.sin(orient));
	ycoords[2]=(int)Math.round(pc[i].cy+pc[i].thirdHeight*Math.cos(orient)-pc[i].halfEdge*Math.sin(orient));
	if (pc[i].p == null){
	    pc[i].p = new Polygon(xcoords, ycoords, 3);
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
	pc[i].lcx = lensWidth/2 + Math.round((vx-lensx)*coef);
	pc[i].lcy = lensHeight/2 - Math.round((vy-lensy)*coef);
	//project height and construct polygon
	pc[i].lcr=Math.round(vh*coef);
	pc[i].lhalfEdge=Math.round(halfEdgeFactor*pc[i].lcr);
	pc[i].lthirdHeight=Math.round(thirdHeightFactor*pc[i].lcr);
	xcoords[0]=(int)Math.round(pc[i].lcx-pc[i].lcr*Math.sin(orient));
	xcoords[1]=(int)Math.round(pc[i].lcx-pc[i].lhalfEdge*Math.cos(orient)+pc[i].lthirdHeight*Math.sin(orient));
	xcoords[2]=(int)Math.round(pc[i].lcx+pc[i].lhalfEdge*Math.cos(orient)+pc[i].lthirdHeight*Math.sin(orient));
	ycoords[0]=(int)Math.round(pc[i].lcy-pc[i].lcr*Math.cos(orient));
	ycoords[1]=(int)Math.round(pc[i].lcy+pc[i].lthirdHeight*Math.cos(orient)+pc[i].lhalfEdge*Math.sin(orient));
	ycoords[2]=(int)Math.round(pc[i].lcy+pc[i].lthirdHeight*Math.cos(orient)-pc[i].lhalfEdge*Math.sin(orient));
	if (pc[i].lp == null){
	    pc[i].lp = new Polygon(xcoords, ycoords, 3);
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
        VTriangleOr res = new VTriangleOr(vx, vy, 0, vh, color, borderColor, orient, (alphaC != null) ? alphaC.getAlpha() : 1f);
        res.cursorInsideColor = this.cursorInsideColor;
        return res;
    }

}
