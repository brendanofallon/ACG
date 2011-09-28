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
import java.io.IOException;

import math.RandomSource;

import arg.ARG;
import arg.CoalNode;
import arg.TreeUtils;
import arg.argIO.ARGParseException;
import arg.argIO.ARGParser;

public class EmitRootHeights {

	public static void main(String[] args) {
		RandomSource.initialize(); //For jiggling of node heights if necessary
		int bins = 100;
		
		if (args.length == 0) {
			System.out.println("Please enter the name of the file containing the ARG");
			return;
		}

		String filename = args[0];
		if (! filename.endsWith("xml")) {
			System.out.println("Please enter the name of the input file as the first argument (must end with xml)");
			return;
		}

		if (args.length==2) {
			String bStr = args[1];
			try {
				bins = Integer.parseInt(bStr);
			}
			catch (NumberFormatException ex) {
				System.out.println("Warning, could not parse an integer number of bins from second argument (got " + bStr + "), defaulting to 100 bins");
			}
		}

		ARGParser parser = new ARGParser();
		try {
			ARG arg = parser.readARG(new File(filename));
			
			int binStep = arg.getSiteCount() / bins;
			for(int site = 0; site < arg.getSiteCount(); site+= binStep) {
				CoalNode root = TreeUtils.createMarginalTree(arg, site);
				System.out.println(site + "\t" + root.getHeight());
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ARGParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
