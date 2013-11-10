/*   FILE: GlyphUtils.java
 *   DATE OF CREATION:   Thu Apr 17 16:02:27 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Thu Jul 10 16:10:14 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */
 
package fr.inria.zvtm.glyphs;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VDiamond;
import fr.inria.zvtm.glyphs.VEllipse;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VRoundRect;
import fr.inria.zvtm.glyphs.VShape;
import fr.inria.zvtm.glyphs.VTriangle;
import fr.inria.zvtm.glyphs.VTriangleOr;

  /**
   * Utility functions associated with Glyphs
   * @author Emmanuel Pietriga
   */

public class GlyphUtils {

    /**
     * get a java.awt.Shape instance describing Glyph g (only working for non-Or shapes for now, but could be easily extended to support them)
     */
    public static Shape getJava2DShape(Glyph g){
	if (g instanceof VEllipse){
	    VEllipse el1=(VEllipse)g;
	    return new Ellipse2D.Double(el1.vx-el1.getWidth(),el1.vy-el1.getHeight(),el1.getWidth()*2,el1.getHeight()*2);
	}
	else if (g instanceof VRectangle){
	    VRectangle el1=(VRectangle)g;
	    return new Rectangle2D.Double(el1.vx-el1.getWidth(),el1.vy-el1.getHeight(),el1.getWidth()*2,el1.getHeight()*2);
	}
	else if (g instanceof VRoundRect){
	    VRoundRect el1=(VRoundRect)g;
	    return new RoundRectangle2D.Double(el1.vx-el1.getWidth(),el1.vy-el1.getHeight(),el1.getWidth()*2,el1.getHeight()*2,el1.getArcWidth(),el1.getArcHeight());
	}
	else if (g instanceof VCircle){
	    return new Ellipse2D.Double(g.vx-g.getSize(),g.vy-g.getSize(),g.getSize()*2,g.getSize()*2);
	}
	else if (g instanceof VShape){
	    VShape sh1=(VShape)g;
	    float vertexAngle=sh1.getOrient();
	    float[] vertices=sh1.getVertices();
	    int[] xcoords=new int[vertices.length];
	    int[] ycoords=new int[vertices.length];
	    float size=g.getSize();
	    for (int j=0;j<vertices.length-1;j++){
		xcoords[j]=(int)Math.round(sh1.vx+size*Math.cos(vertexAngle)*vertices[j]);
		ycoords[j]=(int)Math.round(sh1.vy+size*Math.sin(vertexAngle)*vertices[j]);
		vertexAngle+=2*Math.PI/vertices.length;
	    }//last iteration outside to loop to avoid one vertexAngle computation too many
	    xcoords[vertices.length-1]=(int)Math.round(sh1.vx+size*Math.cos(vertexAngle)*vertices[vertices.length-1]);
	    ycoords[vertices.length-1]=(int)Math.round(sh1.vy+size*Math.sin(vertexAngle)*vertices[vertices.length-1]);
	    return new Polygon(xcoords,ycoords,vertices.length);
	}
	else if (g instanceof VPolygon){
	    VPolygon pg1=(VPolygon)g;
	    LongPoint[] vertices=pg1.getVertices();
	    int[] xcoords=new int[vertices.length];
	    int[] ycoords=new int[vertices.length];
	    for (int j=0;j<vertices.length;j++){
		xcoords[j]=(int)Math.round(pg1.vx+vertices[j].x);
		ycoords[j]=(int)Math.round(pg1.vy+vertices[j].y);
	    }
	    return new Polygon(xcoords,ycoords,vertices.length);
	}
	else if (g instanceof VTriangle){
	    int size=Math.round(g.getSize());
	    int halfEdge=Math.round(0.866f*g.getSize());
	    int thirdHeight=Math.round(0.5f*g.getSize());
	    if (g instanceof VTriangleOr){
		float orient=g.getOrient();
		int[] xcoords=new int[3];
		int[] ycoords=new int[3];
		xcoords[0]=(int)Math.round(g.vx-size*Math.sin(orient));
		xcoords[1]=(int)Math.round(g.vx-halfEdge*Math.cos(orient)+thirdHeight*Math.sin(orient));
		xcoords[2]=(int)Math.round(g.vx+halfEdge*Math.cos(orient)+thirdHeight*Math.sin(orient));
		ycoords[0]=(int)Math.round(g.vy+size*Math.cos(orient));
		ycoords[1]=(int)Math.round(g.vy-thirdHeight*Math.cos(orient)+halfEdge*Math.sin(orient));
		ycoords[2]=(int)Math.round(g.vy-thirdHeight*Math.cos(orient)-halfEdge*Math.sin(orient));
		return new Polygon(xcoords,ycoords,3);
	    }
	    else {
		int[] xcoords={(int)g.vx,(int)g.vx-halfEdge,(int)g.vx+halfEdge};
		int[] ycoords={(int)g.vy+size,(int)g.vy-thirdHeight,(int)g.vy-thirdHeight};
		return new Polygon(xcoords,ycoords,3);
	    }
	}
	else if (g instanceof VDiamond){
	    int size=Math.round(g.getSize());
	    int[] xcoords={(int)g.vx+size,(int)g.vx,(int)g.vx-size,(int)g.vx};
	    int[] ycoords={(int)g.vy,(int)g.vy-size,(int)g.vy,(int)g.vy+size};
	    return new Polygon(xcoords,ycoords,4);
	}
	else if (g instanceof VImage){
	    VImage im1=(VImage)g;
	    return new Rectangle2D.Double(im1.vx-im1.getWidth(),im1.vy-im1.getHeight(),im1.getWidth()*2,im1.getHeight()*2);	    
	}
	else return new Rectangle2D.Double(g.vx,g.vy,1,1);  //should never happen, just for robustness
    }
    
    /**returns a (basic) clone of Glyph g, returns null if g belongs to a class that does not implement cloneable. Basic means that it copies the shape, size, orientation and position, nothing more*/
    public static Glyph basicClone(Glyph g){
	if (g instanceof Cloneable){return (Glyph)g.clone();}
	else {return null;}
    }
    
}
