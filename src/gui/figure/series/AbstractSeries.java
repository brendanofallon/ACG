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


package gui.figure.series;

/**
 * Implements the idea of a named list of points, where the points have a x- and y-labels of some sort. Various 
 * subclasses implement series with double-valued points for ordinal series or string=valued x-values and double-valued
 * y values for categorical data. 
 * @author brendan
 *
 */
public abstract class AbstractSeries {

	float weight; 
	String name;
	
	public AbstractSeries() {
		weight = 1.0f;
		name = "Untitled series";
	}
	
	public abstract int size();
	
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	

	public void setWeight(int w) {
		if (w<0) 
			w = 1;
		weight = w;
	}
	

	
	public float getWeight() {
		return weight;
	}
	
}
