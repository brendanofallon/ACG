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

import java.util.Map;

/**
 * A simple XML-friendly wrapper for a newick string. Actually, one could put any string in here. But the intent
 * is that this object is easily passed to the arg constructor to facilitate initial arg generation
 * @author brendan
 *
 */
public class Newick {

	String newick;
	
	public Newick(String newickString) {
		this.newick = newickString;
	}
	
	public Newick(Map<String, String> attrs) {
		newick = attrs.get("content");
		//System.out.println("Found newick string : " +  newick);
	}	
	
	public String getNewick() {
		return newick;
	}
}
