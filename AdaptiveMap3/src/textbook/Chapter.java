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
public class Chapter {
    
    private ArrayList<Topic> topics;
    
    private final String title;
    private final String description;
    
    public Chapter(String title, String description) {
        super();
        this.title = title;
        this.description = description;
        topics = new ArrayList();
    }
    
    public void addTopic(Topic topic) {
        topics.add(topic);
    }
    
}
