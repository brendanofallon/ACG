package newgui.gui.display.resultsDisplay;

import gui.figure.series.HistogramSeries;
import gui.figure.series.XYSeries;
import gui.figure.series.XYSeriesElement;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JComboBox;

import newgui.ErrorWindow;
import newgui.UIConstants;
import newgui.datafile.XMLConversionError;
import newgui.datafile.resultsfile.ResultsFile;
import newgui.datafile.resultsfile.XYSeriesInfo;
import newgui.gui.widgets.AbstractSeriesPanel;
import newgui.gui.widgets.ToggleButton;

/**
 * A panel containing a series figure that shows the traces of various likelihoods
 * and parameters, which the user can select using a combobox
 * @author brendan
 *
 */
public class StateLoggerPanel extends AbstractSeriesPanel {

	protected ResultsFile resFile;
	
	public void initialize(ResultsFile resFile) {
		this.resFile = resFile;

		List<String> seriesNames = resFile.getStateSeriesNames();
		String[] nameArray = seriesNames.toArray(new String[]{});
		chooseBox = new JComboBox(nameArray);
		chooseBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent iv) {
				clearSeries();
				addSelectedSeries();
			}
		});
		
		addOptionsComponent(chooseBox);
		
		addSelectedSeries();
		
		
		ToggleButton histoToggle = new ToggleButton(UIConstants.getIcon("gui/icons/histoToggleLeft.png"), UIConstants.getIcon("gui/icons/histoToggleRight.png"));
		histoToggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showHistogram = ! showHistogram;
				switchHistogramTrace();
			}
		});
		addOptionsComponent(histoToggle);
	}
	
	/**
	 * Switches to histogram mode by removing all current series from the figure and adding
	 * a newly created HistogramSeries using the currently selected series as the data source
	 */
	protected void switchHistogramTrace() {
		seriesFig.removeAllSeries();
		String seriesName = (String) chooseBox.getSelectedItem();
		
		if (showHistogram) {
			
			XYSeries series;
			try {
				series = resFile.getStateSeries(seriesName).getSeries();
				
				//TODO must be user settable
				int bins = 50;
				double min = series.getMinY();
				double max = series.getMaxY();
				HistogramSeries histo = new HistogramSeries(series.getName(), bins, min, max, series.getPointList());
				XYSeriesElement histoEl = addSeries(histo);
				histoEl.setMode(XYSeriesElement.BOXES);
				
				seriesFig.setYLabel("Frequency");
				seriesFig.setXLabel("Value");
				
			} catch (XMLConversionError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		else {
			addSelectedSeries();
		}
		
		
		seriesFig.inferBoundsFromCurrentSeries();
	}
	
	/**
	 * Obtain a string representing the currently displayed series
	 * @return
	 */
	protected String getDataString() {
		StringBuilder strB = new StringBuilder();
		String seriesName = (String) chooseBox.getSelectedItem();
		String sep = System.getProperty("line.separator");
		
		XYSeriesInfo seriesInfo;
		try {
			seriesInfo = resFile.getStateSeries(seriesName);
			strB.append(seriesName + sep);
			XYSeries series = seriesInfo.getSeries();
			for(int i=0; i<series.size(); i++) {
				strB.append(series.getX(i) + "," + series.getY(i) + sep);
			}
			return strB.toString();
		} catch (XMLConversionError e) {
			e.printStackTrace();
			ErrorWindow.showErrorWindow(e, "Error obtaining data to export");
		}
		return "";
	}
		
	/**
	 * Add the series that is currently selected in the "ChooseBox". This adds both
	 * the burn-in and the values series
	 */
	protected void addSelectedSeries() {
		clearSeries();
		setXLabel("MCMC step");
		setYLabel("Value");
		
		String seriesName = (String) chooseBox.getSelectedItem();
		XYSeriesInfo seriesInfo;
		try {
			seriesInfo = resFile.getStateSeries(seriesName);

			if (showHistogram) {

				XYSeries series = resFile.getStateSeries(seriesName).getSeries();

				//TODO must be user settable
				int bins = 50;
				double min = series.getMinY();
				double max = series.getMaxY();
				HistogramSeries histo = new HistogramSeries(series.getName(), bins, min, max, series.getPointList());
				XYSeriesElement histoEl = addSeries(histo);
				histoEl.setMode(XYSeriesElement.BOXES);

				seriesFig.setYLabel("Frequency");
				seriesFig.setXLabel("Value");
			}
			else {
				XYSeriesElement seriesEl = seriesFig.addDataSeries(seriesInfo.getSeries());
				seriesEl.setLineColor(seriesInfo.getColor());
				seriesEl.setLineWidth(seriesInfo.getWidth());
			}
		} catch (XMLConversionError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		seriesFig.inferBoundsFromCurrentSeries();
	}

	private boolean showHistogram = false;
	private JComboBox chooseBox;
	
}
