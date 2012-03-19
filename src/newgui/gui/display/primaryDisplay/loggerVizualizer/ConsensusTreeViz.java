package newgui.gui.display.primaryDisplay.loggerVizualizer;

import java.awt.BorderLayout;

import logging.ConsensusTreeLogger;

import gui.figure.TextElement;
import gui.figure.treeFigure.DrawableTree;
import gui.figure.treeFigure.SquareTree;
import gui.figure.treeFigure.TreeElement;
import gui.figure.treeFigure.TreeFigure;

/**
 * Visualizer for consensus tree logger. Uses TreeFigure to draw a tree 
 * @author brendano
 *
 */
public class ConsensusTreeViz extends AbstractLoggerViz {

	@Override
	public void initialize() {
		this.remove(fig);
		treeLogger = (ConsensusTreeLogger)logger;
		treeFig = new TreeFigure();
		add(treeFig, BorderLayout.CENTER);
		
		burninMessage = new TextElement("(Burnin period not exceeded)", fig);
		burninMessage.setPosition(0.45, 0.5);
		treeFig.addElement(burninMessage);
	}

	protected int getUpdateFrequency() {
		return 1000;
	}
	
	@Override
	public String getDataString() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void update() {
		if (burninMessage != null && logger.getBurninExceeded()) {
			treeFig.removeElement(burninMessage);
			burninMessage = null;
		}
		if (logger.getBurninExceeded()) {
			String consNewick = treeLogger.getSummaryString();
			System.out.println("Consensus tree logger burnin exceeded, newick is:" + consNewick);
			SquareTree drawableTree = new SquareTree(consNewick);
			treeFig.removeAllTrees();
			treeFig.addTree(drawableTree);
			if (treeFig.getScaleType()==DrawableTree.NO_SCALE_BAR)
				treeFig.setScaleType(DrawableTree.SCALE_AXIS);
			treeFig.repaint();
		}
		repaint();
	}

	private TextElement burninMessage;
	private ConsensusTreeLogger treeLogger;
	private TreeElement treeElement;
	private TreeFigure treeFig;

}
