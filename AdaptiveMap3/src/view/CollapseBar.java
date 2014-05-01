/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package view;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;

/**
 *
 * @author Shawn
 */
public class CollapseBar extends JPanel {
    
    private final ChapterList list;
    
    public CollapseBar(ChapterList list) {
        super();
        this.list = list;
        init();
        this.setPreferredSize(new Dimension(25, 50));
        //this.setMaximumSize(new Dimension(50, 100));
        this.setMinimumSize(new Dimension(50, 100));
        this.setBorder(new MatteBorder(2, 0, 2, 2, Color.gray));
    }

    private void init() {
        //this.setOpaque(false);
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e); 
                if(list.isCollapsed())
                    list.extend();
                else
                    list.collapse();
                
            }


        });
        //this.setText("NOM");
        //this.setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); //To change body of generated methods, choose Tools | Templates.
        g.setColor(Color.white);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        
        g.setColor(Color.gray);
        
        final int NUM_LINES = 5;
        // draw stripes
        for(double i = 0; i < NUM_LINES; i++) {
            int x = (int) (this.getWidth() * (i / NUM_LINES));
            g.drawLine(x, 0, x, this.getHeight());
        }
        
        
    }
        
    
    
}
