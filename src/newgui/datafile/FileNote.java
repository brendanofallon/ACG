package newgui.datafile;

import newgui.xml.XMLUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Small class containing notes that can be associated with a given file
 * @author brendan
 *
 */
public class FileNote {
	
	public static final String NOTES = "notes";
	protected String note = null;
	protected String date = null;
	protected final String id;
	
	
	public FileNote(String content, String date, String id) {
		this.note = content;
		this.date = date;
		this.id = id;
	}
	
	public FileNote(String content, String date) {
		this.note = content;
		this.date = date;
		this.id = makeID();
	}
	
	/**
	 * Create a hopefully-unique ID string for this note so we can tell notes apart
	 * @return
	 */
	private static String makeID() {
		StringBuffer buf = new StringBuffer();
		for(int i=0; i<20; i++) {
			int r = (int)(chars.length()*Math.random());
			char c= chars.charAt(r);
			buf.append(c);
		}
		return buf.toString();
	}
	
	public String getContent() {
		return note;
	}
	
	public String getDate() {
		return date;
	}
	
	public static FileNote makeNote(String content, String date, String id) {
		FileNote note = new FileNote(content, date, id);
		return note;
	}
	
	public static XMLConverter getConverter() {
		return new XMLConverter();
	}
	
	static class XMLConverter {
		
		public static final String NOTE = "note";
		public static final String DATE = "date";
		public static final String ID = "id";
		
		/**
		 * Parse information in the xml element and return it as a new FileNote
		 * @param el
		 * @return
		 * @throws XMLConversionError
		 */
		public FileNote readFromXML(Element el) throws XMLConversionError {
			if (el.getNodeName().equals(NOTE)) {
				String content = XMLUtils.getChildText(el);
				if (content == null)
					throw new XMLConversionError("No text content found", el);
				String date = el.getAttribute(DATE);
				String id = el.getAttribute(ID);
				FileNote note = new FileNote(content, date, id);
				return note;
			}
			else {
				throw new XMLConversionError("Cannot convert element to FileNote", el);
			}
		}
		
		
		public Element writeToXML(Document doc, FileNote note) {
			Element el = doc.createElement(NOTE);
			Node content = doc.createTextNode(((FileNote) note).getContent());
			el.appendChild(content);
			el.setAttribute(DATE, ((FileNote) note).getDate());
			el.setAttribute(ID, ((FileNote) note).id);
			return el;
		}
		
	}

	//Used for generation of random id strings
	private static final String chars = new String("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
}
