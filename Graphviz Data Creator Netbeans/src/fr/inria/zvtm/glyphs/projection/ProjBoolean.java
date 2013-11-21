/*   FILE: ProjBoolean.java
 *   DATE OF CREATION:   Nov 08 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Thu Jan 24 10:29:22 2002 by Emmanuel Pietriga
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
 *
 * $Id: ProjBoolean.java 2102 2009-06-23 08:57:56Z rprimet $
 */

package fr.inria.zvtm.glyphs.projection;

import java.awt.geom.Area;

/**project coordinates of a boolean shape
 * @author Emmanuel Pietriga
 */

public class ProjBoolean extends ProjectedCoords {

    /**main shape*/
    public Area mainArea;
    /**main shape size in camera space*/
    public float cszx,cszy;

    /**main shape in lens space*/
    public Area lmainArea;
    /**main shape size in lens space*/
    public float lcszx, lcszy;

}
