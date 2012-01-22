package logging;

import gui.figure.series.XYSeries;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parameter.AbstractParameter;
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
	
	//Storage for all series that store likelihoods
	private Map<String, XYSeries> likelihoodSeriesMap = new HashMap<String, XYSeries>();
	
	//Storage for series that are from parameters
	private Map<String, XYSeries> paramSeriesMap = new HashMap<String, XYSeries>();
	
	private List<String> seriesNames = new ArrayList<String>();
	
	public MemoryStateLogger() {
		
	}
	
	/**
	 * Obtain a list of the names of all series tracked by this logger
	 * @return
	 */
	public List<String> getSeriesNames() {
		return seriesNames;
	}
	
	/**
	 * Obtain an XY series associated with the given param/likelihood name and logKey
	 * @param name
	 * @param logKey
	 * @return
	 */
	public XYSeries getSeries(String name, String logKey) {
		String serName = name + " - " + logKey;
		return getSeries(serName);
	}

	
	/**
	 * Obtain the XY series associated with the given series name
	 * @param name
	 * @param logKey
	 * @return
	 */
	public XYSeries getSeries(String seriesName) {
		for(String ser : likelihoodSeriesMap.keySet()) {
			if (ser.equals(seriesName))
				return likelihoodSeriesMap.get(ser);
		}
		
		for(String ser : paramSeriesMap.keySet()) {
			if (ser.equals(seriesName)) {
				return paramSeriesMap.get(ser);
			}
		}
		
		return null;
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
			likelihoodSeriesMap.put(comp.getName(), series);
			seriesNames.add(comp.getName());
		}
		
		for(Parameter<?> p : chain.getParameters()) {
			AbstractParameter<?> absParam = (AbstractParameter<?>)p;
			for(int i=0; i<absParam.getKeyCount(); i++) {
				String serName = absParam.getName() + " - " + absParam.getLogKeys()[i];
				XYSeries series = new XYSeries(serName);
				paramSeriesMap.put(serName, series);
				seriesNames.add(serName);
			}
		}
		
		initialized = true;
	}

	@Override
	public void newState(int stateNumber) {
		if (stateNumber % logFrequency == 0) {
			for(LikelihoodComponent comp : chain.getComponents()) {
				String name = comp.getName();
				XYSeries series = getLikelihoodSeries(name);
				Point2D p = new Point2D.Float(stateNumber, new Float(comp.getProposedLogLikelihood()));
				series.addPointInOrder(p);
			}
			
			for(Parameter<?> param : chain.getParameters()) {
				AbstractParameter<?> absParam = (AbstractParameter<?>)param;
				for(int i=0; i<absParam.getKeyCount(); i++) {
					String key = absParam.getLogKeys()[i];
				
					String serName = absParam.getName() + " - " + key;
					XYSeries series = paramSeriesMap.get(serName);
					Object obj = absParam.getLogItem(key);
					Float val = null;
					if (obj instanceof Double) {
						val = new Float((Double)obj);
					}
					if (obj instanceof Float) {
						val = (Float)obj;
					}
					if (obj instanceof Integer) {
						val = new Float( (Integer)obj );
					}
					if (val == null) {
						System.err.println("Cannot find plottable value for param: " + absParam.getName() + " item:" + obj);
					}
					else {
						Point2D p = new Point2D.Float(stateNumber, val);
						series.addPointInOrder(p);
					}
				}
				
				
			}
			
		}
	}
	
	

	/**
	 * Return the series associated with the given name
	 * @param name
	 * @return
	 */
	private XYSeries getLikelihoodSeries(String name) {
		return likelihoodSeriesMap.get(name);
	}

	@Override
	public void chainIsFinished() {
		//Nothing to do
	}
	
}
