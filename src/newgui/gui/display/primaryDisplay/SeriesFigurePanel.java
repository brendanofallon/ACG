package newgui.gui.display.primaryDisplay;

import gui.figure.series.XYSeries;
import gui.figure.series.XYSeriesElement;
import gui.figure.series.XYSeriesFigure;
import gui.monitors.MonitorPanel;
import gui.widgets.FloatingPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import newgui.UIConstants;
import newgui.gui.widgets.BorderlessButton;

import logging.MemoryStateLogger;

import parameter.AbstractParameter;
import xml.XMLLoader;

public class SeriesFigurePanel extends FloatingPanel implements ActionListener {

	private MemoryStateLogger memLogger; //Logger that stores information about param values and likelihoods
	private MultiSeriesPanel parentPanel; //Reference to parent, used to notify when we want to remove this component 
	
	public SeriesFigurePanel() {
		initComponents();
	}
	
	public SeriesFigurePanel(MultiSeriesPanel parentPanel) {
		this();
		this.parentPanel = parentPanel;
	}
	

	/**
	 * Set the memory logger that will be used to find series information
	 * @param logger
	 */
	public void setMemoryLogger(MemoryStateLogger logger) {
		this.memLogger = logger;
		initializeBottomPanel();
	}
	
	private void initComponents() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(1, 10, 0, 0));
		//setBorder(BorderFactory.createLineBorder(Color.green));
		setOpaque(false);
		
		fig = new XYSeriesFigure();
		fig.setAllowMouseDragSelection(false);
		fig.setXLabel("State");
		fig.setYLabel("Value");
		fig.getAxes().setNumXTicks(4);
		fig.getAxes().setNumYTicks(4);
		add(fig, BorderLayout.CENTER);
		

		//Components to bottom panel are added when the memory logger is set (via initializeBottomPanel())	
		bottomPanel = new JPanel();
		bottomPanel.setBackground(fig.getBackground());
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		this.add(bottomPanel, BorderLayout.NORTH);

		
		JPanel topPanel = new JPanel();
		topPanel.setBackground(fig.getBackground());
		topLabel = new JLabel("Top label here");
		topLabel.setFont(UIConstants.sansFont);
		topPanel.add(topLabel);
		//this.add(topPanel, BorderLayout.NORTH);
		
	}
	
	public void updateTopLabel() {
		
	}

	/**
	 * Create various combo boxes, etc in bottom panel
	 */
	private void initializeBottomPanel() {

		bottomPanel.add(Box.createHorizontalStrut(10));
		List<String> seriesNames = memLogger.getSeriesNames();
		String[] nameArray = seriesNames.toArray(new String[]{});
		chooseBox = new JComboBox(nameArray);
		bottomPanel.add(chooseBox);
		
		bottomPanel.add(Box.createHorizontalStrut(10));
		bottomPanel.add(Box.createHorizontalGlue());
		clearButton = new BorderlessButton(UIConstants.clearButton);
		clearButton.setXDif(-1);
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fig.removeAllSeries();
				fig.repaint();
			}
		});
		clearButton.setToolTipText("Remove all series");
		bottomPanel.add(clearButton);
		
		
		addButton = new BorderlessButton(UIConstants.addButton);
		addButton.setToolTipText("Add new series");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addSelectedSeries();
			}
		});
		bottomPanel.add(addButton);
		
		BorderlessButton saveButton = new BorderlessButton(UIConstants.saveButton);
		saveButton.setToolTipText("Save image");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		bottomPanel.add(saveButton);
		
		BorderlessButton removeButton = new BorderlessButton(UIConstants.closeButton);
		removeButton.setToolTipText("Remove this panel");
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeThisFigure();
			}
		});
		bottomPanel.add(removeButton);
		
		
		BorderlessButton histoButton = new BorderlessButton(UIConstants.histogramButton);
		histoButton.setToolTipText("Switch to histogram view");
		histoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//removeThisFigure();
			}
		});
		bottomPanel.add(histoButton);
		
		bottomPanel.add(Box.createHorizontalGlue());
		bottomPanel.revalidate();
		bottomPanel.repaint();
	}
	
	protected void removeThisFigure() {
		if (parentPanel != null) {
			parentPanel.removeFigure(this);
		}
	}

	protected void addSelectedSeries() {
		String seriesName = (String) chooseBox.getSelectedItem();
		XYSeries series = memLogger.getSeries(seriesName);
		addSeries(series);
	}

	/**
	 * Adds a new series for this figure to draw
	 * @param name
	 * @param series
	 */
	public void addSeries(XYSeries series) {
		XYSeriesElement element = new XYSeriesElement(series, fig.getAxes(), fig);
		fig.addSeriesElement(element);
		element.setLineColor(Color.blue);
		element.setLineWidth(1.25f);
		element.setCanConfigure(true);
		this.series = series;
		fig.repaint();
		
		//Timer ticks twice a second to monitor series for changes
		if (repaintTimer == null)
			repaintTimer = new Timer(500, this);
		if (! repaintTimer.isRunning())
			repaintTimer.start();
	}
	
	/**
	 * Gets called when timer ticks so we can repaint series...
	 */
	public void actionPerformed(ActionEvent arg0) {
		fig.inferBoundsPolitely();
		fig.repaint();
	}
	
	private JPanel bottomPanel;
	private JComboBox chooseBox;
	private BorderlessButton clearButton;
	private BorderlessButton addButton;
	private BorderlessButton saveButton;
	
	private JLabel topLabel;
	private XYSeries series;
	private Timer repaintTimer = null;
	private XYSeriesFigure fig; //Actually draws series, graph axes, etc

}
