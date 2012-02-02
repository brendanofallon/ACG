package newgui.gui.display.primaryDisplay.loggerVizualizer;

import gui.figure.series.XYSeriesFigure;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

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
		this.logger = logger;
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
		if (! updating)
			updateViz();
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
		bottomPanel.setOpaque(false);
		
		this.add(bottomPanel, BorderLayout.SOUTH);
	}
	
	private boolean updating = false; 
	protected Timer timer;
	protected JPanel bottomPanel;
	protected XYSeriesFigure fig;
}
