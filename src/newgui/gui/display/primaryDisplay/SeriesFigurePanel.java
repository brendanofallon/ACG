package newgui.gui.display.primaryDisplay;

import gui.figure.TextElement;
import gui.figure.series.HistogramSeries;
import gui.figure.series.XYSeries;
import gui.figure.series.XYSeriesElement;
import gui.figure.series.XYSeriesFigure;
import gui.monitors.MonitorPanel;
import gui.widgets.FloatingPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import newgui.UIConstants;
import newgui.gui.widgets.AbstractSeriesPanel;
import newgui.gui.widgets.BorderlessButton;
import newgui.gui.widgets.HighlightButton;
import newgui.gui.widgets.ToggleButton;

import logging.MemoryStateLogger;

import parameter.AbstractParameter;
import xml.XMLLoader;

public class SeriesFigurePanel extends AbstractSeriesPanel implements ActionListener {

	private MemoryStateLogger memLogger; //Logger that stores information about param values and likelihoods
	
	/**
	 * Set the memory logger that will be used to find series information
	 * @param logger
	 */
	public void setMemoryLogger(MemoryStateLogger logger) {
		this.memLogger = logger;
		
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
		
		
		ToggleButton histoToggle = new ToggleButton(UIConstants.getIcon("gui/icons/histoToggleLeft.png"), UIConstants.getIcon("gui/icons/histoToggleRight.png"));
		histoToggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showHistogram = ! showHistogram;
				switchHistogramTrace();
			}
		});
		addOptionsComponent(histoToggle);
		
		repaintTimer = new Timer(500, this);
		repaintTimer.start();
	}


	/**
	 * Switches to histogram mode by removing all current series from the figure and adding
	 * a newly created HistogramSeries using the currently selected series as the data source
	 */
	protected void switchHistogramTrace() {
		fig.removeAllSeries();
		String seriesName = (String) chooseBox.getSelectedItem();

		if (burninMessage != null && memLogger.getBurninExceeded()) {
			fig.removeElement(burninMessage);
			burninMessage = null;
		}
		
		if (showHistogram) {
			if ( (!memLogger.getBurninExceeded()) && burninMessage == null) {
				burninMessage = new TextElement("(Burnin period not yet exceeded)", fig);
				burninMessage.setPosition(0.4, 0.4);
				fig.addElement(burninMessage);
			}	
			
			HistogramSeries histo = memLogger.getHistogram(seriesName);
			XYSeriesElement histoEl = addSeries(histo);
			histoEl.setMode(XYSeriesElement.BOXES);
						
			fig.setYLabel("Frequency");
			fig.setXLabel("Value");
		}
		else {
			addSelectedSeries();
		}
		
		
		fig.inferBoundsFromCurrentSeries();
	}

	/**
	 * Add the series that is currently selected in the "ChooseBox". This adds both
	 * the burn-in and the values series
	 */
	protected void addSelectedSeries() {
		fig.removeAllSeries();
		String seriesName = (String) chooseBox.getSelectedItem();

		if (burninMessage != null && memLogger.getBurninExceeded()) {
			fig.removeElement(burninMessage);
			burninMessage = null;
		}
		
		if (showHistogram) {
			if ( (!memLogger.getBurninExceeded()) && burninMessage == null) {
				burninMessage = new TextElement("(Burnin period not yet exceeded)", fig);
				burninMessage.setPosition(0.4, 0.4);
				fig.addElement(burninMessage);
			}	
			
			HistogramSeries histo = memLogger.getHistogram(seriesName);
			XYSeriesElement histoEl = addSeries(histo);
			histoEl.setMode(XYSeriesElement.BOXES);
			
			fig.setYLabel("Frequency");
			fig.setXLabel("Value");
		}
		else {
			XYSeries burnin = memLogger.getBurninSeries(seriesName);
			addSeries(burnin, Color.gray);

			XYSeries series = memLogger.getSeries(seriesName);
			addSeries(series);

			//Never show the burnin message when we're drawing traces
			if (burninMessage != null) {
				fig.removeElement(burninMessage);
			}

			fig.setYLabel("Value");
			fig.setXLabel("State");
			fig.inferBoundsFromCurrentSeries();
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
			fig.removeElement(burninMessage);
			burninMessage = null;
		}
		if (memLogger.getChainIsDone()) {
			repaintTimer.stop();
		}
		fig.inferBoundsFromCurrentSeries();
		fig.repaint();
	}
	
	private JComboBox chooseBox;
	private Timer repaintTimer = null;
	
	TextElement burninMessage = null;
	private boolean showHistogram = false; //If true, draw histogram instead of trace


}
