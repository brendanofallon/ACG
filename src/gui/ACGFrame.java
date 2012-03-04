/********************************************************************
*
* 	Copyright 2011 Brendan O'Fallon
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
***********************************************************************/


package gui;

import gui.document.ACGDocument;
import gui.document.StructureWarningException;
import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.DocMemberConfigPanel;
import gui.widgets.ButtonBar;
import gui.widgets.ButtonBarItem;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.print.attribute.standard.JobState;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import jobqueue.ExecutingChain;
import jobqueue.JobState.State;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import xml.InvalidInputFileException;

import mcmc.MCMC;
import mcmc.MCMCListener;

/**
 * The primary frame of the ACG UI. As the user goes through various steps the central panel
 * is replaced by different panels (StartFrame, PickMonitorsPanel, and MainOutputPane). 
 * This thing also has the run and pause buttons and the progress bar along the bottom. 
 * Right now, there are NO menu options!
 * @author brendan
 *
 */
public class ACGFrame extends JFrame implements WindowListener {
	
	public static final Color backgroundColor = new Color(0.89f, 0.89f, 0.89f);
	private final boolean onAMac; //This gets set to true if we're on a mac, and we do a couple things differently
	
	public ACGFrame( /* might be nice to get some properties here */ ) {
		super("ACG");

		String os = System.getProperty("os.name");
        if (os.contains("Mac") || os.contains("mac")) {
        	onAMac = true;
        }
        else {
        	onAMac = false;
        }
		
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
        
		initComponents();
		setPreferredSize(new Dimension(850, 450));
		//We handle things from a listener of our own design
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this);
		//this.setLocationByPlatform(true);
		this.setLocationRelativeTo(null);
		pack();
	}
	
	/**
	 * Returns true if we think we're on a mac platform
	 * @return
	 */
	public boolean onAMac() {
		return onAMac;
	}
	
	public void initializeProgressBar(MCMC chain, int maxValue) {
		Container mainContainer = this.getContentPane();
		mainContainer.remove(buttonBar);
		mainContainer.add(bottomPanel, BorderLayout.SOUTH);
		mainContainer.validate();
		repaint();
		
		progressBar.setMaximum(maxValue);
		chain.addListener(new MCMCListener() {
			@Override
			public void newState(int stateNumber) {
				if (stateNumber % 1000 == 0) {
					setProgress(stateNumber);
				}
			}

			@Override
			public void chainIsFinished() {	}

			public void setMCMC(MCMC chain) {	}
		});
	}
	
	
	/**
	 *  Read the given document and instantiate all of its elements, then turn control
	 *  over to a PickMonitorsPanel
	 */
	protected void pickParameters(String name, ACGDocument acgDocument) {
		if (name != null)
			this.setTitle("ACG : " + name);
		
		try {
			//In the future we may want to do a few other things before instantiating the objects... but for now
			//we just create 'em right away...
			acgDocument.instantiateAll();
			
			PickMonitorsPanel pickPanel = new PickMonitorsPanel(this, acgDocument);
			this.replaceCenterPanel(pickPanel);
		}
		catch (InvalidInputFileException ex) {
			ErrorWindow.showErrorWindow(ex, "Error encountered while reading input file :");
		}

	}
	
	public void setRunner(ExecutingChain runner) {
		this.runner = runner;
		resumeButtonPressed(); //Sets run and pause button to correct enabled states
	}
	
	
	/**
	 * Returns an icon from the given URL, with a bit of exception handling. 
	 * @param url
	 * @return
	 */
	public static ImageIcon getIcon(String url) {
		ImageIcon icon = null;
		try {
			java.net.URL imageURL = ACGFrame.class.getResource(url);
			icon = new ImageIcon(imageURL);
		}
		catch (Exception ex) {
			System.out.println("Error loading icon from resouce : " + ex);
		}
		return icon;
	}
	
	public static Font getFont(String url) {
		Font font = null;
		try {
			InputStream fontStream = ACGFrame.class.getResourceAsStream(url);
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
	
	/**
	 * Set the value of the progress bar
	 * @param prog
	 */
	public void setProgress(int val) {
		progressBar.setValue(val);
	}
	
	public void replaceCenterPanel(JPanel newCenterPanel) {
		if (centerPanel != null) {
			this.remove(centerPanel);
			this.validate();
		}
		
		this.centerPanel = newCenterPanel;
		this.add(newCenterPanel, BorderLayout.CENTER);
		this.validate();
	}
	
	/**
	 * Called when user clicks on run button, we switch from paused to running state
	 */
	protected void resumeButtonPressed() {
		if (runner != null) {
			runner.setPaused(false);
			resumeButton.setEnabled(false);
			pauseButton.setEnabled(true);
		}
	}
	
//	public void enableRunButton() {
//		toolBar.enableRunButton();
//	}
	
	/**
	 * Replace the center panel with a new DocMemberConfigPanel and cause it to
	 * show settings read from the given document
	 * @param doc
	 */
	public void loadDocMemberConfig(ACGDocument doc) {
		DocMemberConfigPanel configPanel = new DocMemberConfigPanel(this);
		configPanel.loadSettingsFromDocument(doc);
		replaceCenterPanel(configPanel);
	}
	
	/**
	 * Called when user clicks on 'pause' button
	 */
	protected void pauseButtonPressed() {
		if (runner != null) {
			runner.setPaused(true);
			resumeButton.setEnabled(true);
			pauseButton.setEnabled(false);
		}
	}

	
	/**
	 * Assumes the centerPanel is a DocMemberConfigPanel, and attempts to load the Document from it and
	 * then display the pickParameters panel from it. 
	 */
	public void startNewRun() {
		ACGDocument acgDoc = null;
		try {
			acgDoc = ((DocMemberConfigPanel)centerPanel).getACGDocument();
			startRunFromDocument(acgDoc);
		} catch (InputConfigException e) {
			ErrorWindow.showErrorWindow(e);
			return;
		}
	}
	
	/**
	 * Show the pickParametersPanel based on the given document
	 * @param doc
	 */
	public void startRunFromDocument(ACGDocument doc) {
		try {
			doc.loadAndVerifyClasses();
			doc.turnOffMCMC();
			
		}
		catch (StructureWarningException warning) {
			Object[] options = {"Cancel",
			"Continue"};
			int op = JOptionPane.showOptionDialog(this,
					"Warning : " + warning.getMessage(),
					"Document structure warning",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[1]);
			if (op == 0) {
				return;
			}
		}
		catch (Exception e1) {
			ErrorWindow.showErrorWindow(e1);
			return;
		}
		
		String title = null;
		if (doc.getSourceFile() != null)
			title = doc.getSourceFile().getName();
		pickParameters(title, doc);
	}
	
	/**
	 * Assumes the centerPanel is a DocMemberConfigPanel, and attempts to load the Document from it and
	 * then display the pickParameters panel from it. 
	 */
	public void saveSettings() {
		DocMemberConfigPanel docPanel = null;
		try {
			docPanel = ((DocMemberConfigPanel)centerPanel);
		}
		catch (Exception e1) {
			//Dont worry about it, we just wont save the settings
		}
		
		if (docPanel != null) {
			docPanel.saveSettings();
		}
	}
	
	/**
	 * Create and display and new DocMemberConfigPanel in the center area of this frame
	 */
	public void showDocMemberConfigPanel() {
		DocMemberConfigPanel configPanel = new DocMemberConfigPanel(this);
		
		replaceCenterPanel(configPanel);
		runButton.setEnabled(false);
		saveButton.setEnabled(false);
	}
	
	/**
	 * Prompt to use to select an input file, and upon selection load and display the file
	 * in a DocMemberConfigPanel
	 */
	protected void browseAndLoad() {
		File file = browseForFile();
		if (file != null)
			loadFile(file);
	}
	
	/**
	 * Prompt to user to select a file (via browseForFile) and run the document 
	 */
	protected void loadAndRun() {
		File file = browseForFile();
		if (file != null) {
			ACGDocument doc = null;
			try {
				doc = new ACGDocument(file);
			}
			catch (InvalidInputFileException ex) {
				ErrorWindow.showErrorWindow(ex);
			}
			
			if (doc != null) {
				this.setTitle("ACG : " + file.getName());
				startRunFromDocument(doc);
			}
		}
	}
	
	/**
	 * Called when user clicks 'Browse' button, returns file user has selected or null if no file
	 */
	private File browseForFile() {
		//If we're on a mac then a FileDialog looks better and supports a few mac-specific options
		File selectedFile = null;
		if (onAMac()) {
			FileDialog fileDialog = new FileDialog(this, "Choose a file");
			fileDialog.setMode(FileDialog.LOAD);
			String userDir = System.getProperty("user.dir");
			if (userDir != null)
				fileDialog.setDirectory(userDir);
			
			fileDialog.setVisible(true);
			
			String filename = fileDialog.getFile();
			if (filename == null)
				return null; //User aborted selecting file
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
				loadDocMemberConfig(acgDocument);
				enableRunButton();
				enableSaveButton();
			}
		}
		catch (InvalidInputFileException ex) {
			ErrorWindow.showErrorWindow(ex);
		}
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
	
	private void initComponents() {
		BorderLayout layout = new BorderLayout();
		Container mainContainer = this.getContentPane();
		mainContainer.setLayout(layout);
		setBackground(backgroundColor);
		this.getContentPane().setBackground(backgroundColor);
		
		buttonBar = new ButtonBar();
		buttonBar.setBackground(backgroundColor);
		buttonBar.setRightIcon( getIcon("icons/acgImage.png"));
		ButtonBarItem newButton = new ButtonBarItem("New", getIcon("icons/addIcon.png"));
		newButton.setToolTipText("Clear settings and create a new analysis");
		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showDocMemberConfigPanel();
			}
		});
		buttonBar.addButton(newButton);
		
		
		
		ButtonBarItem loadButton = new ButtonBarItem("Load", getIcon("icons/upArrow.png"));
		loadButton.setToolTipText("Load analysis settings from a saved file");
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				browseAndLoad();
			}
		});
		buttonBar.addButton(loadButton);
		
		saveButton = new ButtonBarItem("Save", getIcon("icons/downArrow.png"));
		saveButton.setToolTipText("Save settings to a file");
		saveButton.setEnabled(false);
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveSettings();
			}
		});
		buttonBar.addButton(saveButton);
		
		runButton = new ButtonBarItem("Run", getIcon("icons/rightArrow.png"));
		runButton.setToolTipText("Run an analysis");
		runButton.setEnabled(false);
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				startNewRun();
			}
		});
		buttonBar.addButton(runButton);
		
		
		ButtonBarItem runFileButton = new ButtonBarItem("Run file", getIcon("icons/rightArrowFile.png"));
		runFileButton.setToolTipText("Run an analysis from an external file");
		runFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				loadAndRun();
			}
		});
		buttonBar.addButton(runFileButton);
		
		this.add(buttonBar, BorderLayout.NORTH);
				
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		progressBar = new JProgressBar(0, 1000);
		progressBar.setPreferredSize(new Dimension(400, 12));
		progressBar.setMaximumSize(new Dimension(4000, 18));
		progressBar.setStringPainted(true);
		bottomPanel.add(Box.createHorizontalStrut(20));
		bottomPanel.add(progressBar);
		resumeButton = new JButton();
		resumeButton.setBorder(null);
		resumeButton.setPreferredSize(new Dimension(40, 40));
		resumeButton.setMaximumSize(new Dimension(40, 40));
		ImageIcon runIcon = getIcon("icons/runButton.png");
		resumeButton.setIcon(runIcon);
		resumeButton.setToolTipText("Resume run");
		resumeButton.setEnabled(false);
		resumeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				resumeButtonPressed();
			}
		});
		
		pauseButton = new JButton();
		pauseButton.setPreferredSize(new Dimension(41, 40));
		pauseButton.setMaximumSize(new Dimension(41, 40));
		ImageIcon pauseIcon = getIcon("icons/pauseButton.png");
		pauseButton.setToolTipText("Pause run");
		pauseButton.setIcon(pauseIcon);
		pauseButton.setEnabled(false);
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pauseButtonPressed();
			}
		});
		
		bottomPanel.add(Box.createHorizontalStrut(10));
		
		bottomPanel.add(resumeButton);
		
		bottomPanel.add(pauseButton);
	}


	public void enableRunButton() {
		runButton.setEnabled(true);
	}
	
	public void enableSaveButton() {
		saveButton.setEnabled(true);
	}
	
	
	/***************************** Window listener implementation **************/
	
	@Override
	public void windowClosing(WindowEvent e) {  
		if (runner != null && !(runner.getJobState().getState() == State.COMPLETED)) {
			Object[] options = {"Cancel",
					"Continue in background",
			"Abort run"};
			int op = JOptionPane.showOptionDialog(this,
					"Abort current run ? ",
					"Abort run",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[2]);
			if (op == 0) {
				return;
			}

			if (op==1) {
				this.setVisible(false);
				this.dispose();
			}

			if (op == 2) {
				runner.cancel(true);
				//wait half a sec, then exit
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
				}

				this.setVisible(false);
				this.dispose();
				System.exit(0);
			}

		}
		else {
			this.setVisible(false);
			this.dispose();
			System.exit(0);
		}
	}

	
	@Override
	public void windowClosed(WindowEvent e) { }


	
	@Override
	public void windowActivated(WindowEvent e) { 	}
	
	@Override
	public void windowDeactivated(WindowEvent e) {	}

	@Override
	public void windowDeiconified(WindowEvent e) {	}

	@Override
	public void windowIconified(WindowEvent e) { }

	@Override
	public void windowOpened(WindowEvent e) { }
	
	private ExecutingChain runner = null;
	
	private ButtonBar buttonBar;
	private ButtonBarItem runButton;
	private ButtonBarItem saveButton;
	private JPanel centerPanel;
	private JPanel bottomPanel;
	private JProgressBar progressBar;
	private JButton resumeButton;
	private JButton pauseButton;
	
	
	//private TopToolBar toolBar;
	private JFileChooser fileChooser; //Used on non-mac platforms
	private FileDialog fileDialog; //Used on mac systems
	

}
