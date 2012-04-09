package newgui.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import gui.ErrorWindow;
import gui.figure.series.AbstractSeries;
import gui.figure.series.UnifiedConfigFrame;
import gui.figure.series.XYSeries;
import gui.figure.series.XYSeriesElement;
import gui.figure.series.XYSeriesFigure;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import newgui.UIConstants;
import newgui.datafile.XMLConversionError;
import newgui.datafile.resultsfile.XYSeriesInfo;

/**
 * A generic panel that contains an XYSeriesFigure and a toolbar with a few useful buttons
 * @author brendan
 *
 */
public abstract class AbstractSeriesPanel extends JPanel {

	protected XYSeriesFigure fig = new XYSeriesFigure();
	protected static JFileChooser fileChooser = null;
	
	public AbstractSeriesPanel() {
		initComponents();
	}
	
	/**
	 * Obtain a string representing the currently displayed series
	 * @return
	 */
	protected abstract String getDataString();

	/**
	 * Obtain the figure used to draw the data
	 * @return
	 */
	public XYSeriesFigure getFigure() {
		return fig;
	}
	
	/**
	 * Remove all series from the current figure
	 */
	public void clearSeries() {
		fig.removeAllSeries();
	}
	
	public void setXLabel(String xLabel) {
		fig.setXLabel(xLabel);
		fig.repaint();
	}
	
	public void setYLabel(String yLabel) {
		fig.setYLabel(yLabel);
		fig.repaint();
	}
	
	/**
	 * Add the series to those drawn by the figure
	 * @param series
	 */
	public XYSeriesElement addSeries(XYSeries series) {
		XYSeriesElement el = fig.addDataSeries(series);
		repaint();
		return el;
	}
	
	/**
	 * Add the given series to the figure and set its color to the given color
	 * @param series
	 * @param col
	 * @return
	 */
	public XYSeriesElement addSeries(XYSeries series, Color col) {
		XYSeriesElement el = fig.addDataSeries(series);
		el.setLineColor(col);
		repaint();
		return el;
	}
	
	/**
	 * Add the given series to the figure and set its color and width to the given params
	 * @param series
	 * @param col
	 * @return
	 */
	public XYSeriesElement addSeries(XYSeries series, Color col, float width) {
		XYSeriesElement el = fig.addDataSeries(series);
		el.setLineColor(col);
		el.setLineWidth(width);
		repaint();
		return el;
	}
	
	
	/**
	 * Add the given component to the options panel (really a toolbar for this figure)
	 * @param comp
	 */
	public void addOptionsComponent(JComponent comp) {
		optionsPanel.add(comp);
		optionsPanel.revalidate();
		optionsPanel.repaint();
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

	/**
	 * Write the data in the current series to a file
	 */
	protected void exportData() {
		String data = getDataString();
		if (fileChooser == null)
			fileChooser = new JFileChooser( System.getProperty("user.dir"));
    	int val = fileChooser.showSaveDialog(this);
    	if (val==JFileChooser.APPROVE_OPTION) {
    		File file = fileChooser.getSelectedFile();
    		BufferedWriter writer;
			try {
				writer = new BufferedWriter(new FileWriter(file));
	    		writer.write(data);
	    		writer.close();
			} catch (IOException e) {
				ErrorWindow.showErrorWindow(e, "Error writing data to file : " + e.getMessage());
				e.printStackTrace();
			}
    	}		
	}
	
	
	protected void showConfigFrame() {
		if (configFrame == null)
			configFrame = new UnifiedConfigFrame(fig);
		
		configFrame.readSettingsFromFigure();
		configFrame.setVisible(true);
	}

	private void initComponents() {
		this.setLayout(new BorderLayout());
		fig = new XYSeriesFigure();
		fig.setAllowMouseDragSelection(false);
		fig.setXLabel("State");
		fig.setYLabel("Value");
		fig.getAxes().setNumXTicks(4);
		fig.getAxes().setNumYTicks(4);
		add(fig, BorderLayout.CENTER);
		
		optionsPanel = new JPanel();
		this.add(optionsPanel, BorderLayout.NORTH);
		
		BorderlessButton showConfigButton = new BorderlessButton(UIConstants.settings);
		showConfigButton.setToolTipText("Select figure options");
		showConfigButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showConfigFrame();
			}
		});
		optionsPanel.add(showConfigButton);
		
		
		BorderlessButton exportDataButton = new BorderlessButton(UIConstants.writeData);
		exportDataButton.setToolTipText("Export raw data");
		exportDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				exportData();
			}
		});
		optionsPanel.add(exportDataButton);
		
		BorderlessButton saveButton = new BorderlessButton(UIConstants.saveGrayButton);
		saveButton.setToolTipText("Save figure image");
		saveButton.setToolTipText("Save image");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveImage();
			}
		});
		optionsPanel.add(saveButton);
	}
	
	


	protected UnifiedConfigFrame configFrame = null;
	private JPanel optionsPanel;

}
