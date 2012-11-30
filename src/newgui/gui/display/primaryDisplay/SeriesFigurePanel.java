package newgui.gui.display.primaryDisplay;

import gui.figure.TextElement;
import gui.figure.series.HistogramSeries;
import gui.figure.series.XYSeries;
import gui.figure.series.XYSeriesElement;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.Timer;

import newgui.UIConstants;
import newgui.gui.widgets.AbstractSeriesPanel;
import newgui.gui.widgets.ToggleButton;

import logging.MemoryStateLogger;

public class SeriesFigurePanel extends AbstractSeriesPanel implements ActionListener {

	private MemoryStateLogger memLogger; //Logger that stores information about param values and likelihoods
	
	/**
	 * Set the memory logger that will be used to find series information
	 * @param logger
	 */
	public void setMemoryLogger(MemoryStateLogger logger) {
		this.memLogger = logger;
		
		
		//Choose series box
		List<String> seriesNames = logger.getSeriesNames();
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
		
		
		//Histogram / trace toggle switch
		ToggleButton histoToggle = new ToggleButton(UIConstants.getIcon("gui/icons/histoToggleLeft.png"), UIConstants.getIcon("gui/icons/histoToggleRight.png"));
		histoToggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showHistogram = ! showHistogram;
				addSelectedSeries();
			}
		});
		addOptionsComponent(histoToggle);
		
		repaintTimer = new Timer(500, this);
		repaintTimer.start();
	}



	/**
	 * Add the series that is currently selected in the "ChooseBox". This adds both
	 * the burn-in and the values series
	 */
	protected void addSelectedSeries() {
		seriesFig.removeAllSeries();
		String seriesName = (String) chooseBox.getSelectedItem();

		if (burninMessage != null && memLogger.getBurninExceeded()) {
			seriesFig.removeElement(burninMessage);
			burninMessage = null;
		}
		
		if (showHistogram) {
			if ( (!memLogger.getBurninExceeded()) && burninMessage == null) {
				burninMessage = new TextElement("(Burnin period not yet exceeded)", seriesFig);
				burninMessage.setPosition(0.4, 0.4);
				seriesFig.addElement(burninMessage);
			}	
			
			if(memLogger.getBurninExceeded() && burninMessage != null) {
				seriesFig.removeElement(burninMessage);
			}

			HistogramSeries histo = memLogger.getHistogram(seriesName);
			XYSeriesElement histoEl = addSeries(histo);
			histoEl.setMode(XYSeriesElement.BOXES);

			seriesFig.setYLabel("Frequency");
			seriesFig.setXLabel("Value");
			
		}
		else {
			XYSeries burnin = memLogger.getBurninSeries(seriesName);
			addSeries(burnin, Color.gray);

			XYSeries series = memLogger.getSeries(seriesName);
			addSeries(series);

			//Never show the burnin message when we're drawing traces
			if (burninMessage != null) {
				seriesFig.removeElement(burninMessage);
			}

			seriesFig.setYLabel("Value");
			seriesFig.setXLabel("State");
			seriesFig.inferBoundsFromCurrentSeries();
		}
	}


	@Override
	protected String getDataString() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/**
	 * Gets called when timer ticks so we can repaint series...
	 */
	public void actionPerformed(ActionEvent arg0) {
		if (memLogger.getBurninExceeded() && burninMessage != null) {
			seriesFig.removeElement(burninMessage);
			burninMessage = null;
		}
		if (memLogger.getChainIsDone()) {
			repaintTimer.stop();
		}
		seriesFig.inferBoundsFromCurrentSeries();
		seriesFig.repaint();
	}
	
	private JComboBox chooseBox;
	private Timer repaintTimer = null;
	
	TextElement burninMessage = null;
	private boolean showHistogram = false; //If true, draw histogram instead of trace


}
