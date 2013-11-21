/*   FILE: ObservedRegionListener.java
 *   DATE OF CREATION:  Wed Jul 12 15:01:11 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ObservedRegionListener.java 2102 2009-06-23 08:57:56Z rprimet $
 */ 

package fr.inria.zvtm.engine;

/** Interface to handle events related to areas representing the region observed through a camera
 * @author Emmanuel Pietriga
 */

public interface ObservedRegionListener {

    /**projected values of west, north, east and south intersections*/
    public void intersectsParentRegion(long[] wnes);
    

}
