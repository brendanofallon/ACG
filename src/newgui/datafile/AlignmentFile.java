package newgui.datafile;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import newgui.gui.display.Display;
import newgui.gui.display.primaryDisplay.PrimaryDisplay;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sequence.Alignment;
import sequence.BasicSequenceAlignment;
import sequence.CompressedAlignment;

import xml.XMLUtils;

/**
 * This class wraps an XMLDataFile and to provide some easy access to alignment-related
 * features
 * @author brendan
 *
 */
public class AlignmentFile extends XMLDataFile {

	private Alignment aln = null;
	
	public AlignmentFile(Alignment aln) {
		setAlignment(aln);
	}
	
	public AlignmentFile(Document doc) {
		super(doc);
		// TODO Auto-generated constructor stub
	}
	
	public AlignmentFile(File file) {
		super(file);
	}

	/**
	 * Obtain the Alignment contained in this file. The sourceFile attribute of the Alignment
	 * is set to this AlignmentFile object
	 * @return
	 */
	public Alignment getAlignment() {
		try {
			if (this.aln == null) {
				this.aln = XMLConverter.readFromXML(doc.getDocumentElement());
			}
			aln.setSourceFile(this);
			return aln;
		} catch (XMLConversionError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Display getDisplay() {
		String title = "New alignment";
		if (source != null) 
			title = source.getName();
		PrimaryDisplay primaryDisplay = new PrimaryDisplay();
		primaryDisplay.setTitle(title);
		primaryDisplay.addAlignment(getAlignment(), title);
		return primaryDisplay;
	}
	
	/**
	 * Replace all current alignment information with the new alignment
	 * @param aln
	 */
	public void setAlignment(Alignment aln) {
		if (aln instanceof CompressedAlignment) {
			this.aln = (CompressedAlignment)aln;
		}
		else {
			this.aln = new CompressedAlignment(aln);
		}
		
		if (doc == null)
			throw new IllegalArgumentException("XMLDocument has not been created");
		Element alnEl = AlignmentFile.XMLConverter.writeToXML(doc, aln);
		Element oldAln = getTopLevelElement(XMLConverter.ALIGNMENT);
		if (oldAln != null)
			doc.removeChild(oldAln);
		doc.getDocumentElement().appendChild(alnEl);
	}


	
	static class XMLConverter {
		
		public static final String ALIGNMENT = "alignment";
		public static final String LABELS = "labels";
		public static final String labelSeparator = "\n";
		public static final String COLMAP = "columnmap";
		public static final String COLUMNS = "columns";
		public static final String COLUMN = "column";
		public static final String INDEX = "index";
		public static final String ID = "id";
		
		/**
		 * Parse information in the xml element and return it as a new Alignment
		 * @param el
		 * @return
		 * @throws XMLConversionError
		 */
		public static Alignment readFromXML(Element el) throws XMLConversionError {
			Element alignmentEl = getChildByName(el, ALIGNMENT);
			if (alignmentEl == null)
				throw new XMLConversionError("Could not find alignment element in data file", el);
			
			Element labels = getChildByName(alignmentEl, LABELS);
			if (labels == null)
				throw new XMLConversionError("Could not find labels element", alignmentEl);
			Element colMap = getChildByName(alignmentEl, COLMAP);
			if (colMap == null)
				throw new XMLConversionError("Could not find column map element", alignmentEl);
			Element columnsEl = getChildByName(alignmentEl, COLUMNS);
			if (columnsEl == null)
				throw new XMLConversionError("Could not find columns element", alignmentEl);
			
			
			List<String> allLabels = parseLabels(labels);
			int[] colMapping = parseIntArray(colMap);
			List<int[]> columns = parseColumns(columnsEl);
			
			
			//Error checking
			for(int i=0; i<columns.size(); i++) {
				if (columns.get(i).length != columns.get(0).length) {
					throw new XMLConversionError("Not all columns are of equal length (column " + i + " has length " + columns.get(i).length + " , but column 0 has length: " + columns.get(0).length, el);	
				}
			}
			if (allLabels.size() != columns.get(0).length) {
				throw new XMLConversionError("Number of labels does not match column length", el);
			}
			
			CompressedAlignment compAln = new CompressedAlignment(allLabels, colMapping, columns);
			BasicSequenceAlignment basicAln = new BasicSequenceAlignment(compAln);
			return basicAln;
		}
		
		private static List<int[]> parseColumns(Element colsEl) {
			List<int[]> cols = new ArrayList<int[]>();
			NodeList children = colsEl.getChildNodes();
			for(int i=0; i<children.getLength(); i++) {
				Node child = children.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(COLUMN)) {
					int[] column = parseIntArray((Element)child);
					cols.add(column);
				}
			}
			return cols;
		}

		private static int[] parseIntArray(Element colMapEl) {
			Node textChild = colMapEl.getFirstChild();
			while (textChild != null && textChild.getNodeType() != Node.TEXT_NODE)
				textChild = textChild.getNextSibling();
			if (textChild == null)
				return null;
			
			String[] colStrs = textChild.getNodeValue().split(",");
			int[] colMap = new int[colStrs.length];
			for(int i=0; i<colMap.length; i++) {
				try {
					int col = -1;
					col = Integer.parseInt(colStrs[i]);
					colMap[i] = col;
				}
				catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
				
			}
			return colMap;
		}

		/**
		 * Obtain labels from given xml element
		 * @param labes
		 * @return
		 */
		private static List<String> parseLabels(Element labes) {
			List<String> labelList = new ArrayList<String>();
			Node textChild = labes.getFirstChild();
			while (textChild != null && textChild.getNodeType() != Node.TEXT_NODE)
				textChild = textChild.getNextSibling();
			if (textChild == null)
				return null;

			String[] labSplit = textChild.getNodeValue().split(labelSeparator);
			for(int i=0; i<labSplit.length; i++) {
				labelList.add(labSplit[i].trim());
			}
			return labelList;
		}


		public static Element writeToXML(Document doc, Alignment aln) {
			Element el = doc.createElement(ALIGNMENT);
			CompressedAlignment compAln;
			if (aln instanceof CompressedAlignment)
				compAln = (CompressedAlignment)aln;
			else
				compAln = new CompressedAlignment(aln);
			
			//Label element
			Element labelsEl = doc.createElement(LABELS);
			StringBuffer labelsStr = new StringBuffer();
			for(int i=0; i<compAln.getSequenceCount(); i++) {
				labelsStr.append(compAln.getLabels().get(i) + labelSeparator);
			}
			Node labelText = doc.createTextNode(labelsStr.toString());
			labelsEl.appendChild(labelText);
			el.appendChild(labelsEl);
			
			//Column map part
			StringBuffer colMapStr = new StringBuffer();
			int[] colMap = compAln.getColumnMap();
			for(int i=0; i<colMap.length; i++) 
				colMapStr.append(colMap[i] + ",");
			Element mapEl = doc.createElement(COLMAP);
			Node mapText = doc.createTextNode(colMapStr.toString());
			mapEl.appendChild(mapText);
			el.appendChild(mapEl);
			
			//Actual columns
			Element colsEl = doc.createElement(COLUMNS);
			for(int i=0; i<compAln.getUniqueColumnCount(); i++) {
				int[] col = compAln.getUniqueColumn(i);
				Element colEl = doc.createElement(COLUMN);
				StringBuffer colStr = new StringBuffer();
				for(int j=0; j<col.length; j++)
					colStr.append(col[j] +",");
				Node colText = doc.createTextNode(colStr.toString());
				colEl.appendChild(colText);
				colEl.setAttribute(INDEX, "" + i);
				colsEl.appendChild(colEl);
			}
			el.appendChild(colsEl);
			
			return el;
		}
		
	}



	
	
}
