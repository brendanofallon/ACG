package newgui.gui.display.primaryDisplay.loggerVizualizer;

import gui.ErrorWindow;
import gui.figure.series.XYSeriesFigure;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import newgui.UIConstants;
import newgui.gui.widgets.BorderlessButton;

import logging.PropertyLogger;

/**
 * LoggerViz objects vizualize / display the output from a logger, typically a PropertyLogger. This
 * may take slightly different forms depending on the logger type, but often involve displaying a 1-D or
 * 2-D histogram of some sort.  
 * @author brendano
 *
 */
public abstract class AbstractLoggerViz extends JPanel implements ActionListener {

	protected PropertyLogger logger = null;
	
	public AbstractLoggerViz(PropertyLogger logger) {
		this.logger = logger;
		timer = new Timer(getUpdateFrequency(), this);
		initComponents();
		timer.start();
	}

	public AbstractLoggerViz() {
		timer = new Timer(getUpdateFrequency(), this);
		initComponents();
	}
	
	/**
	 * Timing frequency for update timer, in millis. Default is to
	 * fire twice each second. Some methods may want to slow this down. 
	 * @return
	 */
	protected int getUpdateFrequency() {
		return 500;
	}
	
	/**
	 * Set the PropertyLogger this object will display, start the Timer, and 
	 * delegate initialize() to subclasses
	 * @param logger
	 */
	public void initialize(PropertyLogger logger) {
		initialize(logger, true);
	}
	
	public void initialize(PropertyLogger logger, boolean startTimer) {
		this.logger = logger;
		if (startTimer)
			timer.start();
		initialize();
	}
	
	public abstract void initialize();
	
	/**
	 * Update UI components to reflect new values of logger data 
	 */
	public abstract void update();
	
	/**
	 * If updating takes a long time, then we may call this multiple times before 
	 * the first one returns. To avoid this, set a boolean flag to indicate when
	 * we're performing an update, and don't actually call the method if the
	 * previous one hasn't finished. 
	 */
	public synchronized void updateViz() {
		updating = true;
		update();
		updating = false;
	}
	
	/**
	 * Called when timer fires, we don't do much besides call .update() here
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (! updating) {
			updateViz();
			if (logger.isDoneCollecting()) {
				timer.stop();
			}
			
		}
		else {
			System.out.println("Aborting update attempt for logger : " + this.getClass().getCanonicalName());
		}
	}
	
	/**
	 * Initialize some basic components
	 */
	protected void initComponents() {
		setOpaque(false);
		setLayout(new BorderLayout());
		
		fig = new XYSeriesFigure();
		this.add(fig, BorderLayout.CENTER);
		
		bottomPanel = new JPanel();
		bottomPanel.setBackground(Color.white);
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		bottomPanel.add(Box.createHorizontalGlue());
		
		BorderlessButton exportDataButton = new BorderlessButton(UIConstants.writeData);
		exportDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				exportData();
			}
		});
		bottomPanel.add(exportDataButton);
		
		BorderlessButton saveImageButton = new BorderlessButton(UIConstants.saveGrayButton);
		saveImageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveImage();
			}
		});
		bottomPanel.add(saveImageButton);
		bottomPanel.add(Box.createHorizontalStrut(10));
		
		this.add(bottomPanel, BorderLayout.NORTH);
	}
	
	/**
	 * Obtain a string representing the data of in this logger 
	 * @return
	 */
	public abstract String getDataString();
	
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
	 * Create an image of the current figure and open a dialog allowig the user to save the image to a file
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


	static JFileChooser fileChooser;
	private boolean updating = false; 
	protected Timer timer;
	private JPanel bottomPanel;
	protected XYSeriesFigure fig;
}
