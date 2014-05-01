/*   Copyright (c) INRIA, 2013. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: PCircle.java 4932 2013-02-15 18:15:04Z epietrig $
 */

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.Paint;

import fr.inria.zvtm.glyphs.projection.BProjectedCoords;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VCircle;

/**
 * Circle filled using a customizable gradient paint.
 *@author Emmanuel Pietriga
 *@see fr.inria.zvtm.glyphs.VCircle
 *@see fr.inria.zvtm.glyphs.PRectangle
 *@see fr.inria.zvtm.glyphs.SICircle
 */

public class PCircle<T> extends VCircle {

    Paint gp;
    Paint highlightPaint = null;
    volatile boolean highlighted = false;

    AffineTransform at;

    /**
     *@param gp fill gradient paint
     */
    public PCircle(Paint gp){
        this(0, 0, 0, 10, gp);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param d diameter in virtual space
     *@param p gradient or texture paint
     */
    public PCircle(double x, double y, int z, double d, Paint p){
        this(x, y, z, d, p, Color.BLACK);
    }

    /**
        *@param x coordinate in virtual space
        *@param y coordinate in virtual space
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param d diameter in virtual space
        *@param p gradient or texture paint
        *@param bc border color
        */
    public PCircle(double x, double y, int z, double d, Paint p, Color bc){
        super(x, y, z, d, Color.WHITE, bc);
        this.gp = p;
    }

    /** Set a gradient or texture paint to fill this glyph.
     *@param p gradient or texture paint
     */
    public void setPaint(Paint p){
        this.gp = p;
    }

    /** Get the gradient or texture paint used to fill this glyph
     *@return gradient or texture paint used
     */
    public Paint getPaint(){
        return gp;
    }

    /**
     * Sets the highlight Paint.
     * @param p the Paint to use when highlighting this PRectangle,
     * or null to disable highlighting.
     */
    public void setCursorInsidePaint(Paint p){
        highlightPaint = p;
    }

    /**
     * Highlights this PCircle. The Color argument is unused.
     */
    @Override public void highlight(boolean h, Color unused){
        highlighted = h;
        VirtualSpaceManager.INSTANCE.repaint();
    }

    @Override
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if ((pc[i].cr>1) && (pc[i].cr>1)){
            at = AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cr/2,dy+pc[i].cy-pc[i].cr/2);
            at.concatenate(AffineTransform.getScaleInstance(coef, coef));
            g.setTransform(at);
            //repaint only if object is visible
            if (isFilled()){
                g.setPaint((highlighted && highlightPaint != null) ? highlightPaint : gp);
                g.fillOval(0, 0, (int)Math.round(size), (int)Math.round(size));
            }
            if (isBorderDrawn()){
                g.setColor(borderColor);
                if (stroke!=null) {
                    g.setStroke(stroke);
                    g.drawOval(0, 0, (int)Math.round(size), (int)Math.round(size));
                    g.setStroke(stdS);
                }
                else {
                    g.drawOval(0, 0, (int)Math.round(size), (int)Math.round(size));
                }
            }
            g.setTransform(stdT);
        }
        else {
            g.setColor(this.color);
            g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
        }
    }

    @Override
    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if ((pc[i].lcr>1) && (pc[i].lcr>1)){
            at = AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcr/2,dy+pc[i].lcy-pc[i].lcr/2);
            at.concatenate(AffineTransform.getScaleInstance(coef, coef));
            g.setTransform(at);
            //repaint only if object is visible
            if (isFilled()){
                g.setPaint((highlighted && highlightPaint != null) ? highlightPaint : gp);
                g.fillOval(0, 0, (int)Math.round(size), (int)Math.round(size));
            }
            if (isBorderDrawn()){
                g.setColor(borderColor);
                if (stroke!=null) {
                    g.setStroke(stroke);
                    g.drawOval(0, 0, (int)Math.round(size), (int)Math.round(size));
                    g.setStroke(stdS);
                }
                else {
                    g.drawOval(0, 0, (int)Math.round(size), (int)Math.round(size));
                }
            }
            g.setTransform(stdT);
        }
        else {
            g.setColor(this.color);
            g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
        }
    }

    @Override
    public Object clone(){
        PCircle res = new PCircle(vx, vy, 0, size, gp, getBorderColor());
        res.cursorInsideColor = this.cursorInsideColor;
        return res;
    }

}
