/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package textbook;

/**
 *
 * @author Shawn
 */
public class ChapterNode extends Node {
    
    private final Chapter chapter;
    private final int fixedX, fixedY;
    
    public ChapterNode(Chapter chapter, int x, int y) {
        super(x, y);
        this.chapter = chapter;
        fixedX = x;
        fixedY = y;
    }
    
}
