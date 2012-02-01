/*   FILE: EventHandlerTest.java
 *   DATE OF CREATION:   Jul 28 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
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
 */

package fr.inria.zvtm.tests;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import fr.inria.zvtm.engine.ViewEventHandler;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.glyphs.Glyph;

public class EventHandlerTest implements ViewEventHandler{

    Test application;

    long lastX,lastY,lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)

    EventHandlerTest(Test appli){
        application=appli;
    }

    long x1,x2,y1,y2;

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        application.setLens(jpx, jpy);
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){

    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){

    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){

    }

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
    }

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
    }

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        //application.vsm.setSync(false);
        lastJPX=jpx;
        lastJPY=jpy;
        //application.vsm.animator.setActiveCam(v.cams[0]);
        v.setDrawDrag(true);
        application.vsm.activeView.mouse.setSensitivity(false);
        //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        application.vsm.getAnimationManager().setXspeed(0);
        application.vsm.getAnimationManager().setYspeed(0);
        application.vsm.getAnimationManager().setZspeed(0);
        v.setDrawDrag(false);
        application.vsm.activeView.mouse.setSensitivity(true);
        /*Camera c=v.cams[0];
        application.vsm.getAnimationManager().createCameraAnimation(500,2,new LongPoint(lastX-application.vsm.mouse.vx,lastY-application.vsm.mouse.vy),c.getID());*/
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
        //application.moveLens(jpx, jpy);
    }

    static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
            Camera c=application.vsm.getActiveCamera();
            float a=(c.focal+Math.abs(c.altitude))/c.focal;
            if (mod == META_SHIFT_MOD) {
                application.vsm.getAnimationManager().setXspeed(0);
                application.vsm.getAnimationManager().setYspeed(0);
                application.vsm.getAnimationManager().setZspeed(((lastJPY-jpy)*(ZOOM_SPEED_COEF)));
                //50 is just a speed factor (too fast otherwise)
            }
            else {
                application.vsm.getAnimationManager().setXspeed((c.altitude>0) ? (long)((jpx-lastJPX)*(a/PAN_SPEED_COEF)) : (long)((jpx-lastJPX)/(a*PAN_SPEED_COEF)));
                application.vsm.getAnimationManager().setYspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/PAN_SPEED_COEF)) : (long)((lastJPY-jpy)/(a*PAN_SPEED_COEF)));
                application.vsm.getAnimationManager().setZspeed(0);
            }
        }
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
        Camera c=application.vsm.getActiveCamera();
        float a=(c.focal+Math.abs(c.altitude))/c.focal;
        if (wheelDirection == WHEEL_UP){
            c.altitudeOffset(-a*5);
            application.vsm.repaintNow();
        }
        else {
            //wheelDirection == WHEEL_DOWN
            c.altitudeOffset(a*5);
            application.vsm.repaintNow();
        }
    }

    public void enterGlyph(Glyph g){
        g.highlight(true, null);
    }

    public void exitGlyph(Glyph g){
        g.highlight(false, null);
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}
    
    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (c == 'a'){application.incX();}
        else if (c == 's'){application.incY();}
        
    }
    
    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

}
