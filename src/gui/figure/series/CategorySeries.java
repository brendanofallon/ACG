package figure.series;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Implements a non-ordinal set of data, in which values are associated only with string labels. Hence 
 * there is no natural x-ordering of the values.  
 * @author brendan
 *
 */
public class CategorySeries extends AbstractSeries {

	List<String> labels;
	List<Double> data; //Stores all data points and their labels
	
	public CategorySeries() {
		labels = new ArrayList<String>();
		data = new ArrayList<Double>();
	}
	
	/**
	 * Returns an iterator over the keys of the data
	 */
	public Iterator<String> getIterator() {
		return labels.iterator();
	}
	
	public void addPoint(String label, Double value) {
		labels.add(label);
		data.add(value);
	}
	
	public void removePoint(String label) {
		data.remove(label);
	}
	
	public Double get(String label) {
		for(int i=0; i<labels.size(); i++)
			if (labels.get(i).equals(label))
				return data.get(i);
		return null;
	}
	
	public List<String> getLabels() {
		return labels;
	}

	@Override
	public int size() {
		return data.size();
	}

	
	/**
	 * Obtain the data at position i
	 * @param i
	 * @return
	 */
	public double getDataPoint(int i) {
		return data.get(i);
	}
	
}
