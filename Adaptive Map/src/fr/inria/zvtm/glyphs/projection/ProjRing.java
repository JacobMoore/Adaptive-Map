/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: $
 */

package fr.inria.zvtm.glyphs.projection;

import java.awt.geom.Area;

/**project coordinates of a ring slice
 * @author Emmanuel Pietriga
 */

public class ProjRing extends ProjSlice {

    public int innerRingRadius;

    public int linnerRingRadius;
    
    public Area ring, lring;
    
}
