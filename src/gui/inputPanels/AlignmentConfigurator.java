package gui.inputPanels;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
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
import gui.ErrorWindow;
import gui.document.ACGDocument;
import gui.widgets.RoundedPanel;

public class AlignmentConfigurator extends RoundedPanel implements Configurator {

	protected File selectedFile = null;
	protected JTextField fileField;
	List<Sequence> seqs;
	ACGFrame acgParent;
	BuildPanel buildPanel;
	
	private static final String defaultNodeLabel = "Alignment";
	private String nodeLabel = defaultNodeLabel;
	
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
		fileField.setEditable(false);
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
			String shortname = selectedFile.getName();
			shortname = shortname.substring(0, shortname.lastIndexOf("."));
			fileField.setText(selectedFile.getName());
			nodeLabel = shortname;
			readSequences(selectedFile); //read sequences from file into list
			buildPanel.alignmentSelected(); //Causes other windows to appear in the build panel
		}
	}

	public String getNodeName() {
		return nodeLabel;
	}
	
	/**
	 * Read sequences from the given file and put them in the 'seqs' field
	 * @param file
	 */
	private void readSequences(File file) {
		Map<String, String> alnAttrs = new HashMap<String, String>();
		alnAttrs.put(Alignment.SEQUENCE_FILE_ATTR, selectedFile.getPath());
		Alignment aln = new Alignment(alnAttrs);
		seqs = aln.getSequences();
	}
	
	
	@Override
	public Element[] getRootXMLNodes(Document doc) throws ParserConfigurationException, InputConfigException {
		if (seqs == null || seqs.size()==0) {
			throw new InputConfigException("No sequences found");
		}
		Element root = doc.createElement(getNodeName());
		root.setAttribute(XMLLoader.CLASS_NAME_ATTR,  Alignment.class.getCanonicalName());
		
		Element seqList = doc.createElement("sequences1");
		seqList.setAttribute(XMLLoader.CLASS_NAME_ATTR,  XMLLoader.LIST_ATTR);
		root.appendChild(seqList);
		
		for(Sequence seq : seqs) {
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
	public void readNodesFromDocument(ACGDocument doc) throws InputConfigException {
		List<String> alnLabels = doc.getLabelForClass(Alignment.class);
		
		if (alnLabels.size() == 0) {
			throw new InputConfigException("Could not find any sequences in document");
		}
		
		if (alnLabels.size() > 1) {
			throw new InputConfigException("Currently, the document builder can only handle a single alignment (found " + alnLabels.size() + ") sorry!");			
		}
		
		Object alnObj;
		try {
			nodeLabel = alnLabels.get(0);
			alnObj = doc.getObjectForLabel(alnLabels.get(0));
			if (alnObj instanceof Alignment) {
				Alignment aln = (Alignment)alnObj;
				seqs = aln.getSequences();
			}
		} catch (Exception ex) {
			ErrorWindow.showErrorWindow(ex);
		}
		
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
