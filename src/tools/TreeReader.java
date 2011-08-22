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
	
	public TreeReader(File file) throws IOException {
		reader = new BufferedReader(new FileReader(file));
		currentLine = reader.readLine();
	}
	
	public Node getNextTreeRoot() {
		if (currentLine == null)
			return null;
		
		int firstParen = currentLine.indexOf("(");
		int lastParen = currentLine.lastIndexOf(")");
		try {
			while (currentLine == null && (firstParen < 0 || lastParen < 0) ) {
				currentLine = reader.readLine();
				firstParen = currentLine.indexOf("(");
				lastParen = currentLine.lastIndexOf(")");
			}

			if (currentLine == null)
				return null;

			String treeStr = currentLine.substring(firstParen, lastParen+1);

			//System.out.println("\n\n Parsing tree: " + treeStr);
			CoalNode coalroot = TreeUtils.buildTreeFromNewick(treeStr);
			Tree tree = new Tree();
			tree.cloneFromARG(coalroot);

			currentLine = reader.readLine();

			return tree.getRoot();
			
		} catch (IOException e) {
			currentLine = null;
		}

		return null;
	}
}
