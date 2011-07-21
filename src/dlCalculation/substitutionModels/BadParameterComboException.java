package dlCalculation.substitutionModels;

import modifier.ModificationImpossibleException;

/**
 * This gets thrown when the stationaries or other parameters in a mutation model are inconsistent.
 * For instance, in TN93 mutation kappa must be greater than (piA*piG + piC*piT)/(piA+piG)/(piC+piT). 
 * Similarly (equivalently, even), in K2P mutation kappa must be greater than 0.5 (right?) 
 * @author brendano
 *
 */
public class BadParameterComboException extends ModificationImpossibleException {

	public BadParameterComboException(String message) {
		super(message);
	}
	
}
