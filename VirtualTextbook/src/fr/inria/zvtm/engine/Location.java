/*   FILE: Location.java
 *   DATE OF CREATION:   Fri Jan 31 14:13:31 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Mon Feb 03 10:50:18 2003 by Emmanuel Pietriga
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */ 

package fr.inria.zvtm.engine;

import java.util.Vector;

import fr.inria.zvtm.engine.LongPoint;

/**Mean to store a camera location - provides 2 long fields for X,Y and a float field for altitude*/

public class Location {


    /**
     * returns the difference betzeen two locations (l2-l1, how much to go from l1 to l2) as a vector whose first element is the altitude difference and second element is a LongPoint for X,Y difference
     */
    public static Vector getDifference(Location l1,Location l2){
	Vector res=new Vector();
	Float f=new Float(l2.getAltitude()-l1.getAltitude());
	res.add(f);
	LongPoint p=new LongPoint(l2.getX()-l1.getX(),l2.getY()-l1.getY());
	res.add(p);
	return res;
    }

    public static boolean equals(Location l1,Location l2){
	if (l1.getX()==l2.getX() && l1.getY()==l2.getY() && l1.getAltitude()==l2.getAltitude()){return true;}
	else return false;
    }

    public Location(long x,long y,float a){
	vx=x;
	vy=y;
	alt=a;
    }

    /**a zvtm X coordinate*/
    public long vx;

    /**a zvtm Y coordinate*/
    public long vy;

    /**a zvtm altitude*/
    public float alt;

    public void setPosition(LongPoint p){
	vx=p.x;
	vy=p.y;
    }

    public void setPositionX(long x){
	vx=x;
    }

    public void setPositionY(long y){
	vy=y;
    }

    public void setAltitude(float a){
	alt=a;
    }

    public LongPoint getPosition(){
        return new LongPoint(vx,vy);
    }

    public long getX(){
	return vx;
    }

    public long getY(){
	return vy;
    }

    public float getAltitude(){
	return alt;
    }

    public String toString(){
	return "x="+vx+", y="+vy+", alt="+alt;
    }

}
