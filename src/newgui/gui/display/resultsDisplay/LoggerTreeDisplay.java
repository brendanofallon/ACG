package newgui.gui.display.resultsDisplay;

import java.util.List;

import gui.figure.treeFigure.DrawableTree;
import gui.figure.treeFigure.SquareTree;
import gui.figure.treeFigure.TreeFigure;

public class LoggerTreeDisplay extends LoggerResultDisplay {

	TreeFigure treeFig = null;
	
	@Override
	public void initializeFigure() {
		fig = new TreeFigure();
		treeFig = (TreeFigure)fig;
	}

	
	public void setNewick(String newick) {
		SquareTree drawableTree = new SquareTree(newick);
		treeFig.removeAllTrees();
		treeFig.addTree(drawableTree);	
	}
	
	
	@Override
	public void showConfigFrame() {
		System.out.println("Not implemented yet");
	}

	@Override
	protected String getDataString() {
		List<DrawableTree> trees = treeFig.getTrees();
		if (trees.size() > 0) {
			return trees.get(0).getNewick();
		}
		return "No trees found";
	}

}
