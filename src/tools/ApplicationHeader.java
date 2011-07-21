package tools;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import xml.XMLLoader;

/**
 * Container for some general info about the entire application, including version, a header string with credits, etc. 
 * @author brendano
 *
 */
public class ApplicationHeader {

	public final static String version = "0.01";
	public final static String versionDate = "July 20, 2011";
	
	public static String getHeader() {
		StringBuilder header = new StringBuilder();
		header.append(" ACG : Markov-chain Monte Carlo analysis of recombinant genealogies \n");
		header.append(" version : " + version + "\t" + versionDate + "\n");
		header.append("\n Brendan O'Fallon \n University of Washington \n");
		header.append(" email: brendano@u.washington.edu \n");
		
		return header.toString();
	}
	
	/**
	 * Here's a simple main class that should work by parsing an xml-file with xmlloader
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println(ApplicationHeader.getHeader());
		
		if (args.length == 0) {
			//Fire up a GUI?
			System.out.println("Please enter the name of the input file you wish to execute.");
			System.exit(0);
		}
		
		File inputFile = new File(args[0]);
		if (!inputFile.exists()) {
			System.out.println("Could not find input file with name " + args[0]);
			System.exit(0);			
		}
		try {
			XMLLoader loader = new XMLLoader(inputFile);
			String userDir = System.getProperty("user.dir");
			if (userDir != null) {
				File pluginFolder = new File(userDir + "/plugins");
				if (pluginFolder.exists() && pluginFolder.isDirectory()) {
					loader.addPath(pluginFolder);
				}
			}
			//Path loading behavior, if none is specified then use the current directory...
			
			//If more arguments exist, assume they are paths to plugin-able classes / jar files /directories
			for(int i=1; i<args.length; i++) {
				File file = new File( args[i] );
				if (file.exists())
					loader.addPath(file);
			}
			
			
			//Finally, load all of the classes and instantiate ad nauseum
			loader.loadAllClasses();
			loader.instantiateAll();
			
		} catch (ParserConfigurationException e) {
			System.out.println("An XML-parsing exception was encountered : " + e);
		} catch (SAXException e) {
			System.out.println("An XML-parsing exception was encountered : " + e);
		} catch (IOException e) {
			System.out.println("An IO Exception was encountered: \n " + e);
		}
		
	}
}
