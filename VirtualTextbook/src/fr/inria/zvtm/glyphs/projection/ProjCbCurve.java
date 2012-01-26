/*   FILE: ProjCbCurve.java
 *   DATE OF CREATION:   Oct 03 2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Thu Jan 24 10:29:57 2002 by Emmanuel Pietriga
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
 * $Id: ProjCbCurve.java 2102 2009-06-23 08:57:56Z rprimet $
 */

package fr.inria.zvtm.glyphs.projection;

import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;

/**project coordinates of a quadratic curve
 * @author Emmanuel Pietriga
 */

public class ProjCbCurve extends BProjectedCoords {

    /**curve*/
    public CubicCurve2D.Double curve=new CubicCurve2D.Double();
    /**start point*/
    public Point2D.Double start=new Point2D.Double();
    /**end point*/
    public Point2D.Double end=new Point2D.Double();
    /**control point 1 (controls start point tangent vector)*/
    public Point2D.Double ctrlStart=new Point2D.Double();
    /**control point 2 (controls end point tangent vector)*/
    public Point2D.Double ctrlEnd=new Point2D.Double();

    /**curve*/
    public CubicCurve2D.Double lcurve=new CubicCurve2D.Double();
    /**start point*/
    public Point2D.Double lstart=new Point2D.Double();
    /**end point*/
    public Point2D.Double lend=new Point2D.Double();
    /**control point 1 (controls start point tangent vector)*/
    public Point2D.Double lctrlStart=new Point2D.Double();
    /**control point 2 (controls end point tangent vector)*/
    public Point2D.Double lctrlEnd=new Point2D.Double();

}
