/*   Copyright (c) INRIA, 2011-2012. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: Menu.java 4892 2012-12-21 13:18:25Z epietrig $
 */

package fr.inria.zvtm.widgets;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.PRectangle;
import fr.inria.zvtm.glyphs.VText;

/**
 * Implementation of a linear menu.
 *@author R.Primet, INRIA
 *@author E.Pietriga, INRIA Chile
 */

public class Menu {
    protected double width = 200;
    protected static final double DEFAULT_ITEM_HEIGHT = 25;
    protected static final Color DEFAULT_MENU_BGCOLOR = new Color(70, 70, 70);
    protected static final Color DEFAULT_MENU_HCOLOR = new Color(120,120,120);
    protected static final Color DEFAULT_MENU_LBCOLOR = Color.WHITE;
    protected Color MENU_BGCOLOR = DEFAULT_MENU_BGCOLOR;
    protected Color MENU_HCOLOR = DEFAULT_MENU_HCOLOR;
    protected Color MENU_LBCOLOR = DEFAULT_MENU_LBCOLOR;
    protected short LABEL_ANCHOR = VText.TEXT_ANCHOR_MIDDLE;
    protected static final String MENU_BOX_TYPE = "MENU_BOX";

    protected final VirtualSpace space;
    protected final List<Glyph> elems = new ArrayList<Glyph>();

    public Menu(VirtualSpace space){
        this.space = space;
    }


    /**
     *@param space VirtualSpace in which to instantiate the menu
     *@param bgColor background color of menu item boxes
     *@param hColor color of menu item boxes (when cursor inside)
     *@param lColor menu item label color
     *@param labelAnchor alignment (one of VText.TEXT_ANCHOR_*)
     */
    public Menu(VirtualSpace space, Color bgColor, Color hColor, Color lColor, short labelAnchor){
        this.space = space;
        MENU_BGCOLOR = bgColor;
        MENU_HCOLOR = hColor;
        MENU_LBCOLOR = lColor;
        LABEL_ANCHOR = labelAnchor;
    }

    /**
     * Shows the menu
     */
    public void show(){
        for(Glyph elem: elems){
            space.show(elem);
        }
    }

    /**
     * Hides the menu
     */
    public void hide(){
        for(Glyph elem: elems){
            space.hide(elem);
        }
    }

    /**
     * Clears the menu
     */
    public void clear(){
        for(Glyph elem: elems){
            space.removeGlyph(elem);
        }
        elems.clear();
    }

    /**
     * Returns the width of the menu, in virtual space units.
     */
    public double getWidth(){
        return width;
    }

    /**
     * Sets the width of the menu.
     * @param width new width of the menu, in virtualspace units
     * @throws IllegalArgumentException if <code>width</code> is less than or equal to 0.
     */
    public void setWidth(double width){
        if(width <= 0){
            throw new IllegalArgumentException("setWidth: positive value expected");
        }
        this.width = width;
    }

    /**
     * Override this method if you need to customize menu labels
     * (e.g. set color...)
     */
    protected VText makeLabel(MenuItem item, double xpos, double ypos){
        VText retval = new VText(xpos, ypos, 0, DEFAULT_MENU_LBCOLOR, 
                    item.getText());
        retval.setTextAnchor(LABEL_ANCHOR);
        return retval;
    }

    /**
     * Builds a menu from the given items.
     * @param coords coordinates of the topmost menu item, in virtual space units.
     */
    public void populate(List<MenuItem> items, Point2D.Double coords){
        double xpos = coords.getX();
        double ypos = coords.getY();
        for(MenuItem item: items){
            PRectangle menuBox = new PRectangle(xpos, ypos, 0, 
                    width, DEFAULT_ITEM_HEIGHT, 
                    Utils.makeDiagGradient((float)width, 
                        (float)DEFAULT_ITEM_HEIGHT, MENU_BGCOLOR));
            menuBox.setCursorInsidePaint(Utils.makeDiagGradient(
                        (float)width, (float)DEFAULT_ITEM_HEIGHT,
                        MENU_HCOLOR));
            menuBox.setType(MENU_BOX_TYPE);
            menuBox.setOwner(item);

            VText label = makeLabel(item, xpos, ypos);
            label.setSensitivity(false);

            elems.add(menuBox);
            elems.add(label);
            space.addGlyph(menuBox);
            space.addGlyph(label);
            ypos -= (DEFAULT_ITEM_HEIGHT + 1);
        }
    }

    /**
     * Returns the MenuItem associated to a given Glyph, if
     * there is such an item. 
     * @return the MenuItem associated to the given Glyph, or null
     * if there is no such item.
     */
    public MenuItem getItemForGlyph(Glyph glyph){
        int idx = elems.indexOf(glyph);
        if(idx == -1){
            return null;
        }
        assert(elems.get(idx).getOwner() instanceof MenuItem);
        return (MenuItem)(elems.get(idx).getOwner());
    }

    /**
     * Should be called from outside to highlight the menu items
     * if needed.
     */
    public void onEnterGlyph(Glyph g){
        if(g.getType().equals(MENU_BOX_TYPE) && elems.contains(g)){
            g.highlight(true, null);
        }
    }

    /**
     * Should be called from outside to highlight the menu items
     * if needed.
     */
    public void onExitGlyph(Glyph g){
        if(g.getType().equals(MENU_BOX_TYPE) && elems.contains(g)){
            g.highlight(false, null);
        }
    }
  }

