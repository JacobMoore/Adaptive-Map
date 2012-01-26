/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: DynaSpotListener.java 2102 2009-06-23 08:57:56Z rprimet $
 */
 
package fr.inria.zvtm.engine;

import fr.inria.zvtm.engine.VCursor;

public interface DynaSpotListener {
	
	public void spotSizeChanged(VCursor c, int dynaSpotRadius);
	
}
