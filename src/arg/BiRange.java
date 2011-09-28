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
 * An object that specifies a continuous region broken into two smaller parts at a single 'breakpoint'.
 * The breakpoint specifies the site immediately to the 'right' of the split. So if breakpoint = i, the
 * split is between i-1 and i. This is to avoid confusion when specifying ranges for sites. If we refer
 *  to site ranges in the usual half-open manner with the max value one greater than the max index in 
 *  the range, then the two ranges defined by the BiRange are 0..breakpoint and breakpoint..siteMax
 *    
 * @author brendan
 *
 */
public class BiRange {

	//Absolute minimum index for sites. Usually zero. 
	private int siteMin = Integer.MAX_VALUE;
	
	//One greater than the maximum index for sites. 
	private int siteMax = Integer.MAX_VALUE;
	
	protected int breakpoint;
	
	public BiRange(int siteMin, int breakpoint, int siteMax) {
		if (breakpoint<=siteMin) {
			throw new IllegalArgumentException("Breakpoint must be strictly > siteMin (got bp=" + breakpoint + " min=" + siteMin);
		}
		if (breakpoint >= siteMax) {
			throw new IllegalArgumentException("Breakpoint must be strictly < siteMax (got bp=" + breakpoint + " max=" + siteMax);
		}
		
		this.siteMin = siteMin;
		this.breakpoint = breakpoint;
		this.siteMax = siteMax;
	}
	
	/**
	 * Returns 0 if the site is in the first interval (in [0, breakpoint) ), and 1
	 * if the given site is in the second interval (in [breakpoint, max));
	 * @param site
	 * @return
	 */
	public int indexRangeForSite(int site) {
		if (site < siteMin || site >= siteMax) {
			throw new IllegalArgumentException("Cannot index site " + site + " in BiRange " + this);
		}
		return site < breakpoint ? 0 : 1; 
	}
	
	/**
	 * Returns the breakpoint associated with this range. The breakpoint is immediately right (or greater than) the
	 * true split location. If the
	 * @return
	 */
	public int getBreakpoint() {
		return breakpoint;
	}
	
	public int getMin() {
		return siteMin;
	}
	
	public int getMax() {
		return siteMax;
	}
	
	public String toString() {
		return "[" + siteMin + " .. " + breakpoint + " .. " + siteMax + ")";
	}

}
