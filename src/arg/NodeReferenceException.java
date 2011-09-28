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


package arg;

/**
 * A type of exception thrown when a violation of node reference structure is detected - for instance, if 
 * a node's parent doesn't have the node as an offspring. 
 * @author brendan
 *
 */
public class NodeReferenceException extends Exception {

	ARGNode perp = null;
	
	public NodeReferenceException(ARGNode node, String message) {
		super(message);
		perp = node;
	}
	
	public ARGNode getNode() {
		return perp;
	}
}
