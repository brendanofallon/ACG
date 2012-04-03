package newgui.gui.display.resultsDisplay;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import newgui.datafile.XMLConversionError;
import newgui.datafile.resultsfile.ResultsFile;
import newgui.gui.ViewerWindow;

/**
 * A panel that displays some summary info for a completed analysis, typically
 * read in from the properties map of a results file
 * @author brendan
 *
 */
public class RunSummaryPanel extends JPanel {

	public RunSummaryPanel() {
		initComponents();
	}

	public void initialize(ResultsFile resFile) {
		SummaryTableModel data = (SummaryTableModel)table.getModel();
		
		Map<String, String> resProps;
		try {
			resProps = resFile.getPropertiesMap();
			data.addRow("Start time", resProps.get(ResultsFile.MCMC_STARTTIME));
			data.addRow("End time", resProps.get(ResultsFile.MCMC_ENDTIME));
			data.addRow("Run length", resProps.get(ResultsFile.MCMC_RUNLENGTH));
			String durStr = parseDurationString(resProps.get(ResultsFile.MCMC_RUNTIMEMS));
			data.addRow("Duration", durStr);
			
			DecimalFormat formatter = new DecimalFormat("#0.00");
			String ratioStr = formatter.format( Double.parseDouble(resProps.get(ResultsFile.MCMC_ACCEPTEDRATIO)));
			data.addRow("States Accepted", resProps.get(ResultsFile.MCMC_ACCEPTED) + " (" + ratioStr + "%)");
			

		} catch (XMLConversionError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//table.setModel(data);
		table.repaint();
	}
	
	private static String parseDurationString(String timeStr) {
		long ms = Long.parseLong(timeStr);
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		
		hours = (int)Math.round(ms / (1000.0 * 60.0 * 60.0));
		
		minutes = (int)Math.round(ms / (1000.0 * 60.0 ))   % 60;
			
		seconds = (int)Math.round(ms / 1000.0 ) % 60;
		if (hours > 0) {
			return hours + " hours, " + minutes + " minutes and " + seconds + " seconds";
		}
		return minutes + " minutes and " + seconds + " seconds";
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		topPanel.add(Box.createRigidArea(new Dimension(20, 40)));
		JLabel lab = new JLabel("Run summary");
		lab.setFont(ViewerWindow.sansFont.deriveFont(18f));
		topPanel.add(lab);
		
		this.add(topPanel, BorderLayout.NORTH);
		
		SummaryTableModel summaryModel= new SummaryTableModel();
		table = new JTable(summaryModel);
		JScrollPane tableSP = new JScrollPane(table);
		tableSP.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 40));
		tableSP.setViewportBorder(null);
		table.setTableHeader(null);
		table.setCellSelectionEnabled(false);
		table.setShowHorizontalLines(false);
		table.setShowVerticalLines(false);
		//table.setIntercellSpacing(new Dimension(6, 10));
		TableColumn firstCol = table.getColumnModel().getColumn(0);
		firstCol.setPreferredWidth(100);
		firstCol.setMaxWidth(300);
		CellRenderer renderer = new CellRenderer();
		renderer.setFont(ViewerWindow.sansFont.deriveFont(14f));
		firstCol.setCellRenderer(renderer);
		
		
		
		this.add(tableSP, BorderLayout.CENTER);
		
		
	}
	
	class CellRenderer extends JLabel implements TableCellRenderer {

		public CellRenderer() {
			this.setHorizontalAlignment(RIGHT);
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			this.setText(value.toString());
			
			return this;
		}
		
	}
	
	
	private JTable table;
	
}
