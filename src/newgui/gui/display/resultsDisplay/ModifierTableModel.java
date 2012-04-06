package newgui.gui.display.resultsDisplay;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Model used to store data for the Modifier-info table in the RunSummaryPanel
 * @author brendan
 *
 */
public class ModifierTableModel extends AbstractTableModel {

	List<ModInfo> data = new ArrayList<ModInfo>();
	
	public void addRow(String label, String calls, double acceptRate) {
		ModInfo info = new ModInfo();
		info.label = label;
		info.calls = calls;
		info.acceptRate = acceptRate;
		
		data.add(info);
		this.fireTableRowsInserted(data.size()-1, data.size()-1);
	}
	
	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		ModInfo info = data.get(rowIndex);
		if (columnIndex == 0)
			return info.label;
		if (columnIndex == 1)
			return info.calls;
		if (columnIndex == 2) 
			return info.acceptRate;
		
		return null;
	}
	
	
	class ModInfo {
		String label;
		String calls;
		Double acceptRate;
	}
}
