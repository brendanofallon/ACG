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


package sequence;

/**
 * An interface for things that can map one site index to another. These are useful for when we alter an alignment
 * prior to input (for instance, to remove gapped columns), and after an analysis we want to map sites
 * back to their original position.   
 * Now that we can properly handle gaps in sequences this is mostly unused. Maybe it will be useful if we have multiple, nearby alignments?
 * @author brendano
 *
 */
public interface SiteMap {

	/**
	 * Find the original position of a site that now has the given index
	 * @param site
	 * @return
	 */
	public int getOriginalSite(int site);
	
	
}
