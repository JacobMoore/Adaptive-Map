/*   FILE: Java2DPainter.java
 *   DATE OF CREATION:  Fri Aug 26 09:31:59 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: Utils.java 2102 2009-06-23 08:57:56Z rprimet $
 */ 

package fr.inria.zvtm.engine;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;

public class Utils {

    public static void tile(String src, String targetPattern, int tileSize){
	try {
	    File source = new File(src);
	    ImageInputStream iis = ImageIO.createImageInputStream(source);
	    Iterator readers = ImageIO.getImageReaders(iis);

	    final ImageReader reader = (ImageReader) readers.next();
	    reader.setInput(iis, true);

	    final ImageReadParam param = reader.getDefaultReadParam();
            
	    Rectangle sourceRegion = new Rectangle();
	    int w = reader.getWidth(0);
	    int h = reader.getHeight(0);
	    int c = 1;
	    for (int y = 0; y < h; y += tileSize) {
		int th = Math.min(tileSize, h - y);
		sourceRegion.y = y;
		sourceRegion.height = th;
		for (int x = 0; x < w; x += tileSize) {
		    System.out.println("tile "+c);
		    File target = new File(targetPattern + Integer.toString(c) + ".png");
		    int tw = Math.min(tileSize, w - x);
		    sourceRegion.x = x;
		    sourceRegion.width = tw;
		    param.setSourceRegion(sourceRegion);
		    BufferedImage bi = reader.read(0, param);

		    ImageWriter writer=(ImageWriter)ImageIO.getImageWritersByFormatName("png").next();
		    writer.setOutput(ImageIO.createImageOutputStream(target));
		    writer.write(bi);
		    //bi.dispose();
		    writer.dispose();
		    c = c+1;
		    System.gc();
		}
	    }
	    reader.dispose();
	}
	catch (IOException ex){ex.printStackTrace();}
    }

    public static Image readImage(String filePath){
	try {
	    File source = new File(filePath);
	    ImageInputStream iis = ImageIO.createImageInputStream(source);
	    Iterator readers = ImageIO.getImageReadersByFormatName("png");
	    final ImageReader reader = (ImageReader)readers.next();
	    reader.setInput(iis, true);
	    final ImageReadParam param = reader.getDefaultReadParam();
	    BufferedImage bi = reader.read(0, param);
	    reader.dispose();
	    return bi;
	}
	catch (IOException ex){
	    System.err.println("Failed to load image "+filePath);
	    return null;
	}
    }

    public static void main(String[] args){
	Utils.tile(args[0], args[1], Integer.parseInt(args[2]));
    }

}
