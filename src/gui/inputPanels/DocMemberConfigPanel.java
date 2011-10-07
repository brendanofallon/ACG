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


package gui.inputPanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import gui.ACGFrame;
import gui.ErrorWindow;
import gui.document.ACGDocument;
import gui.document.ACGDocumentBuilder;
import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.loggerConfigs.LoggersPanel;
import gui.widgets.BorderlessButton;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Element;
import sequence.Alignment;

/**
 * A JPanel to display some information in a DocumentMember - typically alignment/ sitemodel / coalescent model things
 * 
 * @author brendano
 *
 */
public class DocMemberConfigPanel extends JPanel {

	JPanel topPanel;
	JLabel topLabel;
	JButton chooseButton;
	
	JTabbedPane tabPane;
	
	SiteModelView siteModelPanel;
	CoalescentView coalescentModelPanel;
	LoggersPanel loggersPanel;
	MCMCModelView mcView;
	
	AlignmentElement alignmentEl;
	MCMCModelElement mcElement;
	ARGModelElement argModel;
	
	ACGFrame acgParent;
	
	public DocMemberConfigPanel(final ACGFrame acgParent) {
		this.setLayout(new BorderLayout());
		this.acgParent = acgParent;
		
		initComponents();
	}


	/**
	 * Attempt to read all model settings, including alignment, site model, loggers, and MCMC settings, from the
	 * given document. This also catches InputConfigExceptions and presents an ErrorWindow if one is caught
	 * @param doc
	 */
	public void loadSettingsFromDocument(ACGDocument doc) {
		try {
			alignmentEl.readElement(doc);
			argModel.readElements(doc);
			updateTopLabel(alignmentEl.getNodeLabel());
			siteModelPanel.readNodesFromDocument(doc);
			coalescentModelPanel.readNodesFromDocument(doc);
			loggersPanel.readNodesFromDocument(doc);
			mcElement.readElements(doc);			
			argView.updateViewFromModel();
			revalidate();
			repaint();
		}
		catch (InputConfigException e) {
			ErrorWindow.showErrorWindow(e);
		}
		catch (Exception ex) {
			ErrorWindow.showErrorWindow(ex);
		}
	}
	
	
	/**
	 * Save the settings to a new file. We always save as a new file so it's clear that
	 * any old one will be overwritten. 
	 */
	public void saveSettings() {
		String os = System.getProperty("os.name");
		boolean onAMac = false;
        if (os.contains("Mac") || os.contains("mac")) {
        	onAMac = true;
        }
       
        
		File selectedFile = null;
		if (onAMac) {
			if (fileDialog == null)
				fileDialog = new FileDialog(acgParent, "Save settings");
			fileDialog.setMode(FileDialog.SAVE);
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

			int option = fileChooser.showSaveDialog(getRootPane());
			if (option == JFileChooser.APPROVE_OPTION) {
				selectedFile = fileChooser.getSelectedFile();
			}
			
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
		}

		
		
		if (selectedFile != null) {
			try {
				BufferedWriter writer;
				ACGDocument acgDoc = this.getACGDocument();
				acgDoc.setSourceFile(selectedFile);
				String docText;
				docText = acgDoc.getXMLString();
				
				writer = new BufferedWriter(new FileWriter(selectedFile));
				writer.write(docText + "\n");
				writer.close();
			} catch (TransformerException ex) {
				ErrorWindow.showErrorWindow(ex);
			} catch (IOException e) {
				ErrorWindow.showErrorWindow(e);
			} catch (InputConfigException e) {
				ErrorWindow.showErrorWindow(e);
			}
			
			
		}
	}
	
	
	/**
	 * Build an ACGDocument given the current settings 
	 * @return
	 * @throws InputConfigException 
	 * @throws ParserConfigurationException 
	 */
	public ACGDocument getACGDocument() throws InputConfigException {
		ACGDocumentBuilder docBuilder = null;
		try {
			if (mcView == null) {
				throw new InputConfigException("MCMC settings have not been specified yet");
			}
			mcView.updateToModel();
			
			docBuilder = new ACGDocumentBuilder();

			docBuilder.addRandomSource();
			
			docBuilder.appendNodes( alignmentEl );

			argView.updateModelFromView();
			argModel.setAlignmentRef(alignmentEl);
			docBuilder.appendNodes( argModel );			

			siteModelPanel.getSiteModel().setARGRef( argModel );
			docBuilder.appendNodes( siteModelPanel.getSiteModel() );
			
			coalescentModelPanel.getModel().setARGRef(argModel);
			docBuilder.appendNodes( coalescentModelPanel.getModel() );
			
			//Prior stuff comes last
			List<DoubleParamElement> paramModels = new ArrayList<DoubleParamElement>();
			paramModels.addAll(siteModelPanel.getSiteModel().getDoubleParameters());
			paramModels.addAll(coalescentModelPanel.getModel().getDoubleParameters());
			
			for(DoubleParamElement paramElement : paramModels) {
				if (paramElement.getPriorModel() != null) {
					Element priorNode = paramElement.getPriorModel().getElement(docBuilder.getACGDocument());
					docBuilder.appendNode(priorNode);
				}
			}
			
			
			mcElement.clearReferences();
			
			
			loggersPanel.setARGReference(argModel);
			List<Element> loggers = loggersPanel.getLoggerNodes(docBuilder.getACGDocument());
			for(Element node : loggers) {
				docBuilder.appendNode(node);
				mcElement.addListenerRef(node);
			}
			
			List<Element> params = docBuilder.getParameters();
			for(Element param : params) {
				mcElement.addParamRef(param);
			}
			
			List<Element> likelihoods = docBuilder.getLikelihoods();
			for(Element like : likelihoods) {
				mcElement.addLikelihoodRef(like);
			}
			
			
			docBuilder.appendNodes( mcElement );
				
			docBuilder.appendHeader();
			docBuilder.appendTimeAndDateComment();
			
		} catch (ParserConfigurationException e) {
			ErrorWindow.showErrorWindow(e);
		} 
		
		if (docBuilder != null)
			return docBuilder.getACGDocument();
		else
			return null;
	}
	
	private void browseForAlignmentFile() {
		boolean macMode = false;
		String os = System.getProperty("os.name");
        if (os.contains("Mac") || os.contains("mac")) {
        	macMode = true;
        }
        
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
			JFileChooser fileChooser = new JFileChooser( System.getProperty("user.dir"));
			int option = fileChooser.showOpenDialog(acgParent);
			if (option == JFileChooser.APPROVE_OPTION) {
				selectedFile = fileChooser.getSelectedFile();
			}
		}
		
		//If we found a valid selected file, set the info in the text field (a a couple other things)
		if (selectedFile != null && selectedFile.exists()) {
			String shortname = selectedFile.getName();
			shortname = shortname.substring(0, shortname.lastIndexOf("."));
			Alignment aln = new Alignment(selectedFile);
			alignmentEl.setElement(aln);
			updateTopLabel(shortname);
			acgParent.enableRunButton();
			acgParent.enableSaveButton();
		}
	}
	
	/**
	 * Reset info in top label to reflect changes in alignment
	 */
	private void updateTopLabel(String alnName) {
		DecimalFormat formatter = new DecimalFormat("###,###");
		topLabel.setText("Alignment \"" + alnName + "\" : " + alignmentEl.getSequenceCount() + " sequences of length " + formatter.format(alignmentEl.getSequenceLength()) + " sites");
		topLabel.revalidate();
	}
	
	private void initComponents() {
		setBackground(ACGFrame.backgroundColor);
		alignmentEl = new AlignmentElement();
		mcElement = new MCMCModelElement();
		argModel = new ARGModelElement();
		
		topPanel = new JPanel();
		topPanel.setOpaque(false);
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
		
		JPanel alnPanel = new JPanel();
		alnPanel.setOpaque(false);
		alnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		topLabel = new JLabel("Choose an alignment");
		chooseButton = new JButton("Choose");
		chooseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browseForAlignmentFile();
			}
		});

		
		alnPanel.add(topLabel);
		alnPanel.add(chooseButton);
		topPanel.add(alnPanel);
		
		argView = new ARGModelView(argModel);
		topPanel.add(argView);
		this.add(topPanel, BorderLayout.NORTH);
		
		tabPane = new JTabbedPane();
		siteModelPanel = new SiteModelView();
		tabPane.insertTab("Site model", null, siteModelPanel, "Substitution model for this alignment", 0);
		
		coalescentModelPanel = new CoalescentView();
		tabPane.insertTab("Coalescent model", null, coalescentModelPanel, "Coalescent model for this alignment", 1);
		
		loggersPanel = new LoggersPanel();
		tabPane.insertTab("Loggers", null, loggersPanel, "Logging and output options", 2);
		
		mcView = new MCMCModelView( mcElement  );
		mcView.updateFromModel();
		tabPane.insertTab("Markov chain", null, mcView, "Options for Markov chain", 3);
		this.add(tabPane, BorderLayout.CENTER);
		
	}
	
	private ARGModelView argView;
	private File selectedFile = null;
	private JFileChooser fileChooser; //Used on non-mac platforms
	private FileDialog fileDialog; //Used on mac systems
	
}
