package controller;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

import view.EditorSplitPane;


public class Main {
	private static JFrame mainFrame;
	private static EditorSplitPane splitPane;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		mainFrame = getDevelopmentFrame();
		
		File xmlFile = null;
		boolean parseXML = false;
		
		Object[] options = { "Yes", "No" };
		int choice = JOptionPane.showOptionDialog(mainFrame,
				"Create New XML File?\n" + 
				"Note: This will create file in the current directory, overwriting an existing file.",
				"Adaptive Map GUI XML Generator", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
		if ( choice == 0 ) // Create new file
		{
			EditorXMLParser.initializeLists();
			xmlFile = new File(".", "nodes.xml");
			if ( xmlFile.canRead() )
			{
				int overwriteChoice = JOptionPane.showOptionDialog(mainFrame,
						"The file nodes.xml in the current directory will be overwritten!\n"
						+ "Do you wish to continue?",
						"Warning", JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE, null, options, options[1]);
				if ( overwriteChoice == 0 )
				{
					try {
						xmlFile.createNewFile();
					} catch (IOException e) {
						JOptionPane.showMessageDialog(mainFrame,
							    "Unable to create file.\n" + 
							    "Check the permissions of the current directory.",
							    "Error",
							    JOptionPane.WARNING_MESSAGE);
						mainFrame.dispose();
					}
				}
				else
				{
					mainFrame.dispose();
				}
			}

		}
		else // Load file
		{
			EditorXMLParser.initializeLists();
			// Prompt the user to select the xml file
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Open XML File");
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setCurrentDirectory((new File(".")));
			fileChooser.setFileFilter(new XMLFilter());
			int returnValue = fileChooser.showOpenDialog(mainFrame);
			switch(returnValue)
			{
			case JFileChooser.APPROVE_OPTION:
				// Process the xml file
				parseXML = true;
				xmlFile = fileChooser.getSelectedFile();
				break;
			case JFileChooser.CANCEL_OPTION:
			case JFileChooser.ERROR_OPTION:
				JOptionPane.showMessageDialog(mainFrame,
					    "A valid file was not selected.",
					    "Error",
					    JOptionPane.WARNING_MESSAGE);
				mainFrame.dispose();
				break;
			}
		}
		
		if (parseXML)
		{
			EditorXMLParser.parseLinkProperties(xmlFile);
			EditorXMLParser.parseChapterProperties(xmlFile);
			EditorXMLParser.parseNodeInformation(xmlFile);
		}
		splitPane = new EditorSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, mainFrame);

		splitPane.refreshLinks(EditorXMLParser.linkData);
		splitPane.refreshChapters(EditorXMLParser.chapterData);
		splitPane.refreshNodes(EditorXMLParser.nodeData);
		
		final File location = xmlFile;
		
		//Where the GUI is created:
		JMenuBar menuBar;
		JMenu menu;
		JMenuItem menuSave, menuQuit;

		//Create the menu bar.
		menuBar = new JMenuBar();

		//Build the first menu.
		menu = new JMenu("File");
		menuBar.add(menu);

		//A group of JMenuItems
		menuSave = new JMenuItem("Save", KeyEvent.VK_S);
		menuSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		menuSave.getAccessibleContext().setAccessibleDescription(
		        "Saves changes to the xml file.");
		menuSave.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				EditorXMLParser.save(location);
			}
		});
		menu.add(menuSave);
		
		menuQuit = new JMenuItem("Quit", KeyEvent.VK_Q);
		menuQuit.getAccessibleContext().setAccessibleDescription(
				"Exits the program, discarding unsaved changes.");
		menuQuit.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				mainFrame.dispose();
			}
		});
		menu.add(menuQuit);
		
		mainFrame.setJMenuBar(menuBar);
		mainFrame.add(splitPane);
		mainFrame.pack();
	}

	/**
	 * Creates the main frame.
	 * @return The new frame.
	 */
	private static JFrame getDevelopmentFrame() {
		JFrame developmentWindow = new JFrame();
		developmentWindow.setVisible(true);
		developmentWindow.setResizable(false);
		developmentWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		developmentWindow.setTitle("Adaptive Map GUI Generator");
		developmentWindow.setSize(new Dimension(800, 600));
		return developmentWindow;
	}
}
