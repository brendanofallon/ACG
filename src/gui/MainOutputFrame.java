package gui;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import mcmc.MCMC;
import mcmc.MCMCListener;

import component.LikelihoodComponent;

import parameter.AbstractParameter;

/**
 * Panel that displays various output charts
 * @author brendano
 *
 */
public class MainOutputFrame extends JPanel implements MCMCListener {
	
	private int rows = 2;
	private int cols = 2;
	private MCMC mcmc = null;
	private int frequency;
	private boolean initialized = false;
	
	private List<StatusFigure> figureList = new ArrayList<StatusFigure>();
	
	public MainOutputFrame(MCMC chain, int frequency, int rows, int cols) {
		this.mcmc = chain;
		chain.addListener(this);
		this.frequency = frequency;
		this.rows = rows;
		this.cols = cols;
		initComponents();
	}
	
	public void addChart(AbstractParameter<?> param, String logKey) {		
		StatusFigure figure;
		if (logKey != null)
			 figure = new StatusFigure(param, logKey);
		else
			 figure = new StatusFigure(param);

		
		figureList.add(figure);
		this.add(figure);
	}
	
	public void addChart(AbstractParameter<?> param) {
		addChart(param, null);
	}
	
	public void addChart(LikelihoodComponent comp) {
		StatusFigure figure = new StatusFigure(comp);	
		figureList.add(figure);
		this.add(figure);
	}
	
	
	private void initComponents() {
		GridLayout layout = new GridLayout(rows, cols, 2, 2);
		this.setLayout(layout);
		this.setBackground(ACGFrame.backgroundColor);
	}

	@Override
	public void setMCMC(MCMC chain) {
		this.mcmc = chain;
	}

	@Override
	public void newState(int stateNumber) {
		if (!initialized) {
			initializeMatrix();
		}
		
		if (stateNumber % frequency == 0) {
			for(StatusFigure fig : figureList) {
				fig.update(stateNumber);
			}
		}
	}

	private void initializeMatrix() {
		initialized = true;
		int numFigs = figureList.size();
		if (numFigs < 4) {
			rows = 1;
			cols = numFigs;
		}
		if (numFigs == 4) {
			rows = 2;
			cols = 2;
		}
		if (numFigs > 4 && numFigs < 7) {
			rows = 2;
			cols = 3;
		}
		if (numFigs >=7 && numFigs < 10) {
			rows = 3;
			cols = 3;
		}
		
		GridLayout layout = new GridLayout(rows, cols, 2, 2);
		this.setLayout(layout);
		for(StatusFigure fig : figureList) {
			this.add(fig);
		}
		
		this.revalidate();
		initialized = true;
	}

	@Override
	public void chainIsFinished() {
		// TODO Auto-generated method stub
	}
}
