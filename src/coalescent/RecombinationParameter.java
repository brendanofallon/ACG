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


package coalescent;

/**
 * Species classes that can act as a recombination parameter, which specify how the recombination rate varies over sites and,
 * potentially, time. 
 * @author brendan
 *
 */
public interface RecombinationParameter {

	/**
	 * The rate of recombination of a single lineage at the given depth in the tree
	 * @param t
	 * @return
	 */
	public double getInstantaneousRate(double t);
	
	/**
	 * The integral of the recombination rate from start to end
	 * @param start
	 * @param end
	 * @return
	 */
	public double getIntegral(double start, double end);
	
	/**
	 * Returns a number proportional to the relative probability that a recombination occurs at the given site. 
	 * @param site
	 * @return
	 */
	public double getSiteProbability(int site);
}
