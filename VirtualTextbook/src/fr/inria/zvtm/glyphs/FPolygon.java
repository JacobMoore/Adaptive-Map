/*   FILE: FPolygon.java
 *   DATE OF CREATION:   Mon Jan 13 13:34:44 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: FPolygon.java 3443 2010-07-28 08:57:10Z epietrig $
 */ 

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import fr.inria.zvtm.glyphs.projection.ProjPolygon;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.LongPoint;

/**
 * Fast Polygon. Can neither be resized nor reoriented (for now).
 * This is the old implementation of VPolygon, as found in ZVTM 0.8.2.<br>
 * The new version of VPolygon can be resized, but at some cost from an efficiency point of view, so the old version is still provided here and can be used by people who do not intend to resize their Polygon instances.<br>
 * This implementation uses longs instead of doubles for its internal representation of the vertices, see VPolygon for more details. 
 *@author Emmanuel Pietriga
 *@see fr.inria.zvtm.glyphs.VPolygon
 **/

public class FPolygon extends ClosedShape {

    long vs;

    /*array of projected coordinates - index of camera in virtual space is equal to index of projected coords in this array*/
    ProjPolygon[] pc;

    /*store x,y vertex coords as relative coordinates w.r.t polygon's centroid*/
    long[] xcoords;
    long[] ycoords;

    /**
     *@param v list of x,y vertices ABSOLUTE coordinates
     *@param c fill color
     */
    public FPolygon(LongPoint[] v,Color c){
	    this(v, Color.WHITE, Color.BLACK, 1.0f);
    }

    public FPolygon(LongPoint[] v, Color c, Color bc){
	    this(v, c, Color.BLACK, 1.0f);
    }

    /**
     *@param v list of x,y vertices ABSOLUTE coordinates
     *@param c fill color
     *@param bc border color
     *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
     */
    public FPolygon(LongPoint[] v, Color c, Color bc, float alpha){
        //should be zero here first as this is assumed when calling getCentroid later to compute the centroid's coordinates
        //several lines belowvx=0;
        vy=0;
        vz=0;
        xcoords=new long[v.length];
        ycoords=new long[v.length];
        for (int i=0;i<v.length;i++){
            xcoords[i]=v[i].x;
            ycoords[i]=v[i].y;
        }
        orient=0;
        LongPoint ct=getCentroid();
        vx=ct.x;
        vy=ct.y;
        for (int i=0;i<xcoords.length;i++){
            //translate to get relative coords w.r.t centroid
            xcoords[i]-=vx;
            ycoords[i]-=vy;
        }
        computeSize();
        setColor(c);
        setBorderColor(bc);
        setTranslucencyValue(alpha);
    }

    public void initCams(int nbCam){
        pc=new ProjPolygon[nbCam];
        for (int i=0;i<nbCam;i++){
            pc[i]=new ProjPolygon(xcoords.length);
        }
    }

    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		ProjPolygon[] ta=pc;
		pc=new ProjPolygon[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new ProjPolygon(xcoords.length);
	    }
	    else {System.err.println("FPolygon:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new ProjPolygon[1];
		pc[0]=new ProjPolygon(xcoords.length);
	    }
	    else {System.err.println("FPolygon:Error while adding camera "+verifIndex);}
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

    /*compute size (bounding circle radius)*/
    synchronized void computeSize(){
 	size=0;
	double f;
	for (int i=0;i<xcoords.length;i++){//at this point, the xcoords,ycoords should contain relative vertices coordinates (w.r.t vx/vy=centroid)
	    f=Math.sqrt(Math.pow(xcoords[i],2)+Math.pow(ycoords[i],2));
	    if (f>size){size=(float)f;}
	}
	vs=Math.round(size);
    }

    /** Cannot be resized. */
    public synchronized void sizeTo(float radius){}

    /** Cannot be resized. */
    public synchronized void reSize(float factor){}

    public boolean fillsView(long w,long h,int camIndex){
        if ((alphaC == null) &&
            (pc[camIndex].p.contains(0,0)) && (pc[camIndex].p.contains(w,0)) && (pc[camIndex].p.contains(0,h)) && (pc[camIndex].p.contains(w,h))){return true;}
        else {return false;}
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

    /** Get this polygon's list of vertices (relative coordinates).
     *@return relative coordinates (w.r.t polygon's centroid)
     */
    public LongPoint[] getVertices(){
	LongPoint[] res=new LongPoint[xcoords.length];
	for (int i=0;i<xcoords.length;i++){
	    res[i]=new LongPoint(Math.round(xcoords[i]),Math.round(ycoords[i]));
	}
	return res;
    }

    /** Get this polygon's list of vertices (absolute coordinates).
     *@return absolute coordinates
     */
    public LongPoint[] getAbsoluteVertices(){
	LongPoint[] res=new LongPoint[xcoords.length];
	for (int i=0;i<xcoords.length;i++){
	    res[i]=new LongPoint(Math.round(xcoords[i]+vx),Math.round(ycoords[i]+vy));
	}
	return res;
    }

    /** Get a serialization of this polygon's list of vertices.
     *@return a semicolon-separated string representation of all vertex absolute coordinates (x and y coordinates seperated by commas, e.g. x1,y1;x2,y2;x3,y3 etc.)
     */
    public String getVerticesAsText(){
	StringBuffer res=new StringBuffer();
	for (int i=0;i<xcoords.length-1;i++){
	    res.append(Math.round(xcoords[i]+vx)+","+Math.round(ycoords[i]+vy)+";");
	}
	res.append(Math.round(xcoords[xcoords.length-1]+vx)+","+Math.round(ycoords[ycoords.length-1]+vy));
	return res.toString();
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
	for (int j=0;j<xcoords.length;j++){
	    pc[i].xpcoords[j]=(int)Math.round(pc[i].cx+xcoords[j]*coef);
	    pc[i].ypcoords[j]=(int)Math.round(pc[i].cy-ycoords[j]*coef);
	}
	if (pc[i].p == null){
	    pc[i].p = new Polygon(pc[i].xpcoords, pc[i].ypcoords, xcoords.length);
	}
	else {
	    pc[i].p.npoints = xcoords.length;
	    for (int j=0;j<xcoords.length;j++){
		pc[i].p.xpoints[j] = pc[i].xpcoords[j];
		pc[i].p.ypoints[j] = pc[i].ypcoords[j];
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
	for (int j=0;j<xcoords.length;j++){
	    pc[i].lxpcoords[j]=(int)Math.round(pc[i].lcx+xcoords[j]*coef);
	    pc[i].lypcoords[j]=(int)Math.round(pc[i].lcy-ycoords[j]*coef);
	}
	if (pc[i].lp == null){
	    pc[i].lp = new Polygon(pc[i].lxpcoords, pc[i].lypcoords, xcoords.length);
	}
	else {
	    pc[i].lp.npoints = xcoords.length;
	    for (int j=0;j<xcoords.length;j++){
		pc[i].lp.xpoints[j] = pc[i].lxpcoords[j];
		pc[i].lp.ypoints[j] = pc[i].lypcoords[j];
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
    
    /** Get the polygon's area. */
    public double getArea(){
	double[] xcoordsForArea=new double[xcoords.length];
	double[] ycoordsForArea=new double[ycoords.length];
	for (int i=0;i<xcoords.length;i++){
	    xcoordsForArea[i]=vx+xcoords[i];
	    ycoordsForArea[i]=vy+ycoords[i];
	}
	int j,k;
	double res=0;
	for (j=0;j<xcoords.length;j++){
	    k=(j+1) % xcoords.length;
	    res+=(xcoordsForArea[j]*ycoordsForArea[k]-ycoordsForArea[j]*xcoordsForArea[k]);
	}
	res=res/2.0;
	return ((res<0) ? -res : res);
    }

    /** Get the double precision coordinates of this polygon's centroid.
     *@see #getCentroid()
     */
    public Point2D.Double getPreciseCentroid(){
	//compute polygon vertices
	double[] xcoordsForArea=new double[xcoords.length];
	double[] ycoordsForArea=new double[ycoords.length];
	for (int i=0;i<xcoords.length;i++){
	    xcoordsForArea[i]=vx+xcoords[i];
	    ycoordsForArea[i]=vy+ycoords[i];
	}
	//compute polygon area
	int j,k;
	double area=0;
	for (j=0;j<xcoords.length;j++){
	    k=(j+1) % xcoords.length;
	    area+=(xcoordsForArea[j]*ycoordsForArea[k]-ycoordsForArea[j]*xcoordsForArea[k]);
	}
	area=area/2.0;
	//area=((area<0) ? -area : area);  //do not do that!!! it can change the centroid's coordinates
	                                   //(-x,-y instead of x,y) depending on the order in which the
	                                   //sequence of vertex coords
	//compute centroid
	double factor=0;
	double cx=0;
	double cy=0;
	for (j=0;j<xcoords.length;j++){
	    k=(j+1) % xcoords.length;
	    factor=xcoordsForArea[j]*ycoordsForArea[k]-xcoordsForArea[k]*ycoordsForArea[j];
	    cx+=(xcoordsForArea[j]+xcoordsForArea[k])*factor;
	    cy+=(ycoordsForArea[j]+ycoordsForArea[k])*factor;
	}
	area*=6.0;
	factor=1/area;
	cx*=factor;
	cy*=factor;
	Point2D.Double res=new Point2D.Double(cx,cy);
	return res;
    }

    /** Get the coordinates of this polygon's centroid in virtual space.
     *@see #getPreciseCentroid()
     */
    public LongPoint getCentroid(){
	Point2D.Double p2dd=this.getPreciseCentroid();
	return new LongPoint(Math.round(p2dd.getX()),Math.round(p2dd.getY()));
    }

    public Object clone(){
	LongPoint[] lps=new LongPoint[xcoords.length];
	for (int i=0;i<lps.length;i++){
	    lps[i]=new LongPoint(xcoords[i]+vx,ycoords[i]+vy);
	}
	FPolygon res=new FPolygon(lps,color);
	res.borderColor=this.borderColor;
	res.cursorInsideColor=this.cursorInsideColor;
	res.bColor=this.bColor;
	return res;
    }

}
