/*   Copyright (c) INRIA, 2010-2013. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;

import java.util.Vector;

/**
 * Multiline text.
 * By default, text will be rendered on one line. Specifiy a width
 * constraint to make the text overflow on multiple lines. Specify
 * a height constraint to truncate the text. A height constraint is only
 * meaningful if a width constraint has been defined.
 * The 'hot spot' of a MultilineText instance is its top-left corner.
 * This is unlike VText.
 * Note: setting a background color is unsupported.
 */
public class MultilineText<T> extends VText {

    // See http://java.sun.com/developer/onlineTraining/Media/2DText/style.html#multiple
    private double widthConstraint = Double.POSITIVE_INFINITY;
    private double heightConstraint = Double.POSITIVE_INFINITY;
    private LineBreakMeasurer lbm;
    private AttributedString atText;
    private static final FontRenderContext DEFAULT_FRC =
        new FontRenderContext(null, false, false);

    TextLayout[] lines;

    public MultilineText(String text){
        super(text);
        initLbm();
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param c fill color
     *@param bkg background color (null if not painted)
     *@param t text string
     *@param ta text-anchor (for alignment: one of TEXT_ANCHOR_*)
     *@param scale scaleFactor w.r.t original image size
      *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
     */
    public MultilineText(double x, double y, int z, Color c, String t, short ta, float scale, float alpha){
        super(x, y, z, c, t, ta, scale, alpha);
        initLbm();
        processText();
    }

    void initLbm(){
        atText = new AttributedString(text);
        atText.addAttribute(TextAttribute.FONT,
                usesSpecificFont() ? getFont() : getMainFont());
        lbm = new LineBreakMeasurer(atText.getIterator(), DEFAULT_FRC);
        processText();
    }

    /**
     * Width constraint, in virtual space units.
     */
    public void setWidthConstraint(double constraint){
        widthConstraint = constraint;
        processText();
        invalidate();
    }

    /**
     * Height constraint, in virtual space units.
     * Text will be truncated if it overflows the
     * height constraint.
     */
    public void setHeightConstraint(double constraint){
        heightConstraint = constraint;
        invalidate();
    }

    /**
     * Gets the width constraint for this MultilineText.
     * A value of Double.POSITIVE_INFINITY means that the
     * width is unconstrained.
     */
    public double getWidthConstraint(){
        return widthConstraint;
    }

    public double getHeightConstraint(){
        return heightConstraint;
    }

    @Override public void setText(String text){
        super.setText(text);
        atText = new AttributedString(text);
        lbm = new LineBreakMeasurer(atText.getIterator(), DEFAULT_FRC);
        processText();
        invalidate();
    }

    @Override public void setFont(Font f){
        super.setFont(f);
        atText.addAttribute(TextAttribute.FONT,
                usesSpecificFont() ? getFont() : getMainFont());
        lbm = new LineBreakMeasurer(atText.getIterator(), DEFAULT_FRC);
        processText();
        invalidate();
    }

    @Override
    public boolean visibleInRegion(double wb, double nb, double eb, double sb, int i){
        if (!validBounds(i)){return true;}
        if ((vx>=wb) && (vx<=eb) && (vy>=sb) && (vy<=nb)){
            //if glyph hotspot is in the region, it is obviously visible
            return true;
        }
        else {
            // cw and ch actually hold width and height of text *in virtual space*
            if (text_anchor==TEXT_ANCHOR_START){
                //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
                //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
                return (vx<=eb) && ((vx+pc[i].cw)>=wb) && (vy>=sb) && ((vy-pc[i].ch)<=nb);
            }
            else if (text_anchor==TEXT_ANCHOR_MIDDLE){
                return ((vx-pc[i].cw/2<=eb) && ((vx+pc[i].cw/2)>=wb) && (vy>=sb) && ((vy-pc[i].ch)<=nb));
                //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
                //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
            }
            else {
                //TEXT_ANCHOR_END
                return ((vx-pc[i].cw<=eb) && (vx>=wb) && (vy>=sb) && ((vy-pc[i].ch)<=nb));
                //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
                //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
            }
        }
    }

    @Override
    public boolean containedInRegion(double wb, double nb, double eb, double sb, int i){
        if ((vx>=wb) && (vx<=eb) && (vy <= nb) && (vy >= sb)){
            /* Glyph hotspot is in the region.
               There is a good chance the glyph is contained in the region, but this is not sufficient. */
            // cw and ch actually hold width and height of text *in virtual space*
            if (text_anchor==TEXT_ANCHOR_START){
                //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
                return ((vx<=eb) && ((vx+pc[i].cw)>=wb) && (vy <= nb) && (vy - pc[i].ch >= sb));
                //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
            }
            else if (text_anchor==TEXT_ANCHOR_MIDDLE){
                return ((vx+pc[i].cw/2<=eb) && ((vx-pc[i].cw/2)>=wb) && (vy <= nb) && (vy - pc[i].ch >= sb));
                //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
                //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
            }
            else {//TEXT_ANCHOR_END
                return ((vx+pc[i].cw<=eb) && (vx>=wb) && (vy <= nb) && (vy - pc[i].ch >= sb));
                //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
                //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
            }
        }
        return false;
    }

    @Override
    public boolean coordInside(int jpx, int jpy, int camIndex, double cvx, double cvy){
        boolean res=false;
        switch (text_anchor){
            case VText.TEXT_ANCHOR_START:{
                if ((cvx>=vx) && (cvy<=vy) && (cvx<=(vx+pc[camIndex].cw)) && (cvy >= vy-pc[camIndex].ch)){res=true;}
                break;
            }
            case VText.TEXT_ANCHOR_MIDDLE:{
                if ((cvx>=vx-pc[camIndex].cw/2) && (cvy<=vy) && (cvx<=(vx+pc[camIndex].cw/2)) && (cvy >= vy-pc[camIndex].ch)){res=true;}
                break;
            }
            default:{
                if ((cvx<=vx) && (cvy<=vy) && (cvx>=(vx-pc[camIndex].cw)) && (cvy >= vy-pc[camIndex].ch)){res=true;}
            }
        }
        return res;
    }

    void processText(){
        lbm.setPosition(atText.getIterator().getBeginIndex());
        int paragraphEnd = atText.getIterator().getEndIndex();
        float drawPosY = 0;
        TextLayout line = null;
        Vector<TextLayout> lineV = new Vector(5);
        while(lbm.getPosition() < paragraphEnd &&
                      drawPosY <= heightConstraint){
            line = lbm.nextLayout((float)widthConstraint);
            lineV.add(line);
            drawPosY += line.getAscent() + line.getDescent() + line.getLeading();
        }
        lines = lineV.toArray(new TextLayout[lineV.size()]);
    }

    /** Get number of lines in this MultilineText.
     * This may vary depending on constraintWidth's value.
     */
    public int getLineCount(){
        return lines.length;
    }

    @Override public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alphaC != null && alphaC.getAlpha()==0){return;}
        double trueCoef = scaleFactor * coef;
        if (trueCoef*fontSize > VText.TEXT_AS_LINE_PROJ_COEF || !zoomSensitive || !pc[i].valid){
            //if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
            g.setFont((font!=null) ? font : getMainFont());
            AffineTransform at = AffineTransform.getTranslateInstance(dx+pc[i].cx,dy+pc[i].cy);
            if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(trueCoef, trueCoef));}
            g.setTransform(at);
            g.setColor(this.color);
            float drawPosY = 0;
            Rectangle2D lbounds;
            if (alphaC != null){
                g.setComposite(alphaC);
                for (TextLayout line:lines){
                    drawPosY += line.getAscent();
                    if (text_anchor==TEXT_ANCHOR_START){
                        line.draw(g, 0, drawPosY);
                    }
                    else if (text_anchor==TEXT_ANCHOR_MIDDLE){
                        lbounds = line.getBounds();
                        line.draw(g, (float)(-lbounds.getWidth()/2f), drawPosY);
                    }
                    else {
                        // text_anchor == TEXT_ANCHOR_END
                        lbounds = line.getBounds();
                        line.draw(g, (float)-lbounds.getWidth(), drawPosY);
                    }
                    drawPosY += line.getDescent() + line.getLeading();
                }
                g.setComposite(acO);
            }
            else {
                for (TextLayout line:lines){
                    drawPosY += line.getAscent();
                    if (text_anchor==TEXT_ANCHOR_START){
                        line.draw(g, 0, drawPosY);
                    }
                    else if (text_anchor==TEXT_ANCHOR_MIDDLE){
                        lbounds = line.getBounds();
                        line.draw(g, (float)(-lbounds.getWidth()/2f), drawPosY);
                    }
                    else {
                        // text_anchor == TEXT_ANCHOR_END
                        lbounds = line.getBounds();
                        line.draw(g, (float)-lbounds.getWidth(), drawPosY);
                    }
                    drawPosY += line.getDescent() + line.getLeading();
                }
            }
            g.setTransform(stdT);
            if (!pc[i].valid || (!zoomSensitive && coef != oldcoef)){
                if(widthConstraint == Double.POSITIVE_INFINITY){
                    double max = lines[0].getBounds().getWidth();
                    for (int j=1;j<lines.length;j++){
                        if (lines[j].getBounds().getWidth() > max){max = lines[j].getBounds().getWidth();}
                    }
                    if (zoomSensitive){
                        pc[i].cw = max * scaleFactor;
                        pc[i].ch = drawPosY * scaleFactor;
                    }
                    else {
                        pc[i].cw = max * scaleFactor / coef;
                        pc[i].ch = drawPosY * scaleFactor / coef;
                        oldcoef = coef;
                    }
                }
                else {
                    if (zoomSensitive){
                        pc[i].cw = widthConstraint * scaleFactor;
                        pc[i].ch = drawPosY * scaleFactor;
                    }
                    else {
                        pc[i].cw = widthConstraint * scaleFactor / coef;
                        pc[i].ch = drawPosY * scaleFactor / coef;
                    }
                }
                pc[i].valid = true;
            }
        }
        else {
            g.setColor(this.color);
            if (alphaC != null){
                g.setComposite(alphaC);
                g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
                g.setComposite(acO);
            }
            else {
                g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
            }
        }
    }

    @Override public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        //XXX
    }

    @Override public Object clone(){
        MultilineText res = new MultilineText(vx, vy, vz, color, (new StringBuffer(text)).toString(),
            text_anchor, getScale(), (alphaC != null) ? alphaC.getAlpha() : 1.0f);
        res.cursorInsideColor = this.cursorInsideColor;
        return res;
    }
}
