package newgui.gui.display.resultsDisplay;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Holds data displayed in the main table in a RowSummaryPanel
 * @author brendan
 *
 */
public class SummaryTableModel extends AbstractTableModel {

	private List<StringPair> data = new ArrayList<StringPair>();
	
	public void addRow(String label, String value) {
		data.add(new StringPair(label, value));
		this.fireTableRowsInserted(data.size()-1, data.size()-1);
	}
	
	public void clearData() {
		data.clear();
	}
	
	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0)
			return data.get(rowIndex).first;
		else
			return data.get(rowIndex).second;
	}

	class StringPair {
		String first;
		String second;
		
		public StringPair(String label, String value) {
			this.first = label;
			this.second = value;
		}
	}
}
