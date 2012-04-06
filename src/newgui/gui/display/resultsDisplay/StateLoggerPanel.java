package newgui.gui.display.resultsDisplay;

import gui.ErrorWindow;
import gui.figure.TextElement;
import gui.figure.series.AbstractSeries;
import gui.figure.series.HistogramSeries;
import gui.figure.series.XYSeries;
import gui.figure.series.XYSeriesElement;
import gui.figure.series.XYSeriesFigure;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import newgui.UIConstants;
import newgui.datafile.XMLConversionError;
import newgui.datafile.resultsfile.ResultsFile;
import newgui.datafile.resultsfile.XYSeriesInfo;
import newgui.gui.widgets.BorderlessButton;
import newgui.gui.widgets.HighlightButton;

public class StateLoggerPanel extends JPanel {

	protected ResultsFile resFile;
	
	public StateLoggerPanel() {
		initComponents();
	}
	
	public void initialize(ResultsFile resFile) {
		this.resFile = resFile;

		List<String> seriesNames = resFile.getStateSeriesNames();
		String[] nameArray = seriesNames.toArray(new String[]{});
		chooseBox = new JComboBox(nameArray);
		chooseBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent iv) {
				fig.removeAllSeries();
				addSelectedSeries();
				fig.repaint();
			}
		});
		
		optionsPanel.add(chooseBox);
		addSelectedSeries();
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
	 * Obtain a string representing the currently displayed series
	 * @return
	 */
	private String getDataString() {
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
	
	/**
	 * Add the series that is currently selected in the "ChooseBox". This adds both
	 * the burn-in and the values series
	 */
	protected void addSelectedSeries() {
		fig.removeAllSeries();
		String seriesName = (String) chooseBox.getSelectedItem();
		
		XYSeriesInfo seriesInfo;
		try {
			seriesInfo = resFile.getStateSeries(seriesName);
			XYSeriesElement seriesEl = fig.addDataSeries(seriesInfo.getSeries());
			seriesEl.setLineColor(seriesInfo.getColor());
			seriesEl.setLineWidth(seriesInfo.getWidth());
		} catch (XMLConversionError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fig.inferBoundsFromCurrentSeries();
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
		
		
		BorderlessButton exportDataButton = new BorderlessButton(UIConstants.writeData);
		exportDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				exportData();
			}
		});
		optionsPanel.add(exportDataButton);
		
		BorderlessButton saveButton = new BorderlessButton(UIConstants.saveGrayButton);
		saveButton.setToolTipText("Save image");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveImage();
			}
		});
		optionsPanel.add(saveButton);
	}

	private boolean showHistogram = false;
	private JPanel optionsPanel;
	private XYSeriesFigure fig;
	private JComboBox chooseBox;
	private JFileChooser fileChooser;
}
