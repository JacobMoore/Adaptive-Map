package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;

import controller.EditorXMLParser;
import controller.EditorXMLParser.ChapterData;
import controller.EditorXMLParser.Link;
import controller.EditorXMLParser.LinkData;
import controller.EditorXMLParser.NodeData;


public class EditorSplitPane extends JSplitPane {
	private static final long serialVersionUID = 2822456245621846586L;
	private JFrame mainFrame;
	JPanel nodePanel, chapterPanel, linkPanel;
	
	private DefaultListModel<String> nodeListModel;
	private JList<String> nodeInfoList;
	private DefaultListModel<String> chapterListModel;
	private JList<String> chapterInfoList;
	private DefaultListModel<String> linkListModel;
	private JList<String> linkInfoList;

	public EditorSplitPane(int horizontalSplit, boolean b, JFrame frame)
	{
		super(horizontalSplit, b);
		mainFrame = frame;
		
		JPanel rightPanel = new JPanel();
		rightPanel.setBackground(Color.BLUE.darker().darker());
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		
		JTabbedPane rightTabbedPane = makeInfoComponents();
		rightPanel.add(rightTabbedPane);
		setRightComponent(rightPanel);
		
		JPanel leftPanel = new JPanel();
		leftPanel.setBackground(Color.BLUE.darker().darker());
		
		JTabbedPane tabbedPane = new JTabbedPane();

		nodePanel = new JPanel();
		makeNodeComponents(nodePanel);
		JScrollPane nodeScroller = new JScrollPane(nodePanel);
		nodeScroller.setPreferredSize(new Dimension(350, 400 ));
		nodeScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		nodeScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		tabbedPane.addTab("Create New Node", nodeScroller);

		chapterPanel = new JPanel();
		chapterPanel.setPreferredSize(new Dimension(350, 400));
		makeChapterComponents(chapterPanel);
		tabbedPane.addTab("Create New Chapter", chapterPanel);

		linkPanel = new JPanel();
		linkPanel.setPreferredSize(new Dimension(350, 400));
		makeLinkComponents(linkPanel);
		tabbedPane.addTab("Create New Link Type", linkPanel);
		
		leftPanel.add(tabbedPane);
		setLeftComponent(leftPanel);
	}
	
	public void refreshNodes(LinkedList<NodeData> nodes)
	{
		nodeListModel = new DefaultListModel<String>();
		if ( nodes.isEmpty() )
			nodeListModel.addElement("\tEmpty");
		else
		{
			for ( NodeData n : nodes )
				nodeListModel.addElement( n.nodeChapter + " | " + n.nodeTitle);
		}
		Object[] data = nodeListModel.toArray();
		java.util.Arrays.sort(data);
		nodeListModel = new DefaultListModel<String>();
		for ( int i=0; i < data.length; i++ )
			nodeListModel.addElement((String)data[i]);
		nodeInfoList.setModel(nodeListModel);
		chapterPanel.removeAll();
		makeChapterComponents(chapterPanel);
	}

	public void refreshChapters(LinkedList<ChapterData> chapters)
	{
		chapterListModel = new DefaultListModel<String>();
		if ( chapters.isEmpty() )
			chapterListModel.addElement("\tEmpty");
		else
		{
			for ( ChapterData c : chapters )
			{
				String title = c.chapterTitle.toString();
				if (c.isDefaultChapter)
					title += "  |  DEFAULT";
				chapterListModel.addElement(title);
			}
		}
		Object[] data = chapterListModel.toArray();
		java.util.Arrays.sort(data);
		chapterListModel = new DefaultListModel<String>();
		for ( int i=0; i < data.length; i++ )
			chapterListModel.addElement((String)data[i]);
		chapterInfoList.setModel(chapterListModel);
	}
	
	public void refreshLinks(LinkedList<LinkData> links)
	{
		linkListModel = new DefaultListModel<String>();
		if ( links.isEmpty() )
			linkListModel.addElement("\tEmpty");
		else
		{
			for ( LinkData l : links )
				linkListModel.addElement(l.linkTitle);
		}
		Object[] data = linkListModel.toArray();
		java.util.Arrays.sort(data);
		linkListModel = new DefaultListModel<String>();
		for ( int i=0; i < data.length; i++ )
			linkListModel.addElement((String)data[i]);
		linkInfoList.setModel(linkListModel);
	}
	
	private JTabbedPane makeInfoComponents()
	{
		JTabbedPane pane = new JTabbedPane();
		
		// Node
		JPanel nodeInfoPanel = new JPanel();
		nodeListModel = new DefaultListModel<String>();
		nodeListModel.addElement("\tEmpty");
		nodeInfoList = new JList<String>(nodeListModel);
		nodeInfoList.setBorder(new LineBorder(Color.BLACK));
		nodeInfoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		nodeInfoList.setLayoutOrientation(JList.VERTICAL);
		nodeInfoList.setVisibleRowCount(-1);
		nodeInfoList.addMouseListener(new NodeInfoListener());
		JScrollPane nodeListScroller = new JScrollPane(nodeInfoList);
		nodeListScroller.setPreferredSize(new Dimension(380, 400));
		nodeInfoPanel.add(nodeListScroller);
		pane.addTab("Existing Nodes", nodeInfoPanel);
		
		// Chapter
		JPanel chapterInfoPanel = new JPanel();
		chapterListModel = new DefaultListModel<String>();
		chapterListModel.addElement("\tEmpty");
		chapterInfoList = new JList<String>(chapterListModel);
		chapterInfoList.setBorder(new LineBorder(Color.BLACK));
		chapterInfoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		chapterInfoList.setLayoutOrientation(JList.VERTICAL);
		chapterInfoList.setVisibleRowCount(-1);
		chapterInfoList.addMouseListener(new ChapterInfoListener());
		JScrollPane chapterListScroller = new JScrollPane(chapterInfoList);
		chapterListScroller.setPreferredSize(new Dimension(380, 400));
		chapterInfoPanel.add(chapterListScroller);
		pane.addTab("Existing Chapters", chapterInfoPanel);
		
		// Link
		JPanel linkInfoPanel = new JPanel();
		linkListModel = new DefaultListModel<String>();
		linkListModel.addElement("\tEmpty");
		linkInfoList = new JList<String>(linkListModel);
		linkInfoList.setBorder(new LineBorder(Color.BLACK));
		linkInfoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		linkInfoList.setLayoutOrientation(JList.VERTICAL);
		linkInfoList.setVisibleRowCount(-1);
		linkInfoList.addMouseListener(new LinkInfoListener());
		JScrollPane linkListScroller = new JScrollPane(linkInfoList);
		linkListScroller.setPreferredSize(new Dimension(380, 400));
		linkInfoPanel.add(linkListScroller);
		pane.addTab("Existing Link Types", linkInfoPanel);
		
		return pane;
	}
	
	private class NodeInfoListener implements MouseListener
	{
	    public void mouseClicked(MouseEvent evt) {
	        if (evt.getClickCount() == 2) {
	            String[] splitString = nodeListModel.get(nodeInfoList.getSelectedIndex())
	            		.split(" \\| ");

	            for( NodeData n : EditorXMLParser.nodeData )
	            {
	            	if ( n.nodeChapter.equals(splitString[0]) && 
	            			n.nodeTitle.equals(splitString[1]) )
	            	{
	            		openNodeInfoDialog(n);
	            		return;
	            	}
	            }
	        }
	    }
		@Override public void mouseEntered(MouseEvent e) {}
		@Override public void mouseExited(MouseEvent e) {}
		@Override public void mousePressed(MouseEvent e) {}
		@Override public void mouseReleased(MouseEvent e) {}
	}

	private ArrayList<JPanel> nodeInfoLinksList;
	private int nodeInfoLinksNumber;
	
	private void openNodeInfoDialog(NodeData node)
	{
		final JPanel dialogPanel = new JPanel();
		
		JPanel titlePanel = new JPanel();
		JLabel title = new JLabel("Node Title");
		titlePanel.add(title);
		JTextField titleIn = new JTextField(30);
		titleIn.setText(node.nodeTitle);
		titlePanel.add(titleIn);
		dialogPanel.add(titlePanel);
		
		JPanel descriptionPanel = new JPanel();
		JLabel description = new JLabel("Node Description");
		descriptionPanel.add(description);
		JTextField descriptionIn = new JTextField(25);
		descriptionIn.setText(node.nodeDescription);
		descriptionPanel.add(descriptionIn);
		dialogPanel.add(descriptionPanel);
		
		JPanel chapterPanel = new JPanel();
		JLabel chapter = new JLabel("Node Chapter");
		chapterPanel.add(chapter);
		final JTextField chapterIn = new JTextField(20);
		chapterIn.addKeyListener(new KeyListener(){
			@Override public void keyPressed(KeyEvent arg0) {}
			@Override public void keyReleased(KeyEvent arg0) {}
			@Override
			public void keyTyped(KeyEvent e) {
					String text = chapterIn.getText();
					if (e.getKeyChar() != '\b' )
						text += e.getKeyChar();
				for ( ChapterData c : EditorXMLParser.chapterData )
				{
					if (c.chapterTitle.equals(text) )
					{
						chapterIn.setForeground(Color.black);
						return;
					}
				}
				chapterIn.setForeground(Color.red);
			}
		});
		chapterIn.setText(node.nodeChapter);
		chapterPanel.add(chapterIn);
		dialogPanel.add(chapterPanel);
		
		JPanel pagePanel = new JPanel();
		JLabel page = new JLabel("Node Webpage");
		pagePanel.add(page);
		JTextField pageIn = new JTextField(27);
		pageIn.setText(node.nodeWebpage);
		pageIn.setToolTipText("Address should be in the form: \"websites/chapterfolder/page.html\".");
		pagePanel.add(pageIn);
		dialogPanel.add(pagePanel);
		
		JPanel toolsPanel = new JPanel();

		JButton nodeAddLinksButton = new JButton("Add New Link");
		nodeAddLinksButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JPanel linksPanel = new JPanel();
				JLabel linkLabel = new JLabel("link " + (nodeInfoLinksNumber+1));
				linkLabel.setName("link" + (nodeInfoLinksNumber+1));
				linksPanel.add(linkLabel);
				
				final JTextField linkIn = new JTextField(10);
				linkIn.setName("linkIn" + (nodeInfoLinksNumber+1));
				linkIn.addKeyListener(new KeyListener(){
					@Override public void keyPressed(KeyEvent arg0) {}
					@Override public void keyReleased(KeyEvent arg0) {}
					@Override
					public void keyTyped(KeyEvent e) {
						String text = linkIn.getText();
						if (e.getKeyChar() != '\b' )
							text += e.getKeyChar();
						for ( NodeData n : EditorXMLParser.nodeData )
						{
							if (n.nodeTitle.equals(text) )
							{
								linkIn.setForeground(Color.black);
								return;
							}
						}
						linkIn.setForeground(Color.red);
					}
				});
				linksPanel.add(linkIn);
				
				JLabel linkTypeLabel = new JLabel("link " + (nodeInfoLinksNumber+1) + " Type");
				linkTypeLabel.setName("linkType" + (nodeInfoLinksNumber+1));
				linksPanel.add(linkTypeLabel);
				
				JComboBox<String> linkTypeIn = new JComboBox<String>();
				linkTypeIn.setName("linkTypeIn" + (nodeInfoLinksNumber+1));
				for ( LinkData links : EditorXMLParser.linkData)
					linkTypeIn.addItem(links.linkTitle);			
				linksPanel.add(linkTypeIn);
				nodeInfoLinksList.add(linksPanel);
				dialogPanel.add(linksPanel);
				nodeInfoLinksNumber++;
				dialogPanel.repaint();
				dialogPanel.getParent().validate();
				dialogPanel.getParent().repaint();
			}
		});
		toolsPanel.add(nodeAddLinksButton);
		JButton nodeRemoveLinkButton = new JButton("Remove Link");
		nodeRemoveLinkButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if ( nodeInfoLinksNumber > 0)
				{
					JPanel p = nodeInfoLinksList.remove(nodeInfoLinksNumber-1);
					dialogPanel.remove(p);
					nodeInfoLinksNumber--;
					dialogPanel.repaint();
				}
			}});
		toolsPanel.add(nodeRemoveLinkButton);
		dialogPanel.add(toolsPanel);
		
		nodeInfoLinksNumber = 0;
		nodeInfoLinksList = new ArrayList<JPanel>();
		
		for ( Link l : node.linkList )
		{
			JPanel linksPanel = new JPanel();
			JLabel linkLabel = new JLabel("link " + (nodeInfoLinksNumber+1));
			linkLabel.setName("link" + (nodeInfoLinksNumber+1));
			linksPanel.add(linkLabel);
			
			final JTextField linkIn = new JTextField(10);
			linkIn.setName("linkIn" + (nodeInfoLinksNumber+1));
			linkIn.setText(l.linkedNode);
			linkIn.addKeyListener(new KeyListener(){
				@Override public void keyPressed(KeyEvent arg0) {}
				@Override public void keyReleased(KeyEvent arg0) {}
				@Override
				public void keyTyped(KeyEvent e) {
						String text = linkIn.getText();
						if (e.getKeyChar() != '\b' )
							text += e.getKeyChar();
					for ( NodeData n : EditorXMLParser.nodeData )
					{
						if (n.nodeTitle.equals(text) )
						{
							linkIn.setForeground(Color.black);
							return;
						}
					}
					linkIn.setForeground(Color.red);
				}
			});
			linksPanel.add(linkIn);
			
			JLabel linkTypeLabel = new JLabel("link " + (nodeInfoLinksNumber+1) + " Type");
			linkTypeLabel.setName("linkType" + (nodeInfoLinksNumber+1));
			linksPanel.add(linkTypeLabel);
			
			JComboBox<String> linkTypeIn = new JComboBox<String>();
			linkTypeIn.setName("linkTypeIn" + (nodeInfoLinksNumber+1));
			for ( LinkData links : EditorXMLParser.linkData)
				linkTypeIn.addItem(links.linkTitle);
			boolean isExistingLink = false;
			for ( int i=0; i < linkTypeIn.getItemCount(); i++)
			{
				if ( linkTypeIn.getItemAt(i).equals(l.linkType)) {
					linkTypeIn.setSelectedIndex(i);
					break;
				}
			}
			if ( !isExistingLink )
			{
				linkTypeIn.addItem(l.linkType);
				linkTypeIn.setSelectedIndex(linkTypeIn.getItemCount()-1);
				//TODO: Warning icon for incorrect link
			}

			linksPanel.add(linkTypeIn);
			nodeInfoLinksList.add(linksPanel);
			dialogPanel.add(linksPanel);
			nodeInfoLinksNumber++;
		}
		dialogPanel.setPreferredSize(new Dimension(400, 300 + nodeInfoLinksNumber * 30));
		
		JScrollPane nodeInfoScroller = new JScrollPane(dialogPanel);
		nodeInfoScroller.setPreferredSize(new Dimension(410, 310 ));
		nodeInfoScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		nodeInfoScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
        Object[] options = { "OK", "DELETE NODE", "CANCEL" };
        int selection = JOptionPane.showOptionDialog(
            mainFrame, nodeInfoScroller, "Edit Selected Node",
            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
            null, options, options[0] );
        
        if ( selection == 0 ) {
        	node.nodeTitle = titleIn.getText();
        	node.nodeChapter = chapterIn.getText();
        	node.nodeDescription = descriptionIn.getText();
        	node.nodeWebpage = pageIn.getText();
        	node.linkList.clear();
			for ( JPanel i : nodeInfoLinksList )
			{
				String name = ((JTextField)i.getComponent(1)).getText();
				String type = (String) ((JComboBox<String>)i.getComponent(3)).getSelectedItem();
				node.linkList.add(new Link(name, type));
			}
        	refreshNodes(EditorXMLParser.nodeData);
        }
        else if ( selection == 1 )
        {
        	JLabel warning = new JLabel("Are you sure you want to delete this node?");
            Object[] deleteOptions = { "OK", "CANCEL" };
            int deleteSelection = JOptionPane.showOptionDialog(
                mainFrame, warning, "Delete Node",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, deleteOptions, deleteOptions[0] );
            if ( deleteSelection == 0 )
            {
            	EditorXMLParser.nodeData.remove(node);
            	refreshNodes(EditorXMLParser.nodeData);
            }
        }
	}
	
	private class ChapterInfoListener implements MouseListener
	{
	    public void mouseClicked(MouseEvent evt) {
	        if (evt.getClickCount() == 2) {
	            String[] splitString = chapterListModel.get(chapterInfoList.getSelectedIndex())
	            		.split(" \\| ");
	            for( ChapterData c : EditorXMLParser.chapterData )
	            {
	            	if ( c.chapterTitle.equals(splitString[0]) )
	            	{
	            		openChapterInfoDialog(c);
	            		return;
	            	}
	            }
	        }
	    }
		@Override public void mouseEntered(MouseEvent e) {}
		@Override public void mouseExited(MouseEvent e) {}
		@Override public void mousePressed(MouseEvent e) {}
		@Override public void mouseReleased(MouseEvent e) {}
	}
	
	private void openChapterInfoDialog(ChapterData chapter)
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setPreferredSize(new Dimension(400, 250));

		JPanel titlePanel = new JPanel();
		JLabel title = new JLabel("Chapter Title");
		titlePanel.add(title);
		JTextField chapterTitleIn = new JTextField(27);
		chapterTitleIn.setText(chapter.chapterTitle);
		titlePanel.add(chapterTitleIn);
		dialogPanel.add(titlePanel);
		
		JPanel descriptionPanel = new JPanel();
		JLabel description = new JLabel("Chapter Description");
		descriptionPanel.add(description);
		JTextField chapterDescriptionIn = new JTextField(25);
		chapterDescriptionIn.setText(chapter.description);
		descriptionPanel.add(chapterDescriptionIn);
		dialogPanel.add(descriptionPanel);
		
		JPanel colorPanel = new JPanel();
		JLabel colorLabel = new JLabel("Chapter Color");
		colorPanel.add(colorLabel);
		JComboBox<String> chapterColorBoxIn = new JComboBox<String>();
		for ( String color : EditorXMLParser.getChapterColors())
			chapterColorBoxIn.addItem(color);
		
		chapterColorBoxIn.setSelectedItem(chapter.chapterColor);
		colorPanel.add(chapterColorBoxIn);
		dialogPanel.add(colorPanel);
		
		JPanel defaultPanel = new JPanel();
		JLabel defaultN = new JLabel("Default Node");
		defaultPanel.add(defaultN);
		final JComboBox<String>  defaultIn = new JComboBox<String>();
		for ( NodeData n : EditorXMLParser.nodeData)
		{
			defaultIn.addItem(n.nodeTitle);
			if (chapter.defaultNode.equals(n.nodeTitle))
				defaultIn.setSelectedItem(n.nodeTitle);
		}
		if ( defaultIn.getItemCount() < 1 )
		{
			defaultIn.addItem("EMPTY");
		}
		defaultIn.setPreferredSize(new Dimension(300, 25));
		defaultPanel.add(defaultIn);
		dialogPanel.add(defaultPanel);
		
		boolean canSetAsDefault = true;
		for ( ChapterData chapters : EditorXMLParser.chapterData)
		{
			if ( chapters.isDefaultChapter && !chapters.equals(chapter) )
				canSetAsDefault = false;
		}
		JPanel defaultChapterPanel = new JPanel();
		JLabel defaultChapter = new JLabel("Is Default Chapter");
		defaultChapterPanel.add(defaultChapter);
		JRadioButton defaultChapterIn = new JRadioButton();
		defaultChapterIn.setSelected(chapter.isDefaultChapter);
		defaultChapterIn.setEnabled(canSetAsDefault);
		defaultChapterPanel.add(defaultChapterIn);
		dialogPanel.add(defaultChapterPanel);
		
		JPanel xPanel = new JPanel();
		JLabel xValue = new JLabel("Fixed X Position");
		xPanel.add(xValue);
		JSpinner xValueIn = new JSpinner();
		xValueIn.setPreferredSize(new Dimension(50, 24));
		xValueIn.setValue(chapter.fixedXPosition);
		xPanel.add(xValueIn);
		dialogPanel.add(xPanel);
		
		JPanel yPanel = new JPanel();
		JLabel yValue = new JLabel("Fixed Y Position");
		yPanel.add(yValue);
		JSpinner yValueIn = new JSpinner();
		yValueIn.setPreferredSize(new Dimension(50, 24));
		yValueIn.setValue(chapter.fixedYPosition);
		yPanel.add(yValueIn);
		dialogPanel.add(yPanel);
		
        Object[] options = { "OK", "REMOVE CHAPTER", "CANCEL" };
        int selection = JOptionPane.showOptionDialog(
            null, dialogPanel, "Edit Selected Chapter",
            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
            null, options, options[0] );
        
        if ( selection == 0 ) {
        	chapter.chapterColor = (String)chapterColorBoxIn.getSelectedItem();
        	chapter.chapterTitle = chapterTitleIn.getText();
        	chapter.defaultNode = (String)defaultIn.getSelectedItem();
        	chapter.description = chapterDescriptionIn.getText();
        	chapter.fixedXPosition = (int)xValueIn.getValue();
        	chapter.fixedYPosition = (int)yValueIn.getValue();
        	chapter.isDefaultChapter = defaultChapterIn.isSelected();
        	refreshChapters(EditorXMLParser.chapterData);
        }
        else if ( selection == 1 )
        {
        	JLabel warning = new JLabel("Are you sure you want to delete this chapter?");
            Object[] deleteOptions = { "OK", "CANCEL" };
            int deleteSelection = JOptionPane.showOptionDialog(
                mainFrame, warning, "Delete Chapter",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, deleteOptions, deleteOptions[0] );
            if ( deleteSelection == 0 )
            {
            	EditorXMLParser.chapterData.remove(chapter);
        		refreshChapters(EditorXMLParser.chapterData);
            }
        }
	}
	
	
	private class LinkInfoListener implements MouseListener
	{
	    public void mouseClicked(MouseEvent evt) {
	        if (evt.getClickCount() == 2) {;
	            for( LinkData l : EditorXMLParser.linkData )
	            {
	            	if ( l.linkTitle.equals(linkListModel.get(linkInfoList.getSelectedIndex())) )
	            	{
	            		openLinkInfoDialog(l);
	            		return;
	            	}
	            }
	        }
	    }
		@Override public void mouseEntered(MouseEvent e) {}
		@Override public void mouseExited(MouseEvent e) {}
		@Override public void mousePressed(MouseEvent e) {}
		@Override public void mouseReleased(MouseEvent e) {}
	}
	
	private void openLinkInfoDialog(LinkData link)
	{
		JPanel dialogPanel = new JPanel();
		dialogPanel.setPreferredSize(new Dimension(400, 100));
		
		JPanel titlePanel = new JPanel();
		JLabel title = new JLabel("Link Title");
		titlePanel.add(title);
		JTextField linkTitleIn = new JTextField(20);
		linkTitleIn.setText(link.linkTitle);
		titlePanel.add(linkTitleIn);
		dialogPanel.add(titlePanel);
		
		JPanel descriptionPanel = new JPanel();
		JLabel description = new JLabel("Link Description");
		descriptionPanel.add(description);
		JTextField linkDescriptionIn = new JTextField(26);
		linkDescriptionIn.setText(link.description);
		descriptionPanel.add(linkDescriptionIn);
		dialogPanel.add(descriptionPanel);
		
		/*JPanel typePanel = new JPanel();
		JLabel type = new JLabel("Link Line Type");
		typePanel.add(type);
		JTextField linkLineTypeIn = new JTextField(20);
		linkLineTypeIn.setText(link.linkLineType);
		typePanel.add(linkLineTypeIn);
		dialogPanel.add(typePanel);*/
		
        Object[] options = { "OK", "REMOVE LINK", "CANCEL" };
        int selection = JOptionPane.showOptionDialog(
            null, dialogPanel, "Edit Selected Node",
            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
            null, options, options[0] );
        
        if ( selection == 0 ) {
        	link.linkTitle = linkTitleIn.getText();
        	link.description = linkDescriptionIn.getText();
        	//link.linkLineType = linkLineTypeIn.getText();
        	refreshLinks(EditorXMLParser.linkData);
        }
        else if ( selection == 1 )
        {
        	JLabel warning = new JLabel("Are you sure you want to delete this link type?");
            Object[] deleteOptions = { "OK", "CANCEL" };
            int deleteSelection = JOptionPane.showOptionDialog(
                mainFrame, warning, "Delete Link Type",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, deleteOptions, deleteOptions[0] );
            if ( deleteSelection == 0 )
            {
            	EditorXMLParser.linkData.remove(link);
        		refreshLinks(EditorXMLParser.linkData);
            }
        }
	}
	
	private ArrayList<JPanel> nodeLinksList;
	private int nodeLinksNumber = 0;

	private void makeNodeComponents(final JPanel nodePanel)
	{		
		JPanel titlePanel = new JPanel();
		JLabel title = new JLabel("Node Title");
		titlePanel.add(title);
		final JTextField titleIn = new JTextField(20);
		titleIn.addKeyListener(new KeyListener(){
			@Override public void keyPressed(KeyEvent arg0) {}
			@Override public void keyReleased(KeyEvent arg0) {}
			@Override
			public void keyTyped(KeyEvent e) {
					String text = titleIn.getText();
					if (e.getKeyChar() != '\b' )
						text += e.getKeyChar();
				for ( NodeData n : EditorXMLParser.nodeData )
				{
					if (n.nodeTitle.equals(text) )
					{
						titleIn.setForeground(Color.red);
						return;
					}
				}
				titleIn.setForeground(Color.black);
			}
		});
		titlePanel.add(titleIn);
		nodePanel.add(titlePanel);
		
		JPanel descriptionPanel = new JPanel();
		JLabel description = new JLabel("Node Description");
		descriptionPanel.add(description);
		final JTextField descriptionIn = new JTextField(20);
		descriptionPanel.add(descriptionIn);
		nodePanel.add(descriptionPanel);
		
		JPanel chapterPanel = new JPanel();
		JLabel chapter = new JLabel("Node Chapter");
		chapterPanel.add(chapter);
		final JTextField chapterIn = new JTextField(20);
		chapterIn.addKeyListener(new KeyListener(){
			@Override public void keyPressed(KeyEvent arg0) {}
			@Override public void keyReleased(KeyEvent arg0) {}
			@Override
			public void keyTyped(KeyEvent e) {
				String text = chapterIn.getText();
				if (e.getKeyChar() != '\b' )
					text += e.getKeyChar();
				for ( ChapterData c : EditorXMLParser.chapterData )
				{
					if (c.chapterTitle.equals(text) )
					{
						chapterIn.setForeground(Color.black);
						return;
					}
				}
				chapterIn.setForeground(Color.red);
			}
		});
		chapterPanel.add(chapterIn);
		nodePanel.add(chapterPanel);
		
		JPanel pagePanel = new JPanel();
		JLabel page = new JLabel("Node Webpage");
		pagePanel.add(page);
		final JTextField pageIn = new JTextField(20);
		pageIn.setToolTipText("Address should be in the form: \"websites/chapterfolder/page.html\".");
		pagePanel.add(pageIn);
		nodePanel.add(pagePanel);
		
		JPanel toolsPanel = new JPanel();
		JButton nodeCreateButton = new JButton("Create!");
		nodeCreateButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if ( titleIn.getText().isEmpty() || descriptionIn.getText().isEmpty()
						|| pageIn.getText().isEmpty() || chapterIn.getText().isEmpty() )
					return;
				
				NodeData newNode = new NodeData(titleIn.getText(), chapterIn.getText(), 
						descriptionIn.getText(), pageIn.getText());
				for ( JPanel i : nodeLinksList )
				{
					String name = ((JTextField)i.getComponent(1)).getText();
					String type = "";
					if ( i.getComponent(3) instanceof JComboBox )
						type = (String) ((JComboBox<String>)i.getComponent(3)).getSelectedItem();
					newNode.linkList.add(new Link(name, type));
				}
				EditorXMLParser.nodeData.add(newNode);
				refreshNodes(EditorXMLParser.nodeData);
			}
		});
		toolsPanel.add(nodeCreateButton);
		JButton nodeAddLinksButton = new JButton("Add New Link");
		nodeAddLinksButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JPanel linksPanel = new JPanel();
				JLabel linkLabel = new JLabel("link " + (nodeLinksNumber+1));
				linkLabel.setName("link" + (nodeLinksNumber+1));
				linksPanel.add(linkLabel);
				
				final JTextField linkIn = new JTextField(10);
				linkIn.setName("linkIn" + (nodeLinksNumber+1));
				linkIn.addKeyListener(new KeyListener(){
					@Override public void keyPressed(KeyEvent arg0) {}
					@Override public void keyReleased(KeyEvent arg0) {}
					@Override
					public void keyTyped(KeyEvent e) {
						String text = linkIn.getText();
						if (e.getKeyChar() != '\b' )
							text += e.getKeyChar();
						for ( NodeData n : EditorXMLParser.nodeData )
						{
							if (n.nodeTitle.equals(text) )
							{
								linkIn.setForeground(Color.black);
								return;
							}
						}
						linkIn.setForeground(Color.red);
					}
				});
				linksPanel.add(linkIn);
				
				JLabel linkTypeLabel = new JLabel("link " + (nodeLinksNumber+1) + " Type");
				linkTypeLabel.setName("linkType" + (nodeLinksNumber+1));
				linksPanel.add(linkTypeLabel);
				
				JComboBox<String> linkTypeIn = new JComboBox<String>();
				linkTypeIn.setName("linkTypeIn" + (nodeInfoLinksNumber+1));
				for ( LinkData links : EditorXMLParser.linkData)
					linkTypeIn.addItem(links.linkTitle);
				linksPanel.add(linkTypeIn);
				nodeLinksList.add(linksPanel);
				nodePanel.add(linksPanel);
				nodeLinksNumber++;
				nodePanel.setPreferredSize(new Dimension(350, 300 + nodeLinksNumber * 30));
				mainFrame.repaint();
			}
		});
		toolsPanel.add(nodeAddLinksButton);
		JButton nodeRemoveLinkButton = new JButton("Remove Link");
		nodeRemoveLinkButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if ( nodeLinksNumber > 0)
				{
					JPanel p = nodeLinksList.remove(nodeLinksNumber-1);
					nodePanel.remove(p);
					nodeLinksNumber--;
					mainFrame.repaint();
				}
			}});
		toolsPanel.add(nodeRemoveLinkButton);
		
		nodeLinksList = new ArrayList<JPanel>();
		
		nodePanel.add(toolsPanel);
		nodePanel.setPreferredSize(new Dimension(350, 300 + nodeLinksNumber * 30));
	}
	
	private void makeChapterComponents(JPanel chapterPanel)
	{	
		JPanel titlePanel = new JPanel();
		JLabel title = new JLabel("Chapter Title");
		titlePanel.add(title);
		final JTextField chapterTitleIn = new JTextField(20);
		chapterTitleIn.addKeyListener(new KeyListener(){
			@Override public void keyPressed(KeyEvent arg0) {}
			@Override public void keyReleased(KeyEvent arg0) {}
			@Override
			public void keyTyped(KeyEvent e) {
				String text = chapterTitleIn.getText();
				if (e.getKeyChar() != '\b' )
					text += e.getKeyChar();
				for ( ChapterData c : EditorXMLParser.chapterData )
				{
					if (c.chapterTitle.equals(text) )
					{
						chapterTitleIn.setForeground(Color.red);
						return;
					}
				}
				chapterTitleIn.setForeground(Color.black);
			}
		});
		titlePanel.add(chapterTitleIn);
		chapterPanel.add(titlePanel);
		
		JPanel descriptionPanel = new JPanel();
		JLabel description = new JLabel("Chapter Description");
		descriptionPanel.add(description);
		final JTextField chapterDescriptionIn = new JTextField(20);
		descriptionPanel.add(chapterDescriptionIn);
		chapterPanel.add(descriptionPanel);
		
		JPanel colorPanel = new JPanel();
		JLabel colorLabel = new JLabel("Chapter Color");
		colorPanel.add(colorLabel);
		final JComboBox<String> chapterColorBoxIn = new JComboBox<String>();
		for ( String color : EditorXMLParser.getChapterColors())
			chapterColorBoxIn.addItem(color);
		
		colorPanel.add(chapterColorBoxIn);
		chapterPanel.add(colorPanel);

		JPanel defaultPanel = new JPanel();
		JLabel defaultN = new JLabel("Default Node");
		defaultPanel.add(defaultN);
		final JComboBox<String>  defaultIn = new JComboBox<String>();
		for ( NodeData n : EditorXMLParser.nodeData)
		{
			defaultIn.addItem(n.nodeTitle);
		}
		if ( defaultIn.getItemCount() < 1 )
		{
			defaultIn.addItem("EMPTY");
		}
		defaultIn.setPreferredSize(new Dimension(250, 25));
		defaultPanel.add(defaultIn);
		chapterPanel.add(defaultPanel);
		
		boolean canSetAsDefault = true;
		for ( ChapterData chapters : EditorXMLParser.chapterData)
		{
			if ( chapters.isDefaultChapter )
				canSetAsDefault = false;
		}
		JPanel defaultChapterPanel = new JPanel();
		JLabel defaultChapter = new JLabel("Is Default Chapter");
		defaultChapterPanel.add(defaultChapter);
		final JRadioButton defaultChapterIn = new JRadioButton();
		defaultChapterPanel.add(defaultChapterIn);
		defaultChapterIn.setEnabled(canSetAsDefault);
		chapterPanel.add(defaultChapterPanel);
		
		JPanel xPanel = new JPanel();
		JLabel xValue = new JLabel("Fixed X Position");
		xPanel.add(xValue);
		final JSpinner xValueIn = new JSpinner();
		xValueIn.setPreferredSize(new Dimension(50, 20));
		xPanel.add(xValueIn);
		chapterPanel.add(xPanel);
		
		JPanel yPanel = new JPanel();
		JLabel yValue = new JLabel("Fixed Y Position");
		yPanel.add(yValue);
		final JSpinner yValueIn = new JSpinner();
		yValueIn.setPreferredSize(new Dimension(50, 20));
		yPanel.add(yValueIn);
		chapterPanel.add(yPanel);
		
		
		JPanel toolsPanel = new JPanel();
		JButton chapterCreateButton = new JButton("Create!");
		chapterCreateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ( chapterTitleIn.getText().isEmpty() || chapterDescriptionIn.getText().isEmpty() )
					return;
				
				ChapterData newChapter = new ChapterData(chapterTitleIn.getText(), 
						(String)chapterColorBoxIn.getSelectedItem(), chapterDescriptionIn.getText(),
						(int)xValueIn.getValue(), (int)yValueIn.getValue(), (String)defaultIn.getSelectedItem(), 
						defaultChapterIn.isSelected());
				EditorXMLParser.chapterData.add(newChapter);
				refreshChapters(EditorXMLParser.chapterData);
			}
		});
		toolsPanel.add(chapterCreateButton);
		chapterPanel.add(toolsPanel);
	}
	
	private void makeLinkComponents(JPanel linkPanel)
	{
		JButton linkCreateButton;
		
		JPanel titlePanel = new JPanel();
		JLabel title = new JLabel("Link Title");
		titlePanel.add(title);
		final JTextField linkTitleIn = new JTextField(20);
		linkTitleIn.addKeyListener(new KeyListener(){
			@Override public void keyPressed(KeyEvent arg0) {}
			@Override public void keyReleased(KeyEvent arg0) {}
			@Override
			public void keyTyped(KeyEvent e) {
					String text = linkTitleIn.getText();
					if (e.getKeyChar() != '\b' )
						text += e.getKeyChar();
				for ( LinkData l : EditorXMLParser.linkData )
				{
					if (l.linkTitle.equals(text) )
					{
						linkTitleIn.setForeground(Color.red);
						return;
					}
				}
				linkTitleIn.setForeground(Color.black);
			}
		});
		titlePanel.add(linkTitleIn);
		linkPanel.add(titlePanel);
		
		JPanel descriptionPanel = new JPanel();
		JLabel description = new JLabel("Link Description");
		descriptionPanel.add(description);
		final JTextField linkDescriptionIn = new JTextField(20);
		descriptionPanel.add(linkDescriptionIn);
		linkPanel.add(descriptionPanel);
		
		/*JPanel typePanel = new JPanel();
		JLabel type = new JLabel("Link Line Type");
		typePanel.add(type);
		final JTextField linkLineTypeIn = new JTextField(20);
		typePanel.add(linkLineTypeIn);
		linkPanel.add(typePanel);*/
		
		JPanel toolsPanel = new JPanel();
		linkCreateButton = new JButton("Create!");
		linkCreateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ( linkTitleIn.getText().isEmpty() || linkDescriptionIn.getText().isEmpty() )
					return;
				
				LinkData newLink = new LinkData(linkTitleIn.getText(), Color.black, 
						linkDescriptionIn.getText(), "STANDARD");//linkLineTypeIn.getText());
				EditorXMLParser.linkData.add(newLink);
				refreshLinks(EditorXMLParser.linkData);
			}
		});
		toolsPanel.add(linkCreateButton);
		linkPanel.add(toolsPanel);
	}
}
