package newgui.gui.modelViews;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import gui.ACGFrame;
import gui.ErrorWindow;
import gui.document.ACGDocument;
import gui.inputPanels.ARGModelElement;
import gui.inputPanels.Configurator.InputConfigException;


import gui.inputPanels.loggerConfigs.LoggerModel;
import gui.inputPanels.loggerConfigs.StateLoggerModel;
import gui.widgets.BorderlessButton;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import logging.PropertyLogger;
import logging.StateLogger;

import newgui.gui.modelViews.loggerViews.AvailableLoggers;
import newgui.gui.modelViews.loggerViews.StateLoggerView;

import org.w3c.dom.Element;

import xml.XMLLoader;


public class LoggersView extends JPanel {

	private List<LoggerWrapper> loggers = new ArrayList<LoggerWrapper>();
	private AddLoggersFrame addFrame;
	private JButton addButton;
	
	//Reference to ARG object that may be used by loggers
	//This implementation sucks because what if different loggers want to reference different ARGs?
	//Maybe there should be one logger panel per ARG? per alignment? 
	protected ARGModelElement ARGref = null;
	
	static final ImageIcon removeIcon = ACGFrame.getIcon("inputPanels/loggerConfigs/icons/removeButton.png");
	
	public LoggersView() {
		this.setOpaque(false);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);
		
		addFrame = new AddLoggersFrame(this);
		addFrame.setVisible(false);
		
		addButton = new JButton("Add logger");
		addButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAddFrame();
			}
		});
		add(addButton);
		
		//Must come after above initialization
		addLogger( new StateLoggerView(new StateLoggerModel()) );
	}
	
	protected void showAddFrame() {
		addFrame.setVisible(true);
		
	}
	
	public void setARGReference(ARGModelElement argRef) {
		this.ARGref = argRef;
	}
	
	/**
	 * Clear list of logger views and add brand new ones created from the given
	 * list of models
	 * @param newModels
	 */
	public void setLoggerModels(List<LoggerModel> newModels) {
		loggers.clear();
		for(LoggerModel model : newModels) {
			addLogger(AvailableLoggers.createDefaultViewForModel(model));
		}
	}
	
	public void addLogger(DefaultLoggerView logger) {
		LoggerWrapper wrapped = new LoggerWrapper(logger);
		loggers.add(wrapped);
		layoutLoggers();
	}
	
	/**
	 * Force a re-layout of the components in this view, mostly
	 * by removing all components and then adding them back in
	 * This should be called after any change to the loggers field 
	 */
	protected void layoutLoggers() {
		this.removeAll();		
		this.add(addButton);
		
		for(LoggerWrapper wrapper : loggers) {
			add(wrapper);
			if (wrapper != loggers.get(loggers.size()-1))
				add(new JSeparator(JSeparator.HORIZONTAL));
		}
		
		
		revalidate();
		repaint();
	}
	
	public void removeLogger(DefaultLoggerView which) {
		LoggerWrapper toRemove = null;
		for(LoggerWrapper logger : loggers) {
			if (logger.config == which) {
				toRemove = logger;
			}
		}
		
		if (toRemove != null) {
			remove(toRemove);
			loggers.remove(toRemove);				
		}
		
		layoutLoggers();
	}
	
	
	public List<Element> getLoggerNodes(ACGDocument doc) throws InputConfigException {
		List<Element> nodes = new ArrayList<Element>();
		
		for(LoggerWrapper logger : loggers) {
			DefaultLoggerView view = logger.config;
			view.updateFields();
			view.getModel().setArgRef( ARGref );
			Element loggerElement;
			loggerElement = (Element) view.getModel().getElements(doc).get(0);
			nodes.add( loggerElement );
		}
		
		return nodes;
	}
	
	public List<LoggerModel> getLoggerModels() throws InputConfigException {
		List<LoggerModel> models = new ArrayList<LoggerModel>();

		for(LoggerWrapper logger : loggers) {
			DefaultLoggerView view = logger.config;
			view.updateFields();
			view.getModel().setArgRef( ARGref );
			models.add(view.getModel());
		}
		
		return models;
	}
	
	public void removeAllLoggers() {
		for(LoggerWrapper wrapper : loggers) {
			remove(wrapper);
		}
		loggers.clear();
		revalidate();
		repaint();
	}
	
	public void readNodesFromDocument(ACGDocument doc) {
		removeAllLoggers();
		List<String> docLoggers = doc.getLabelForClass(PropertyLogger.class);
		docLoggers.addAll( doc.getLabelForClass(StateLogger.class));
		
		for(String loggerLabel : docLoggers) {
			Element el = doc.getElementForLabel(loggerLabel);
			String className = el.getAttribute(XMLLoader.CLASS_NAME_ATTR);
			DefaultLoggerView view = addFrame.getViewForClass( className );
			try {
				if (view!= null) {
					view.getModel().readElements(doc);
					view.updateView();
					addLogger(view);
				}

			} catch (InputConfigException e) {
				ErrorWindow.showErrorWindow(e);
			}
			
		}
		
	}
	
	/**
	 * Something to wrap logger views and add a remove button to them
	 * @author brendano
	 *
	 */
	class LoggerWrapper extends JPanel {
		
		DefaultLoggerView config;
		
		public LoggerWrapper(final DefaultLoggerView conf) {
			this.config = conf;
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setOpaque(false);
			setPreferredSize(conf.getPreferredDimensions());
			setMaximumSize(conf.getPreferredDimensions());
			setAlignmentX(Component.LEFT_ALIGNMENT);
			setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
			conf.setAlignmentY(TOP_ALIGNMENT);
			add(conf);
			add(Box.createHorizontalGlue());
			
			BorderlessButton remove = new BorderlessButton(removeIcon);
			remove.setAlignmentY(TOP_ALIGNMENT);
			remove.setToolTipText("Remove " + conf.getModel().getModelLabel() );
			remove.setXDif(-2);
			remove.setYDif(-2);
			remove.setMinimumSize(new Dimension(24, 30));
			remove.setPreferredSize(new Dimension(24, 30));
			
			remove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeLogger(conf);
				}
			});
			add(remove);
		}
		
				
	}
	
	
}
