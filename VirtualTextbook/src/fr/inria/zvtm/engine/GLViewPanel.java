/*   FILE: GLViewPanel.java
 *   DATE OF CREATION:   Tue Oct 12 09:10:47 2004
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *   $Id: GLViewPanel.java 3347 2010-06-11 11:46:17Z epietrig $
 */ 

package fr.inria.zvtm.engine;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
import javax.swing.Timer;
import java.util.Vector;

import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.engine.ViewEventHandler;

/**
 * Each view runs in its own thread - uses OpenGL acceletation provided by J2SE 5.0<br>
 * The use of GLViewPanel requires the following Java property: -Dsun.java2d.opengl=true
 * @author Emmanuel Pietriga
 */

public class GLViewPanel extends ViewPanel {
    
    Dimension oldSize;
	Timer edtTimer;

    GLViewPanel(Vector cameras,View v, boolean arfome) {	
		ActionListener taskPerformer = new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				repaint();
			}
		};
		edtTimer = new Timer(frameTime, taskPerformer);

	addHierarchyListener(
	    new HierarchyListener() {
	       public void hierarchyChanged(HierarchyEvent e) {
		   if (isShowing()) {
		       start();
		   } else {
		       stop();
		   }
	       }
	   }
	);
	parent=v;
	//init of camera array
	cams=new Camera[cameras.size()];  //array of Camera
	evHs = new ViewEventHandler[cams.length];
	for (int nbcam=0;nbcam<cameras.size();nbcam++){
	    cams[nbcam]=(Camera)(cameras.get(nbcam));
	}
	//init other stuff
	setBackground(backColor);
	this.addMouseListener(this);
	this.addMouseMotionListener(this);
	this.addMouseWheelListener(this);
	this.addComponentListener(this);
	setAutoRequestFocusOnMouseEnter(arfome);
	setAWTCursor(Cursor.CUSTOM_CURSOR);  //custom cursor means VTM cursor
	this.size = this.getSize();
	if (VirtualSpaceManager.debugModeON()){System.out.println("View refresh time set to "+frameTime+"ms");}
	start();
    }

    public void start(){
	size = getSize();
	oldSize = size;
	edtTimer.start();
    }

    public synchronized void stop() {
	notify();
	edtTimer.stop();
    }

    public void paint(Graphics g) {
	super.paint(g);
	// stableRefToBackBufferGraphics is used here not as a Graphics from a back buffer image, but directly as the OpenGL graphics context
	// (simply reusing an already declared var instead of creating a new one for nothing)
	stableRefToBackBufferGraphics = (Graphics2D)g;
	try {
	    updateMouseOnly = false;
	    size = this.getSize();
	    if (size.width != oldSize.width || size.height != oldSize.height) {
		if (VirtualSpaceManager.debugModeON()){System.out.println("Resizing JPanel: ("+oldSize.width+"x"+oldSize.height+") -> ("+size.width+"x"+size.height+")");}
		oldSize=size;
		updateAntialias=true;
		updateFont=true;
	    }
	    if (updateFont){stableRefToBackBufferGraphics.setFont(VText.getMainFont());updateFont=false;}
	    if (updateAntialias){if (antialias){stableRefToBackBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);} else {stableRefToBackBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);}updateAntialias=false;}
	    standardStroke=stableRefToBackBufferGraphics.getStroke();
	    standardTransform=stableRefToBackBufferGraphics.getTransform();
	    if (notBlank){
		stableRefToBackBufferGraphics.setPaintMode();
		stableRefToBackBufferGraphics.setBackground(backColor);
		stableRefToBackBufferGraphics.clearRect(0,0,getWidth(),getHeight());
		backgroundHook();
		//begin actual drawing here
		for (int nbcam=0;nbcam<cams.length;nbcam++){
		    if ((cams[nbcam]!=null) && (cams[nbcam].enabled) && ((cams[nbcam].eager) || (cams[nbcam].shouldRepaint()))){
			camIndex=cams[nbcam].getIndex();
			drawnGlyphs=cams[nbcam].parentSpace.getDrawnGlyphs(camIndex);
			    drawnGlyphs.removeAllElements();
			    float uncoef=(float)((cams[nbcam].focal+cams[nbcam].altitude)/cams[nbcam].focal);
			    //compute region seen from this view through camera
				long viewW = size.width;
				long viewH = size.height;
			    long viewWC = (long)(cams[nbcam].posx-(viewW/2-visibilityPadding[0])*uncoef);
			    long viewNC = (long)(cams[nbcam].posy+(viewH/2-visibilityPadding[1])*uncoef);
			    long viewEC = (long)(cams[nbcam].posx+(viewW/2-visibilityPadding[2])*uncoef);
			    long viewSC = (long)(cams[nbcam].posy-(viewH/2-visibilityPadding[3])*uncoef);
			    gll = cams[nbcam].parentSpace.getDrawingList();
			    for (int i=0;i<gll.length;i++){
				if (gll[i].visibleInViewport(viewWC, viewNC, viewEC, viewSC, cams[nbcam])){
				    //if glyph is at least partially visible in the reg. seen from this view, display
					gll[i].project(cams[nbcam], size);
					if (gll[i].isVisible()){
					    gll[i].draw(stableRefToBackBufferGraphics,size.width,size.height,cams[nbcam].getIndex(),standardStroke,standardTransform, 0, 0);
					}
					// notifying outside if branch because glyph sensitivity is not
					// affected by glyph visibility when managed through Glyph.setVisible()
					cams[nbcam].parentSpace.drewGlyph(gll[i], camIndex);
				}
			    }
		    }
		}
		foregroundHook();
		afterLensHook();
		drawPortals();
		portalsHook();
		if (inside){//deal with mouse glyph only if mouse cursor is inside this window
			try {
			parent.mouse.unProject(cams[activeLayer],this); //we project the mouse cursor wrt the appropriate coord sys
			if (computeListAtEachRepaint && parent.mouse.isSensitive()){
			    parent.mouse.computeCursorOverList(evHs[activeLayer],cams[activeLayer]);
			}
		    }
		    catch (NullPointerException ex) {if (VirtualSpaceManager.debugModeON()){System.err.println("viewpanel.run.drawdrag "+ex);}}
		    stableRefToBackBufferGraphics.setColor(parent.mouse.hcolor);
		    if (drawDrag){stableRefToBackBufferGraphics.drawLine(origDragx,origDragy,parent.mouse.mx,parent.mouse.my);}
		    if (drawRect){stableRefToBackBufferGraphics.drawRect(Math.min(origDragx,parent.mouse.mx),Math.min(origDragy,parent.mouse.my),Math.max(origDragx,parent.mouse.mx)-Math.min(origDragx,parent.mouse.mx),Math.max(origDragy,parent.mouse.my)-Math.min(origDragy,parent.mouse.my));}
		    if (drawOval){
			if (circleOnly){
			    stableRefToBackBufferGraphics.drawOval(origDragx-Math.abs(origDragx-parent.mouse.mx),origDragy-Math.abs(origDragx-parent.mouse.mx),2*Math.abs(origDragx-parent.mouse.mx),2*Math.abs(origDragx-parent.mouse.mx));
			}
			else {
			    stableRefToBackBufferGraphics.drawOval(origDragx-Math.abs(origDragx-parent.mouse.mx),origDragy-Math.abs(origDragy-parent.mouse.my),2*Math.abs(origDragx-parent.mouse.mx),2*Math.abs(origDragy-parent.mouse.my));
			}
		    }
		    if (drawVTMcursor){
			    parent.mouse.draw(stableRefToBackBufferGraphics);
			    oldX=parent.mouse.mx;
			    oldY=parent.mouse.my;
		    }
		}
		//end drawing here
	    }
	    else {
		stableRefToBackBufferGraphics.setPaintMode();
		stableRefToBackBufferGraphics.setColor(blankColor);
		stableRefToBackBufferGraphics.fillRect(0, 0, getWidth(), getHeight());
		portalsHook();
	    }
	}
	catch (NullPointerException ex0){if (VirtualSpaceManager.debugModeON()){System.err.println("GLViewPanel.paint "+ex0);}}
	if (repaintListener != null){repaintListener.viewRepainted(this.parent);}
    }

    //XXX: TBW
    public BufferedImage getImage(){
	return null;
    }

}
