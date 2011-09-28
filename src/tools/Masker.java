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


package tools;

import java.io.File;

import sequence.Alignment;
import sequence.DataMatrix;

/**
 * A class to aid in masking out erroneous-looking regions
 * @author brendano
 *
 */
public class Masker {

	
	public static double calcPolymorphism(int start, int end, int[] segSites) {
		double count = 0;
		for(int i=0; i<segSites.length; i++) {
			if (segSites[i]>=start && segSites[i]<end)
				count++;
		}
		
		double val = count/(double)(end-start);
//		if (val > 0.5) {
//			System.out.println("wha? val = " + val);
//			count = 0;
//			for(int i=0; i<segSites.length; i++) {
//				if (segSites[i]>=start && segSites[i]<end) {
//					System.out.println("Site " + segSites[i] + " is in the range, count is: " + count);
//					count++;
//				}
//			}	
//		}
		return val;
	}
	

	private static void maskSites(int start, int end, int[] segSites, Alignment aln) {
		for(int i=0; i<segSites.length; i++) {
			int site = segSites[i];
			if (site >= start && site < end) {
				char c = aln.getMaxFreqBase(site);
				aln.conditionalMask(c, site);
			}
		}
	}
	
	
	public static void main(String[] args) {
		if (args.length==0) {
			System.out.println("Enter the name of the file to mask");
			return;
		}
		File file = new File(args[0]);
		
		Alignment aln = new Alignment(file);
		
		
		int totalLength = aln.getSiteCount();
		int[] segSites = aln.getNonGapPolymorphicSites();
		
		if (segSites.length < 10) {
			System.out.println(aln);
			System.exit(0);
		}
		else {
			
			int windowSize = 100;
			int windowStep = 10;
			for(int start = 0; start < totalLength-windowSize; start+=windowStep){
				double poly = calcPolymorphism(start, start+windowSize, segSites);
				//System.out.println(start + "\t" + poly);
				if (poly > 0.10) {
					maskSites(start, start+windowSize, segSites, aln);
				}
				
				
			}
			
			
			windowSize = 500;
			windowStep = 50;
			for(int start = 0; start < totalLength-windowSize; start+=windowStep){
				double poly = calcPolymorphism(start, start+windowSize, segSites);
				//System.out.println(start + "\t" + poly);
				if (poly > 0.02) {
					maskSites(start, start+windowSize, segSites, aln);
				}
			}
			
			
			windowSize = 25;
			windowStep = 5;
			for(int start = 0; start < totalLength-windowSize; start+=windowStep){
				double poly = calcPolymorphism(start, start+windowSize, segSites);
				//System.out.println(start + "\t" + poly);
				if (poly > 0.2) {
					maskSites(start, start+windowSize, segSites, aln);
				}
			}
			
			System.out.println(aln);
		}
		
	}


}
