package newgui.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
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


import newgui.datafile.DataFile;
import newgui.gui.display.Display;
import newgui.gui.display.DisplayPane;
import newgui.gui.filepanel.FileTree;
import newgui.gui.widgets.panelPile.PPanel;
import newgui.gui.widgets.panelPile.PanelPile;


public class ViewerWindow extends JFrame {

	public static Font sansFont;
	private static ViewerWindow viewer; //Handy static reference to main window
	
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
	
		sansFont = getFont("fonts/ClienB.ttf");
		
		initComponents();
		
		this.setSize(1000, 600);
		this.setPreferredSize(new Dimension(1000,600));
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
		displayPane.addDisplay(display);
	}
	
	/**
	 * Return an icon associated with the given url. For instance, if the url is icons/folder.png, we look in the
	 * package icons for the image folder.png, and create and return an icon from it. 
	 * @param url
	 * @return
	 */
	public static ImageIcon getIcon(String url) {
		ImageIcon icon = null;
		try {
			java.net.URL imageURL = ViewerWindow.class.getResource(url);
			icon = new ImageIcon(imageURL);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return icon;
	}
	
	public static Font getFont(String url) {
		Font font = null;
		try {
			InputStream fontStream = ViewerWindow.class.getResourceAsStream(url);
			font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
			font = font.deriveFont(12f);
		} catch (FontFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return font;
	}

	private void initComponents() {
		Container contentPane = this.getContentPane();
		
		contentPane.setLayout(new BorderLayout());
		contentPane.setBackground(Color.white);

		mainPanel = new ViewerBackground();
		contentPane.add(mainPanel, BorderLayout.CENTER);
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		JPanel leftPanelTop = new TopLeftPanel();
		leftPanel.add(leftPanelTop, BorderLayout.NORTH);
		JComponent filesPanel = createFilesPanel();
		leftPanel.add(filesPanel, BorderLayout.CENTER);
		leftPanel.setPreferredSize(new Dimension(200, 10000));
		leftPanel.setMaximumSize(new Dimension(200, 10000));
		mainPanel.add(leftPanel);
		
		//List<Range> ranges = new ArrayList<Range>(2048);
		//System.out.println("Creating ranges...");
//		int intMax = 10000;
//		int prev = 0;
//		while (prev < intMax) {
//			int begin = (int)Math.round( 100.0*Math.random() )+prev;
//			int length = (int)Math.round( 500.0 * Math.random() );
//			prev = begin + length;
//			ranges.add(new AbstractRange(begin, begin+length));
//			//System.out.println("Adding rect at begin: "+ begin + " - " + (begin+length));
//		}
		//System.out.println("Creating range block...");
		//RangeBlock block = new RangeBlock(ranges, 0, intMax);

		displayPane = new DisplayPane();
//		Display first = new FirstDisplay();
//		displayPane.addDisplay(first);
		
		mainPanel.add(displayPane);		
		
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
		String inputFilesPath = "inputfiles";
		FileTree inputsTree = new FileTree(new File(inputFilesPath));
		inputsPanel.add(inputsTree);
		PPanel analPanel = new PPanel(pile, "Analyses");
		JTree tree2 = new JTree();
		analPanel.add(tree2);
		PPanel resultsPanel = new PPanel(pile, "Results");
		pile.addPanel(inputsPanel);
		
		pile.addPanel(analPanel);
		pile.addPanel(resultsPanel);
		mainPanel.add(pile);
		pile.showPanel(0, false);
		return pile;
	}
	
	private DisplayPane displayPane;
	private JPanel mainPanel;
}
