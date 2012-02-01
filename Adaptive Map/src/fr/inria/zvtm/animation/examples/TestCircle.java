/*   FILE: TestCircle.java
 *   DATE OF CREATION:   Jul 11 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Romain Primet
 *   Copyright (c) INRIA, 2009
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
 * $Id: TestCircle.java 2320 2009-09-08 10:07:29Z epietrig $ 
 */

package fr.inria.zvtm.animation.examples;

import java.awt.*;
import javax.swing.*;

import java.util.Vector;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import fr.inria.zvtm.engine.*;
import fr.inria.zvtm.glyphs.*;
import fr.inria.zvtm.engine.*;
import fr.inria.zvtm.glyphs.*;
import fr.inria.zvtm.widgets.*;

import fr.inria.zvtm.animation.*;
import org.jdesktop.animation.timing.*;
import org.jdesktop.animation.timing.interpolation.*;

public class TestCircle {

    VirtualSpaceManager vsm;
    VirtualSpace vs;
    ViewEventHandler eh;   //class that receives the events sent from views (include mouse click, entering object,...)

    View testView;

    TestCircle(short ogl){
        vsm=VirtualSpaceManager.INSTANCE;
        vsm.setDebug(true);
        initTest(ogl);
    }

    public void initTest(short ogl){
        eh=new TestCircle.EventHandlerTest(this);
        vs = vsm.addVirtualSpace("src");
        vs.addCamera();
        Vector cameras=new Vector();
        cameras.add(vsm.getVirtualSpace("src").getCamera(0));
        vsm.getVirtualSpace("src").getCamera(0).setZoomFloor(-90);
        short vt = View.STD_VIEW;
        switch(ogl){
	case View.OPENGL_VIEW:{vt = View.OPENGL_VIEW;break;}
        }
        testView = vsm.addFrameView(cameras, "Test", vt, 800, 600, false, true);
        testView.setBackgroundColor(Color.LIGHT_GRAY);
        testView.setEventHandler(eh);
        testView.setNotifyMouseMoved(true);
	final Glyph circle = new VCircle(100,0,0,40,Color.WHITE);
        vsm.getVirtualSpace("src").getCamera(0).setAltitude(50);
	vs.addGlyph(circle);
        vsm.repaintNow();

	AnimationManager am = vsm.getAnimationManager();

	for(int i=0; i<4; ++i){
	    Animation anim = 
		am.getAnimationFactory().createAnimation(3000, //milliseconds
							 1.0,
							 Animation.RepeatBehavior.LOOP,
							 circle,
							 Animation.Dimension.POSITION,
							 new TimingHandler(){
							     public void begin(Object subject, Animation.Dimension dim){}
							     public void end(Object subject, Animation.Dimension dim){}
							     public void repeat(Object subject, Animation.Dimension dim){}
							     public void timingEvent(float fraction, 
										     Object subject, Animation.Dimension dim){
								 Glyph g = (Glyph)subject;
								 g.moveTo(100 - Float.valueOf(400*fraction).longValue(), 0);
							     }
							 },
							 new SplineInterpolator(0.7f,0.1f,0.3f,0.9f));
	    am.startAnimation(anim, false);
	}

	Animation anim = 
	    am.getAnimationFactory().createAnimation(8000, 
						     1.0,
						     Animation.RepeatBehavior.LOOP,
						     circle,
						     Animation.Dimension.FILLCOLOR,
						     new TimingHandler(){
							 public void begin(Object subject, Animation.Dimension dim){}
							 public void end(Object subject, Animation.Dimension dim){}
							 public void repeat(Object subject, Animation.Dimension dim){}
							 public void timingEvent(float fraction, 
										 Object subject, Animation.Dimension dim){
							     Glyph g = (Glyph)subject;
							     g.setColor(new Color(0,
										  0,
										  Float.valueOf(255*fraction).intValue()));
							 }
						     });
	am.startAnimation(anim, false);
	 
	Animation animSize = 
	    am.getAnimationFactory().createAnimation(4000, 
						     1.0,
						     Animation.RepeatBehavior.LOOP,
						     circle,
						     Animation.Dimension.SIZE,
						     new TimingHandler(){
							 public void begin(Object subject, Animation.Dimension dim){}
							 public void end(Object subject, Animation.Dimension dim){}
							 public void repeat(Object subject, Animation.Dimension dim){}
							 public void timingEvent(float fraction, 
										 Object subject, Animation.Dimension dim){
							     Glyph g = (Glyph)subject;
							     g.sizeTo(40+60*fraction);
							 }
						     });
	am.startAnimation(animSize, false);
	 
    }
    
    public static void main(String[] args){
        System.out.println("-----------------");
        System.out.println("General information");
        System.out.println("JVM version: "+System.getProperty("java.vm.vendor")+" "+System.getProperty("java.vm.name")+" "+System.getProperty("java.vm.version"));
        System.out.println("OS type: "+System.getProperty("os.name")+" "+System.getProperty("os.version")+"/"+System.getProperty("os.arch")+" "+System.getProperty("sun.cpu.isalist"));
        System.out.println("-----------------");
        System.out.println("Directory information");
        System.out.println("Java Classpath: "+System.getProperty("java.class.path"));	
        System.out.println("Java directory: "+System.getProperty("java.home"));
        System.out.println("Launching from: "+System.getProperty("user.dir"));
        System.out.println("-----------------");
        System.out.println("User informations");
        System.out.println("User name: "+System.getProperty("user.name"));
        System.out.println("User home directory: "+System.getProperty("user.home"));
        System.out.println("-----------------");
        new TestCircle((args.length > 0) ? Short.parseShort(args[0]) : 0);
    }
    
    class EventHandlerTest implements ViewEventHandler{

	TestCircle application;

	long lastX,lastY,lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)

	EventHandlerTest(TestCircle appli){
	    application=appli;
	}

	long x1,x2,y1,y2;

	public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){

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

	}

	public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	    if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
		Camera c=application.vsm.getActiveCamera();
		float a=(c.focal+Math.abs(c.altitude))/c.focal;
		if (mod == META_SHIFT_MOD) {
		    application.vsm.getAnimationManager().setXspeed(0);
		    application.vsm.getAnimationManager().setYspeed(0);
		    application.vsm.getAnimationManager().setZspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50)));
		    //50 is just a speed factor (too fast otherwise)
		}
		else {
		    application.vsm.getAnimationManager().setXspeed((c.altitude>0) ? (long)((jpx-lastJPX)*(a/50.0f)) : (long)((jpx-lastJPX)/(a*50)));
		    application.vsm.getAnimationManager().setYspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50)));
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
    
	public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){}
    
	public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

	public void viewActivated(View v){}

	public void viewDeactivated(View v){}

	public void viewIconified(View v){}

	public void viewDeiconified(View v){}

	public void viewClosing(View v){System.exit(0);}

    }

}
