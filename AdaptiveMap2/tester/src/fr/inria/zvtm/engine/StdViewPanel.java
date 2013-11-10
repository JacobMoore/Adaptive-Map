/*   FILE: StdViewPanel.java
 *   DATE OF CREATION:   Jul 11 2000
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
 * $Id: StdViewPanel.java 3424 2010-07-12 09:26:23Z epietrig $
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
import java.util.Vector;

import javax.swing.Timer;

import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.engine.ViewEventHandler;


/**
 * @author Emmanuel Pietriga
 */
public class StdViewPanel extends ViewPanel {

	/** Double Buffering uses a BufferedImage as the back buffer. */
	BufferedImage backBuffer;
	int backBufferW = 0;
	int backBufferH = 0;

	private Graphics2D backBufferGraphics = null;
	Dimension oldSize;
	Graphics2D lensG2D = null;

	private Timer edtTimer;

	StdViewPanel(Vector cameras,View v, boolean arfome) {
		ActionListener taskPerformer = new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				drawOffscreen();
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
		this.setDoubleBuffered(false);
		setAutoRequestFocusOnMouseEnter(arfome);
		setAWTCursor(Cursor.CUSTOM_CURSOR);  //custom cursor means VTM cursor
		this.size = this.getSize();
		if (VirtualSpaceManager.debugModeON()){System.out.println("View refresh time set to "+frameTime+"ms");}
	}

	private void start(){
		backBufferGraphics = null;
		edtTimer.start();
	}

	public void stop(){
		edtTimer.stop();
		if (stableRefToBackBufferGraphics != null) {
			stableRefToBackBufferGraphics.dispose();
		}
	}

	private void updateOffscreenBuffer(){
		size = this.getSize();
		if (size.width != oldSize.width || size.height != oldSize.height || backBufferW != size.width || backBufferH != size.height) {
			//each time the parent window is resized, adapt the buffer image size
			backBuffer = null;
			if (backBufferGraphics != null) {
				backBufferGraphics.dispose();
				backBufferGraphics = null;
			}
			if (lens != null){
				lens.resetMagnificationBuffer();
				if (lensG2D != null) {
					lensG2D.dispose();
					lensG2D = null;
				}
			}
			if (VirtualSpaceManager.debugModeON()){
				System.out.println("Resizing JPanel: ("+oldSize.width+"x"+oldSize.height+") -> ("+size.width+"x"+size.height+")");
			}
			oldSize=size;
			updateAntialias=true;
			updateFont=true;
		}
		if (backBuffer == null){
			gconf = getGraphicsConfiguration();
			// assign minimal size of 1
			backBuffer = gconf.createCompatibleImage((size.width > 0) ? size.width : 1, (size.height > 0) ? size.height : 1);
			backBufferW = backBuffer.getWidth();
			backBufferH = backBuffer.getHeight();
			if (backBufferGraphics != null){
				backBufferGraphics.dispose();
				backBufferGraphics = null;
			}
		}
		if (backBufferGraphics == null) {
			backBufferGraphics = backBuffer.createGraphics();
			updateAntialias=true;
			updateFont=true;
		}
		if (lens != null){
			lensG2D = lens.getMagnificationGraphics();
			lensG2D.setFont(VText.getMainFont());
			if (antialias){
				lensG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}
			else {
				lensG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			}
		}
		if (updateFont){
			backBufferGraphics.setFont(VText.getMainFont());
			if (lensG2D != null){
				lensG2D.setFont(VText.getMainFont());
			}
			updateFont = false;
		}
		if (updateAntialias){
			if (antialias){
				backBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				if (lensG2D != null){
					lensG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				}
			}
			else {
				backBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
				if (lensG2D != null){
					lensG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
				}
			}
			updateAntialias = false;
		}
		stableRefToBackBufferGraphics = backBufferGraphics;
		standardStroke=stableRefToBackBufferGraphics.getStroke();
		standardTransform=stableRefToBackBufferGraphics.getTransform();
	}

	private void drawScene(boolean drawLens){	
		if(drawLens){
			if (lensG2D == null){
				updateOffscreenBuffer();
			}
			lensG2D.setPaintMode(); // to the lens from LAnimation.animate() methods and this thread
			lensG2D.setBackground(backColor);
			lensG2D.clearRect(0, 0, lens.mbw, lens.mbh);
		}
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
				long lviewWC = 0;
				long lviewNC = 0;
				long lviewEC = 0;
				long lviewSC = 0;
				long lensVx = 0;
				long lensVy = 0;
				if(drawLens){
					lviewWC = (long)(cams[nbcam].posx + (lens.lx-lens.lensWidth/2)*uncoef);
					lviewNC = (long)(cams[nbcam].posy + (-lens.ly+lens.lensHeight/2)*uncoef);
					lviewEC = (long)(cams[nbcam].posx + (lens.lx+lens.lensWidth/2)*uncoef);
					lviewSC = (long)(cams[nbcam].posy + (-lens.ly-lens.lensHeight/2)*uncoef);
					lensVx = (lviewWC+lviewEC)/2;
					lensVy = (lviewSC+lviewNC)/2;

				}
				gll = cams[nbcam].parentSpace.getDrawingList();
				for (int i=0;i<gll.length;i++){
					if (gll[i] != null){
						if (gll[i].visibleInViewport(viewWC, viewNC, viewEC, viewSC, cams[nbcam])){
							/* if glyph is at least partially visible in the reg. seen from this view,
							   compute in which buffer it should be rendered: */
							/* always draw in the main buffer */
							gll[i].project(cams[nbcam], size);
							if (gll[i].isVisible()){
								gll[i].draw(stableRefToBackBufferGraphics, size.width, size.height, cams[nbcam].getIndex(),
										standardStroke, standardTransform, 0, 0);
							}
							if(drawLens){	
								if (gll[i].visibleInViewport(lviewWC, lviewNC, lviewEC, lviewSC, cams[nbcam])){
									/* partially within the region seen through the lens
									   draw it in both buffers */
									gll[i].projectForLens(cams[nbcam], lens.mbw, lens.mbh, lens.getMaximumMagnification(), lensVx, lensVy);
									if (gll[i].isVisibleThroughLens()){
										gll[i].drawForLens(lensG2D, lens.mbw, lens.mbh, cams[nbcam].getIndex(),
												standardStroke, standardTransform, 0, 0);
									}
								}
							}
							/* notifying outside of above test because glyph sensitivity is not
							   affected by glyph visibility when managed through Glyph.setVisible() */
							cams[nbcam].parentSpace.drewGlyph(gll[i], camIndex);
						}
					}
				}
			}
		}
		foregroundHook();
		if(drawLens){
			try {
				lens.transform(backBuffer);
				lens.drawBoundary(stableRefToBackBufferGraphics);
			}
			catch (ArrayIndexOutOfBoundsException ex){
				if (VirtualSpaceManager.debugModeON()){ex.printStackTrace();}
			}
			catch (NullPointerException ex2){
				// this sometimes happens when the lens is unset after entering this branch but before doing the actual transform
				if (VirtualSpaceManager.debugModeON()){ex2.printStackTrace();}
			}
		}
	}

	private void drawCursor(){
		stableRefToBackBufferGraphics.setColor(parent.mouse.hcolor);
		if (drawDrag){stableRefToBackBufferGraphics.drawLine(origDragx,origDragy,parent.mouse.mx,parent.mouse.my);}
		if (drawRect){
			stableRefToBackBufferGraphics.drawRect(Math.min(origDragx,parent.mouse.mx),
					Math.min(origDragy,parent.mouse.my),
					Math.max(origDragx,parent.mouse.mx)-Math.min(origDragx,parent.mouse.mx),
					Math.max(origDragy,parent.mouse.my)-Math.min(origDragy,parent.mouse.my));}
		if (drawOval){
			if (circleOnly){
				stableRefToBackBufferGraphics.drawOval(origDragx-Math.abs(origDragx-parent.mouse.mx),
						origDragy-Math.abs(origDragx-parent.mouse.mx),
						2*Math.abs(origDragx-parent.mouse.mx),
						2*Math.abs(origDragx-parent.mouse.mx));
			}
			else {
				stableRefToBackBufferGraphics.drawOval(origDragx-Math.abs(origDragx-parent.mouse.mx),
						origDragy-Math.abs(origDragy-parent.mouse.my),
						2*Math.abs(origDragx-parent.mouse.mx),
						2*Math.abs(origDragy-parent.mouse.my));
			}
		}
		if (drawVTMcursor){
			stableRefToBackBufferGraphics.setXORMode(backColor);
			parent.mouse.draw(stableRefToBackBufferGraphics);
			oldX = parent.mouse.mx;
			oldY = parent.mouse.my;			    
		}

	}

	private void doCursorPicking(){
		try {                  // branch and we want to catch new requests for repaint
			parent.mouse.unProject(cams[activeLayer],this); //we project the mouse cursor wrt the appropriate coord sys
			if (computeListAtEachRepaint && parent.mouse.isSensitive()){parent.mouse.computeCursorOverList(evHs[activeLayer],cams[activeLayer], this);}
		}
		catch (NullPointerException ex) {if (VirtualSpaceManager.debugModeON()){System.err.println("viewpanel.run.drawdrag "+ex);}}
	}

	//draw ONCE (no more infinite thread loop; will be driven
	//from an EDT timer)
	public void drawOffscreen() {
		oldSize=getSize();
		if (notBlank){
			if (active){
				if (repaintNow){
					try {
						repaintNow=false; //do this first as the thread can be interrupted inside
						//this branch and we want to catch new requests for repaint
						updateMouseOnly=false;
						updateOffscreenBuffer();
						stableRefToBackBufferGraphics.setPaintMode();
						stableRefToBackBufferGraphics.setBackground(backColor);
						stableRefToBackBufferGraphics.clearRect(0, 0, size.width, size.height);
						backgroundHook();
						//begin actual drawing here
						if(lens != null) { 
							drawScene(true); 
						} else {
							drawScene(false); 
						}
						afterLensHook();
						drawPortals();
						portalsHook();

						if (inside){//deal with mouse glyph only if mouse cursor is inside this window
							doCursorPicking();
							drawCursor();
						}
						//end drawing here
						if (stableRefToBackBufferGraphics == backBufferGraphics) {
							paintImmediately(0,0,size.width,size.height);
						}
					}
					catch (NullPointerException ex0){
						if (VirtualSpaceManager.debugModeON()){
							System.err.println("viewpanel.run (probably due to backBuffer.createGraphics()) "+ex0);
							ex0.printStackTrace();
						}
					}
				}
				else if (updateMouseOnly){
					updateMouseOnly=false; // do this first as the thread can be interrupted inside this
					doCursorPicking();
					if (drawVTMcursor){
						try {
							stableRefToBackBufferGraphics.setXORMode(backColor);
							stableRefToBackBufferGraphics.setColor(parent.mouse.color);
							stableRefToBackBufferGraphics.drawLine(oldX-parent.mouse.size,oldY,oldX+parent.mouse.size,oldY);
							stableRefToBackBufferGraphics.drawLine(oldX,oldY-parent.mouse.size,oldX,oldY+parent.mouse.size);
							stableRefToBackBufferGraphics.drawLine(parent.mouse.mx-parent.mouse.size,parent.mouse.my,parent.mouse.mx+parent.mouse.size,parent.mouse.my);
							stableRefToBackBufferGraphics.drawLine(parent.mouse.mx,parent.mouse.my-parent.mouse.size,parent.mouse.mx,parent.mouse.my+parent.mouse.size);
							oldX = parent.mouse.mx;
							oldY = parent.mouse.my;							        
						}
						//XXX: a nullpointerex on stableRefToBackBufferGraphics seems to occur from time to time when going in or exiting from blank mode
						//just catch it and wait for next loop until we find out what's causing this
						catch (NullPointerException ex47){if (VirtualSpaceManager.debugModeON()){System.err.println("viewpanel.run.runview.drawVTMcursor "+ex47);}} 
					}
					paintImmediately(0,0,size.width,size.height);
				}
				else {
				}
			}
			else {
			}
		}
		else {
			updateOffscreenBuffer();	
			stableRefToBackBufferGraphics.setPaintMode();
			stableRefToBackBufferGraphics.setColor(blankColor);
			stableRefToBackBufferGraphics.fillRect(0,0,getWidth(),getHeight());
			portalsHook();				
			paintImmediately(0,0,size.width,size.height);
		}
	}

	public void paint(Graphics g) {
		if (backBuffer != null){
			g.drawImage(backBuffer, 0, 0, this);
			if (repaintListener != null){repaintListener.viewRepainted(this.parent);}
		}
	}

	/** Get a snapshot of this view.*/
	public BufferedImage getImage(){
		return this.backBuffer;
	}

}
