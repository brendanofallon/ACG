package gui;

import java.awt.geom.Point2D;
import java.util.List;

import parameter.AbstractParameter;
import xml.XMLLoader;

import gui.figure.series.XYSeries;
import gui.figure.series.XYSeriesElement;

import component.LikelihoodComponent;

public class LikelihoodMonitor extends MonitorPanel {

	private LikelihoodComponent comp;
	
	public LikelihoodMonitor(LikelihoodComponent comp) {
		this.comp = comp;
		initializeFigure();
		series = new XYSeries[1];
		series[0] = new XYSeries(comp.getLogHeader());
		titles = new String[1];
		titles[0] = "Log likelihood";
		XYSeriesElement serEl = traceFigure.addDataSeries(series[0]);
		serEl.setLineWidth(defaultLineWidth);
	}
	
	
	/**
	 * Called when the mcmc chain fires a new state ot the MainOutputWindow,
	 * this is where we add new data to the chart 
	 * @param state
	 */
	protected void update(int state) {
		//In MC3 runs the parameter we're listening to changes when the chain are swapped. If the chain has changed, query the new chain
		//to find the parameter with the same NODE_ID as the one we're listening to, and use replace the current param
		//with that one. 
		if ( getChainHasChanged() ) {
			List<LikelihoodComponent> likes = getCurrentChain().getLikelihoods();
			boolean found = false;
			final String id = comp.getAttribute(XMLLoader.NODE_ID);
			if (id == null)
				throw new IllegalStateException("Can't perform chain switch because likelihood " + comp + " doesn't have NODE_ID defined");

			for(LikelihoodComponent like : likes) {
				if (like.getAttribute(XMLLoader.NODE_ID).equals(id)) {
					comp = like;
					found = true;
					break;
				}
			} 

			if (!found) {
				System.out.println("Error : can't find new likelihood in chain");
				throw new IllegalStateException("Could not find likelihood with ID: " + comp.getAttribute(XMLLoader.NODE_ID) + " for chain switch ");
			}
		}

		Double val = comp.getCurrentLogLikelihood();
		Point2D.Double point = new Point2D.Double(state, val);
		series[0].addPointInOrder(point);

		if (histoSeries != null) {
			createHistograms();
			histoSeries[0].addValue(val);
		}
		traceFigure.inferBoundsPolitely();
		repaint();
	}


	@Override
	public double[] getMean() {
		return new double[]{ series[0].getYMean() };
	}


	@Override
	public int getCalls() {
		return comp.getProposalCount();
	}


	@Override
	public double getAcceptanceRate() {
		return comp.getAcceptanceRate();
	}
}