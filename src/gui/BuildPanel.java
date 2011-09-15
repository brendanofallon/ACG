package gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gui.document.ACGDocument;
import gui.document.ACGDocumentBuilder;
import gui.inputPanels.ARGConfigurator;
import gui.inputPanels.AlignmentConfigurator;
import gui.inputPanels.CoalescentConfigurator;
import gui.inputPanels.Configurator;
import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.loggerConfigs.LoggersPanel;
import gui.inputPanels.DLConfigurator;
import gui.inputPanels.MCMCConfigurator;
import gui.inputPanels.SiteModelConfigurator;
import gui.widgets.RoundedPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class BuildPanel extends JPanel {

	private JPanel bottomPanel;
	
	//List of components capable of creating ACGDocument nodes
	List<Configurator> configList = new ArrayList<Configurator>();

	private static String documentHeader = "ACG input document created by ACGUI. To run this file, open it with ACG or type java -jar acg.jar [this file name] at the command line";
	
	public BuildPanel(ACGFrame acgParent) {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(6, 6, 1, 6));
		
		add(Box.createVerticalStrut(3));
		alnPanel = new AlignmentConfigurator(acgParent);
		add(alnPanel);
		add(Box.createVerticalStrut(3));
		
		siteModelPanel = new SiteModelConfigurator();
		add(siteModelPanel);
		add(Box.createVerticalStrut(3));
		
		coalescentPanel = new CoalescentConfigurator();
		add(coalescentPanel);
		add(Box.createVerticalStrut(3));
		
		configList.add(alnPanel);
		configList.add(siteModelPanel);
		configList.add(coalescentPanel);
		
		loggingPanel = new LoggersPanel();
		
		
		add(loggingPanel);
		
		mcConfig = new MCMCConfigurator();
		add(Box.createVerticalStrut(12));
		add(mcConfig);
		
		add(Box.createVerticalGlue());
		
		
		JButton addAlignmentButton = new JButton("Add alignment");
		addAlignmentButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				buildDocument();
			}
		});
		alnPanel.setAlignmentY(LEFT_ALIGNMENT);
		this.add(addAlignmentButton);
		
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		JButton runButton = new JButton("Run");
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				run();
			}
		});
		bottomPanel.add(runButton);
		
		JButton saveButton = new JButton("Save settings");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveSettings();
			}
		});
		bottomPanel.add(saveButton);
		
		add(bottomPanel);
	}

	/**
	 * Save the current settings to a file. This is done by building an ACGDocument by issuing
	 * a call to buildDocument, then converting the document to text and saving the text to a file. 
	 */
	protected void saveSettings() {
		JFileChooser fileChooser = new JFileChooser( System.getProperty("user.dir"));

		int option = fileChooser.showSaveDialog(getRootPane());
		if (option == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			
			//Check about overwriting the existing file
			if (selectedFile != null && selectedFile.exists()) {
				Object[] options = {"Overwrite", "Cancel"};
				int n = JOptionPane.showOptionDialog(getRootPane(),
						"Overwrite existing file  " + selectedFile.getName() + "?",
						"File exists",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE,
						null,
						options,
						options[1]);
				//Abort
				if (n == 1) 
					return;
				
			}
			
			if (selectedFile != null) {
				ACGDocument acgDoc = buildDocument();
				String docText;
				try {
					docText = acgDoc.getXMLString();
				} catch (TransformerException ex) {
					ErrorWindow.showErrorWindow(ex);
					return;
				}
				
				BufferedWriter writer;
				try {
					writer = new BufferedWriter(new FileWriter(selectedFile));
					writer.write(docText + "\n");
					writer.close();
				} catch (IOException e) {
					ErrorWindow.showErrorWindow(e);
				}
			}
		}
	}

	protected void run() {
		// TODO Auto-generated method stub
		
	}

	private List<Configurator> getAllConfigurators() {
		return configList;
	}
	
	/**
	 * Obtain a list of all parameter elements provided by all configurators. This will 
	 * return an empty list before buildDocument has been called. 
	 * 
	 * @return
	 */
	private List<Element> getAllParameters() {
		List<Element> params = new ArrayList<Element>();
		for(Configurator config : getAllConfigurators()) {
			Element[] pars = config.getParameters();
			for(int i=0; i<pars.length; i++) {
				if (! containsLabel( pars[i].getNodeName(), params )) {
					params.add(pars[i]);
				}
			}
		}
		return params;
	}
	
	private static boolean containsLabel(String label, List<Element> list) {
		for(Element el : list) {
			if (el.getNodeName().equals(label)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Obtain a list of all Likelihood elements provided by all configurators. This will 
	 * return an empty list before buildDocument has been called. 
	 * 
	 * @return
	 */
	private List<Element> getAllLikelihoods() {
		List<Element> likes = new ArrayList<Element>();
		for(Configurator config : getAllConfigurators()) {
			Element[] likeArr = config.getLikelihoods();
			for(int i=0; i<likeArr.length; i++) {
				if (! containsLabel( likeArr[i].getNodeName(), likes))
					likes.add(likeArr[i]);
			}
		}
		return likes;
	}
	
	protected ACGDocument buildDocument() {
		
		Element[] alnNodes;
		try {
			ACGDocumentBuilder docBuilder = new ACGDocumentBuilder();
			Node header = docBuilder.getDocument().createComment(documentHeader);
			docBuilder.appendNode(header);
			
			
			alnNodes = alnPanel.getRootXMLNodes(docBuilder.getDocument());
			
			for(int i=0; i<alnNodes.length; i++)
				docBuilder.appendNode(alnNodes[i]);	
			
			ARGConfigurator argConfig = new ARGConfigurator();
			argConfig.setAlignment(alnNodes[0]);
			configList.add( argConfig );
			Element argEl = argConfig.getRootXMLNodes( docBuilder.getDocument() )[0];
			
			//At some point we could allow more initial ARG config options....
			
			docBuilder.appendNode( argEl );
			coalescentPanel.setARG(argEl);
			
			Element[] siteModelNodes = siteModelPanel.getRootXMLNodes(docBuilder.getDocument());
			Element mutModelEl = siteModelNodes[0];
			Element siteModelEl = siteModelNodes[1];
			
			for(int i=0; i<siteModelNodes.length; i++)
				docBuilder.appendNode(siteModelNodes[i]);

			Node[] coalModelNodes = coalescentPanel.getRootXMLNodes(docBuilder.getDocument());
			for(int i=0; i<coalModelNodes.length; i++)
				docBuilder.appendNode(coalModelNodes[i]);
			
			
			dlConfig = new DLConfigurator();
			configList.add(dlConfig);
			dlConfig.setARG(argEl);
			dlConfig.setMutModel(mutModelEl);
			dlConfig.setSiteModel(siteModelEl);
			
			Element[] dlCalcNodes = dlConfig.getRootXMLNodes(docBuilder.getDocument());
			
			for(int i=0; i<dlCalcNodes.length; i++)
				docBuilder.appendNode(dlCalcNodes[i]);

		
			loggingPanel.setARGReference(argEl);
			Element[] loggerNodes = loggingPanel.getRootXMLNodes(docBuilder.getDocument());
			for(int i=0; i<loggerNodes.length; i++) {
				docBuilder.appendNode(loggerNodes[i]);
			}
			
			mcConfig.clearAllReferences(); //Clear any previous references to parameters, likelihoods, etc
			List<Element> allParams = getAllParameters();
			for(Element param : allParams)
				mcConfig.addParameterRef(param);
			
			List<Element> allLikelihoods = getAllLikelihoods();
			for(Element like : allLikelihoods) 
				mcConfig.addLikelihoodRef(like);
					
			for(int i=0; i<loggerNodes.length; i++)
				mcConfig.addListenerRef(loggerNodes[i]);
			
			Element[] mcNodes = mcConfig.getRootXMLNodes(docBuilder.getDocument());
			for(int i=0; i<mcNodes.length; i++)
				docBuilder.appendNode(mcNodes[i]);
			
			ACGDocument doc = docBuilder.getACGDocument();
			return doc;
		} catch (Exception e) {
			ErrorWindow.showErrorWindow(e);
		}
		
		return null;
	}
	
	
	AlignmentConfigurator alnPanel;
	SiteModelConfigurator siteModelPanel;
	CoalescentConfigurator coalescentPanel;
	DLConfigurator dlConfig;
	MCMCConfigurator mcConfig;
	LoggersPanel loggingPanel;
	
}
