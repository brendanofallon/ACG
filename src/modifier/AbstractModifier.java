package modifier;

import java.util.Map;

import parameter.Parameter;

/**
 * An abstract base class with a few handy functions for modifiers, mostly related to proposal tuning
 * @author brendan
 *
 * @param <T>
 */
public abstract class AbstractModifier<T extends Parameter> implements Modifier<T> {
	
	public static final String XML_FREQUENCY = "frequency";
	
	private int totalCalls = 0;
	private int totalAccepts = 0;
	private int resetFrequency = 2500; //Parameter tuning is generally based on the number of 'recent' acceptances vs calls, we don't want to get swamped by a bunch of super-old noise
	private int callsSinceReset = 0;
	private int acceptsSinceReset = 0;

	//We seek to have the acceptance rate in certain range, as defined by the following two numbers
	protected double lowRatio = 0.2;
	protected double highRatio = 0.3;
	
	//Relative probability that this modifier is picked, conditional on the parameter it belongs to getting chosen
	protected double frequency = 1.0;
	
	//The parameter we modify
	protected T param;
	
	String modStr = "Generic modifier";

	/**
	 * XML-friendly constructor. We look for the optional attribute frequency and attempt to parse a double from its value
	 * @param attributes
	 */
	public AbstractModifier(Map<String, String> attributes) {
		String freq = attributes.get(XML_FREQUENCY);
		if (freq != null) {
			try {
				frequency = Double.parseDouble(freq);
			}
			catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("Could not parse modifier frequency from modifier " + this + ", got : " + freq);
			}
		}
	}
	
	/**
	 * Copy constructor, used by subclasses
	 * @param param
	 * @param lowRatio
	 * @param highRatio
	 * @param frequency
	 */
	protected AbstractModifier(T param, double lowRatio, double highRatio, double frequency) {
		this.param = param;
		this.lowRatio = lowRatio;
		this.highRatio = highRatio;
		this.frequency = frequency;
	}
	
	/**
	 * Set the parameter this modifier modifies
	 */
	public void setParameter(T param) {
		this.param = param;
	}
	
	/**
	 * Obtain the frequency field, which affects how often this modifier is picked to modify its parameter
	 * @return
	 */
	public double getFrequency() {
		return frequency;
	}

	/**
	 * Increment both the total number of acceptances and the number of accepts since the last reset
	 */
	public void tallyAcceptance() {
		totalAccepts++;
		acceptsSinceReset++;
	}
	
	public int getTotalCalls() {
		return totalCalls;
	}
	
	public int getCallsSinceReset() {
		return callsSinceReset;
	}

	public void tallyRejection() {
		
	}
	
	public String getModStr() {
		return modStr;
	}
	
	/**
	 * Set the upper boundary that defines the acceptable acceptance rate for the parameter to the new value. 
	 * @param newHigh
	 */
	protected void setHighRatio(double newHigh) {
		highRatio = newHigh;
	}
	
	/**
	 * Set the lower boundary for acceptable acceptance ratio to the new value
	 * @param newLow
	 */
	protected void setLowRatio(double newLow) {
		lowRatio = newLow;
	}
	
	
	public int getResetFrequency() {
		return resetFrequency;
	}
	
	/**
	 * Set the 'reset frequency', which determines how often the 'recent acceptances' and 'recent calls' fields are reset, 
	 * which affects in turn how often the parameters are tuned
	 * @param newFreq
	 */
	public void setResetFrequency(int newFreq) {
		this.resetFrequency = newFreq;
	}
	
	/**
	 * Returns the fraction of calls since the last reset which have led to acceptances
	 * @return
	 */
	public double getRecentAcceptanceRatio() {
		return (double)acceptsSinceReset / (double)callsSinceReset;
	}
	
	/**
	 * Tally a new 'call' for this modifier, which increments the total number of calls and the number of calls since reset. 
	 * Every 'resetFrequency' calls, calls since reset is forced to zero, so we can track the number of accepted proposals
	 * in a 'recent' window of time 
	 */
	public void tallyCall() {
		totalCalls++;
		callsSinceReset++;
		if (totalCalls > resetFrequency && totalCalls % resetFrequency==0) {
			callsSinceReset = 1;
			acceptsSinceReset = 0;
		}
	}
	
	/**
	 * Returns the total fraction of all calls since the creation of this modifier that have been accepted
	 * @return
	 */
	public double getTotalAcceptanceRatio() {
		return (double)totalAccepts / (double)totalCalls;
	}
	
	/**
	 * Return the total number of calls that have been accepted
	 */
	public int getTotalAcceptances() {
		return totalAccepts;
	}
	
	/**
	 * Return the total number of times this modifier has been used
	 */
	public int getCalls() {
		return totalCalls;
	}

	public String toString() {
		String str = this.getClass().toString();
		int index = str.indexOf(".");
		if (index<0)
			index = 0;
		else 
			index++;
		return str.substring(index);
	}
}
