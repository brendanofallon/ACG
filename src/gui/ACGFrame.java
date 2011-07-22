package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import mcmc.MCMC;
import mcmc.MCMCListener;

public class ACGFrame extends JFrame {
	
	public static final Color backgroundColor = new Color(120, 120, 120);
	
	
	public ACGFrame( /* might be nice to get some properties here */ ) {
		super("ACG");
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
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		pack();
		
	}
	
	
	public void initializeProgressBar(MCMC chain) {
		progressBar.setMaximum(chain.getUserRunLength());
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
			System.out.println("Error loadind icon from resouce : " + ex);
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
		
		centerPanel = new StartFrame(this);
		mainContainer.add(centerPanel, BorderLayout.CENTER);
		
		
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		progressBar = new JProgressBar(0, 1000);
		progressBar.setPreferredSize(new Dimension(400, 24));
		
		bottomPanel.add(Box.createHorizontalStrut(20));
		bottomPanel.add(progressBar);
		runButton = new JButton();
		runButton.setBorder(null);
		runButton.setPreferredSize(new Dimension(40, 40));
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





	private ExecutingChain runner = null;
	
	private JPanel centerPanel;
	private JPanel bottomPanel;
	private JProgressBar progressBar;
	private JButton runButton;
	private JButton pauseButton;
}
