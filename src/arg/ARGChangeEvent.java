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
 * A class that summarizes the type of alterations to the arg that occurred
 * @author brendan
 *
 */
public class ARGChangeEvent {
	
	enum ChangeType { NodeAdded, NodeRemoved, StructureChanged, HeightChanged, BreakpointChanged }
	
	ChangeType type;
	ARGNode[] nodesChanged;
	
	public ARGChangeEvent(ChangeType type, ARGNode nodeChanged) {
		this(type, new ARGNode[]{nodeChanged});
	}
	
	public ARGChangeEvent(ChangeType type, ARGNode[] nodesChanged) {
		this.type = type;
		this.nodesChanged = nodesChanged;
	}
	
	public ChangeType getType() {
		return type;
	}
	
	public ARGNode[] getNodesChanged() {
		return nodesChanged;
	}
}
