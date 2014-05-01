/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package textbook;

import java.util.ArrayList;
import model.Link;

/**
 *
 * @author Shawn
 */
public class Topic {
    
    private final String title;
    private final String description;
    
    private final ArrayList<Link> links;
    
    public Topic(String title, String description) {
        this.title = title;
        this.description = description; 
        links = new ArrayList();
    }
    
    public void addLink(Link link) {
        links.add(link);
    }
    
    public Link[] getLinks() {
        return links.toArray(new Link[links.size()]);
    }
}
