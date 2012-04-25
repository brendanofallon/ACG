package newgui.gui.display.resultsDisplay;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import newgui.datafile.XMLConversionError;
import newgui.datafile.resultsfile.ResultsFile;
import newgui.datafile.resultsfile.ResultsFile.ModInfo;
import newgui.gui.ViewerWindow;
import newgui.gui.widgets.MeterBar;

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
		SummaryTableModel data = (SummaryTableModel)summaryTable.getModel();
		
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
			
			

			List<ModInfo> modInfoList = resFile.getModifierData();
			ModifierTableModel modModel = (ModifierTableModel) modifierTable.getModel();
			for(ModInfo mod : modInfoList) {
				modModel.addRow(mod.getLabel(), mod.getCalls(), mod.getAcceptRatio());
			}
			
		} catch (XMLConversionError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//table.setModel(data);
		summaryTable.repaint();
	}
	
	private static String parseDurationString(String timeStr) {
		long ms = Long.parseLong(timeStr);
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		
		hours = (int)Math.floor(ms / (1000.0 * 60.0 * 60.0));
		minutes = (int)Math.floor(ms / (1000.0 * 60.0 ))   % 60;
		seconds = (int)Math.floor(ms / 1000.0 ) % 60;
		if (hours > 0) {
			return hours + " hours, " + minutes + " minutes and " + seconds + " seconds";
		}
		return minutes + " minutes and " + seconds + " seconds";
	}

	private void initComponents() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		topPanel.add(Box.createRigidArea(new Dimension(20, 40)));
		JLabel lab = new JLabel("Run summary");
		lab.setFont(ViewerWindow.sansFont.deriveFont(18f));
		topPanel.add(lab);
		topPanel.add(Box.createHorizontalGlue());
		
		this.add(topPanel);
		
		SummaryTableModel summaryModel = new SummaryTableModel();
		summaryTable = new JTable(summaryModel);
		summaryTable.setTableHeader(null);
		summaryTable.setCellSelectionEnabled(false);
		summaryTable.setShowHorizontalLines(false);
		summaryTable.setShowVerticalLines(false);
		summaryTable.setOpaque(false);
		summaryTable.setBackground(this.getBackground());
		summaryTable.setIntercellSpacing(new Dimension(10, 2));
		TableColumn firstCol = summaryTable.getColumnModel().getColumn(0);
		firstCol.setPreferredWidth(200);
		firstCol.setMaxWidth(300);
		SummaryCellRenderer renderer = new SummaryCellRenderer();
		renderer.setFont(ViewerWindow.sansFont.deriveFont(15f));
		firstCol.setCellRenderer(renderer);
				
		this.add(summaryTable);
		this.add(Box.createVerticalStrut(10));
		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
		sep.setMaximumSize(new Dimension(1000, 20));
		this.add(sep);
		this.add(Box.createVerticalStrut(25));
		JPanel modLabelPanel = new JPanel();
		modLabelPanel.setLayout(new BoxLayout(modLabelPanel, BoxLayout.X_AXIS));
		JLabel modTitle = new JLabel("Modifier summary");
		modTitle.setFont(ViewerWindow.sansFont.deriveFont(18f));
		modLabelPanel.add(Box.createHorizontalStrut(10));
		modLabelPanel.add(modTitle);
		modLabelPanel.add(Box.createHorizontalGlue());
		modLabelPanel.setMaximumSize(new Dimension(1000, 40));
		this.add(modLabelPanel);
		this.add(Box.createVerticalStrut(10));
		
		String[] columnHeaders = new String[]{"Label", "Proposals", "% Accepted", ""}; 
		ModifierTableModel modModel = new ModifierTableModel(columnHeaders);
		modifierTable = new JTable(modModel);
//		JScrollPane modTableSP = new JScrollPane(modifierTable);
//		modTableSP.setPreferredSize(new Dimension(500, 200));
		//modTableSP.setMaximumSize(new Dimension(2000, 250));
//		modTableSP.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 40));
//		modTableSP.setViewportBorder(null);
		modifierTable.setTableHeader(null);
		modifierTable.setCellSelectionEnabled(false);
		modifierTable.setShowHorizontalLines(false);
		modifierTable.setShowVerticalLines(false);
		modifierTable.setIntercellSpacing(new Dimension(10, 2));
		modifierTable.setOpaque(false);
		modifierTable.setBackground(this.getBackground());
		
		TableColumn col = modifierTable.getColumnModel().getColumn(0);
		col.setPreferredWidth(100);
		col.setMaxWidth(200);
		col.setCellRenderer(new SummaryCellRenderer());
		
		col = modifierTable.getColumnModel().getColumn(1);
		col.setPreferredWidth(60);
		col.setMaxWidth(150);		
		
		col = modifierTable.getColumnModel().getColumn(2);
		col.setPreferredWidth(50);
		col.setMaxWidth(100);	
		
		col = modifierTable.getColumnModel().getColumn(3);
		col.setCellRenderer(new MeterBarCellRenderer());
		col.setPreferredWidth(200);
		col.setMaxWidth(200);		
		
		this.add(modifierTable);
		
		this.add(Box.createVerticalGlue());
		
	}
	
	class SummaryCellRenderer extends JLabel implements TableCellRenderer {

		public SummaryCellRenderer() {
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
	

	class MeterBarCellRenderer extends MeterBar implements TableCellRenderer {

		final Color badColor = Color.red;
		final Color warningColor = Color.yellow;
		final Color okColor = new Color(0, 200, 0);
		final JLabel label = new JLabel("");
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			if (value instanceof String) {
				label.setText(value.toString());
				return label;
			}
			
			Double val = (Double)value;
			this.setValue(val);
			this.setBarColor(okColor);
			if (val < 0.1 || val > 0.7) 
				this.setBarColor(warningColor);
			if (val < 0.05 || val > 0.9) 
				this.setBarColor(badColor);


			return this;
		}
		
	}

	private JTable summaryTable;
	private JTable modifierTable;
	
}
