/********************************************************************
*
* 	Copyright 2011 Brendan O'Fallon
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
***********************************************************************/


package parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public CompoundParameter(Map<String, String> attrs) {
		super(attrs);
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
	
	
	
	/**
	 * Generally speaking CompoundParameters do not have a single loggable value, so
	 * by default the LogKeys system has been overridden to not provide any info. 
	 */
	
	/**
	 * Compound parameters typically do not have any loggable items
	 */
	public int getKeyCount() {
		return 0;
	}
	
	/**
	 * Returns a zero-length array - CompoundParameters typically do not have any logKeys 
	 * 
	 */
	public String[] getLogKeys() {
		return emptyStr;
	}
	
	/**
	 * By default this returns null
	 */
	public Object getLogItem(String key) {
		return null;
	}
	
	private final String[] emptyStr = new String[]{};
}
