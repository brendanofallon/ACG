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


package arg;

/**
 * Things that are associated with a vector of probability states should implement this interface
 * @author brendan
 *
 */
public interface ProbVector {

	public int getStateCount(); //The number of possible character states, typically 4 for nucleotide data
	
	public int getPatternCount(); //The number of patterns this node holds, which is the number of states prob vectors we maintain (don't confuse this with the total number of sites / column in the data matrix)
	
	public double[] getStates(int siteIndex); //Should probably be indexed so we can get the states for site i
	
	public void setStates(double[] newStates, int siteIndex); //
	
}
