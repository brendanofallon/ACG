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

import java.util.List;

import org.w3c.dom.Element;

/**
 * A document member is a self-contained collection of likelihoods, parameters, and at least one alignment or tree/ARG. 
 * Typically, it will contain the following elements:
 *  
 *  An alignment
 *  A site model
 *  A coalescent likelihood model
 *  A DL element
 *  
 *  These elements are currently stored as XML DOM objects, since that provides a handy tree-oriented way to describe the collection
 *  
 * @author brendano
 *
 */
public class DocumentMember {

	
	
	
	public Element getAlignmentElement() {
		return null;
	}
	
	public List<Element> getSiteModelElements() {
		return null;
	}
	
	public List<Element> getCoalescentModelElements() {
		return null;
	}
	
	public List<Element> getDLElements() {
		return null;
	}
	
	public List<Element> getRootLevelElements() {
		return null;
	}
	
	public List<Element> getAllParameters() {
		return null;
	}
	
	public List<Element> getAllLikelihoods() {
		return null;
	}
	
	public List<Element> getAllListeners() {
		return null;
	}
	
}
