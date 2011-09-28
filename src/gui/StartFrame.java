package gui;

import gui.document.ACGDocument;
import gui.widgets.BorderlessButton;
import gui.widgets.Style;
import gui.widgets.Stylist;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import component.LikelihoodComponent;

import parameter.DoubleParameter;
import xml.InvalidInputFileException;
import xml.XMLLoader;

import mcmc.MCMC;

/**
 * First panel allowing used to pick an input data file
 * @author brendano
 *
 */
public class StartFrame extends JPanel {
	
	final ACGFrame acgParent;
	boolean macMode = false;
	
	public StartFrame(final ACGFrame acgParentFrame, boolean macMode) {
		this.acgParent = acgParentFrame;
		this.macMode = macMode;
		initComponents();
	}

	
	private void initComponents() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(Box.createVerticalStrut(50));

		Stylist stylist = new Stylist();
		stylist.addStyle(new Style() {
			public void apply(JComponent comp) {
				comp.setMinimumSize(new Dimension(250, 60));
				comp.setPreferredSize(new Dimension(250, 60));
				comp.setMaximumSize(new Dimension(250, 60));
				Font font = ACGFrame.getFont("fonts/glasnostlightfwf.ttf");
				font = font.deriveFont(18f);
				
				if (comp instanceof BorderlessButton) {
					((BorderlessButton)comp).setIconGap(8);
					((BorderlessButton)comp).setYDif(-2);
				}
				if (font != null)
					comp.setFont(font);
			}
		});

		
		ImageIcon createIcon = ACGFrame.getIcon("icons/addIcon.png");
		BorderlessButton createButton = new BorderlessButton("Create a new model", createIcon);
		stylist.applyStyle(createButton);
		createButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				acgParent.showDocMemberConfigPanel();
			}
		});
		add(createButton);
		
		ImageIcon loadIcon = ACGFrame.getIcon("icons/upArrow.png");
		BorderlessButton loadButton = new BorderlessButton("Load an input file", loadIcon);
		loadButton.setIconGap(9);
		stylist.applyStyle(loadButton);
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				browseAndLoad();
			}
		});
		add(loadButton);
		
		ImageIcon runIcon = ACGFrame.getIcon("icons/rightArrow.png");
		BorderlessButton runButton = new BorderlessButton("Run an input file", runIcon);
		stylist.applyStyle(runButton);
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				browseAndRun();
			}
		});
		add(runButton);
		
		

		this.add(Box.createVerticalStrut(50));
	}


	protected void browseAndLoad() {
		File file = browseForFile();
		loadFile(file);
	}


	/**
	 * Perform a few very basic correctness checks and pop up a friendly error-message windows
	 * if something is awry.  
	 * @param file
	 */
	protected boolean checkValidity(File file) {
		if (! file.exists()) {
			JOptionPane.showMessageDialog(this, "The file " + file.getName() + " cannot be found");
			return false;
		}
		
		if (! file.getName().endsWith(".xml")) {
			JOptionPane.showMessageDialog(this, "The file " + file.getName() + " does not appear to be valid xml");
			return false;
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);
			
		} catch (ParserConfigurationException e) {
			
			JOptionPane.showMessageDialog(this, "Error reading file " + file.getName() + ", it does not appear to be valid xml.");
			return false;
		} catch (SAXException e) {
			JOptionPane.showMessageDialog(this, "Error reading file " + file.getName() + ", it does not appear to be valid xml.");
			return false;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "IO Error reading file " + file.getName() + ", the file cannot be read.");
			return false;
		}
			
		return true;
	}
	
	
	/**
	 * Cause the given file to be shown in a docMemberConfigPanel
	 * @param file
	 */
	private void loadFile(File inputFile) {
		if (inputFile == null || (! inputFile.exists())) {
			String filename = inputFile == null ? "(empty)" : inputFile.getName();
			JOptionPane.showMessageDialog(getRootPane(), "The file " + filename + " cannot be found.");
			return;
		}
		
		try {
			boolean ok = checkValidity(inputFile);
			if (ok) {
				ACGDocument acgDocument = new ACGDocument(inputFile);
				acgParent.loadDocMemberConfig(acgDocument);
			}
		}
		catch (InvalidInputFileException ex) {
			ErrorWindow.showErrorWindow(ex);
		}
	}


	protected void browseAndRun() {
		File file = browseForFile();
		runFile(file);
	}


	/**
	 * Attempt to load the file selected, run it in the background, and
	 * destroy the acgParent (and this object as well)
	 */
	protected void loadAndRunInBackground(File inputFile) {
		if (inputFile == null || (! inputFile.exists())) {
			String filename = inputFile == null ? "(empty)" : inputFile.getName();
			JOptionPane.showMessageDialog(getRootPane(), "The file " + filename + " cannot be found.");
			return;
		}
		

		ACGDocument acgDocument = new ACGDocument(inputFile);
		acgDocument.instantiateAll();
		ExecutingChain runner = acgDocument.runMCMC();
		this.setVisible(false);
		acgParent.dispose();
	}
	

	/**
	 * Called when the user has clicked on the 'Done' button. Read the file 
	 * contained specified by selectedFile (or, if null, the text from the 
	 * filenameField), and replace the center panel with the PickParameters 
	 * panel
	 */
	protected void runFile(File inputFile) {
		
		if (inputFile == null || (! inputFile.exists())) {
			String filename = inputFile == null ? "(empty)" : inputFile.getName();
			JOptionPane.showMessageDialog(getRootPane(), "The file " + filename + " cannot be found.");
			return;
		}
		
		boolean ok = checkValidity(inputFile);
		if (ok) {
			ACGDocument acgDocument = new ACGDocument(inputFile);
			acgParent.pickParameters(inputFile.getName(), acgDocument);
		}
	}
	
	/**
	 * Called when user clicks 'Browse' button 
	 */
	protected File browseForFile() {
		//If we're on a mac then a FileDialog looks better and supports a few mac-specific options
		File selectedFile = null;
		if (macMode) {
			FileDialog fileDialog = new FileDialog(acgParent, "Choose a file");
			fileDialog.setMode(FileDialog.LOAD);
			String userDir = System.getProperty("user.dir");
			if (userDir != null)
				fileDialog.setDirectory(userDir);
			
			fileDialog.setVisible(true);
			
			String filename = fileDialog.getFile();
			String path = fileDialog.getDirectory();
			selectedFile = new File(path + filename);
		}
		else {
			//Not on a mac, use a JFileChooser instead of a FileDialog
			
			//Construct a new file choose whose default path is the path to this executable, which 
			//is returned by System.getProperty("user.dir")
			if (fileChooser == null)
				fileChooser = new JFileChooser( System.getProperty("user.dir"));

			
			int option = fileChooser.showOpenDialog(getRootPane());
			if (option == JFileChooser.APPROVE_OPTION) {
				selectedFile = fileChooser.getSelectedFile();
			}
		}
		
		return selectedFile;
	}
	
	private JFileChooser fileChooser = null;

}
