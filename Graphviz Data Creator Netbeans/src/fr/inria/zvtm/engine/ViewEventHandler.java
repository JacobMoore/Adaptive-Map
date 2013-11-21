/*   FILE: ViewEventHandler.java
 *   DATE OF CREATION:  Fri May 26 15:01:11 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ViewEventHandler.java 2102 2009-06-23 08:57:56Z rprimet $
 */ 

package fr.inria.zvtm.engine;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.glyphs.Glyph;

/** Interface to handle events happening in a View. Replaces abstract class fr.inria.zvtm.engine.AppEventHandler
 * @author Emmanuel Pietriga
 */

public interface ViewEventHandler {

    public static int NO_MODIFIER = 0;
    public static int SHIFT_MOD = 1;
    public static int CTRL_MOD = 2;
    public static int CTRL_SHIFT_MOD = 3;
    public static int META_MOD = 4;
    public static int META_SHIFT_MOD = 5;
    public static int ALT_MOD = 6;
    public static int ALT_SHIFT_MOD = 7;
    
    public static short WHEEL_UP = 1;
    public static short WHEEL_DOWN = 0;
    
    /**mouse button 1 pressed callback
     *@param mod one of NO_MODIFIER, SHIFT_MOD, CTRL_MOD, CTRL_SHIFT_MOD, META_MOD, META_SHIFT_MOD, ALT_MOD, ALT_SHIFT_MOD
     */
    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e);
    /**mouse button 1 released callback
     *@param mod one of NO_MODIFIER, SHIFT_MOD, CTRL_MOD, CTRL_SHIFT_MOD, META_MOD, META_SHIFT_MOD, ALT_MOD, ALT_SHIFT_MOD
     */
    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e);
    /**mouse button 1 clicked callback
     *@param mod one of NO_MODIFIER, SHIFT_MOD, CTRL_MOD, CTRL_SHIFT_MOD, META_MOD, META_SHIFT_MOD, ALT_MOD, ALT_SHIFT_MOD
     */
    public void click1(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e);
    /**mouse button 2 pressed callback
     *@param mod one of ALT_MOD, ALT_SHIFT_MOD (the middle mouse button by itself is considered as being ALT-modified by default by Java)
     */
    public void press2(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e);
    /**mouse button 2 released callback
     *@param mod one of ALT_MOD, ALT_SHIFT_MOD (the middle mouse button by itself is considered as being ALT-modified by default by Java)
     */
    public void release2(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e);
    /**mouse button 2 clicked callback
     *@param mod one of ALT_MOD, ALT_SHIFT_MOD (the middle mouse button by itself is considered as being ALT-modified by default by Java)
     */
    public void click2(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e);
    /**mouse button 3 pressed callback
     *@param mod one of META_MOD, META_SHIFT_MOD (the right mouse button by itself is considered as being META-modified by default by Java)
     */
    public void press3(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e);
    /**mouse button 3 released callback
     *@param mod one of META_MOD, META_SHIFT_MOD (the right mouse button by itself is considered as being META-modified by default by Java)
     */
    public void release3(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e);
    /**mouse button 3 clicked callback
     *@param mod one of META_MOD, META_SHIFT_MOD (the right mouse button by itself is considered as being META-modified by default by Java)
     */
    public void click3(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e);

    /**mouse moved callback (this callback needs to be activated by View.setNotifyMouseMoved() ; it is inactive by default)*/
    public void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e);

    /**mouse dragged callback
     *@param buttonNumber one of 1 (left), 2 (middle) or 3 (right)
     *@param jpx mouse coord in JPanel coord syst
     *@param jpy mouse coord in JPanel coord syst
     *@param mod see pressX/releaseX/clickX with X=buttonNumber for possible values
     */
    public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpx, int jpy, MouseEvent e);

    /**mouse wheel moved callback
     *@param wheelDirection is one of WHEEL_UP, WHEEL_DOWN
     *@param jpx mouse coord in JPanel coord syst
     *@param jpy mouse coord in JPanel coord syst
     */
    public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpx, int jpy, MouseWheelEvent e);

    /**cursor enters a Glyph
     *@param g Glyph the cursor just entered
     */
    public void enterGlyph(Glyph g);

    /**cursor exits a Glyph
     *@param g Glyph the cursor just exited
     */
    public void exitGlyph(Glyph g);

    /**beware: code is always 0 in Ktype (it is the value of KeyEvent.getKeyCode() which is always equal to VK_UNDEFINED according to Sun). If you need to access code, use Kpress or Krelease.*/
    public void Ktype(ViewPanel v, char c, int code, int mod, KeyEvent e);
    public void Kpress(ViewPanel v, char c, int code, int mod, KeyEvent e);
    public void Krelease(ViewPanel v, char c, int code, int mod, KeyEvent e);

    public void viewActivated(View v);
    public void viewDeactivated(View v);
    public void viewIconified(View v);
    public void viewDeiconified(View v);
    public void viewClosing(View v);

}
