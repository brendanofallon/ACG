package gui;

import gui.document.ACGDocument;
import gui.inputPanels.DocMemberConfigPanel;

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
import javax.xml.transform.TransformerException;

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
	
	public static final Color backgroundColor = new Color(140, 140, 140);
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
		setPreferredSize(new Dimension(950, 450));
		//We handle things from a listener of our own design
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this);
		this.setLocationByPlatform(true);
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
	 * Called when user clicks on 'pause' button
	 */
	protected void pauseButtonPressed() {
		if (runner != null) {
			runner.setPaused(true);
			resumeButton.setEnabled(true);
			pauseButton.setEnabled(false);
		}
	}
	
	public void clearAndMakeNew() {
		centerPanel = new DocMemberConfigPanel(this);
		this.getContentPane().add(centerPanel, BorderLayout.CENTER);
		this.getContentPane().validate();
		repaint();
	}
	
	public ACGDocument readDocumentFromFile() {
		//If we're on a mac then a FileDialog looks better and supports a few mac-specific options 
		File selectedFile = null;
		if (onAMac) {
			if (fileDialog == null)
				fileDialog = new FileDialog(this, "Choose a file");
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
			ACGDocument doc = new ACGDocument(selectedFile);
			return doc;
		}
		
		return null;
	}
	
	/**
	 * Allow user to pick a file and then attempt to read an ACG document from it, and 
	 * display the associated settings in a new buildPanel that replaces the current centerPanel.
	 * 
	 */
	public void loadDocumentFromFile() {
		ACGDocument doc = readDocumentFromFile();
		if (doc != null) {
			if (centerPanel instanceof DocMemberConfigPanel) {
				DocMemberConfigPanel configPanel = (DocMemberConfigPanel)centerPanel;
				configPanel.loadSettingsFromDocument(doc);
				//toolBar.enableRunButton();
			}
			
		}
	}
	
	/**
	 * Start a new run from the document described by the DocMemberConfigPanel
	 */
	public void startNewRun() {
		try {
			ACGDocument acgDoc = ((DocMemberConfigPanel)centerPanel).getACGDocument();
			acgDoc.loadAndVerifyClasses();
			acgDoc.turnOffMCMC();
			String title = null;
			if (acgDoc.getSourceFile() != null)
				title = acgDoc.getSourceFile().getName();
			pickParameters(title, acgDoc);
		}
		catch (Exception e1) {
			ErrorWindow.showErrorWindow(e1);
			return;
		}
		
		
	}
	

		
	public void showDocMemberConfigPanel() {
		DocMemberConfigPanel configPanel = new DocMemberConfigPanel(this);
		replaceCenterPanel(configPanel);
	}
	
	private void initComponents() {
		BorderLayout layout = new BorderLayout();
		Container mainContainer = this.getContentPane();
		mainContainer.setLayout(layout);
		
		//toolBar = new TopToolBar(this);
		//this.add(toolBar, BorderLayout.NORTH);
		
		//centerPanel = new DocMemberConfigPanel(this);
		//centerPanel = new BuildPanel(this);
		centerPanel = new StartFrame(this, onAMac);
		
		mainContainer.add(centerPanel, BorderLayout.CENTER);
		
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
		
		mainContainer.add(bottomPanel, BorderLayout.SOUTH);
	}



	@Override
	public void windowClosed(WindowEvent e) { }


	@Override
	public void windowClosing(WindowEvent e) {  
		if (runner != null && (!runner.isDone())) {
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
	
	private JPanel centerPanel;
	private JPanel bottomPanel;
	private JProgressBar progressBar;
	private JButton resumeButton;
	private JButton pauseButton;
	
	
	//private TopToolBar toolBar;
	private JFileChooser fileChooser; //Used on non-mac platforms
	private FileDialog fileDialog; //Used on mac systems
	

}
