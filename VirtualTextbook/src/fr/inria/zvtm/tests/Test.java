/*   FILE: Test.java
 *   DATE OF CREATION:   Jul 11 2000
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
 *
 * $Id: Test.java 2957 2010-02-24 10:19:25Z epietrig $
 */

package fr.inria.zvtm.tests;

import java.awt.*;
import javax.swing.*;

import java.util.Vector;

import fr.inria.zvtm.engine.*;
import fr.inria.zvtm.glyphs.*;
import fr.inria.zvtm.engine.*;
import fr.inria.zvtm.animation.*;
import fr.inria.zvtm.animation.interpolation.*;
import fr.inria.zvtm.glyphs.*;
import fr.inria.zvtm.lens.*;
import fr.inria.zvtm.widgets.*;

public class Test {

    VirtualSpaceManager vsm;
    VirtualSpace vs;
    ViewEventHandler eh;   //class that receives the events sent from views (include mouse click, entering object,...)

    View testView;

    static int LENS_R1 = 100;
    static int LENS_R2 = 50;
    static final int LENS_ANIM_TIME = 300;
    static double MAG_FACTOR = 8.0;
    
    Test(short ogl){
        vsm=VirtualSpaceManager.INSTANCE;
        vsm.setDebug(true);
        //vsm.setDefaultMultiFills(true);
        initTest(ogl);
    }

    public void initTest(short ogl){
        eh=new EventHandlerTest(this);
        vs = vsm.addVirtualSpace("src");
        vs.addCamera();
        Vector cameras=new Vector();
        cameras.add(vsm.getVirtualSpace("src").getCamera(0));
        vsm.getVirtualSpace("src").getCamera(0).setZoomFloor(-90f);
        short vt = View.STD_VIEW;
        switch(ogl){
            case View.OPENGL_VIEW:{vt = View.OPENGL_VIEW;break;}
        }
        testView = vsm.addFrameView(cameras, "Test", vt, 800, 600, false, true, false, null);
        testView.setBackgroundColor(Color.LIGHT_GRAY);
        testView.setEventHandler(eh);
        testView.setNotifyMouseMoved(true);
        vsm.getVirtualSpace("src").getCamera(0).setAltitude(1000);
        
        vs.addGlyph(new CircleNR(0,0,0,100,Color.RED));
        vs.addGlyph(new VCircle(0,200,0,100,Color.RED));
        vsm.repaintNow();
    }
    
    FixedSizeLens lens;
    
    void setLens(int x, int y){
        lens = (FixedSizeLens)testView.setLens(getLensDefinition(x, y));
        lens.setBufferThreshold(1.5f);
        lens.setInnerRadiusColor(Color.RED);
        lens.setOuterRadiusColor(Color.RED);
    }

    Lens getLensDefinition(int x, int y){
        return new FSLinearLens(4.0f, LENS_R1, LENS_R2, x - 400, y - 300);
    }
    
    void moveLens(int x, int y){
        if (lens == null){return;}
        lens.setAbsolutePosition(x, y);
        vsm.repaintNow();
    }
    
    void incX(){
        lens.setXfocusOffset(lens.getXfocusOffset()-1);
        vsm.repaintNow();
    }

    void incY(){
        lens.setYfocusOffset(lens.getYfocusOffset()-1);
        vsm.repaintNow();
    }

    public static void main(String[] args){
//		try{UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");}
//		catch (Exception ex){ex.printStackTrace();}
//com.sun.java.swing.plaf.gtk.GTKLookAndFeel
//com.sun.java.swing.plaf.motif.MotifLookAndFeel
//com.sun.java.swing.plaf.windows.WindowsLookAndFeel
//javax.swing.plaf.metal.MetalLookAndFeel
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
        new Test((args.length > 0) ? Short.parseShort(args[0]) : 0);
    }
    
}
