package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
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

import mcmc.MCMC;

/**
 * First panel allowing used to pick an input data file
 * @author brendano
 *
 */
public class StartFrame extends JPanel {
	
	ACGFrame acgParent;
	
	public StartFrame(ACGFrame acgParentFrame) {
		this.acgParent = acgParentFrame;
		initComponents();
	}

	
	private void initComponents() {
		this.setLayout(new CardLayout());
		
		/*********** Card 1 is where we ask the user for the input file **********/
		
		firstPanel = makeFirstPanel();
		this.add(firstPanel, "first");
		
		/// MMM, other cards?
			
	}
	
	/**
	 * Create and return the panel that allows the user to select an input file
	 * @return
	 */
	private JPanel makeFirstPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		topPanel.add(Box.createVerticalStrut(100));
		//Add a logo or something to the top panel....
		panel.add(topPanel, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel();
		
		centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(centerPanel, BorderLayout.CENTER);
		
		
		filenameField = new JTextField("Enter name of file");
		filenameField.setPreferredSize(new Dimension(150, 24));
		filenameField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearSelectedFile();
			}
		});
		centerPanel.add(filenameField);
		
		JButton browse = new JButton("Browse");
		browse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				browseForFile();
			}
		});
		centerPanel.add(browse);
		
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton done = new JButton("Done");
		done.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				loadFile();
			}
		});
		bottomPanel.add(done);
		panel.add(bottomPanel, BorderLayout.SOUTH);
		
		return panel;
	}

	/**
	 * Called when the user has clicked on the 'Done' button. We see if a valid input file has been selected, and
	 * then attempt to install some hooks and run it
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
		
		RunnableInputFile runnableFile = new RunnableInputFile(inputFile);
		
		List<String> paramLabels = runnableFile.getParameterLabels();
//		System.out.println("Found these parameters : ");
//		for(String label : paramLabels) {
//			System.out.println(label);
//		}
//
		
		List<String> likeLabels = runnableFile.getLikelihoodLabels();
		
		List<String> mcLabels = runnableFile.getMCMCLabels();
		MCMC chain;
		try {
			chain = (MCMC)runnableFile.getObjectForLabel(mcLabels.get(0));
			MainOutputFrame outputPane = new MainOutputFrame(chain, 10000, 2, 2);
			
			DoubleParameter kappa = (DoubleParameter)runnableFile.getObjectForLabel("kappa");
			DoubleParameter popSize = (DoubleParameter)runnableFile.getObjectForLabel("constantPopSize");
			
			LikelihoodComponent dl = (LikelihoodComponent)runnableFile.getObjectForLabel("DLCalculator");
			
			outputPane.addChart(kappa);
			outputPane.addChart(popSize);
			outputPane.addChart(dl);
			
			acgParent.initializeProgressBar(chain);
			
			this.remove(firstPanel);
			this.revalidate();
			
			this.add(outputPane, BorderLayout.CENTER);
			this.revalidate();
			
			runnableFile.runMCMC();
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void clearSelectedFile() {
		selectedFile = null;
	}
	
	protected void browseForFile() {
		if (fileChooser == null)
			fileChooser = new JFileChooser( System.getProperty("user.dir"));
		
		int option = fileChooser.showOpenDialog(getRootPane());
		if (option == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooser.getSelectedFile();
			filenameField.setText(selectedFile.getName());
		}
	}
	
	//Stores the file the user selected from the file chooser, or null
	private File selectedFile = null;
	private JPanel firstPanel = null;
	private JFileChooser fileChooser = null;
	private JTextField filenameField;

}
