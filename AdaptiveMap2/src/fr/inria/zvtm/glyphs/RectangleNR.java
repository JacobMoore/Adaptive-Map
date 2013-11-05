/*   FILE: RectangleNR.java
 *   DATE OF CREATION:   Thu Dec 05 13:53:36 2002
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: RectangleNR.java 3443 2010-07-28 08:57:10Z epietrig $
 */

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import fr.inria.zvtm.glyphs.projection.RProjectedCoords;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;

/**
 * Rectangle with constant apparent width and height, no matter the camera's altitude.
 * Used for instance to create resizing handles. Cannot be reoriented.
 * @author Emmanuel Pietriga
 **/

public class RectangleNR extends ClosedShape implements RectangularShape {

    long vw,vh;
    float ar;

    RProjectedCoords[] pc;

    public RectangleNR(){
	    this(0, 0, 0, 5, 5, Color.WHITE, Color.BLACK);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param w half width in virtual space
     *@param h half height in virtual space
     *@param c fill color
     */
    public RectangleNR(long x,long y, int z,long w,long h,Color c){
	    this(x, y, z, w, h, c, Color.BLACK);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param w half width in virtual space
     *@param h half height in virtual space
     *@param c fill color
     *@param bc border color
     */
    public RectangleNR(long x, long y, int z, long w, long h, Color c, Color bc){
	vx=x;
	vy=y;
	vz=z;
	vw=w;
	vh=h;
	computeSize();
	if (vw==0 && vh==0){ar=1.0f;}
	else {ar=(float)vw/(float)vh;}
	//if (vh!=0){ar=vw/vh;}else{ar=0;}
	orient=0;
	setColor(c);
	setBorderColor(bc);
    }

    public void initCams(int nbCam){
	pc=new RProjectedCoords[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new RProjectedCoords();
	    pc[i].cw=(int)vw;
	    pc[i].ch=(int)vh;
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
		pc[pc.length-1].cw=(int)vw;
		pc[pc.length-1].ch=(int)vh;
	    }
	    else {System.err.println("RectangleNR:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new RProjectedCoords[1];
		pc[0]=new RProjectedCoords();
		pc[0].cw=(int)vw;
		pc[0].ch=(int)vh;
	    }
	    else {System.err.println("RectangleNR:Error while adding camera "+verifIndex);}
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
	updateProjectedWH();
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void setWidth(long w){ 
	vw=w;
	computeSize();
	updateProjectedWH();
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void setHeight(long h){
	vh=h;
	computeSize();
	updateProjectedWH();
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    public void reSize(float factor){//resizing factor
	size*=factor;
	vw=(long)Math.round((size*ar)/(Math.sqrt(Math.pow(ar,2)+1)));
	vh=(long)Math.round((size)/(Math.sqrt(Math.pow(ar,2)+1)));
	updateProjectedWH();
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

	/** Get the bounding box of this Glyph in virtual space coordinates.
	 *@return west, north, east and south bounds in virtual space.
	 */
	public long[] getBounds(){
		long[] res = {vx-vw,vy+vh,vx+vw,vy-vh};
		return res;
	}

    private void updateProjectedWH(){
	if (pc!=null){
	    for (int i=0;i<pc.length;i++){
		try {
		    pc[i].cw=(int)vw;
		    pc[i].ch=(int)vh;
		}//some pc[i] might be null (if cameras were deleted from the virtual space)
		catch (NullPointerException e){}
	    }
	}
    }

    public boolean visibleInRegion(long wb, long nb, long eb, long sb, int i){
	if ((vx>=wb) && (vx<=eb) && (vy>=sb) && (vy<=nb)){
	    /* Glyph hotspot is in the region. The glyph is obviously visible */
	    return true;
	}
	else {
	    if (((vx-pc[i].cw)<=eb) && ((vx+pc[i].cw)>=wb) && ((vy-pc[i].ch)<=nb) && ((vy+pc[i].ch)>=sb)){
		/* Glyph is at least partially in region.
		   We approximate using the glyph bounding box, meaning that some glyphs not
		   actually visible can be projected and drawn (but they won't be displayed)) */
		return true;  
	    }
	}
	return false;
    }

    public boolean containedInRegion(long wb, long nb, long eb, long sb, int i){
	if ((vx>=wb) && (vx<=eb) && (vy>=sb) && (vy<=nb)){
	    /* Glyph hotspot is in the region.
	       There is a good chance the glyph is contained in the region, but this is not sufficient. */
	    if (((vx+pc[i].cw)<=eb) && ((vx-pc[i].cw)>=wb) && ((vy+pc[i].ch)<=nb) && ((vy-pc[i].ch)>=sb)){
		return true;
	    }
	    else return false;   //otherwise the glyph is not visible
	}
	return false;
    }

    public boolean fillsView(long w,long h,int camIndex){//width and height of view - pc[i].c? are JPanel coords
	if ((w<=pc[camIndex].cx+pc[camIndex].cw) && (0>=pc[camIndex].cx-pc[camIndex].cw) && (h<=pc[camIndex].cy+pc[camIndex].ch) && (0>=pc[camIndex].cy-pc[camIndex].ch)){return true;}
	else {return false;}
    }
    
    public boolean visibleInDisc(long dvx, long dvy, long dvr, Shape dvs, int camIndex, int jpx, int jpy, int dpr){
		return dvs.intersects(vx-vw, vy-vh, 2*vw, 2*vh);
	}

    public boolean coordInside(int jpx, int jpy, int camIndex, long cvx, long cvy){
        if ((jpx>=(pc[camIndex].cx-pc[camIndex].cw)) && (jpx<=(pc[camIndex].cx+pc[camIndex].cw)) &&
            (jpy>=(pc[camIndex].cy-pc[camIndex].ch)) && (jpy<=(pc[camIndex].cy+pc[camIndex].ch))){return true;}
        else {return false;}
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
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude)) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].lcx = (lensWidth/2) + Math.round((vx-(lensx))*coef);
	pc[i].lcy = (lensHeight/2) - Math.round((vy-(lensy))*coef);
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alphaC != null && alphaC.getAlpha()==0){return;}
        if ((pc[i].cw>1) && (pc[i].ch>1)) {
            if (alphaC != null){
                g.setComposite(alphaC);
                //repaint only if object is visible
                if (filled) {
                    g.setColor(this.color);
                    g.fillRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch);
                }
                if (paintBorder){
                    g.setColor(borderColor);
                    g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
                }
                g.setComposite(acO);
            }
            else {
                //repaint only if object is visible
                if (filled) {
                    g.setColor(this.color);
                    g.fillRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch);
                }
                if (paintBorder){
                    g.setColor(borderColor);
                    g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
                }
            }
        }
        else if ((pc[i].cw<=1) ^ (pc[i].ch<=1)) {
            //repaint only if object is visible  (^ means xor)
            g.setColor(this.color);
            if (alphaC != null){
                g.setComposite(alphaC);
                if (pc[i].cw<=1){
                    g.fillRect(dx+pc[i].cx,dy+pc[i].cy-pc[i].ch,1,2*pc[i].ch);
                }
                else if (pc[i].ch<=1){
                    g.fillRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy,2*pc[i].cw,1);
                }
                g.setComposite(acO);
            }
            else {
                //repaint only if object is visible
                if (pc[i].cw<=1){
                    g.fillRect(dx+pc[i].cx,dy+pc[i].cy-pc[i].ch,1,2*pc[i].ch);
                }
                else if (pc[i].ch<=1){
                    g.fillRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy,2*pc[i].cw,1);
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
        if ((pc[i].lcw>1) && (pc[i].lch>1)) {
            if (alphaC != null){
                g.setComposite(alphaC);
                //repaint only if object is visible
                if (filled) {
                    g.setColor(this.color);
                    g.fillRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch);
                }
                if (paintBorder){
                    g.setColor(borderColor);
                    g.drawRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw-1,2*pc[i].lch-1);
                }
                g.setComposite(acO);
            }
            else {
                //repaint only if object is visible
                if (filled) {
                    g.setColor(this.color);
                    g.fillRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch);
                }
                if (paintBorder){
                    g.setColor(borderColor);
                    g.drawRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw-1,2*pc[i].lch-1);
                }
            }
        }
        else if ((pc[i].lcw<=1) ^ (pc[i].lch<=1)) {
            //repaint only if object is visible  (^ means xor)
            g.setColor(this.color);
            if (alphaC != null){
                g.setComposite(alphaC);
                if (pc[i].lcw<=1){
                    g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy-pc[i].lch,1,2*pc[i].lch);
                }
                else if (pc[i].lch<=1){
                    g.fillRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy,2*pc[i].lcw,1);
                }
                g.setComposite(acO);
            }
            else {
                //repaint only if object is visible
                if (pc[i].lcw<=1){
                    g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy-pc[i].lch,1,2*pc[i].lch);
                }
                else if (pc[i].lch<=1){
                    g.fillRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy,2*pc[i].lcw,1);
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

    public Object clone(){
        RectangleNR res = new RectangleNR(vx,vy,0,vw,vh,color);
        res.borderColor = this.borderColor;
        res.cursorInsideColor = this.cursorInsideColor;
        res.bColor = this.bColor;
        res.setTranslucencyValue(getTranslucencyValue());
        return res;
    }

}
