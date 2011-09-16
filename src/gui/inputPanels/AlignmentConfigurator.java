package gui.inputPanels;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sequence.Alignment;
import sequence.Sequence;

import xml.XMLLoader;

import gui.ACGFrame;
import gui.BuildPanel;
import gui.widgets.RoundedPanel;

public class AlignmentConfigurator extends RoundedPanel implements Configurator {

	protected File selectedFile = null;
	protected JTextField fileField; 
	ACGFrame acgParent;
	BuildPanel buildPanel;
	
	public AlignmentConfigurator(BuildPanel buildPanel, ACGFrame acgParent) {
		super();
		this.acgParent = acgParent;
		this.buildPanel = buildPanel;
		setMaximumSize(new Dimension(1000, 55));
		setPreferredSize(new Dimension(500, 55));
		add(Box.createGlue());
		add(Box.createHorizontalStrut(20));
		add(new JLabel("Alignment :"));
		fileField = new JTextField("Choose file");
		fileField.setPreferredSize(new Dimension(150, 30));
		add(fileField);
		JButton chooseAlnButton = new JButton("Browse");
		chooseAlnButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				browseForFile();
			}
		});
		add(chooseAlnButton);
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
			fileField.setText(selectedFile.getName());
			buildPanel.alignmentSelected(); //Causes other windows to appear in the build panel
		}
	}

	public String getNodeName() {
		return "alignment1";
	}
	
	@Override
	public Element[] getRootXMLNodes(Document doc) throws ParserConfigurationException, InputConfigException {
		if (selectedFile == null || (! selectedFile.exists())) {
			throw new InputConfigException("Please select a valid input alignment file");
		}
		Element root = doc.createElement(getNodeName());
		root.setAttribute(XMLLoader.CLASS_NAME_ATTR,  Alignment.class.getCanonicalName());
		
		Element seqList = doc.createElement("sequences1");
		seqList.setAttribute(XMLLoader.CLASS_NAME_ATTR,  XMLLoader.LIST_ATTR);
		root.appendChild(seqList);
		
		Map<String, String> alnAttrs = new HashMap<String, String>();
		alnAttrs.put(Alignment.SEQUENCE_FILE_ATTR, selectedFile.getPath());
		Alignment aln = new Alignment(alnAttrs);
		if (aln.getSequenceCount()==0) {
			throw new InputConfigException("Could not read sequences from file: " + selectedFile.getName());
		}
		
		for(Sequence seq : aln.getSequences()) {
			Element seqEl = getElementForSequence(doc, seq);
			seqList.appendChild(seqEl);
		}
		
		return new Element[]{root};
	}

	/**
	 * Return an XML element that contains the given sequence
	 * @param doc
	 * @param seq
	 * @return
	 */
	private Element getElementForSequence(Document doc, Sequence seq) {
		Element seqEl = doc.createElement(seq.getLabel());
		seqEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, Sequence.class.getCanonicalName());
		Node textNode = doc.createTextNode(seq.getSequence());
		seqEl.appendChild(textNode);
		return seqEl;
	}

	@Override
	public Element[] getParameters() {
		//Alignments create no parameters
		return new Element[]{};
	}

	@Override
	public Element[] getLikelihoods() {
		//Alignments create no likelihoods
		return new Element[]{};
	}
	
	
}
