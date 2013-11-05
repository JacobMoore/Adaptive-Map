/*   FILE: LongPoint.java
 *   DATE OF CREATION:   Aug 23 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Thu Jan 24 10:21:38 2002 by Emmanuel Pietriga
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2007. All Rights Reserved
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
   * similar to java.awt.Point but uses long instead of int
   * @author Emmanuel Pietriga
   **/

public class LongPoint{

    /**X coordinate*/
    public long x;
    /**Y coordinate*/
    public long y;

    public LongPoint(){
	x=0;
	y=0;
    }

    public LongPoint(long xc,long yc){
	x=xc;
	y=yc;
    }

    public LongPoint(double xc, double yc){
	x = Math.round(xc);
	y = Math.round(yc);
    }

    public void setLocation(long xc,long yc){
	this.x=xc;
	this.y=yc;
    }

    public void translate(long dx,long dy){
	this.x+=dx;
	this.y+=dy;
    }

    public double distance(LongPoint p){
        return distance(p.x, p.y);
    }

    public double distance(double px, double py){
        return Math.sqrt(distanceSq(px, py));
    }

    public double distanceSq(LongPoint p){
        return distanceSq(p.x, p.y);
    }

    public double distanceSq(double px, double py){
        return (this.x - px)*(this.x - px) + (this.y - py)*(this.y - py);
    }

    public String toString(){
	return "("+x+","+y+")";
    }

}
