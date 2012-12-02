package newgui.gui.widgets;

import gui.figure.Figure;
import gui.figure.series.UnifiedConfigFrame;
import gui.figure.series.XYSeriesFigure;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import newgui.ErrorWindow;
import newgui.UIConstants;

/**
 * A JPanel that holds a figure and a small toolbar with a few options buttons, including save & export data
 * @author brendan
 *
 */
public abstract class AbstractFigurePanel extends JPanel {
	
	protected Figure fig = null;
	protected static JFileChooser fileChooser = null;
	protected JPanel optionsPanel;

	public AbstractFigurePanel() {
		initializeFigure();
		initComponents();
	}
	
	/**
	 * Create the figure and add various elements as needed
	 */
	public abstract void initializeFigure();
	
	/**
	 * Make visible a frame showing configuration options
	 */
	public abstract void showConfigFrame();
	

	/**
	 * Obtain a string representing the currently displayed series
	 * @return
	 */
	protected abstract String getDataString();
	
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

		if (image == null) {
			JOptionPane.showMessageDialog(this, "Error saving image: " + new IllegalStateException("Could not obtain image"));			
		}
		
		if (fileChooser == null)
			fileChooser = new JFileChooser( System.getProperty("user.dir"));

		int val = fileChooser.showSaveDialog(this);
		if (val==JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			if (! file.getName().endsWith(".png"))
				file = new File(file.getAbsolutePath() + ".png");
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
	
	public Figure getFigure() {
		return fig;
	}

	/**
	 * Replace the current Figure with the new one
	 * @param newFigure
	 */
	public void setFigure(Figure newFigure) {
		this.add(newFigure, BorderLayout.CENTER);
		revalidate();
		repaint();
	}
	
	protected  void initComponents() {
		this.setLayout(new BorderLayout());
		add(fig, BorderLayout.CENTER);
		
		optionsPanel = new JPanel();
		optionsPanel.setBackground(fig.getBackground());
		optionsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		optionsPanel.add(Box.createHorizontalStrut(25));
		this.add(optionsPanel, BorderLayout.SOUTH);
		
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
	
	

}
