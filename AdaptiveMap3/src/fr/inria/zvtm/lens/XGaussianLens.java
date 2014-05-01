/*   FILE: TLinearLens.java
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2013. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: XGaussianLens.java 4912 2013-02-07 20:16:30Z epietrig $
 */

package fr.inria.zvtm.lens;

import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

/**Profile: linear - Distance metric: L(2) (circular shape)<br>Rendering enhanced through alpha blending between focus and context in spatially-distorted transition zone.<br>Size expressed as an absolute value in pixels*/

public class XGaussianLens extends XLinearLens {

    protected double c = 0;
    protected double e = 0;

    /**
     * create a lens with a maximum magnification factor of 2.0
     */
    public XGaussianLens(){
	    this(2.0f, 0, 1, 100, 50, 0, 0);
    }

    /**
     * create a lens with a given maximum magnification factor
     *
     *@param mm magnification factor, mm in [0,+inf[
     */
    public XGaussianLens(float mm){
	    this(mm, 0, 1, 100, 50, 0, 0);
    }

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm magnification factor, mm in [0,+inf[
     *@param tc translucency value (at junction between transition and context), tc in [0,1.0]
     *@param tf translucency value (at junction between transition and focus), tf in [0,1.0]
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     */
    public XGaussianLens(float mm, float tc, float tf, int outerRadius, int innerRadius){
	    this(mm, tc, tf, outerRadius, innerRadius, 0, 0);
    }

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm magnification factor, mm in [0,+inf[
     *@param tc translucency value (at junction between transition and context), tc in [0,1.0]
     *@param tf translucency value (at junction between transition and focus), tf in [0,1.0]
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     *@param x horizontal coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
     *@param y vertical coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
     */
    public XGaussianLens(float mm, float tc, float tf, int outerRadius, int innerRadius, int x, int y){
	super(mm, tc, tf, outerRadius, innerRadius, x, y);
	a = Math.PI/(LR1-LR2);
	b = - Math.PI*LR2/(LR1-LR2);
	c = (MM-1)/2;
	e = (1+MM)/2;
    }

    public void gf(float x, float y, float[] g){
	dd = Math.sqrt((x-sw-lx)*(x-sw-lx) + (y-sh-ly)*(y-sh-ly));
	if (dd <= LR2){
	    g[0] = g[1] = MM;
	}
	else if (dd <= LR1){
	    g[0] = g[1] = (float)(c * Math.cos(a*dd+b) + e);
	}
	else {
	    g[0] = g[1] = 1;
	}
    }

    void computeDropoffFactors(){
	aT = (MMTc-MMTf) / ((float)(LR1-LR2));
	bT = (MMTf*LR1-MMTc*LR2) / ((float)(LR1-LR2));
	a = Math.PI/(LR1-LR2);
	b = - Math.PI*LR2/(LR1-LR2);
	c = (MM-1)/2;
	e = (1+MM)/2;
    }

    synchronized void transformI(WritableRaster iwr, WritableRaster ewr){
	synchronized(this){
	    if (BMl == null){// || BMm == null || BOl == null || BOm == null
		// initialization of raster band configuration (should only occur once)
		SinglePixelPackedSampleModel SMl = (SinglePixelPackedSampleModel)ewr.getSampleModel();
		SinglePixelPackedSampleModel SMm = (SinglePixelPackedSampleModel)iwr.getSampleModel();
		BMl = SMl.getBitMasks();
		BMm = SMm.getBitMasks();
		BOl = SMl.getBitOffsets();
		BOm = SMm.getBitOffsets();
	    }
	    // get source pixels in an array
	    iwr.getDataElements(lurd[0], lurd[1], lensWidth, lensHeight, oPixelsI);
	    // get magnified source pixels in a second array
	    ewr.getDataElements(0, 0, mbw, mbh, mPixelsI);
	    // transfer them to the target array taking the gain function into account
	    if (BMl.length == 4){// the sample model features four bands
		for (int x=lurd[0];x<lurd[2];x++){
		    for (int y=lurd[1];y<lurd[3];y++){
			/* SPATIAL DISTORTION */
			/* gain is computed w.r.t main buffer pixels
			   (we do not want to compute the gain for pixels that won't be in the output) */
			this.gf(x,y,gain);
			tmPixelsI[(y-lurd[1])*(lensWidth)+(x-lurd[0])] =
			    mPixelsI[Math.round(((y-lurd[1]) * MM - hmbh) / gain[1] + hmbh)*mbw+Math.round(((x-lurd[0]) * MM - hmbw) / gain[0] + hmbw)];
			toPixelsI[(y-lurd[1])*(lensWidth)+(x-lurd[0])] =
			    oPixelsI[(Math.round((((float)y-sh-ly)/gain[1])+sh+ly)-lurd[1])*(lensWidth)+(Math.round((((float)x-sw-lx)/gain[0])+sw+lx)-lurd[0])];
			/* ALPHA BLENDING */
			// get pixel from lens raster
			Pl = tmPixelsI[(y-lurd[1])*(lensWidth)+(x-lurd[0])];
			Rl = (Pl & BMl[0]) >>> BOl[0];
			Gl = (Pl & BMl[1]) >>> BOl[1];
			Bl = (Pl & BMl[2]) >>> BOl[2];
			// get pixel from main raster
			Pm = toPixelsI[(y-lurd[1])*(lensWidth)+(x-lurd[0])];
			Rm = (Pm & BMm[0]) >>> BOm[0];
			Gm = (Pm & BMm[1]) >>> BOm[1];
			Bm = (Pm & BMm[2]) >>> BOm[2];
			Am = (Pm & BMm[3]) >>> BOm[3];
			// compute contribution from each pixel, for each band
			// Use the Porter-Duff Source Atop Destination rule to achieve our effect.
			// Fs = Ad and Fd = (1-As), thus:
			//   Cd = Cs*Ad + Cd*(1-As)
			//   Ad = As*Ad + Ad*(1-As) = Ad
			this.gfT(x,y,gainT);
			Rr = Math.round(Rl*gainT[0] + Rm*(1-gainT[0]));
			Gr = Math.round(Gl*gainT[0] + Gm*(1-gainT[0]));
			Br = Math.round(Bl*gainT[0] + Bm*(1-gainT[0]));
			// set new pixel value in target raster
			tPixelsI[(y-lurd[1])*(lensWidth)+(x-lurd[0])] = (Rr << BOm[0]) | (Gr << BOl[1]) | (Br << BOl[2]) | (Am << BOl[3]);
		    }
		}
	    }
	    else {// the sample model probably features 3 bands
		for (int x=lurd[0];x<lurd[2];x++){
		    for (int y=lurd[1];y<lurd[3];y++){
			/* SPATIAL DISTORTION */
			/* gain is computed w.r.t main buffer pixels
			   (we do not want to compute the gain for pixels that won't be in the output) */
			this.gf(x,y,gain);
			tmPixelsI[(y-lurd[1])*(lensWidth)+(x-lurd[0])] =
			    mPixelsI[Math.round(((y-lurd[1]) * MM - hmbh) / gain[1] + hmbh)*mbw+Math.round(((x-lurd[0]) * MM - hmbw) / gain[0] + hmbw)];
			toPixelsI[(y-lurd[1])*(lensWidth)+(x-lurd[0])] =
			    oPixelsI[(Math.round((((float)y-sh-ly)/gain[1])+sh+ly)-lurd[1])*(lensWidth)+(Math.round((((float)x-sw-lx)/gain[0])+sw+lx)-lurd[0])];
			/* ALPHA BLENDING */
			// get pixel from lens raster
			Pl = tmPixelsI[(y-lurd[1])*(lensWidth)+(x-lurd[0])];
			Rl = (Pl & BMl[0]) >>> BOl[0];
			Gl = (Pl & BMl[1]) >>> BOl[1];
			Bl = (Pl & BMl[2]) >>> BOl[2];
			// get pixel from main raster
			Pm = toPixelsI[(y-lurd[1])*(lensWidth)+(x-lurd[0])];
			Rm = (Pm & BMm[0]) >>> BOm[0];
			Gm = (Pm & BMm[1]) >>> BOm[1];
			Bm = (Pm & BMm[2]) >>> BOm[2];
			// compute contribution from each pixel, for each band
			// Use the Porter-Duff Source Atop Destination rule to achieve our effect.
			// Fs = Ad and Fd = (1-As), thus:
			//   Cd = Cs*Ad + Cd*(1-As)
			//   Ad = As*Ad + Ad*(1-As) = Ad
			this.gfT(x,y,gainT);
			Rr = Math.round(Rl*gainT[0] + Rm*(1-gainT[0]));
			Gr = Math.round(Gl*gainT[0] + Gm*(1-gainT[0]));
			Br = Math.round(Bl*gainT[0] + Bm*(1-gainT[0]));
			// set new pixel value in target raster
			tPixelsI[(y-lurd[1])*(lensWidth)+(x-lurd[0])] = (Rr << BOm[0]) | (Gr << BOl[1]) | (Br << BOl[2]);
		    }
		}
	    }
	    // transfer pixels in the target array back to the raster
	    iwr.setDataElements(lurd[0], lurd[1], lensWidth, lensHeight, tPixelsI);
	}
    }

    synchronized void transformS(WritableRaster iwr, WritableRaster ewr){System.err.println("Error: translucent lens: Sample model not supported yet");}

    synchronized void transformB(WritableRaster iwr, WritableRaster ewr){System.err.println("Error: translucent lens: Sample model not supported yet");}

}
