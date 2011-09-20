package gui.inputPanels;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sequence.Alignment;
import sequence.Sequence;
import xml.XMLLoader;
import gui.ErrorWindow;
import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;

/**
 * This class handles reading and writing of XML for Alignments. 
 * @author brendano
 *
 */
public class AlignmentElement implements ElementProvider {

	private List<Sequence> seqs = new ArrayList<Sequence>();
	private static final String defaultNodeLabel = "Alignment";
	private String nodeLabel = defaultNodeLabel;
	
	public void readElement(ACGDocument doc) throws InputConfigException {
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
	
	/**
	 * Clear the current list of sequences and add all those found in the alignment provided
	 * @param aln
	 */
	public void setElement(Alignment aln) {
		seqs.clear();
		for(Sequence s : aln.getSequences()) {
			seqs.add(s);
		}
	}
	
	/**
	 * Return the number of sequences in the current sequence list
	 * @return
	 */
	public int getSequenceCount() {
		return seqs.size();
	}
	
	/**
	 * Returns the length of the first sequence, or 0 if there are 0 sequences
	 * @return
	 */
	public int getSequenceLength() {
		if (seqs.size()==0)
			return 0;
		else
			return seqs.get(0).getLength();
	}
	
	
	/**
	 * The name of the node holding the alignment
	 * @return
	 */
	public String getNodeName() {
		return nodeLabel;
	}
	
	public List<Node> getElements(ACGDocument doc) throws InputConfigException {
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
		
		List<Node> alnEl = new ArrayList<Node>();
		alnEl.add(root);
		return alnEl;
		
	}
	
	private Element getElementForSequence(ACGDocument doc, Sequence seq) {
		Element seqEl = doc.createElement(seq.getLabel());
		seqEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, Sequence.class.getCanonicalName());
		Node textNode = doc.createTextNode(seq.getSequence());
		seqEl.appendChild(textNode);
		return seqEl;
	}
}
