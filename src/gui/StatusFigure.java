package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import component.LikelihoodComponent;
import gui.figure.series.HistogramSeries;
import gui.figure.series.XYSeries;
import gui.figure.series.XYSeriesElement;
import gui.figure.series.XYSeriesFigure;
import parameter.AbstractParameter;
import parameter.DoubleParameter;

public class StatusFigure extends JPanel {

	XYSeriesFigure traceFigure;
	XYSeries[] series;
	HistogramSeries[] histoSeries;
	
	String logKey = null;
	
	public StatusFigure(AbstractParameter<?> param, String logKey) {
		this.param = param;
		this.logKey = logKey;
		initializeFigure();
		//This ALL NEEDS TO BE REFACTORED BADLY! BUT how do we know if things should be grouped together, or not? 
		String[] logKeyItems = logKey.split("\\t");
		
		
		String logStr = (param.getLogItem(logKey)).toString();
		String[] logToks = logStr.split("\\t");
		series = new XYSeries[logToks.length];
		for(int i=0; i<Math.min(logToks.length, logKeyItems.length); i++) {
			String displayName = logKeyItems[i];
			if (i>0)
				displayName = logKeyItems[i] + "(" + i + ")";
			series[i] = new XYSeries(displayName);
			XYSeriesElement serEl = traceFigure.addDataSeries(series[i]);
			serEl.setLineWidth(defaultLineWidth);	
		}
	}
	
	public StatusFigure(AbstractParameter<?> param) {
		this.param = param;
		Object t = param.getValue();
		
		initializeFigure();
		if (t instanceof Double) {
			series = new XYSeries[1];
			series[0] = new XYSeries(param.getName());
			XYSeriesElement serEl = traceFigure.addDataSeries(series[0]);
			serEl.setLineWidth(defaultLineWidth);	
		}
		else {
			if (t instanceof double[]) {
				//also fine, but we'll draw multiple lines
				double[] vals = (double[])t;
				series = new XYSeries[ vals.length ];
				for(int i=0; i<vals.length; i++) {
					series[i] = new XYSeries(param.getName() + "(" + i + ")");
					XYSeriesElement serEl = traceFigure.addDataSeries(series[i]);
					serEl.setLineWidth(defaultLineWidth);	
				}
			}
			else {
				if (t instanceof Integer) {
					//OK
					//We could theoretically do an array of integers?
					series = new XYSeries[1];
				}
				else {
					throw new IllegalArgumentException("Can't create a Figure for parameter with type " + t.getClass());
				}
			}
		}
	}
	
	public StatusFigure(LikelihoodComponent comp) {
		this.comp = comp;
		initializeFigure();
		series = new XYSeries[1];
		series[0] = new XYSeries(comp.getLogHeader());
		XYSeriesElement serEl = traceFigure.addDataSeries(series[0]);
		serEl.setLineWidth(defaultLineWidth);
	}
	
	private void initializeFigure() {
		this.setLayout(new BorderLayout());
		traceFigure = new XYSeriesFigure();
		this.add(traceFigure, BorderLayout.CENTER);
		traceFigure.setAllowMouseDragSelection(false);
		traceFigure.setXLabel("MCMC state");
		traceFigure.getAxes().setNumXTicks(4);
		traceFigure.getAxes().setNumYTicks(4);
		initializePopup();
		traceFigure.setYLabel(null);
	}
	
	private void initializePopup() {
		 popup = new JPopupMenu();
		 JMenuItem switchItem = new JMenuItem("Switch to histogram");
		 switchItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchHistoTrace();
			}
		 });
		 popup.add(switchItem);
		 
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
	
	

	protected void saveImage() {
		// TODO Auto-generated method stub
		
	}

	protected void switchHistoTrace() {
		if (histoSeries == null) {
			traceFigure.removeAllSeries();
			histoSeries = new HistogramSeries[series.length];
			for(int i=0; i<series.length; i++) {
				HistogramSeries hSeries = new HistogramSeries("Density", series[i].getPointList(), 100, series[i].getMinX()*0.8, series[i].getMaxX()*1.5);
				histoSeries[i] = hSeries;
				traceFigure.addDataSeries(hSeries);
			}
		}
		else {
			traceFigure.removeAllSeries();
			for(int i=0; i<series.length; i++) {
				traceFigure.addDataSeries(series[i]);
			}
			histoSeries = null;
		}
		
		traceFigure.inferBoundsFromCurrentSeries();
		traceFigure.repaint();
	}

	public void update(int state) {
		if (param != null) {
			String logStr = (param.getLogItem(logKey)).toString();
			String[] logToks = logStr.split("\\t");
			for(int i=0; i<logToks.length; i++) {
				try {
					Double val = Double.parseDouble(logToks[i]);
					Point2D.Double point = new Point2D.Double(state, val);
					series[i].addPointInOrder(point);
					if (histoSeries != null) 
						histoSeries[i].addValue(val);
				}
				catch (NumberFormatException nex) {
					//don't worry about it
				}
			}
		}
		else {
			Double val = comp.getCurrentLogLikelihood();
			Point2D.Double point = new Point2D.Double(state, val);
			series[0].addPointInOrder(point);
			if (histoSeries != null)
				histoSeries[0].addValue(val);
		}
		traceFigure.inferBoundsPolitely();
		repaint();
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
	
	private JPopupMenu popup;
	private AbstractParameter<?> param = null;
	private LikelihoodComponent comp = null;
	
	private float defaultLineWidth = 1.1f;
}
