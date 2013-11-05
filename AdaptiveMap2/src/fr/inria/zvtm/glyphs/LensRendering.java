/*   FILE: LensRendering.java
 *   DATE OF CREATION:  Mon May 29 08:34:23 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: LensRendering.java 2102 2009-06-23 08:57:56Z rprimet $
 */

package fr.inria.zvtm.glyphs;

import java.awt.Color;

/**
 * Lens rendering interface implemented by some Glyphs.
 * Makes it possible to change some rendering attributes depending on whether the glyph is seen through a distortion lens or not.
 * @author Emmanuel Pietriga
 **/

public interface LensRendering {

    /** Make this glyph (in)visible when seen through a lens.
     * The glyph remains sensitive to cursor in/out events.
     *@param b true to make glyph visible, false to make it invisible
     */
    public void setVisibleThroughLens(boolean b);

    /** Get this glyph's visibility state when seen through the lens. */
    public boolean isVisibleThroughLens();

    /** Set the color used to paint the glyph's interior. */
    public void setFillColorThroughLens(Color c);

    /** Set the color used to paint the glyph's border. */
    public void setBorderColorThroughLens(Color c);

    /** Get the color used to paint the glyph's interior. */
    public Color getFillColorThroughLens();

    /** Get the color used to paint the glyph's border. */
    public Color getBorderColorThroughLens();

}
