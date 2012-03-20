/********************************************************************
*
* 	Copyright 2011 Brendan O'Fallon
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
***********************************************************************/


package gui.monitors;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import logging.StringUtils;
import mcmc.MCMC;

import component.LikelihoodComponent;
import gui.figure.FigureElement;
import gui.figure.series.HistogramSeries;
import gui.figure.series.XYSeries;
import gui.figure.series.XYSeriesElement;
import gui.figure.series.XYSeriesFigure;
import gui.widgets.FloatingPanel;
import parameter.AbstractParameter;
import parameter.DoubleParameter;

public abstract class MonitorPanel extends FloatingPanel {

	enum Mode {TRACE, HISTOGRAM};
	
	XYSeriesFigure traceFigure;
	private XYSeries[] burnins;
	private XYSeries[] series;
	private XYSeries[] seriesMeans;
	private XYSeries[] stdUpper;
	private XYSeries[] stdLower;

	private XYSeriesElement[] burninEls;
	private XYSeriesElement[] seriesEls;
	private XYSeriesElement[] meansEls;
	private XYSeriesElement[] stdLowerEls;
	private XYSeriesElement[] stdUpperEls;
	
	
	HistogramSeries[] histoSeries;
	String[] titles;
	protected String logKey = null;
	Mode mode = Mode.TRACE;

	private int addedSeriesCount = 0; //The number of XYSeries that have been added via calls to addSeries.
	
	//A reference to the current (cold) chain. This will change over time in an MC3 analysis
	private MCMC currentChain;
	private boolean chainHasChanged = false;
	
	int topLabelSize = 11;
	Font topLabelFont = new Font("Sans", Font.PLAIN, topLabelSize);
	
	//Determines whether or not we draw the value series, means, and errors
	private boolean showBurnin = true;
	private boolean showValues = true;
	private boolean showMeans = true;
	private boolean showStdevs = true;
	
	//Initial fraction of values in series to ignore
	private final int burnin;
	private boolean burninReached = false;
	
	public static final Color[] seriesColors = new Color[]{Color.blue, Color.green, Color.orange, Color.cyan, Color.magenta, Color.YELLOW, Color.black};
	
	public MonitorPanel(int burnin) {
		this.burnin = burnin;
		setOpaque(false);
	}
	

	/**
	 * Create the arrays that store the data series
	 * @param seriesCount
	 */
	protected void initializeSeries(int seriesCount) {
		burnins = new XYSeries[seriesCount];
		series = new XYSeries[seriesCount];
		seriesMeans = new XYSeries[seriesCount];
		stdLower = new XYSeries[seriesCount];
		stdUpper = new XYSeries[seriesCount];
		titles = new String[seriesCount];
		
		burninEls = new XYSeriesElement[seriesCount];
		seriesEls = new XYSeriesElement[seriesCount];
		meansEls = new XYSeriesElement[seriesCount];
		stdUpperEls = new XYSeriesElement[seriesCount];
		stdLowerEls = new XYSeriesElement[seriesCount];
	}
	
	/**
	 * Set the header string drawn on the top panel
	 * @param label
	 */
	public void setHeaderLabel(String label) {
		topPanel.setLabel(label);
	}
	
	public int addSeries(String seriesName) {
		if (addedSeriesCount == series.length)
			throw new IllegalArgumentException("Already added " + series.length + " series");
		
		titles[addedSeriesCount] = seriesName;
		
		burnins[addedSeriesCount] = new XYSeries(seriesName + " (burnin)");
		XYSeriesElement burnEl = traceFigure.addDataSeries(burnins[addedSeriesCount]);
		burninEls[addedSeriesCount] = burnEl;
		burnEl.setLineWidth(defaultLineWidth);
		burnEl.setLineColor(Color.gray);
		
		series[addedSeriesCount] = new XYSeries(seriesName);
		XYSeriesElement serEl = traceFigure.addDataSeries(series[addedSeriesCount]);
		seriesEls[addedSeriesCount] = serEl;
		serEl.setLineWidth(defaultLineWidth);
		serEl.setLineColor(seriesColors[addedSeriesCount % (seriesColors.length)]);
		
		seriesMeans[addedSeriesCount] = new XYSeries(seriesName + " (mean)");
		XYSeriesElement serMeanEl = traceFigure.addDataSeries(seriesMeans[addedSeriesCount]);
		meansEls[addedSeriesCount] = serMeanEl;
		serMeanEl.setLineWidth(0.8f);
		serMeanEl.setLineColor(Color.RED);
		
		stdUpper[addedSeriesCount] = new XYSeries(seriesName + " (+stdev)");
		XYSeriesElement stdUpperEl = traceFigure.addDataSeries(stdUpper[addedSeriesCount]);
		stdUpperEls[addedSeriesCount] = stdUpperEl;
		stdUpperEl.setLineWidth(0.8f);
		stdUpperEl.setLineColor(Color.RED);
		stdUpperEl.setStroke(new BasicStroke(1.0f,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10.0f, new float[]{10.0f}, 0.0f) );
		
		stdLower[addedSeriesCount] = new XYSeries(seriesName + " (-stdev)");
		XYSeriesElement stdLowerEl = traceFigure.addDataSeries(stdLower[addedSeriesCount]);
		stdLowerEls[addedSeriesCount] = stdLowerEl;
		stdLowerEl.setLineWidth(0.8f);
		stdLowerEl.setLineColor(Color.RED);
		stdLowerEl.setStroke(new BasicStroke(1.0f,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10.0f, new float[]{10.0f}, 0.0f) );
		
		addedSeriesCount++;
		return addedSeriesCount-1;
	}
	
	/**
	 * Get the number of series that have been added to this monitor so far
	 * @return
	 */
	public int getSeriesCount() {
		return addedSeriesCount;
	}
	
	public void setShowBurnin(boolean showBurnin) {
		if (this.showBurnin != showBurnin) {
			this.showBurnin = showBurnin;
			redrawSeries();
		}
	}
	
	public void setShowMeans(boolean showMeans) {
		if (this.showMeans != showMeans) {
			this.showMeans = showMeans;
			redrawSeries();
		}
	}
	
	public void setShowValues(boolean showVals) {
		if (this.showValues != showVals) {
			this.showValues = showVals;
			redrawSeries();
		}
	}
	
	public void setShowStdevs(boolean showStds) {
		if (this.showStdevs != showStds) {
			this.showStdevs = showStds;
			redrawSeries();
		}
	}
	
	public void setShowLegend(boolean showLegend) {
		traceFigure.setShowLegend(showLegend);
		traceFigure.repaint();
	}
	
	private void redrawSeries() {
		traceFigure.removeAllSeries();
		if (mode==Mode.TRACE) {
			for(int i=0; i<series.length; i++) {
				if (showBurnin) {
					traceFigure.addSeriesElement(burninEls[i]);
				}
				if (showValues) {
					traceFigure.addSeriesElement(seriesEls[i]);
				}
				if (showMeans) {
					traceFigure.addSeriesElement(meansEls[i]);
				}
				if (showStdevs) { 
					traceFigure.addSeriesElement(stdUpperEls[i]);
					traceFigure.addSeriesElement(stdLowerEls[i]);
				}
			}
		}
	}

	protected void addPointToSeries(int index, int state, double val) {
		Point2D.Double point = new Point2D.Double(state, val);
		if (state < burnin) {
			burnins[index].addPointInOrder(point);
		}
		else {
			series[index].addPointInOrder(point);
			if (histoSeries != null) {
				if (histoSeries[index] == null)
					createHistograms();
				histoSeries[index].addPointInOrder(point);
			}

			seriesMeans[index].addPointInOrder(new Point2D.Double(state, series[index].getYMean()));
			stdUpper[index].addPointInOrder(new Point2D.Double(state, series[index].getYMean()+series[index].getYStdev()));
			stdLower[index].addPointInOrder(new Point2D.Double(state, series[index].getYMean()-series[index].getYStdev()));
		}
		
		traceFigure.inferBoundsPolitely();
		traceFigure.repaint();
	}
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
		
		//When burnin is reached remove burnin series from legend
		if (! burninReached && steps > burnin) {
			for(int i=0; i<burninEls.length; i++)
				burninEls[i].setIncludeInLegend(false);
			burninReached = true;
		}
		
		if (series != null && series.length > 0) {
			double[] means = new double[seriesMeans.length];
			for(int i=0; i<seriesMeans.length; i++)
				means[i] = seriesMeans[i].lastYValue();

			StringBuilder meanStr = new StringBuilder();
			for(int i=0; i< seriesMeans.length; i++) {
				meanStr.append(StringUtils.format(means[i]) + " " );
			}
			topPanel.setText("Mean: " + meanStr + "    Proposals: " + getCalls() + " (" + StringUtils.format( 100*getAcceptanceRate() ) + "%) ");
			topPanel.revalidate();
		}
	}
		
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
		setLayout(new BorderLayout());
		traceFigure = new XYSeriesFigure();
		add(traceFigure, BorderLayout.CENTER);
		traceFigure.setAllowMouseDragSelection(false);
		traceFigure.setXLabel("MCMC state");
		traceFigure.getAxes().setNumXTicks(4);
		traceFigure.getAxes().setNumYTicks(4);
		initializePopup();
		traceFigure.setYLabel(null);
		
		topPanel = new MonitorHeader(this);
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		topPanel.setBackground(Color.LIGHT_GRAY);
		topPanel.add(Box.createHorizontalStrut(100));
		topPanel.setText("Mean : ?   Calls: ?");
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
		 
		 final JCheckBoxMenuItem showLegendItem =new JCheckBoxMenuItem("Show legend");
		 showLegendItem.setSelected(true);
		 popup.add(showLegendItem);
		 showLegendItem.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setShowLegend(showLegendItem.isSelected());
			} 
		 });
		
		 final JCheckBoxMenuItem showBurninItem =new JCheckBoxMenuItem("Show burn in");
		 showBurninItem.setSelected(true);
		 popup.add(showBurninItem);
		 showBurninItem.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setShowBurnin(showBurninItem.isSelected());
			} 
		 });
		 
		 final JCheckBoxMenuItem showValsItem =new JCheckBoxMenuItem("Show trace");
		 showValsItem.setSelected(true);
		 popup.add(showValsItem);
		 showValsItem.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setShowValues(showValsItem.isSelected());
			} 
		 });
		 
		 final JCheckBoxMenuItem showMeansItem =new JCheckBoxMenuItem("Show means");
		 showMeansItem.setSelected(true);
		 popup.add(showMeansItem);
		 showMeansItem.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setShowMeans(showMeansItem.isSelected());
			} 
		 });
		 
		 final JCheckBoxMenuItem showStdevsItem = new JCheckBoxMenuItem("Show std. devs.");
		 showStdevsItem.setSelected(true);
		 popup.add(showStdevsItem);
		 showStdevsItem.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setShowStdevs(showStdevsItem.isSelected());
			} 
		 });
		 
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
		if (mode == Mode.TRACE && switchItem.getText().contains("histogram")) { //Current mode is trace, so switch it to histogram
			mode = Mode.HISTOGRAM; //We're now in histogram mode
			traceFigure.removeAllSeries();
			createHistograms();

			for(int i=0; i<series.length; i++) {
				XYSeriesElement el = traceFigure.addDataSeries(histoSeries[i]);
				el.setMode(XYSeriesElement.BOXES);				
			}
			//traceFigure.placeBoxSeries();
			switchItem.setText("Switch to trace");
			traceFigure.setXLabel(titles[0]);
		}
		else { //Current mode is histogram, so we're switching back to trace
			if (switchItem.getText().contains("trace")) {
				mode = Mode.TRACE;
				redrawSeries();
				switchItem.setText("Switch to histogram");
				traceFigure.setXLabel("MCMC State");
			}
		}
		

		traceFigure.inferBoundsPolitely();
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
//	public void thinSeries() {
//		for(int j=0; j<series.length; j++) {
//			XYSeries ser = series[j];
//
//			//We're actually incrementing the index removed by two, since removing a point increments the index by one itself
//			for(int i=0; i<ser.size()/2; i++) {
//				ser.removePoint(i);
//			}
//		}
//	}
	
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
	
	
//	public void paintComponent(Graphics g) {
//		Graphics2D g2d = (Graphics2D)g;
//		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//                RenderingHints.VALUE_ANTIALIAS_ON);
//
//		super.paintComponent(g);
//		
//		GradientPaint paint1 = new GradientPaint(new Point2D.Double(getWidth()-5, 5), Color.gray, new Point2D.Double(getWidth()-1, 5), new Color(0.9f, 0.9f, 0.9f, 0.5f));
//		g2d.setPaint(paint1);
//		g2d.fillRoundRect(getWidth()-5, 6, 5, getHeight()-8, 4, 4);
//		
//		GradientPaint paint2 = new GradientPaint(new Point2D.Double(5, getHeight()-5), Color.gray, new Point2D.Double(5, getHeight()), new Color(0.9f, 0.9f, 0.9f, 0.5f));
//		g2d.setPaint(paint2);
//		g2d.fillRoundRect(3, getHeight()-5, getWidth()-6, 5, 4, 4);
//
//	}
	
	static JFileChooser fileChooser;
	private JMenuItem histoOptions;
	private JMenuItem switchItem;
	private JPopupMenu popup;
	private MonitorHeader topPanel;

	
	protected float defaultLineWidth = 1.1f;	
}
