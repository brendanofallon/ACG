package gui.inputPanels;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import gui.ACGFrame;
import gui.ErrorWindow;
import gui.document.ACGDocument;
import gui.document.ACGDocumentBuilder;
import gui.inputPanels.Configurator.InputConfigException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;

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
	JPanel loggersPanel;
	
	AlignmentElement alignmentEl;
	ARGModelElement argModel;
	
	ACGFrame acgParent;
	
	public DocMemberConfigPanel(ACGFrame acgParent) {
		this.setLayout(new BorderLayout());
		this.acgParent = acgParent;
		
		alignmentEl = new AlignmentElement();
		argModel = new ARGModelElement();
		
		topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		topLabel = new JLabel("Choose an alignment");
		chooseButton = new JButton("Choose");
		chooseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browseForFile();
			}
		});
		
		topPanel.add(topLabel);
		topPanel.add(chooseButton);
		this.add(topPanel, BorderLayout.NORTH);
		
		tabPane = new JTabbedPane();
		siteModelPanel = new SiteModelView();
		tabPane.insertTab("Site model", null, siteModelPanel, "Substitution model for this alignment", 0);
		
		coalescentModelPanel = new CoalescentView();
		tabPane.insertTab("Coalescent model", null, coalescentModelPanel, "Coalescent model for this alignment", 1);
		
		loggersPanel = new JPanel();
		loggersPanel.add(new JLabel("Loggers stuff here"));
		tabPane.insertTab("Loggers", null, loggersPanel, "Logging and output options", 2);
		
		this.add(tabPane, BorderLayout.CENTER);
	}
	
	public void loadSettingsFromDocument(ACGDocument doc) {
		try {
			alignmentEl.readElement(doc);
			argModel.readElements(doc);
			updateTopLabel(alignmentEl.getNodeLabel());
			siteModelPanel.readNodesFromDocument(doc);
			coalescentModelPanel.readNodesFromDocument(doc);
		}
		catch (InputConfigException e) {
			ErrorWindow.showErrorWindow(e);
		}
		catch (Exception ex) {
			ErrorWindow.showErrorWindow(ex);
		}
	}
	
	/**
	 * Build an ACGDocument given the current settings 
	 * @return
	 * @throws ParserConfigurationException 
	 */
	public ACGDocument getACGDocument() {
		ACGDocumentBuilder docBuilder = null;
		try {
			docBuilder = new ACGDocumentBuilder();
			docBuilder.appendHeader();
			docBuilder.appendTimeAndDateComment();
			docBuilder.addRandomSource();
			
			docBuilder.appendNodes( alignmentEl );

			argModel.setAlignmentRef(alignmentEl);
			docBuilder.appendNodes( argModel );			

			docBuilder.appendNodes( siteModelPanel.getSiteModel() );
			
			coalescentModelPanel.getModel().setARGRef(argModel);
			docBuilder.appendNodes( coalescentModelPanel.getModel() );
						
			
		} catch (ParserConfigurationException e) {
			ErrorWindow.showErrorWindow(e);
		} catch (InputConfigException e) {
			ErrorWindow.showErrorWindow(e);
		}
		
		if (docBuilder != null)
			return docBuilder.getACGDocument();
		else
			return null;
	}
	
	protected void browseForFile() {
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
		}
	}
	
	/**
	 * Reset info in top label to reflect changes in alignment
	 */
	private void updateTopLabel(String alnName) {
		topLabel.setText("Alignment \"" + alnName + "\" : " + alignmentEl.getSequenceCount() + " sequences of length  " + alignmentEl.getSequenceLength() + " sites");
		topLabel.revalidate();
	}
	
	private File selectedFile = null;
	
}
