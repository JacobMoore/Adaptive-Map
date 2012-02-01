/*   FILE: PortalEventHandler.java
 *   DATE OF CREATION:  Sat Jun 17 10:22:59 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: PortalEventHandler.java 2102 2009-06-23 08:57:56Z rprimet $
 */ 

package fr.inria.zvtm.engine;

/**Interface to handle events happening in a Portal.
 * @author Emmanuel Pietriga
 */

public interface PortalEventHandler {
    
    /**cursor enters portal*/
    public void enterPortal(Portal p);

    /**cursor exits portal*/
    public void exitPortal(Portal p);

}
