package figure.series;

import java.awt.Color;
import java.util.Iterator;

/**
 * Implements the idea of a named list of points, where the points have a x- and y-labels of some sort. Various 
 * subclasses implement series with double-valued points for ordinal series or string=valued x-values and double-valued
 * y values for categorical data. 
 * @author brendan
 *
 */
public abstract class AbstractSeries {

	//Color color;
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
