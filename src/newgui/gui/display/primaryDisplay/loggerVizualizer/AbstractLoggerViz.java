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
import newgui.gui.widgets.AbstractFigurePanel;
import newgui.gui.widgets.AbstractSeriesPanel;
import newgui.gui.widgets.BorderlessButton;

import logging.PropertyLogger;

/**
 * LoggerViz objects visualize / display the output from a logger, typically a PropertyLogger. This
 * may take slightly different forms depending on the logger type, but often involve displaying a 1-D or
 * 2-D histogram of some sort.  
 * @author brendano
 *
 */
public abstract class AbstractLoggerViz extends AbstractSeriesPanel implements ActionListener {

	protected PropertyLogger logger = null;
	
	public AbstractLoggerViz(PropertyLogger logger) {
		super();
		this.logger = logger;
		timer = new Timer(getUpdateFrequency(), this);
		//initComponents();
		timer.start();
	}

	public AbstractLoggerViz() {
		super();
		timer = new Timer(getUpdateFrequency(), this);
		//initComponents();
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
	
	private boolean updating = false; 
	protected Timer timer;
	
}
