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

import java.util.Collections;
import java.util.List;

public class RemovedColumnsMap implements SiteMap {

	final int maxSites; //Maximum number of sites in original alignment
	
	private int[] siteMap = null;
	
	public RemovedColumnsMap(int maxSites) {
		this.maxSites = maxSites;
	}
	
	/**
	 * Removed cols is a list of all columns which have been removed from the original alignment, this
	 * builds the site map. 
	 * @param removedCols
	 */
	public void setRemovedColumns(List<Integer> removedCols) {
		Integer origIndex = 0;
		Collections.sort(removedCols);
		int newMax = maxSites - removedCols.size();
		siteMap = new int[newMax];
		
		
		for(int i=0; i<newMax; i++) {
			while (removedCols.contains(origIndex)) {
				origIndex++;
			}
			siteMap[i] = origIndex;
			origIndex++;
		}
	}
	
	@Override
	public int getOriginalSite(int site) {
		return siteMap[site];
	}

	
//	public static void main(String[] args) {
//		RemovedColumnsMap map = new RemovedColumnsMap(10);
//		List<Integer> sites = new ArrayList<Integer>();
//		sites.add(0);
//		sites.add(9);
//		
//		map.setRemovedColumns(sites);
//		
//		for(int i=0; i<10-sites.size(); i++) {
//			System.out.println(i + "\t" + map.getOriginalSite(i));
//		}
//	}
}
