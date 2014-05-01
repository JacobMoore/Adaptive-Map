/*   FILE: ProjectedCoords.java
 *   DATE OF CREATION:   Nov 08 2000
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
 * $Id: RProjectedCoords.java 4949 2013-02-22 20:10:46Z epietrig $
 */

package fr.inria.zvtm.glyphs.projection;

/**project coordinates - parent class of all projected coordinates objects
 * @author Emmanuel Pietriga
 */

public class RProjectedCoords extends ProjectedCoords {

    /**half width and height in camera space*/
    public int cw,ch;

    /**half width and height in lens space*/
    public int lcw,lch;

}
