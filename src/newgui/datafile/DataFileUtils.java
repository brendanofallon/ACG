package newgui.datafile;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * A few static utilities for checking DataFiles
 * @author brendan
 *
 */
public class DataFileUtils {

	private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	private DocumentBuilder builder = null;
	
	public DataFileUtils() {
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Attempts to determine if we can parse this file into
	 * a readable DataFile
	 * @param file
	 * @return
	 */
	public boolean isDataFile(File file) {
		if ( (! file.exists()) || file.isDirectory())
			return false;
		
		try {
			Document doc = builder.parse(file);
			Element root = doc.getDocumentElement();
			if (root.getNodeName().equalsIgnoreCase(XMLDataFile.ROOT_NAME)) {
				return true;
			}
		} catch (Exception ex) {
			return false;
		}
		return false;
	}
}
