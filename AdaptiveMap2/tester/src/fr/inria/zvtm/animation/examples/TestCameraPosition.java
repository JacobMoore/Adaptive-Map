/*   DATE OF CREATION:   Jul 11 2000
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
 * $Id: TestCameraPosition.java 3149 2010-04-19 09:27:12Z rprimet $ 
 */

package fr.inria.zvtm.animation.examples;

import java.awt.*;
import javax.swing.*;

import java.util.Random;
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
import fr.inria.zvtm.animation.interpolation.*;
import org.jdesktop.animation.timing.interpolation.*;

// Click handler that animates the Camera to a new position.
// Left-click in the view to move the camera.
public class TestCameraPosition {

    VirtualSpaceManager vsm;
    VirtualSpace vs;
    ViewEventHandler eh;   //class that receives the events sent from views (include mouse click, entering object,...)
    AnimationManager am;

    View testView;

    TestCameraPosition(){
        vsm=VirtualSpaceManager.INSTANCE;
	am = vsm.getAnimationManager();
        vsm.setDebug(true);
    }

    public void startAnim(short ogl){
        eh=new TestCameraPosition.MyEventHandler(this);
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

	final int NB_GLYPHS = 100;
	java.util.List<Glyph> circles = new java.util.ArrayList<Glyph>();

	Random rnd = new Random();

	for(int i=0; i<NB_GLYPHS;++i){
	    final Glyph circle = new VCircle((60+3)*i,30*i,0,30,Color.getHSBColor(rnd.nextFloat(),
										  rnd.nextFloat(),
										  rnd.nextFloat()));
	    circles.add(circle);
	    vs.addGlyph(circle);

	    Animation anim = am.getAnimationFactory().createAnimation(3000, 
						Animation.INFINITE,
						Animation.RepeatBehavior.REVERSE,
						circle,
						Animation.Dimension.POSITION,
						new DefaultTimingHandler(){
						    final long initX = circle.vx;
						    final long initY = circle.vy;

						    public void timingEvent(float fraction, 
									    Object subject, Animation.Dimension dim){
							Glyph g = (Glyph)subject;
							
							g.moveTo(initX,
								 Float.valueOf((1-fraction)*initY).longValue());
						    }
						},
						new SplineInterpolator(0.1f,0.95f,0.2f,0.95f));
	    anim.setStartFraction(rnd.nextFloat());
	    am.startAnimation(anim, false);
	}

	vsm.getVirtualSpace("src").getCamera(0).setLocation(testView.getGlobalView(vsm.getVirtualSpace("src").getCamera(0)));
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
	System.out.println("Left-click in the view to move the camera");
        new TestCameraPosition().startAnim((args.length > 0) ? Short.parseShort(args[0]) : 0);
    }

    class MyEventHandler implements ViewEventHandler{
	TestCameraPosition application;

	long lastX,lastY;    //remember last mouse coords to compute translation  (dragging)

	MyEventHandler(TestCameraPosition appli){
	    application=appli;
	}

	long x1,x2,y1,y2;

	public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){

	}

	public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){

	}

	public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	    //Animate from the current position to the target position 
	    //over the course of a second
	    final long sx = vsm.getVirtualSpace("src").getCamera(0).posx;
	    final long sy = vsm.getVirtualSpace("src").getCamera(0).posy;
	    final long ex = v.getVCursor().vx;
	    final long ey = v.getVCursor().vy;

	    Animation trans = am.getAnimationFactory().createAnimation(1000, 1f, Animation.RepeatBehavior.LOOP,
						 vsm.getVirtualSpace("src").getCamera(0),
						 Animation.Dimension.POSITION,
						  new DefaultTimingHandler(){
						      final long startX = sx;
						      final long startY = sy;
						      final long endX = ex;
						      final long endY = ey;

						      public void timingEvent(float fraction, 
									      Object subject, Animation.Dimension dim){
							  Camera c = (Camera)subject;
							  
							  c.moveTo((long)(fraction*(endX-startX))+startX,
								   (long)(fraction*(endY-startY))+startY);
						      }},
						 SlowInSlowOutInterpolator.getInstance());

	    Animation altitude = am.getAnimationFactory().createAnimation(1000,
						    vsm.getVirtualSpace("src").getCamera(0),
						    Animation.Dimension.ALTITUDE,
						    new DefaultTimingHandler(){
							final float initZ = vsm.getVirtualSpace("src").getCamera(0).getAltitude();
							public void timingEvent(float fraction, 
										Object subject, Animation.Dimension dim){
							    Camera c = (Camera)subject;
							    
							    c.setAltitude((float)(initZ + 0.25*initZ*Math.sin(Math.PI * fraction)));
							}

							@Override public void end(Object subject, 
                                Animation.Dimension dim){
							    Camera c = (Camera)subject;
							    c.setAltitude(initZ);
							}
						    }
						    );
	    am.startAnimation(trans, true); //force anim start
	    //XXX I think this makes a case that we should be able to request 
	    //bypassed animations to execute their end action when forcing 
	    //a new animation to start
	    am.startAnimation(altitude, true);
	}

	public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){

	}

	public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	}

	public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	}

	public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
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
	}

	public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

	public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){

	}

	public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	    
	}

	public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	    Camera c=application.vsm.getActiveCamera();
	    float a=(c.focal+Math.abs(c.altitude))/c.focal;
	    if (wheelDirection == WHEEL_UP){
		c.altitudeOffset(-a*5);
		application.vsm.repaintNow();
	    }
	    else {
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
