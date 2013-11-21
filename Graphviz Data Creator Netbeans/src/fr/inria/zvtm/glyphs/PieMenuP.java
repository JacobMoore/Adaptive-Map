/*   FILE: PieMenuP.java
 *   DATE OF CREATION:  Thu Aug 25 14:14:50 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: PieMenuP.java 3443 2010-07-28 08:57:10Z epietrig $
 */ 

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Font;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.Utilities;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VTextOr;

public class PieMenuP extends PieMenu {

    /**Pie Menu constructor - should not be used directly
        *@param stringLabels text label of each menu item
        *@param menuCenterCoordinates (mouse cursor's coordinates in virtual space as a LongPoint)
        *@param vsName name of the virtual space in which to create the pie menu
        *@param vsm instance of VirtualSpaceManager
        *@param radius radius of pie menu
        *@param startAngle first menu item will have an offset of startAngle interpreted relative to the X horizontal axis (counter clockwise)
        *@param fillColor menu items' fill color
        *@param borderColor menu items' border color
        *@param fillSColor menu items' fill color, when selected<br>can be null if color should not change
        *@param borderSColor menu items' border color, when selected<br>can be null if color should not change
        *@param labelColor menu labels' color
        *@param alphaT menu items' translucency value: between 0 (transparent) and 1.0 (opaque)
        *@param sensitRadius sensitivity radius (as a percentage of the menu's actual radius)
        *@param font font used for menu labels
        */
    public PieMenuP(String[] stringLabels, LongPoint menuCenterCoordinates, 
                    String vsName, VirtualSpaceManager vsm,
                    long radius, double startAngle,
                    Color fillColor, Color borderColor, Color fillSColor, Color borderSColor, Color labelColor, float alphaT,
                    double sensitRadius, Font font){
        this.vs = vsm.getVirtualSpace(vsName);
        long vx = menuCenterCoordinates.x;
        long vy = menuCenterCoordinates.y;
        LongPoint[] coords;
        items = new VPolygon[stringLabels.length];
        labels = new VTextOr[stringLabels.length];
        double angle = startAngle;
        double angleDelta = 2 * Math.PI/((double)stringLabels.length);
        long pieMenuRadius = radius;
        double textAngle;
        for (int i=0;i<labels.length;i++){
            coords = new LongPoint[3];
            coords[0] = new LongPoint(vx, vy);
            coords[1] = new LongPoint(vx+Math.round(Math.cos(angle)*pieMenuRadius), vy+Math.round(Math.sin(angle)*pieMenuRadius));
            angle += angleDelta;
            coords[2] = new LongPoint(vx+Math.round(Math.cos(angle)*pieMenuRadius), vy+Math.round(Math.sin(angle)*pieMenuRadius));
            items[i] = new VPolygon(coords, 0, fillColor, borderColor, alphaT);
            items[i].setCursorInsideFillColor(fillSColor);
            items[i].setCursorInsideHighlightColor(borderSColor);
            vs.addGlyph(items[i], false, false);
            if (stringLabels[i] != null && stringLabels[i].length() > 0){
                if (orientText){
                    textAngle = angle - angleDelta / ((float)2);
                    if (angle > Utilities.HALF_PI){
                        if (angle > Math.PI){
                            if (angle < Utilities.THREE_HALF_PI){textAngle -= Math.PI;}
                        }
                        else {textAngle +=Math.PI;}
                    }
                    labels[i] = new VTextOr(Math.round(vx+Math.cos(angle-angleDelta/2.0)*pieMenuRadius/2),
                        Math.round(vy+Math.sin(angle-angleDelta/2.0)*pieMenuRadius/2),
                        0, labelColor, stringLabels[i], (float)textAngle, VText.TEXT_ANCHOR_MIDDLE);
                }
                else {
                    textAngle = angle - angleDelta / ((float)2);
                    if (angle > Utilities.HALF_PI){
                        if (angle > Math.PI){
                            if (angle < Utilities.THREE_HALF_PI){textAngle -= Math.PI;}
                        }
                        else {textAngle +=Math.PI;}
                    }
                    labels[i] = new VTextOr(Math.round(vx+Math.cos(angle-angleDelta/2.0)*pieMenuRadius/2),
                        Math.round(vy+Math.sin(angle-angleDelta/2.0)*pieMenuRadius/2),
                        0, labelColor, stringLabels[i], 0, VText.TEXT_ANCHOR_MIDDLE);
                }
                labels[i].setBorderColor(borderColor);
                labels[i].setSpecialFont(font);
                labels[i].setSensitivity(false);
                vs.addGlyph(labels[i]);
            }
        }
        boundary = new VCircle(vx, vy, 0, Math.round(pieMenuRadius*sensitRadius), Color.white);
        boundary.setVisible(false);
        vs.addGlyph(boundary);
        vs.atBottom(boundary);
    }

    /**Pie Menu constructor - should not be used directly
        *@param stringLabels text label of each menu item
        *@param menuCenterCoordinates (mouse cursor's coordinates in virtual space as a LongPoint)
        *@param vsName name of the virtual space in which to create the pie menu
        *@param vsm instance of VirtualSpaceManager
        *@param radius radius of pie menu
        *@param startAngle first menu item will have an offset of startAngle interpreted relative to the X horizontal axis (counter clockwise)
        *@param fillColors menu items' fill colors (this array should have the same length as the stringLabels array)
        *@param borderColors menu items' border colors (this array should have the same length as the stringLabels array)
        *@param labelColors menu labels' colors (this array should have the same length as the stringLabels array)
        *@param fillSColors menu items' fill colors, when selected (this array should have the same length as the stringLabels array)<br>elements can be null if color should not change
        *@param borderSColors menu items' border colors, when selected (this array should have the same length as the stringLabels array)<br>elements can be null if color should not change
        *@param alphaT menu items' translucency value: between 0 (transparent) and 1.0 (opaque)
        *@param sensitRadius sensitivity radius (as a percentage of the menu's actual radius)
        *@param font font used for menu labels
        */
    public PieMenuP(String[] stringLabels, LongPoint menuCenterCoordinates, 
                    String vsName, VirtualSpaceManager vsm,
                    long radius, double startAngle,
                    Color[] fillColors, Color[] borderColors, Color[] fillSColors, Color[] borderSColors, Color[] labelColors, float alphaT,
                    double sensitRadius, Font font){
        this.vs = vsm.getVirtualSpace(vsName);
        long vx = menuCenterCoordinates.x;
        long vy = menuCenterCoordinates.y;
        LongPoint[] coords;
        items = new VPolygon[stringLabels.length];
        labels = new VTextOr[stringLabels.length];
        double angle = startAngle;
        double angleDelta = 2 * Math.PI/((double)stringLabels.length);
        long pieMenuRadius = radius;
        double textAngle;
        for (int i=0;i<labels.length;i++){
            coords = new LongPoint[3];
            coords[0] = new LongPoint(vx, vy);
            coords[1] = new LongPoint(vx+Math.round(Math.cos(angle)*pieMenuRadius), vy+Math.round(Math.sin(angle)*pieMenuRadius));
            angle += angleDelta;
            coords[2] = new LongPoint(vx+Math.round(Math.cos(angle)*pieMenuRadius), vy+Math.round(Math.sin(angle)*pieMenuRadius));
            items[i] = new VPolygon(coords, 0, fillColors[i], borderColors[i], alphaT);
            items[i].setCursorInsideFillColor(fillSColors[i]);
            items[i].setCursorInsideHighlightColor(borderSColors[i]);
            vs.addGlyph(items[i], false, false);
            if (stringLabels[i] != null && stringLabels[i].length() > 0){
                if (orientText){
                    textAngle = angle - angleDelta / ((float)2);
                    if (angle > Utilities.HALF_PI){
                        if (angle > Math.PI){
                            if (angle < Utilities.THREE_HALF_PI){textAngle -= Math.PI;}
                        }
                        else {textAngle +=Math.PI;}
                    }
                    labels[i] = new VTextOr(Math.round(vx+Math.cos(angle-angleDelta/2.0)*pieMenuRadius/2),
                        Math.round(vy+Math.sin(angle-angleDelta/2.0)*pieMenuRadius/2),
                        0, labelColors[i], stringLabels[i], (float)textAngle, VText.TEXT_ANCHOR_MIDDLE);
                }
                else {
                    labels[i] = new VTextOr(Math.round(vx+Math.cos(angle-angleDelta/2.0)*pieMenuRadius/2),
                        Math.round(vy+Math.sin(angle-angleDelta/2.0)*pieMenuRadius/2),
                        0, labelColors[i], stringLabels[i], 0, VText.TEXT_ANCHOR_MIDDLE);
                }
                labels[i].setBorderColor(borderColors[i]);
                labels[i].setSpecialFont(font);
                labels[i].setSensitivity(false);
                vs.addGlyph(labels[i]);
            }
        }
        boundary = new VCircle(vx, vy, 0, Math.round(pieMenuRadius*sensitRadius), Color.white);
        boundary.setVisible(false);
        vs.addGlyph(boundary);
        vs.atBottom(boundary);
    }

    /**Pie Menu constructor - should not be used directly
        *@param stringLabels text label of each menu item
        *@param menuCenterCoordinates (mouse cursor's coordinates in virtual space as a LongPoint)
        *@param vsName name of the virtual space in which to create the pie menu
        *@param vsm instance of VirtualSpaceManager
        *@param radius radius of pie menu
        *@param startAngle first menu item will have an offset of startAngle interpreted relative to the X horizontal axis (counter clockwise)
        *@param fillColor menu items' fill color
        *@param borderColor menu items' border color
        *@param labelColor menu labels' color
        *@param fillSColor menu items' fill color, when selected<br>can be null if color should not change
        *@param borderSColor menu items' border color, when selected<br>can be null if color should not change
        *@param alphaT menu items' translucency value: between 0 (transparent) and 1.0 (opaque)
        *@param sensitRadius sensitivity radius (as a percentage of the menu's actual radius)
        *@param font font used for menu labels
        *@param labelOffsets x,y offset of each menu label w.r.t their default posisition, in virtual space units<br>(this array should have the same length as the labels array)
        */
    public PieMenuP(String[] stringLabels, LongPoint menuCenterCoordinates, 
                    String vsName, VirtualSpaceManager vsm,
                    long radius, double startAngle,
                    Color fillColor, Color borderColor, Color fillSColor, Color borderSColor, Color labelColor, float alphaT,
                    double sensitRadius, Font font, LongPoint[] labelOffsets){
        this.vs = vsm.getVirtualSpace(vsName);
        long vx = menuCenterCoordinates.x;
        long vy = menuCenterCoordinates.y;
        LongPoint[] coords;
        items = new VPolygon[stringLabels.length];
        labels = new VTextOr[stringLabels.length];
        double angle = startAngle;
        double angleDelta = 2 * Math.PI/((double)stringLabels.length);
        long pieMenuRadius = radius;
        double textAngle;
        for (int i=0;i<labels.length;i++){
            coords = new LongPoint[3];
            coords[0] = new LongPoint(vx, vy);
            coords[1] = new LongPoint(vx+Math.round(Math.cos(angle)*pieMenuRadius), vy+Math.round(Math.sin(angle)*pieMenuRadius));
            angle += angleDelta;
            coords[2] = new LongPoint(vx+Math.round(Math.cos(angle)*pieMenuRadius), vy+Math.round(Math.sin(angle)*pieMenuRadius));
            items[i] = new VPolygon(coords, 0, fillColor, borderColor, alphaT);
            items[i].setCursorInsideFillColor(fillSColor);
            items[i].setCursorInsideHighlightColor(borderSColor);
            vs.addGlyph(items[i], false, false);
            if (stringLabels[i] != null && stringLabels[i].length() > 0){
                if (orientText){
                    textAngle = angle - angleDelta / ((float)2);
                    if (angle > Utilities.HALF_PI){
                        if (angle > Math.PI){
                            if (angle < Utilities.THREE_HALF_PI){textAngle -= Math.PI;}
                        }
                        else {textAngle +=Math.PI;}
                    }
                    labels[i] = new VTextOr(Math.round(vx+Math.cos(angle-angleDelta/2.0)*pieMenuRadius/2 + labelOffsets[i].x),
                        Math.round(vy+Math.sin(angle-angleDelta/2.0)*pieMenuRadius/2 + labelOffsets[i].y),
                        0, labelColor, stringLabels[i], (float)textAngle, VText.TEXT_ANCHOR_MIDDLE);
                }
                else {
                    textAngle = angle - angleDelta / ((float)2);
                    if (angle > Utilities.HALF_PI){
                        if (angle > Math.PI){
                            if (angle < Utilities.THREE_HALF_PI){textAngle -= Math.PI;}
                        }
                        else {textAngle +=Math.PI;}
                    }
                    labels[i] = new VTextOr(Math.round(vx+Math.cos(angle-angleDelta/2.0)*pieMenuRadius/2 + labelOffsets[i].x),
                        Math.round(vy+Math.sin(angle-angleDelta/2.0)*pieMenuRadius/2 + labelOffsets[i].y),
                        0, labelColor, stringLabels[i], 0, VText.TEXT_ANCHOR_MIDDLE);
                }
                labels[i].setBorderColor(borderColor);
                labels[i].setSpecialFont(font);
                labels[i].setSensitivity(false);
                vs.addGlyph(labels[i]);
            }
        }
        boundary = new VCircle(vx, vy, 0, Math.round(pieMenuRadius*sensitRadius), Color.white);
        boundary.setVisible(false);
        vs.addGlyph(boundary);
        vs.atBottom(boundary);
    }

    /**Pie Menu constructor - should not be used directly
        *@param stringLabels text label of each menu item
        *@param menuCenterCoordinates (mouse cursor's coordinates in virtual space as a LongPoint)
        *@param vsName name of the virtual space in which to create the pie menu
        *@param vsm instance of VirtualSpaceManager
        *@param radius radius of pie menu
        *@param startAngle first menu item will have an offset of startAngle interpreted relative to the X horizontal axis (counter clockwise)
        *@param fillColors menu items' fill colors (this array should have the same length as the stringLabels array)
        *@param borderColors menu items' border colors (this array should have the same length as the stringLabels array)
        *@param labelColors menu labels' colors (this array should have the same length as the stringLabels array)
        *@param fillSColors menu items' fill colors, when selected (this array should have the same length as the stringLabels array)<br>elements can be null if color should not change
        *@param borderSColors menu items' border colors, when selected (this array should have the same length as the stringLabels array)<br>elements can be null if color should not change
        *@param alphaT menu items' translucency value: between 0 (transparent) and 1.0 (opaque)
        *@param sensitRadius sensitivity radius (as a percentage of the menu's actual radius)
        *@param font font used for menu labels
        *@param labelOffsets x,y offset of each menu label w.r.t their default posisition, in virtual space units<br>(this array should have the same length as the labels array)
        */
    public PieMenuP(String[] stringLabels, LongPoint menuCenterCoordinates, 
                    String vsName, VirtualSpaceManager vsm,
                    long radius, double startAngle,
                    Color[] fillColors, Color[] borderColors, Color[] fillSColors, Color[] borderSColors, Color[] labelColors, float alphaT,
                    double sensitRadius, Font font, LongPoint[] labelOffsets){
        this.vs = vsm.getVirtualSpace(vsName);
        long vx = menuCenterCoordinates.x;
        long vy = menuCenterCoordinates.y;
        LongPoint[] coords;
        items = new VPolygon[stringLabels.length];
        labels = new VTextOr[stringLabels.length];
        double angle = startAngle;
        double angleDelta = 2 * Math.PI/((double)stringLabels.length);
        long pieMenuRadius = radius;
        double textAngle;
        for (int i=0;i<labels.length;i++){
            coords = new LongPoint[3];
            coords[0] = new LongPoint(vx, vy);
            coords[1] = new LongPoint(vx+Math.round(Math.cos(angle)*pieMenuRadius), vy+Math.round(Math.sin(angle)*pieMenuRadius));
            angle += angleDelta;
            coords[2] = new LongPoint(vx+Math.round(Math.cos(angle)*pieMenuRadius), vy+Math.round(Math.sin(angle)*pieMenuRadius));
            items[i] = new VPolygon(coords, 0, fillColors[i], borderColors[i], alphaT);
            items[i].setCursorInsideFillColor(fillSColors[i]);
            items[i].setCursorInsideHighlightColor(borderSColors[i]);
            vs.addGlyph(items[i], false, false);
            if (stringLabels[i] != null && stringLabels[i].length() > 0){
                if (orientText){
                    textAngle = angle - angleDelta / ((float)2);
                    if (angle > Utilities.HALF_PI){
                        if (angle > Math.PI){
                            if (angle < Utilities.THREE_HALF_PI){textAngle -= Math.PI;}
                        }
                        else {textAngle +=Math.PI;}
                    }
                    labels[i] = new VTextOr(Math.round(vx+Math.cos(angle-angleDelta/2.0)*pieMenuRadius/2 + labelOffsets[i].x),
                        Math.round(vy+Math.sin(angle-angleDelta/2.0)*pieMenuRadius/2 + labelOffsets[i].y),
                        0, labelColors[i], stringLabels[i], (float)textAngle, VText.TEXT_ANCHOR_MIDDLE);
                }
                else {
                    labels[i] = new VTextOr(Math.round(vx+Math.cos(angle-angleDelta/2.0)*pieMenuRadius/2 + labelOffsets[i].x),
                        Math.round(vy+Math.sin(angle-angleDelta/2.0)*pieMenuRadius/2 + labelOffsets[i].y),
                        0, labelColors[i], stringLabels[i], 0, VText.TEXT_ANCHOR_MIDDLE);
                }
                labels[i].setBorderColor(borderColors[i]);
                labels[i].setSpecialFont(font);                
                labels[i].setSensitivity(false);
                vs.addGlyph(labels[i]);
            }
        }
        boundary = new VCircle(vx, vy, 0, Math.round(pieMenuRadius*sensitRadius), Color.white);
        boundary.setVisible(false);
        vs.addGlyph(boundary);
        vs.atBottom(boundary);
    }

}
