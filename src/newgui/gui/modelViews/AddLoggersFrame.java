package newgui.gui.modelViews;

import gui.inputPanels.loggerConfigs.LoggerModel;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;


import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import newgui.gui.modelViews.LoggerReceiver;
import newgui.gui.modelViews.loggerViews.AvailableLoggers;
import newgui.gui.modelViews.loggerViews.LoggerItemRenderer;

import logging.StateLogger;

/**
 * A frame that allows users to pick from a variety of different loggers. There's not really
 * a clean model / view separation here - we just store the list of all available views in a list
 * (which is also created in this class), 
 * @author brendan
 *
 */
public class AddLoggersFrame extends JFrame {

	LoggersView parentPanel;
	List<DefaultLoggerView> loggerList = new ArrayList<DefaultLoggerView>();
	
//	public AddLoggerFrame(LoggersView parentPanel) {
//		
//	}
	
	public AddLoggersFrame(LoggersView parentPanel) {
		super("Choose loggers");
		this.parentPanel = parentPanel;
		initialize();
	}

	private void initialize() {
		
		this.setLayout(new BorderLayout());
				
		//Build list of loggers
		importLoggers();
		
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		JScrollPane scrollPane = new JScrollPane(listPanel);
		
		for(DefaultLoggerView logger : loggerList) {
			LoggerItemRenderer renderer = new LoggerItemRenderer(logger, this);
			listPanel.add(renderer);
			listPanel.add(Box.createVerticalStrut(3));
		}
		
		add(scrollPane, BorderLayout.CENTER);
		this.getRootPane().setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		pack();
		if (parentPanel instanceof JComponent)
			setLocationRelativeTo( (JComponent)parentPanel );
		else
			setLocationRelativeTo(null);
		setVisible(false);	
	}
	
	/**
	 * Build the list of available loggers to be represented in this frame
	 */
	private void importLoggers() {
		List<LoggerModel> models = AvailableLoggers.getLoggers();
		for(LoggerModel model : models) {
			loggerList.add( AvailableLoggers.createDefaultViewForModel(model));
		}
	}
	
	/**
	 * Obtain the view associated with the given class, or null if there is not one
	 * @param clazz
	 * @return
	 */
	public DefaultLoggerView getViewForClass(String canonicalName) {
		for(DefaultLoggerView view : loggerList){
			if (view.getModel().getLoggerClass().getCanonicalName().equals( canonicalName )) {
				return view;
			}
		}
		return null;
	}
	
	/**
	 * Return the logger model associated with the given class
	 * @param canonicalName
	 * @return
	 */
	public LoggerModel getLoggerModelForClass(String canonicalName) {
		DefaultLoggerView view = getViewForClass(canonicalName);
		if (view == null)
			return null;
		else
			return view.getModel();
	}

	/**
	 * Adds the given logger to the LoggersPanel associated with this frame
	 * @param config
	 */
	public void addLoggerToPanel(DefaultLoggerView config) {
		parentPanel.addLogger(config);
	}

	
}

