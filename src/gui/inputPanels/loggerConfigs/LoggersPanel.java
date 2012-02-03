/********************************************************************
*
* 	Copyright 2011 Brendan O'Fallon
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
***********************************************************************/


package gui.inputPanels.loggerConfigs;

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
import gui.widgets.BorderlessButton;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import logging.PropertyLogger;
import logging.StateLogger;

import newgui.gui.modelViews.LoggerReceiver;

import org.w3c.dom.Element;

import xml.XMLLoader;


public class LoggersPanel extends JPanel implements LoggerReceiver {

	List<LoggerWrapper> loggers = new ArrayList<LoggerWrapper>();
	
	JButton addButton;
	
	//Reference to ARG object that may be used by loggers
	//This implementation sucks because what if different loggers want to reference different ARGs?
	//Maybe there should be one logger panel per ARG? per alignment? 
	ARGModelElement ARGref = null;
	
	static final ImageIcon removeIcon = ACGFrame.getIcon("inputPanels/loggerConfigs/icons/removeButton.png");
	
	public LoggersPanel() {
		this.setOpaque(false);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);
		
		addFrame = new AddLoggerFrame(this);
		addFrame.setVisible(false);
		
		addButton = new JButton("Add logger...");
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
			addLogger(AvailableLoggers.createViewForModel(model));
		}
	}
	
	public void addLogger(AbstractLoggerView logger) {
		LoggerWrapper wrapped = new LoggerWrapper(logger);
		wrapped.setAlignmentX(Component.LEFT_ALIGNMENT);
		loggers.add(wrapped);
		this.remove(addButton);
		add(wrapped);
		this.add(addButton);
		revalidate();
		repaint();
	}
	
	public void removeLogger(AbstractLoggerView which) {
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
		revalidate();
		repaint();
	}
	
	
	public List<Element> getLoggerNodes(ACGDocument doc) throws InputConfigException {
		List<Element> nodes = new ArrayList<Element>();
		
		for(LoggerWrapper logger : loggers) {
			AbstractLoggerView view = logger.config;
			view.updateFields();
			view.getModel().setArgRef( ARGref );
			Element loggerElement;
			loggerElement = view.getModel().getElement(doc);
			nodes.add( loggerElement );
		}
		
		return nodes;
	}
	
	public List<LoggerModel> getLoggerModels() throws InputConfigException {
		List<LoggerModel> models = new ArrayList<LoggerModel>();

		for(LoggerWrapper logger : loggers) {
			AbstractLoggerView view = logger.config;
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
			AbstractLoggerView view = addFrame.getViewForClass( className );
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
		
		AbstractLoggerView config;
		
		public LoggerWrapper(final AbstractLoggerView conf) {
			this.config = conf;
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setOpaque(false);
			setPreferredSize(conf.getPreferredDimensions());
			setMaximumSize(new Dimension(32000, (int)conf.getPreferredDimensions().getHeight()));
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
	

	
	private AddLoggerFrame addFrame;
	
}
