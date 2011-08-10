package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.List;

import gui.figure.series.XYSeries;
import gui.figure.series.XYSeriesElement;
import parameter.AbstractParameter;
import xml.XMLLoader;

public class ParamMonitor extends MonitorPanel {

	private AbstractParameter<?> param;
	
	public ParamMonitor(AbstractParameter<?> param, String logKey) {
		this.param = param;
		this.logKey = logKey;
		initializeFigure();
		//This ALL NEEDS TO BE REFACTORED BADLY! BUT how do we know if things should be grouped together, or not? 
		String[] logKeyItems = logKey.split("\\t");
		
		
		String logStr = (param.getLogItem(logKey)).toString();
		String[] logToks = logStr.split("\\t");
		initializeSeries(logToks.length);

		for(int i=0; i<Math.min(logToks.length, logKeyItems.length); i++) {
			String displayName = logKeyItems[i];
			if (i>0)
				displayName = logKeyItems[i] + "(" + i + ")";
			super.addSeries(displayName);			
		}
		
		if (getSeriesCount() > 2) {
			setShowMeans(false);
			setShowStdevs(false);
		}
	}
	
	public ParamMonitor(AbstractParameter<?> param) {
		this.param = param;
		Object t = param.getValue();
		
		initializeFigure();
		if (t instanceof Double) {
			initializeSeries(1);
			super.addSeries(param.getName());
		}
		else {
			if (t instanceof double[]) {
				//also fine, but we'll draw multiple lines
				double[] vals = (double[])t;
				initializeSeries(vals.length);
				for(int i=0; i<vals.length; i++) {
					addSeries(param.getName() + "(" + i + ")");
				}
			}
			else {
				if (t instanceof Integer) {
					//OK
					//We could theoretically do an array of integers?
					addSeries(param.getName());
				}
				else {
					throw new IllegalArgumentException("Can't create a Figure for parameter with type " + t.getClass());
				}
			}
		}

		//means = new double[series.length];
	}
	

	protected void update(int state) {
		//In MC3 runs the parameter we're listening to changed when the chain are swapped. If the chain has changed, query the new chain
		//to find the parameter with the same NODE_ID as the one we're listening to, and use replace the current param
		//with that one. 
		if ( getChainHasChanged() ) {
			List<AbstractParameter<?>> params = getCurrentChain().getParameters();
			boolean found = false;
			final String id = param.getAttribute(XMLLoader.NODE_ID);
			if (id == null)
				throw new IllegalStateException("Can't perform chain switch because likelihood " + param + " doesn't have NODE_ID defined");

			for(AbstractParameter<?> par : params) {
				
				if (par.getAttribute(XMLLoader.NODE_ID).equals(id)) {
					param = par;
					found = true;
					break;
				}
			} 
			

			if (!found) {
				//We get here if we can't find a parameter with the given id in the new chain. This seems like an error,
				//but maybe it's not since some "Parameters", for instance those to do with chain heating, aren't tied to a particular
				//chain and only have a single instance. In this case, we won't find one in the new chain... so right
				//now we ignore this error, but it would really be bad if it happened for other, multiple-instance parameters
				//throw new IllegalStateException("Could not find parameter with ID: " + param.getAttribute(XMLLoader.NODE_ID) + " for chain switch ");
			}
		}

		String logStr = (param.getLogItem(logKey)).toString();
		String[] logToks = logStr.split("\\t");
		for(int i=0; i<logToks.length; i++) {
			try {
				Double val = Double.parseDouble(logToks[i]);
				super.addPointToSeries(i, state, val);
			}
			catch (NumberFormatException nex) {
				//don't worry about it
			}
		}

		
		traceFigure.inferBoundsPolitely();
		repaint();
	}

	@Override
	public int getCalls() {
		return param.getCalls();
	}

	@Override
	public double getAcceptanceRate() {
		return param.getAcceptanceRatio();
	}
	
}
