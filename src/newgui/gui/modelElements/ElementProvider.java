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


package newgui.gui.modelElements;


import java.util.List;

import newgui.gui.modelElements.Configurator.InputConfigException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import document.ACGDocument;

/**
 * An interface for all things that can produce a list of DOM Nodes (not necessarily elements, I guess)
 * @author brendano
 *
 */
public interface ElementProvider {

	public List<Node> getElements(ACGDocument doc) throws InputConfigException ;
	
	
}
