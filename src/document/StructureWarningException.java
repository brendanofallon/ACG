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


package document;

import org.w3c.dom.Element;

/**
 * These are thrown when a non-critical document parsing error is encountered
 * while reading an ACGDocument.
 * @author brendan
 *
 */
public class StructureWarningException extends Exception {
	
	Element offendingElement = null;
	
	
	public StructureWarningException(String message) {
		super(message);
	}
	
	public StructureWarningException(Element offendingElement, String message) {
		super(message);
		this.offendingElement = offendingElement;
	}
	
	public Element getOffendingElement() {
		return offendingElement;
	}

}