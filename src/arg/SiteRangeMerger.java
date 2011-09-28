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

import dlCalculation.computeCore.ComputeCore;

/**
 * An object that facilitates the merging of two lists of site ranges into lists of descendant and coalescing sites.
 * The big design goal here is to compute, given two site ranges that descend from some node, exactly which sites
 * are 'descendant' sites, meaning that they flow through this node but do not coalesce here, and which sites are
 * 'coalescent' sites, meaning that they flow through both of our descendants and hence coalesce here. We'd like to
 * do all this in just a single pass over the combined list of sites, which we do, though a bit clumsily. 
 * We also keep track of some additional info, for instance some the fields which mark the ComputeNode a range of
 * sites references must be kept track of, and we need some convenient filtering stuff for when we know that
 * only a subrange of sites comes our way from a particular (recombination) node.  For some reason this 
 * is harder than it should be....
 * 
 * This has been slowly optimized to the point where it's now a single static method... I guess we could move this into SiteRangeList
 * at some point if we wanted...
 * @author brendano
 *
 */
public class SiteRangeMerger {

	SiteRangeList listR;
	SiteRangeList listL;
	
	
	SiteRange rFilter = null;
	SiteRange lFilter = null;

	
	public static void processListsWithFilters(SiteRangeList listR, SiteRange rFilter, 
												SiteRangeList listL, SiteRange lFilter, 
												SiteRangeList descendantRanges, CoalRangeList coalescentRanges,
                                                CoalRangeList currentRanges, ComputeCore core) {
					
		if (lFilter != null) {
			listL.applyFilter(lFilter.getMin(), lFilter.getMax());
		}
		
		if (rFilter != null) {
			listR.applyFilter(rFilter.getMin(), rFilter.getMax());
		}

		boolean inA = false;
		boolean inB = false;
		
		int aSite;
		int bSite;

		int begin; //Tracks beginning of interval under consider
		int end; //end of interval under consideration

		SiteRangeList a = listR;
		SiteRangeList b = listL;
		
		final boolean anyA = a.size()>0;
		final boolean anyB = b.size()>0;
		
		int aIndex = a.getFirstSiteIndex();
		int bIndex = b.getFirstSiteIndex();

		final int aMax = a.maxSiteIndex();
		final int bMax = b.maxSiteIndex();
		
		if (anyA) {
			if (anyB)
				begin = Math.min( a.at(aIndex), b.at(bIndex) );
			else 
				begin = a.at(aIndex);
		}
		else {
			if (anyB)
				begin = b.at(bIndex);
			else
				begin = Integer.MAX_VALUE;
		}
		
		if (anyA && anyB) {
			aSite = a.at(aIndex);
			bSite = b.at(bIndex);

			//Initialize by setting begin to first site encountered, and flags accordingly
			if (aSite < bSite) {
				begin = aSite;
				inA = true;
				aIndex++;
				aSite = a.at(aIndex);
			}
			else {
				begin = bSite;
				inB = true;
				bIndex++;
				bSite = b.at(bIndex);
			}

             //Indexes which range in the current coalescent list is next
             int currentRangeIndex = 0;

			//Loop over all ranges in ascending order of site boundary, maintaining flags (booleans) indicating
			//which ranges we're currently 'in', so that we can quickly deduce which intervals are empty, 
			//descendant, or coalescent 
			while(aIndex < aMax && bIndex < bMax) {
				aSite = a.at(aIndex);
				bSite = b.at(bIndex);
				
				if (aSite < bSite) {
					end = aSite;
					aIndex++;
				}
				else {
					end = bSite;
					bIndex++;
				}

				//See if range has more than zero sites and if it includes either A or B. If it includes
				//just one range, it's a descendant range, and we just copy the info to us. If it
				//includes both A and B, then that range of sites coalesces here and we make a new
				//coalescent range and assign it's lChild and rChild fields to be the indices of the
				//sites that coalesced here
				if (end > begin) {
					if (inA) {
						if (inB) {
							//In both, add both coalescent and descandant ranges
							//When we append the new ranges we need to give them an appropriate refID, so use from current ranges if possible, else 
							//get a new one from the core
							int newRefID;
							if (currentRangeIndex < currentRanges.rangeCount()) {
								newRefID = currentRanges.getRefID(currentRangeIndex);
							}
							else {
								newRefID = core.nextNumber();
							}

							boolean merged = coalescentRanges.appendRange(begin, end, newRefID, b.getRefID((bIndex-1)/2), a.getRefID((aIndex-1)/2));
							
							if (merged) {
								descendantRanges.mergeRange(begin, end, newRefID);
							}
							else {								
								currentRangeIndex++;
								descendantRanges.forceAppendRange(begin, end, newRefID);
							}
						}
						else {
							//Not in B, but in A
							descendantRanges.appendRange(begin, end, a.getRefID((aIndex-1)/2));
						}
					}
					else {
						//Not inA, are we in B?
						if (inB)
							descendantRanges.appendRange(begin, end, b.getRefID((bIndex-1)/2));
					}
				}

				begin = end;
				inB = (bIndex & 1)==1; //Test for oddness, equivalent to bIndex%2 == 1, but uses hopefully faster bitwise operator
				inA = (aIndex & 1)==1;		
			} //end of while loop over site boundaries

		} //if both ranges were not empty
		
		while(aIndex<aMax) {
			aSite = a.at(aIndex);
			end = aSite; 
			aIndex++;
			inA = (aIndex & 1)==0;
			if (inA && end > begin) {
				descendantRanges.appendRange(begin, end, a.getRefID((aIndex-1)/2));
			}
			begin = end;
		}
		
		
		while(bIndex<bMax) {
			bSite = b.at(bIndex);
			end = bSite;
			bIndex++;
			inB = (bIndex & 1)==0;
			if (inB && end > begin) {
				descendantRanges.appendRange(begin, end, b.getRefID((bIndex-1)/2));
			}
			begin = end;
		}
		
		listR.unapplyFilter();
		listL.unapplyFilter();
	}
	
//	public static void main(String[] args) {
//		SiteRangeList rRanges = new SiteRangeList();
//		SiteRangeList lRanges = new SiteRangeList();
//
//		rRanges.appendRange(0, 100, 1);
//		//rRanges.appendRange(150, 300, 2);
//		//rRanges.appendRange(70, 100, 3);
//
//		lRanges.appendRange(50, 150, 5);
//		//lRanges.appendRange(200, 300, 7);
//		//lRanges.appendRange(60, 70, 6);
//
//		//SiteRange rFilter = new SiteRange(100, 150);
//		//SiteRange lFilter = new SiteRange(10, 80);
//		//SiteRangeList inter = rRanges.copyFilter(3, 12);
//
//		//System.out.println("Intersection : " + inter);
//
//		SiteRangeList dRanges = new SiteRangeList();
//		SiteRangeList cRanges = new SiteRangeList();
//
//		//SiteRangeMerger merger = new SiteRangeMerger(rRanges, null, lRanges, null);
//
//		SiteRangeMerger.processListsWithFilters(rRanges, null, lRanges, null, dRanges, cRanges);
//
//
//		System.out.println("\n Descending ranges : " + dRanges);
//		System.out.println("\n Coalescent ranges : " + cRanges);
//
////		SiteRangeList newRange = new SiteRangeList();
////		newRange.appendRange(0, 250, 17);
////
////		System.out.println("\n New range : " + newRange);
////
////		SiteRangeMerger m2 = new SiteRangeMerger(rRanges, newRange);
////		m2.processListsWithFilters(dRanges, cRanges);
////
////		System.out.println("New d ranges : " + dRanges);
////		System.out.println("New c ranges : " + cRanges);
//	}
}
