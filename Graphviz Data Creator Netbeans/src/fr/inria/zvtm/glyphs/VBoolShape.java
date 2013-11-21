/*   FILE: VBoolShape.java
 *   DATE OF CREATION:   Oct 03 2000
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
 * $Id: VBoolShape.java 2102 2009-06-23 08:57:56Z rprimet $
 */

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import fr.inria.zvtm.glyphs.projection.ProjBoolean;

import fr.inria.zvtm.engine.Camera;

  /**
   * Glyphs defined as a main shape modified through boolean operations using secondary shapes.
   * Defined by a main glyph and a list of boolean operations (applied according to their order in the constructor's array).
   * Right now we only support RectangularShape derivatives (Ellipse, Rectangle)
   * @author Emmanuel Pietriga
   */

public class VBoolShape extends ClosedShape implements RectangularShape {

    /** List of boolean operations (applied in the order given by the array). */
    BooleanOps[] booleanShapes;
    /** Main shape width in virtual space. */
    long szx;
    /** Main shape height in virtual space. */
    long szy;


    /** One of BooleanOps.SHAPE_TYPE_*. */
    int shapeType;

    ProjBoolean[] pc;

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering) in virtual space
     *@param sx horizontal size in virtual space
     *@param sy vertical size in virtual space
     *@param st shape type, one of BooleanOps.SHAPE_TYPE_*
     *@param b array of boolean operations
     *@param c main shape's color
     */
    public VBoolShape(long x,long y, int z,long sx,long sy,int st,BooleanOps[] b,Color c){
	    this(x, y, z, sx, sy, st, b, c, Color.BLACK);
    }
    
    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering) in virtual space
     *@param sx horizontal size in virtual space
     *@param sy vertical size in virtual space
     *@param st shape type, one of BooleanOps.SHAPE_TYPE_*
     *@param b array of boolean operations
     *@param c main shape's color
     *@param bc main shape's border color
     */
    public VBoolShape(long x, long y, int z, long sx, long sy, int st, BooleanOps[] b, Color c, Color bc){
	vx=x;
	vy=y;
	vz=z;
	szx=sx;
	szy=sy;
	shapeType=st;
	booleanShapes=b;
	setColor(c);
	setBorderColor(bc);
    }

    public void initCams(int nbCam){
	pc=new ProjBoolean[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new ProjBoolean();
	}
    }

    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		ProjBoolean[] ta=pc;
		pc=new ProjBoolean[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new ProjBoolean();
	    }
	    else {System.err.println("VBoolShape:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new ProjBoolean[1];
		pc[0]=new ProjBoolean();
	    }
	    else {System.err.println("VBoolShape:Error while adding camera "+verifIndex);}
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

    public float getSize(){return 0;}

    /** Cannot be resized. */
    public void sizeTo(float radius){}

    /** Cannot be resized. */
    public void setWidth(long w){}

    /** Cannot be resized. */
    public void setHeight(long h){}

    /** Get half width. */
    public long getWidth(){return szx / 2;}

    /** Get half height. */
    public long getHeight(){return szy / 2;}

    /** Cannot be resized. */
    public void reSize(float factor){}

    public boolean fillsView(long w,long h,int camIndex){//would be too complex: just say no
	return false;
    }

    public boolean coordInside(int jpx, int jpy, int camIndex, long cvx, long cvy){
        if (pc[camIndex].mainArea.contains(jpx, jpy)){return true;}
        else {return false;}
    }

    /** The disc is actually approximated to its bounding box here. Precise intersection computation would be too costly. */
	public boolean visibleInDisc(long dvx, long dvy, long dvr, Shape dvs, int camIndex, int jpx, int jpy, int dpr){
		return pc[camIndex].mainArea.intersects(jpx-dpr, jpy-dpr, 2*dpr, 2*dpr);
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
	//translate in JPanel coords
	pc[i].cx=(d.width/2)+Math.round((vx-c.posx)*coef);
	pc[i].cy=(d.height/2)-Math.round((vy-c.posy)*coef);
	for (int j=0;j<booleanShapes.length;j++){
	    booleanShapes[j].project(coef,pc[i].cx,pc[i].cy);
	}
	pc[i].cszx=szx*coef;
	pc[i].cszy=szy*coef;
	switch (shapeType) {
	case 1:{//ellipse
	    pc[i].mainArea=new Area(new Ellipse2D.Float(pc[i].cx-szx/2*coef,pc[i].cy-szy/2*coef,pc[i].cszx,pc[i].cszy));
	    break;
	}
	case 2:{//rectangle
	    pc[i].mainArea=new Area(new Rectangle2D.Float(pc[i].cx-szx/2*coef,pc[i].cy-szy/2*coef,pc[i].cszx,pc[i].cszy));
	    break;
	}
	default:{//ellipse as default
	    pc[i].mainArea=new Area(new Ellipse2D.Float(pc[i].cx-szx/2*coef,pc[i].cy-szy/2*coef,pc[i].cszx,pc[i].cszy));
	}
	}
	for (int j=0;j<booleanShapes.length;j++){
	    switch (booleanShapes[j].opType) {
	    case 1:{
		pc[i].mainArea.add(booleanShapes[j].ar);
		break;
	    }
	    case 2:{
		pc[i].mainArea.subtract(booleanShapes[j].ar);
		break;
	    }
	    case 3:{
		pc[i].mainArea.intersect(booleanShapes[j].ar);
		break;
	    }
	    case 4:{
		pc[i].mainArea.exclusiveOr(booleanShapes[j].ar);
		break;
	    }
	    default:{
		System.err.println("Error: VBoolShape: boolean operation not defined");
	    }
	    }
	}
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i=c.getIndex();
	coef = ((float)(c.focal/(c.focal+c.altitude))) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].cx=(lensWidth/2) + Math.round((vx-lensx)*coef);
	pc[i].cy=(lensHeight/2) - Math.round((vy-lensy)*coef);
	for (int j=0;j<booleanShapes.length;j++){
	    booleanShapes[j].projectForLens(coef,pc[i].lcx,pc[i].lcy);
	}
	pc[i].lcszx=szx*coef;
	pc[i].lcszy=szy*coef;
	switch (shapeType) {
	case 1:{//ellipse
	    pc[i].lmainArea=new Area(new Ellipse2D.Float(pc[i].lcx-szx/2*coef,pc[i].lcy-szy/2*coef,pc[i].lcszx,pc[i].lcszy));
	    break;
	}
	case 2:{//rectangle
	    pc[i].lmainArea=new Area(new Rectangle2D.Float(pc[i].lcx-szx/2*coef,pc[i].lcy-szy/2*coef,pc[i].lcszx,pc[i].lcszy));
	    break;
	}
	default:{//ellipse as default
	    pc[i].lmainArea=new Area(new Ellipse2D.Float(pc[i].lcx-szx/2*coef,pc[i].lcy-szy/2*coef,pc[i].lcszx,pc[i].lcszy));
	}
	}
	for (int j=0;j<booleanShapes.length;j++){
	    switch (booleanShapes[j].opType) {
	    case 1:{
		pc[i].lmainArea.add(booleanShapes[j].lar);
		break;
	    }
	    case 2:{
		pc[i].lmainArea.subtract(booleanShapes[j].lar);
		break;
	    }
	    case 3:{
		pc[i].lmainArea.intersect(booleanShapes[j].lar);
		break;
	    }
	    case 4:{
		pc[i].lmainArea.exclusiveOr(booleanShapes[j].lar);
		break;
	    }
	    default:{
		System.err.println("Error: VBoolShape: boolean operation not defined");
	    }
	    }
	}
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alphaC != null && alphaC.getAlpha()==0){return;}
        if ((pc[i].mainArea.getBounds().width>2) && (pc[i].mainArea.getBounds().height>2)){
            if (alphaC != null){
                g.setComposite(alphaC);
                if (filled){
                    g.setColor(this.color);
                    g.translate(dx,dy);
                    g.fill(pc[i].mainArea);
                    g.translate(-dx,-dy);
                }
                if (paintBorder){
                    g.setColor(borderColor);
                    if (stroke!=null){
                        g.setStroke(stroke);
                        g.translate(dx,dy);
                        g.draw(pc[i].mainArea);
                        g.translate(-dx,-dy);
                        g.setStroke(stdS);
                    }
                    else {
                        g.translate(dx,dy);
                        g.draw(pc[i].mainArea);
                        g.translate(-dx,-dy);
                    }		   
                }
                g.setComposite(acO);
            }
            else {
                if (filled){
                    g.setColor(this.color);
                    g.translate(dx,dy);
                    g.fill(pc[i].mainArea);
                    g.translate(-dx,-dy);
                }
                if (paintBorder){
                    g.setColor(borderColor);
                    if (stroke!=null){
                        g.setStroke(stroke);
                        g.translate(dx,dy);
                        g.draw(pc[i].mainArea);
                        g.translate(-dx,-dy);
                        g.setStroke(stdS);
                    }
                    else {
                        g.translate(dx,dy);
                        g.draw(pc[i].mainArea);
                        g.translate(-dx,-dy);
                    }		   
                }
            }
        }
        else {
            if (alphaC != null){
                g.setComposite(alphaC);
                g.setColor(this.color);
                g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
                g.setComposite(acO);
            }
            else {
                g.setColor(this.color);
                g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
            }
        }
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alphaC != null && alphaC.getAlpha()==0){return;}
        if ((pc[i].lmainArea.getBounds().width>2) && (pc[i].lmainArea.getBounds().height>2)){
            if (alphaC != null){
                g.setComposite(alphaC);
                if (filled){
                    g.setColor(this.color);
                    g.translate(dx,dy);
                    g.fill(pc[i].lmainArea);
                    g.translate(-dx,-dy);
                }
                if (paintBorder){
                    g.setColor(borderColor);
                    if (stroke!=null){
                        g.setStroke(stroke);
                        g.translate(dx,dy);
                        g.draw(pc[i].lmainArea);
                        g.translate(-dx,-dy);
                        g.setStroke(stdS);
                    }
                    else {
                        g.translate(dx,dy);
                        g.draw(pc[i].lmainArea);
                        g.translate(-dx,-dy);
                    }		   
                }
                g.setComposite(acO);
            }
            else {
                if (filled){
                    g.setColor(this.color);
                    g.translate(dx,dy);
                    g.fill(pc[i].lmainArea);
                    g.translate(-dx,-dy);
                }
                if (paintBorder){
                    g.setColor(borderColor);
                    if (stroke!=null){
                        g.setStroke(stroke);
                        g.translate(dx,dy);
                        g.draw(pc[i].lmainArea);
                        g.translate(-dx,-dy);
                        g.setStroke(stdS);
                    }
                    else {
                        g.translate(dx,dy);
                        g.draw(pc[i].lmainArea);
                        g.translate(-dx,-dy);
                    }		   
                }
            }
        }
        else {
            if (alphaC != null){
                g.setComposite(alphaC);
                g.setColor(this.color);
                g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
                g.setComposite(acO);
            }
            else {
                g.setColor(this.color);
                g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
            }
        }
    }
    
    /** For internal use. */
    public int getMainShapeType(){
	return shapeType;
    }

    /** For internal use. */
    public BooleanOps[] getOperations(){
	return booleanShapes;
    }

    public Object clone(){
        VBoolShape res = new VBoolShape(vx,vy,0,szx,szy,shapeType,booleanShapes,color);
        res.setTranslucencyValue(getTranslucencyValue());
        return res;
    }

}
