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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import tools.Tree.Node;

import arg.CoalNode;
import arg.TreeUtils;

/**
 * A class to read newick style trees in succession from a file
 * @author brendan
 *
 */
public class TreeReader {

	BufferedReader reader;
	private String currentLine = null;
	private boolean stripAnnotations = false;
	int burnin = 0;
	
	public TreeReader(File file) throws IOException {
		reader = new BufferedReader(new FileReader(file));
		currentLine = reader.readLine();
	}
	
	/**
	 * Set the state number at which to begin reading trees
	 * @param burnin
	 */
	public void setBurnin(int burnin) {
		this.burnin = burnin;
	}
	
	/**
	 * When set to true, this will strip all node annotation data (i.e. [& support=1.0....] ) from the string before reading
	 * @param strip
	 */
	public void setStripAnnotations(boolean strip) {
		this.stripAnnotations = strip;
	}
	
	public Node getNextTreeRoot() {
		if (currentLine == null)
			return null;
		
		try {
			
			while (currentLine != null && currentLine.trim().length()==0) {
				currentLine = reader.readLine();
			}
			if (currentLine == null)
				return null;
			
			if (burnin > 0) {
				Integer state = getState();
				if (state == null)
					throw new IllegalArgumentException("Could not read state from line: " + currentLine);
				
				while(state < burnin && currentLine != null) {
					currentLine = reader.readLine();
					state = getState();
					//System.out.println("Skipping line starting with state : " + state);
					if (state == null)
						throw new IllegalArgumentException("Could not read state from line: " + currentLine);
				}
				
			}

			int firstParen = currentLine.indexOf("(");
			int lastParen = currentLine.lastIndexOf(")");
			while (currentLine == null && (firstParen < 0 || lastParen < 0) ) {
				currentLine = reader.readLine();
				firstParen = currentLine.indexOf("(");
				lastParen = currentLine.lastIndexOf(")");
			}

			if (currentLine == null)
				return null;

			
			
			String treeStr = currentLine.substring(firstParen, lastParen+1);

			if (stripAnnotations)
				treeStr = treeStr.replaceAll("\\[&[^\\]]+\\]", ""); //Used to strip off beast-style annotations, this may not be the right spot for this...

			//System.out.println("\n\n Parsing tree: " + treeStr);
			Tree tree = TreeUtils.buildMultiTreeFromNewick(treeStr);

			currentLine = reader.readLine();

			return tree.getRoot();			
		} catch (IOException e) {
			currentLine = null;
		}

		return null;
	}
	
	/**
	 * Returns the 'state' value of the current line, if it exists. If not and burnin has been set, throws
	 * an exception
	 * @return
	 */
	private Integer getState() {
		int stateIndex = currentLine.indexOf("state=");
		if (stateIndex < 0 && burnin > 0) {
			throw new IllegalStateException("Could not read state value for line : " + currentLine);
		}
		int bracketIndex = currentLine.indexOf("]", stateIndex);
		
		String stateStr = currentLine.substring(stateIndex+7, bracketIndex);
		try {
			Integer state = Integer.parseInt(stateStr);
			//System.out.println("Parsed state " + state + " from string : " + stateStr);

			return state;
		}
		catch(NumberFormatException nfe) {
			System.out.println("Error reading state value for line: " + currentLine);
			return null;
		}
	}
}
