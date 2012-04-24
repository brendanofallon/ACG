package newgui.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;


import newgui.UIConstants;
import newgui.datafile.DataFile;
import newgui.gui.display.Display;
import newgui.gui.display.DisplayPane;
import newgui.gui.display.TestDisplay;
import newgui.gui.display.jobDisplay.JobQueueDisplay;
import newgui.gui.filepanel.AnalysisFilesManager;
import newgui.gui.filepanel.FileTree;
import newgui.gui.filepanel.InputFilesManager;
import newgui.gui.filepanel.ResultsFilesManager;
import newgui.gui.widgets.BorderlessButton;
import newgui.gui.widgets.RegionFader;
import newgui.gui.widgets.fileBlocks.AbstractBlock;
import newgui.gui.widgets.fileBlocks.BlocksManager;
import newgui.gui.widgets.fileBlocks.BlocksPanel;
import newgui.gui.widgets.panelPile.PPanel;
import newgui.gui.widgets.panelPile.PanelPile;

/**
 * Main frame containing the application window. Right now, just consists 
 * of a way to open files (through a PanelPile, at left), and a 
 * FancyTabPane that displays the files when opened
 * @author brendan
 *
 */
public class ViewerWindow extends JFrame {

	public static Font sansFont = UIConstants.sansFont;
	private static ViewerWindow viewer; //Handy static reference to main window
	private BlocksManager fileManager = null;
	
	public ViewerWindow() {
		super("View");
		viewer = this;
		try {
        	String plaf = UIManager.getSystemLookAndFeelClassName();
        	String gtkLookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        	//Attempt to avoid metal look and feel if possible
        	if (plaf.contains("metal")) {

        		UIManager.setLookAndFeel(gtkLookAndFeel);
        	}

        	UIManager.setLookAndFeel( plaf );
		}
        catch (Exception e) {
            System.err.println("Could not set look and feel, exception : " + e.toString());
        }	
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		BorderlessButton.setDefaultFont(sansFont.deriveFont(16f));
		
		createFileManager();
		
		initComponents();
		
		this.setSize(1000, 700);
		this.setPreferredSize(new Dimension(1000, 700));
		pack();
		setLocationRelativeTo(null);
	}
	
	/**
	 * Handy static getter for main window
	 * @return
	 */
	public static ViewerWindow getViewer() {
		return viewer;
	}
	
	/**
	 * Create a new window in the displaypane containing a Display associated with
	 * the given DataFile
	 * @param dataFile
	 */
	public void displayDataFile(DataFile dataFile) {
		Display display = dataFile.getDisplay();
		if (display.getTitle() == null) {
		File sourceFile = dataFile.getSourceFile();
			if (sourceFile != null)
				display.setTitle( sourceFile.getName().replace(".xml", "")	);
			else 
				display.setTitle("New display");
				
		}
		displayPane.addDisplay(display);
	}

	
	/**
	 * Create a new window in the displaypane containing a Display associated with
	 * the given DataFile, and using the title provided
	 * @param dataFile
	 */
	public void displayDataFile(DataFile dataFile, String title) {
		Display display = dataFile.getDisplay();
		display.setTitle(title);
		displayPane.addDisplay(display);
		repaint();
	}
	
	public void showJobQueueDisplay() {
		JobQueueDisplay currentDisp = (JobQueueDisplay) displayPane.getDisplayForClass(JobQueueDisplay.class);
		if ( currentDisp != null ) {
			displayPane.selectComponent(currentDisp);
		}
		else {
			if (jobDisplay == null) {
				jobDisplay = new JobQueueDisplay();
			}
			displayPane.addDisplay(jobDisplay);
		}
	}
	
	private void createFileManager() {
		String fileSep = System.getProperty("file.separator");
		String rootDirPath = System.getProperty("user.dir") + fileSep + ".acgdata";
		File rootDir = new File(rootDirPath);
		fileManager = new BlocksManager( rootDir );
	}

	private void initComponents() {
		Container contentPane = this.getContentPane();
		
		contentPane.setLayout(new BorderLayout());
		contentPane.setBackground(UIConstants.lightBackground);

		mainPanel = new JPanel();
		mainPanel.setBackground(UIConstants.lightBackground);
		contentPane.add(mainPanel, BorderLayout.CENTER);
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		JPanel leftPanelTop = new TopLeftPanel();
		leftPanel.add(leftPanelTop, BorderLayout.NORTH);

		BlocksPanel filesPanel = new BlocksPanel(fileManager);
//		filesPanel.setBackground(UIConstants.lightBackground);
//		FileTree inputsTree = new FileTree(InputFilesManager.getManager().getRootDirectory());
//		InputFilesManager.getManager().addListener(inputsTree);
//		AbstractBlock inputBlock = new AbstractBlock("Input files");
//
//		inputBlock.setMainComponent(inputsTree);
//		filesPanel.addBlock(inputBlock);
//		
//		FileTree analysisTree = new FileTree(AnalysisFilesManager.getManager().getRootDirectory());
//		AnalysisFilesManager.getManager().addListener(analysisTree);
//		AbstractBlock analysisBlock = new AbstractBlock("Analysis files");
//		analysisBlock.setMainComponent(analysisTree);
//		filesPanel.addBlock(analysisBlock);
//		
//		FileTree resultsTree = new FileTree(ResultsFilesManager.getManager().getRootDirectory());
//		
//		ResultsFilesManager.getManager().addListener(analysisTree);
//		AbstractBlock resultsBlock = new AbstractBlock("Results files");
//		resultsBlock.setMainComponent(resultsTree);
//		filesPanel.addBlock(resultsBlock);
		
		leftPanel.add(filesPanel, BorderLayout.CENTER);
		leftPanel.setPreferredSize(new Dimension(220, 10000));
		leftPanel.setMaximumSize(new Dimension(220, 10000));
		mainPanel.add(leftPanel);
		displayPane = new DisplayPane();
		
		JPanel displayBackground = new JPanel();
		displayBackground.setBackground(UIConstants.lightBackground);
		displayBackground.setLayout(new BorderLayout());
		displayBackground.add(displayPane, BorderLayout.CENTER);
		displayPane.setOpaque(false);
		mainPanel.add(displayBackground);		
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(new JSeparator(JSeparator.HORIZONTAL));

		contentPane.add(bottomPanel, BorderLayout.SOUTH);		
	}
	
	
	/**
	 * Create the PanelPile showing various files 
	 * @return
	 */
	private JComponent createFilesPanel() {
		PanelPile pile = new PanelPile();
		
		PPanel inputsPanel = new PPanel(pile, "Input files");
		FileTree inputsTree = new FileTree(InputFilesManager.getManager().getRootDirectory());
		InputFilesManager.getManager().addListener(inputsTree);
		inputsPanel.add(inputsTree);
		
		PPanel analPanel = new PPanel(pile, "Analyses");
		FileTree analysisTree = new FileTree(AnalysisFilesManager.getManager().getRootDirectory());
		AnalysisFilesManager.getManager().addListener(analysisTree);
		analPanel.add(analysisTree);
		
		PPanel resultsPanel = new PPanel(pile, "Results");
		FileTree resultsTree = new FileTree(ResultsFilesManager.getManager().getRootDirectory());
		ResultsFilesManager.getManager().addListener(resultsTree);
		resultsPanel.add(resultsTree);
		
		pile.addPanel(inputsPanel);
		pile.addPanel(analPanel);
		pile.addPanel(resultsPanel);
		mainPanel.add(pile);
		pile.showPanel(0, false);
		return pile;
	}
	
	private JobQueueDisplay jobDisplay = null;
	private DisplayPane displayPane;
	private JPanel mainPanel;
}
