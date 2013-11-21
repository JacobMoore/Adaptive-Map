/*   FILE: VSlice.java
 *   DATE OF CREATION:  Mon Aug 29 15:27:03 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: VSlice.java 3435 2010-07-23 15:38:52Z rprimet $
 */

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import fr.inria.zvtm.glyphs.projection.ProjSlice;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.Utilities;
import fr.inria.zvtm.engine.VirtualSpaceManager;

/**
 * Slice. This version is the most efficient, but it cannot be made translucent (see VSliceST).<br>
 * Slices are useful e.g. to draw pie menus.
 * @author Emmanuel Pietriga
 */

public class VSlice extends ClosedShape {

    /*vertex x coords*/
    public int[] xpcoords;
    /*vertex y coords*/
    public int[] ypcoords;

    public static final double RAD2DEG_FACTOR = 360 / Utilities.TWO_PI;
    public static final double DEG2RAD_FACTOR = Utilities.TWO_PI / 360.0;

    /*2nd point (arc end point)*/
    public LongPoint p1 = new LongPoint(0,0);
    /*3rd point (arc end point)*/
    public LongPoint p2 = new LongPoint(0,0);
    /*1st point corresponding to the outer triangle (near 2nd point)*/
    public LongPoint p3 = new LongPoint(0,0);
    /*2nd point corresponding to the outer triangle (near 3rd point)*/
    public LongPoint p4 = new LongPoint(0,0);

    /*radius in virtual space (equal to bounding circle radius since this is a circle)*/
    public long vr;

    public double angle;
    public double orient;
    public int angleDeg;
    public int orientDeg;
    public ProjSlice[] pc;

    public VSlice(){
		initCoordArray(3);	
        vx = 0;
        vy = 0;
        vz = 0;
        p1 = new LongPoint(10, 10);
        p2 = new LongPoint(-10, 10);
        computeSize();
        computeOrient();
        computeAngle();
        computePolygonEdges();
        setColor(Color.WHITE);
        setBorderColor(Color.BLACK);
    }
    
    /** Construct a slice by giving its 3 vertices
        *@param v array of 3 points representing the absolute coordinates of the slice's vertices. The first element must be the point that is not an endpoint of the arc 
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param c fill color
        *@param bc border color
        */
    public VSlice(LongPoint[] v, int z, Color c, Color bc){
        this(v, z, c, bc, 1.0f);
    }
    
    /** Construct a slice by giving its 3 vertices
        *@param v array of 3 points representing the absolute coordinates of the slice's vertices. The first element must be the point that is not an endpoint of the arc 
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param c fill color
        *@param bc border color
        *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
        */
    public VSlice(LongPoint[] v, int z, Color c, Color bc, float alpha){
		initCoordArray(3);	
        vx = v[0].x;
        vy = v[0].y;
        vz = z;
        p1 = v[1];
        p2 = v[2];
        computeSize();
        computeOrient();
        computeAngle();
        computePolygonEdges();
        setColor(c);
        setBorderColor(bc);
        setTranslucencyValue(alpha);
    }

    /** Construct a slice by giving its size, angle and orientation
        *@param x x-coordinate in virtual space of vertex that is not an arc endpoint
        *@param y y-coordinate in virtual space of vertex that is not an arc endpoint
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param vs arc radius in virtual space (in rad)
        *@param ag arc angle in virtual space (in rad)
        *@param or slice orientation in virtual space (interpreted as the orientation of the segment linking the vertex that is not an arc endpoint to the middle of the arc)
        *@param c fill color
        *@param bc border color
        */
    public VSlice(long x, long y, int z, long vs, double ag, double or, Color c, Color bc){
        this(x, y, z, vs, ag, or, c, bc, 1.0f);
    }
    
    /** Construct a slice by giving its size, angle and orientation
        *@param x x-coordinate in virtual space of vertex that is not an arc endpoint
        *@param y y-coordinate in virtual space of vertex that is not an arc endpoint
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param vs arc radius in virtual space (in rad)
        *@param ag arc angle in virtual space (in rad)
        *@param or slice orientation in virtual space (interpreted as the orientation of the segment linking the vertex that is not an arc endpoint to the middle of the arc)
        *@param c fill color
        *@param bc border color
        *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
        */
    public VSlice(long x, long y, int z, long vs, double ag, double or, Color c, Color bc, float alpha){
		initCoordArray(3);	
        vx = x;
        vy = y;
        vz = z;
        size = (float)vs;
        vr = vs;
        orient = or;
        orientDeg = (int)Math.round(orient * RAD2DEG_FACTOR);
        angle = ag;
        angleDeg = (int)Math.round(angle * RAD2DEG_FACTOR);
        computeSliceEdges();
        computePolygonEdges();
        setColor(c);
        setBorderColor(bc);
        setTranslucencyValue(alpha);
    }

    /** Construct a slice by giving its size, angle and orientation
        *@param x x-coordinate in virtual space of vertex that is not an arc endpoint
        *@param y y-coordinate in virtual space of vertex that is not an arc endpoint 
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param vs arc radius in virtual space (in degrees)
        *@param ag arc angle in virtual space (in degrees)
        *@param or slice orientation in virtual space (interpreted as the orientation of the segment linking the vertex that is not an arc endpoint to the middle of the arc)
        *@param c fill color
        *@param bc border color
        */
    public VSlice(long x, long y, int z, long vs, int ag, int or, Color c, Color bc){
        this(x, y, z, vs, ag, or, c, bc, 1.0f);
    }
    
    /** Construct a slice by giving its size, angle and orientation
        *@param x x-coordinate in virtual space of vertex that is not an arc endpoint
        *@param y y-coordinate in virtual space of vertex that is not an arc endpoint 
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param vs arc radius in virtual space (in degrees)
        *@param ag arc angle in virtual space (in degrees)
        *@param or slice orientation in virtual space (interpreted as the orientation of the segment linking the vertex that is not an arc endpoint to the middle of the arc)
        *@param c fill color
        *@param bc border color
        *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
        */
    public VSlice(long x, long y, int z, long vs, int ag, int or, Color c, Color bc, float alpha){
		initCoordArray(3);	
        vx = x;
        vy = y;
        vz = z;
        size = (float)vs;
        vr = vs;
        orient = or * DEG2RAD_FACTOR;
        orientDeg = or;
        angle = ag * DEG2RAD_FACTOR;
        angleDeg = ag;
        computeSliceEdges();
        computePolygonEdges();
        setColor(c);
        setBorderColor(bc);
        setTranslucencyValue(alpha);
    }

	/** FOR INTERNAL USE ONLY */
	public void initCoordArray(int n){
		xpcoords = new int[n];
	    ypcoords = new int[n];
	}
	
	public void moveTo(long x, long y){
	    p1.translate(x-vx, y-vy);
	    p2.translate(x-vx, y-vy);
	    super.moveTo(x, y);
	    computeSize();
        computeOrient();
        computeAngle();
        computePolygonEdges();
        VirtualSpaceManager.INSTANCE.repaintNow();
    }

	public void move(long x, long y){
	    p1.translate(x, y);
	    p2.translate(x, y);
	    super.move(x, y);
	    computeSize();
        computeOrient();
        computeAngle();
        computePolygonEdges();
        VirtualSpaceManager.INSTANCE.repaintNow();
    }

	/** FOR INTERNAL USE ONLY */
    public void computeSize(){
	size = (float)Math.sqrt(Math.pow(p1.x-vx, 2) + Math.pow(p1.y-vy, 2));
	vr = Math.round(size);
    }

	/** FOR INTERNAL USE ONLY */
    public void computeOrient(){
	double c = Math.sqrt(Math.pow(p1.x-vx, 2) + Math.pow(p1.y-vy, 2));
	double a1 = (p1.y-vy >= 0) ? Math.acos((p1.x-vx)/c) : Utilities.TWO_PI - Math.acos((p1.x-vx)/c);
	double a2 = (p2.y-vy >= 0) ? Math.acos((p2.x-vx)/c) : Utilities.TWO_PI - Math.acos((p2.x-vx)/c);
	// was initially (360/(4*Math.PI)) * (a1 + a2) / 2.0
	orient = (a1 + a2) / 2.0;
	orientDeg = (int)Math.round(orient * RAD2DEG_FACTOR);
    }

	/** FOR INTERNAL USE ONLY */
	public void computeAngle(){
	double c = Math.sqrt(Math.pow(p1.x-vx, 2) + Math.pow(p1.y-vy, 2));
	double a1 = (p1.y-vy >= 0) ? Math.acos((p1.x-vx)/c) : Utilities.TWO_PI - Math.acos((p1.x-vx)/c);
	double a2 = (p2.y-vy >= 0) ? Math.acos((p2.x-vx)/c) : Utilities.TWO_PI - Math.acos((p2.x-vx)/c);
	angle = a2 - a1;
	angleDeg = (int)Math.round(angle * RAD2DEG_FACTOR);
    }

	/** FOR INTERNAL USE ONLY */
    public void computeSliceEdges(){
	p1.x = Math.round(Math.cos(orient-angle/2.0)*size) + vx;
	p1.y = Math.round(Math.sin(orient-angle/2.0)*size) + vy;
	p2.x = Math.round(Math.cos(orient+angle/2.0)*size) + vx;
	p2.y = Math.round(Math.sin(orient+angle/2.0)*size) + vy;
    }

	/** FOR INTERNAL USE ONLY */
    public void computePolygonEdges(){
	if (angle < Math.PI){
	    p3.x = vx + Math.round((p1.x-vx)/Math.cos(angle/2.0));
	    p3.y = vy + Math.round((p1.y-vy)/Math.cos(angle/2.0));
	    p4.x = vx + Math.round((p2.x-vx)/Math.cos(angle/2.0));
	    p4.y = vy + Math.round((p2.y-vy)/Math.cos(angle/2.0));
	}
	else if (angle > Math.PI){// if angle >= PI a triangle cannot be used to model the bounding polygon
	    p3.x = vx - Math.round((p1.x-vx)/Math.cos(angle/2.0)); // compute coordInside by checking that 
	    p3.y = vy - Math.round((p1.y-vy)/Math.cos(angle/2.0)); // point is in circle and *not* inside triangle
	    p4.x = vx - Math.round((p2.x-vx)/Math.cos(angle/2.0)); // (triangle modeling the part not covered by
	    p4.y = vy - Math.round((p2.y-vy)/Math.cos(angle/2.0)); // the slice)
	}
	else {// angle == Math.PI  - case of zero division
	    p3.x = p1.x;
	    p3.y = p1.y;
	    p4.x = p2.x;
	    p4.y = p2.y;
	}
    }
    
    public void initCams(int nbCam){
	pc = new ProjSlice[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i] = new ProjSlice();
	}
    }

    public void addCamera(int verifIndex){
	if (pc != null){
	    if (verifIndex == pc.length){
		ProjSlice[] ta = pc;
		pc = new ProjSlice[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i] = ta[i];
		}
		pc[pc.length-1] = new ProjSlice();
	    }
	    else {System.err.println("VSlice:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex == 0){
		pc = new ProjSlice[1];
		pc[0] = new ProjSlice();
	    }
	    else {System.err.println("VSlice:Error while adding camera "+verifIndex);}
	}
    }

    public void removeCamera(int index){
	pc[index] = null;
    }

    public void resetMouseIn(){
	for (int i=0;i<pc.length;i++){
	    resetMouseIn(i);
	}
    }

    public void resetMouseIn(int i){
	if (pc[i] != null){pc[i].prevMouseIn = false;}
	borderColor = bColor;
    }

    public void sizeTo(float sz){
	size = sz;
	vr = Math.round(size);
	computeSliceEdges();
	computePolygonEdges();
    VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void reSize(float factor){
	size *= factor;
	vr = Math.round(size);
	computeSliceEdges();
	computePolygonEdges();
    VirtualSpaceManager.INSTANCE.repaintNow();
    }

    /** Set the slice's orientation.
     *@param ag slice orientation in virtual space, interpreted as the orientation of the segment linking the vertex that is not an arc endpoint to the middle of the arc (bisector of the main angle). In [0:2Pi[
     */
    public void orientTo(float ag){
	orient = (ag > Utilities.TWO_PI) ? (ag % Utilities.TWO_PI) : ag;
	orientDeg = (int)Math.round(orient * RAD2DEG_FACTOR);
	computeSliceEdges();
	computePolygonEdges();
    VirtualSpaceManager.INSTANCE.repaintNow();
    }

    /** Set the arc angle.
     *@param ag in [0:2Pi[
     */
    public void setAngle(double ag){
	angle = (ag > Utilities.TWO_PI) ? (ag % Utilities.TWO_PI) : ag;
	angleDeg = (int)Math.round(angle * RAD2DEG_FACTOR);
	computeSliceEdges();
	computePolygonEdges();
    VirtualSpaceManager.INSTANCE.repaintNow();
    }

    /** Get the arc angle.
     *@return the angle in [0:2Pi[
     */
    public double getAngle(){
	return angle;
    }

    public float getSize(){
	return size;
    }

    /** Get the slice's orientation.
     *@return slice's orientation in virtual space, interpreted as the orientation of the segment linking the vertex that is not an arc endpoint to the middle of the arc (bisector of the main angle). In [0:2Pi[
     */
    public float getOrient(){return (float)orient;}

    public boolean fillsView(long w,long h,int camIndex){
	//XXX: TBW (call coordInside() for the four view corners)
	return false;
    }

    public boolean coordInside(int jpx, int jpy, int camIndex, long cvx, long cvy){
        if (Math.sqrt(Math.pow(jpx-pc[camIndex].cx, 2)+Math.pow(jpy-pc[camIndex].cy, 2)) <= pc[camIndex].outerCircleRadius){
            // see computePolygonEdges() for an explanation of the following tests
            if (angle < Math.PI && pc[camIndex].boundingPolygon.contains(jpx, jpy) ||
                angle > Math.PI && !pc[camIndex].boundingPolygon.contains(jpx, jpy) ||
            angle == Math.PI && coordInsideHemisphere(jpx, jpy, camIndex)){
                return true;
            }
        }
        return false;
    }
    
    public boolean coordInsideHemisphere(int x, int y, int camIndex){
	if (orient == 0){
	    return (x >= pc[camIndex].cx) ? true : false;
	}
	else if (orient == Math.PI){
	    return (x <= pc[camIndex].cx) ? true : false;
	}
	else {
	    double a = (pc[camIndex].p2y-pc[camIndex].p1y) / (pc[camIndex].p2x-pc[camIndex].p1x);
	    double b = (pc[camIndex].p1y*pc[camIndex].p2x - pc[camIndex].p2y*pc[camIndex].p1x) / (pc[camIndex].p2x-pc[camIndex].p1x);
	    if (orient < Math.PI && y <= a*x+b ||
		orient > Math.PI && y >= a*x+b){
		return true;
	    }
	    else {
		return false;
	    }
	}
    }

    /** The disc is actually approximated to its bounding box here. Precise intersection computation would be too costly. */
	public boolean visibleInDisc(long dvx, long dvy, long dvr, Shape dvs, int camIndex, int jpx, int jpy, int dpr){
		if (Math.sqrt(Math.pow(vx-dvx, 2)+Math.pow(vy-dvy, 2)) < (dvr + vr)){
		    if (angle < Math.PI && pc[camIndex].boundingPolygon.intersects(jpx-dpr, jpy-dpr, 2*dpr, 2*dpr) ||
    		    angle > Math.PI && !pc[camIndex].boundingPolygon.intersects(jpx-dpr, jpy-dpr, 2*dpr, 2*dpr) ||
    		    angle == Math.PI && coordInsideHemisphere(jpx, jpy, camIndex)){
    		        //XXX last test for case when slice is a hemisphere is not sufficient, only covers case of 1px disc
    		        return true;
    	    }
		}
	    return false;
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

    //XXX: visibleInRegion() could be slightly optimized for VSlice (what about containedInRegion() ?)

    public void project(Camera c, Dimension d){
	int i = c.getIndex();
	coef = (float)(c.focal / (c.focal + c.altitude));
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	int hw = d.width/2;
	int hh = d.height/2;
	pc[i].cx = hw + Math.round((vx-c.posx) * coef);
	pc[i].cy = hh - Math.round((vy-c.posy) * coef);
	// do the same for other points
	pc[i].p1x = hw + Math.round((p1.x-c.posx) * coef);
	pc[i].p1y = hh - Math.round((p1.y-c.posy) * coef);
	pc[i].p2x = hw + Math.round((p2.x-c.posx) * coef);
	pc[i].p2y = hh - Math.round((p2.y-c.posy) * coef);
	xpcoords[0] = pc[i].cx;
	ypcoords[0] = pc[i].cy;
	xpcoords[1] = hw + Math.round((p3.x-c.posx) * coef);
	ypcoords[1] = hh - Math.round((p3.y-c.posy) * coef);
	xpcoords[2] = hw + Math.round((p4.x-c.posx) * coef);
	ypcoords[2] = hh - Math.round((p4.y-c.posy) * coef);
	if (pc[i].boundingPolygon == null){
	    pc[i].boundingPolygon = new Polygon(xpcoords, ypcoords, 3);
	}
	else {
	    for (int j=0;j<xpcoords.length;j++){
		pc[i].boundingPolygon.xpoints[j] = xpcoords[j];
		pc[i].boundingPolygon.ypoints[j] = ypcoords[j];
	    }
	    pc[i].boundingPolygon.invalidate();
	}
	pc[i].outerCircleRadius = Math.round(size * coef);
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i = c.getIndex();
	coef = (float)(c.focal / (c.focal + c.altitude)) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	int hw = lensWidth/2;
	int hh = lensHeight/2;
	pc[i].lcx = hw + Math.round((vx-lensx) * coef);
	pc[i].lcy = hh - Math.round((vy-lensy) * coef);
	// do the same for other points
	pc[i].lp1x = hw + Math.round((p1.x-lensx) * coef);
	pc[i].lp1y = hh - Math.round((p1.y-lensy) * coef);
	pc[i].lp2x = hw + Math.round((p2.x-lensx) * coef);
	pc[i].lp2y = hh - Math.round((p2.y-lensy) * coef);
	xpcoords[0] = pc[i].lcx;
	ypcoords[0] = pc[i].lcy;
	xpcoords[1] = hw + Math.round((p3.x-lensx) * coef);
	ypcoords[1] = hh - Math.round((p3.y-lensy) * coef);
	xpcoords[2] = hw + Math.round((p4.x-lensx) * coef);
	ypcoords[2] = hh - Math.round((p4.y-lensy) * coef);
	if (pc[i].lboundingPolygon == null){
	    pc[i].lboundingPolygon = new Polygon(xpcoords, ypcoords, 3);
	}
	else {
	    for (int j=0;j<xpcoords.length;j++){
		pc[i].lboundingPolygon.xpoints[j] = xpcoords[j];
		pc[i].lboundingPolygon.ypoints[j] = ypcoords[j];
	    }
	    pc[i].lboundingPolygon.invalidate();
	}
	pc[i].louterCircleRadius = Math.round(size * coef);
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alphaC != null && alphaC.getAlpha() == 0){return;}
        if (pc[i].outerCircleRadius > 2){
            //paint a dot if too small
            if (alphaC != null){
                // translucent
                g.setComposite(alphaC);
                if (filled){
                    g.setColor(this.color);
                    g.fillArc(dx+pc[i].cx - pc[i].outerCircleRadius, dy+pc[i].cy - pc[i].outerCircleRadius,
                        2 * pc[i].outerCircleRadius, 2 * pc[i].outerCircleRadius,
                        (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
                }
                if (paintBorder){
                    g.setColor(borderColor);
                    if (stroke != null){
                        g.setStroke(stroke);
                        g.drawArc(dx+pc[i].cx - pc[i].outerCircleRadius, dy+pc[i].cy - pc[i].outerCircleRadius,
                            2 * pc[i].outerCircleRadius, 2 * pc[i].outerCircleRadius,
                            (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
                        g.drawLine(dx+pc[i].cx, dy+pc[i].cy, pc[i].p1x, pc[i].p1y);
                        g.drawLine(dx+pc[i].cx, dy+pc[i].cy, pc[i].p2x, pc[i].p2y);
                        g.setStroke(stdS);
                    }
                    else {
                        g.drawArc(dx+pc[i].cx - pc[i].outerCircleRadius, dy+pc[i].cy - pc[i].outerCircleRadius,
                            2 * pc[i].outerCircleRadius, 2 * pc[i].outerCircleRadius,
                            (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
                        g.drawLine(dx+pc[i].cx, dy+pc[i].cy, pc[i].p1x, pc[i].p1y);
                        g.drawLine(dx+pc[i].cx, dy+pc[i].cy, pc[i].p2x, pc[i].p2y);
                    }
                }
                g.setComposite(acO);
            }
            else {
                // opaque
                if (filled){
                    g.setColor(this.color);
                    g.fillArc(dx+pc[i].cx - pc[i].outerCircleRadius, dy+pc[i].cy - pc[i].outerCircleRadius,
                        2 * pc[i].outerCircleRadius, 2 * pc[i].outerCircleRadius,
                        (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
                }
                if (paintBorder){
                    g.setColor(borderColor);
                    if (stroke != null){
                        g.setStroke(stroke);
                        g.drawArc(dx+pc[i].cx - pc[i].outerCircleRadius, dy+pc[i].cy - pc[i].outerCircleRadius,
                            2 * pc[i].outerCircleRadius, 2 * pc[i].outerCircleRadius,
                            (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
                        g.drawLine(dx+pc[i].cx, dy+pc[i].cy, pc[i].p1x, pc[i].p1y);
                        g.drawLine(dx+pc[i].cx, dy+pc[i].cy, pc[i].p2x, pc[i].p2y);
                        g.setStroke(stdS);
                    }
                    else {
                        g.drawArc(dx+pc[i].cx - pc[i].outerCircleRadius, dy+pc[i].cy - pc[i].outerCircleRadius,
                            2 * pc[i].outerCircleRadius, 2 * pc[i].outerCircleRadius,
                            (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
                        g.drawLine(dx+pc[i].cx, dy+pc[i].cy, pc[i].p1x, pc[i].p1y);
                        g.drawLine(dx+pc[i].cx, dy+pc[i].cy, pc[i].p2x, pc[i].p2y);
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
        if (alphaC != null && alphaC.getAlpha() == 0){return;}
        if (pc[i].louterCircleRadius > 2){
            //paint a dot if too small
            if (alphaC != null){
                // translucent
                g.setComposite(alphaC);
                if (filled){
                    g.setColor(this.color);
                    g.fillArc(dx+pc[i].lcx - pc[i].louterCircleRadius, dy+pc[i].lcy - pc[i].louterCircleRadius,
                        2 * pc[i].louterCircleRadius, 2 * pc[i].louterCircleRadius,
                        (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
                }
                if (paintBorder){
                    g.setColor(borderColor);
                    if (stroke != null){
                        g.setStroke(stroke);
                        g.drawArc(dx+pc[i].lcx - pc[i].louterCircleRadius, dy+pc[i].lcy - pc[i].louterCircleRadius,
                            2 * pc[i].louterCircleRadius, 2 * pc[i].louterCircleRadius,
                            (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
                        g.drawLine(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lp1x, pc[i].lp1y);
                        g.drawLine(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lp2x, pc[i].lp2y);
                        g.setStroke(stdS);
                    }
                    else {
                        g.drawArc(dx+pc[i].lcx - pc[i].louterCircleRadius, dy+pc[i].lcy - pc[i].louterCircleRadius,
                            2 * pc[i].louterCircleRadius, 2 * pc[i].louterCircleRadius,
                            (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
                        g.drawLine(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lp1x, pc[i].lp1y);
                        g.drawLine(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lp2x, pc[i].lp2y);
                    }
                }
                g.setComposite(acO);
            }
            else {
                // opaque
                if (filled){
                    g.setColor(this.color);
                    g.fillArc(dx+pc[i].lcx - pc[i].louterCircleRadius, dy+pc[i].lcy - pc[i].louterCircleRadius,
                        2 * pc[i].louterCircleRadius, 2 * pc[i].louterCircleRadius,
                        (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
                }
                if (paintBorder){
                    g.setColor(borderColor);
                    if (stroke != null){
                        g.setStroke(stroke);
                        g.drawArc(dx+pc[i].lcx - pc[i].louterCircleRadius, dy+pc[i].lcy - pc[i].louterCircleRadius,
                            2 * pc[i].louterCircleRadius, 2 * pc[i].louterCircleRadius,
                            (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
                        g.drawLine(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lp1x, pc[i].lp1y);
                        g.drawLine(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lp2x, pc[i].lp2y);
                        g.setStroke(stdS);
                    }
                    else {
                        g.drawArc(dx+pc[i].lcx - pc[i].louterCircleRadius, dy+pc[i].lcy - pc[i].louterCircleRadius,
                            2 * pc[i].louterCircleRadius, 2 * pc[i].louterCircleRadius,
                            (int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1);
                        g.drawLine(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lp1x, pc[i].lp1y);
                        g.drawLine(dx+pc[i].lcx, dy+pc[i].lcy, pc[i].lp2x, pc[i].lp2y);
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

    /** Not implement yet. */
    public Object clone(){
        //XXX: TBW
        return null;
    }
    
}
