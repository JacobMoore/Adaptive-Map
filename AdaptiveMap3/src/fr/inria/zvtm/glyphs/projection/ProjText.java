/*   FILE: ProjText.java
 *   DATE OF CREATION:   Nov 24 2001
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
 * $Id: ProjText.java 4949 2013-02-22 20:10:46Z epietrig $
 */

package fr.inria.zvtm.glyphs.projection;

/**project coordinates of a rectangle
 * @author Emmanuel Pietriga
 */

public class ProjText extends ProjectedCoords {

    public double cw,ch;

    public double lcw,lch;

    /**tells whether the bounding rectangle needs to be computed at next paint call or not (in main space)*/
    public boolean valid=false;

    /**tells whether the bounding rectangle needs to be computed at next paint call or not (in lens space)*/
    public boolean lvalid = false;

}
