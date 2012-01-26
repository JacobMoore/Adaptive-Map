/*   FILE: VirtualSpace.java
 *   DATE OF CREATION:   Jul 11 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2008. All Rights Reserved
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
 * $Id: VirtualSpace.java 3443 2010-07-28 08:57:10Z epietrig $
 */

package fr.inria.zvtm.engine;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.RectangularShape;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

  /**
   * A virtual space contains glyphs and can be observed through multiple cameras
   * @author Emmanuel Pietriga
   **/

public class VirtualSpace {

    /**
     *computes the geometrical center of a set of glyphs (takes glyph sizes into account)  (0,0 if list is empty)
     *@param gl a list of Glyph instances
     */
    public static LongPoint getGlyphSetGeometricalCenter(Glyph[] gl){
	if (gl!=null && gl.length>0){
	    long[] tmpC=new long[4];
	    long size=(long)gl[0].getSize();
	    tmpC[0]=gl[0].vx-size;
	    tmpC[1]=gl[0].vy+size;
	    tmpC[2]=gl[0].vx+size;
	    tmpC[3]=gl[0].vy-size;
	    long tmp;
	    for (int i=1;i<gl.length;i++){
		size=(long)gl[i].getSize();
		tmp=gl[i].vx-size; if (tmp<tmpC[0]){tmpC[0]=tmp;}
		tmp=gl[i].vy+size; if (tmp>tmpC[1]){tmpC[1]=tmp;}
		tmp=gl[i].vx+size; if (tmp>tmpC[2]){tmpC[2]=tmp;}
		tmp=gl[i].vy-size; if (tmp<tmpC[3]){tmpC[3]=tmp;}
	    }
	    return new LongPoint((tmpC[2]+tmpC[0])/2,(tmpC[1]+tmpC[3])/2);
	}
	else {return new LongPoint(0,0);}
    }

    /**name of virtual space*/
    public String spaceName;
    
    /**camera manager for this virtual space*/
    CameraManager cm;

    /** All glyphs in this virtual space, visible or not. Glyph instances. */
    Vector visualEnts;

    /** Visible glyphs. Ordering is important: biggest index gets drawn on top.<br>
        Shared by all cameras in the virtual space as it is the same for all of them. */
    Glyph[] drawingList;

    /** List of glyphs draw for a given camera. Vector contains Glyph instances. */
    private Vector[] camera2drawnList;
    //sharing drawnList was causing a problem ; we now have one for each camera

    /**
     *@param n virtual space name
     */
    VirtualSpace(String n){
	cm=new CameraManager(this);
	visualEnts=new Vector();
	camera2drawnList=new Vector[0];
  	drawingList = new Glyph[0];
	spaceName=n;
    }

    /**get virtual space name*/
    public String getName(){return spaceName;}

    /**get virtual space's i-th camera*/
    public Camera getCamera(int i){return cm.getCamera(i);}
    
    /**
     *@deprecated As of zvtm 0.9.0, replaced by getCameraListAsArray
     *@see #getCameraListAsArray()
     */
    public Vector getCameraList(){
	Vector res=new Vector();
	for (int i=0;i<cm.cameraList.length;i++){
	    res.add(cm.cameraList[i]);
	}
	return res;
    }

    /**returns the list of all cameras in this virtual space*/
    public Camera[] getCameraListAsArray(){return cm.cameraList;}

    /** Create a new camera*/
    public Camera addCamera(){
        Camera c=cm.addCamera();
        //create a new drawnList for it
        Vector[] newDrawnListList=new Vector[camera2drawnList.length+1];
        System.arraycopy(camera2drawnList,0,newDrawnListList,0,camera2drawnList.length);
        newDrawnListList[camera2drawnList.length]=new Vector();
        camera2drawnList=newDrawnListList;
        c.setOwningSpace(this);
        for (Enumeration e=visualEnts.elements();e.hasMoreElements();){
            Glyph g=(Glyph)e.nextElement();
            g.addCamera(c.getIndex());
        }
        return c;
    }
    
    /**remove camera at index i
     * when a camera is destroyed, its index is not reused for another one - so if camera number #3 is removed and then a new camera is added it will be assigned number #4 even though there is no camera at index #3 any longer
     *@param i index of camera in virtual space
     */
    public void removeCamera(int i){
	if (cm.cameraList.length>i){
	    for (int j=0;j<VirtualSpaceManager.INSTANCE.allViews.length;j++){
		if (VirtualSpaceManager.INSTANCE.allViews[j].cameras.contains(cm.getCamera(i))){
		    VirtualSpaceManager.INSTANCE.allViews[j].destroyCamera(cm.getCamera(i));
		}
	    }
	    for (Enumeration e=visualEnts.elements();e.hasMoreElements();){
		Glyph g=(Glyph)e.nextElement();
		g.removeCamera(i);
	    }
	    cm.removeCamera(i);
	    camera2drawnList[i]=null;
	}
    }

	/**destroy this virtual space - call method in virtual space manager*/
	protected void destroy(){
		for (int i=0;i<cm.cameraList.length;i++){
			this.removeCamera(i);
		}
		for (Enumeration e=visualEnts.elements();e.hasMoreElements();){
			this.removeGlyph((Glyph)e.nextElement());
		}
	}

    /**add glyph g to this space*/
    public void addGlyph(Glyph g, boolean initColors, boolean repaint){
        if (g == null){return;}
        if (initColors){g.setCursorInsideHighlightColor(Glyph.getDefaultCursorInsideHighlightColor());}
        g.initCams(cm.cameraList.length);
        visualEnts.add(g);
        addGlyphToDrawingList(g);
        if (repaint){VirtualSpaceManager.INSTANCE.repaintNow();}
    }

    /**add glyph g to this space*/
    public void addGlyph(Glyph g){
        addGlyph(g, true, true);
    }
    
    /**add glyph g to this space*/
    public void addGlyph(Glyph g, boolean repaint){
        addGlyph(g, true, repaint);
    }

	public void addGlyphs(Glyph[] glyphs, boolean repaint){
		for(Glyph glyph: glyphs){
			glyph.initCams(cm.cameraList.length);
			visualEnts.add(glyph);
		}
		addGlyphsToDrawingList(glyphs);
		if (repaint){VirtualSpaceManager.INSTANCE.repaintNow();}
	}

	public void addGlyphs(Glyph[] glyphs){
		addGlyphs(glyphs, true);
	}
	
    /** Get all glyphs in this space, visible or not, sensitive or not.
     * IMPORTANT: Read-only. Do not temper with this data structure unless you know what you are doing.
     * It is highly recommended to clone it if you want to add/remove elements from it for your own purposes.
     */
    public Vector getAllGlyphs(){
	return visualEnts;
    }

    /**get all visible glyphs
     *@deprecated as of zvtm 0.9.2
     *@see #getDrawnGlyphs(int cameraIndex)
     */
    public Vector getVisibleGlyphs(){
	Vector res = new Vector();
	for (int i=0;i<drawingList.length;i++){
	    res.add(drawingList[i]);
	}
	return res;
    }

    /** Get all visible glyphs (not cloned). */
	public Glyph[] getDrawingList(){
		return drawingList;
	}

    /** Get all visible glyphs (clone). */
    public Glyph[] getVisibleGlyphList(){
		Glyph[] res = new Glyph[drawingList.length];
		System.arraycopy(drawingList, 0, res, 0, drawingList.length);
		return res;
    }

    /**
     *@deprecated as of zvtm 0.9.0
     *@see #getDrawnGlyphs(int cameraIndex)
     */
    public Vector getDrawnGlyphs(){
	if (camera2drawnList.length>0){
	    return camera2drawnList[0];
	}
	else return null;
    }

    /**
     *get all glyphs actually drawn for a given camera in this virtual space
     */
    public Vector getDrawnGlyphs(int cameraIndex){
	if (cameraIndex<camera2drawnList.length){
	    return camera2drawnList[cameraIndex];
	}
	else return null;
    }

    /*put glyph gl in the list of glyphs actually drawn (this list is used to compute the list of glyphs under mouse)*/
    protected void drewGlyph(Glyph gl,int cameraIndex){
	if (cameraIndex<camera2drawnList.length && camera2drawnList[cameraIndex]!=null){
	    camera2drawnList[cameraIndex].add(gl);
	}
    }

    /**get selected glyphs*/
    public Vector getSelectedGlyphs(){
	Vector v=new Vector();
	Glyph g;
	for (Enumeration e=visualEnts.elements();e.hasMoreElements();){
	    g=(Glyph)e.nextElement();
	    if (g.isSelected()){
		v.add(g);
	    }
	}
	return v;
    }

    /**select all glyphs*/
    public void selectAllGlyphs(){
	for (Enumeration e=visualEnts.elements();e.hasMoreElements();){
	    ((Glyph)e.nextElement()).select(true);
	}
    }

    /**unselect all glyphs*/
    public void unselectAllGlyphs(){
	for (Enumeration e=visualEnts.elements();e.hasMoreElements();){
	    ((Glyph)e.nextElement()).select(false);
	}
    }

    /**get all glyphs of type t - if t=="" then select all glyphs (means ANY type)*/
    public Vector getGlyphsOfType(String t){//
	Vector v=new Vector();
	Glyph g;
	for (Enumeration e=visualEnts.elements();e.hasMoreElements();){
	    g=(Glyph)e.nextElement();
	    if ((t.equals("")) || (t.equals(g.getType()))){v.add(g);}
	}
	return v;
    }

//    /** Remove this glyph from this virtual space. ZVTM no longer holds a reference to it. View will be updated.
//     *@param gID glyph's ID
//     */
//    public void removeGlyph(Long gID){
//	    removeGlyph(VirtualSpaceManager.INSTANCE.getGlyph(gID), true);
//    }
//
//    /** Remove this glyph from this virtual space. ZVTM no longer holds a reference to it.
//     *@param gID glyph's ID
//     *@param repaint should the view be updated automatically or not
//     */
//    public void removeGlyph(Long gID, boolean repaint){
//	    removeGlyph(VirtualSpaceManager.INSTANCE.getGlyph(gID), repaint);
//    }

    /** Remove all glyphs in this virtual space.
     *
     */
    public void removeAllGlyphs(){
        Vector entClone = (Vector)getAllGlyphs().clone();
        for (int i=0;i<entClone.size();i++){
            removeGlyph((Glyph)entClone.elementAt(i), false);
        }
        VirtualSpaceManager.INSTANCE.repaintNow();        
    }

    /** Remove this glyph from this virtual space. ZVTM no longer holds a reference to it. View will be updated. */
    public void removeGlyph(Glyph g){
        removeGlyph(g, true);
    }

    /** Remove this glyph from this virtual space. ZVTM no longer holds a reference to it.
        *@param repaint should the view be updated automatically or not
        */
    public void removeGlyph(Glyph g, boolean repaint){
        try {
            if (g.stickedTo!=null){
                if (g.stickedTo instanceof Glyph){((Glyph)g.stickedTo).unstick(g);}
                else if (g.stickedTo instanceof Camera){((Camera)g.stickedTo).unstick(g);}
                else {((VCursor)g.stickedTo).unstickSpecificGlyph(g);}
            }
            for (int i=0;i<camera2drawnList.length;i++){
                if (camera2drawnList[i]!=null){
                    //camera2drawnlist[i] can be null if camera i has been removed from the virtual space
                    camera2drawnList[i].remove(g);
                }
            }
            View v;
            for (int i=0;i<cm.cameraList.length;i++){
                if (cm.cameraList[i] != null && cm.cameraList[i].view != null){
                    cm.cameraList[i].view.mouse.removeGlyphFromList(g);
                }
            }
            visualEnts.remove(g);
            removeGlyphFromDrawingList(g);
            if (repaint){
                VirtualSpaceManager.INSTANCE.repaintNow();
            }
        }
        catch (NullPointerException ex){
            System.err.println("ZVTM Error: VirtualSpace.removeGlyph(): the glyph you are trying to delete might not be a member of this virtual space ("+spaceName+") or might be null");
            ex.printStackTrace();
        }
    }

	/** Remove this glyph from this virtual space. ZVTM no longer holds a reference to it. View will be updated.
		*@deprecated as of zvtm 0.9.8
		*@see #removeGlyph(Glyph g)
		*/
	public void destroyGlyph(Glyph g){
        removeGlyph(g, true);
    }

	/** Remove this glyph from this virtual space. ZVTM no longer holds a reference to it.
		*@param repaint should the view be updated automatically or not
		*@deprecated as of zvtm 0.9.8
		*@see #removeGlyph(Glyph g, boolean repaint)
		*/
    public void destroyGlyph(Glyph g, boolean repaint){
		removeGlyph(g, repaint);
    }

    /**show Glyph g
     * <br>- use show() and hide() to change both the visibility and sensitivity of glyphs
     * <br>- use Glyph.setVisible() to only change the glyph's visibility
     *@see #hide(Glyph g)*/
    public void show(Glyph g){
	if (visualEnts.contains(g) && glyphIndexInDrawingList(g) == -1){addGlyphToDrawingList(g);}
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    /**hide Glyph g
     * <br>- use show() and hide() to change both the visibility and sensitivity of glyphs
     * <br>- use Glyph.setVisible() to only change the glyph's visibility
     *@see #show(Glyph g)*/
    public void hide(Glyph g){
	removeGlyphFromDrawingList(g);
	g.resetMouseIn();
	View v;
	for (int i=0;i<cm.cameraList.length;i++){
	    if (cm.cameraList[i] != null && cm.cameraList[i].view != null){
		cm.cameraList[i].view.mouse.removeGlyphFromList(g);
	    }
	}
	VirtualSpaceManager.INSTANCE.repaintNow();
    }

    /** Put this glyph on top of the drawing list (will be drawn last).
        * Important: this might affect the glyph's z-index.
        */
    public void onTop(Glyph g){
        if (glyphIndexInDrawingList(g) != -1){
            removeGlyphFromDrawingList(g);
            addGlyphToDrawingList(g);
            // assign the glyph the same z-index as the glyph that was previously the topmost in the list
            g.setZindex((drawingList.length>0) ? drawingList[drawingList.length-1].getZindex() : 0);
        }
    }

    /** Put this glyph at bottom of the drawing list (will be drawn first).
        * Important: this might affect the glyph's z-index.
        */
    public void atBottom(Glyph g){
        if (glyphIndexInDrawingList(g) != -1){
            removeGlyphFromDrawingList(g);
            insertGlyphInDrawingList(g,0);
            // assign the glyph the lowest z-index possible
            g.setZindex(0);
        }
    }

	public void onTop(Glyph g, int z){
		if (glyphIndexInDrawingList(g) != -1){
            removeGlyphFromDrawingList(g);
	        // insert at bottom of list if no other glyph has a lower z-index
	        int insertAt = 0;
	            // insert glyph in the drawing list so that 
	            // it is the last glyph to be drawn for a given z-index
	            for (int i=drawingList.length-1;i>=0;i--){
	                if (drawingList[i].getZindex() <= z){
	                    insertAt = i + 1;
	                    break;
	                }
	            }
	            insertGlyphInDrawingList(g, insertAt);
            g.setZindex(z);
        }
	}
	
	public void atBottom(Glyph g, int z){
		if (glyphIndexInDrawingList(g) != -1){
            removeGlyphFromDrawingList(g);
	        // insert at bottom of list if no other glyph has a lower z-index
	        int insertAt = 0;
	            // insert glyph in the drawing list so that 
	            // it is the last glyph to be drawn for a given z-index
	            for (int i=0;i<drawingList.length;i++){
	                if (drawingList[i].getZindex() > z){
	                    insertAt = i;
	                    break;
	                }
	            }
	            insertGlyphInDrawingList(g, insertAt);
            g.setZindex(z);
        }
	}

    /** Put glyph g1 just above glyph g2 in the drawing list (g1 painted after g2).
        * Important: this might affect the glyph's z-index.
        */
    public void above(Glyph g1, Glyph g2){
	if(g1 == g2) return;

        if ((glyphIndexInDrawingList(g1) != -1) && (glyphIndexInDrawingList(g2) != -1)){
            removeGlyphFromDrawingList(g1);
            int i = glyphIndexInDrawingList(g2);
            insertGlyphInDrawingList(g1,i+1);
            g1.setZindex(g2.getZindex());
        }
    }

    /** Put glyph g1 just below glyph g2 in the drawing list (g1 painted before g2).
        * Important: this might affect the glyph's z-index.
        */
    public void below(Glyph g1, Glyph g2){
	if(g1 == g2) return;

        if ((glyphIndexInDrawingList(g1) != -1) && (glyphIndexInDrawingList(g2) != -1)){
            removeGlyphFromDrawingList(g1);
            int i = glyphIndexInDrawingList(g2);
            insertGlyphInDrawingList(g1,i);
            g1.setZindex(g2.getZindex());
        }
    }

    /**returns the leftmost Glyph x-pos, upmost Glyph y-pos, rightmost Glyph x-pos, downmost Glyph y-pos visible in this virtual space*/
    public long[] findFarmostGlyphCoords(){
	long[] res = new long[4];
	return findFarmostGlyphCoords(res);
    }
    
	/**returns the leftmost Glyph x-pos, upmost Glyph y-pos, rightmost Glyph x-pos, downmost Glyph y-pos visible in this virtual space*/
	public long[] findFarmostGlyphCoords(long[] res){
		Glyph[] gl = this.getVisibleGlyphList();
		if (gl.length > 0){
			RectangularShape rs;
			long size;
			//init result with first glyph found
			if (gl[0] instanceof RectangularShape){
				rs = (RectangularShape)gl[0];
				res[0] = gl[0].vx - rs.getWidth();
				res[1] = gl[0].vy + rs.getHeight();
				res[2] = gl[0].vx + rs.getWidth();
				res[3] = gl[0].vy - rs.getHeight();				
			}
			else {
				size = (long)gl[0].getSize();
				res[0] = gl[0].vx - size;
				res[1] = gl[0].vy + size;
				res[2] = gl[0].vx + size;
				res[3] = gl[0].vy - size;
			}
			long tmp;
			for (int i=1;i<gl.length;i++){
				if (gl[i] instanceof RectangularShape){
					rs = (RectangularShape)gl[i];
					tmp = gl[i].vx - rs.getWidth();if (tmp<res[0]){res[0] = tmp;}
					tmp = gl[i].vy + rs.getHeight();if (tmp>res[1]){res[1] = tmp;}
					tmp = gl[i].vx + rs.getWidth();if (tmp>res[2]){res[2] = tmp;}
					tmp = gl[i].vy - rs.getHeight();if (tmp<res[3]){res[3] = tmp;}
				}
				else {
					size = (long)gl[i].getSize();
					tmp = gl[i].vx - size;if (tmp<res[0]){res[0] = tmp;}
					tmp = gl[i].vy + size;if (tmp>res[1]){res[1] = tmp;}
					tmp = gl[i].vx + size;if (tmp>res[2]){res[2] = tmp;}
					tmp = gl[i].vy - size;if (tmp<res[3]){res[3] = tmp;}
				}
			}
			return res;
		}
		else {
			Arrays.fill(res, 0);
			return res;
		}
	}

    protected void addGlyphToDrawingList(Glyph g){
        int zindex = g.getZindex();
        // insert at bottom of list if no other glyph has a lower z-index
        int insertAt = 0;
            // insert glyph in the drawing list so that 
            // it is the last glyph to be drawn for a given z-index
            for (int i=drawingList.length-1;i>=0;i--){
                if (drawingList[i].getZindex() <= zindex){
                    insertAt = i + 1;
                    break;
                }
            }
            insertGlyphInDrawingList(g, insertAt);
    }

	protected void addGlyphsToDrawingList(Glyph[] glyphs){
			//create a new drawingList array of the right size
			Glyph[] newDrawingList = new Glyph[drawingList.length + glyphs.length];
			//merge glyphs and drawingList into the new array
			System.arraycopy(drawingList,0,newDrawingList,0,drawingList.length);
			System.arraycopy(glyphs,0,newDrawingList,drawingList.length,
					glyphs.length);
			Arrays.sort(newDrawingList,
					new java.util.Comparator<Glyph>(){
						public int compare(Glyph g1, Glyph g2){
							if(g1.getZindex() < g2.getZindex()){
								return -1;
							} else if (g1.getZindex() > g2.getZindex()){
								return 1;
							} else {
								return 0;
							}
						}
					});
			//overwrite drawingList
			drawingList = newDrawingList;
	}

    protected void insertGlyphInDrawingList(Glyph g, int index){
            Glyph[] newDrawingList = new Glyph[drawingList.length + 1];
            System.arraycopy(drawingList, 0, newDrawingList, 0, index);
            newDrawingList[index] = g;
            System.arraycopy(drawingList, index, newDrawingList, index+1, drawingList.length-index);
            drawingList = newDrawingList;
    }

    protected void removeGlyphFromDrawingList(Glyph g){
            for (int i=0;i<drawingList.length;i++){
                if (drawingList[i] == g){
                    Glyph[] newDrawingList = new Glyph[drawingList.length - 1];
                    System.arraycopy(drawingList, 0, newDrawingList, 0, i);
                    System.arraycopy(drawingList, i+1, newDrawingList, i, drawingList.length-i-1);
                    drawingList = newDrawingList;
                    break;
                }
            }
    }

    protected int glyphIndexInDrawingList(Glyph g){
            for (int i=0;i<drawingList.length;i++){
                if (drawingList[i] == g){
                    return i;
                }
            }
        return -1;
    }

}
