package gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import gui.document.ACGDocument;
import gui.document.ACGDocumentBuilder;
import gui.inputPanels.AlignmentConfigurator;
import gui.inputPanels.CoalescentConfigurator;
import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.SiteModelConfigurator;
import gui.widgets.RoundedPanel;

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

import org.w3c.dom.Node;

public class BuildPanel extends JPanel {

	private JPanel bottomPanel;
	
	public BuildPanel(ACGFrame acgParent) {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		add(Box.createVerticalStrut(5));
		alnPanel = new AlignmentConfigurator(acgParent);
		add(alnPanel);
		add(Box.createVerticalStrut(5));
		
		siteModelPanel = new SiteModelConfigurator();
		add(siteModelPanel);
		add(Box.createVerticalStrut(5));
		
		CoalescentConfigurator coalescentPanel = new CoalescentConfigurator();
		add(coalescentPanel);
		add(Box.createVerticalStrut(5));
		
		RoundedPanel loggingPanel = new RoundedPanel();
		coalescentPanel.setMaximumSize(new Dimension(1000, 50));
		coalescentPanel.setPreferredSize(new Dimension(500, 50));
		loggingPanel.add(new JLabel("Logging :"));
		loggingPanel.add(new JCheckBox("Parameter values"));
		loggingPanel.add(new JCheckBox("Recomb. position"));
		loggingPanel.add(new JCheckBox("TMRCA"));
		loggingPanel.add(new JCheckBox("Marginal trees"));
		add(loggingPanel);
		
		
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
						"Overwrite existing file  " + selectedFile.getName(),
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

	protected ACGDocument buildDocument() {
		
		Node[] alnNodes;
		try {
			ACGDocumentBuilder docBuilder = new ACGDocumentBuilder();
			alnNodes = alnPanel.getXMLNodes(docBuilder.getDocument());
			
			for(int i=0; i<alnNodes.length; i++)
				docBuilder.appendNode(alnNodes[i]);	
			
			
			Node[] siteModelNodes = siteModelPanel.getXMLNodes(docBuilder.getDocument());
			for(int i=0; i<siteModelNodes.length; i++)
				docBuilder.appendNode(siteModelNodes[i]);
			
			ACGDocument doc = docBuilder.getACGDocument();
			return doc;
		} catch (Exception e) {
			ErrorWindow.showErrorWindow(e);
		}
		
		return null;
	}
	
	
	AlignmentConfigurator alnPanel;
	SiteModelConfigurator siteModelPanel;
}
