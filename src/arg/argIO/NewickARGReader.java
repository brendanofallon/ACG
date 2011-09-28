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


package arg.argIO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import arg.ARG;
import arg.ARGNode;
import arg.CoalNode;
import arg.TreeUtils;

/**
 * Reads newick formatted files and converts them to an ARG
 * Why do we need this? Can't we do everything from TreeUtils?
 * @author brendan
 *
 */
public class NewickARGReader implements ARGReader {

	public List<ARGNode> readARGNodes(File file) throws IOException,
			ARGParseException {

		String treeStr;

		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuilder treeLines = new StringBuilder();
		String line = reader.readLine();
		while(line != null) {
			treeLines.append(line);
			line = reader.readLine();
		}
		treeStr = treeLines.toString();

		CoalNode rootNode = TreeUtils.buildTreeFromNewick(treeStr);
		List<ARGNode> allNodes = TreeUtils.collectAllNodes(rootNode);
		
		return allNodes;
	}

	@Override
	public ARG readARG(File file) throws IOException, ARGParseException {
		// TODO Auto-generated method stub
		return null;
	}

}
