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

import arg.ARG;
import arg.TreeUtils;
import arg.argIO.ARGParseException;
import arg.argIO.ARGParser;

/**
 * A small utility to read in an ARG in graphML format and emit a marginal  tree in newick form corresponding
 * to a particular site. 
 * @author brendano
 *
 */
public class ExtractTree {

	
	public static void main(String[] args) {
		
		if (args.length != 2) {
			System.out.println("Please enter the name of the file containing the ARG and the site for which to extract the tree.");
			return;
		}
		
		String filename = args[0];
		if (! filename.endsWith("xml")) {
			System.out.println("Please enter the name of the input file as the first argument (must end with xml)");
			return;
		}
		
		String siteStr = args[1];
		Integer site = -1;
		try {
			site = Integer.parseInt(siteStr);
		}
		catch (NumberFormatException nfe) {
			System.out.println("Could not parse an integer for the site to extract, got : " + siteStr);
			return;
		}
		
		
		ARGParser parser = new ARGParser();
		try {
			ARG arg = parser.readARG(new File(filename));
			String newick = TreeUtils.getMarginalNewickTree(arg, site);
			System.out.println("\n" + newick + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ARGParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
