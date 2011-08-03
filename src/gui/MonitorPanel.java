package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import logging.StringUtils;
import mcmc.MCMC;

import component.LikelihoodComponent;
import gui.figure.FigureElement;
import gui.figure.series.HistogramSeries;
import gui.figure.series.XYSeries;
import gui.figure.series.XYSeriesElement;
import gui.figure.series.XYSeriesFigure;
import parameter.AbstractParameter;
import parameter.DoubleParameter;

public abstract class MonitorPanel extends JPanel {

	
	enum Mode {TRACE, HISTOGRAM};
	
	XYSeriesFigure traceFigure;
	XYSeries[] series;
	HistogramSeries[] histoSeries;
	String[] titles;
	String logKey = null;
	Mode mode = Mode.TRACE;

	//A reference to the current (cold) chain. This will change over time in an MC3 analysis
	private MCMC currentChain;
	private boolean chainHasChanged = false;

	int topLabelSize = 11;
	Font topLabelFont = new Font("Sans", Font.PLAIN, topLabelSize);
	
	/**
	 * Set the current chain for the analysis. Monitors will get their parameter values from this chain. 
	 * @param currentChain
	 */
	public void setChain(MCMC chain) {
		if (chain != currentChain) {
			chainHasChanged = true;
		}
		this.currentChain = chain;
	}
	
	
	
	/**
	 * Returns the current chain of the analysis - this is the cold chain in an MC3 analysis or just
	 * the single chain in a normal run. 
	 * @return
	 */
	public MCMC getCurrentChain() {
		return currentChain;
	}
	
	public boolean getChainHasChanged() {
		return chainHasChanged;
	}
	
	/**
	 * Called by the main output frame when we log a new value. The chainHasChanged field is set back to false after
	 * this call. 
	 * @param steps
	 */
	public void updateMonitor(int steps) {
		update(steps);
		chainHasChanged = false;
		
		StringBuilder strB = new StringBuilder();
		
		
		double[] means = getMean();
		StringBuilder meanStr = new StringBuilder();
		for(int i=0; i< means.length; i++) {
			meanStr.append(StringUtils.format(means[i]) + " " );
		}
		topLabel.setText("Mean: " + meanStr + "     Calls: " + getCalls() + "(" + StringUtils.format( 100*getAcceptanceRate() ) + "%) ");
		topLabel.revalidate();
	}
	
	public abstract double[] getMean();
	
	/**
	 * Get the number of times a new value has been proposed for this param / likelihood
	 * @return
	 */
	public abstract int getCalls();
	

	/**
	 * Get the fraction of accepted proposals for this param / likelihood
	 * @return
	 */
	public abstract double getAcceptanceRate();
	
	
	/**
	 * Called when object we're tracking changes so we can append a new values to the series
	 * @param steps
	 */
	protected abstract void update(int steps);
	
	/**
	 * Creates the figure and sets a few defaults for it
	 */
	protected void initializeFigure() {
		this.setLayout(new BorderLayout());
		traceFigure = new XYSeriesFigure();
		this.add(traceFigure, BorderLayout.CENTER);
		traceFigure.setAllowMouseDragSelection(false);
		traceFigure.setXLabel("MCMC state");
		traceFigure.getAxes().setNumXTicks(4);
		traceFigure.getAxes().setNumYTicks(4);
		initializePopup();
		traceFigure.setYLabel(null);
		
		topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		topPanel.setBackground(traceFigure.getBackground());
		topPanel.add(Box.createHorizontalStrut(10));
		topLabel = new JLabel("Mean: ?  Calls: ? (?)");
		
		topPanel.add(topLabel);
		this.add(topPanel, BorderLayout.NORTH);
		
	}
	
	/**
	 * Creates the popup 
	 */
	private void initializePopup() {
		 popup = new JPopupMenu();
		popup.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY) );
		 switchItem = new JMenuItem("Switch to histogram");
		 switchItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchHistoTrace();
			}
		 });
		 popup.add(switchItem);
		 
		 
		 histoOptions = new JMenuItem("Configure histogram");
		 histoOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				popupHistoOptions();
			} 
		 });
		 popup.add(histoOptions);
		 
		 JMenuItem saveImage = new JMenuItem("Save image");
		 saveImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveImage();
			}
		 });
		 
		 popup.add(saveImage);
		 PopupListener popupListener = new PopupListener();
		 this.addMouseListener(popupListener);
		 traceFigure.addMouseListener(popupListener);
	}
	
	

	protected void popupHistoOptions() {
		if (histoSeries == null || histoSeries[0] == null) {
			createHistograms();
		}
		HistoOptionsFrame histoFrame = new HistoOptionsFrame(this, histoSeries);
		histoFrame.setVisible(true);
	}

	/**
	 * Gather an image of the figure and save it to a file
	 */
	protected void saveImage() {
		BufferedImage image = traceFigure.getImage(); 
		
		if (fileChooser == null)
			fileChooser = new JFileChooser( System.getProperty("user.dir"));
		
    	int val = fileChooser.showSaveDialog(this);
    	if (val==JFileChooser.APPROVE_OPTION) {
    		File file = fileChooser.getSelectedFile();
    		try {
    			ImageIO.write(image, "png", file);
    		}
    		catch(IOException ioe) {
    			JOptionPane.showMessageDialog(this, "Error saving image: " + ioe.getLocalizedMessage());
    		}
    	}		
	}

	/**
	 * Create the histogram series that are used to store data for drawing
	 * in XYSeriesElements
	 */
	protected void createHistograms() {
		histoSeries = new HistogramSeries[series.length];
		for(int i=0; i<series.length; i++) {
			HistogramSeries hSeries = new HistogramSeries(titles[i] , series[i].getPointList(), 100, series[i].getMinY(), series[i].getMaxY());
			histoSeries[i] = hSeries;
		}		
	}
	
	
	/**
	 * Called to switch between the Histogram and Trace modes
	 */
	protected void switchHistoTrace() {
		if (mode == Mode.TRACE) { //Current mode is trace, so switch it to histogram
			mode = Mode.HISTOGRAM; //We're now in histogram mode
			traceFigure.removeAllSeries();
			createHistograms();
			
			
			for(int i=0; i<series.length; i++) {
				XYSeriesElement el = traceFigure.addDataSeries(histoSeries[i]);
				el.setMode(XYSeriesElement.BOXES);				
			}

			traceFigure.repaint();
			switchItem.setText("Switch to trace");
			traceFigure.setXLabel(titles[0]);
		}
		else { //Current mode is histogram, so we're switching back to trace
			mode = Mode.TRACE;
			traceFigure.removeAllSeries();
			for(int i=0; i<series.length; i++) {
				traceFigure.addDataSeries(series[i]);
			}
			switchItem.setText("Switch to histogram");
			traceFigure.setXLabel("MCMC State");
		}
		
		traceFigure.repaint();
	}

	public void updateHistogram(HistogramSeries hSeries, Integer bins) {
		//Find the series we're replacing
		int i = 0;
		for(i=0; i<histoSeries.length; i++) {
			if (histoSeries[i] == hSeries) {
				break;
			}
		}
		
		if (i==histoSeries.length) {
			//Couldn't find series we're replacing...abort
			return;
		}
		
		//Find element to remove from figure
		XYSeriesElement el = null;
		for(Object obj : traceFigure.getElementList()) {
			if (obj instanceof XYSeriesElement) {
				XYSeriesElement testEl = (XYSeriesElement)obj;
				if (testEl.getSeries() == hSeries) {
					el = testEl;
					break;
				}
			}
		}
		
		if (el == null) {
			return;
		}
		
		HistogramSeries newSeries = new HistogramSeries(titles[i] , series[i].getPointList(), bins, series[i].getMinY(), series[i].getMaxY());
		el.setSeries(newSeries);				
		histoSeries[i] = newSeries;
		repaint();
	}
	
	/**
	 * Removes every even-indexed elemtent of the series, thus halving it in size
	 * @param series
	 */
	public void thinSeries() {
		for(int j=0; j<series.length; j++) {
			XYSeries ser = series[j];

			//We're actually incrementing the index removed by two, since removing a point increments the index by one itself
			for(int i=0; i<ser.size()/2; i++) {
				ser.removePoint(i);
			}
		}
	}
	
	/**
	 * Returns the number of data points currently in series 0
	 * @return
	 */
	public int getSeriesSize() {
		return series[0].size();
	}
	

	
	
	class PopupListener extends MouseAdapter {
	    public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
	            popup.show(e.getComponent(),
	                       e.getX(), e.getY());
	        }
	    }
	}
	
	static JFileChooser fileChooser;
	private JMenuItem histoOptions;
	private JMenuItem switchItem;
	private JPopupMenu popup;
	private JPanel topPanel;
	JLabel topLabel;

	
	protected float defaultLineWidth = 1.1f;

	
}
