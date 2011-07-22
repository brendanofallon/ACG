package gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import mcmc.MCMC;
import mcmc.MCMCListener;

public class ACGFrame extends JFrame {
	
	public ACGFrame( /* might be nice to get some properties here */ ) {
		
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
		setPreferredSize(new Dimension(1000, 500));
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
	/**
	 * Set the value of the progress bar
	 * @param prog
	 */
	public void setProgress(int val) {
		progressBar.setValue(val);
	}
	
	private void initComponents() {
		BorderLayout layout = new BorderLayout();
		Container mainContainer = this.getContentPane();
		mainContainer.setLayout(layout);
		
		JPanel centerPanel = new StartFrame(this);
		mainContainer.add(centerPanel, BorderLayout.CENTER);
		
		
		bottomPanel = new JPanel();
		progressBar = new JProgressBar(0, 1000);
		progressBar.setPreferredSize(new Dimension(800, 24));
		bottomPanel.add(progressBar);
		mainContainer.add(bottomPanel, BorderLayout.SOUTH);
	}

	private JPanel bottomPanel;
	private JProgressBar progressBar;
}
