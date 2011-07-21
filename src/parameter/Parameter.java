package parameter;

import modifier.ModificationImpossibleException;
import modifier.Modifier;

public interface Parameter<T> {

	
	public T getValue();
	
	/**
	 * Set the parameter value to the supplied value
	 * @param newValue
	 * @throws InvalidParameterValueException
	 * @throws ModificationImpossibleException 
	 */
	public void proposeValue(T newValue) throws InvalidParameterValueException, ModificationImpossibleException;
	
	/**
	 * A user-recognizable text description of this parameter
	 * @return
	 */
	public String getName();
	
	/**
	 * A single-word description of this parameter without spaces or tabs, used as a header for 
	 * the log file. In abstract parameter, this defaults to getName(), for better or worse
	 * @return
	 */
	public String getLogHeader();

	/**
	 * Returns true if a new value has been proposed, but neither accept or reject has been called
	 * @return
	 */
	public boolean isProposed();
	
	/**
	 * Obtain a string representation of this parameter suitable for logging
	 * @return
	 */
	public String getLogString();
	
	/**
	 * Revert the state of this parameter to that immediately prior to the last call to setValue.
	 */
	public void revertValue();
	
	/**
	 * Add a new parameterListener to listen for changes. In general we hope to not have multiple identical listeners for the same
	 * parameter, so implementations should check to make sure the newListener is not already in the list. The returned value should
	 * be true if the newListener was successfully added to the list.  
	 */
	public boolean addListener(ParameterListener newListener); 
	
	/**
	 * Remove the given listener from the list of listeners. Returns true if the removal was successful (if the listener
	 * was in the list). 
	 * @param toRemove ParameterListener to remove
	 * @return True if the listener was in the list and is now removed. 
	 */
	public boolean removeListener(ParameterListener toRemove);
	
	
	/**
	 * Called to notify this parameter that the most recent proposed value has been accepted, and thus should be made the current value 
	 */
	public void acceptValue();
	
	
	/**
	 * Return the Modifier at index 'which' 
	 * @param which
	 * @return
	 */
	public Modifier getModifier(int which);
	
	/**
	 * Return the number of modifiers associated with this parameter. 
	 * @return The number of modifiers
	 */
	public int getModifierCount();
	
}
