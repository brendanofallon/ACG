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


	package gui.monitors;


import java.util.List;

import xml.XMLLoader;

import component.LikelihoodComponent;

public class LikelihoodMonitor extends MonitorPanel {

	private LikelihoodComponent comp;
	
	public LikelihoodMonitor(LikelihoodComponent comp, int burnin) {
		super(burnin);
		this.comp = comp;
		initializeFigure();
		initializeSeries(1);
		addSeries("Log likelihood");
		setHeaderLabel(comp.getAttribute(XMLLoader.NODE_ID));
		setShowStdevs(false);
	}
	
	
	/**
	 * Called when the mcmc chain fires a new state ot the MainOutputWindow,
	 * this is where we add new data to the chart 
	 * @param state
	 */
	protected void update(int state) {
		//In MC3 runs the parameter we're listening to changes when the chain are swapped. If the chain has changed, query the new chain
		//to find the parameter with the same NODE_ID as the one we're listening to, and use replace the current param
		//with that one. 
		if ( getChainHasChanged() ) {
			List<LikelihoodComponent> likes = getCurrentChain().getLikelihoods();
			boolean found = false;
			final String id = comp.getAttribute(XMLLoader.NODE_ID);
			if (id == null)
				throw new IllegalStateException("Can't perform chain switch because likelihood " + comp + " doesn't have NODE_ID defined");

			for(LikelihoodComponent like : likes) {
				if (like.getAttribute(XMLLoader.NODE_ID).equals(id)) {
					comp = like;
					found = true;
					break;
				}
			} 

			if (!found) {
				System.out.println("Error : can't find new likelihood in chain");
				throw new IllegalStateException("Could not find likelihood with ID: " + comp.getAttribute(XMLLoader.NODE_ID) + " for chain switch ");
			}
		}

		Double val = comp.getCurrentLogLikelihood();
		addPointToSeries(0, state, val);
	}


	@Override
	public int getCalls() {
		return comp.getProposalCount();
	}


	@Override
	public double getAcceptanceRate() {
		return comp.getAcceptanceRate();
	}
}