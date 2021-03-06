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


package gui.loggerConfigs;

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

import logging.StateLogger;

/**
 * A frame that allows users to pick from a variety of different loggers. There's not really
 * a clean model / view separation here - we just store the list of all available views in a list
 * (which is also created in this class), 
 * @author brendan
 *
 */
public class AddLoggerFrame extends JFrame {

	LoggerReceiver parentPanel;
	List<AbstractLoggerView> loggerList = new ArrayList<AbstractLoggerView>();
	
//	public AddLoggerFrame(LoggersView parentPanel) {
//		
//	}
	
	public AddLoggerFrame(LoggerReceiver parentPanel) {
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
		
		for(AbstractLoggerView logger : loggerList) {
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
			loggerList.add( AvailableLoggers.createViewForModel(model));
		}
	}
	
	/**
	 * Obtain the view associated with the given class, or null if there is not one
	 * @param clazz
	 * @return
	 */
	public AbstractLoggerView getViewForClass(String canonicalName) {
		for(AbstractLoggerView view : loggerList){
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
		AbstractLoggerView view = getViewForClass(canonicalName);
		if (view == null)
			return null;
		else
			return view.getModel();
	}

	/**
	 * Adds the given logger to the LoggersPanel associated with this frame
	 * @param config
	 */
	public void addLoggerToPanel(AbstractLoggerView config) {
		parentPanel.addLogger(config);
	}

	
}
