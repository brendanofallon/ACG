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
				font = font.deriveFont(16f);
				if (comp instanceof BorderlessButton) {
					((BorderlessButton)comp).setIconGap(8);
				}
				if (font != null)
					comp.setFont(font);
			}
		});

		
		ImageIcon createIcon = ACGFrame.getIcon("icons/addIcon.png");
		BorderlessButton createButton = new BorderlessButton("Create a new input file", createIcon);
		stylist.applyStyle(createButton);
		createButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				acgParent.showDocMemberConfigPanel();
			}
		});
		add(createButton);
		
		ImageIcon loadIcon = ACGFrame.getIcon("icons/upArrow.png");
		BorderlessButton loadButton = new BorderlessButton("Load an input file", loadIcon);
		stylist.applyStyle(loadButton);
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				acgParent.showDocMemberConfigPanel();
			}
		});
		add(loadButton);
		
		ImageIcon runIcon = ACGFrame.getIcon("icons/rightArrow.png");
		BorderlessButton runButton = new BorderlessButton("Run an input file", runIcon);
		stylist.applyStyle(runButton);
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				browseForFile();
			}
		});
		add(runButton);
		
		

		this.add(Box.createVerticalStrut(50));
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
	protected void loadFile(File inputFile) {
		
		if (inputFile == null || (! inputFile.exists())) {
			String filename = inputFile == null ? "(empty)" : inputFile.getName();
			JOptionPane.showMessageDialog(getRootPane(), "The file " + filename + " cannot be found.");
			return;
		}
		

		ACGDocument acgDocument = new ACGDocument(inputFile);
		acgParent.pickParameters(inputFile.getName(), acgDocument);
	}
	
	/**
	 * Called when user clicks 'Browse' button 
	 */
	protected void browseForFile() {
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
		
		//If we found a valid selected file, set the info in the text field (a a couple other things)
		if (selectedFile != null && selectedFile.exists()) {
			loadFile(selectedFile);
		}
	}
	
	private JFileChooser fileChooser = null;

}
