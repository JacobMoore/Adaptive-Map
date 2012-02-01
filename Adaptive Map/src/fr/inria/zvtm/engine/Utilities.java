/*   FILE: Utilities.java
 *   DATE OF CREATION:   Jan 09 2002
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004. All Rights Reserved
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
 * $Id: Utilities.java 2102 2009-06-23 08:57:56Z rprimet $
 */

package fr.inria.zvtm.engine;

import java.awt.Color;
import java.util.Hashtable;

import fr.inria.zvtm.glyphs.Glyph;

/**various misc utility methods*/

public class Utilities {

    public static final double TWO_PI = 2 * Math.PI;
    public static final double HALF_PI = Math.PI / 2.0;
    public static final double THREE_HALF_PI = 1.5 * Math.PI;

    private static Hashtable colorsByKeyword;

    /** 
     *get the Java AWT color which corresponds to a color keyword as defined in <a href="http://www.w3.org/TR/SVG/types.html#ColorKeywords">http://www.w3.org/TR/SVG/types.html#ColorKeywords</a>
     *@param keyword a color keyword name such as black, blue, lime, darkorchid, etc...
     */
    public static Color getColorByKeyword(String keyword){
        if (colorsByKeyword==null){
            //init table if this is the first time we access it
            colorsByKeyword=new Hashtable();
            colorsByKeyword.put("aliceblue",new Color(240,248,255));
            colorsByKeyword.put("antiquewhite",new Color(250,235,215));
            colorsByKeyword.put("aqua",new Color(0,255,255));
            colorsByKeyword.put("aquamarine",new Color(127,255,212));
            colorsByKeyword.put("azure",new Color(240,255,255));
            colorsByKeyword.put("beige",new Color(245,245,220));
            colorsByKeyword.put("bisque",new Color(255,228,196));
            colorsByKeyword.put("black",new Color(0,0,0));
            colorsByKeyword.put("blanchedalmond",new Color(255,235,205));
            colorsByKeyword.put("blue",new Color(0,0,255));
            colorsByKeyword.put("blueviolet",new Color(138,43,226));
            colorsByKeyword.put("brown",new Color(165,42,42));
            colorsByKeyword.put("burlywood",new Color(222,184,135));
            colorsByKeyword.put("cadetblue",new Color(95,158,160));
            colorsByKeyword.put("chartreuse",new Color(127,255,0));
            colorsByKeyword.put("chocolate",new Color(210,105,30));
            colorsByKeyword.put("coral",new Color(255,127,80));
            colorsByKeyword.put("cornflowerblue",new Color(100,149,237));
            colorsByKeyword.put("cornsilk",new Color(255,248,220));
            colorsByKeyword.put("crimson",new Color(220,20,60));
            colorsByKeyword.put("cyan",new Color(0,255,255));
            colorsByKeyword.put("darkblue",new Color(0,0,139));
            colorsByKeyword.put("darkcyan",new Color(0,139,139));
            colorsByKeyword.put("darkgoldenrod",new Color(184,134,11));
            colorsByKeyword.put("darkgray",new Color(169,169,169));
            colorsByKeyword.put("darkgreen",new Color(0,100,0));
            colorsByKeyword.put("darkgrey",new Color(169,169,169));
            colorsByKeyword.put("darkkhaki",new Color(189,183,107));
            colorsByKeyword.put("darkmagenta",new Color(139,0,139));
            colorsByKeyword.put("darkolivegreen",new Color(85,107,47));
            colorsByKeyword.put("darkorange",new Color(255,140,0));
            colorsByKeyword.put("darkorchid",new Color(153,50,204));
            colorsByKeyword.put("darkred",new Color(139,0,0));
            colorsByKeyword.put("darksalmon",new Color(233,150,122));
            colorsByKeyword.put("darkseagreen",new Color(143,188,143));
            colorsByKeyword.put("darkslateblue",new Color(72,61,139));
            colorsByKeyword.put("darkslategray",new Color(47,79,79));
            colorsByKeyword.put("darkslategrey",new Color(47,79,79));
            colorsByKeyword.put("darkturquoise",new Color(0,206,209));
            colorsByKeyword.put("darkviolet",new Color(148,0,211));
            colorsByKeyword.put("deeppink",new Color(255,20,147));
            colorsByKeyword.put("deepskyblue",new Color(0,191,255));
            colorsByKeyword.put("dimgray",new Color(105,105,105));
            colorsByKeyword.put("dimgrey",new Color(105,105,105));
            colorsByKeyword.put("dodgerblue",new Color(30,144,255));
            colorsByKeyword.put("firebrick",new Color(178,34,34));
            colorsByKeyword.put("floralwhite",new Color(255,250,240));
            colorsByKeyword.put("forestgreen",new Color(34,139,34));
            colorsByKeyword.put("fuchsia",new Color(255,0,255));
            colorsByKeyword.put("gainsboro",new Color(220,220,220));
            colorsByKeyword.put("ghostwhite",new Color(248,248,255));
            colorsByKeyword.put("gold",new Color(255,215,0));
            colorsByKeyword.put("goldenrod",new Color(218,165,32));
            colorsByKeyword.put("gray",new Color(128,128,128));
            colorsByKeyword.put("grey",new Color(128,128,128));
            colorsByKeyword.put("green",new Color(0,128,0));
            colorsByKeyword.put("greenyellow",new Color(173,255,47));
            colorsByKeyword.put("honeydew",new Color(240,255,240));
            colorsByKeyword.put("hotpink",new Color(255,105,180));
            colorsByKeyword.put("indianred",new Color(205,92,92));
            colorsByKeyword.put("indigo",new Color(75,0,130));
            colorsByKeyword.put("ivory",new Color(255,255,240));
            colorsByKeyword.put("khaki",new Color(240,230,140));
            colorsByKeyword.put("lavender",new Color(230,230,250));
            colorsByKeyword.put("lavenderblush",new Color(255,240,245));
            colorsByKeyword.put("lawngreen",new Color(124,252,0));
            colorsByKeyword.put("lemonchiffon",new Color(255,250,205));
            colorsByKeyword.put("lightblue",new Color(173,216,230));
            colorsByKeyword.put("lightcoral",new Color(240,128,128));
            colorsByKeyword.put("lightcyan",new Color(224,255,255));
            colorsByKeyword.put("lightgoldenrodyellow",new Color(250,250,210));
            colorsByKeyword.put("lightgray",new Color(211,211,211));
            colorsByKeyword.put("lightgreen",new Color(144,238,144));
            colorsByKeyword.put("lightgrey",new Color(211,211,211));
            colorsByKeyword.put("lightpink",new Color(255,182,193));
            colorsByKeyword.put("lightsalmon",new Color(255,160,122));
            colorsByKeyword.put("lightseagreen",new Color(32,178,170));
            colorsByKeyword.put("lightskyblue",new Color(135,206,250));
            colorsByKeyword.put("lightslategray",new Color(119,136,153));
            colorsByKeyword.put("lightslategrey",new Color(119,136,153));
            colorsByKeyword.put("lightsteelblue",new Color(176,196,222));
            colorsByKeyword.put("lightyellow",new Color(255,255,224));
            colorsByKeyword.put("lime",new Color(0,255,0));
            colorsByKeyword.put("limegreen",new Color(50,205,50));
            colorsByKeyword.put("linen",new Color(250,240,230));
            colorsByKeyword.put("magenta",new Color(255,0,255));
            colorsByKeyword.put("maroon",new Color(128,0,0));
            colorsByKeyword.put("mediumaquamarine",new Color(102,205,170));
            colorsByKeyword.put("mediumblue",new Color(0,0,205));
            colorsByKeyword.put("mediumorchid",new Color(186,85,211));
            colorsByKeyword.put("mediumpurple",new Color(147,112,219));
            colorsByKeyword.put("mediumseagreen",new Color(60,179,113));
            colorsByKeyword.put("mediumslateblue",new Color(123,104,238));
            colorsByKeyword.put("mediumspringgreen",new Color(0,250,154));
            colorsByKeyword.put("mediumturquoise",new Color(72,209,204));
            colorsByKeyword.put("mediumvioletred",new Color(199,21,133));
            colorsByKeyword.put("midnightblue",new Color(25,25,112));
            colorsByKeyword.put("mintcream",new Color(245,255,250));
            colorsByKeyword.put("mistyrose",new Color(255,228,225));
            colorsByKeyword.put("moccasin",new Color(255,228,181));
            colorsByKeyword.put("navajowhite",new Color(255,222,173));
            colorsByKeyword.put("navy",new Color(0,0,128));
            colorsByKeyword.put("oldlace",new Color(253,245,230));
            colorsByKeyword.put("olive",new Color(128,128,0));
            colorsByKeyword.put("olivedrab",new Color(107,142,35));
            colorsByKeyword.put("orange",new Color(255,165,0));
            colorsByKeyword.put("orangered",new Color(255,69,0));
            colorsByKeyword.put("orchid",new Color(218,112,214));
            colorsByKeyword.put("palegoldenrod",new Color(238,232,170));
            colorsByKeyword.put("palegreen",new Color(152,251,152));
            colorsByKeyword.put("paleturquoise",new Color(175,238,238));
            colorsByKeyword.put("palevioletred",new Color(219,112,147));
            colorsByKeyword.put("papayawhip",new Color(255,239,213));
            colorsByKeyword.put("peachpuff",new Color(255,218,185));
            colorsByKeyword.put("peru",new Color(205,133,63));
            colorsByKeyword.put("pink",new Color(255,192,203));
            colorsByKeyword.put("plum",new Color(221,160,221));
            colorsByKeyword.put("powderblue",new Color(176,224,230));
            colorsByKeyword.put("purple",new Color(128,0,128));
            colorsByKeyword.put("red",new Color(255,0,0));
            colorsByKeyword.put("rosybrown",new Color(188,143,143));
            colorsByKeyword.put("royalblue",new Color(65,105,225));
            colorsByKeyword.put("saddlebrown",new Color(139,69,19));
            colorsByKeyword.put("salmon",new Color(250,128,114));
            colorsByKeyword.put("sandybrown",new Color(244,164,96));
            colorsByKeyword.put("seagreen",new Color(46,139,87));
            colorsByKeyword.put("seashell",new Color(255,245,238));
            colorsByKeyword.put("sienna",new Color(160,82,45));
            colorsByKeyword.put("silver",new Color(192,192,192));
            colorsByKeyword.put("skyblue",new Color(135,206,235));
            colorsByKeyword.put("slateblue",new Color(106,90,205));
            colorsByKeyword.put("slategray",new Color(112,128,144));
            colorsByKeyword.put("slategrey",new Color(112,128,144));
            colorsByKeyword.put("snow",new Color(255,250,250));
            colorsByKeyword.put("springgreen",new Color(0,255,127));
            colorsByKeyword.put("steelblue",new Color(70,130,180));
            colorsByKeyword.put("tan",new Color(210,180,140));
            colorsByKeyword.put("teal",new Color(0,128,128));
            colorsByKeyword.put("thistle",new Color(216,191,216));
            colorsByKeyword.put("tomato",new Color(255,99,71));
            colorsByKeyword.put("turquoise",new Color(64,224,208));
            colorsByKeyword.put("violet",new Color(238,130,238));
            colorsByKeyword.put("wheat",new Color(245,222,179));
            colorsByKeyword.put("white",new Color(255,255,255));
            colorsByKeyword.put("whitesmoke",new Color(245,245,245));
            colorsByKeyword.put("yellow",new Color(255,255,0));
            colorsByKeyword.put("yellowgreen",new Color(154,205,50));
        }
        if (keyword != null){
            // see above -- all keys are lowercase
            keyword = keyword.toLowerCase();
        }
        return (Color)colorsByKeyword.get(keyword);
    }

    /*takes an array of floats and returns a single string containing all values separated by commas*/
    public static String arrayOffloatAsCSStrings(float[] ar){
	String res="";
	for (int i=0;i<ar.length-1;i++){
	    res+=Float.toString(ar[i])+",";
	}
	res+=Float.toString(ar[ar.length-1]);
	return res;
    }

    /**
     * tells wheter the current JVM is version 1.4.0 and later (or not)
     */
    public static boolean javaVersionIs140OrLater(){
	String version=System.getProperty("java.vm.version");
	float numVer=(new Float(version.substring(0,3))).floatValue();
	if (numVer>=1.4f){return true;}
	else {return false;}
    }

    private static boolean osIsMacOSX = false;
    private static boolean osIsWindows = false;
    static {
	if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")){osIsMacOSX = true;}
	if (System.getProperty("os.name").toLowerCase().startsWith("windows")){osIsWindows = true;}
    }

    /**
     * tells wheter the underlying OS is Mac OS X or not
     */
    public static boolean osIsMacOS(){
	return osIsMacOSX;
    }

    /**
     * tells whether the underlying OS is Windows(Win32) or not
     */
    public static boolean osIsWindows(){
	return osIsWindows;
    }

    /**
     *Replaces all occurences of key in input by replacement
     */
    public static String replaceString(String input, String key, String replacement) {
        String res="";
        int keyLength=key.length();
        int index=input.indexOf(key);
        int lastIndex=0;
        while (index>=0) {
            res=res+input.substring(lastIndex,index)+replacement;
            lastIndex=index+keyLength;
            index=input.indexOf(key,lastIndex);
        }
	res+=input.substring(lastIndex,input.length());
        return res;
    }

    /**
     *@param sb a StringBuffer from which leading whitespaces should be removed
     */
    public static void delLeadingSpaces(StringBuffer sb){
	while ((sb.length()>0) && (Character.isWhitespace(sb.charAt(0)))){
	    sb.deleteCharAt(0);
	}
    }

	/** Checks whether Glyph array ga contains glyph g or not.
		*/
	public static boolean containsGlyph(Glyph[] ga, Glyph g){
		return containsGlyph(ga, g, ga.length);
	}


	/** Checks whether Glyph array ga contains glyph g or not within its first N items.
		*@param maxIndex look for g in items in range [0,maxIndex[
		*/
	public static boolean containsGlyph(Glyph[] ga, Glyph g, int maxIndex){	
		for (int i=0;i<maxIndex;i++){
			if (ga[i] == g){return true;}
		}
		return false;
	}

	/** Checks whether Glyph array ga contains glyph g or not and returns its index.
		*/
	public static int indexOfGlyph(Glyph[] ga, Glyph g){
		return indexOfGlyph(ga, g, ga.length);
	}
	
	/** Checks whether Glyph array ga contains glyph g or not within its first N items, and returns its index.
		*@param maxIndex look for g in items in range [0,maxIndex[
		*/
	public static int indexOfGlyph(Glyph[] ga, Glyph g, int maxIndex){
		for (int i=0;i<maxIndex;i++){
			if (ga[i] == g){return i;}
		}
		return -1;
	}

}
