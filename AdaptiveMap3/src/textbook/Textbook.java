/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package textbook;

import java.util.ArrayList;

/**
 *
 * @author Shawn
 */
public class Textbook {
    
    private final ArrayList<Chapter> chapters;
    
    public Textbook() {
        chapters = new ArrayList();
    }
    
    public void addChapter(Chapter chapter) {
        chapters.add(chapter);
    }
    
    public Chapter[] getChapters() {
        return chapters.toArray(new Chapter[chapters.size()]);
    }
    
    public String toString() {
        return chapters.toString();
    }
}
