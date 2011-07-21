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
