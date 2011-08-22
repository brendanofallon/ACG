package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

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
		setPreferredSize(new Dimension(800, 500));
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
	
	public void setRunner(ExecutingChain runner) {
		this.runner = runner;
		runButtonPressed(); //Sets run and pause button to correct enabled states
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
	
	/**
	 * Set the value of the progress bar
	 * @param prog
	 */
	public void setProgress(int val) {
		progressBar.setValue(val);
	}
	
	public void replaceCenterPanel(JPanel newCenterPanel) {
		this.remove(centerPanel);
		this.validate();
		
		this.centerPanel = newCenterPanel;
		this.add(newCenterPanel, BorderLayout.CENTER);
		this.validate();
	}
	
	/**
	 * Called when user clicks on run button, we switch from paused to running state
	 */
	protected void runButtonPressed() {
		if (runner != null) {
			runner.setPaused(false);
			runButton.setEnabled(false);
			pauseButton.setEnabled(true);
		}
	}
	
	/**
	 * Called when user clicks on 'pause' button
	 */
	protected void pauseButtonPressed() {
		if (runner != null) {
			runner.setPaused(true);
			runButton.setEnabled(true);
			pauseButton.setEnabled(false);
		}
	}
	
	
	private void initComponents() {
		BorderLayout layout = new BorderLayout();
		Container mainContainer = this.getContentPane();
		mainContainer.setLayout(layout);
		
		JPanel testPanel = new BuildPanel(this);
		mainContainer.add(testPanel, BorderLayout.CENTER);
		centerPanel = new StartFrame(this, onAMac);
		mainContainer.add(centerPanel, BorderLayout.CENTER);
		
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		progressBar = new JProgressBar(0, 1000);
		progressBar.setPreferredSize(new Dimension(400, 12));
		progressBar.setMaximumSize(new Dimension(4000, 18));
		progressBar.setStringPainted(true);
		bottomPanel.add(Box.createHorizontalStrut(20));
		bottomPanel.add(progressBar);
		runButton = new JButton();
		runButton.setBorder(null);
		runButton.setPreferredSize(new Dimension(40, 40));
		runButton.setMaximumSize(new Dimension(40, 40));
		ImageIcon runIcon = getIcon("icons/runButton.png");
		runButton.setIcon(runIcon);
		runButton.setToolTipText("Resume run");
		runButton.setEnabled(false);
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runButtonPressed();
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
		
		bottomPanel.add(runButton);
		
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
	private JButton runButton;
	private JButton pauseButton;

}
