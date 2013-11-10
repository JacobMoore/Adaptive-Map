/*   FILE: PolarCoords.java
 *   DATE OF CREATION:   Oct 05 2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Thu Jan 24 10:21:47 2002 by Emmanuel Pietriga
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


package fr.inria.zvtm.engine;

  /**
   * polar coordinates of a point (r,theta)
   * @author Emmanuel Pietriga
   **/

public class PolarCoords{

    /**radius coordinate*/
    public long r;
    /**angle coordinate*/
    public float theta;

    public PolarCoords(){
	r=0;
	theta=0;
    }

    public PolarCoords(long radius,float angle){
	r=radius;
	theta=angle;
    }

    public String toString(){
	return "("+r+","+theta+")";
    }

}
