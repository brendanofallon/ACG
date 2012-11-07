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


package tools;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import xml.XMLLoader;

/**
 * Main entry point for application. 
 * This class is a container for some general info about the entire application, including version, a header string with credits, etc.
 * 
 * Its main method does a small bit of argument parsing, if no args are given then the gui is launched. 
 * If args are present, we search for a) .jar or class files or directories to add to the plugin loader, 
 * and .xml files to run.
 *  
 * @author brendano
 *
 */
public class ApplicationHeader {

	public final static String version = "0.8";
	public final static String versionDate = "September, 2011";
	
	public static String getHeader() {
		StringBuilder header = new StringBuilder();
		header.append(" ACG : Markov-chain Monte Carlo analysis of recombinant genealogies \n");
		header.append(" version : " + version + "\t" + versionDate + "\n");
		header.append("\n Brendan O'Fallon \n University of Washington \n");
		header.append(" email: brendano@u.washington.edu \n");
		
		return header.toString();
	}
	
	
	public static void launchGUI() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ACGFrame frame = new ACGFrame();
				frame.setVisible(true);
			}
		});
	}

	
	/**
	 * Here's a simple main class that should work by parsing an xml-file with xmlloader
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length == 0) {
			launchGUI();
			return;
		}
		
		//If some args are present, then assume we're running from the command line
		//and print out the header
		System.out.println(ApplicationHeader.getHeader());
		
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
		} catch (InstantiationException e) {
			System.out.println("An instantiation exception was encountered: \n " + e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.out.println("An illegal access exception was encountered: \n " + e);
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			System.out.println("An invocation target exception was encountered: \n " + e);
			e.printStackTrace();
		}
		
	}
}
