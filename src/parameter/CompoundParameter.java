package parameter;

import java.util.ArrayList;
import java.util.List;

import mcmc.AcceptRejectListener;
import modifier.ModificationImpossibleException;

/**
 * A parameter whose state depends on other parameters. We listen for changes in a list of parameters we track,
 * and when one of them changes the proposeNewValue() method is called, wherein we have a chance to recalculate
 * our own value. Not that we don't automatically fire a parameter change when this happens, so the burden is
 * on the implementer of subclasses to fire parameter changes to the likelihoods as appropriate
 * @author brendano
 *
 * @param <T>
 */
public abstract class CompoundParameter<T> extends AbstractParameter<T> implements ParameterListener, AcceptRejectListener {

	protected List<AbstractParameter<?>> parameters = new ArrayList<AbstractParameter<?>>();
	
	public CompoundParameter() {
		setFrequency(0.0);
	}
	
	/**
	 * Add a new parameter to this object, we listen for changes in the given parameter and 
	 * @param newParam
	 */
	public void addParameter(AbstractParameter<?> newParam) {
		if (! parameters.contains(newParam)) {
			newParam.addListener(this);
			parameters.add(newParam);
		}
	}
	
	/**
	 * This gets called when a new value is proposed for one of the parameters we depend on
	 */
	protected abstract void proposeNewValue(Parameter<?> source);
	
	public void stateAccepted() {
		acceptValue();
	}
	
	public void stateRejected() {
		//revertValue(); //revert and fire a change event
		revertSilently(); //revert without firing
	}
	
	/**
	 * No single value to report by default
	 */
	public String getLogString() {
		return "";
	}

	/**
	 * No log to report by default, subclasses should override this if necessary
	 * @return
	 */
	public String getLogHeader() {
		return "";
	}
	
	
	/**
	 * These get fired whenever parameters we listen to have a new value proposed OR are reverted.
	 * If a new value has been proposed, then we 
	 * @param source
	 */
	public void parameterChanged(Parameter<?> source) throws ModificationImpossibleException {			
		if (source.isProposed()) {
			newValuesCount++;
			proposeNewValue(source); //Will fire a parameter change
		}
	}
	
}
