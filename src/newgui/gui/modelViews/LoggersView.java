package newgui.gui.modelViews;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;



import gui.loggerConfigs.LoggerModel;
import gui.loggerConfigs.StateLoggerModel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import logging.PropertyLogger;
import logging.StateLogger;

import net.miginfocom.swing.MigLayout;
import newgui.ErrorWindow;
import newgui.UIConstants;
import newgui.gui.modelElements.ARGModelElement;
import newgui.gui.modelElements.PopSizeModelElement;
import newgui.gui.modelElements.Configurator.InputConfigException;
import newgui.gui.modelViews.loggerViews.AvailableLoggers;
import newgui.gui.modelViews.loggerViews.DefaultLoggerView;
import newgui.gui.modelViews.loggerViews.StateLoggerView;
import newgui.gui.widgets.BorderlessButton;

import org.w3c.dom.Element;

import document.ACGDocument;

import xml.XMLLoader;


public class LoggersView extends JPanel {

	private List<DefaultLoggerView> loggers = new ArrayList<DefaultLoggerView>();
	private AddLoggersFrame addFrame;
	private BorderlessButton addButton;
	
	private JScrollPane scrollPane;
	private JPanel mainPanel;
	
	ImageIcon addIcon = UIConstants.getIcon("gui/icons/addButton.png");
	
	//Reference to ARG object that may be used by loggers
	//This implementation sucks because what if different loggers want to reference different ARGs?
	//Maybe there should be one logger panel per ARG? per alignment? 
	protected ARGModelElement ARGref = null;
	protected PopSizeModelElement popSizeref = null;
		
	public LoggersView() {
		this.setOpaque(false);
		this.setLayout(new BorderLayout());
		
		mainPanel = new JPanel();
		mainPanel.setOpaque(false);
		scrollPane = new JScrollPane(mainPanel);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		add(scrollPane, BorderLayout.CENTER);
		
		//mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setLayout(new MigLayout());
		mainPanel.setOpaque(false);
		
		addFrame = new AddLoggersFrame(this);
		addFrame.setVisible(false);
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		topPanel.setOpaque(false);
		addButton = new BorderlessButton("Add logger", addIcon);
		addButton.setTextPosition(BorderlessButton.TEXT_RIGHT);
		addButton.setHorizontalTextAlignment(LEFT_ALIGNMENT);
		addButton.setPreferredSize(new Dimension(120, 32));
		addButton.setMinimumSize(new Dimension(120, 22));
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAddFrame();
			}
		});
		topPanel.add(addButton);
		add(topPanel, BorderLayout.NORTH);
		
		//Must come after above initialization
		addLogger( new StateLoggerView(new StateLoggerModel()) );
	}
	
	protected void showAddFrame() {
		addFrame.setVisible(true);
		
	}
	
	/**
	 * Set the state of all logger views to 'closed'
	 */
	public void closeAllLoggers() {
		for (DefaultLoggerView logger : loggers) {
			logger.setClosed();
		}
		layoutLoggers();
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
		logger.setLoggerParent(this);
		loggers.add(logger);
		logger.setOpen();
		layoutLoggers();
	}
	
	/**
	 * Force a re-layout of the components in this view, mostly
	 * by removing all components and then adding them back in
	 * This should be called after any change to the loggers field 
	 */
	public  void layoutLoggers() {
		mainPanel.removeAll();	
		
		for(DefaultLoggerView view : loggers) {
			mainPanel.add(view, "wrap");
			if (view != loggers.get(loggers.size()-1)) {
				JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
				sep.setMinimumSize(new Dimension(view.getPreferredDimensionsLarge().width, 10));
				//sep.setBorder(BorderFactory.createLineBorder(Color.RED));
				mainPanel.add(sep, "wrap");
			}
		}
		
	
		mainPanel.revalidate();
		repaint();
	}
	
	public void removeLogger(DefaultLoggerView which) {
		loggers.remove(which);				
		layoutLoggers();
	}
	
	/**
	 * Push changes from UI to all logger models 
	 * @throws InputConfigException 
	 */
	public void updateModels() throws InputConfigException {
		for(DefaultLoggerView view : loggers) {
			view.updateFields();
			view.getModel().setArgRef( ARGref );
			view.getModel().setPopSizeRef( popSizeref );
		}
	}
	
	public List<LoggerModel> getLoggerModels() throws InputConfigException {
		List<LoggerModel> models = new ArrayList<LoggerModel>();

		for(DefaultLoggerView view : loggers) {
			view.updateFields();
			view.getModel().setArgRef( ARGref );
			view.getModel().setPopSizeRef( popSizeref );
			models.add(view.getModel());
		}
		
		return models;
	}
	
	public void removeAllLoggers() {
		loggers.clear();
		layoutLoggers();
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
	
		closeAllLoggers();
	}
	
	
	
}
