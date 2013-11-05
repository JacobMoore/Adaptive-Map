/*   FILE: CGlyph.java
 *   DATE OF CREATION:   Oct 01 2002
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) E. Pietriga, 2002. All Rights Reserved
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
 * $Id: CGlyph.java 2847 2010-01-29 15:47:12Z rprimet $
 */

package fr.inria.zvtm.glyphs;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;

  /**
   * Composite glyph (abstract glyph made of a primary shape and [optional] secondary shapes). A composite glyph has no visual representation of its own : it is just a means to tie glyphs between themselves. CGlyph only offers an higher level construction to group standard glyphs ; it entirely relies on lower level functions that are also available to the programmer ; so if you are not happy with the way CGlyph works, you can always create a modified version of it. <br> 
   * IMPORTANT : both CGlyphs AND and their components (standard glyphs) should be added to the virtual space<br>
   * The event firing policy can be changed using method setSensitivity(int) ; Note: the glyph sent as a parameter of the event triggering is the component, not the CGlyph ; the CGlyph can be retrieved by calling Glyph.getCGlyph()
   * @author Emmanuel Pietriga
   */

public class CGlyph extends Glyph implements Cloneable {

    /**Fire enter/exit Glyph events only when entering primary glyph*/
    public static short PRIMARY_GLYPH_ONLY=0;
    /**Fire enter/exit Glyph events when entering primary glyph and secondary glyphs*/
    public static short ALL_GLYPHS=1;

    short compSensit=CGlyph.ALL_GLYPHS;

    Glyph pGlyph; //primaryGlyph
    SGlyph[] sGlyphs;  //secondary glyphs

	/**
		*REMINDER : both CGlyphs AND and their components (primary and all secondary glyphs) should be added to the virtual space.
		*@param primary primary glyph in the composition (null if none, call setPrimaryGlyph to set it later)
		*@param secondaries array of secondary glyphs (null if none)
		*@see #setPrimaryGlyph(Glyph g)
		*/
	public CGlyph(Glyph primary,SGlyph[] secondaries){
		if (primary != null){
			setPrimaryGlyph(primary);
		}
		if (secondaries!=null && secondaries.length>0){
			sGlyphs=secondaries;
			for (int i=0;i<sGlyphs.length;i++){
				sGlyphs[i].g.moveTo(pGlyph.vx+Math.round(sGlyphs[i].xoffset), pGlyph.vy+Math.round(sGlyphs[i].yoffset));
			}
		}
	}

    public void initCams(int nbCam){}

    public void addCamera(int verifIndex){}

    public void removeCamera(int index){}

    public void resetMouseIn(){}

    public void resetMouseIn(int i){}

    public void move(long x,long y){
	vx+=x;
	vy+=y;
	pGlyph.move(x,y);
	if (sGlyphs!=null){
	    for (int i=0;i<sGlyphs.length;i++){
		sGlyphs[i].g.move(x,y);
	    }
	}
	propagateMove(x,y);  //take care of sticked glyphs
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void moveTo(long x,long y){
	propagateMove(x-vx,y-vy);  //take care of sticked glyphs
	pGlyph.moveTo(x,y);
	if (sGlyphs!=null){
	    double teta=(double)-getOrient();
	    long x2,y2;
	    for (int i=0;i<sGlyphs.length;i++){
		if ((sGlyphs[i].rotationPolicy==SGlyph.FULL_ROTATION) 
		    || (sGlyphs[i].rotationPolicy==SGlyph.ROTATION_POSITION_ONLY)){
		    x2=Math.round((sGlyphs[i].xoffset*Math.cos(teta)+sGlyphs[i].yoffset*Math.sin(teta)));
		    y2=Math.round((sGlyphs[i].yoffset*Math.cos(teta)-sGlyphs[i].xoffset*Math.sin(teta)));
		    sGlyphs[i].g.moveTo(pGlyph.vx+x2,pGlyph.vy+y2);
		}
		else {
		    sGlyphs[i].g.moveTo(x+Math.round(sGlyphs[i].xoffset), Math.round(y+sGlyphs[i].yoffset));
		}
	    }
	}
	this.vx=pGlyph.vx;
	this.vy=pGlyph.vy;
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public float getOrient(){
	if (pGlyph!=null){return pGlyph.getOrient();}
	else {return 0;}
    }

    public void orientTo(float angle){
	try {
	    pGlyph.orientTo(angle);
	    if (sGlyphs!=null){
		long x2,y2;
		double teta=(double)-angle;
		for (int i=0;i<sGlyphs.length;i++){
		    if ((sGlyphs[i].rotationPolicy==SGlyph.FULL_ROTATION) 
			|| (sGlyphs[i].rotationPolicy==SGlyph.ROTATION_ANGLE_ONLY)){
			sGlyphs[i].g.orientTo(angle+sGlyphs[i].aoffset);
		    }
		    if ((sGlyphs[i].rotationPolicy==SGlyph.FULL_ROTATION) 
			|| (sGlyphs[i].rotationPolicy==SGlyph.ROTATION_POSITION_ONLY)){
			x2=Math.round(sGlyphs[i].xoffset*Math.cos(teta)+sGlyphs[i].yoffset*Math.sin(teta));
			y2=Math.round(sGlyphs[i].yoffset*Math.cos(teta)-sGlyphs[i].xoffset*Math.sin(teta));
			sGlyphs[i].g.moveTo(pGlyph.vx+x2,pGlyph.vy+y2);
		    }
		}
	    }
	}
	catch(NullPointerException e){}
    }

    public float getSize(){
	if (pGlyph!=null){return pGlyph.getSize();}
	else {return 0;}
    }

    public synchronized void sizeTo(float radius){
	if (sGlyphs!=null){
	    float ratio=radius/getSize();
	    double teta = (double)getOrient();
	    long x2,y2;
	    for (int i=0;i<sGlyphs.length;i++){
		sGlyphs[i].xoffset=sGlyphs[i].xoffset*ratio;
		sGlyphs[i].yoffset=sGlyphs[i].yoffset*ratio;
		if ((sGlyphs[i].rotationPolicy==SGlyph.FULL_ROTATION) 
		    || (sGlyphs[i].rotationPolicy==SGlyph.ROTATION_POSITION_ONLY)){
		    x2=Math.round((sGlyphs[i].xoffset*Math.cos(teta)-sGlyphs[i].yoffset*Math.sin(teta)));
		    y2=Math.round((sGlyphs[i].xoffset*Math.sin(teta)+sGlyphs[i].yoffset*Math.cos(teta)));
		    sGlyphs[i].g.moveTo(pGlyph.vx+x2,pGlyph.vy+y2);
		}
		else {
		    sGlyphs[i].g.moveTo(pGlyph.vx+Math.round(sGlyphs[i].xoffset),pGlyph.vy+Math.round(sGlyphs[i].yoffset));
		}
		if (sGlyphs[i].sizePolicy==SGlyph.RESIZE){
		    sGlyphs[i].g.reSize(ratio);
		}
	    }
	}
	pGlyph.sizeTo(radius);
    }

    public synchronized void reSize(float factor){
	if (sGlyphs!=null){
	    double teta = (double)getOrient();
	    long x2,y2;
	    for (int i=0;i<sGlyphs.length;i++){
		sGlyphs[i].xoffset=sGlyphs[i].xoffset*factor;
		sGlyphs[i].yoffset=sGlyphs[i].yoffset*factor;
		if ((sGlyphs[i].rotationPolicy==SGlyph.FULL_ROTATION) 
		    || (sGlyphs[i].rotationPolicy==SGlyph.ROTATION_POSITION_ONLY)){
		    x2=Math.round((sGlyphs[i].xoffset*Math.cos(teta)-sGlyphs[i].yoffset*Math.sin(teta)));
		    y2=Math.round((sGlyphs[i].xoffset*Math.sin(teta)+sGlyphs[i].yoffset*Math.cos(teta)));
		    sGlyphs[i].g.moveTo(pGlyph.vx+x2,pGlyph.vy+y2);
		}
		else {
		    sGlyphs[i].g.moveTo(pGlyph.vx+Math.round(sGlyphs[i].xoffset),pGlyph.vy+Math.round(sGlyphs[i].yoffset));
		}
		if (sGlyphs[i].sizePolicy==SGlyph.RESIZE){
		    sGlyphs[i].g.reSize(factor);
		}
	    }
	}
	pGlyph.reSize(factor);
    }
    
    public boolean fillsView(long w,long h,int camIndex){//would be too complex: just say no
	return false;
    }

    public boolean coordInside(int jpx, int jpy, int camIndex, long cvx, long cvy){
	return false;
    }

    /**A composite glyph does not by itself fire cursor entry/exit events.
     * Its components do (as normal standalone glyphs).
     */
    public short mouseInOut(int jpx, int jpy, int camIndex, long cvx, long cvy){
	    return Glyph.NO_CURSOR_EVENT;
    }

    public void project(Camera c, Dimension d){}

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){}

    void textDraw(Graphics2D g,int i){}

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){}

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){}

    /** Set sensitivity of components.
	This setting does not override the setSensitivity(boolean) setting.
	Note: the glyph sent as a parameter of the event triggering is the component, not the CGlyph.
	* The CGlyph can be retrieved by calling Glyph.getCGlyph()
	*@param s one of PRIMARY_GLYPH_ONLY or ALL_GLYPHS.
    */
    public void setSensitivity(short s){
	if (s!=compSensit){
	    if (s==PRIMARY_GLYPH_ONLY && sGlyphs!=null){
		for (int i=0;i<sGlyphs.length;i++){
		    sGlyphs[i].g.setSensitivity(false);
		}
	    }
	    else if (s==ALL_GLYPHS && sGlyphs!=null){
		for (int i=0;i<sGlyphs.length;i++){
		    sGlyphs[i].g.setSensitivity(true);
		}
	    }
	    compSensit=s;
	}
    }

    /** Change the composition's primary glyph.
     *@param g change primary glyph in the composition
     */
    public void setPrimaryGlyph(Glyph g){
	pGlyph=g;
	this.vx=pGlyph.vx;
	this.vy=pGlyph.vy;
    }

    /** Add one secondary glyph more to the composition.
     *@param g Glyph to be added in the composition
     *@param rx relative position w.r.t primary glyph's center 
     *@param ry relative position w.r.t primary glyph's center
     *@see #addSecondaryGlyph(SGlyph sGlyph)
     *@see #removeSecondaryGlyph(Glyph g)
     */
    public void addSecondaryGlyph(Glyph g,long rx,long ry){
	if (sGlyphs==null){
	    sGlyphs=new SGlyph[1];
	    sGlyphs[0]=new SGlyph(g,rx,ry);
	    sGlyphs[0].g.moveTo(pGlyph.vx+Math.round(sGlyphs[0].xoffset),
				pGlyph.vy+Math.round(sGlyphs[0].yoffset));
	}
	else {
	    SGlyph[] tmpA=new SGlyph[sGlyphs.length+1];
	    System.arraycopy(sGlyphs,0,tmpA,0,sGlyphs.length);
	    tmpA[tmpA.length-1]=new SGlyph(g,rx,ry);
	    sGlyphs=tmpA;
	    sGlyphs[sGlyphs.length-1].g.moveTo(pGlyph.vx+Math.round(sGlyphs[sGlyphs.length-1].xoffset),
					       pGlyph.vy+Math.round(sGlyphs[sGlyphs.length-1].yoffset));
    }
    }

    /** Add one secondary glyph more to the composition.
     *@param g Glyph to be added in the composition
     *@see #addSecondaryGlyph(Glyph g,long rx,long ry)
     *@see #removeSecondaryGlyph(Glyph g)
     */
    public void addSecondaryGlyph(SGlyph g){
	if (sGlyphs==null){
	    sGlyphs=new SGlyph[1];
	    sGlyphs[0] = g;
	    sGlyphs[0].g.moveTo(pGlyph.vx+Math.round(sGlyphs[0].xoffset),
				pGlyph.vy+Math.round(sGlyphs[0].yoffset));
	}
	else {
	    SGlyph[] tmpA=new SGlyph[sGlyphs.length+1];
	    System.arraycopy(sGlyphs,0,tmpA,0,sGlyphs.length);
	    tmpA[tmpA.length-1] = g;
	    sGlyphs=tmpA;	    
	    sGlyphs[sGlyphs.length-1].g.moveTo(pGlyph.vx+Math.round(sGlyphs[sGlyphs.length-1].xoffset),
					       pGlyph.vy+Math.round(sGlyphs[sGlyphs.length-1].yoffset));
	}
    }

    /** Remove a glyph form this composite.This does not remove the glyph from the virtual space.
     *@param g Glyph to be removed from the composition
     *@see #addSecondaryGlyph(Glyph g,long rx,long ry)
     *@see #addSecondaryGlyph(SGlyph sGlyph)
     */
    public void removeSecondaryGlyph(Glyph g){
	if (sGlyphs!=null){
	    for (int i=0;i<sGlyphs.length;i++){
		if (sGlyphs[i].g==g){
		    SGlyph[] tmpA=new SGlyph[sGlyphs.length-1];
		    System.arraycopy(sGlyphs,0,tmpA,0,i);
		    System.arraycopy(sGlyphs,i+1,tmpA,i,sGlyphs.length-i-1);
		    sGlyphs=tmpA;
		    break;
		}
	    }
	    if (sGlyphs.length==0){sGlyphs=null;}
	}
    }

    /**
     * Get the secondary glyph encapsulating the glyph provided as parameter.
     */
    public SGlyph getSGlyph(Glyph gl){
	SGlyph res=null;
	if (sGlyphs!=null){
	    for (int i=0;i<sGlyphs.length;i++){
		if (sGlyphs[i].g==gl){res=sGlyphs[i];break;}
	    }
	}
	return res;
    }

    /**
     * Get all secondary glyphs associated with this CGlyph.
     */
    public SGlyph[] getSecondaryGlyphs(){
	return sGlyphs;
    }

    /**
     * Get primary glyph associated with this CGlyph.
     */
    public Glyph getPrimaryGlyph(){
	return pGlyph;
    }

    /** Not implemented yet. */
    public Object clone(){return null;}

    public void highlight(boolean b, java.awt.Color selectedColor){}

}
