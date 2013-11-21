/*   FILE: VPoint.java
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
 * $Id: VPoint.java 3443 2010-07-28 08:57:10Z epietrig $
 */

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.Shape;

import fr.inria.zvtm.glyphs.projection.ProjectedCoords;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;

/**
 * Point. Actually, a rectangle with constant size of 1.
 * @author Emmanuel Pietriga
 **/

public class VPoint extends Glyph {

    ProjectedCoords[] pc;

    public VPoint(){
	    this(0, 0, 0, Color.BLACK, 1.0f);
    }

    /**
        *@param x coordinate in virtual space
        *@param y coordinate in virtual space
        *@param c color
        */
    public VPoint(long x,long y, Color c){
        this(x, y, 0, c, 1.0f);
    }

    /**
        *@param x coordinate in virtual space
        *@param y coordinate in virtual space
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param c color
        */
    public VPoint(long x,long y, int z, Color c){
        this(x, y, z, c, 1.0f);
    }
    
    /**
        *@param x coordinate in virtual space
        *@param y coordinate in virtual space
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param c color
        *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
        */
    public VPoint(long x,long y, int z, Color c, float alpha){
        vx=x;
        vy=y;
        vz = z;
        size=1;   //radius of the bounding circle
        setColor(c);
        setTranslucencyValue(alpha);
    }

    public void initCams(int nbCam){
	pc=new ProjectedCoords[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new ProjectedCoords();
	}
    }

    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		ProjectedCoords[] ta=pc;
		pc=new ProjectedCoords[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new ProjectedCoords();
	    }
	    else {System.err.println("VPoint:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new ProjectedCoords[1];
		pc[0]=new ProjectedCoords();
	    }
	    else {System.err.println("VPoint:Error while adding camera "+verifIndex);}
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

    /** Cannot be resized (it makes on sense). */
    public void sizeTo(float factor){}

    /** Cannot be resized (it makes on sense). */
    public void reSize(float factor){}

    /** Cannot be reoriented (it makes on sense). */
    public void orientTo(float angle){}

    public float getSize(){return 1.0f;}

    public float getOrient(){return 0;}

    public boolean fillsView(long w,long h,int camIndex){
	return false;
    }

    public boolean coordInside(int jpx, int jpy, int camIndex, long cvx, long cvy){
        if (jpx==pc[camIndex].cx && jpy==pc[camIndex].cy){return true;}
        else {return false;}
    }

	public boolean visibleInDisc(long dvx, long dvy, long dvr, Shape dvs, int camIndex, int jpx, int jpy, int dpr){
		return Math.sqrt(Math.pow(vx-dvx, 2)+Math.pow(vy-dvy, 2)) <= dvr;
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
        if (alphaC != null){
            // glyph is not opaque
            if (alphaC.getAlpha() == 0){
                // glyph is totally transparent
                return;
            }
            // glyph is translucent
            g.setColor(this.color);
            g.setComposite(alphaC);
            g.fillRect(dx+pc[i].cx, dy+pc[i].cy, 1, 1);
            g.setComposite(acO);
        }
        else {
            // glyph is opaque
            g.setColor(this.color);
            g.fillRect(dx+pc[i].cx, dy+pc[i].cy, 1, 1);
        }
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alphaC != null){
            // glyph is not opaque
            if (alphaC.getAlpha() == 0){
                // glyph is totally transparent
                return;
            }
            // glyph is translucent
            g.setColor(this.color);
            g.setComposite(alphaC);
            g.fillRect(dx+pc[i].lcx, dy+pc[i].lcy, 1, 1);
            g.setComposite(acO);
        }
        else {
            // glyph is opaque
            g.setColor(this.color);
            g.fillRect(dx+pc[i].lcx, dy+pc[i].lcy, 1, 1);
        }
    }

    public Object clone(){
        VPoint res = new VPoint(vx, vy, 0, color, (alphaC != null) ? alphaC.getAlpha(): 1.0f);
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
