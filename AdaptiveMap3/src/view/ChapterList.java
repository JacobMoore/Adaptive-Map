/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import model.Node;

/**
 *
 * @author Shawn
 */
 public class ChapterList extends JList<Node> implements MouseListener, MouseMotionListener {

     private boolean collapsed = false;
     private final AppCanvas parent;
    private final CollapseBar bar;
    private Node lastHoveredNode;
    
    public ChapterList(Node[] nodes, AppCanvas canvas){
        super(nodes);
        this.parent = canvas;
        bar = new CollapseBar(this);
        init();
    }

    private void init() {
        
        Insets matteBorderInsets = new Insets(2, 2, 2, 2);
        MatteBorder matteBorder = new MatteBorder(matteBorderInsets, Color.black);
        Insets emptyBorderInsets = new Insets(5, 10, 5, 10);
        EmptyBorder emptyBorder = new EmptyBorder(emptyBorderInsets);
        CompoundBorder compoundBorder = BorderFactory.createCompoundBorder(matteBorder, emptyBorder);
        this.setBorder(compoundBorder);
        
        this.setCellRenderer(new ChapterListRenderer());
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    /*
    @Override
    public void mouseClicked(MouseEvent e) {
        Node selectedNode = this.getSelectedValue();
        parent.setSelectedNode(selectedNode);
        parent.selectMedButton();
        
        this.collapse();
    }*/
    
    public void collapse() {
        //new ChapterListMotion(this).run();
        Thread thread = new Thread(new ChapterListMotion(this));
        thread.start();
        collapsed = true;
    }

    public void extend() {
        Thread thread = new Thread(new ChapterListMotion(this));
        thread.start();
        collapsed = false;
    }
    
    public boolean isCollapsed() {
        return collapsed;
    }
    
    @Override
    public void mousePressed(MouseEvent e) { 
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Node selectedNode = this.getSelectedValue();
        parent.setSelectedNode(selectedNode);
        //parent.selectMedButton();
        
        this.collapse();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        repaint();
    }

    /*
    @Override
    public void mouseExited(MouseEvent e) {
        lastHoveredNode = null;
        if(this.getSelectedValue() != null) {
            parent.setSelectedNode(this.getSelectedValue());
            parent.switchToMidLevelView();
        } 
        repaint();
    }*/
    
    @Override
    public void mouseExited(MouseEvent e) { }

     @Override
    public void mouseClicked(MouseEvent e) {
        Node selectedNode = this.getSelectedValue();
        parent.setSelectedNode(selectedNode);
        parent.selectMedButton();
        
        this.collapse();
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {}

    /*
    @Override
    public void mouseMoved(MouseEvent e) {
        int index = this.locationToIndex(e.getPoint());
         final Node node = this.getModel().getElementAt(index);
         if(lastHoveredNode != node) {
            lastHoveredNode = node;
            parent.setSelectedNode(node);
            parent.selectMedButton();
         }
    }*/

    @Override
    public void mouseMoved(MouseEvent e) {
        repaint();
    }
    
    public void setAppropriateBounds() {
        Dimension pref = this.getPreferredSize();
        Dimension barpref = this.getCollapseBar().getPreferredSize();
        if(collapsed) {
            this.getParent().setBounds(-pref.width, 0, pref.width + barpref.width , this.getParent().getParent().getHeight());
        } else {
            this.getParent().setBounds(0, 0, pref.width + barpref.width , this.getParent().getParent().getHeight());
        }
    }

    public CollapseBar getCollapseBar() {
        return bar;
    }

    
    private class ChapterListRenderer implements ListCellRenderer<Node> {

        public ChapterListRenderer() {
            super();
        }
        
        @Override
        public Component getListCellRendererComponent(JList<? extends Node> list, Node value, int index, boolean isSelected, boolean cellHasFocus) {
            //System.out.println("Rendered " + value.getNodeChapter());
            NodePanel nodePanel = new NodePanel(value);
            if(cellHasFocus)
                nodePanel.setBackground(Color.gray);
            else
                nodePanel.setBackground(Color.white);
            
            if(isSelected)
                nodePanel.setBackground(Color.gray.brighter());
            return nodePanel;
        }
        
        
    }
    
    private class NodePanel extends JPanel implements MouseListener {
            
        private final Node node;

        public NodePanel(Node node) {
            super(new BorderLayout());
            this.node = node;
            init();
            this.addMouseListener(this);
            
        }

        private void init() {
            Insets insets = new Insets(5, 5, 5, 5);;
            MatteBorder matteBorder = new MatteBorder(insets, node.getNodeChapterColor());
            EmptyBorder emptyBorder = new EmptyBorder(insets);
            CompoundBorder border = BorderFactory.createCompoundBorder(matteBorder, emptyBorder);
            this.setBorder(border);
            this.add(new JLabel(node.getNodeChapter()), BorderLayout.CENTER);
            this.setFocusable(true);
        }
        
        public Node getNode() {
            return node;
        }
        
             @Override
             public void mouseClicked(MouseEvent e) {
                Node selectedNode = getNode();
                parent.setSelectedNode(selectedNode);
                parent.selectMedButton();

                collapse();
             }

             @Override
             public void mousePressed(MouseEvent e) {
             }

             @Override
             public void mouseReleased(MouseEvent e) {
             }

             @Override
             public void mouseEntered(MouseEvent e) {
                 setBackground(Color.gray);
                 System.out.println("Hello");
                 repaint();
             }

             @Override
             public void mouseExited(MouseEvent e) {
                 System.out.println("Goodbye");
                 setBackground(Color.white);
                 repaint();
             }

        
        
    }
    
    
    private class ChapterListMotion implements Runnable {
        
        private final int COLLAPSE_INC = -10;
        private final int EXTEND_INC = 10;
        private final int FPS = 50;
        private final int MOVEMENT_SPEED = 20;
        
        private final ChapterList list;
        
        public ChapterListMotion(ChapterList list) {
            super();
            this.list = list;
        }
        
        public void run() {
            int rightBound  = -1;
            Timer updateTimer = new Timer(FPS, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Window window = SwingUtilities.getWindowAncestor(ChapterList.this);
                    window.revalidate();
                    
                }
            });
            updateTimer.start();
            if(!list.isCollapsed()) { // then extend
                do {
                    Rectangle bounds = ChapterList.this.getParent().getBounds();
                    ChapterList.this.getParent().setBounds(bounds.x + EXTEND_INC, 0, bounds.width, bounds.height);
                    rightBound = ChapterList.this.getParent().getX() + ChapterList.this.getParent().getWidth();
                    try {
                        Thread.sleep(MOVEMENT_SPEED);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ChapterList.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } while(rightBound < ChapterList.this.getParent().getWidth());
            } else { // then collpase
                do {
                    Rectangle bounds = ChapterList.this.getParent().getBounds();
                    ChapterList.this.getParent().setBounds(bounds.x + COLLAPSE_INC, 0, bounds.width, bounds.height);
                    rightBound = ChapterList.this.getParent().getX() + ChapterList.this.getParent().getWidth();
                    try {
                        Thread.sleep(MOVEMENT_SPEED);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ChapterList.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } while(list.getCollapseBar().getWidth() < rightBound);
            }
            
            updateTimer.stop();
        }
    }
}
