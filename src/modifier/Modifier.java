package modifier;

import parameter.InvalidParameterValueException;
import parameter.Parameter;

public interface Modifier<T extends Parameter> {

	/**
	 * Perform some operation on the parameter associated with this modifier
	 * @return
	 */
	public Double modify() throws InvalidParameterValueException, IllegalModificationException, ModificationImpossibleException;

	/**
	 * Set the parameter associated with this modifier. Subsequent calls to .modify 
	 * will work on this parameter. 
	 * @param param
	 */
	public void setParameter(T param);
	
	/**
	 * Modifiers can specify a frequency with which they're picked relative to other modifiers of the same parameter.
	 * The default is 1.0
	 * @return
	 */
	public double getFrequency();
	
	/**
	 * Should be called when a new state is accepted, so we can tune
	 */
	public void tallyAcceptance();
	
	/**
	 * Called when state has been rejected
	 */
	public void tallyRejection();
	
	/**
	 * Get the total number of times this modifier has been used
	 * @return
	 */
	public int getCalls();
	
	/**
	 * Get the total number of times this modifier has proposed a state that was accepted
	 * @return
	 */
	public int getTotalAcceptances();
	
	/**
	 * Get the over (since inception) acceptance ratio
	 * @return
	 */
	public double getTotalAcceptanceRatio();
	
	/**
	 * A debugging function that returns a string describing the most recent operation
	 */
	public String getModStr();

	
}
