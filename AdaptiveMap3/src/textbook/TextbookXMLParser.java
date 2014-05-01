/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package textbook;

import controller.xml.XmlParser;
import java.io.File;
import org.w3c.dom.Element;

/**
 *
 * @author Shawn
 */
public class TextbookXMLParser {
    
    private static final boolean EXIT_ON_ERROR = true;
    private static boolean encounteredException = false;
    
    
    public static Textbook parse(File file) {
        Textbook textbook = new Textbook();
        
        Element textbookElement = XmlParser.getNodeDocument(file);
        
        Chapter[] chapters = XmlParser.parseChapterNodes(textbookElement);
        for(Chapter chapter: chapters) {
            textbook.addChapter(chapter);
        }
        
        System.out.println(textbook.toString());
        return null;
    }
}
