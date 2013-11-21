/*   FILE: SGlyph.java
 *   DATE OF CREATION:   Oct 03 2002
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Tue Oct 08 09:43:46 2002 by Emmanuel Pietriga
 *   Copyright (c) E. Pietriga, 2002. All Rights Reserved
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
 */

package fr.inria.zvtm.glyphs;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.glyphs.Glyph;

  /**
   * Class to be used to create new components for a composite glyphs (secondary glyphs)<br>
   * Use this to change the nature or offset (position) of this secondary glyph
   * @author Emmanuel Pietriga
   */

public class SGlyph {

    /** When rotating the composite glyph, rotate this component and change its position w.r.t the primary glyph to match the rotation (so that it keeps the same relative position inside its parent). */
    public static short FULL_ROTATION=3;
    
    /** When rotating the composite glyph, do not modify the position or the orientation of this glyph. */
    public static short NO_ROTATION=0;

    /** When rotating the composite glyph, change its position w.r.t the primary glyph to match the rotation (so that it keeps the same relative position inside its parent) but do not rotate this component. */
    public static short ROTATION_POSITION_ONLY=2;

    /** When rotating the composite glyph, rotate this component but do not change its position w.r.t the primary glyph (its relative position inside its parent will change). */
    public static short ROTATION_ANGLE_ONLY=1;

    /** When resizing the composite glyph, resize this component and change its position w.r.t the primary glyph to keep the same aspect (i.e. the same relative position inside its parent). */
    public static short RESIZE=0;

    /** When resizing the composite glyph, do not resize this component but do change its position w.r.t the primary glyph to keep the same aspect (i.e. the same relative position inside its parent). */
    public static short NO_RESIZE=1;
    
    Glyph g;
    double xoffset;
    double yoffset;
    short rotationPolicy=FULL_ROTATION;
    float aoffset=0;  //angle offset
    short sizePolicy=RESIZE;

    /**
     *Constructs a new SGlyph with default values (angle offset=0) 
     */
    public SGlyph(Glyph gl,long x,long y){
	this.g=gl;
	this.xoffset=x;
	this.yoffset=y;
	this.aoffset=gl.getOrient();
    }

    /**
     *Constructs a new SGlyph
     *@param gl glyph that will be made a component of the CGlyph (it must manually be added to the virtual space)
     *@param x horizontal distance between the center of the CGlyph's primary glyph and this glyph's center
     *@param y vertical distance between the center of the CGlyph's primary glyph and this glyph's center
     *@param rotPol rotation policy for this component (one of NO_ROTATION, ROTATION_ANGLE_ONLY, FULL_ROTATION, ROTATION_POSITION_ONLY)
     *@param szPol resizing policy for this component (one of NO_RESIZE, RESIZE)
     */
    public SGlyph(Glyph gl,long x,long y,short rotPol,short szPol){
	this.g=gl;
	this.xoffset=x;
	this.yoffset=y;
	this.rotationPolicy=rotPol;
	this.sizePolicy=szPol;
	this.aoffset=gl.getOrient();
    }

    /** Set the glyph that constitutes this secondary glyph. */
    public void setGlyph(Glyph gl){this.g=gl;}
    
    /** Get the glyph that constitutes this secondary glyph. */
    public Glyph getGlyph(){return g;}

    /** Change position of this component w.r.t the position of the CGlyph's primary glyph (relative coordinates). */
    public void setOffset(LongPoint p){xoffset=p.x;yoffset=p.y;}

    /** Change horizontal position of this component w.r.t the position of the CGlyph's primary glyph (relative coordinates). */
    public void setHorizontalOffset(long x){xoffset=x;}
    
    /** Change vertical position of this component w.r.t the position of the CGlyph's primary glyph (relative coordinates). */
    public void setVerticalOffset(long y){yoffset=y;}

    /** Get position of this component w.r.t the position of the CGlyph's primary glyph (relative coordinates). */
    public LongPoint getOffset(){return new LongPoint(xoffset,yoffset);}

    /**
     * Set angle offset. It is initialized with the glyph's own orientation at SGlyph creation time, but it can be changed afterwards.
     */
    public void setAngleOffset(float angle){
	float newangle=g.getOrient()-aoffset+angle;
	aoffset=angle;
	g.orientTo(newangle);
    }

    /**
     * Get angle offset. It is initialized with the glyph's own orientation at SGlyph creation time, but it can be changed afterwards.
     */
    public float getAngleOffset(){return aoffset;}

}
