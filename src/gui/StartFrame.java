package gui;

import gui.document.ACGDocument;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import component.LikelihoodComponent;

import parameter.DoubleParameter;
import xml.InvalidInputFileException;

import mcmc.MCMC;

/**
 * First panel allowing used to pick an input data file
 * @author brendano
 *
 */
public class StartFrame extends JPanel {
	
	ACGFrame acgParent;
	boolean macMode = false;
	
	public StartFrame(ACGFrame acgParentFrame, boolean macMode) {
		this.acgParent = acgParentFrame;
		this.macMode = macMode;
		initComponents();
	}

	
	private void initComponents() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBackground(ACGFrame.backgroundColor);
		
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		topPanel.add(Box.createVerticalStrut(50));
		topPanel.setBackground(ACGFrame.backgroundColor);
		//Add a logo or something to the top panel....?
		add(topPanel);
		
		JPanel centerPanel = new FancyInputBox();
		
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
		centerPanel.setBackground(ACGFrame.backgroundColor);
		centerPanel.add(Box.createHorizontalStrut(50));
		centerPanel.setPreferredSize(new Dimension(300, 150));
		centerPanel.setMaximumSize(new Dimension(400, 200));
		
		
		filenameField = new JTextField("Enter name of input file");
		filenameField.setPreferredSize(new Dimension(150, 24));
		filenameField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearSelectedFile();
			}
		});
		filenameField.setMaximumSize(new Dimension(800, 30));
		centerPanel.add(filenameField);
		
		JButton browse = new JButton("Browse");
		
		browse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				browseForFile();
			}
		});
		centerPanel.add(browse);
		centerPanel.add(Box.createHorizontalStrut(50));
		add(centerPanel);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setMaximumSize(new Dimension(2000, 40));
		bottomPanel.setAlignmentY(BOTTOM_ALIGNMENT);
		bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		bottomPanel.setBackground(ACGFrame.backgroundColor);
		runInGUIButton = new JButton("Run with UI");
		runInGUIButton.setEnabled(false);
		runInGUIButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				loadFile();
			}
		});
		bottomPanel.add(runInGUIButton);
		
		
		runNoGUIButton = new JButton("Run in background");
		runNoGUIButton.setEnabled(false);
		runNoGUIButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				loadAndRunInBackground();
			}
		});
		bottomPanel.add(runNoGUIButton);
		add(Box.createGlue());
		add(bottomPanel);	
	}


	/**
	 * Attempt to load the file selected, run it in the background, and
	 * destroy the acgParent (and this object as well)
	 */
	protected void loadAndRunInBackground() {
		File inputFile = null;
		if (selectedFile != null) {
			inputFile = selectedFile;
		}
		else {
			inputFile = new File( filenameField.getText() );
		}
		
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
	protected void loadFile() {
		File inputFile = null;
		if (selectedFile != null) {
			inputFile = selectedFile;
		}
		else {
			inputFile = new File( filenameField.getText() );
		}
		
		if (inputFile == null || (! inputFile.exists())) {
			String filename = inputFile == null ? "(empty)" : inputFile.getName();
			JOptionPane.showMessageDialog(getRootPane(), "The file " + filename + " cannot be found.");
			return;
		}
		

		ACGDocument acgDocument = new ACGDocument(inputFile);
		acgParent.loadFile(inputFile.getName(), acgDocument);
	}
	



	private void clearSelectedFile() {
		selectedFile = null;
	}
	
	/**
	 * Called when user clicks 'Browse' button 
	 */
	protected void browseForFile() {
		//If we're on a mac then a FileDialog looks better and supports a few mac-specific options 
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
		
		//If we found a valid selected file, set the info in the text field (a a couple other things)
		if (selectedFile != null && selectedFile.exists()) {
			filenameField.setText(selectedFile.getName());
			runInGUIButton.setEnabled(true);
			runNoGUIButton.setEnabled(true);
		}
	}
	
	//Stores the file the user selected from the file chooser, or null
	JButton runNoGUIButton;
	JButton runInGUIButton;
	private File selectedFile = null;
	private JFileChooser fileChooser = null;
	private JTextField filenameField;

}
