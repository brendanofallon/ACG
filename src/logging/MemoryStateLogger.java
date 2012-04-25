package logging;

import gui.figure.series.HistogramSeries;
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
 * object and the data is used to produce the charts and graphs shown in a PrimaryDisplay's RunningJobPanel
 * @author brendano
 *
 */
public class MemoryStateLogger implements MCMCListener {

	private int statesToStore = 5000; //Maximum number of values to store for each parameter tracked
	private Integer logFrequency = null; //Computed when we 'initialize'
	private boolean initialized = false;
	private MCMC chain;
	private int ignorePeriod = 10000; //Totally ignore the first few values
	private int burnin = 0;
	private boolean burninExceeded = false; //Turns to true when we're beyond the burnin period
	private boolean isChainDone = false;
	
	
	//Storage for all series that store likelihoods
	private Map<String, SeriesWrapper> likelihoodSeriesMap = new HashMap<String, SeriesWrapper>();
	
	//Storage for series that are from parameters
	private Map<String, SeriesWrapper> paramSeriesMap = new HashMap<String, SeriesWrapper>();
	
	private List<String> seriesNames = new ArrayList<String>();
	
	public MemoryStateLogger() {
		
	}
	
	public boolean getBurninExceeded() {
		return burninExceeded;
	}
	
	/**
	 * Returns true if chainIsFinished has been called
	 * @return
	 */
	public boolean getChainIsDone() {
		return isChainDone;
	}
	
	/**
	 * Set the burn-in period for this memory logger. Results are undefined if this is called
	 * after we start collecting values.
	 *
	 * @param burninLength
	 */
	public void setBurnin(int burninLength) {
		this.burnin = burninLength;
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
				return likelihoodSeriesMap.get(ser).values;
		}
		
		for(String ser : paramSeriesMap.keySet()) {
			if (ser.equals(seriesName)) {
				return paramSeriesMap.get(ser).values;
			}
		}
		
		return null;
	}
	
	public XYSeries getBurninSeries(String seriesName) {
		for(String ser : likelihoodSeriesMap.keySet()) {
			if (ser.equals(seriesName))
				return likelihoodSeriesMap.get(ser).burnin;
		}
		
		for(String ser : paramSeriesMap.keySet()) {
			if (ser.equals(seriesName)) {
				return paramSeriesMap.get(ser).burnin;
			}
		}
		
		return null;
	}
	
	/**
	 * Obtain a HistogramSeries associated with the given series name. The histogram is 
	 * continually updates as the chain progresses. 
	 * @param seriesName
	 * @return
	 */
	public HistogramSeries getHistogram(String seriesName) {
		for(String ser : likelihoodSeriesMap.keySet()) {
			if (ser.equals(seriesName)) {
				SeriesWrapper wrapper = likelihoodSeriesMap.get(ser);
				if (wrapper.histo == null)
					createHistogramForSeries(wrapper);
				return wrapper.histo;
			}
		}
		
		for(String ser : paramSeriesMap.keySet()) {
			if (ser.equals(seriesName)) {
				SeriesWrapper wrapper = paramSeriesMap.get(ser);
				if (wrapper.histo == null)
					createHistogramForSeries(wrapper);
				return paramSeriesMap.get(ser).histo;
			}
		}
		
		return null;
	}
	
	/**
	 * Creates a HistogramSeries for the given series name and stores it in the appropriate SeriesWrapper.
	 * This clobbers the old value in wrapper.histo, if there is one. 
	 * @param ser
	 */
	private void createHistogramForSeries(SeriesWrapper wrapper) {
		double min = wrapper.values.getMinY();
		double max = wrapper.values.getMaxY();
		int bins = 100;
		HistogramSeries histoSeries = new HistogramSeries(wrapper.name, wrapper.values.getPointList(), bins, min, max);
		wrapper.histo = histoSeries;
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
			XYSeries burnin = new XYSeries(comp.getLogHeader() + " (burnin)");
			SeriesWrapper wrapper = new SeriesWrapper();
			wrapper.name = comp.getName();
			wrapper.values = series;
			wrapper.burnin = burnin;
			likelihoodSeriesMap.put(comp.getName(), wrapper);
			seriesNames.add(comp.getName());
		}
		
		for(Parameter<?> p : chain.getParameters()) {
			AbstractParameter<?> absParam = (AbstractParameter<?>)p;
			for(int i=0; i<absParam.getKeyCount(); i++) {
				String serName = absParam.getName() + " - " + absParam.getLogKeys()[i];
				XYSeries series = new XYSeries(serName);
				XYSeries burnin= new XYSeries(serName + " (burnin)");
				SeriesWrapper wrapper = new SeriesWrapper();
				wrapper.name = serName;
				wrapper.values = series;
				wrapper.burnin = burnin;
				paramSeriesMap.put(serName, wrapper);
				seriesNames.add(serName);
			}
		}
		
		initialized = true;
	}

	@Override
	public void newState(int stateNumber) {
		if (stateNumber < ignorePeriod) {
			return;
		}
		if (stateNumber % logFrequency == 0) {
			burninExceeded = stateNumber >= burnin;
			
			for(LikelihoodComponent comp : chain.getComponents()) {
				String name = comp.getName();
				SeriesWrapper wrapper = getLikelihoodSeries(name);
				//XYSeries series = wrapper.values;
				Point2D p = new Point2D.Float(stateNumber, new Float(comp.getProposedLogLikelihood()));
				
				if (! burninExceeded)
					wrapper.burnin.addPointInOrder(p);
				else
					wrapper.values.addPointInOrder(p);
				
				if (wrapper.histo != null) {
					wrapper.histo.addValue(comp.getProposedLogLikelihood());
				}
			}
			
			for(Parameter<?> param : chain.getParameters()) {
				AbstractParameter<?> absParam = (AbstractParameter<?>)param;
				for(int i=0; i<absParam.getKeyCount(); i++) {
					String key = absParam.getLogKeys()[i];
				
					String serName = absParam.getName() + " - " + key;
					SeriesWrapper wrapper = paramSeriesMap.get(serName);
					
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
						throw new IllegalArgumentException("Cannot find plottable value for param: " + absParam.getName() + " item:" + obj);
					}
					else {
						Point2D p = new Point2D.Float(stateNumber, val);
						if (! burninExceeded)
							wrapper.burnin.addPointInOrder(p);
						else
							wrapper.values.addPointInOrder(p);
						if (wrapper.histo != null) {
							wrapper.histo.addValue(val);
						}
						
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
	private SeriesWrapper getLikelihoodSeries(String name) {
		return likelihoodSeriesMap.get(name);
	}

	@Override
	public void chainIsFinished() {
		isChainDone = true;
	}
	
	/**
	 * Stores a variety of data series associated with a given series type 
	 * @author brendano
	 *
	 */
	class SeriesWrapper {
		String name;
		XYSeries burnin = null;
		XYSeries values = null;
		HistogramSeries histo = null;
	}
}
