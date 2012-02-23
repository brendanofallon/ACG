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
import newgui.gui.widgets.BorderlessButton;
import newgui.gui.widgets.HighlightButton;

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
		chooseBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent iv) {
				fig.removeAllSeries();
				addSelectedSeries();
				fig.repaint();
			}
		});
		bottomPanel.add(chooseBox);
		bottomPanel.add(Box.createHorizontalGlue());
		
//		bottomPanel.add(Box.createHorizontalStrut(10));
//		bottomPanel.add(Box.createHorizontalGlue());
//		clearButton = new BorderlessButton(UIConstants.clearButton);
//		clearButton.setXDif(-1);
//		clearButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				fig.removeAllSeries();
//				fig.repaint();
//			}
//		});
//		clearButton.setToolTipText("Remove all series");
//		bottomPanel.add(clearButton);
//		
//		
//		addButton = new BorderlessButton(UIConstants.addButton);
//		addButton.setToolTipText("Add new series");
//		addButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent arg0) {
//				addSelectedSeries();
//			}
//		});
//		bottomPanel.add(addButton);
		
		HighlightButton saveButton = new HighlightButton(UIConstants.saveGrayButton, UIConstants.saveBlueButton);
		saveButton.setToolTipText("Save image");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveImage();
			}
		});
		bottomPanel.add(saveButton);
		
		HighlightButton removeButton = new HighlightButton(UIConstants.grayCloseButton, UIConstants.redCloseButton);
		removeButton.setToolTipText("Remove this panel");
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeThisFigure();
			}
		});
		bottomPanel.add(removeButton);
		
//		HighlightButton histoButton = new HighlightButton(UIConstants.grayHistogram, UIConstants.blueHistogram);
//		histoButton.setToolTipText("Switch to histogram view");
//		histoButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				switchToHistogram();
//			}
//		});
//		bottomPanel.add(histoButton);
		
		final ImageIcon histoSwitchIconLeft = UIConstants.getIcon("gui/icons/histoSwitcherLeft.png");
		final ImageIcon histoSwitchIconRight = UIConstants.getIcon("gui/icons/histoSwitcherRight.png");
		
		final BorderlessButton histoSwitchButton = new BorderlessButton(histoSwitchIconLeft);
		histoSwitchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (showHistogram) {
					showHistogram = false;
					histoSwitchButton.setIcon(histoSwitchIconLeft);
				}
				else {
					showHistogram = true;
					histoSwitchButton.setIcon(histoSwitchIconRight);	
				}
				switchHistogramTrace();
			}
			
		});
		bottomPanel.add(histoSwitchButton);
		
		bottomPanel.add(Box.createHorizontalGlue());
		bottomPanel.revalidate();
		bottomPanel.repaint();
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
			addSeries(histo);
			
			fig.setYLabel("Frequency");
			fig.setXLabel("Value");
		}
		else {
			addSelectedSeries();
		}
		
		
		fig.inferBoundsFromCurrentSeries();
	}

	
	
	/**
	 * Create an image of the figure and save it to a file (right now, always in .png format)
	 */
	protected void saveImage() {
		BufferedImage image = fig.getImage();

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

	protected void removeThisFigure() {
		if (parentPanel != null) {
			parentPanel.removeFigure(this);
		}
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
			addSeries(histo);
			
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

	/**
	 * Add a single series with the default series color (blue)
	 * @param series
	 */
	private void addSeries(XYSeries series) {
		addSeries(series, Color.blue);
	}
	
	/**
	 * Adds a new series for this figure to draw, using the line color indicated
	 * @param name
	 * @param series
	 */
	private void addSeries(XYSeries series, Color seriesColor) {
		XYSeriesElement element = new XYSeriesElement(series, fig.getAxes(), fig);
		fig.addSeriesElement(element);
		element.setLineColor(seriesColor);
		element.setLineWidth(1.25f);
		element.setCanConfigure(true);
		
		if (series instanceof HistogramSeries) {
			element.setMode(XYSeriesElement.BOXES);
		}
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
		if (memLogger.getBurninExceeded() && burninMessage != null) {
			fig.removeElement(burninMessage);
			burninMessage = null;
		}
		fig.inferBoundsPolitely();
		fig.repaint();
	}
	
	private JFileChooser fileChooser;
	private JPanel bottomPanel;
	private JComboBox chooseBox;
	private JLabel topLabel;
	private Timer repaintTimer = null;
	private XYSeriesFigure fig; //Actually draws series, graph axes, etc

	TextElement burninMessage = null;
	private boolean showHistogram = false; //If true, draw histogram instead of trace
}
