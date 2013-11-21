/*   FILE: BooleanOps.java
 *   DATE OF CREATION:   Oct 03 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Thu Jan 24 10:29:38 2002 by Emmanuel Pietriga
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
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

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * Class used to store a boolean operation to be made on a VBoolShape. Right now we only support RectangularShape derivatives (Ellipse, Rectangle).
 * @author Emmanuel Pietriga
 */

public class BooleanOps {

    public static final short BOOLEAN_UNION = 1;
    public static final short BOOLEAN_SUBTRACTION = 2;
    public static final short BOOLEAN_INTERSECTION = 3;
    public static final short BOOLEAN_XOR = 4;

    public static final short SHAPE_TYPE_ELLIPSE = 1;
    public static final short SHAPE_TYPE_RECTANGLE = 2;

    /** Horizontal distance to main glyph's center in virtual space. */
    public long ox;
    /** Vertical distance to main glyph's center in virtual space. */
    public long oy;

    /** Width. */
    public long szx;
    /** Height. */
    public long szy;
    
    /** One of SHAPE_TYPE_*. */
    public int shapeType;
    
    /** One of  BOOLEAN_*. */
    public int opType;

    /* Actual projected area (used in the boolean operation before drawing, called by project()) */
    Area ar;
    Area lar;
    
    /** 
     *@param x virtual horizontal distance to main shape's center in virtual space
     *@param y virtual vertical distance to main shape's center in virtual space
     *@param sx size along X axis
     *@param sy size along Y axis
     *@param t shape type, one of SHAPE_TYPE_*
     *@param o boolean operation type, one of BOOLEAN_*
     */
    public BooleanOps(long x,long y,long sx,long sy,int t,int o){
	ox=x;
	oy=y;
	szx=sx;
	szy=sy;
	shapeType=t;
	opType=o;
    }

    void project(float coef,long cx,long cy){//cx and cy are projected coordinates of main area
	switch (shapeType) {
	case SHAPE_TYPE_ELLIPSE:{//ellipse
	    ar=new Area(new Ellipse2D.Float(cx+(ox-szx/2)*coef,cy-(oy+szy/2)*coef,szx*coef,szy*coef));
	    break;
	}
	case SHAPE_TYPE_RECTANGLE:{//rectangle
	    ar=new Area(new Rectangle2D.Float(cx+(ox-szx/2)*coef,cy-(oy+szy/2)*coef,szx*coef,szy*coef));
	    break;
	}
	default:{//ellipse as default
	    ar=new Area(new Ellipse2D.Float(cx+(ox-szx/2)*coef,cy-(oy+szy/2)*coef,szx*coef,szy*coef));
	}
	}
    }

    void projectForLens(float coef,long cx,long cy){//cx and cy are projected coordinates of main area
	switch (shapeType) {
	case SHAPE_TYPE_ELLIPSE:{//ellipse
	    lar=new Area(new Ellipse2D.Float(cx+(ox-szx/2)*coef,cy-(oy+szy/2)*coef,szx*coef,szy*coef));
	    break;
	}
	case SHAPE_TYPE_RECTANGLE:{//rectangle
	    lar=new Area(new Rectangle2D.Float(cx+(ox-szx/2)*coef,cy-(oy+szy/2)*coef,szx*coef,szy*coef));
	    break;
	}
	default:{//ellipse as default
	    lar=new Area(new Ellipse2D.Float(cx+(ox-szx/2)*coef,cy-(oy+szy/2)*coef,szx*coef,szy*coef));
	}
	}
    }

}
