package parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logging.LogItemProvider;
import math.RandomSource;
import modifier.AbstractModifier;
import modifier.ModificationImpossibleException;
import modifier.Modifier;

/**
 * A convenient base class for parameters that includes a few common fields and methods. 
 * @author brendan
 *
 */
public abstract class AbstractParameter<T> implements Parameter<T>, LogItemProvider {

	public static final String XML_PARAM_FREQUENCY = "frequency";
	
	protected List<ParameterListener> listeners = new ArrayList<ParameterListener>(3);
	
	protected List<Modifier> modifiers = new ArrayList<Modifier>(2);
	
	protected T proposedValue;
	
	protected T currentValue;
	
	protected T activeValue;

	protected int newValuesCount = 0; //The number of times setValue has been called
	
	protected int rejectedValuesCount =0; //The number of times  
	
	//Relative probability that this parameter will be picked to modify
	protected double frequency = 1.0;
	
	protected boolean modFrequenciesKnown = false;
	protected double modFrequencySum = 0;
	
	protected Map<String, String> attrs = new HashMap<String, String>();
	
//	public AbstractParameter() {
//		//Intentionally blank, sample frequency will be 1.0
//	}
	
	public AbstractParameter(Map<String, String> attrs) {
		String freqStr = attrs.get(XML_PARAM_FREQUENCY);
		this.attrs = attrs;
		if (freqStr != null) {
			try {
				frequency = Double.parseDouble(freqStr);
				if (frequency < 0) {
					throw new IllegalArgumentException("Parameter sample frequency must be greater than zero");
				}
			}
			catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("Could not parse a number for parameter sample frequency, got : " + freqStr);
			}
		}

	}
	
	/**
	 * Get the attribute associated with the given key
	 * @param key
	 * @return
	 */
	public String getAttribute(String key) {
		return attrs.get(key);
	}
	
	public void setFrequency(double frequency) {
		if (frequency < 0)
			throw new IllegalArgumentException("Frequency for parameter " + getName() + " must be > 0 (got " + frequency + ") ");
		this.frequency = frequency;
	}
	
	/**
	 * Get the active value of this parameter, which is the current value unless a new value has been
	 * proposed and neither accept nor reject has been called. 
	 */
	public T getValue() {
		return activeValue;
	}
	
	/**
	 * Set the value of the parameter to the new value.  
	 * @param newValue
	 * @throws ModificationImpossibleException 
	 * @throws InvalidParameterValueException 
	 */
	public void proposeValue(T newValue) throws ModificationImpossibleException {
		proposedValue = newValue;
		activeValue = proposedValue;
		newValuesCount++;
		fireParameterChange();
	}
	
	/**
	 * Returns true if this active value is the proposed value
	 * @return
	 */
	public boolean isProposed() {
		return activeValue == proposedValue;
	}
	
	/**
	 * Set the relative probability that this parameter will be picked to modify
	 * @param freq
	 */
	public void setSampleFrequency(double freq) {
		frequency = freq;
	}
	
	/**
	 * Obtain the relative probability of picking this parameter when the MCMC is looking for one to modify
	 * @return
	 */
	public double getSampleFrequency() {
		return frequency;
	}
	
	/**
	 * Returns true if the current value is equal to the proposed value. This should always be the case after a call to acceptValue or rejectValue,
	 * In general, it should be illegal to modify parameters if this is not true. 
	 * @return
	 */
	public boolean isProposeable() {
		return activeValue == currentValue;
	}
	
	/**
	 * Called when the state proposed by the mcmc has been accepted. Simple parameters can safely ignore this, but more complex parameters
	 * (such as trees) may need to be notified of this. 
	 */
	public void acceptValue() {
		if (activeValue == proposedValue) {
			T tmp = currentValue;
			currentValue = proposedValue;
			proposedValue = tmp;
			activeValue = currentValue;			
		}
	}
	
	/**
	 * Reverts the state of this parameter to that immediately prior to 
	 * the last call to setValue. 
	 */
	public void revertValue() {
		revertSilently();
		try {
			//Right now this is REQUIRED, since some things that listen to these parameters (for instance, mutation models)
			//must know to recalculate their intermediates when parameters they rely on have changed
			fireParameterChange(); 
		} catch (ModificationImpossibleException e) {
			throw new IllegalStateException("Uh-oh, exception thrown on reversion of parameter " + this + "\n Message : " + e);
		}
	}
	
	/**
	 * Reverts the state of this parameter to that immediately prior to 
	 * the last call to setValue, but does not fire a parameter change event. 
	 */
	public void revertSilently() {
		activeValue = currentValue;
		rejectedValuesCount++;
	}
	
	/**
	 * Return a String representation of this parameter suitable for logging
	 * @return
	 */
	public String getLogString() {
		String[] logKeys = getLogKeys();
		StringBuilder strB = new StringBuilder();
		
		for(int i=0; i<logKeys.length-1; i++)
			strB.append( getLogItem(logKeys[i]) + "\t");
		
		strB.append(getLogItem(logKeys[logKeys.length-1] + " "));
		
		return strB.toString();
	}

	/**
	 * This string that is displayed on at the top of the log for this parameter
	 * @return
	 */
	public String getLogHeader() {
		StringBuffer buf = new StringBuffer();
		String[] logKeys = getLogKeys();
		for(int i=0; i<logKeys.length-1; i++)
			buf.append(logKeys[i] + "\t");
		
		buf.append(logKeys[logKeys.length-1] + " ");
		
		return buf.toString();
	}
	
	public void addModifier(Modifier mod) {
		mod.setParameter(this);
		modifiers.add(mod);
		modFrequenciesKnown = false;
	}

	/**
	 * Remove the given modifier from this parameter. 
	 * @param modToRemove
	 * @return
	 */
	public boolean removeModifier(Modifier<?> modToRemove) {
		boolean in = modifiers.remove(modToRemove);
		if (in) {
			modToRemove.setParameter(null);
			modFrequenciesKnown = false;	
		}
		return in;
	}
	
	public int getModifierCount() {
		return modifiers.size();
	}
	
	public Modifier<?> getModifier(int which) {
		return modifiers.get(which);
	}
	
	/**
	 * Returns a modifier picked with probability equal to it's relative frequency
	 * @return
	 */
	public Modifier<?> pickModifier() {
		if (! modFrequenciesKnown) {
			double sum = 0;
			for(Modifier<?> m : modifiers)
				sum += m.getFrequency();
			modFrequencySum = sum;
			modFrequenciesKnown = true;
		}
		
		double r = RandomSource.getNextUniform();
		Modifier<?> mod = modifiers.get(0);
		int count = 0;
		while(r > mod.getFrequency()/modFrequencySum) {
			r -= mod.getFrequency()/modFrequencySum;
			count++;
			mod = modifiers.get(count);
		}
		
		return mod;
	}
	
	public boolean addListener(ParameterListener newListener) {
		if (! listeners.contains(newListener)) {
			listeners.add(newListener);
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Remove the given listener from the list of listeners. Returns true if
	 * the listener was in the list. 
	 */
	public boolean removeListener(ParameterListener toRemove) {
		return listeners.remove(toRemove);
	}

	/**
	 * Notify all listeners that this parameter has changed its value. 
	 * @throws ModificationImpossibleException 
	 */
	public void fireParameterChange() throws ModificationImpossibleException {
		for(ParameterListener l : listeners) {
			l.parameterChanged(this);
		}
	}
	
	/**
	 * Returns the number of times setValue has been called
	 * @return
	 */
	public int getCalls() {
		return this.newValuesCount;
	}
	
	/**
	 * Return number of times that revertValue has been called
	 * @return
	 */
	public int getRejections() {
		return this.rejectedValuesCount;
	}
	
	/**
	 * Returns 1.0 - getRejections / getCalls, which is equal to the fraction of proposed states
	 * that were not rejected, aka the acceptance ratio
	 * @return
	 */
	public double getAcceptanceRatio() {
		return (double)(newValuesCount - rejectedValuesCount)/(newValuesCount);
	}
	
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(this.getClass() + " modifiers: " + modifiers.size() + "  value = " + proposedValue);
		for(Modifier<?> m : modifiers) {
			str.append("\t" + m + "\n");
		}
		return str.toString();
	}
	
	
	
	
	/********************* Default LogItemProvider implementation *************************/
	
	/**
	 * Obtain number of loggable items provided by this parameter.
	 * Default implementation is a single item.
	 */
	public int getKeyCount() {
		return 1;
	}
	
	/**
	 * Returns a list of keys associated with the loggable items of this param. 
	 * Default implementation is a single item whose key is 'getName()'
	 */
	public String[] getLogKeys() {
		String[] keys = new String[getKeyCount()];
		keys[0] = getName();
		return keys;
	}
	
	/**
	 * Return the current value of the log item associated with the given key.
	 * Default implementation is to always just return currentValue
	 */
	public Object getLogItem(String key) {
		return activeValue;
	}
	
}
