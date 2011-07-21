package figure.rowFigure;

import java.util.ArrayList;
import java.util.List;

/**
 * A figure composed of a bunch of horizontal rows each of which spans the entire width
 * of the figure. SequenceFigure is a subclass. 
 * 
 * @author brendan
 *
 */
public class RowFigure extends figure.Figure {

	double defaultRowHeight = 0.1;
	
	List<RowElement> rows;
	
	public RowFigure() {
		rows = new ArrayList<RowElement>();
	}
	
	public void removeRow(RowElement row) {
		rows.remove(row);
		elements.remove(row);
	}
	
	public void addRow(RowElement row) {
		rows.add(row);
		addElement(row);
		revalidateRows();
		listHeights();
	}
	
	
	public void listHeights() {
		for(int i=0; i<rows.size(); i++) {
			System.out.println("Row : " + i + " y: " + rows.get(i).getBounds().y + " height: " + rows.get(i).getBounds().height);
		}
		System.out.println("Figure width: " + this.getWidth() + " height: " + this.getHeight());
	}
	/**
	 * 
	 */
	public void revalidateRows() {
		int sumHeights = 0;
		for(int i=0; i<rows.size(); i++) {
			sumHeights+=rows.get(i).getHeight();
		}
		
		double factor = 1.0;
		if (sumHeights>1.0) {
			factor = sumHeights;
		}
		
		double sum = 0;
		for(int i=0; i<rows.size(); i++) {
			rows.get(i).setPosition(0, sum);
			rows.get(i).setHeight( rows.get(i).getHeight()/factor );
			sum+=rows.get(i).getHeight();
		}
		
	}
}
