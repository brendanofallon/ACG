package logging;

import gui.figure.series.XYSeries;

import java.util.HashMap;
import java.util.Map;

import parameter.Parameter;

import component.LikelihoodComponent;

import mcmc.MCMC;
import mcmc.MCMCListener;

/**
 * This is a special type of logger that stores much the same information as a state logger, but keeps 
 * it all in memory. This logger is surreptitiously added when jobs are run from a PrimaryDisplay (newgui)
 * object and the data is used to produce the charts and graphs shown in a PrimaryDisplay 
 * @author brendano
 *
 */
public class MemoryStateLogger implements MCMCListener {

	private int statesToStore = 5000; //Maximum number of values to store for each parameter tracked
	private Integer logFrequency = null; //Computed when we 'initialize'
	private boolean initialized = false;
	private MCMC chain;
	
	//Storage for all series that hold actual parameter values, key is label of series
	private Map<String, XYSeries> seriesMap = new HashMap<String, XYSeries>();
	
	public MemoryStateLogger() {
		
	}
	
	@Override
	public void setMCMC(MCMC chain) {
		this.chain = chain;
		if (! initialized )
			initialize();
	}

	/**
	 * Query the chain to create storage for various parameter values in seriesMap and 
	 * determine logFrequency 
	 */
	private void initialize() {

		//Determine frequency at which to collect values
		int maxRunlength = chain.getUserRunLength();
		double freq = (double) maxRunlength / (double) statesToStore;
		logFrequency = (int)Math.ceil(freq);
		
		for(LikelihoodComponent comp : chain.getComponents()) {
			XYSeries series = new XYSeries(comp.getLogHeader());
			seriesMap.put(series.getName(), series);
		}
		
		for(Parameter<?> p : chain.getParameters()) {
			XYSeries series = new XYSeries(p.getLogHeader());
			seriesMap.put(series.getName(), series);
		}
	}

	@Override
	public void newState(int stateNumber) {
		if (stateNumber % logFrequency == 0) {
			
		}
	}

	@Override
	public void chainIsFinished() {
		//Nothing to do
	}

}
