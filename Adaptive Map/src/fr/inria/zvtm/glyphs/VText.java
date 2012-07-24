/*   FILE: VText.java
 *   DATE OF CREATION:   Nov 23 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2010. All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For full terms see the file COPYING.
 *
 * $Id: VText.java 3443 2010-07-28 08:57:10Z epietrig $
 */
package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import fr.inria.zvtm.glyphs.projection.ProjText;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import java.awt.FontMetrics;

import controller.Configuration;

/**
 * Standalone Text.  This version is the most efficient, but it cannot be reoriented (see VTextOr*).<br>
 * Font properties are set globally in the view, but can be changed on a per-instance basis using setSpecialFont(Font f).<br>
 * (vx, vy) are the coordinates of the lower-left corner, or lower middle point, or lower-right corner depending on the text anchor (start, middle, end).
 * @author Emmanuel Pietriga
 *@see fr.inria.zvtm.glyphs.VTextOr
 */
public class VText extends Glyph {

    /** Text alignment (for text anchor) used to align a VText (vx,vy coordinates coincides with start of String). */
    public static final short TEXT_ANCHOR_START = 0;
    /** Text alignment (for text anchor) used to align a VText (vx,vy coordinates coincides with middle of String). */
    public static final short TEXT_ANCHOR_MIDDLE = 1;
    /** Text alignment (for text anchor) used to align a VText (vx,vy coordinates coincides with end of String). */
    public static final short TEXT_ANCHOR_END = 2;
    protected short text_anchor = TEXT_ANCHOR_START;
    /** For internal use. */
    protected ProjText[] pc;
    protected boolean zoomSensitive = true;
    protected static Font mainFont = new Font("Dialog", 0, 10);
    /** Font size in pixels (read-only). */
    protected float fontSize = mainFont.getSize2D();

    /* Begin edited code */
    /**
     * Width of the longest line
     */
    public long textContainerWidth;
    /**
     * Height of the lines
     */
    public long textContainerHeight;
    /* End edited code */
    
    /**returns default font used by glyphs*/
    public static Font getMainFont() {
        return mainFont;
    }

    /**set default font used by glyphs*/
    public static void setMainFont(Font f) {
        mainFont = f;
        VirtualSpaceManager.INSTANCE.onMainFontUpdated();
    }
    protected Font font;
    protected String text;
    protected float scaleFactor = 1.0f;

    public VText(String t) {
        this(0, 0, 0, Color.BLACK, t, TEXT_ANCHOR_START, 1f, 1f);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param c fill color
     *@param t text string
     */
    public VText(long x, long y, int z, Color c, String t) {
        this(x, y, z, c, t, TEXT_ANCHOR_START, 1f, 1f);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param c fill color
     *@param t text string
     *@param ta text-anchor (for alignment: one of TEXT_ANCHOR_*)
     */
    public VText(long x, long y, int z, Color c, String t, short ta) {
        this(x, y, z, c, t, ta, 1f, 1f);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param c fill color
     *@param t text string
     *@param ta text-anchor (for alignment: one of TEXT_ANCHOR_*)
     *@param scale scaleFactor w.r.t original image size
     */
    public VText(long x, long y, int z, Color c, String t, short ta, float scale) {
        this(x, y, z, c, t, ta, scale, 1.0f);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param c fill color
     *@param t text string
     *@param ta text-anchor (for alignment: one of TEXT_ANCHOR_*)
     *@param scale scaleFactor w.r.t original image size
     *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
     */
    public VText(long x, long y, int z, Color c, String t, short ta, float scale, float alpha) {
        vx = x;
        vy = y;
        vz = z;
        text = t;
        setColor(c);
        text_anchor = ta;
        scaleFactor = scale;
        setTranslucencyValue(alpha);
    }

    public void initCams(int nbCam) {
        pc = new ProjText[nbCam];
        for (int i = 0; i < nbCam; i++) {
            pc[i] = new ProjText();
        }
    }

    public void addCamera(int verifIndex) {
        if (pc != null) {
            if (verifIndex == pc.length) {
                ProjText[] ta = pc;
                pc = new ProjText[ta.length + 1];
                for (int i = 0; i < ta.length; i++) {
                    pc[i] = ta[i];
                }
                pc[pc.length - 1] = new ProjText();
            } else {
                System.err.println("VText:Error while adding camera " + verifIndex);
            }
        } else {
            if (verifIndex == 0) {
                pc = new ProjText[1];
                pc[0] = new ProjText();
            } else {
                System.err.println("VText:Error while adding camera " + verifIndex);
            }
        }
    }

    public void removeCamera(int index) {
        pc[index] = null;
    }

    public void resetMouseIn() {
        for (int i = 0; i < pc.length; i++) {
            resetMouseIn(i);
        }
    }

    public void resetMouseIn(int i) {
        if (pc[i] != null) {
            pc[i].prevMouseIn = false;
        }
    }

    /** Cannot be resized. */
    public void sizeTo(float factor) {
    }

    /** Cannot be resized. */
    public void reSize(float factor) {
    }

    /** Cannot be reoriented. */
    public void orientTo(float angle) {
    }

    /** Get glyph's size (radius of bounding circle).
     * Will return 0 if bounds of this VText have never been validated (through painting).
     *@see #getBounds(int i)
     *@see #validBounds(int i)
     *@see #invalidate()
     */
    public float getSize() {
        for (int i = 0; i < pc.length; i++) {
            if (pc[i] != null & pc[i].valid) {
                return (float) Math.sqrt(Math.pow(pc[i].cw, 2) + Math.pow(pc[i].ch, 2));
            }
        }
        // return 0 if could not find any valid bounds for any camera
        return 0;
    }

    public float getOrient() {
        return orient;
    }

    /** Set to false if the text should not be scaled according to camera's altitude. Its apparent size will always be the same, no matter the camera's altitude.
     *@see #isZoomSensitive()
     */
    public void setZoomSensitive(boolean b) {
        if (zoomSensitive != b) {
            zoomSensitive = b;
            VirtualSpaceManager.INSTANCE.repaintNow();
        }
    }

    /** Indicates whether the text is scaled according to camera's altitude.
     *@see #setZoomSensitive(boolean b)
     */
    public boolean isZoomSensitive() {
        return zoomSensitive;
    }

    public boolean visibleInRegion(long wb, long nb, long eb, long sb, int i) {
        if (!validBounds(i)) {
            return true;
        }
        if ((vx >= wb) && (vx <= eb) && (vy >= sb) && (vy <= nb)) {
            //if glyph hotspot is in the region, it is obviously visible
            return true;
        } else {
            // cw and ch actually hold width and height of text *in virtual space*
            if (text_anchor == TEXT_ANCHOR_START) {
                if ((vx <= eb) && ((vx + pc[i].cw) >= wb) && (vy <= nb) && ((vy + pc[i].ch) >= sb)) {
                    //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
                    //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
                    return true;
                } //otherwise the glyph is not visible
                else {
                    return false;
                }
            } else if (text_anchor == TEXT_ANCHOR_MIDDLE) {
                if ((vx - pc[i].cw / 2 <= eb) && ((vx + pc[i].cw / 2) >= wb) && (vy <= nb) && ((vy + pc[i].ch) >= sb)) {
                    //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
                    //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
                    return true;
                } //otherwise the glyph is not visible
                else {
                    return false;
                }
            } else {
                //TEXT_ANCHOR_END
                if ((vx - pc[i].cw <= eb) && (vx >= wb) && (vy <= nb) && ((vy + pc[i].ch) >= sb)) {
                    //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
                    //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
                    return true;
                } //otherwise the glyph is not visible
                else {
                    return false;
                }
            }
        }
    }

    public boolean containedInRegion(long wb, long nb, long eb, long sb, int i) {
        if ((vx >= wb) && (vx <= eb) && (vy >= sb) && (vy <= nb)) {
            /* Glyph hotspot is in the region.
            There is a good chance the glyph is contained in the region, but this is not sufficient. */
            // cw and ch actually hold width and height of text *in virtual space*
            if (text_anchor == TEXT_ANCHOR_START) {
                if ((vx <= eb) && ((vx + pc[i].cw) >= wb) && (vy <= nb) && ((vy - pc[i].ch) >= sb)) {
                    //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
                    return true;  //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
                }
            } else if (text_anchor == TEXT_ANCHOR_MIDDLE) {
                if ((vx + pc[i].cw / 2 <= eb) && ((vx - pc[i].cw / 2) >= wb) && (vy <= nb) && ((vy - pc[i].ch) >= sb)) {
                    //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
                    return true;  //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
                }
            } else {//TEXT_ANCHOR_END
                if ((vx + pc[i].cw <= eb) && (vx >= wb) && (vy <= nb) && ((vy - pc[i].ch) >= sb)) {
                    //if glyph is at least partially in region  (we approximate using the glyph bounding circle, meaning that some
                    return true;  //glyphs not actually visible can be projected and drawn  (but they won't be displayed))
                }
            }
        }
        return false;
    }

    public boolean fillsView(long w, long h, int camIndex) {
        return false;
    }

    public boolean coordInside(int jpx, int jpy, int camIndex, long cvx, long cvy) {
        boolean res = false;
        switch (text_anchor) {
            case VText.TEXT_ANCHOR_START: {
                if ((cvx >= vx) && (cvy >= vy) && (cvx <= (vx + pc[camIndex].cw)) && (cvy <= (vy + pc[camIndex].ch))) {
                    res = true;
                }
                break;
            }
            case VText.TEXT_ANCHOR_MIDDLE: {
                if ((cvx >= vx - pc[camIndex].cw / 2) && (cvy >= vy) && (cvx <= (vx + pc[camIndex].cw / 2)) && (cvy <= (vy + pc[camIndex].ch))) {
                    res = true;
                }
                break;
            }
            default: {
                if ((cvx <= vx) && (cvy >= vy) && (cvx >= (vx - pc[camIndex].cw)) && (cvy <= (vy + pc[camIndex].ch))) {
                    res = true;
                }
            }
        }
        return res;
    }

    public boolean visibleInDisc(long dvx, long dvy, long dvr, Shape dvs, int camIndex, int jpx, int jpy, int dpr) {
        if (text_anchor == TEXT_ANCHOR_START) {
            return dvs.intersects(vx, vy, pc[camIndex].cw, pc[camIndex].ch);
        } else if (text_anchor == TEXT_ANCHOR_MIDDLE) {
            return dvs.intersects(vx - pc[camIndex].cw / 2, vy, pc[camIndex].cw, pc[camIndex].ch);
        } else {
            //TEXT_ANCHOR_END
            return dvs.intersects(vx - pc[camIndex].cw, vy, pc[camIndex].cw, pc[camIndex].ch);
        }
    }

    public short mouseInOut(int jpx, int jpy, int camIndex, long cvx, long cvy) {
        if (coordInside(jpx, jpy, camIndex, cvx, cvy)) {
            //if the mouse is inside the glyph
            if (!pc[camIndex].prevMouseIn) {
                //if it was not inside it last time, mouse has entered the glyph
                pc[camIndex].prevMouseIn = true;
                return Glyph.ENTERED_GLYPH;
            } //if it was inside last time, nothing has changed
            else {
                return Glyph.NO_CURSOR_EVENT;
            }
        } else {
            //if the mouse is not inside the glyph
            if (pc[camIndex].prevMouseIn) {
                //if it was inside it last time, mouse has exited the glyph
                pc[camIndex].prevMouseIn = false;
                return Glyph.EXITED_GLYPH;
            }//if it was not inside last time, nothing has changed
            else {
                return Glyph.NO_CURSOR_EVENT;
            }
        }
    }

    public void project(Camera c, Dimension d) {
        int i = c.getIndex();
        coef = (float) (c.focal / (c.focal + c.altitude));
        //find coordinates of object's geom center wrt to camera center and project
        //translate in JPanel coords
        pc[i].cx = (d.width / 2) + Math.round((vx - c.posx) * coef);
        pc[i].cy = (d.height / 2) - Math.round((vy - c.posy) * coef);
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy) {
        int i = c.getIndex();
        coef = (float) (c.focal / (c.focal + c.altitude)) * lensMag;
        //find coordinates of object's geom center wrt to camera center and project
        //translate in JPanel coords
        pc[i].lcx = lensWidth / 2 + Math.round((vx - lensx) * coef);
        pc[i].lcy = lensHeight / 2 - Math.round((vy - lensy) * coef);
    }

    public void draw(Graphics2D g, int vW, int vH, int i, Stroke stdS, AffineTransform stdT, int dx, int dy) {
        if (!pc[i].valid) {
            g.setFont((font != null) ? font : getMainFont());
            Rectangle2D bounds = g.getFontMetrics().getStringBounds(text, g);
            // cw and ch actually hold width and height of text *in virtual space*
            pc[i].cw = (int) Math.round(bounds.getWidth() * scaleFactor);
            pc[i].ch = (int) Math.round(bounds.getHeight() * scaleFactor);
            pc[i].valid = true;
        }
        if (alphaC != null && alphaC.getAlpha() == 0) {
            return;
        }
        float trueCoef = scaleFactor * coef;
        g.setColor(this.color);
        if (trueCoef * fontSize > VirtualSpaceManager.INSTANCE.getTextDisplayedAsSegCoef() || !zoomSensitive) {
            //if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
            g.setFont((font != null) ? font : getMainFont());
            AffineTransform at;
            if (text_anchor == TEXT_ANCHOR_START) {
                at = AffineTransform.getTranslateInstance(dx + pc[i].cx, dy + pc[i].cy);
            } else if (text_anchor == TEXT_ANCHOR_MIDDLE) {
                at = AffineTransform.getTranslateInstance(dx + pc[i].cx - pc[i].cw * coef / 2.0f, dy + pc[i].cy);
            } else {
                at = AffineTransform.getTranslateInstance(dx + pc[i].cx - pc[i].cw * coef, dy + pc[i].cy);
            }
            if (zoomSensitive) {
                at.concatenate(AffineTransform.getScaleInstance(trueCoef, trueCoef));
            }
            g.setTransform(at);
            if (alphaC != null) {
                g.setComposite(alphaC);
                drawTheString(g);
                g.setComposite(acO);
            } else {
                drawTheString(g);
            }
            g.setTransform(stdT);
        } else {
            if (alphaC != null) {
                g.setComposite(alphaC);
                g.fillRect(dx + pc[i].cx, dy + pc[i].cy, 1, 1);
                g.setComposite(acO);
            } else {
                g.fillRect(dx + pc[i].cx, dy + pc[i].cy, 1, 1);
            }
        }
    }

    /* Begin edited code*/
    private void drawTheString(Graphics2D g) {
        String[] words = text.split(" ");
        textContainerWidth = 0;
        FontMetrics fm = g.getFontMetrics();
        if (text.length() > Configuration.MAX_CHARS_PER_LINE) {
            int startIndex = 0, endIndex = 0, rows = 0,
            	numOfChars = 0;
            long highestLineLength = 0;
            String lineToWrite;
            while (true) {
            	if ( endIndex >= words.length-1 )
            	{
            		lineToWrite = buildString(words, startIndex, words.length);
            		g.drawString(lineToWrite, 0f, fm.getHeight() * rows);

                    if ((long)fm.getStringBounds(lineToWrite, g).getWidth() > highestLineLength) {
                    	highestLineLength = textContainerWidth 
                    		= (long)fm.getStringBounds(lineToWrite, g).getWidth();
                    }
            		rows++;
            		break;
            	}
            	else if ( numOfChars + words[endIndex].length() 
            			< Configuration.MAX_CHARS_PER_LINE && !words[endIndex].contains("\n")) {
            		numOfChars += words[endIndex].length();
            	}
            	else
            	{
            		lineToWrite = buildString(words, startIndex, endIndex+1);
            		g.drawString(lineToWrite, 0f, fm.getHeight() * rows);
            		numOfChars = 0;
            		startIndex = endIndex+1;
            		endIndex++;
            		rows++;
                    if ((long)fm.getStringBounds(lineToWrite, g).getWidth() > highestLineLength) {
                    	highestLineLength = textContainerWidth 
                			= (long)fm.getStringBounds(lineToWrite, g).getWidth();
                    }
            	}
        		endIndex++;
            }
            textContainerHeight = fm.getHeight() * rows;
        } else {
            textContainerWidth = (long)fm.getStringBounds(text, g).getWidth();
            textContainerHeight = (long)fm.getStringBounds(text, g).getHeight();
    		g.drawString(text, 0.0f, 0.0f);
        }
    }

    private String buildString(String[] strArray, int start, int end) {
        String str = "";
        for (int i = start; i < end; i++) {
            str += strArray[i] + " ";
        }
        return str;
    }

    /* End edited code */
    public void drawForLens(Graphics2D g, int vW, int vH, int i, Stroke stdS, AffineTransform stdT, int dx, int dy) {
        if (!pc[i].lvalid) {
            g.setFont((font != null) ? font : getMainFont());
            Rectangle2D bounds = g.getFontMetrics().getStringBounds(text, g);
            // lcw and lch actually hold width and height of text *in virtual space*
            pc[i].lcw = (int) Math.round(bounds.getWidth() * scaleFactor);
            pc[i].lch = (int) Math.round(bounds.getHeight() * scaleFactor);
            pc[i].lvalid = true;
        }
        if (alphaC != null && alphaC.getAlpha() == 0) {
            return;
        }
        float trueCoef = scaleFactor * coef;
        g.setColor(this.color);
        if (trueCoef * fontSize > VirtualSpaceManager.INSTANCE.getTextDisplayedAsSegCoef() || !zoomSensitive) {
            g.setFont((font != null) ? font : getMainFont());
            //if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
            AffineTransform at;
            if (text_anchor == TEXT_ANCHOR_START) {
                at = AffineTransform.getTranslateInstance(dx + pc[i].lcx, dy + pc[i].lcy);
            } else if (text_anchor == TEXT_ANCHOR_MIDDLE) {
                at = AffineTransform.getTranslateInstance(dx + pc[i].lcx - pc[i].lcw * coef / 2.0f, dy + pc[i].lcy);
            } else {
                at = AffineTransform.getTranslateInstance(dx + pc[i].lcx - pc[i].lcw * coef, dy + pc[i].lcy);
            }
            if (zoomSensitive) {
                at.concatenate(AffineTransform.getScaleInstance(trueCoef, trueCoef));
            }
            g.setTransform(at);
            if (alphaC != null) {
                g.setComposite(alphaC);
                g.drawString(text, 0.0f, 0.0f);
                g.setComposite(acO);
            } else {
                g.drawString(text, 0.0f, 0.0f);
            }
            g.setTransform(stdT);
        } else {
            if (alphaC != null) {
                g.setComposite(alphaC);
                g.fillRect(dx + pc[i].lcx, dy + pc[i].lcy, 1, 1);
                g.setComposite(acO);
            } else {
                g.fillRect(dx + pc[i].lcx, dy + pc[i].lcy, 1, 1);
            }
        }
    }

    /** Set text that should be painted. */
    public void setText(String t) {
        text = t;
        VirtualSpaceManager.INSTANCE.repaintNow();
        invalidate();
    }

    /** Set the scale factor for this text.
     * The actual size of the text will be that defined by the font size multiplied by this scale factor.
     *@param s scale factor
     */
    public void setScale(float s) {
        scaleFactor = s;
        invalidate();
    }

    /** Get the scale factor for this text.
     * The actual size of the text is that defined by the font size multiplied by the scale factor returned by this method.
     */
    public float getScale() {
        return scaleFactor;
    }

    /** Force computation of text's bounding box at next call to draw().
     *@see #validBounds(int i)
     *@see #getBounds(int i)
     */
    public void invalidate() {
        try {
            for (int i = 0; i < pc.length; i++) {
                pc[i].valid = false;
                pc[i].lvalid = false;
            }
        } catch (NullPointerException ex) {
        }
    }

    /** Get the width and height of the bounding box in virtual space.
     *@param i index of camera (Camera.getIndex())
     *@see #validBounds(int i)
     *@see #invalidate()
     *@return the width and height of the text's bounding box, as a LongPoint
     */
    public LongPoint getBounds(int i) {
        return new LongPoint(pc[i].cw, pc[i].ch);
    }

    /** Indicates whether the bounds of the text are valid at this time or not.
     * The bounds can be invalid if the thread in charge of painting has not dealt with this glyph since invalidate() was last called on it.
     * It is advisable to test this before calling getBounds(int i)
     *@see #getBounds(int i)
     *@see #invalidate()
     */
    public boolean validBounds(int i) {
        return pc[i].valid;
    }

    /** Change the Font used to display this specific text object.
     *@param f set to null to use the default font
     */
    public void setSpecialFont(Font f) {
        if (f != null) {
            font = f;
            fontSize = font.getSize2D();
        } else {
            font = null;
            fontSize = getMainFont().getSize2D();
        }
        VirtualSpaceManager.INSTANCE.repaintNow();
        invalidate();
    }

    /** Get the Font used to display this specific text object.
     *@return the main ZVTM font if no specific Font is used in this object
     */
    public Font getFont() {
        if (font != null) {
            return font;
        } else {
            return getMainFont();
        }
    }

    /** Indicates whether this glyph is using a special font or not.
     * Using a special font does not necessarily mean that this font is different from the default font.
     */
    public boolean usesSpecialFont() {
        if (font == null) {
            return false;
        } else {
            return true;
        }
    }

    /** Get text painted by this glyph. */
    public String getText() {
        return text;
    }

    /** Set the text anchor
     *@param ta one of TEXT_ANCHOR_START, TEXT_ANCHOR_MIDDLE, TEXT_ANCHOR_END
     */
    public void setTextAnchor(short ta) {
        text_anchor = ta;
    }

    /** Get text anchor.
     *@return one of TEXT_ANCHOR_START, TEXT_ANCHOR_MIDDLE, TEXT_ANCHOR_END
     */
    public short getTextAnchor() {
        return text_anchor;
    }

    public Object clone() {
        VText res = new VText(vx, vy, 0, color, (new StringBuffer(text)).toString(), text_anchor, getScale(), (alphaC != null) ? alphaC.getAlpha() : 1.0f);
        res.cursorInsideColor = this.cursorInsideColor;
        return res;
    }

    /** Highlight this glyph to give visual feedback when the cursor is inside it. */
    public void highlight(boolean b, Color selectedColor) {
        boolean update = false;
        if (b) {
            if (cursorInsideColor != null) {
                color = cursorInsideColor;
                update = true;
            }
        } else {
            if (isSelected() && selectedColor != null) {
                color = selectedColor;
                update = true;
            } else {
                if (cursorInsideColor != null) {
                    color = fColor;
                    update = true;
                }
            }
        }
        if (update) {
            VirtualSpaceManager.INSTANCE.repaintNow();
        }
    }
}
