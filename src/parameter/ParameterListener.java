package parameter;

import modifier.ModificationImpossibleException;

/**
 * Classes implementing this interface may listen for parameter change events. The parameterChanged(...) method is called
 * whenever some aspect of the parameter has been altered
 * @author brendan
 *
 */
public interface ParameterListener {

	public void parameterChanged(Parameter<?> source) throws ModificationImpossibleException;
	
}
