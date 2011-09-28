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

import gui.document.ACGDocument;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface Configurator {
		
	
	/**
	 * Set the state of this configurator to be that described by the document
	 * @param doc
	 */
	public void readNodesFromDocument(ACGDocument doc)  throws InputConfigException;
	
	/**
	 * Return a list of Elements created by this node reflecting the proper XML structure, suitable
	 * for handing directly to an ACGDocumentBuilder
	 * @param doc The Document needed to create new Elements
	 * @return
	 * @throws ParserConfigurationException 
	 * @throws InputConfigException
	 */
	public Element[] getRootXMLNodes(Document doc) throws ParserConfigurationException, InputConfigException;
	
	/**
	 * Return a list of Parameters generated by the most recent call to getRootXMLNodes, or null if there has
	 * been no call yet to getRootXMLNodes
	 * @return
	 */
	public Element[] getParameters();
	
	/**
	 * Return a list of Likelihoods generated by the most recent call to getRootXMLNodes, or null if there has
	 * been no call yet to getRootXMLNodes
	 * @return
	 */	
	public Element[] getLikelihoods();
	
	class InputConfigException extends Exception {
		
		public InputConfigException(String message) {
			super(message);
		}
	}
}
