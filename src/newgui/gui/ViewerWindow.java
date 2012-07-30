package newgui.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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

import app.ACGApp;
import app.ACGProperties;


import newgui.UIConstants;
import newgui.datafile.DataFile;
import newgui.gui.display.Display;
import newgui.gui.display.DisplayPane;
import newgui.gui.display.TestDisplay;
import newgui.gui.display.jobDisplay.JobQueueDisplay;
import newgui.gui.filepanel.FileTree;
import newgui.gui.widgets.BorderlessButton;
import newgui.gui.widgets.MemoryGauge;
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

	//A couple property keys..
	public static final String WINDOW_WIDTH = "main.window.width";
	public static final String WINDOW_HEIGHT = "main.window.height";
	
	public static Font sansFont = UIConstants.sansFont;
	private static ViewerWindow viewer; //Handy static reference to main window
	private BlocksManager fileManager = null;
	
	public ViewerWindow() {
		super("ACG");
		viewer = this;
		try {
        	String plaf = UIManager.getSystemLookAndFeelClassName();
        	String gtkLookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        	//Attempt to avoid metal look and feel if possible
        	if (plaf.contains("metal")) {

        		UIManager.setLookAndFeel(gtkLookAndFeel);
        	}

        	ACGApp.logger.info("Setting look and feel to : " + plaf);
        	UIManager.setLookAndFeel( plaf );
		}
        catch (Exception e) {
            System.err.println("Could not set look and feel, exception : " + e.toString());
            ACGApp.logger.warning("Error setting look and feel "+ e.toString());
        }	
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		BorderlessButton.setDefaultFont(sansFont.deriveFont(16f));
		
		createFileManager();
		
		initComponents();
		
		int preferredWidth = 1000;
		int preferredHeight = 700;
		if (ACGProperties.hasProperty(WINDOW_WIDTH)) {
			preferredWidth = ACGProperties.getIntegerProperty(WINDOW_WIDTH);
		}
		
		if (ACGProperties.hasProperty(WINDOW_HEIGHT)) {
			preferredHeight = ACGProperties.getIntegerProperty(WINDOW_HEIGHT);
		}
		
		this.setSize(preferredWidth, preferredHeight);
		this.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
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
		ACGApp.logger.info("Displaying data file : " + dataFile.getSourceFile().getName());
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
		ACGApp.logger.info("Creating file manager root at path : " + rootDirPath);
		File rootDir = new File(rootDirPath);
		fileManager = new BlocksManager( rootDir );
	}
	
	public BlocksManager getFileManager() {
		return fileManager;
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
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		bottomPanel.add(new JSeparator(JSeparator.HORIZONTAL));
		MemoryGauge memGauge = new MemoryGauge();
		memGauge.setAlignmentX(Component.RIGHT_ALIGNMENT);
		bottomPanel.add(memGauge);
		contentPane.add(bottomPanel, BorderLayout.SOUTH);		
	}
	
	
	
	private JobQueueDisplay jobDisplay = null;
	private DisplayPane displayPane;
	private JPanel mainPanel;
}
