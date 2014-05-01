/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package textbook;

import fr.inria.zvtm.glyphs.VRectangle;
import java.awt.Color;

/**
 *
 * @author Shawn
 */
public class Node extends VRectangle {
    
    private int x, y;
    private int width, height;
    private Color color;

    public Node(int x, int y, int width, int height, Color color) {
        super(x, y, 1, width, height, color);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
    }
    
    
}
