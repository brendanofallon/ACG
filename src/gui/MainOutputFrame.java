package gui;

import java.awt.GridLayout;

import javax.swing.JPanel;

import mcmc.MCMC;
import mcmc.MCMCListener;

import component.LikelihoodComponent;

import parameter.AbstractParameter;

public class MainOutputFrame extends JPanel implements MCMCListener {
	
	//
	private int rows = 2;
	private int cols = 2;
	private int nextRowIndex = 0;
	private int nextColIndex = 0;
	private MCMC mcmc = null;
	private int frequency;
	
	protected StatusFigure[][] figures = new StatusFigure[rows][cols];
	
	public MainOutputFrame(MCMC chain, int frequency, int rows, int cols) {
		this.mcmc = chain;
		this.frequency = frequency;
		this.rows = rows;
		this.cols = cols;
		initComponents();
	}
	
	public void addChart(AbstractParameter<?> param) {
		if (nextRowIndex == rows) {
			throw new IllegalArgumentException("Can't add any more charts to this panel!");
		}
		StatusFigure figure = new StatusFigure(param);
		figures[nextRowIndex][nextColIndex] = figure;
		nextColIndex++;
		if (nextColIndex == cols) {
			nextColIndex = 0;
			nextRowIndex++;
		}
		
		this.add(figure);
	}
	
	public void addChart(LikelihoodComponent comp) {
		if (nextRowIndex == rows) {
			throw new IllegalArgumentException("Can't add any more charts to this panel!");
		}
		StatusFigure figure = new StatusFigure(comp);
		figures[nextRowIndex][nextColIndex] = figure;
		nextColIndex++;
		if (nextColIndex == cols) {
			nextColIndex = 0;
			nextRowIndex++;
		}
		
		this.add(figure);
	}
	
	
	private void initComponents() {
		GridLayout layout = new GridLayout(rows, cols, 2, 2);
		this.setLayout(layout);
	}

	@Override
	public void setMCMC(MCMC chain) {
		this.mcmc.removeListener(this);
		this.mcmc = chain;
		this.mcmc.addListener(this);
	}

	@Override
	public void newState(int stateNumber) {
		if (stateNumber % frequency == 0) {
			//alert those figures of the new state!
		}
	}

	@Override
	public void chainIsFinished() {
		// TODO Auto-generated method stub
	}
}
